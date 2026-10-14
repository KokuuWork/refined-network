package com.kokuu.rswirelesscrafting.neoforge;

import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.DefaultEnergyUsage;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationDefaults;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createTranslationKey;

public class ConfigImpl implements Config {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;
    private final WirelessCraftingGridEntry wirelessCraftingGrid;
    private final ChargingStationEntry airCooledChargingStation;
    private final ChargingStationEntry liquidCooledChargingStation;

    public ConfigImpl() {
        wirelessCraftingGrid = new WirelessCraftingGridEntryImpl();
        airCooledChargingStation = new ChargingStationEntryImpl(
            "airCooledChargingStation",
            DefaultEnergyUsage.AIR_COOLED_CHARGING_STATION
        );
        liquidCooledChargingStation = new ChargingStationEntryImpl(
            "liquidCooledChargingStation",
            DefaultEnergyUsage.LIQUID_COOLED_CHARGING_STATION
        );
        spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return spec;
    }

    @Override
    public WirelessCraftingGridEntry getWirelessCraftingGrid() {
        return wirelessCraftingGrid;
    }

    @Override
    public ChargingStationEntry getAirCooledChargingStation() {
        return airCooledChargingStation;
    }

    @Override
    public ChargingStationEntry getLiquidCooledChargingStation() {
        return liquidCooledChargingStation;
    }

    private static String translationKey(final String value) {
        return createTranslationKey("text.autoconfig", "option." + value);
    }

    private class WirelessCraftingGridEntryImpl implements WirelessCraftingGridEntry {
        private final ModConfigSpec.LongValue energyCapacity;
        private final ModConfigSpec.LongValue batteryUpgradeCapacity;
        private final ModConfigSpec.LongValue openEnergyUsage;
        private final ModConfigSpec.LongValue craftingEnergyUsage;
        private final ModConfigSpec.LongValue autocraftingEnergyUsage;
        private final ModConfigSpec.LongValue clearMatrixEnergyUsage;
        private final ModConfigSpec.LongValue recipeTransferEnergyUsage;
        private final ModConfigSpec.LongValue interdimensionalEnergyMultiplier;
        private final ModConfigSpec.IntValue extendedRange;

