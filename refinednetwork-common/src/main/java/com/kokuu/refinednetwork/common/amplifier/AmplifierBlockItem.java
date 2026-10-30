package com.kokuu.refinednetwork.common.amplifier;

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
 * 增幅塔的方块物品：把当前生效的覆盖半径、折扣与耗电摊开写清楚，
 * 玩家不用去翻配置文件就知道自己买的这座塔有多大用。
 */
public class AmplifierBlockItem extends BlockItem {
    public AmplifierBlockItem(final AmplifierBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final Config.AmplifierEntry entry = Platform.getConfig().getAmplifier();
        lines.add(ContentNames.WIRELESS_AMPLIFIER_DESC);
        lines.add(Component.empty());
        lines.add(Component.translatable(
            ContentNames.AMPLIFIER_RADIUS_TRANSLATION_KEY,
            entry.getRadius()
        ).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable(
            ContentNames.AMPLIFIER_MULTIPLIER_TRANSLATION_KEY,
            entry.getMultiplierPercent()
        ).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable(
            ContentNames.AMPLIFIER_ENERGY_TRANSLATION_KEY,
            entry.getEnergyUsage()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(ContentNames.AMPLIFIER_REDSTONE_HINT);
    }
}
