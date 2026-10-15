package com.kokuu.rswirelesscrafting.common.bridge;

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
 * 无线桥的方块物品：把「它有两种身份」「耗多少电」「会自动保区块」摊开写清楚，
 * 玩家不用翻配置也知道这块东西是干什么的。
 */
public class WirelessBridgeBlockItem extends BlockItem {
    public WirelessBridgeBlockItem(final WirelessBridgeBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final Config.BridgeEntry entry = Platform.getConfig().getBridge();
        lines.add(ContentNames.WIRELESS_BRIDGE_DESC);
        lines.add(Component.empty());
        lines.add(ContentNames.WIRELESS_BRIDGE_ANCHOR_HINT);
        lines.add(ContentNames.WIRELESS_BRIDGE_ENDPOINT_HINT);
        lines.add(Component.translatable(
            ContentNames.BRIDGE_ENERGY_TRANSLATION_KEY,
            entry.getEnergyUsage()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(
            ContentNames.BRIDGE_CHUNK_ENERGY_TRANSLATION_KEY,
            entry.getChunkLoadingEnergyUsage()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(ContentNames.WIRELESS_BRIDGE_CHUNK_HINT);
        lines.add(ContentNames.WIRELESS_BRIDGE_REDSTONE_HINT);
    }
}
