package com.kokuu.rswirelesscrafting.common.networkbridge;

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
 * 网络桥接器的方块物品：把「和无线桥的区别」「互通速率上限」「链路费怎么算」写清楚。
 */
public class NetworkBridgeBlockItem extends BlockItem {
    public NetworkBridgeBlockItem(final NetworkBridgeBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final Config.NetworkBridgeEntry entry = Platform.getConfig().getNetworkBridge();
        lines.add(ContentNames.NETWORK_BRIDGE_DESC);
        lines.add(Component.empty());
        lines.add(ContentNames.NETWORK_BRIDGE_HINT_BIND);
        lines.add(Component.translatable(
            ContentNames.NETWORK_BRIDGE_ITEM_RATE_TRANSLATION_KEY,
            entry.getItemTransferRate()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(
            ContentNames.NETWORK_BRIDGE_ENERGY_RATE_TRANSLATION_KEY,
            entry.getEnergyTransferRate()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(
            ContentNames.NETWORK_BRIDGE_FEE_TRANSLATION_KEY,
            entry.getEnergyUsage()
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
