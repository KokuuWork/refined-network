package com.kokuu.rswirelesscrafting.common.upgrade;

import com.kokuu.rswirelesscrafting.common.ContentNames;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * Removes the distance limit of a wireless crafting grid bound to a Wireless Transmitter,
 * within the same dimension.
 */
public class RangeUpgradeItem extends Item {
    public RangeUpgradeItem() {
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
        lines.add(ContentNames.RANGE_UPGRADE_DESC);
        lines.add(ContentNames.RANGE_UPGRADE_REQUIRES);
    }
}
