package com.kokuu.refinednetwork.common.upgrade;

import com.kokuu.refinednetwork.common.ContentNames;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * Draws the grid's energy straight from the bound network instead of the item's own battery.
 *
 * <p>Without this upgrade a wireless crafting grid slowly drains its internal buffer and has to be
 * recharged externally. With it installed the grid never needs charging: every action is paid for by
 * the bound Refined Storage network, and running out of network energy simply makes the grid inactive.
 */
public class RemoteEnergyUpgradeItem extends Item {
    public RemoteEnergyUpgradeItem() {
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
        lines.add(ContentNames.REMOTE_ENERGY_UPGRADE_DESC);
        lines.add(ContentNames.REMOTE_ENERGY_UPGRADE_REQUIRES);
    }
}
