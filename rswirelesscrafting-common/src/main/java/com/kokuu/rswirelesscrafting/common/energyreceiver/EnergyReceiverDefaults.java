package com.kokuu.rswirelesscrafting.common.energyreceiver;

/**
 * 无线能量接收器的默认数值。
 *
 * @param capacity    内部 FE 缓冲容量——同时就是它向网络「贡献」的那部分能量
 * @param energyUsage 链路费的**基础值**（FE/s），实际按距离倍率缩放；跨维度按维度倍率
 */
public record EnergyReceiverDefaults(long capacity, long energyUsage) {
}
