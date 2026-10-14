package com.kokuu.rswirelesscrafting.common.chargingstation;

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
 * 充电站的方块物品。两种规格的差别全在数值上，所以 tooltip 直接把这一档当前生效的
 * 有线速率、无线速率与半径摊开写（读的是配置，玩家改过配置也会与实物一致）。
 */
public class ChargingStationBlockItem extends BlockItem {
    private final ChargingStationTier tier;

    public ChargingStationBlockItem(final ChargingStationBlock block, final Properties properties) {
        super(block, properties);
        this.tier = block.getTier();
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(ContentNames.CHARGING_STATION_HELP);
        lines.add(Component.empty());
        final Config.ChargingStationEntry entry = tier.getEntry(Platform.getConfig());
        lines.add(Component.translatable(
            ContentNames.CHARGING_STATION_WIRED_TRANSLATION_KEY,
            entry.getWiredChargeRate()
        ).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable(
            ContentNames.CHARGING_STATION_WIRELESS_TRANSLATION_KEY,
            entry.getWirelessChargeRate(),
            entry.getWirelessRadius()
        ).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable(
            ContentNames.CHARGING_STATION_IDLE_TRANSLATION_KEY,
            entry.getEnergyUsage()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(
            ContentNames.CHARGING_STATION_BUFFER_TRANSLATION_KEY,
            entry.getExternalCapacity()
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