        WirelessCraftingGridEntryImpl() {
            builder.translation(translationKey("wirelessCraftingGrid")).push("wirelessCraftingGrid");
            energyCapacity = builder
                .translation(translationKey("wirelessCraftingGrid.energyCapacity"))
                .defineInRange("energyCapacity", DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_CAPACITY, 1, Long.MAX_VALUE);
            batteryUpgradeCapacity = builder
                .translation(translationKey("wirelessCraftingGrid.batteryUpgradeCapacity"))
                .comment("How much extra energy capacity a single Battery Upgrade adds.")
                .defineInRange("batteryUpgradeCapacity", DefaultEnergyUsage.BATTERY_UPGRADE_CAPACITY, 0,
                    Long.MAX_VALUE);
            openEnergyUsage = builder
                .translation(translationKey("wirelessCraftingGrid.openEnergyUsage"))
                .defineInRange("openEnergyUsage", DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_OPEN, 0, Long.MAX_VALUE);
            craftingEnergyUsage = builder
                .translation(translationKey("wirelessCraftingGrid.craftingEnergyUsage"))
                .defineInRange("craftingEnergyUsage", DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_CRAFTING, 0,
                    Long.MAX_VALUE);
            autocraftingEnergyUsage = builder
                .translation(translationKey("wirelessCraftingGrid.autocraftingEnergyUsage"))
                .defineInRange("autocraftingEnergyUsage", DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_AUTOCRAFTING, 0,
                    Long.MAX_VALUE);
            clearMatrixEnergyUsage = builder
                .translation(translationKey("wirelessCraftingGrid.clearMatrixEnergyUsage"))
                .defineInRange("clearMatrixEnergyUsage", DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_CLEAR_MATRIX, 0,
                    Long.MAX_VALUE);
            recipeTransferEnergyUsage = builder
                .translation(translationKey("wirelessCraftingGrid.recipeTransferEnergyUsage"))
                .defineInRange("recipeTransferEnergyUsage", DefaultEnergyUsage.WIRELESS_CRAFTING_GRID_RECIPE_TRANSFER,
                    0, Long.MAX_VALUE);
            interdimensionalEnergyMultiplier = builder
                .translation(translationKey("wirelessCraftingGrid.interdimensionalEnergyMultiplier"))
                .comment("Energy multiplier (in percent) applied when the grid is used across dimensions.")
                .defineInRange("interdimensionalEnergyMultiplier",
                    DefaultEnergyUsage.INTERDIMENSIONAL_ENERGY_MULTIPLIER, 100, Long.MAX_VALUE);
            extendedRange = builder
                .translation(translationKey("wirelessCraftingGrid.extendedRange"))
                .comment("Minimum effective range (in blocks) of the wireless crafting grid in the same dimension.")
                .defineInRange("extendedRange", DefaultEnergyUsage.EXTENDED_RANGE, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity.get();
        }

        @Override
        public long getBatteryUpgradeCapacity() {
            return batteryUpgradeCapacity.get();
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage.get();
        }

        @Override
        public long getCraftingEnergyUsage() {
            return craftingEnergyUsage.get();
        }

        @Override
        public long getAutocraftingEnergyUsage() {
            return autocraftingEnergyUsage.get();
        }

        @Override
        public long getClearMatrixEnergyUsage() {
            return clearMatrixEnergyUsage.get();
        }

        @Override
        public long getRecipeTransferEnergyUsage() {
            return recipeTransferEnergyUsage.get();
        }

        @Override
        public long getInterdimensionalEnergyMultiplier() {
            return interdimensionalEnergyMultiplier.get();
        }

        @Override
        public int getExtendedRange() {
            return extendedRange.get();
        }
    }

    private class ChargingStationEntryImpl implements ChargingStationEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.LongValue wiredChargeRate;
        private final ModConfigSpec.LongValue wirelessChargeRate;
        private final ModConfigSpec.IntValue wirelessRadius;
        private final ModConfigSpec.LongValue externalCapacity;

        ChargingStationEntryImpl(final String path, final ChargingStationDefaults defaults) {
            builder.translation(translationKey(path)).push(path);
            energyUsage = builder
                .translation(translationKey(path + ".energyUsage"))
                .comment("Idle energy usage of this Charging Station on the network.")
                .defineInRange("energyUsage", defaults.energyUsage(), 0, Long.MAX_VALUE);
            wiredChargeRate = builder
                .translation(translationKey(path + ".wiredChargeRate"))
                .comment("Energy per tick transferred to the item in the wired slot.")
                .defineInRange("wiredChargeRate", defaults.wiredChargeRate(), 0, Long.MAX_VALUE);
            wirelessChargeRate = builder
                .translation(translationKey(path + ".wirelessChargeRate"))
                .comment("Energy per tick transferred to nearby players' items.")
                .defineInRange("wirelessChargeRate", defaults.wirelessChargeRate(), 0,
                    Long.MAX_VALUE);
            wirelessRadius = builder
                .translation(translationKey(path + ".wirelessRadius"))
                .comment("Radius (in blocks) of the wireless charging area.")
                .defineInRange("wirelessRadius", defaults.wirelessRadius(), 0,
                    Integer.MAX_VALUE);
            externalCapacity = builder
                .translation(translationKey(path + ".externalCapacity"))
                .comment("Internal FE buffer, used when other mods' energy blocks feed this station.")
                .defineInRange("externalCapacity", defaults.externalCapacity(), 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }

        @Override
        public long getWiredChargeRate() {
            return wiredChargeRate.get();
        }

        @Override
        public long getWirelessChargeRate() {
            return wirelessChargeRate.get();
        }

        @Override
        public int getWirelessRadius() {
            return wirelessRadius.get();
        }

        @Override
        public long getExternalCapacity() {
            return externalCapacity.get();
        }
    }
}
