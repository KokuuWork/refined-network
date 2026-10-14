package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridContainerMenu;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridData;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridState;

import java.util.ArrayList;
import java.util.List;

import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import net.minecraft.core.component.DataComponentType;
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
    }

    protected final void registerDataComponents(final RegistryCallback<DataComponentType<?>> callback) {
        DataComponents.INSTANCE.setWirelessCraftingGridState(callback.register(
            createIdentifier("wireless_crafting_grid_state"),
            () -> DataComponentType.<WirelessCraftingGridState>builder()
                .persistent(WirelessCraftingGridState.CODEC)
                .networkSynchronized(WirelessCraftingGridState.STREAM_CODEC)
                .build()));
        DataComponents.INSTANCE.setUpgrades(callback.register(
            ContentIds.UPGRADES,
            () -> DataComponentType.<List<ItemStack>>builder()
                .persistent(ItemStack.OPTIONAL_CODEC.listOf())
                .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC))
                .build()));
    }
}
