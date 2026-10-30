package com.kokuu.refinednetwork.common.autocraftermanager;

import com.kokuu.refinednetwork.common.virtual.VirtualMenuHolder;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerData;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * 打开「无线自动合成管理器」的界面。
 *
 * <p>比样板终端省事：菜单**直接委托** {@link AutocrafterManagerBlockEntity#createMenu}——
 * 那是 public 的，不需要反射。菜单类型是 RS 自己的（{@code Menus.INSTANCE.getAutocrafterManager()}），
 * 所以客户端会命中 RS 已注册的管理器界面，本模组一行界面代码都不用写。
 *
 * <p>唯一要多做的一步：给菜单打上「虚拟方块实体」标记，放行它的
 * {@code stillValid}（否则那个 {@code stillValidBlockEntity} 恒等检查会让我们一开就关）。
 */
public class WirelessAutocrafterManagerMenuProvider implements ExtendedMenuProvider<AutocrafterManagerData> {
    private final AutocrafterManagerBlockEntity manager;
    private final Component name;
    private final SlotReference slotReference;
    private final ItemStack stack;

    public WirelessAutocrafterManagerMenuProvider(final AutocrafterManagerBlockEntity manager,
                                                  final Component name,
                                                  final SlotReference slotReference,
                                                  final ItemStack stack) {
        this.manager = manager;
        this.name = name;
        this.slotReference = slotReference;
        this.stack = stack;
    }

    @Override
    public AutocrafterManagerData getMenuData() {
        return manager.getMenuData();
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, AutocrafterManagerData> getMenuCodec() {
        return AutocrafterManagerData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return name;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        final AbstractContainerMenu menu = manager.createMenu(syncId, inventory, player);
        if (menu instanceof VirtualMenuHolder holder) {
            holder.refinednetworkMarkVirtual();
            // 终端被移出槽位、被替换或换成别的实例后，这个界面不再代表任何真实物品。
            holder.refinednetworkSetSessionCheck(
                who -> slotReference.resolve(who).map(current -> current == stack).orElse(false)
            );
        }
        return menu;
    }
}
