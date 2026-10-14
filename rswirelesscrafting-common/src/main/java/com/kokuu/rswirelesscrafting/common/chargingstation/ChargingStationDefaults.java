package com.kokuu.rswirelesscrafting.common.chargingstation;

/**
 * 一档充电站的全部默认数值，用于集中声明并传给配置层。
 *
 * <p>三个速率都是<b>每秒（FE/s）</b>，方块内部按每秒 20 tick 摊开；
 * {@code energyUsage} 是待机（空转）时的自耗，与 RS 其他机械同属「在网络上先扣掉」的开销。
 * 待机之所以也按秒计，是为了不出现「站空转就把有线充电全吃掉」的倒挂。
 *
 * @param energyUsage        待机耗电（FE/s）
 * @param wiredChargeRate    有线充电速率（FE/s）
 * @param wirelessChargeRate 无线充电速率（FE/s，每个物品）
 * @param wirelessRadius     无线充电半径（格）
 * @param externalCapacity   内部 FE 缓冲容量（FE）
 */
public record ChargingStationDefaults(long energyUsage,
                                      long wiredChargeRate,
                                      long wirelessChargeRate,
                                      int wirelessRadius,
                                      long externalCapacity) {
}
