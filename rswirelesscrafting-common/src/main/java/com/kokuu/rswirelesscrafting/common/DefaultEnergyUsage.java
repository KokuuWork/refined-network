package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationDefaults;

public final class DefaultEnergyUsage {
    public static final long WIRELESS_CRAFTING_GRID_CAPACITY = 400;
    public static final long WIRELESS_CRAFTING_GRID_OPEN = 5;
    public static final long WIRELESS_CRAFTING_GRID_CRAFTING = 5;
    public static final long WIRELESS_CRAFTING_GRID_AUTOCRAFTING = 10;
    public static final long WIRELESS_CRAFTING_GRID_CLEAR_MATRIX = 5;
    public static final long WIRELESS_CRAFTING_GRID_RECIPE_TRANSFER = 5;
    public static final long BATTERY_UPGRADE_CAPACITY = 1000;

    // 无线设备「按距离扣电」的分段价目表。倍率都是百分比整数。
    public static final int DISTANCE_TIER1_RANGE = 10;
    public static final int DISTANCE_TIER2_RANGE = 100;
    public static final int DISTANCE_TIER3_RANGE = 1000;
    public static final int DISTANCE_TIER1_MULTIPLIER = 100;
    public static final int DISTANCE_TIER2_MULTIPLIER = 200;
    public static final int DISTANCE_TIER3_MULTIPLIER = 400;
    public static final int DISTANCE_TIER4_MULTIPLIER = 800;
    public static final int INTERDIMENSIONAL_ENERGY_MULTIPLIER = 150;
    public static final int BASIC_ENERGY_DIVISOR = 2;

    // 终端充电站（单档）。三个速率都是每秒（FE/s），方块内部按 20 tick/s 分摊，
    // 不是每 tick：有线 200 FE/s = 10 FE/t，无线 20 FE/s = 1 FE/t（每个设备）。
    // 待机损耗提到 128 FE/s（约 6.4 FE/t）：有惩罚感，红石开关才有省电意义；
    // 充电耗电在 chargeItem 里另算，不与待机混在一起。无线充电只有 45% 效率
    // （消耗 100 只给设备充 45，其余作传输损耗），有线充电 100% 无损耗。
    public static final ChargingStationDefaults CHARGING_STATION = new ChargingStationDefaults(
        128,   // 基础待机损耗 FE/s
        true,  // 待机损耗开关（配置总闸，默认开）
        200,   // 有线充电 FE/s（= 10 FE/t，每个槽位）
        20,    // 无线充电 FE/s（= 1 FE/t，每个设备）
        45,    // 无线充电效率（%，消耗 100 充 45）
        5,     // 无线半径（格）
        10000  // 内部 FE 缓冲
    );

    private DefaultEnergyUsage() {
    }
}
