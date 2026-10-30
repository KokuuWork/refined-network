package com.kokuu.refinednetwork.common.upgrade;

import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.Platform;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * Unlocks cross-dimensional access, by letting the grid bind a Network Receiver and
 * travel through the Network Transmitter/Receiver link.
 */
public class InterdimensionalUpgradeItem extends Item {
    public InterdimensionalUpgradeItem() {
        super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(ContentNames.UPGRADE_INSTALL_HINT);
        lines.add(Component.empty());
        lines.add(ContentNames.INTERDIMENSIONAL_UPGRADE_DESC);
        lines.add(ContentNames.INTERDIMENSIONAL_UPGRADE_REQUIRES);
        lines.add(Component.translatable(
            ContentNames.INTERDIMENSIONAL_UPGRADE_ENERGY_TRANSLATION_KEY,
            Platform.getConfig().getDistanceEnergy().getInterdimensionalMultiplier()
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
