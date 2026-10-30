package com.kokuu.refinednetwork.common.monitor;

import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.link.BoundTargetResolver;
import com.kokuu.refinednetwork.common.notify.NotifyCenter;
import com.kokuu.refinednetwork.common.terminal.AbstractWirelessTerminalItem;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 监控规则引擎：**看网络、判规则、执行动作**。
 *
 * <p>它是监控终端在后台真正干活的地方（终端只负责在 {@code inventoryTick} 里调一次
 * {@link #tickTerminal}）。目前支持两种动作：
 * <ul>
 *   <li>{@link MonitorRule.RuleAction#ALERT} —— 越线了就在聊天栏说一声；</li>
 *   <li>{@link MonitorRule.RuleAction#RESTOCK} —— 低于目标就**自动发起合成**把库存补回去
 *       （走 RS 的公开 API {@code AutocraftingNetworkComponent#ensureTask}，
 *       RS 本体只有构造器上那个「缺料补 1 个」的残缺版本）。</li>
 * </ul>
 *
 * <p>三条自律：
 * <ol>
 *   <li><b>只在玩家身上带着监控终端时检查</b>，没带就不查；</li>
 *   <li><b>节流</b>：每 {@link #CHECK_INTERVAL} tick 检查一次；</li>
 *   <li><b>冷却</b>：提醒类 {@link #ALERT_COOLDOWN_TICKS}，补货受阻类
 *       {@link #BLOCKED_COOLDOWN_TICKS}——否则缺料会变成每秒一条的刷屏。</li>
 * </ol>
 *
 * <p><b>补货为什么不会重复下单</b>：{@code ensureTask} 在同类任务已在跑时返回
 * {@code TASK_ALREADY_RUNNING}，这时我们既不重复下单、也**不再收费**；
 * 只有真正 {@code TASK_CREATED} 才扣那笔自动合成费。付不起时静默跳过（不报警，不然会刷屏）。
 */
public final class MonitorEngine {
    private static final int CHECK_INTERVAL = 20;
    private static final long ALERT_COOLDOWN_TICKS = 1_200L;
    private static final long BLOCKED_COOLDOWN_TICKS = 600L;
    /**
     * 冷却表上限：正常情况下条目数 = 玩家数 × 规则数，很小；这里只是兜底防泄漏。
     */
    private static final int MAX_COOLDOWN_ENTRIES = 4_096;
    private static final Map<String, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private MonitorEngine() {
    }

    /**
     * 由监控终端物品的 {@code inventoryTick} 调用。主手 / 副手 / 背包 / 饰品栏都会走到这里。
     */
    public static void tickTerminal(final ServerPlayer player, final ItemStack stack) {
        final List<MonitorRule> rules = MonitorRules.getRaw(stack);
        if (rules.isEmpty()) {
            return;
        }
        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        final GlobalPos bound = AbstractWirelessTerminalItem.getBoundLocation(stack);
        if (bound == null) {
            return;
        }
        final Network network = BoundTargetResolver.resolveNetwork(player.level(), bound).orElse(null);
        if (network == null || !SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, network)) {
            return;
        }
        final StorageNetworkComponent storages = network.getComponent(StorageNetworkComponent.class);
        final long now = player.level().getGameTime();
        for (final MonitorRule rule : rules) {
            if (rule.isEmpty()) {
                continue;
            }
            final long stored = storages.get(ItemResource.ofItemStack(rule.item()));
            if (rule.isRestock()) {
                restock(player, stack, network, rule, stored, now);
            } else if (rule.isTriggered(stored)) {
                alert(player, rule, stored, now);
            }
        }
    }

    private static void alert(final ServerPlayer player,
                              final MonitorRule rule,
                              final long stored,
                              final long now) {
        if (coolingDown(cooldownKey(player, rule) + "|alert", now, ALERT_COOLDOWN_TICKS)) {
            return;
        }
        NotifyCenter.alert(player, rule, stored);
    }

    /**
     * 补货：低于目标就发起合成，补到目标为止。
     */
    private static void restock(final ServerPlayer player,
                                final ItemStack terminal,
                                final Network network,
                                final MonitorRule rule,
                                final long stored,
                                final long now) {
        final long missing = rule.missingToTarget(stored);
        if (missing <= 0) {
            return;
        }
        final ResourceKey resource = ItemResource.ofItemStack(rule.item());
        final AutocraftingNetworkComponent autocrafting =
            network.getComponent(AutocraftingNetworkComponent.class);
        if (autocrafting.getPatternsByOutput(resource).isEmpty()) {
            // 网络里根本没有能产出它的样板——说一次，别让玩家以为补货坏了。
            blocked(player, rule, true, now);
            return;
        }
        if (!SecurityHelper.isAllowed(player, BuiltinPermission.AUTOCRAFTING, network)) {
            return;
        }
        final long baseCost = Platform.getConfig().getWirelessCraftingGrid().getAutocraftingEnergyUsage();
        if (!AbstractWirelessTerminalItem.canAffordEnergy(terminal, player, baseCost)) {
            // 没电就静静等着（供电恢复后自动继续），不报警。
            return;
        }
        final AutocraftingNetworkComponent.EnsureResult result = autocrafting.ensureTask(
            resource,
            missing,
            // 用玩家名做 Actor：合成监视器里能看出这单是谁发起的。
            () -> player.getName().getString(),
            CancellationToken.NONE
        );
        switch (result) {
            case TASK_CREATED -> {
                // 订单真的建起来了才收费，避免「付了钱却没合成」。用 ensureTask 的 Actor 语义，
                // 材料不足时它返回 MISSING_RESOURCES（不会建单），我们也就一分不收。
                AbstractWirelessTerminalItem.tryConsumeEnergy(terminal, player, baseCost);
                NotifyCenter.restockStarted(player, rule, stored);
            }
            case MISSING_RESOURCES -> blocked(player, rule, false, now);
            default -> {
                // TASK_ALREADY_RUNNING：同类任务已经在跑了。既不下单也不收费，安静地等它跑完。
            }
        }
    }

    private static void blocked(final ServerPlayer player,
                                final MonitorRule rule,
                                final boolean noPattern,
                                final long now) {
        final String key = cooldownKey(player, rule) + (noPattern ? "|nopattern" : "|missing");
        if (coolingDown(key, now, BLOCKED_COOLDOWN_TICKS)) {
            return;
        }
        NotifyCenter.restockBlocked(player, rule, noPattern);
    }

    /**
     * 冷却键：同一玩家 + 同一件物品 + 同一套参数。玩家改过规则（阈值 / 方向 / 动作）就会重新计时。
     */
    private static String cooldownKey(final ServerPlayer player, final MonitorRule rule) {
        return player.getUUID()
            + "|" + rule.item().getItem()
            + "|" + rule.threshold()
            + "|" + rule.alertWhenBelow()
            + "|" + rule.action();
    }

    private static boolean coolingDown(final String key, final long now, final long cooldown) {
        final Long last = COOLDOWNS.get(key);
        if (last != null && now - last < cooldown) {
            return true;
        }
        pruneIfHuge(now);
        COOLDOWNS.put(key, now);
        return false;
    }

    /**
     * 兜底清理：玩家删档、规则改来改去都会留下过期条目，超过上限时把老条目丢掉。
     */
    private static void pruneIfHuge(final long now) {
        if (COOLDOWNS.size() <= MAX_COOLDOWN_ENTRIES) {
            return;
        }
        final long cutoff = now - ALERT_COOLDOWN_TICKS * 4;
        final Iterator<Map.Entry<String, Long>> it = COOLDOWNS.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() < cutoff) {
                it.remove();
            }
        }
    }
}
