package com.kokuu.rswirelesscrafting.common.bridge;

/**
 * 无线桥的默认数值。
 *
 * @param energyUsage              桥本身的常驻耗电（FE/s）——配对的两端各算一座
 * @param chunkLoading             是否让据点自动保活两端区块
 * @param chunkLoadingEnergyUsage  每保活一个区块的附加耗电（FE/s）——配对的桥通常保 2 个（两端）
 */
public record BridgeDefaults(long energyUsage, boolean chunkLoading, long chunkLoadingEnergyUsage) {
}
