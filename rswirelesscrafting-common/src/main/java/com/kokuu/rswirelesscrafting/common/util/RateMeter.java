package com.kokuu.rswirelesscrafting.common.util;

/**
 * 把配置里的「每秒」额度摊到每 tick 上。
 *
 * <p>Minecraft 是每秒 20 tick，而 {@code 每秒额度 / 20} 常常除不尽。直接整除会持续漏掉那一小截，
 * 所以这里把余数留在 {@code carry} 里顺延到下一 tick，长期平均恰好等于配置值。
 */
public final class RateMeter {
    private static final long TICKS_PER_SECOND = 20;

    private long carry;

    /**
     * 推进一 tick。
     *
     * @param perSecond 配置里的每秒额度（负数按 0 处理）
     * @return 本 tick 可用的额度
     */
    public long next(final long perSecond) {
        final long total = SaturatedMath.add(carry, Math.max(0L, perSecond));
        carry = total % TICKS_PER_SECOND;
        return total / TICKS_PER_SECOND;
    }
}
