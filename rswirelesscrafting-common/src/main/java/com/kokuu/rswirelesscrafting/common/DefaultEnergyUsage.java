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

    // 终端充电站（单档）。三个速率都是每秒（FE/s），方块内部按 20 tick/s 分摊，
    // 不是每 tick：有线 200 FE/s = 10 FE/t，无线 20 FE/s = 1 FE/t（每个设备）。
    public static final ChargingStationDefaults CHARGING_STATION = new ChargingStationDefaults(
        8,     // 待机耗电 FE/s
        200,   // 有线充电 FE/s（= 10 FE/t，每个槽位）
        20,    // 无线充电 FE/s（= 1 FE/t，每个设备）
        5,     // 无线半径（格）
        10000  // 内部 FE 缓冲
    );

    public static final long BATTERY_UPGRADE_CAPACITY = 1000;

    private DefaultEnergyUsage() {
    }
}
