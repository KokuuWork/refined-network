package com.kokuu.rswirelesscrafting.common;

public interface Config {
    WirelessCraftingGridEntry getWirelessCraftingGrid();

    DistanceEnergyEntry getDistanceEnergy();

    ChargingStationEntry getChargingStation();

    interface WirelessCraftingGridEntry {
        long getEnergyCapacity();

        long getBatteryUpgradeCapacity();

        long getOpenEnergyUsage();

        long getCraftingEnergyUsage();

        long getAutocraftingEnergyUsage();

        long getClearMatrixEnergyUsage();

        long getRecipeTransferEnergyUsage();
    }

    /**
     * 无线设备「按距离扣电」的价目表。
     *
     * <p>同一维度内，设备与绑定网络的水平距离落入某个分段，就按该分段的倍率（百分比整数）
     * 乘到每次动作的基础耗电上；跨维度时距离失去意义，改为套用 {@link #getInterdimensionalMultiplier()}。
     * 四段倍率默认 ×1 / ×2 / ×4 / ×8，越远越贵；「初级能耗插件」把距离倍率减半、
     * 「高级能耗插件」把距离倍率归 1（均不影响跨维度倍率）。
     */
    interface DistanceEnergyEntry {
        int getTier1Range();

        int getTier2Range();

        int getTier3Range();

        int getTier1Multiplier();

        int getTier2Multiplier();

        int getTier3Multiplier();

        int getTier4Multiplier();

        int getInterdimensionalMultiplier();

        int getBasicEnergyDivisor();
    }

    /**
     * 一档充电站的配置。
     *
     * <p>{@code energyUsage} 是<b>基础待机损耗</b>（常驻，充电另算），{@code wiredChargeRate}、
     * {@code wirelessChargeRate} 是充电速率，单位都是 <b>每秒（FE/s）</b>，方块内部按每秒
     * 20 tick 摊到每 tick；{@code wirelessChargeRate} 指的是<b>每个物品</b>的额度。
     *
     * <p>{@link #isIdleEnergyUsageEnabled()} 是待机损耗的<b>配置总闸</b>（关掉后永不扣）；
     * 游戏内还可用红石信号临时关掉无线充电并停扣待机损耗，作为省电开关。
     *
     * <p>{@link #getWirelessChargeEfficiencyPercent()} 是<b>无线充电效率</b>：消耗的能量里只有
     * 这个百分比真正充进设备，其余作为传输损耗蒸发；有线充电恒为 100%（无损耗）。
     */
    interface ChargingStationEntry {
        long getEnergyUsage();

        boolean isIdleEnergyUsageEnabled();

        long getWiredChargeRate();

        long getWirelessChargeRate();

        int getWirelessChargeEfficiencyPercent();

        int getWirelessRadius();

        long getExternalCapacity();
    }
}
