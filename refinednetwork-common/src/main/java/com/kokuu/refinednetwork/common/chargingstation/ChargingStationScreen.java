package com.kokuu.refinednetwork.common.chargingstation;

import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.ModIdentifierUtil;
import com.kokuu.refinednetwork.common.util.CompactNumber;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 终端充电站界面：一张 3×2 连接面板（上排 3 个有线槽、下排 3 个无线链接灯）+ 一条缓冲电量条。
 *
 * <p>灯的状态由服务端经 {@code ContainerData} 同步（见 {@link ChargingStationBlockEntity}）：
 * 有线槽「充电中」蓝灯呼吸闪烁、充满绿灯；无线链接「正在充电」时呼吸闪烁的亮灯。
 * 呼吸用 {@code level.getGameTime()} 驱动，纯客户端视觉，不占用额外网络包。
 */
public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationContainerMenu> {
    private static final ResourceLocation TEXTURE =
        ModIdentifierUtil.createIdentifier("textures/gui/terminal_charging_station.png");

    // 缓冲电量条的几何必须与 gen_gui_textures.py 里的 panel() 调用逐像素一致。
    private static final Bar BUFFER_BAR = new Bar(26, 61, 124, 10);

    private static final int CHARGING_COLOR = 0xFF40A8FF;
    private static final int FULL_COLOR = 0xFF4BE070;
    private static final int WIRELESS_COLOR = 0xFF34E0C8;
    private static final int BUFFER_FILL_COLOR = 0xFF40A8FF;
    private static final int LABEL_COLOR = 0x404040;

    public ChargingStationScreen(final ChargingStationContainerMenu menu,
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
        // 注意：7 参 blit 假定纹理是 256x256，会把 176x166 的背景只画出左上角一小块；
        // 必须用带纹理尺寸的重载，按 1:1 铺满整张背景。
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        renderBufferBar(graphics);
        renderConnectionPanel(graphics, partialTick);
    }

    private void renderBufferBar(final GuiGraphics graphics) {
        final int value = menu.getDataValue(0);
        final int max = Math.max(1, menu.getDataValue(1));
        final int filled = (int) Math.min(BUFFER_BAR.width - 2L, (long) value * (BUFFER_BAR.width - 2) / max);
        if (filled > 0) {
            graphics.fill(
                leftPos + BUFFER_BAR.x + 1,
                topPos + BUFFER_BAR.y + 1,
                leftPos + BUFFER_BAR.x + 1 + filled,
                topPos + BUFFER_BAR.y + BUFFER_BAR.height - 1,
                BUFFER_FILL_COLOR
            );
        }
    }

    private void renderConnectionPanel(final GuiGraphics graphics, final float partialTick) {
        final double phase = breathePhase(partialTick);
        for (int i = 0; i < ChargingStationContainerMenu.WIRED_SLOT_X.length; i++) {
            renderWiredLight(graphics, i, phase);
            renderWirelessLight(graphics, i, phase);
        }
    }

    private void renderWiredLight(final GuiGraphics graphics, final int slot, final double phase) {
        final ChargeStatus status = ChargeStatus.byOrdinal(menu.getDataValue(4 + slot));
        final int color = switch (status) {
            case CHARGING -> pulse(CHARGING_COLOR, phase);
            case FULL -> FULL_COLOR;
            case IDLE -> 0;
            default -> 0;
        };
        if (color == 0) {
            return;
        }
        drawFrame(
            graphics,
            ChargingStationContainerMenu.WIRED_SLOT_X[slot],
            ChargingStationContainerMenu.WIRED_SLOT_Y,
            color
        );
    }

    private void renderWirelessLight(final GuiGraphics graphics, final int link, final double phase) {
        final boolean active = menu.getDataValue(7 + link) != 0;
        if (!active) {
            return;
        }
        drawDot(
            graphics,
            ChargingStationContainerMenu.WIRELESS_LIGHT_X[link],
            ChargingStationContainerMenu.WIRELESS_LIGHT_Y,
            pulse(WIRELESS_COLOR, phase)
        );
    }

    /**
     * 呼吸相位：2 秒一个周期，供蓝灯与无线灯同步呼吸。
     */
    private double breathePhase(final float partialTick) {
        final long gameTime = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        return ((gameTime % 40L) + partialTick) / 40.0;
    }

    /**
     * 把颜色按正弦波压暗再提亮，实现「呼吸」闪烁（0.35x ~ 1.0x）。
     */
    private static int pulse(final int color, final double phase) {
        final double wave = 0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0);
        final double factor = 0.35 + 0.65 * wave;
        final int alpha = color & 0xFF000000;
        final int red = (int) (((color >> 16) & 0xFF) * factor);
        final int green = (int) (((color >> 8) & 0xFF) * factor);
        final int blue = (int) ((color & 0xFF) * factor);
        final int rgb = (red << 16) | (green << 8) | blue;
        return alpha | rgb;
    }

    /**
     * 在某个 18×18 凹陷框（左上角 = 菜单坐标 - 1）外沿画一圈 1px 状态框。
     */
    private void drawFrame(final GuiGraphics graphics, final int slotX, final int slotY, final int color) {
        final int x = leftPos + slotX - 1;
        final int y = topPos + slotY - 1;
        graphics.fill(x, y, x + 18, y + 1, color);
        graphics.fill(x, y + 17, x + 18, y + 18, color);
        graphics.fill(x, y, x + 1, y + 18, color);
        graphics.fill(x + 17, y, x + 18, y + 18, color);
    }

    /**
     * 在某个 18×18 凹陷框中心画一个 8×8 的实心灯。
     */
    private void drawDot(final GuiGraphics graphics, final int slotX, final int slotY, final int color) {
        final int x = leftPos + slotX - 1;
        final int y = topPos + slotY - 1;
        graphics.fill(x + 5, y + 5, x + 13, y + 13, color);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(
            font,
            ContentNames.CHARGING_STATION_WIRED_LABEL,
            8,
            ChargingStationContainerMenu.WIRED_SLOT_Y + 5,
            LABEL_COLOR,
            false
        );
        graphics.drawString(
            font,
            ContentNames.CHARGING_STATION_WIRELESS_LABEL,
            8,
            ChargingStationContainerMenu.WIRELESS_LIGHT_Y + 5,
            LABEL_COLOR,
            false
        );
        drawBufferValue(graphics);
    }

    private void drawBufferValue(final GuiGraphics graphics) {
        final Component text = Component.literal(
            CompactNumber.format(menu.getDataValue(0)) + " / "
                + CompactNumber.format(menu.getDataValue(1)) + " FE"
        );
        graphics.drawString(
            font,
            text,
            BUFFER_BAR.x + BUFFER_BAR.width / 2 - font.width(text) / 2,
            BUFFER_BAR.y + (BUFFER_BAR.height - 8) / 2,
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
            BUFFER_BAR.x() - 4,
            BUFFER_BAR.y() - 2,
            BUFFER_BAR.width() + 8,
            BUFFER_BAR.height() + 4,
            mouseX,
            mouseY
        )) {
            graphics.renderTooltip(font, ContentNames.CHARGING_STATION_BUFFER_INFO, mouseX, mouseY);
        }
    }

    private record Bar(int x, int y, int width, int height) {
    }
}
