package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.DefaultEnergyUsage;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationDefaults;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@me.shedaniel.autoconfig.annotation.Config(name = "rswirelesscrafting")
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class ConfigImpl implements ConfigData, Config {
    @ConfigEntry.Gui.CollapsibleObject
    private WirelessCraftingGridEntryEntryImpl wirelessCraftingGrid = new WirelessCraftingGridEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private DistanceEnergyEntryEntryImpl distanceEnergy = new DistanceEnergyEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ChargingStationEntryEntryImpl chargingStation = new ChargingStationEntryEntryImpl();

    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
    }

    @Override
    public WirelessCraftingGridEntry getWirelessCraftingGrid() {
        return wirelessCraftingGrid;
    }

    @Override
    public DistanceEnergyEntry getDistanceEnergy() {
        return distanceEnergy;
    }

    @Override
    public ChargingStationEntry getChargingStation() {
        return chargingStation;
    }

    private static class WirelessCraftingGridEntryEntryImpl implements WirelessCraftingGridEntry {
        @ConfigEntry.BoundedDiscrete(min = 1L, max = Long.MAX_VALUE)
        private long energyCapacity = DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_CAPACITY;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long batteryUpgradeCapacity = DefaultEnergyUsage.BATTERY_UPGRADE_CAPACITY;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long openEnergyUsage = DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_OPEN;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long craftingEnergyUsage = DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_CRAFTING;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long autocraftingEnergyUsage = DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_AUTOCRAFTING;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long clearMatrixEnergyUsage = DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_CLEAR_MATRIX;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long recipeTransferEnergyUsage = DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_RECIPE_TRANSFER;

        @Override
        public long getEnergyCapacity() {
            return energyCapacity;
        }

        @Override
        public long getBatteryUpgradeCapacity() {
            return batteryUpgradeCapacity;
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage;
        }

        @Override
        public long getCraftingEnergyUsage() {
            return craftingEnergyUsage;
        }

        @Override
        public long getAutocraftingEnergyUsage() {
            return autocraftingEnergyUsage;
        }

        @Override
        public long getClearMatrixEnergyUsage() {
            return clearMatrixEnergyUsage;
        }

        @Override
        public long getRecipeTransferEnergyUsage() {
            return recipeTransferEnergyUsage;
        }
    }

    private static class DistanceEnergyEntryEntryImpl implements DistanceEnergyEntry {
        @ConfigEntry.BoundedDiscrete(min = 1, max = Integer.MAX_VALUE)
        private int baseRange = DefaultEnergyUsage.DISTANCE_BASE_RANGE;
        @ConfigEntry.BoundedDiscrete(min = 100, max = Integer.MAX_VALUE)
        private int interdimensionalMultiplier = DefaultEnergyUsage.INTERDIMENSIONAL_ENERGY_MULTIPLIER;
        @ConfigEntry.BoundedDiscrete(min = 1, max = Integer.MAX_VALUE)
        private int basicEnergyDivisor = DefaultEnergyUsage.BASIC_ENERGY_DIVISOR;

        @Override
        public int getBaseRange() {
            return baseRange;
        }

        @Override
        public int getInterdimensionalMultiplier() {
            return interdimensionalMultiplier;
        }

        @Override
        public int getBasicEnergyDivisor() {
            return basicEnergyDivisor;
        }
    }

    private static class ChargingStationEntryEntryImpl implements ChargingStationEntry {
        private static final ChargingStationDefaults DEFAULTS = DefaultEnergyUsage.CHARGING_STATION;

        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long energyUsage = DEFAULTS.energyUsage();
        private boolean idleEnergyUsageEnabled = DEFAULTS.idleEnergyUsageEnabled();
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long wiredChargeRate = DEFAULTS.wiredChargeRate();
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long wirelessChargeRate = DEFAULTS.wirelessChargeRate();
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        private int wirelessChargeEfficiencyPercent = DEFAULTS.wirelessChargeEfficiencyPercent();
        @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
        private int wirelessRadius = DEFAULTS.wirelessRadius();
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long externalCapacity = DEFAULTS.externalCapacity();

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public boolean isIdleEnergyUsageEnabled() {
            return idleEnergyUsageEnabled;
        }

        @Override
        public long getWiredChargeRate() {
            return wiredChargeRate;
        }

        @Override
        public long getWirelessChargeRate() {
            return wirelessChargeRate;
        }

        @Override
        public int getWirelessChargeEfficiencyPercent() {
            return wirelessChargeEfficiencyPercent;
        }

        @Override
        public int getWirelessRadius() {
            return wirelessRadius;
        }

        @Override
        public long getExternalCapacity() {
            return externalCapacity;
        }
    }
}
