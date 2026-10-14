package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockItem;
import com.kokuu.rswirelesscrafting.common.upgrade.BatteryUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.InterdimensionalUpgradeItem;
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
    private Supplier<RangeUpgradeItem> rangeUpgrade;
    @Nullable
    private Supplier<BatteryUpgradeItem> batteryUpgrade;
    @Nullable
    private Supplier<InterdimensionalUpgradeItem> interdimensionalUpgrade;
    @Nullable
    private Supplier<RemoteEnergyUpgradeItem> remoteEnergyUpgrade;
    @Nullable
    private Supplier<ChargingStationBlockItem> terminalChargingStation;

    private Items() {
    }

    public WirelessCraftingGridItem getWirelessCraftingGrid() {
        return requireNonNull(wirelessCraftingGrid).get();
    }

    public void setWirelessCraftingGrid(final Supplier<WirelessCraftingGridItem> supplier) {
        this.wirelessCraftingGrid = supplier;
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

    public ChargingStationBlockItem getTerminalChargingStationBlockItem() {
        return requireNonNull(terminalChargingStation).get();
    }

    public void setTerminalChargingStationBlockItem(final Supplier<ChargingStationBlockItem> supplier) {
        this.terminalChargingStation = supplier;
    }
}
