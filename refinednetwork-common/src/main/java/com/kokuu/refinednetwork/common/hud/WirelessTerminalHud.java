package com.kokuu.refinednetwork.common.hud;

import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.refinednetwork.common.util.SaturatedMath;
import com.kokuu.refinednetwork.common.wirelesscraftinggrid.ExtendedWirelessNetworkItemContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 手持无线终端时的常驻小字提示：**电量 + 距离 + 收费倍率**。
 *
 * <p>设计上守着「不啰嗦」的底线：只在**手里拿着**已绑定的终端时出现，一行字、不占地方、自动居中在物品栏上方；
 * 打开任意界面（背包、箱子……）时自动让位。想彻底关掉就在配置里把 {@code hud.enabled} 改成 false。
 *
 * <p>文案复用终端悬停提示里那几条翻译键，但**不带 DARK_GRAY 样式**——那一套在 GUI 里好看，
 * 压在世界画面上读不清，所以这里用白字 + 阴影，电量低于 25% 时转红提醒。
 */
public final class WirelessTerminalHud {
    /**
     * 字基线相对屏幕底部的高度：物品栏 + 经验条上方一点点。
     */
    private static final int DISTANCE_FROM_BOTTOM = 59;
    private static final int COLOR_NORMAL = 0xFFFFFFFF;
    private static final int COLOR_LOW = 0xFFFF5555;
    private static final int LOW_ENERGY_NUMERATOR = 1;
    private static final int LOW_ENERGY_DENOMINATOR = 4;

    private WirelessTerminalHud() {
    }

    /**
     * 由各加载器的 HUD 钩子调用（NeoForge GUI 层 / Fabric HudRenderCallback）。
     */
    public static void render(final GuiGraphics graphics) {
        final Minecraft minecraft = Minecraft.getInstance();
        final Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        if (!Platform.getConfig().getHud().isEnabled()) {
            return;
        }
        final ItemStack terminal = heldTerminal(player);
        if (terminal.isEmpty() || !AbstractWirelessTerminalItem.isBoundToNetwork(terminal)) {
            return;
        }
        final Component line = buildLine(terminal, player);
        final int x = (graphics.guiWidth() - minecraft.font.width(line)) / 2;
        final int y = graphics.guiHeight() - DISTANCE_FROM_BOTTOM;
        graphics.drawString(
            minecraft.font,
            line,
            x,
            y,
            isLow(terminal) ? COLOR_LOW : COLOR_NORMAL,
            true
        );
    }

    private static Component buildLine(final ItemStack terminal, final Player player) {
        final Component energy = Component.translatable(
            ContentNames.HUD_ENERGY_TRANSLATION_KEY,
            AbstractWirelessTerminalItem.getStoredEnergy(terminal),
            AbstractWirelessTerminalItem.getEnergyCapacity(terminal)
        );
        final long multiplier = ExtendedWirelessNetworkItemContext.distanceMultiplierPercent(terminal, player);
        final GlobalPos location = AbstractWirelessTerminalItem.getBoundLocation(terminal);
        final Component distance;
        if (location != null && !location.dimension().equals(player.level().dimension())) {
            distance = Component.translatable(
                ContentNames.WIRELESS_CRAFTING_GRID_DISTANCE_INTERDIMENSIONAL_TRANSLATION_KEY,
                AbstractWirelessTerminalItem.formatMultiplier(multiplier)
            );
        } else {
            distance = Component.translatable(
                ContentNames.WIRELESS_CRAFTING_GRID_DISTANCE_TRANSLATION_KEY,
                Math.round(ExtendedWirelessNetworkItemContext.distanceToBound(terminal, player)),
                AbstractWirelessTerminalItem.formatMultiplier(multiplier)
            );
        }
        return energy.copy().append(Component.literal("  ·  ")).append(distance);
    }

    private static boolean isLow(final ItemStack terminal) {
        final long capacity = AbstractWirelessTerminalItem.getEnergyCapacity(terminal);
        return SaturatedMath.multiply(
            AbstractWirelessTerminalItem.getStoredEnergy(terminal),
            LOW_ENERGY_DENOMINATOR
        ) < capacity * LOW_ENERGY_NUMERATOR;
    }

    private static ItemStack heldTerminal(final Player player) {
        final ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AbstractWirelessTerminalItem) {
            return mainHand;
        }
        final ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof AbstractWirelessTerminalItem) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}
