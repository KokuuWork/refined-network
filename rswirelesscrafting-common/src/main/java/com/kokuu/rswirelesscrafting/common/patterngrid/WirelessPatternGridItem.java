package com.kokuu.rswirelesscrafting.common.patterngrid;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.rswirelesscrafting.common.virtual.VirtualTerminalSession;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.ExtendedWirelessNetworkItemContext;

import java.util.Optional;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNullElse;

/**
 * 无线样板终端：远程配置自动化样板，不用再跑回基地的样板网格。
 *
 * <p>通用行为（电池容量、绑定 / 距离悬停提示、插件装配界面、距离感知上下文）继承自
 * {@link AbstractWirelessTerminalItem}；这里只负责「拿终端物品 + 绑定网络，交给
 * {@link VirtualPatternGrid} 与 RS 原版样板网格菜单」。
 */
public class WirelessPatternGridItem extends AbstractWirelessTerminalItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(WirelessPatternGridItem.class);
    /**
     * 只在每个游戏会话里打一次成功日志，避免玩家反复开界面把日志刷满。
     */
    private static boolean loggedOpen;

    public WirelessPatternGridItem(final EnergyItemHelper energyItemHelper,
                                   final NetworkItemHelper networkItemHelper) {
        super(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
            energyItemHelper,
            networkItemHelper
        );
    }

    @Override
    protected Component getTerminalDescriptionLine() {
        return ContentNames.WIRELESS_PATTERN_GRID_DESC;
    }

    @Override
    protected Component getTerminalName() {
        return ContentNames.WIRELESS_PATTERN_GRID;
    }

    @Override
    protected void openTerminal(@Nullable final Component name,
                                final ServerPlayer player,
                                final SlotReference slotReference,
                                final NetworkItemContext context) {
        final Optional<ItemStack> resolved = slotReference.resolve(player);
        if (resolved.isEmpty() || resolved.get().isEmpty()) {
            return;
        }
        final Optional<Network> network = context.resolveNetwork();
        final boolean isAllowed = network
            .map(found -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, found)
                && SecurityHelper.isAllowed(player, BuiltinPermission.AUTOCRAFTING, found))
            .orElse(true);
        if (!isAllowed) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, getTerminalName());
            return;
        }
        // 这一整段都不是 RS 的公开 API：反射构造 + 虚拟方块实体，任何一步都可能因为 RS 换版本而失败。
        // 失败时给玩家一条明确提示、把堆栈写进日志，绝不把游戏带崩。
        try {
            if (!WirelessPatternGridMenuProvider.isAvailable()) {
                fail(player, new IllegalStateException("RS 的样板网格菜单服务端构造不可用"));
                return;
            }
            // 开界面本身就要钱：与无线合成终端同口径、按距离倍率结算；付不起一分不扣。
            final VirtualTerminalSession session = new VirtualTerminalSession(resolved.get(), player, context);
            if (!session.tryCharge(Platform.getConfig().getWirelessCraftingGrid().getOpenEnergyUsage())) {
                player.displayClientMessage(ContentNames.TERMINAL_NOT_ENOUGH_ENERGY, false);
                return;
            }
            final VirtualPatternGrid grid = new VirtualPatternGrid(
                resolved.get(),
                player,
                slotReference,
                player.level(),
                network.orElse(null),
                session
            );
            com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
                player,
                new WirelessPatternGridMenuProvider(grid, requireNonNullElse(name, getTerminalName()))
            );
            logFirstOpen(player, resolved.get());
        } catch (final IllegalStateException | NullPointerException | ClassCastException | LinkageError e) {
            // 只捕获可预期的几类：反射/构造失败（IllegalStateException）、RS 内部空指针与类型不符、
            // 以及 RS 换版本导致的 NoSuchMethodError 之类链接错误。checkstyle 禁止 catch 宽泛类型。
            fail(player, e);
        }
    }

    /**
     * 打开失败：日志留全栈、玩家收一条可操作的提示（带上 QQ 反馈群，别让人只能干看着）。
     */
    private static void fail(final ServerPlayer player, final Throwable cause) {
        LOGGER.error("无线样板终端打开失败", cause);
        player.displayClientMessage(ContentNames.WIRELESS_PATTERN_GRID_FAILED, false);
    }

    /**
     * 每个游戏会话记一次成功打开（含电量与距离倍率），方便只靠日志判断功能是否真的跑通。
     */
    private static void logFirstOpen(final ServerPlayer player, final ItemStack stack) {
        if (loggedOpen) {
            LOGGER.debug("无线样板终端再次打开：{}", player.getName().getString());
            return;
        }
        loggedOpen = true;
        LOGGER.info(
            "无线样板终端打开成功：玩家={} 电量={}/{} 距离倍率={}%",
            player.getName().getString(),
            AbstractWirelessTerminalItem.getStoredEnergy(stack),
            AbstractWirelessTerminalItem.getEnergyCapacity(stack),
            ExtendedWirelessNetworkItemContext.distanceMultiplierPercent(stack, player)
        );
    }
}
