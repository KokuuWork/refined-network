package com.kokuu.refinednetwork.common.upgrade;

import com.kokuu.refinednetwork.common.ContentNames;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * 高级能耗插件：完全消除无线终端「按距离」加价的能量倍率（跨维度倍率仍保留）。
 *
 * <p>它是<b>远程输电插件</b>的合成原料，是「初级能耗插件 → 高级能耗插件 → 远程输电插件」
 * 这条套娃链的中间环，前期投入不会浪费。
 */
public class AdvancedEnergyUpgradeItem extends Item {
    public AdvancedEnergyUpgradeItem() {
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
        lines.add(ContentNames.ADVANCED_ENERGY_UPGRADE_DESC);
        lines.add(ContentNames.ADVANCED_ENERGY_UPGRADE_REQUIRES);
    }
}
