package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationContainerMenu;
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
}
