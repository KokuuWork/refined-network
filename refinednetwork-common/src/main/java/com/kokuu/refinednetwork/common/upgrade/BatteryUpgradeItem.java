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
 * 过渡用升级卡：提升无线合成终端的电池容量。
 *
 * <p>无线合成终端默认只有 400 FE，合成/自动合成等动作会持续消耗它，前期难免频繁跑回充电站。
 * 这张卡用中期就能拿到的材料换来更耐用的电池，同时也是<b>远距离输电卡</b>的合成原料，
 * 因此升级路径是「电池卡 → 输电卡」，前期投入不会浪费。
 */
public class BatteryUpgradeItem extends Item {
    public BatteryUpgradeItem() {
        super(new Item.Properties().stacksTo(64).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(ContentNames.UPGRADE_INSTALL_HINT);
        lines.add(Component.empty());
        lines.add(ContentNames.BATTERY_UPGRADE_DESC);
        lines.add(Component.translatable(
            ContentNames.BATTERY_UPGRADE_CAPACITY_TRANSLATION_KEY,
            Platform.getConfig().getWirelessCraftingGrid().getBatteryUpgradeCapacity()
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
