package com.kokuu.refinednetwork.common.redstonelink;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 无线红石端点的方块物品：说清「两个一组」「谁先谁后」「要两端都加载」。
 */
public class RedstoneLinkBlockItem extends BlockItem {
    public RedstoneLinkBlockItem(final RedstoneLinkBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(RedstoneLinkNames.DESC);
        lines.add(Component.empty());
        lines.add(RedstoneLinkNames.HINT);
    }
}
