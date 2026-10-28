package com.kokuu.rswirelesscrafting.common.energymonitor;

import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 能量监控仪的方块物品：把「看什么」「怎么用」「阈值多少」写清楚。
 */
public class EnergyMonitorBlockItem extends BlockItem {
    public EnergyMonitorBlockItem(final EnergyMonitorBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final Config.EnergyMonitorEntry entry = Platform.getConfig().getEnergyMonitor();
        lines.add(ContentNames.WIRELESS_ENERGY_MONITOR_DESC);
        lines.add(Component.empty());
        lines.add(ContentNames.WIRELESS_ENERGY_MONITOR_HINT_PLACE);
        lines.add(ContentNames.WIRELESS_ENERGY_MONITOR_HINT_BIND);
        lines.add(Component.translatable(
            ContentNames.WIRELESS_ENERGY_MONITOR_THRESHOLD_TRANSLATION_KEY,
            entry.getLowThresholdPercent()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(
            ContentNames.WIRELESS_ENERGY_MONITOR_ENERGY_TRANSLATION_KEY,
            entry.getEnergyUsage()
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
