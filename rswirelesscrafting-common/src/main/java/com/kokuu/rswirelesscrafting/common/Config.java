package com.kokuu.rswirelesscrafting.common;

public interface Config {
    WirelessCraftingGridEntry getWirelessCraftingGrid();

    ChargingStationEntry getChargingStation();

    interface WirelessCraftingGridEntry {
        long getEnergyCapacity();

        long getBatteryUpgradeCapacity();

        long getOpenEnergyUsage();

        long getCraftingEnergyUsage();

        long getAutocraftingEnergyUsage();

        long getClearMatrixEnergyUsage();

        long getRecipeTransferEnergyUsage();

        long getInterdimensionalEnergyMultiplier();

        int getExtendedRange();
    }

    /**
     * 一档充电站的配置。
     *
     * <p>{@code energyUsage}、{@code wiredChargeRate}、{@code wirelessChargeRate} 的单位都是
     * <b>每秒（FE/s）</b>，方块内部按每秒 20 tick 摊到每 tick；{@code wirelessChargeRate}
     * 指的是<b>每个物品</b>的额度。
     */
    interface ChargingStationEntry {
        long getEnergyUsage();

        long getWiredChargeRate();

        long getWirelessChargeRate();

        int getWirelessRadius();

        long getExternalCapacity();
    }
}
