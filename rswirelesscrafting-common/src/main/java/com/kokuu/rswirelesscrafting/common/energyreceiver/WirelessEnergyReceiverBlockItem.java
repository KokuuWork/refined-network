package com.kokuu.rswirelesscrafting.common.energyreceiver;

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
 * 无线能量接收器的方块物品：把「它反着输电」「缓冲多大」「链路费怎么算」摊开写清楚，
 * 玩家不用翻配置也知道这块东西是干什么的。
 */
public class WirelessEnergyReceiverBlockItem extends BlockItem {
    public WirelessEnergyReceiverBlockItem(final WirelessEnergyReceiverBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final Config.EnergyReceiverEntry entry = Platform.getConfig().getEnergyReceiver();
        lines.add(ContentNames.WIRELESS_ENERGY_RECEIVER_DESC);
        lines.add(Component.empty());
        lines.add(ContentNames.WIRELESS_ENERGY_RECEIVER_HINT_BIND);
        lines.add(Component.translatable(
            ContentNames.WIRELESS_ENERGY_RECEIVER_CAPACITY_TRANSLATION_KEY,
            entry.getCapacity()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(
            ContentNames.WIRELESS_ENERGY_RECEIVER_ENERGY_TRANSLATION_KEY,
            entry.getEnergyUsage()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(ContentNames.WIRELESS_ENERGY_RECEIVER_HINT_REDSTONE);
    }
}
