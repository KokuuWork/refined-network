package com.kokuu.rswirelesscrafting.common.amplifier;

/**
 * 无线增幅塔的默认数值。
 *
 * @param energyUsage       常驻耗电（FE/s）——塔接在网络上一刻不停，所以要按秒计
 * @param radius            覆盖半径（格，水平）
 * @param multiplierPercent 覆盖范围内距离倍率的折扣（百分比，50 = 打对折）
 */
public record AmplifierDefaults(long energyUsage, int radius, int multiplierPercent) {
}
