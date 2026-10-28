package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.AbstractClientModInitializer;
import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Items;
import com.kokuu.rswirelesscrafting.common.KeyMappings;
import com.kokuu.rswirelesscrafting.common.Menus;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationScreen;
import com.kokuu.rswirelesscrafting.common.client.EnergyMonitorRenderer;
import com.kokuu.rswirelesscrafting.common.hud.WirelessTerminalHud;
import com.kokuu.rswirelesscrafting.common.monitor.WirelessMonitorScreen;
import com.kokuu.rswirelesscrafting.common.smartsorter.SmartSorterScreen;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridUpgradeScreen;

import com.mojang.blaze3d.platform.InputConstants;
import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkItemPropertyFunction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {
    /**
     * Fabric 目前没有 {@code BlockEntityRendererRegistry} 的替代 API（0.116.17 的
     * fabric-rendering-v1 里只有这一个类，标注了弃用但未给出新入口），
     * 所以这里显式抑制弃用警告；等官方给出替代方案再切换。
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onInitializeClient() {
        Platform.setClientPlayerProvider(() -> Minecraft.getInstance().player);
        registerKeyMappings();
        registerMenuScreens();
        registerItemProperties();
        BlockEntityRendererRegistry.register(
            BlockEntities.INSTANCE.getEnergyMonitor(),
            EnergyMonitorRenderer::new
        );
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> WirelessTerminalHud.render(graphics));
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
        MenuScreens.register(Menus.INSTANCE.getWirelessMonitor(), WirelessMonitorScreen::new);
        MenuScreens.register(Menus.INSTANCE.getSmartSorter(), SmartSorterScreen::new);
    }

    private static void registerItemProperties() {
        final NetworkItemPropertyFunction boundActive = new NetworkItemPropertyFunction();
        ItemProperties.register(
            Items.INSTANCE.getWirelessCraftingGrid(),
            NetworkItemPropertyFunction.NAME,
            boundActive
        );
        ItemProperties.register(
            Items.INSTANCE.getWirelessPatternGrid(),
            NetworkItemPropertyFunction.NAME,
            boundActive
        );
        ItemProperties.register(
            Items.INSTANCE.getWirelessAutocrafterManager(),
            NetworkItemPropertyFunction.NAME,
            boundActive
        );
        ItemProperties.register(
            Items.INSTANCE.getWirelessMonitor(),
            NetworkItemPropertyFunction.NAME,
            boundActive
        );
    }
}
