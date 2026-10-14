package com.kokuu.rswirelesscrafting.common.upgrade;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * 初级能耗插件：把无线终端「按距离」加价的能量倍率减半。
 *
 * <p>无线设备离绑定网络越远耗电越狠，这张卡是前中期的续航手段——不是消除距离惩罚，
 * 而是砍一半。它是<b>高级能耗插件</b>的合成原料，升级到高级后才彻底抹掉距离惩罚。
 */
public class EnergyUpgradeItem extends Item {
    public EnergyUpgradeItem() {
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
        lines.add(ContentNames.ENERGY_UPGRADE_DESC);
        lines.add(Component.translatable(
            ContentNames.ENERGY_UPGRADE_DIVISOR_TRANSLATION_KEY,
            Platform.getConfig().getDistanceEnergy().getBasicEnergyDivisor()
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
