package com.kokuu.refinednetwork.common.monitor;

import com.kokuu.refinednetwork.common.DataComponents;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

/**
 * 监控终端身上的规则列表：存于数据组件，界面直接读写它（和升级插件同一套做法）。
 */
public final class MonitorRules {
    /**
     * 规则行数。界面按这个数画固定行数，空行只是没放物品——定 4 行是因为再往上加，
     * 界面高度就会顶到屏幕（GUI 高度 = 顶部标题 + 规则行 + 玩家背包）。
     */
    public static final int MAX_RULES = 4;

    private MonitorRules() {
    }

    public static DataComponentType<List<MonitorRule>> getComponent() {
        return DataComponents.INSTANCE.getMonitorRules();
    }

    /**
     * 读出一份**定长**快照（尾部用空行补齐），所以界面可以直接按槽位下标取值。
     */
    public static List<MonitorRule> snapshot(final ItemStack stack) {
        final List<MonitorRule> rules = new ArrayList<>(getRaw(stack));
        while (rules.size() < MAX_RULES) {
            rules.add(MonitorRule.empty());
        }
        return rules;
    }

    /**
     * 真身（可能短于 {@link #MAX_RULES}）——用于判断「有没有配过东西」。
     */
    public static List<MonitorRule> getRaw(final ItemStack stack) {
        final List<MonitorRule> rules = stack.get(getComponent());
        return rules == null ? List.of() : rules;
    }

    public static boolean isEmpty(final ItemStack stack) {
        for (final MonitorRule rule : getRaw(stack)) {
            if (!rule.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 写回：去掉尾部空行，保持组件精简（悬停提示也只列真实规则）。
     */
    public static void set(final ItemStack stack, final List<MonitorRule> rules) {
        final List<MonitorRule> compacted = new ArrayList<>(rules.size());
        for (final MonitorRule rule : rules) {
            if (!rule.isEmpty()) {
                compacted.add(rule);
            }
        }
        if (compacted.isEmpty()) {
            stack.remove(getComponent());
        } else {
            stack.set(getComponent(), compacted);
        }
    }
}
