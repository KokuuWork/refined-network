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
    private final DistanceEnergyEntry distanceEnergy;
    private final ChargingStationEntry chargingStation;

    public ConfigImpl() {
        wirelessCraftingGrid = new WirelessCraftingGridEntryImpl();
        distanceEnergy = new DistanceEnergyEntryImpl();
        chargingStation = new ChargingStationEntryImpl(
            "chargingStation",
            DefaultEnergyUsage.CHARGING_STATION
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
    public DistanceEnergyEntry getDistanceEnergy() {
        return distanceEnergy;
    }

    @Override
    public ChargingStationEntry getChargingStation() {
        return chargingStation;
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
    }

    private class DistanceEnergyEntryImpl implements DistanceEnergyEntry {
        private final ModConfigSpec.IntValue tier1Range;
        private final ModConfigSpec.IntValue tier2Range;
        private final ModConfigSpec.IntValue tier3Range;
        private final ModConfigSpec.IntValue tier1Multiplier;
        private final ModConfigSpec.IntValue tier2Multiplier;
        private final ModConfigSpec.IntValue tier3Multiplier;
        private final ModConfigSpec.IntValue tier4Multiplier;
        private final ModConfigSpec.IntValue interdimensionalMultiplier;
        private final ModConfigSpec.IntValue basicEnergyDivisor;

        DistanceEnergyEntryImpl() {
            builder.translation(translationKey("distanceEnergy")).push("distanceEnergy");
            tier1Range = builder
                .translation(translationKey("distanceEnergy.tier1Range"))
                .comment("Upper bound (blocks) of the nearest distance tier.")
                .defineInRange("tier1Range", DefaultEnergyUsage.DISTANCE_TIER1_RANGE, 0, Integer.MAX_VALUE);
            tier2Range = builder
                .translation(translationKey("distanceEnergy.tier2Range"))
                .comment("Upper bound (blocks) of the second distance tier.")
                .defineInRange("tier2Range", DefaultEnergyUsage.DISTANCE_TIER2_RANGE, 0, Integer.MAX_VALUE);
            tier3Range = builder
                .translation(translationKey("distanceEnergy.tier3Range"))
                .comment("Upper bound (blocks) of the third distance tier; beyond it is the last tier.")
                .defineInRange("tier3Range", DefaultEnergyUsage.DISTANCE_TIER3_RANGE, 0, Integer.MAX_VALUE);
            tier1Multiplier = builder
                .translation(translationKey("distanceEnergy.tier1Multiplier"))
                .defineInRange("tier1Multiplier", DefaultEnergyUsage.DISTANCE_TIER1_MULTIPLIER, 1, Integer.MAX_VALUE);
            tier2Multiplier = builder
                .translation(translationKey("distanceEnergy.tier2Multiplier"))
                .defineInRange("tier2Multiplier", DefaultEnergyUsage.DISTANCE_TIER2_MULTIPLIER, 1, Integer.MAX_VALUE);
            tier3Multiplier = builder
                .translation(translationKey("distanceEnergy.tier3Multiplier"))
                .defineInRange("tier3Multiplier", DefaultEnergyUsage.DISTANCE_TIER3_MULTIPLIER, 1, Integer.MAX_VALUE);
            tier4Multiplier = builder
                .translation(translationKey("distanceEnergy.tier4Multiplier"))
                .defineInRange("tier4Multiplier", DefaultEnergyUsage.DISTANCE_TIER4_MULTIPLIER, 1, Integer.MAX_VALUE);
            interdimensionalMultiplier = builder
                .translation(translationKey("distanceEnergy.interdimensionalMultiplier"))
                .comment("Energy multiplier (percent) applied when the device is used across dimensions.")
                .defineInRange("interdimensionalMultiplier", DefaultEnergyUsage.INTERDIMENSIONAL_ENERGY_MULTIPLIER,
                    100, Integer.MAX_VALUE);
            basicEnergyDivisor = builder
                .translation(translationKey("distanceEnergy.basicEnergyDivisor"))
                .comment("The Basic Energy Upgrade divides the distance multiplier by this value.")
                .defineInRange("basicEnergyDivisor", DefaultEnergyUsage.BASIC_ENERGY_DIVISOR, 1, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public int getTier1Range() {
            return tier1Range.get();
        }

        @Override
        public int getTier2Range() {
            return tier2Range.get();
        }

        @Override
        public int getTier3Range() {
            return tier3Range.get();
        }

        @Override
        public int getTier1Multiplier() {
            return tier1Multiplier.get();
        }

        @Override
        public int getTier2Multiplier() {
            return tier2Multiplier.get();
        }

        @Override
        public int getTier3Multiplier() {
            return tier3Multiplier.get();
        }

        @Override
        public int getTier4Multiplier() {
            return tier4Multiplier.get();
        }

        @Override
        public int getInterdimensionalMultiplier() {
            return interdimensionalMultiplier.get();
        }

        @Override
        public int getBasicEnergyDivisor() {
            return basicEnergyDivisor.get();
        }
    }

    private class ChargingStationEntryImpl implements ChargingStationEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.BooleanValue idleEnergyUsageEnabled;
        private final ModConfigSpec.LongValue wiredChargeRate;
        private final ModConfigSpec.LongValue wirelessChargeRate;
        private final ModConfigSpec.IntValue wirelessChargeEfficiencyPercent;
        private final ModConfigSpec.IntValue wirelessRadius;
        private final ModConfigSpec.LongValue externalCapacity;

        ChargingStationEntryImpl(final String path, final ChargingStationDefaults defaults) {
            builder.translation(translationKey(path)).push(path);
            energyUsage = builder
                .translation(translationKey(path + ".energyUsage"))
                .comment("Base idle energy drain of this Charging Station, in FE per second. "
                    + "Charging itself is billed separately. A redstone signal shuts wireless "
                    + "charging off and suspends this drain, so keeping it high makes the redstone "
                    + "switch meaningful.")
                .defineInRange("energyUsage", defaults.energyUsage(), 0, Long.MAX_VALUE);
            idleEnergyUsageEnabled = builder
                .translation(translationKey(path + ".idleEnergyUsageEnabled"))
                .comment("Master switch for the idle energy drain. When disabled, the station "
                    + "never drains energy while idle (wireless charging still works).")
                .define("idleEnergyUsageEnabled", defaults.idleEnergyUsageEnabled());
            wiredChargeRate = builder
                .translation(translationKey(path + ".wiredChargeRate"))
                .comment("Energy per second transferred to the item in the wired slot.")
                .defineInRange("wiredChargeRate", defaults.wiredChargeRate(), 0, Long.MAX_VALUE);
            wirelessChargeRate = builder
                .translation(translationKey(path + ".wirelessChargeRate"))
                .comment("Energy per second transferred to each item in nearby players' inventories.")
                .defineInRange("wirelessChargeRate", defaults.wirelessChargeRate(), 0,
                    Long.MAX_VALUE);
            wirelessChargeEfficiencyPercent = builder
                .translation(translationKey(path + ".wirelessChargeEfficiencyPercent"))
                .comment("Wireless charging efficiency (percent): only this share of the energy "
                    + "drawn actually reaches the device, the rest is lost. Wired charging is always 100%.")
                .defineInRange("wirelessChargeEfficiencyPercent", defaults.wirelessChargeEfficiencyPercent(),
                    1, 100);
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
        public boolean isIdleEnergyUsageEnabled() {
            return idleEnergyUsageEnabled.get();
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
        public int getWirelessChargeEfficiencyPercent() {
            return wirelessChargeEfficiencyPercent.get();
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
