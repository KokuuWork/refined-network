package com.kokuu.refinednetwork.common.chargingstation;

import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.Platform;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 终端充电站的方块物品。tooltip 直接把当前生效的有线 / 无线速率、半径与待机开销摊开写
 * （读的是配置，玩家改过配置也会与实物一致）。
 */
public class ChargingStationBlockItem extends BlockItem {
    public ChargingStationBlockItem(final ChargingStationBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(ContentNames.CHARGING_STATION_HELP);
        lines.add(Component.empty());
        final Config.ChargingStationEntry entry = Platform.getConfig().getChargingStation();
        lines.add(Component.translatable(
            ContentNames.CHARGING_STATION_WIRED_TRANSLATION_KEY,
            entry.getWiredChargeRate()
        ).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable(
            ContentNames.CHARGING_STATION_WIRELESS_TRANSLATION_KEY,
            entry.getWirelessChargeRate(),
            entry.getWirelessRadius(),
            entry.getWirelessChargeEfficiencyPercent()
        ).withStyle(ChatFormatting.AQUA));
        lines.add(ContentNames.CHARGING_STATION_WIRELESS_CAPACITY_INFO);
        if (entry.isIdleEnergyUsageEnabled()) {
            lines.add(Component.translatable(
                ContentNames.CHARGING_STATION_IDLE_TRANSLATION_KEY,
                entry.getEnergyUsage()
            ).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.add(ContentNames.CHARGING_STATION_IDLE_DISABLED);
        }
        lines.add(ContentNames.CHARGING_STATION_REDSTONE);
    }
}
