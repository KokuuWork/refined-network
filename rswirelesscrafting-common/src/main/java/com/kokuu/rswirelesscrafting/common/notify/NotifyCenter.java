package com.kokuu.rswirelesscrafting.common.notify;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.monitor.MonitorRule;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 提醒通道：把「规则触发了」这件事推到玩家眼前。
 *
 * <p>做法刻意保持朴素——**不引入任何自定义数据包**：服务端自己去看网络，命中了就直接
 * {@code displayClientMessage}。所谓「通道」在这里就是「服务端主动跟玩家说话」。
 *
 * <p>这里只负责**措辞与发送**；「什么时候该说」由
 * {@link com.kokuu.rswirelesscrafting.common.monitor.MonitorEngine} 决定（它管节流与冷却）。
 */
public final class NotifyCenter {
    private NotifyCenter() {
    }

    /**
     * 监控规则越线提醒（缺料 / 堵料）。
     */
    public static void alert(final ServerPlayer player, final MonitorRule rule, final long stored) {
        player.displayClientMessage(
            Component.translatable(
                rule.alertWhenBelow()
                    ? ContentNames.MONITOR_ALERT_BELOW_TRANSLATION_KEY
                    : ContentNames.MONITOR_ALERT_ABOVE_TRANSLATION_KEY,
                rule.item().getHoverName(),
                amount(stored),
                amount(rule.threshold())
            ).withStyle(ChatFormatting.GOLD),
            false
        );
    }

    /**
     * 自动补货已下单。
     */
    public static void restockStarted(final ServerPlayer player, final MonitorRule rule, final long stored) {
        player.displayClientMessage(
            Component.translatable(
                ContentNames.MONITOR_RESTOCK_STARTED_TRANSLATION_KEY,
                rule.item().getHoverName(),
                amount(stored),
                amount(rule.threshold())
            ).withStyle(ChatFormatting.AQUA),
            false
        );
    }

    /**
     * 自动补货没能进行：没样板 / 材料不足。两者都只报一次（外面有冷却），不然会变成刷屏。
     */
    public static void restockBlocked(final ServerPlayer player,
                                      final MonitorRule rule,
                                      final boolean noPattern) {
        player.displayClientMessage(
            Component.translatable(
                noPattern
                    ? ContentNames.MONITOR_RESTOCK_NO_PATTERN_TRANSLATION_KEY
                    : ContentNames.MONITOR_RESTOCK_MISSING_TRANSLATION_KEY,
                rule.item().getHoverName()
            ).withStyle(ChatFormatting.RED),
            false
        );
    }

    private static Component amount(final long value) {
        return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.YELLOW);
    }
}
