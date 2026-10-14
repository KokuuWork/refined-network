package com.kokuu.rswirelesscrafting.common;

public interface Config {
    WirelessCraftingGridEntry getWirelessCraftingGrid();

    ChargingStationEntry getAirCooledChargingStation();

    ChargingStationEntry getLiquidCooledChargingStation();

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

    interface ChargingStationEntry {
        long getEnergyUsage();

        long getWiredChargeRate();

        long getWirelessChargeRate();

        int getWirelessRadius();

        long getExternalCapacity();
    }
}
