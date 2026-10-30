package com.kokuu.refinednetwork.common.monitor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * 一条监控规则：盯着某种物品，越过阈值后**做一件已配置的事**。
 *
 * @param item           被监控的物品（空栈表示这一行没用）
 * @param threshold      阈值；{@link RuleAction#ALERT} 时是「触发线」，
 *                       {@link RuleAction#RESTOCK} 时是「要维持的目标数量」
 * @param alertWhenBelow {@code true} = 存量**低于**阈值时触发（缺料）；
 *                       {@code false} = 存量**高于**阈值时触发（堵料）
 * @param action         触发后做什么，见 {@link RuleAction}
 */
public record MonitorRule(ItemStack item, long threshold, boolean alertWhenBelow, RuleAction action) {
    /**
     * 新放进去的一行默认按「低于 0」——也就是「一点都没有了」，最保守的缺料告警。
     */
    public static final long DEFAULT_THRESHOLD = 0L;

    public static final Codec<MonitorRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(MonitorRule::item),
        Codec.LONG.fieldOf("threshold").forGetter(MonitorRule::threshold),
        Codec.BOOL.fieldOf("alert_when_below").forGetter(MonitorRule::alertWhenBelow),
        // 老存档里没有这个字段，缺失时退回「只提醒」，所以加动作不会让旧规则失效。
        RuleAction.CODEC.optionalFieldOf("action", RuleAction.ALERT).forGetter(MonitorRule::action)
    ).apply(instance, MonitorRule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MonitorRule> STREAM_CODEC = StreamCodec.composite(
        ItemStack.OPTIONAL_STREAM_CODEC,
        MonitorRule::item,
        ByteBufCodecs.VAR_LONG,
        MonitorRule::threshold,
        ByteBufCodecs.BOOL,
        MonitorRule::alertWhenBelow,
        RuleAction.STREAM_CODEC,
        MonitorRule::action,
        MonitorRule::new
    );

    /**
     * 触发后做什么。
     */
    public enum RuleAction implements StringRepresentable {
        /**
         * 只在聊天栏提醒你（本模组最早的行为）。
         */
        ALERT("alert"),
        /**
         * 低于目标数量时**自动发起合成**，把库存补回目标（RS2 本体没有这个能力）。
         */
        RESTOCK("restock");

        public static final Codec<RuleAction> CODEC = StringRepresentable.fromEnum(RuleAction::values);
        public static final StreamCodec<RegistryFriendlyByteBuf, RuleAction> STREAM_CODEC =
            StreamCodec.of(
                (buffer, action) -> buffer.writeVarInt(action.ordinal()),
                buffer -> byId(buffer.readVarInt())
            );

        private final String name;

        RuleAction(final String name) {
            this.name = name;
        }

        private static RuleAction byId(final int id) {
            final RuleAction[] values = values();
            return id >= 0 && id < values.length ? values[id] : ALERT;
        }

        /**
         * 切到下一个动作（界面上点一下就在两者之间轮换）。
         */
        public RuleAction next() {
            return this == ALERT ? RESTOCK : ALERT;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /**
     * 空行：没放物品，不参与检查，也不显示任何数字。
     */
    public static MonitorRule empty() {
        return new MonitorRule(ItemStack.EMPTY, DEFAULT_THRESHOLD, true, RuleAction.ALERT);
    }

    public boolean isEmpty() {
        return item.isEmpty();
    }

    public boolean isRestock() {
        return action == RuleAction.RESTOCK;
    }

    public MonitorRule withItem(final ItemStack newItem) {
        return new MonitorRule(newItem, threshold, alertWhenBelow, action);
    }

    public MonitorRule withThreshold(final long newThreshold) {
        return new MonitorRule(item, newThreshold, alertWhenBelow, action);
    }

    public MonitorRule withDirection(final boolean below) {
        return new MonitorRule(item, threshold, below, action);
    }

    /**
     * 换动作。切到 {@link RuleAction#RESTOCK} 时方向强制回到「低于」——
     * 「补货」只对缺料有意义，「高于目标就补货」讲不通。
     */
    public MonitorRule withAction(final RuleAction newAction) {
        return new MonitorRule(item, threshold, newAction == RuleAction.RESTOCK || alertWhenBelow, newAction);
    }

    /**
     * 存量是否已经越过这条规则。
     */
    public boolean isTriggered(final long stored) {
        return alertWhenBelow ? stored < threshold : stored > threshold;
    }

    /**
     * 补货还要补多少个（已经达标返回 0）。
     */
    public long missingToTarget(final long stored) {
        return Math.max(0L, threshold - stored);
    }
}
