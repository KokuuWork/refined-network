package com.kokuu.refinednetwork.common.energymonitor;

/**
 * 能量监控仪的默认数值。
 *
 * @param energyUsage          常驻耗电（FE/s）——它只是个读取设备，收一点「看仪表」的钱
 * @param lowThresholdPercent  低于这个百分比就亮红灯
 * @param sampleInterval       净收支采样间隔（tick），默认 20 = 每秒一次
 */
public record EnergyMonitorDefaults(long energyUsage, int lowThresholdPercent, int sampleInterval) {
}
