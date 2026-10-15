package com.kokuu.rswirelesscrafting.common.bridge;

/**
 * 无线桥的默认数值。
 *
 * @param energyUsage 每座桥的常驻耗电（FE/s）——配对的两端各算一座
 * @param chunkLoading 是否让据点自动保活两端区块
 */
public record BridgeDefaults(long energyUsage, boolean chunkLoading) {
}
