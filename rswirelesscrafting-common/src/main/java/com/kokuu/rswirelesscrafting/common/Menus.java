package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationContainerMenu;
import com.kokuu.rswirelesscrafting.common.monitor.WirelessMonitorContainerMenu;
import com.kokuu.rswirelesscrafting.common.smartsorter.SmartSorterContainerMenu;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridContainerMenu;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridUpgradeContainerMenu;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.inventory.MenuType;

import static java.util.Objects.requireNonNull;

public final class Menus {
    public static final Menus INSTANCE = new Menus();

    @Nullable
    private Supplier<MenuType<WirelessCraftingGridContainerMenu>> wirelessCraftingGrid;
    @Nullable
    private Supplier<MenuType<WirelessCraftingGridUpgradeContainerMenu>> wirelessCraftingGridUpgrades;
    @Nullable
    private Supplier<MenuType<ChargingStationContainerMenu>> chargingStation;
    @Nullable
    private Supplier<MenuType<WirelessMonitorContainerMenu>> wirelessMonitor;
    @Nullable
    private Supplier<MenuType<SmartSorterContainerMenu>> smartSorter;

    private Menus() {
    }

    public void setWirelessCraftingGrid(final Supplier<MenuType<WirelessCraftingGridContainerMenu>> supplier) {
        this.wirelessCraftingGrid = supplier;
    }

    public MenuType<WirelessCraftingGridContainerMenu> getWirelessCraftingGrid() {
        return requireNonNull(wirelessCraftingGrid).get();
    }

    public void setWirelessCraftingGridUpgrades(
        final Supplier<MenuType<WirelessCraftingGridUpgradeContainerMenu>> supplier
    ) {
        this.wirelessCraftingGridUpgrades = supplier;
    }

    public MenuType<WirelessCraftingGridUpgradeContainerMenu> getWirelessCraftingGridUpgrades() {
        return requireNonNull(wirelessCraftingGridUpgrades).get();
    }

    public void setChargingStation(final Supplier<MenuType<ChargingStationContainerMenu>> supplier) {
        this.chargingStation = supplier;
    }

    public MenuType<ChargingStationContainerMenu> getChargingStation() {
        return requireNonNull(chargingStation).get();
    }

    public void setWirelessMonitor(final Supplier<MenuType<WirelessMonitorContainerMenu>> supplier) {
        this.wirelessMonitor = supplier;
    }

    public MenuType<WirelessMonitorContainerMenu> getWirelessMonitor() {
        return requireNonNull(wirelessMonitor).get();
    }

    public void setSmartSorter(final Supplier<MenuType<SmartSorterContainerMenu>> supplier) {
        this.smartSorter = supplier;
    }

    public MenuType<SmartSorterContainerMenu> getSmartSorter() {
        return requireNonNull(smartSorter).get();
    }
}
