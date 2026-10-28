package com.kokuu.rswirelesscrafting.common.smartsorter;

/**
 * 智能分拣站的默认数值。
 *
 * @param energyPerStack   每搬动一组物品消耗的网络能量（FE）
 * @param stacksPerSecond  搬运速率上限（组/秒），防止一次抽干网络
 */
public record SmartSorterDefaults(long energyPerStack, int stacksPerSecond) {
}
