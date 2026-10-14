package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationDefaults;

public final class DefaultEnergyUsage {
    public static final long WIRELESS_CRAFTING_GRID_CAPACITY = 1000;
    public static final long WIRELESS_CRAFTING_GRID_OPEN = 5;
    public static final long WIRELESS_CRAFTING_GRID_CRAFTING = 5;
    public static final long WIRELESS_CRAFTING_GRID_AUTOCRAFTING = 10;
    public static final long WIRELESS_CRAFTING_GRID_CLEAR_MATRIX = 5;
    public static final long WIRELESS_CRAFTING_GRID_RECIPE_TRANSFER = 5;
    public static final long INTERDIMENSIONAL_ENERGY_MULTIPLIER = 150;
    public static final int EXTENDED_RANGE = 64;

    // 400V 风冷充电站：入门款，速度快、范围小、待机开销低。
    // 注意：充电站的三个速率都是每秒（FE/s），方块内部按 20 tick/s 分摊，不是每 tick。
    public static final ChargingStationDefaults AIR_COOLED_CHARGING_STATION = new ChargingStationDefaults(
        8,     // 待机耗电 FE/s（= 0.4 FE/t）
        120,   // 有线充电 FE/s（= 6 FE/t）
        20,    // 无线充电 FE/s（= 1 FE/t，每个物品）
        4,     // 无线半径（格）
        10000  // 内部 FE 缓冲
    );

    // 800V 液冷充电站：终局款，速率与范围大幅提升，代价是待机开销与缓冲要求都更高。
    public static final ChargingStationDefaults LIQUID_COOLED_CHARGING_STATION = new ChargingStationDefaults(
        32,    // 待机耗电 FE/s（= 1.6 FE/t）
        240,   // 有线充电 FE/s（= 12 FE/t）
        80,    // 无线充电 FE/s（= 4 FE/t，每个物品）
        10,    // 无线半径（格）
        40000  // 内部 FE 缓冲
    );

    public static final long BATTERY_UPGRADE_CAPACITY = 1000;

    private DefaultEnergyUsage() {
    }
}
