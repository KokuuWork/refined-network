package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlockItem;
import com.kokuu.rswirelesscrafting.common.autocraftermanager.WirelessAutocrafterManagerItem;
import com.kokuu.rswirelesscrafting.common.bridge.WirelessBridgeBlockItem;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockItem;
import com.kokuu.rswirelesscrafting.common.configcard.WirelessConfigurationCardItem;
import com.kokuu.rswirelesscrafting.common.monitor.WirelessMonitorItem;
import com.kokuu.rswirelesscrafting.common.patterngrid.WirelessPatternGridItem;
import com.kokuu.rswirelesscrafting.common.upgrade.AdvancedEnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.BatteryUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.EnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.InterdimensionalUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.OverclockUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.RangeUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.RemoteEnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridItem;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Items {
    public static final Items INSTANCE = new Items();

    @Nullable
    private Supplier<WirelessCraftingGridItem> wirelessCraftingGrid;
    @Nullable
    private Supplier<WirelessPatternGridItem> wirelessPatternGrid;
    @Nullable
    private Supplier<WirelessAutocrafterManagerItem> wirelessAutocrafterManager;
    @Nullable
    private Supplier<RangeUpgradeItem> rangeUpgrade;
    @Nullable
    private Supplier<BatteryUpgradeItem> batteryUpgrade;
    @Nullable
    private Supplier<InterdimensionalUpgradeItem> interdimensionalUpgrade;
    @Nullable
    private Supplier<RemoteEnergyUpgradeItem> remoteEnergyUpgrade;
    @Nullable
    private Supplier<EnergyUpgradeItem> energyUpgrade;
    @Nullable
    private Supplier<AdvancedEnergyUpgradeItem> advancedEnergyUpgrade;
    @Nullable
    private Supplier<OverclockUpgradeItem> overclockUpgrade;
    @Nullable
    private Supplier<ChargingStationBlockItem> terminalChargingStation;
    @Nullable
    private Supplier<AmplifierBlockItem> wirelessAmplifier;
    @Nullable
    private Supplier<WirelessBridgeBlockItem> wirelessBridge;
    @Nullable
    private Supplier<WirelessConfigurationCardItem> wirelessConfigurationCard;
    @Nullable
    private Supplier<WirelessMonitorItem> wirelessMonitor;

    private Items() {
    }

    public WirelessCraftingGridItem getWirelessCraftingGrid() {
        return requireNonNull(wirelessCraftingGrid).get();
    }

    public void setWirelessCraftingGrid(final Supplier<WirelessCraftingGridItem> supplier) {
        this.wirelessCraftingGrid = supplier;
    }

    public WirelessPatternGridItem getWirelessPatternGrid() {
        return requireNonNull(wirelessPatternGrid).get();
    }

    public void setWirelessPatternGrid(final Supplier<WirelessPatternGridItem> supplier) {
        this.wirelessPatternGrid = supplier;
    }

    public WirelessAutocrafterManagerItem getWirelessAutocrafterManager() {
        return requireNonNull(wirelessAutocrafterManager).get();
    }

    public void setWirelessAutocrafterManager(final Supplier<WirelessAutocrafterManagerItem> supplier) {
        this.wirelessAutocrafterManager = supplier;
    }

    public RangeUpgradeItem getRangeUpgrade() {
        return requireNonNull(rangeUpgrade).get();
    }

    public void setRangeUpgrade(final Supplier<RangeUpgradeItem> supplier) {
        this.rangeUpgrade = supplier;
    }

    public BatteryUpgradeItem getBatteryUpgrade() {
        return requireNonNull(batteryUpgrade).get();
    }

    public void setBatteryUpgrade(final Supplier<BatteryUpgradeItem> supplier) {
        this.batteryUpgrade = supplier;
    }

    public InterdimensionalUpgradeItem getInterdimensionalUpgrade() {
        return requireNonNull(interdimensionalUpgrade).get();
    }

    public void setInterdimensionalUpgrade(final Supplier<InterdimensionalUpgradeItem> supplier) {
        this.interdimensionalUpgrade = supplier;
    }

    public RemoteEnergyUpgradeItem getRemoteEnergyUpgrade() {
        return requireNonNull(remoteEnergyUpgrade).get();
    }

    public void setRemoteEnergyUpgrade(final Supplier<RemoteEnergyUpgradeItem> supplier) {
        this.remoteEnergyUpgrade = supplier;
    }

    public EnergyUpgradeItem getEnergyUpgrade() {
        return requireNonNull(energyUpgrade).get();
    }

    public void setEnergyUpgrade(final Supplier<EnergyUpgradeItem> supplier) {
        this.energyUpgrade = supplier;
    }

    public AdvancedEnergyUpgradeItem getAdvancedEnergyUpgrade() {
        return requireNonNull(advancedEnergyUpgrade).get();
    }

    public void setAdvancedEnergyUpgrade(final Supplier<AdvancedEnergyUpgradeItem> supplier) {
        this.advancedEnergyUpgrade = supplier;
    }

    public OverclockUpgradeItem getOverclockUpgrade() {
        return requireNonNull(overclockUpgrade).get();
    }

    public void setOverclockUpgrade(final Supplier<OverclockUpgradeItem> supplier) {
        this.overclockUpgrade = supplier;
    }

    public ChargingStationBlockItem getTerminalChargingStationBlockItem() {
        return requireNonNull(terminalChargingStation).get();
    }

    public AmplifierBlockItem getWirelessAmplifierBlockItem() {
        return requireNonNull(wirelessAmplifier).get();
    }

    public void setWirelessAmplifierBlockItem(final Supplier<AmplifierBlockItem> supplier) {
        this.wirelessAmplifier = supplier;
    }

    public WirelessBridgeBlockItem getWirelessBridgeBlockItem() {
        return requireNonNull(wirelessBridge).get();
    }

    public void setWirelessBridgeBlockItem(final Supplier<WirelessBridgeBlockItem> supplier) {
        this.wirelessBridge = supplier;
    }

    public WirelessConfigurationCardItem getWirelessConfigurationCard() {
        return requireNonNull(wirelessConfigurationCard).get();
    }

    public void setWirelessConfigurationCard(final Supplier<WirelessConfigurationCardItem> supplier) {
        this.wirelessConfigurationCard = supplier;
    }

    public WirelessMonitorItem getWirelessMonitor() {
        return requireNonNull(wirelessMonitor).get();
    }

    public void setWirelessMonitor(final Supplier<WirelessMonitorItem> supplier) {
        this.wirelessMonitor = supplier;
    }

    public void setTerminalChargingStationBlockItem(final Supplier<ChargingStationBlockItem> supplier) {
        this.terminalChargingStation = supplier;
    }
}
