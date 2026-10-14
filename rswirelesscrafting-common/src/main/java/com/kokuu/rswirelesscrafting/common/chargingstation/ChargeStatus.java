package com.kokuu.rswirelesscrafting.common.chargingstation;

/**
 * 充电站连接面板上一个指示灯的状态。
 *
 * <ul>
 *   <li>{@link #IDLE} —— 该位置空闲（有线槽没放物品 / 无线链接没在充电）；</li>
 *   <li>{@link #CHARGING} —— 正在充入能量；</li>
 *   <li>{@link #FULL} —— 物品已充满（仅有线槽有意义）。</li>
 * </ul>
 *
 * <p>状态由服务端每个 tick 计算，经 {@code ContainerData} 同步给客户端，
 * 客户端据此决定画暗灯、呼吸闪烁的蓝灯还是常亮的绿灯。
 */
public enum ChargeStatus {
    IDLE,
    CHARGING,
    FULL;

    /**
     * 按 {@code ContainerData} 里的 int 还原枚举，越界回落到 {@link #IDLE}。
     */
    public static ChargeStatus byOrdinal(final int ordinal) {
        final ChargeStatus[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return IDLE;
        }
        return values[ordinal];
    }
}
