package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.AbstractClientModInitializer;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Items;
import com.kokuu.rswirelesscrafting.common.KeyMappings;
import com.kokuu.rswirelesscrafting.common.Menus;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationScreen;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridUpgradeScreen;

import com.mojang.blaze3d.platform.InputConstants;
import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkItemPropertyFunction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerKeyMappings();
        registerMenuScreens();
        registerItemProperties();
    }

    private void registerKeyMappings() {
        KeyMappings.INSTANCE.setOpenWirelessCraftingGrid(KeyBindingHelper.registerKeyBinding(new KeyMapping(
            ContentNames.OPEN_WIRELESS_CRAFTING_GRID_TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            ContentNames.MOD_TRANSLATION_KEY
        )));
        ClientTickEvents.END_CLIENT_TICK.register(client -> handleInputEvents());
    }

    private static void registerMenuScreens() {
        MenuScreens.register(Menus.INSTANCE.getWirelessCraftingGrid(), CraftingGridScreen::new);
        MenuScreens.register(
            Menus.INSTANCE.getWirelessCraftingGridUpgrades(),
            WirelessCraftingGridUpgradeScreen::new
        );
        MenuScreens.register(Menus.INSTANCE.getChargingStation(), ChargingStationScreen::new);
    }

    private static void registerItemProperties() {
        ItemProperties.register(
            Items.INSTANCE.getWirelessCraftingGrid(),
            NetworkItemPropertyFunction.NAME,
            new NetworkItemPropertyFunction()
        );
    }
}
