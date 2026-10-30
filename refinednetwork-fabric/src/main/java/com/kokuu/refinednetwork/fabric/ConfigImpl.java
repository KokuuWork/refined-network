package com.kokuu.refinednetwork.fabric;

import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.DefaultEnergyUsage;
import com.kokuu.refinednetwork.common.chargingstation.ChargingStationDefaults;
import com.kokuu.refinednetwork.common.energymonitor.EnergyMonitorDefaults;
import com.kokuu.refinednetwork.common.energyreceiver.EnergyReceiverDefaults;
import com.kokuu.refinednetwork.common.networkbridge.NetworkBridgeDefaults;
import com.kokuu.refinednetwork.common.smartsorter.SmartSorterDefaults;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@me.shedaniel.autoconfig.annotation.Config(name = "refinednetwork")
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class ConfigImpl implements ConfigData, Config {
    @ConfigEntry.Gui.CollapsibleObject
    private WirelessCraftingGridEntryEntryImpl wirelessCraftingGrid = new WirelessCraftingGridEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private DistanceEnergyEntryEntryImpl distanceEnergy = new DistanceEnergyEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ChargingStationEntryEntryImpl chargingStation = new ChargingStationEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private HudEntryEntryImpl hud = new HudEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private AmplifierEntryEntryImpl wirelessAmplifier = new AmplifierEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private BridgeEntryEntryImpl wirelessBridge = new BridgeEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private EnergyReceiverEntryEntryImpl energyReceiver = new EnergyReceiverEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private EnergyMonitorEntryEntryImpl energyMonitor = new EnergyMonitorEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SmartSorterEntryEntryImpl smartSorter = new SmartSorterEntryEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private NetworkBridgeEntryEntryImpl networkBridge = new NetworkBridgeEntryEntryImpl();

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

    @Override
    public HudEntry getHud() {
        return hud;
    }

    @Override
    public AmplifierEntry getAmplifier() {
        return wirelessAmplifier;
    }

    @Override
    public BridgeEntry getBridge() {
        return wirelessBridge;
    }

    @Override
    public EnergyReceiverEntry getEnergyReceiver() {
        return energyReceiver;
    }

    @Override
    public EnergyMonitorEntry getEnergyMonitor() {
        return energyMonitor;
    }

    @Override
    public SmartSorterEntry getSmartSorter() {
        return smartSorter;
    }

    @Override
    public NetworkBridgeEntry getNetworkBridge() {
        return networkBridge;
    }

    private static class HudEntryEntryImpl implements HudEntry {
        private boolean enabled = false;

        @Override
        public boolean isEnabled() {
            return enabled;
        }
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
        private boolean offlineModeEnabled = true;

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

        @Override
        public boolean isOfflineModeEnabled() {
            return offlineModeEnabled;
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

    private static class AmplifierEntryEntryImpl implements AmplifierEntry {
        private boolean enabled = true;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long energyUsage = DefaultEnergyUsage.AMPLIFIER.energyUsage();
        @ConfigEntry.BoundedDiscrete(min = 0L, max = 256L)
        private int radius = DefaultEnergyUsage.AMPLIFIER.radius();
        @ConfigEntry.BoundedDiscrete(min = 1L, max = 100L)
        private int multiplierPercent = DefaultEnergyUsage.AMPLIFIER.multiplierPercent();

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public int getRadius() {
            return radius;
        }

        @Override
        public int getMultiplierPercent() {
            return multiplierPercent;
        }
    }

    private static class BridgeEntryEntryImpl implements BridgeEntry {
        private boolean enabled = true;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long energyUsage = DefaultEnergyUsage.BRIDGE.energyUsage();
        private boolean chunkLoading = DefaultEnergyUsage.BRIDGE.chunkLoading();
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long chunkLoadingEnergyUsage = DefaultEnergyUsage.BRIDGE.chunkLoadingEnergyUsage();

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public boolean isChunkLoadingEnabled() {
            return chunkLoading;
        }

        @Override
        public long getChunkLoadingEnergyUsage() {
            return chunkLoadingEnergyUsage;
        }
    }

    private static class EnergyReceiverEntryEntryImpl implements EnergyReceiverEntry {
        private static final EnergyReceiverDefaults DEFAULTS = DefaultEnergyUsage.ENERGY_RECEIVER;

        private boolean enabled = true;
        @ConfigEntry.BoundedDiscrete(min = 1L, max = Long.MAX_VALUE)
        private long capacity = DEFAULTS.capacity();
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long energyUsage = DEFAULTS.energyUsage();

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class EnergyMonitorEntryEntryImpl implements EnergyMonitorEntry {
        private static final EnergyMonitorDefaults DEFAULTS = DefaultEnergyUsage.ENERGY_MONITOR;

        private boolean enabled = true;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long energyUsage = DEFAULTS.energyUsage();
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        private int lowThresholdPercent = DEFAULTS.lowThresholdPercent();
        @ConfigEntry.BoundedDiscrete(min = 1, max = 200)
        private int sampleInterval = DEFAULTS.sampleInterval();

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public int getLowThresholdPercent() {
            return lowThresholdPercent;
        }

        @Override
        public int getSampleInterval() {
            return sampleInterval;
        }
    }

    private static class SmartSorterEntryEntryImpl implements SmartSorterEntry {
        private static final SmartSorterDefaults DEFAULTS = DefaultEnergyUsage.SMART_SORTER;

        private boolean enabled = true;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long energyPerStack = DEFAULTS.energyPerStack();
        @ConfigEntry.BoundedDiscrete(min = 1, max = 64)
        private int stacksPerSecond = DEFAULTS.stacksPerSecond();

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public long getEnergyPerStack() {
            return energyPerStack;
        }

        @Override
        public int getStacksPerSecond() {
            return stacksPerSecond;
        }
    }

    private static class NetworkBridgeEntryEntryImpl implements NetworkBridgeEntry {
        private static final NetworkBridgeDefaults DEFAULTS = DefaultEnergyUsage.NETWORK_BRIDGE;

        private boolean enabled = true;
        @ConfigEntry.BoundedDiscrete(min = 0L, max = Long.MAX_VALUE)
        private long energyUsage = DEFAULTS.energyUsage();
        @ConfigEntry.BoundedDiscrete(min = 1L, max = Long.MAX_VALUE)
        private long itemTransferRate = DEFAULTS.itemTransferRate();
        @ConfigEntry.BoundedDiscrete(min = 1L, max = Long.MAX_VALUE)
        private long energyTransferRate = DEFAULTS.energyTransferRate();

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public long getItemTransferRate() {
            return itemTransferRate;
        }

        @Override
        public long getEnergyTransferRate() {
            return energyTransferRate;
        }
    }
}
