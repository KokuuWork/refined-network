package com.kokuu.refinednetwork.common.patterngrid;

import java.lang.reflect.Constructor;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridData;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 打开「无线样板终端」的界面。
 *
 * <p>关键是**复用 RS 原版的样板网格菜单**：菜单类型是 RS 自己的
 * （{@code Menus.INSTANCE.getPatternGrid()}），所以客户端会命中 RS 已注册的样板网格界面；
 * 所有 {@code PatternGrid*} 数据包也都按「当前打开的菜单」派发，于是整套交互原样可用，
 * 本模组一行界面代码都不用写。
 *
 * <p>唯一的脏活在 {@link #createServerMenu}：RS 让菜单容器与方块实体容器共享的那个构造
 * 是**包级私有**的，本模组在另一个包里，只能靠反射按参数类型取（不依赖混淆映射，参数类
 * 由我们自己的代码引用，运行时是同几个类）。
 */
public class WirelessPatternGridMenuProvider implements ExtendedMenuProvider<PatternGridData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WirelessPatternGridMenuProvider.class);

    @Nullable
    private static final Constructor<PatternGridContainerMenu> SERVER_CONSTRUCTOR = lookupServerConstructor();

    private final PatternGridBlockEntity grid;
    private final Component name;

    public WirelessPatternGridMenuProvider(final PatternGridBlockEntity grid, final Component name) {
        this.grid = grid;
        this.name = name;
    }

    @Override
    public PatternGridData getMenuData() {
        return grid.getMenuData();
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PatternGridData> getMenuCodec() {
        return PatternGridData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return name;
    }

    /**
     * RS 那个构造是否可用。打开界面前先问一句，免得玩家点了终端才收到崩溃报告。
     */
    public static boolean isAvailable() {
        return SERVER_CONSTRUCTOR != null;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        if (SERVER_CONSTRUCTOR == null) {
            throw new IllegalStateException(
                "Refined Storage 的样板网格菜单服务端构造不可用，无线样板终端无法打开"
            );
        }
        try {
            return SERVER_CONSTRUCTOR.newInstance(syncId, inventory, grid);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("无法构造样板网格菜单", e);
        }
    }

    @Nullable
    private static Constructor<PatternGridContainerMenu> lookupServerConstructor() {
        try {
            final Constructor<PatternGridContainerMenu> constructor = PatternGridContainerMenu.class
                .getDeclaredConstructor(int.class, Inventory.class, PatternGridBlockEntity.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (final NoSuchMethodException e) {
            LOGGER.error(
                "找不到 PatternGridContainerMenu(int, Inventory, PatternGridBlockEntity)，无线样板终端不可用",
                e
            );
            return null;
        }
    }
}
