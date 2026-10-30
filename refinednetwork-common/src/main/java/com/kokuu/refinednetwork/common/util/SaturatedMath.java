package com.kokuu.refinednetwork.common.util;

/**
 * 能量和库存计算用的饱和运算。
 *
 * <p>配置允许很大的 long 值时，普通加法/乘法可能溢出成负数，导致负费率、无限额度或错误扣费。
 * 这里的实现不追求 BigInteger 精度，只保证结果不会越过 long 的边界。
 */
public final class SaturatedMath {
    private SaturatedMath() {
    }

    public static long add(final long a, final long b) {
        if (b > 0 && a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }
        if (b < 0 && a < Long.MIN_VALUE - b) {
            return Long.MIN_VALUE;
        }
        return a + b;
    }

    public static long multiply(final long a, final long b) {
        final double result = (double) a * (double) b;
        if (result >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        if (result <= Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        return (long) result;
    }

    public static long multiplyPercent(final long value, final long percent) {
        final double result = (double) value * (double) percent / 100.0;
        if (result >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        if (result <= Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        return (long) result;
    }
}
