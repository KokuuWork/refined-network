package com.kokuu.rswirelesscrafting.common.chargingstation;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.ModIdentifierUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationContainerMenu> {
    private static final ResourceLocation TEXTURE =
        ModIdentifierUtil.createIdentifier("textures/gui/charging_station.png");

    // 两条能量条的几何必须与 gen_gui_textures.py 里的 panel() 调用逐像素一致，
    // 高度统一为 10，才能让条内的 "x / y FE" 文字（9px 高）完整落在条内。
    private static final Bar NETWORK_BAR = new Bar(26, 18, 124, 10, 0xFF1E88C7);
    private static final Bar BUFFER_BAR = new Bar(26, 32, 124, 10, 0xFFD98A00);

    private static final int LABEL_COLOR = 0x404040;

    public ChargingStationScreen(final ChargingStationContainerMenu menu,
                                 final Inventory inventory,
                                 final Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderBar(graphics, 0, 1, NETWORK_BAR);
        renderBar(graphics, 2, 3, BUFFER_BAR);
    }

    private void renderBar(final GuiGraphics graphics,
                           final int valueIndex,
                           final int maxIndex,
                           final Bar bar) {
        final int value = menu.getDataValue(valueIndex);
        final int max = Math.max(1, menu.getDataValue(maxIndex));
        final int filled = (int) Math.min(bar.width - 2L, (long) value * (bar.width - 2) / max);
        graphics.fill(
            leftPos + bar.x,
            topPos + bar.y,
            leftPos + bar.x + bar.width,
            topPos + bar.y + bar.height,
            0xFF555555
        );
        if (filled > 0) {
            graphics.fill(
                leftPos + bar.x + 1,
                topPos + bar.y + 1,
                leftPos + bar.x + 1 + filled,
                topPos + bar.y + bar.height - 1,
                bar.color
            );
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawValue(graphics, 0, 1, NETWORK_BAR);
        drawValue(graphics, 2, 3, BUFFER_BAR);
    }

    private void drawValue(final GuiGraphics graphics, final int valueIndex, final int maxIndex, final Bar bar) {
        final Component text = Component.literal(
            menu.getDataValue(valueIndex) + " / " + menu.getDataValue(maxIndex) + " FE"
        );
        graphics.drawString(
            font,
            text,
            bar.x + bar.width / 2 - font.width(text) / 2,
            bar.y + (bar.height - 8) / 2,
            LABEL_COLOR,
            false
        );
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(
            NETWORK_BAR.x() - 4,
            NETWORK_BAR.y() - 2,
            NETWORK_BAR.width() + 8,
            NETWORK_BAR.height() + 4,
            mouseX,
            mouseY
        )) {
            graphics.renderTooltip(font, ContentNames.CHARGING_STATION_HELP, mouseX, mouseY);
        } else if (isHovering(
            BUFFER_BAR.x() - 4,
            BUFFER_BAR.y() - 2,
            BUFFER_BAR.width() + 8,
            BUFFER_BAR.height() + 4,
            mouseX,
            mouseY
        )) {
            graphics.renderTooltip(font, ContentNames.CHARGING_STATION_WIRELESS_INFO, mouseX, mouseY);
        }
    }

    private record Bar(int x, int y, int width, int height, int color) {
    }
}
