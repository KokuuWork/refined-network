package com.kokuu.rswirelesscrafting.common.monitor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * 一条监控规则：盯着某种物品，越过阈值就提醒。
 *
 * @param item           被监控的物品（空栈表示这一行没用）
 * @param threshold      阈值
 * @param alertWhenBelow {@code true} = 存量**低于**阈值时提醒（缺料告警）；
 *                       {@code false} = 存量**高于**阈值时提醒（堵料告警）
 */
public record MonitorRule(ItemStack item, long threshold, boolean alertWhenBelow) {
    /**
     * 新放进去的一行默认按「低于 0」——也就是「一点都没有了」，最保守的缺料告警。
     */
    public static final long DEFAULT_THRESHOLD = 0L;

    public static final Codec<MonitorRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(MonitorRule::item),
        Codec.LONG.fieldOf("threshold").forGetter(MonitorRule::threshold),
        Codec.BOOL.fieldOf("alert_when_below").forGetter(MonitorRule::alertWhenBelow)
    ).apply(instance, MonitorRule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MonitorRule> STREAM_CODEC = StreamCodec.composite(
        ItemStack.OPTIONAL_STREAM_CODEC,
        MonitorRule::item,
        ByteBufCodecs.VAR_LONG,
        MonitorRule::threshold,
        ByteBufCodecs.BOOL,
        MonitorRule::alertWhenBelow,
        MonitorRule::new
    );

    /**
     * 空行：没放物品，不参与检查，也不显示任何数字。
     */
    public static MonitorRule empty() {
        return new MonitorRule(ItemStack.EMPTY, DEFAULT_THRESHOLD, true);
    }

    public boolean isEmpty() {
        return item.isEmpty();
    }

    public MonitorRule withItem(final ItemStack newItem) {
        return new MonitorRule(newItem, threshold, alertWhenBelow);
    }

    public MonitorRule withThreshold(final long newThreshold) {
        return new MonitorRule(item, newThreshold, alertWhenBelow);
    }

    public MonitorRule withDirection(final boolean below) {
        return new MonitorRule(item, threshold, below);
    }

    /**
     * 存量是否已经触发这条规则。
     */
    public boolean isTriggered(final long stored) {
        return alertWhenBelow ? stored < threshold : stored > threshold;
    }
}
