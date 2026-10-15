package com.kokuu.rswirelesscrafting.common.notify;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.link.BoundTargetResolver;
import com.kokuu.rswirelesscrafting.common.monitor.MonitorRule;
import com.kokuu.rswirelesscrafting.common.monitor.MonitorRules;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 提醒通道：把「网络里某样东西越过阈值」这件事推到玩家眼前。
 *
 * <p>做法刻意保持朴素——**不引入任何自定义数据包**：服务端自己去看网络，命中了就直接
 * {@code displayClientMessage}。所谓「通道」在这里就是「服务端主动跟玩家说话」。
 *
 * <p>三条自律：
 * <ol>
 *   <li><b>只在玩家身上带着监控终端时检查</b>，没带就不查（别人家的网络不关你的事）；</li>
 *   <li><b>节流</b>：每 {@link #CHECK_INTERVAL} tick 才查一次，绕过背包逐件看；</li>
 *   <li><b>冷却</b>：同一条规则触发后 {@link #ALERT_COOLDOWN_TICKS} tick 内不重复提醒，
 *       否则缺料会变成每秒一条的刷屏。</li>
 * </ol>
 *
 * <p>提醒**不收费**：它是一次被动读取，不像开界面 / 造样板那样是玩家的主动操作；
 * 而且频率已经被上面两条限制住了。真正需要付费的动作仍然照旧。
 */
public final class NotifyCenter {
    private static final int CHECK_INTERVAL = 20;
    private static final long ALERT_COOLDOWN_TICKS = 1_200L;
    private static final Map<String, Long> LAST_ALERT = new ConcurrentHashMap<>();

    private NotifyCenter() {
    }

    /**
     * 由监控终端物品的 {@code inventoryTick} 调用。主手 / 副手 / 背包 / 饰品栏都会走到这里。
     */
    public static void tickTerminal(final ServerPlayer player, final ItemStack stack) {
        if (MonitorRules.getRaw(stack).isEmpty()) {
            return;
        }
        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        final GlobalPos bound = AbstractWirelessTerminalItem.getBoundLocation(stack);
        if (bound == null) {
            return;
        }
        final Network network = BoundTargetResolver
            .resolveNetwork(player.level(), bound)
            .orElse(null);
        if (network == null) {
            return;
        }
        final StorageNetworkComponent storages = network.getComponent(StorageNetworkComponent.class);
        final long now = player.level().getGameTime();
        for (final MonitorRule rule : MonitorRules.getRaw(stack)) {
            if (rule.isEmpty()) {
                continue;
            }
            final long stored = storages.get(ItemResource.ofItemStack(rule.item()));
            if (!rule.isTriggered(stored)) {
                continue;
            }
            final String key = cooldownKey(player, bound, rule);
            final Long last = LAST_ALERT.get(key);
            if (last != null && now - last < ALERT_COOLDOWN_TICKS) {
                continue;
            }
            LAST_ALERT.put(key, now);
            player.displayClientMessage(createAlert(rule, stored), false);
        }
    }

    /**
     * 冷却键：同一玩家 + 同一个网络 + 同一件物品 + 同一套参数。玩家改过规则就会重新提醒。
     */
    private static String cooldownKey(final ServerPlayer player, final GlobalPos bound, final MonitorRule rule) {
        return player.getUUID()
            + "|" + bound.dimension().location()
            + "|" + bound.pos().asLong()
            + "|" + rule.item().getItem()
            + "|" + rule.threshold()
            + "|" + rule.alertWhenBelow();
    }

    private static Component createAlert(final MonitorRule rule, final long stored) {
        return Component.translatable(
            rule.alertWhenBelow()
                ? ContentNames.MONITOR_ALERT_BELOW_TRANSLATION_KEY
                : ContentNames.MONITOR_ALERT_ABOVE_TRANSLATION_KEY,
            rule.item().getHoverName(),
            Component.literal(String.valueOf(stored)).withStyle(ChatFormatting.YELLOW),
            Component.literal(String.valueOf(rule.threshold())).withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.GOLD);
    }
}
