package com.kokuu.refinednetwork.common.chargingstation;

/**
 * 一档充电站的全部默认数值，用于集中声明并传给配置层。
 *
 * <p>三个速率都是<b>每秒（FE/s）</b>，方块内部按每秒 20 tick 摊开；
 * {@code energyUsage} 是<b>基础待机损耗</b>（常驻，充电耗电另算），与 RS 其他机械同属
 * 「在网络上先扣掉」的开销。待机之所以也按秒计，是为了不出现「站空转就把有线充电全吃掉」的倒挂。
 * {@code idleEnergyUsageEnabled} 是待机损耗的<b>配置总闸</b>，关掉后待机不再扣电。
 * {@code wirelessChargeEfficiencyPercent} 是<b>无线充电效率</b>（充入量 ÷ 消耗量，百分比），
 * 剩余部分作为无线传输损耗；有线充电恒为 100%。
 *
 * @param energyUsage                      基础待机损耗（FE/s）
 * @param idleEnergyUsageEnabled           是否启用待机损耗（配置总闸）
 * @param wiredChargeRate                  有线充电速率（FE/s）
 * @param wirelessChargeRate               无线充电速率（FE/s，每个物品）
 * @param wirelessChargeEfficiencyPercent  无线充电效率（百分比，45 = 消耗 100 充 45）
 * @param wirelessRadius                   无线充电半径（格）
 * @param externalCapacity                 内部 FE 缓冲容量（FE）
 */
public record ChargingStationDefaults(long energyUsage,
                                      boolean idleEnergyUsageEnabled,
                                      long wiredChargeRate,
                                      long wirelessChargeRate,
                                      int wirelessChargeEfficiencyPercent,
                                      int wirelessRadius,
                                      long externalCapacity) {
}
