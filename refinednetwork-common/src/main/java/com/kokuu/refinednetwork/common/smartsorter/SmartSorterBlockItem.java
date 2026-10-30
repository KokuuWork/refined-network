package com.kokuu.refinednetwork.common.smartsorter;

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
 * 智能分拣站的方块物品：把「比原版输出总线多会什么」「搬多快」「多贵」写清楚。
 */
public class SmartSorterBlockItem extends BlockItem {
    public SmartSorterBlockItem(final SmartSorterBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final Config.SmartSorterEntry entry = Platform.getConfig().getSmartSorter();
        lines.add(ContentNames.SMART_SORTER_DESC);
        lines.add(Component.empty());
        lines.add(ContentNames.SMART_SORTER_HINT_PLACE);
        lines.add(ContentNames.SMART_SORTER_HINT_FILTER);
        lines.add(Component.translatable(
            ContentNames.SMART_SORTER_RATE_TRANSLATION_KEY,
            entry.getStacksPerSecond()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(
            ContentNames.SMART_SORTER_ENERGY_TRANSLATION_KEY,
            entry.getEnergyPerStack()
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
