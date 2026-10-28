package com.kokuu.rswirelesscrafting.common.util;

/**
 * 按「每秒额度」限速的小工具。
 *
 * <p>和 {@link RateMeter} 的区别：{@code RateMeter} 要求调用方每 tick 恰好推进一次，
 * 而限速转发（网络桥接器把调用转给另一张网络）根本控制不了别人调几次——
 * 所以这里改成**时间窗口**式：同一个游戏秒内的调用共用一个额度，跨秒自动重置。
 */
public final class RateLimiter {
    private static final long TICKS_PER_SECOND = 20;

    private long perSecond;
    private long windowStart = Long.MIN_VALUE;
    private long usedInWindow;

    public RateLimiter(final long perSecond) {
        this.perSecond = Math.max(0L, perSecond);
    }

    public void setPerSecond(final long value) {
        this.perSecond = Math.max(0L, value);
    }

    /**
     * 本窗口内还剩多少额度。
     */
    public long remaining(final long gameTime) {
        roll(gameTime);
        return Math.max(0L, perSecond - usedInWindow);
    }

    /**
     * 记下一笔用量（调用方必须保证只记真正放行的那部分）。
     */
    public void consume(final long amount, final long gameTime) {
        roll(gameTime);
        usedInWindow = SaturatedMath.add(usedInWindow, Math.max(0L, amount));
    }

    private void roll(final long gameTime) {
        if (windowStart == Long.MIN_VALUE || gameTime < windowStart
            || gameTime - windowStart >= TICKS_PER_SECOND) {
            windowStart = gameTime;
            usedInWindow = 0;
        }
    }
}
