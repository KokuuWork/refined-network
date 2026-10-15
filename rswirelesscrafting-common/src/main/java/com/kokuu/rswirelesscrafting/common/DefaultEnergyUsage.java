package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierDefaults;
import com.kokuu.rswirelesscrafting.common.bridge.BridgeDefaults;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationDefaults;

public final class DefaultEnergyUsage {
    public static final long WIRELESS_CRAFTING_GRID_CAPACITY = 400;
    // 合成终端的「合成」类动作扣费显著高于「查看 / 清空 / 配方转移」，
    // 体现合成终端（区别于普通无线终端）的额外成本。
    public static final long WIRELESS_CRAFTING_GRID_OPEN = 5;
    public static final long WIRELESS_CRAFTING_GRID_CRAFTING = 20;
    public static final long WIRELESS_CRAFTING_GRID_AUTOCRAFTING = 40;
    public static final long WIRELESS_CRAFTING_GRID_CLEAR_MATRIX = 5;
    public static final long WIRELESS_CRAFTING_GRID_RECIPE_TRANSFER = 5;
    public static final long BATTERY_UPGRADE_CAPACITY = 1000;

    // 无线设备「按距离扣电」：距离 ≤ 基础范围时耗电不变（×1），超出后按距离线性增长——
    // 倍率 = 距离 / 基础范围（百分比取整）。距离越远越贵，让能耗插件 / 远程输电插件真正有价值。
    // 例：基础范围 16 格时，32 格 = ×2、64 格 = ×4、256 格 = ×16、400 格 = ×25、1000 格 ≈ ×62。
    public static final int DISTANCE_BASE_RANGE = 16;
    // 跨维度是「超远距离」的等价物，固定按 ×16 大幅加价（能耗插件不减免跨维度倍率），
    // 让远程输电插件（直接抽网络电、不扣物品电池）在跨维度时成为必需品。
    public static final int INTERDIMENSIONAL_ENERGY_MULTIPLIER = 1600;
    public static final int BASIC_ENERGY_DIVISOR = 2;

    // 终端充电站（单档）。三个速率都是每秒（FE/s），方块内部按 20 tick/s 分摊，
    // 不是每 tick：有线 200 FE/s = 10 FE/t，无线 20 FE/s = 1 FE/t（每个设备）。
    // 待机损耗提到 128 FE/s（约 6.4 FE/t）：有惩罚感，红石开关才有省电意义；
    // 充电耗电在 chargeItem 里另算，不与待机混在一起。无线充电只有 45% 效率
    // （消耗 100 只给设备充 45，其余作传输损耗），有线充电 100% 无损耗。
    // 充电站自带内部 FE 缓冲（externalCapacity），未满时按固定速率从网络补电；
    // 界面显示的是这个缓冲电量，而非整个网络的能量。
    public static final ChargingStationDefaults CHARGING_STATION = new ChargingStationDefaults(
        128,   // 基础待机损耗 FE/s
        true,  // 待机损耗开关（配置总闸，默认开）
        200,   // 有线充电 FE/s（= 10 FE/t，每个槽位）
        20,    // 无线充电 FE/s（= 1 FE/t，每个设备）
        45,    // 无线充电效率（%，消耗 100 充 45）
        5,     // 无线半径（格）
        4000   // 内部 FE 缓冲容量（主电量池，界面显示的就是它）
    );

    // 无线增幅塔：接在网络上的常驻负载（128 FE/s ≈ 6.4 FE/t），覆盖 64 格范围内的无线终端
    // 距离倍率打对折。跨维度不受影响——那条线留给维度升级卡，不给塔打折。
    // 贵、常驻耗电、范围有限，是「要不要在矿场建塔」这个决策的三条腿。
    public static final AmplifierDefaults AMPLIFIER = new AmplifierDefaults(
        128,   // 常驻耗电 FE/s
        64,    // 覆盖半径（格）
        50     // 覆盖范围内的距离倍率（%，50 = 打对折）
    );

    // 无线桥：一对方块把网络延伸到远处（可跨维度、自带区块保活）。耗电按「每座桥」常驻计，
    // 配对的两端各算一座。刻意不做「按距离加倍」——距离的代价已经体现在「要不要在那边建一座桥」
    // 这个决策里，而且按距离计费需要把物品侧的距离公式复制一份到方块侧，容易两边分叉。
    public static final BridgeDefaults BRIDGE = new BridgeDefaults(
        64,    // 每座桥常驻耗电 FE/s
        true,  // 据点自动保活两端区块
        16     // 每个被保活的区块附加 FE/s（配对时两端共 2 个）
    );

    private DefaultEnergyUsage() {
    }
}
