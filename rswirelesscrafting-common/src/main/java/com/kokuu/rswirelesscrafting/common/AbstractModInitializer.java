package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationContainerMenu;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationData;
import com.kokuu.rswirelesscrafting.common.monitor.MonitorMenuData;
import com.kokuu.rswirelesscrafting.common.monitor.MonitorRule;
import com.kokuu.rswirelesscrafting.common.monitor.WirelessMonitorContainerMenu;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.UpgradeMenuData;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridContainerMenu;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridData;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridState;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridUpgradeContainerMenu;

import java.util.ArrayList;
import java.util.List;

import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createIdentifier;

public abstract class AbstractModInitializer {
    protected static boolean allowComponentsUpdateAnimation(final ItemStack oldStack, final ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    protected final void registerMenus(final RegistryCallback<MenuType<?>> callback,
                                       final ExtendedMenuTypeFactory extendedMenuTypeFactory) {
        Menus.INSTANCE.setWirelessCraftingGrid(callback.register(
            ContentIds.WIRELESS_CRAFTING_GRID,
            () -> extendedMenuTypeFactory.create(
                WirelessCraftingGridContainerMenu::new,
                WirelessCraftingGridData.STREAM_CODEC
            )
        ));
        Menus.INSTANCE.setChargingStation(callback.register(
            ContentIds.CHARGING_STATION,
            () -> extendedMenuTypeFactory.create(
                ChargingStationContainerMenu::new,
                ChargingStationData.STREAM_CODEC
            )
        ));
        Menus.INSTANCE.setWirelessMonitor(callback.register(
            ContentIds.WIRELESS_MONITOR,
            () -> extendedMenuTypeFactory.create(
                WirelessMonitorContainerMenu::new,
                MonitorMenuData.STREAM_CODEC
            )
        ));
        Menus.INSTANCE.setWirelessCraftingGridUpgrades(callback.register(
            ContentIds.WIRELESS_CRAFTING_GRID_UPGRADES,
            () -> extendedMenuTypeFactory.create(
                WirelessCraftingGridUpgradeContainerMenu::new,
                UpgradeMenuData.STREAM_CODEC
            )
        ));
    }

    protected final void registerDataComponents(final RegistryCallback<DataComponentType<?>> callback) {
        DataComponents.INSTANCE.setWirelessCraftingGridState(callback.register(
            createIdentifier("wireless_crafting_grid_state"),
            () -> DataComponentType.<WirelessCraftingGridState>builder()
                .persistent(WirelessCraftingGridState.CODEC)
                .networkSynchronized(WirelessCraftingGridState.STREAM_CODEC)
                .build()));
        DataComponents.INSTANCE.setPatternGridState(callback.register(
            ContentIds.WIRELESS_PATTERN_GRID,
            () -> DataComponentType.<CompoundTag>builder()
                .persistent(CompoundTag.CODEC)
                .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                .build()));
        DataComponents.INSTANCE.setMonitorRules(callback.register(
            createIdentifier("wireless_monitor_rules"),
            () -> DataComponentType.<List<MonitorRule>>builder()
                .persistent(MonitorRule.CODEC.listOf())
                .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, MonitorRule.STREAM_CODEC))
                .build()));
        DataComponents.INSTANCE.setUpgrades(callback.register(
            ContentIds.UPGRADES,
            () -> DataComponentType.<List<ItemStack>>builder()
                .persistent(ItemStack.OPTIONAL_CODEC.listOf())
                .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC))
                .build()));
    }
}
