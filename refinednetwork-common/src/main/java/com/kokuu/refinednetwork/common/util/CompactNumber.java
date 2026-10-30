package com.kokuu.refinednetwork.common.util;

/**
 * 把大数值压成紧凑的 K/M/G 后缀形式（如 1200000 → 1.2M）。
 *
 * <p>终端电量、监控存量都可能到几十亿，直接画整串数字会溢出界面。
 */
public final class CompactNumber {
    private CompactNumber() {
    }

    public static String format(final long value) {
        if (value >= 1_000_000_000L) {
            return formatScaled(value, 1_000_000_000L, 'G');
        }
        if (value >= 1_000_000L) {
            return formatScaled(value, 1_000_000L, 'M');
        }
        if (value >= 1_000L) {
            return formatScaled(value, 1_000L, 'K');
        }
        return Long.toString(value);
    }

    /**
     * 一位小数 + 单位后缀（不足一位小数时省略小数点），避免依赖 locale 的字符串格式化。
     */
    private static String formatScaled(final long value, final long unit, final char suffix) {
        final long whole = value / unit;
        final long tenth = value % unit / (unit / 10);
        return tenth == 0 ? whole + String.valueOf(suffix) : whole + "." + tenth + suffix;
    }
}
