package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.ModIdentifierUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WirelessCraftingGridUpgradeScreen
    extends AbstractContainerScreen<WirelessCraftingGridUpgradeContainerMenu> {
    private static final ResourceLocation TEXTURE =
        ModIdentifierUtil.createIdentifier("textures/gui/wireless_crafting_grid_upgrades.png");

    public WirelessCraftingGridUpgradeScreen(final WirelessCraftingGridUpgradeContainerMenu menu,
                                             final Inventory inventory,
                                             final Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        // 1.21 起 AbstractContainerScreen.render 不再自动调用 renderTooltip，
        // 必须像原版 ContainerScreen / RS AbstractBaseScreen 那样手动补一次，否则槽位 tooltip 不显示。
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        // 7 参 blit 假定纹理是 256x256，必须用带纹理尺寸的重载按 1:1 铺满。
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }
}
