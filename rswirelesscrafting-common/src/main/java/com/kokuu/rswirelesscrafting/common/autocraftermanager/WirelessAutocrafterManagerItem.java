package com.kokuu.rswirelesscrafting.common.autocraftermanager;

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
 * 无线自动合成仓管理器：**远程管样板**。
 *
 * <p>写样板已经能远程了（无线样板终端），但「这台样板要放进哪个合成仓、从哪个仓里取出来」
 * 原本还得跑回基地开方块版管理器。这件终端补上最后一步：路远的地方改一条产线，不用来回飞。
 *
 * <p>通用行为（电池容量、绑定 / 距离悬停提示、插件装配界面、距离感知上下文）继承自
 * {@link AbstractWirelessTerminalItem}；这里只负责「拿终端物品 + 绑定网络，交给
 * {@link VirtualAutocrafterManager} 与 RS 原版管理器菜单」。
 *
 * <p><b>计费</b>：开界面按本模组统一的「打开界面」档（5 FE × 距离倍率），界面内移动样板不另计费。
 * 按次拦样板槽需要给 RS 菜单再加一处注入，而「哪些槽位算钱」是菜单特有的语义——先不做，
 * 避免造一个猜出来的抽象（见 {@code docs/ARCHITECTURE.md} §0.2 的泛化原则）。
 */
public class WirelessAutocrafterManagerItem extends AbstractWirelessTerminalItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(WirelessAutocrafterManagerItem.class);
    /**
     * 只在每个游戏会话里打一次成功日志，避免玩家反复开界面把日志刷满。
     */
    private static boolean loggedOpen;

    public WirelessAutocrafterManagerItem(final EnergyItemHelper energyItemHelper,
                                          final NetworkItemHelper networkItemHelper) {
        super(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
            energyItemHelper,
            networkItemHelper
        );
    }

    @Override
    protected Component getTerminalDescriptionLine() {
        return ContentNames.WIRELESS_AUTOCRAFTER_MANAGER_DESC;
    }

    @Override
    protected Component getTerminalName() {
        return ContentNames.WIRELESS_AUTOCRAFTER_MANAGER;
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
        // 开界面本身就要钱：走公共会话结算（同样吃距离倍率），付不起一分不扣、并明确告诉玩家。
        final VirtualTerminalSession session = new VirtualTerminalSession(resolved.get(), player, context);
        if (!session.tryCharge(Platform.getConfig().getWirelessCraftingGrid().getOpenEnergyUsage())) {
            player.displayClientMessage(ContentNames.TERMINAL_NOT_ENOUGH_ENERGY, false);
            return;
        }
        // 这一整段依赖 RS 的内部形状（虚拟方块实体 + 借它的菜单），任何一步都可能因为 RS 换版本而失败。
        // 失败时给玩家一条明确提示、把堆栈写进日志，绝不把游戏带崩。
        try {
            final VirtualAutocrafterManager manager = new VirtualAutocrafterManager(
                player.level(),
                network.orElse(null)
            );
            com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
                player,
                new WirelessAutocrafterManagerMenuProvider(manager, requireNonNullElse(name, getTerminalName()))
            );
            logFirstOpen(player, resolved.get());
        } catch (final IllegalStateException | NullPointerException | ClassCastException | LinkageError e) {
            fail(player, e);
        }
    }

    /**
     * 打开失败：日志留全栈、玩家收一条可操作的提示（带上 QQ 反馈群，别让人只能干看着）。
     */
    private static void fail(final ServerPlayer player, final Throwable cause) {
        LOGGER.error("无线自动合成仓管理器打开失败", cause);
        player.displayClientMessage(ContentNames.WIRELESS_AUTOCRAFTER_MANAGER_FAILED, false);
    }

    /**
     * 每个游戏会话记一次成功打开（含电量与距离倍率），方便只靠日志判断功能是否真的跑通。
     */
    private static void logFirstOpen(final ServerPlayer player, final ItemStack stack) {
        if (loggedOpen) {
            LOGGER.debug("无线自动合成仓管理器再次打开：{}", player.getName().getString());
            return;
        }
        loggedOpen = true;
        LOGGER.info(
            "无线自动合成仓管理器打开成功：玩家={} 电量={}/{} 距离倍率={}%",
            player.getName().getString(),
            AbstractWirelessTerminalItem.getStoredEnergy(stack),
            AbstractWirelessTerminalItem.getEnergyCapacity(stack),
            ExtendedWirelessNetworkItemContext.distanceMultiplierPercent(stack, player)
        );
    }
}
