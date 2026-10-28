package com.kokuu.rswirelesscrafting.common.smartsorter;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.ModIdentifierUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 智能分拣站的界面：3×3 过滤槽 + 一个匹配方式按钮 + 一行「当前命中几种」。
 *
 * <p>尺寸沿用本模组自写界面的 176×182（比原版 166 高，给过滤区和说明行留地方）。
 */
public class SmartSorterScreen extends AbstractContainerScreen<SmartSorterContainerMenu> {
    private static final ResourceLocation TEXTURE =
        ModIdentifierUtil.createIdentifier("textures/gui/smart_sorter.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 182;
    private static final int BUTTON_IDLE_COLOR = 0xFF4C4C4C;
    private static final int BUTTON_HOVER_COLOR = 0xFF6E6E6E;
    private static final int HINT_COLOR = 0xFFA0A0A0;

    public SmartSorterScreen(final SmartSorterContainerMenu menu,
                             final Inventory inventory,
                             final Component title) {
        super(menu, inventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0 && isModeButton(mouseX, mouseY)) {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    SmartSorterContainerMenu.ACTION_TOGGLE_MODE
                );
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isModeButton(final double mouseX, final double mouseY) {
        final int x = leftPos + SmartSorterContainerMenu.MODE_X;
        final int y = topPos + SmartSorterContainerMenu.MODE_Y;
        return mouseX >= x && mouseX < x + SmartSorterContainerMenu.MODE_WIDTH
            && mouseY >= y && mouseY < y + SmartSorterContainerMenu.MODE_HEIGHT;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        // 1.21 起 AbstractContainerScreen.render 不再自动调用 renderTooltip，必须手动补一次。
        super.render(graphics, mouseX, mouseY, partialTick);
        renderModeButton(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        // 必须用带纹理尺寸的重载：7 参 blit 假定 256x256，会把 176x182 的背景只画出左上角一小块。
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        renderMatchCount(graphics);
    }

    private void renderModeButton(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        final SmartSorterContainerMenu current = menu;
        final int x = leftPos + SmartSorterContainerMenu.MODE_X;
        final int y = topPos + SmartSorterContainerMenu.MODE_Y;
        final boolean hovered = mouseX >= x && mouseX < x + SmartSorterContainerMenu.MODE_WIDTH
            && mouseY >= y && mouseY < y + SmartSorterContainerMenu.MODE_HEIGHT;
        graphics.fill(
            x,
            y,
            x + SmartSorterContainerMenu.MODE_WIDTH,
            y + SmartSorterContainerMenu.MODE_HEIGHT,
            hovered ? BUTTON_HOVER_COLOR : BUTTON_IDLE_COLOR
        );
        graphics.drawString(
            font,
            current.getMode() == SorterMatchMode.EXACT
                ? ContentNames.SMART_SORTER_MODE_EXACT
                : ContentNames.SMART_SORTER_MODE_TAG,
            x + 4,
            y + 6,
            0xFFFFFFFF,
            false
        );
        if (hovered) {
            graphics.renderTooltip(
                font,
                Component.translatable(ContentNames.SMART_SORTER_MODE_TOOLTIP_TRANSLATION_KEY),
                mouseX,
                mouseY
            );
        }
    }

    private void renderMatchCount(final GuiGraphics graphics) {
        graphics.drawString(
            font,
            Component.translatable(ContentNames.SMART_SORTER_MATCHES_TRANSLATION_KEY, menu.getMatchCount()),
            SmartSorterContainerMenu.MODE_X,
            SmartSorterContainerMenu.MODE_Y + SmartSorterContainerMenu.MODE_HEIGHT + 6,
            HINT_COLOR,
            false
        );
    }
}
