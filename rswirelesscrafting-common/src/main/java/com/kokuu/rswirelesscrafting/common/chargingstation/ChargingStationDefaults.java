package com.kokuu.rswirelesscrafting.common.chargingstation;

/**
 * 一档充电站的全部默认数值，用于集中声明并传给配置层。
 *
 * @param energyUsage       待机耗电（FE/t）
 * @param wiredChargeRate   有线充电速率（FE/t）
 * @param wirelessChargeRate 无线充电速率（FE/t）
 * @param wirelessRadius    无线充电半径（格）
 * @param externalCapacity  内部 FE 缓冲容量
 */
public record ChargingStationDefaults(long energyUsage,
                                      long wiredChargeRate,
                                      long wirelessChargeRate,
                                      int wirelessRadius,
                                      long externalCapacity) {
}
