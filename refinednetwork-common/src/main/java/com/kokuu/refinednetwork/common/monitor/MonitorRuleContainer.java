package com.kokuu.refinednetwork.common.monitor;

import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 直接读写「监控终端物品的 {@code monitorRules} 数据组件」的容器视图。
 *
 * <p>每一行就是一个槽：放进去什么物品就监控什么，取出来就取消监控。阈值与方向不在槽里，
 * 由界面按钮（{@code clickMenuButton}）改，改动同样落到数据组件上——所以物品本体不重新合成，
 * 关掉界面再打开规则还在。
 */
public class MonitorRuleContainer implements Container {
    /**
     * 阈值档位（对数列）。监控存量这种量级，逐 1 调整没有意义，按档跳更顺手。
     */
    private static final long[] THRESHOLD_STEPS = {
        0L, 1L, 8L, 16L, 64L, 256L, 1_024L, 4_096L, 16_384L, 65_536L
    };

    private final ItemStack stack;

    public MonitorRuleContainer(final ItemStack stack) {
        this.stack = stack;
    }

    public List<MonitorRule> rules() {
        return MonitorRules.snapshot(stack);
    }

    /**
     * 阈值换档。{@code up} 为假时往小走。
     */
    public void cycleThreshold(final int slot, final boolean up) {
        final List<MonitorRule> rules = rules();
        final MonitorRule rule = rules.get(slot);
        if (rule.isEmpty()) {
            return;
        }
        final int current = nearestStep(rule.threshold());
        int next = up ? current + 1 : current - 1;
        next = Math.max(0, Math.min(THRESHOLD_STEPS.length - 1, next));
        rules.set(slot, rule.withThreshold(THRESHOLD_STEPS[next]));
        MonitorRules.set(stack, rules);
    }

    /**
     * 换动作：提醒 ↔ 自动补货。切到补货时方向会被强制回「低于」（见
     * {@link MonitorRule#withAction}）。
     */
    public void cycleAction(final int slot) {
        final List<MonitorRule> rules = rules();
        final MonitorRule rule = rules.get(slot);
        if (rule.isEmpty()) {
            return;
        }
        rules.set(slot, rule.withAction(rule.action().next()));
        MonitorRules.set(stack, rules);
    }

    /**
     * 切换「低于阈值提醒」与「高于阈值提醒」。
     */
    public void toggleDirection(final int slot) {
        final List<MonitorRule> rules = rules();
        final MonitorRule rule = rules.get(slot);
        if (rule.isEmpty()) {
            return;
        }
        rules.set(slot, rule.withDirection(!rule.alertWhenBelow()));
        MonitorRules.set(stack, rules);
    }

    /**
     * 找出当前阈值最接近的档位下标；不在档位上时取不小于它的那一档（这样往大跳一格仍能前进）。
     */
    private static int nearestStep(final long threshold) {
        for (int i = 0; i < THRESHOLD_STEPS.length; i++) {
            if (threshold <= THRESHOLD_STEPS[i]) {
                return i;
            }
        }
        return THRESHOLD_STEPS.length - 1;
    }

    @Override
    public int getContainerSize() {
        return MonitorRules.MAX_RULES;
    }

    @Override
    public boolean isEmpty() {
        return MonitorRules.isEmpty(stack);
    }

    @Override
    public ItemStack getItem(final int slot) {
        return rules().get(slot).item();
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        final List<MonitorRule> rules = rules();
        final ItemStack removed = rules.get(slot).item().copyWithCount(1);
        rules.set(slot, MonitorRule.empty());
        MonitorRules.set(stack, rules);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slot) {
        return removeItem(slot, 1);
    }

    @Override
    public void setItem(final int slot, final ItemStack newStack) {
        final List<MonitorRule> rules = rules();
        final MonitorRule current = rules.get(slot);
        if (newStack.isEmpty()) {
            rules.set(slot, MonitorRule.empty());
        } else if (ItemStack.isSameItem(current.item(), newStack)) {
            // 同一物品只是重新放了一次：保留玩家已经调好的阈值与方向。
            rules.set(slot, current.withItem(newStack.copyWithCount(1)));
        } else {
            // 换了监控目标：阈值与方向跟着新规则回到默认，免得沿用上一件物品的设定。
            rules.set(slot, MonitorRule.empty().withItem(newStack.copyWithCount(1)));
        }
        MonitorRules.set(stack, rules);
    }

    @Override
    public void setChanged() {
        // 每次读写都已经直接落到物品组件上了，这里不需要再做任何事。
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        MonitorRules.set(stack, List.of());
    }
}
