package com.kokuu.refinednetwork.neoforge;

import com.kokuu.refinednetwork.common.AbstractClientModInitializer;
import com.kokuu.refinednetwork.common.BlockEntities;
import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.Items;
import com.kokuu.refinednetwork.common.KeyMappings;
import com.kokuu.refinednetwork.common.Menus;
import com.kokuu.refinednetwork.common.ModIdentifierUtil;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.chargingstation.ChargingStationScreen;
import com.kokuu.refinednetwork.common.client.EnergyMonitorRenderer;
import com.kokuu.refinednetwork.common.hud.WirelessTerminalHud;
import com.kokuu.refinednetwork.common.monitor.WirelessMonitorScreen;
import com.kokuu.refinednetwork.common.smartsorter.SmartSorterScreen;
import com.kokuu.refinednetwork.common.wirelesscraftinggrid.WirelessCraftingGridUpgradeScreen;

import com.mojang.blaze3d.platform.InputConstants;
import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkItemPropertyFunction;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

public class ClientModInitializer extends AbstractClientModInitializer {
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        NeoForge.EVENT_BUS.addListener(ClientModInitializer::onKeyInput);
        NeoForge.EVENT_BUS.addListener(ClientModInitializer::onMouseInput);
        Platform.setClientPlayerProvider(() -> Minecraft.getInstance().player);
        e.enqueueWork(ClientModInitializer::registerItemProperties);
    }

    @SubscribeEvent
    public static void onKeyInput(final InputEvent.Key e) {
        handleInputEvents();
    }

    @SubscribeEvent
    public static void onMouseInput(final InputEvent.MouseButton.Pre e) {
        handleInputEvents();
    }

    @SubscribeEvent
    public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers e) {
        e.registerBlockEntityRenderer(BlockEntities.INSTANCE.getEnergyMonitor(), EnergyMonitorRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(final RegisterMenuScreensEvent e) {
        e.register(Menus.INSTANCE.getWirelessCraftingGrid(), CraftingGridScreen::new);
        e.register(
            Menus.INSTANCE.getWirelessCraftingGridUpgrades(),
            WirelessCraftingGridUpgradeScreen::new
        );
        e.register(Menus.INSTANCE.getChargingStation(), ChargingStationScreen::new);
        e.register(Menus.INSTANCE.getWirelessMonitor(), WirelessMonitorScreen::new);
        e.register(Menus.INSTANCE.getSmartSorter(), SmartSorterScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent e) {
        final KeyMapping openWirelessCraftingGrid = new KeyMapping(
            ContentNames.OPEN_WIRELESS_CRAFTING_GRID_TRANSLATION_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(openWirelessCraftingGrid);
        KeyMappings.INSTANCE.setOpenWirelessCraftingGrid(openWirelessCraftingGrid);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(final RegisterGuiLayersEvent e) {
        e.registerAbove(
            VanillaGuiLayers.HOTBAR,
            ModIdentifierUtil.createIdentifier("wireless_terminal_hud"),
            (graphics, deltaTracker) -> WirelessTerminalHud.render(graphics)
        );
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
