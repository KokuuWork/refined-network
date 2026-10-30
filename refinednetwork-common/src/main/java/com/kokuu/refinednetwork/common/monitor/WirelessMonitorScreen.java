package com.kokuu.refinednetwork.common.monitor;

import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.ModIdentifierUtil;
import com.kokuu.refinednetwork.common.util.CompactNumber;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 无线监控终端界面：一列「监控目标」槽 + 每行的当前存量与规则。
 *
 * <p>交互全部走原版按钮通道（{@code MultiPlayerGameMode#handleInventoryButtonClick}），
 * 所以不需要本模组自己的数据包：
 * <ul>
 *   <li><b>左键</b>点阈值 = 加大一档；</li>
 *   <li><b>右键</b>点阈值 = 减小一档；</li>
 *   <li><b>Shift + 点击</b> = 在「低于提醒 / 高于提醒」之间切换。</li>
 * </ul>
 *
 * <p>存量数字在触发阈值时转红——界面一打开就知道哪条已经在告警了。
 */
public class WirelessMonitorScreen extends AbstractContainerScreen<WirelessMonitorContainerMenu> {
    private static final ResourceLocation TEXTURE =
        ModIdentifierUtil.createIdentifier("textures/gui/wireless_monitor.png");

    private static final int LABEL_COLOR = 0x404040;
    private static final int VALUE_COLOR = 0x2A2A2A;
    private static final int ALERT_COLOR = 0xC62828;
    private static final int RULE_COLOR = 0x3C5A78;
    private static final int RESTOCK_COLOR = 0x1D7A64;
    private static final int EMPTY_HINT_COLOR = 0x9A9A9A;

    public WirelessMonitorScreen(final WirelessMonitorContainerMenu menu,
                                 final Inventory inventory,
                                 final Component title) {
        super(menu, inventory, title);
        // 比标准 166 高一截：多出来的空间给规则行（标准高度塞不下 4 行 + 玩家背包）。
        imageWidth = 176;
        imageHeight = 182;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        // 1.21 起 AbstractContainerScreen.render 不再自动调用 renderTooltip，必须手动补一次。
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        // 用带纹理尺寸的重载按 1:1 铺满背景（7 参重载假定纹理 256x256，会只画出左上角）。
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        renderRowHighlight(graphics, mouseX, mouseY);
    }

    /**
     * 鼠标所在行整行压暗一点，方便对准目标行。
     */
    private void renderRowHighlight(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        final int row = rowAt(mouseX, mouseY);
        if (row < 0) {
            return;
        }
        final int y = topPos + WirelessMonitorContainerMenu.ROW_Y + row * WirelessMonitorContainerMenu.ROW_HEIGHT;
        graphics.fill(
            leftPos + 7,
            y - 1,
            leftPos + imageWidth - 7,
            y + 17,
            0x14FFFFFF
        );
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        for (int slot = 0; slot < WirelessMonitorContainerMenu.RULE_COUNT; slot++) {
            renderRule(graphics, slot);
        }
    }

    private void renderRule(final GuiGraphics graphics, final int slot) {
        final int y = WirelessMonitorContainerMenu.ROW_Y + slot * WirelessMonitorContainerMenu.ROW_HEIGHT + 5;
        if (!menu.hasRule(slot)) {
            if (slot == 0) {
                graphics.drawString(font, ContentNames.MONITOR_EMPTY_HINT, 32, y, EMPTY_HINT_COLOR, false);
            }
            return;
        }
        final long stored = menu.getStoredAmount(slot);
        final boolean triggered = menu.isAlertWhenBelow(slot)
            ? stored < menu.getThreshold(slot)
            : stored > menu.getThreshold(slot);
        final String amount = CompactNumber.format(stored);
        graphics.drawString(
            font,
            amount,
            WirelessMonitorContainerMenu.AMOUNT_RIGHT - font.width(amount),
            y,
            triggered ? ALERT_COLOR : VALUE_COLOR,
            false
        );
        graphics.drawString(
            font,
            monitorRuleText(slot),
            WirelessMonitorContainerMenu.THRESHOLD_X,
            y,
            triggered ? ALERT_COLOR : RULE_COLOR,
            false
        );
        drawMode(graphics, slot, y);
    }

    /**
     * 行动作区：提醒 / 补货。补货是「维持到目标」，所以用箭头而不是不等号。
     */
    private void drawMode(final GuiGraphics graphics, final int slot, final int y) {
        final Component label = menu.isRestock(slot)
            ? ContentNames.MONITOR_ACTION_RESTOCK
            : ContentNames.MONITOR_ACTION_ALERT;
        final int center = WirelessMonitorContainerMenu.MODE_X + WirelessMonitorContainerMenu.MODE_WIDTH / 2;
        graphics.drawString(
            font,
            label,
            center - font.width(label) / 2,
            y,
            menu.isRestock(slot) ? RESTOCK_COLOR : RULE_COLOR,
            false
        );
    }

    /**
     * 规则文字：提醒时是 {@code < 256} / {@code > 256}（符号即方向）；
     * 补货时是 {@code → 512}（箭头表示「补到这个数」）。
     */
    private String monitorRuleText(final int slot) {
        if (menu.isRestock(slot)) {
            return "→ " + CompactNumber.format(menu.getThreshold(slot));
        }
        return (menu.isAlertWhenBelow(slot) ? "< " : "> ")
            + CompactNumber.format(menu.getThreshold(slot));
    }

    /**
     * 鼠标所在的规则行；不在任何一行上时返回 -1。
     */
    private int rowAt(final double mouseX, final double mouseY) {
        final int relX = (int) mouseX - leftPos;
        final int relY = (int) mouseY - topPos;
        if (relX < 7 || relX > imageWidth - 7) {
            return -1;
        }
        for (int slot = 0; slot < WirelessMonitorContainerMenu.RULE_COUNT; slot++) {
            final int rowY = WirelessMonitorContainerMenu.ROW_Y + slot * WirelessMonitorContainerMenu.ROW_HEIGHT;
            if (relY >= rowY - 1 && relY < rowY + 17) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final int slot = rowAt(mouseX, mouseY);
        if (slot >= 0 && menu.hasRule(slot)) {
            final int relX = (int) mouseX - leftPos;
            if (inRange(relX, WirelessMonitorContainerMenu.THRESHOLD_X,
                WirelessMonitorContainerMenu.THRESHOLD_WIDTH)) {
                sendButton(slot, button);
                return true;
            }
            if (inRange(relX, WirelessMonitorContainerMenu.MODE_X,
                WirelessMonitorContainerMenu.MODE_WIDTH)) {
                sendAction(slot, WirelessMonitorContainerMenu.ACTION_TOGGLE_MODE);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * 把「哪一行 + 哪个动作」编码成一个原版菜单按钮 id 发给服务端。
     */
    private void sendButton(final int slot, final int mouseButton) {
        final int action;
        if (Screen.hasShiftDown()) {
            action = WirelessMonitorContainerMenu.ACTION_TOGGLE_DIRECTION;
        } else {
            action = mouseButton == 1
                ? WirelessMonitorContainerMenu.ACTION_THRESHOLD_DOWN
                : WirelessMonitorContainerMenu.ACTION_THRESHOLD_UP;
        }
        sendAction(slot, action);
    }

    private void sendAction(final int slot, final int action) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId,
                slot * WirelessMonitorContainerMenu.ACTION_COUNT + action
            );
        }
    }

    private static boolean inRange(final int value, final int start, final int width) {
        return value >= start && value < start + width;
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        final int slot = rowAt(mouseX, mouseY);
        if (slot < 0 || !menu.hasRule(slot)) {
            return;
        }
        final int relX = (int) mouseX - leftPos;
        if (inRange(relX, WirelessMonitorContainerMenu.MODE_X,
            WirelessMonitorContainerMenu.MODE_WIDTH)) {
            graphics.renderTooltip(
                font,
                Component.translatable(
                    ContentNames.MONITOR_MODE_TOOLTIP_TRANSLATION_KEY,
                    menu.isRestock(slot)
                        ? ContentNames.MONITOR_ACTION_RESTOCK
                        : ContentNames.MONITOR_ACTION_ALERT
                ),
                mouseX,
                mouseY
            );
            return;
        }
        if (!inRange(relX, WirelessMonitorContainerMenu.THRESHOLD_X,
            WirelessMonitorContainerMenu.THRESHOLD_WIDTH)) {
            return;
        }
        graphics.renderTooltip(
            font,
            Component.translatable(
                menu.isRestock(slot)
                    ? ContentNames.MONITOR_TARGET_TOOLTIP_TRANSLATION_KEY
                    : ContentNames.MONITOR_RULE_TOOLTIP_TRANSLATION_KEY,
                menu.isRestock(slot)
                    ? ContentNames.MONITOR_RULE_RESTOCK
                    : (menu.isAlertWhenBelow(slot)
                        ? ContentNames.MONITOR_RULE_BELOW
                        : ContentNames.MONITOR_RULE_ABOVE)
            ),
            mouseX,
            mouseY
        );
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 93;
    }
}
