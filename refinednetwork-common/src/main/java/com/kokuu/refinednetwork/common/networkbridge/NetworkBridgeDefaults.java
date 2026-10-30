package com.kokuu.refinednetwork.common.networkbridge;

/**
 * 网络桥接器的默认数值。
 *
 * @param energyUsage         链路费基础值（FE/s），按距离倍率缩放
 * @param itemTransferRate    经过桥搬运物品的速率上限（件/秒）
 * @param energyTransferRate  经过桥搬运能量的速率上限（FE/s）
 */
public record NetworkBridgeDefaults(long energyUsage, long itemTransferRate, long energyTransferRate) {
}
