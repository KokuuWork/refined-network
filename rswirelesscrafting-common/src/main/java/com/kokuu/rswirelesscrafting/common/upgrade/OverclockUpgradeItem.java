package com.kokuu.rswirelesscrafting.common.upgrade;

import com.kokuu.rswirelesscrafting.common.ContentNames;

import java.util.List;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * 超频升级卡：插进 **Refined Storage 原版的自动合成器**，让它跑得更快。
 *
 * <p>RS 自己的速度升级卡有硬上限——4 张封顶（等待间隔 2 tick），再多会掉进
 * {@code default -> 0} 分支，而那个 0 会撞上 {@code ticks % tickRate} 直接除零崩游戏。
 * 所以「更快」这件事在 RS 里原本没有出口。
 *
 * <p>这张卡换一条路：不动 RS 的档位表，而是在它判断「这一 tick 能不能推进」时
 * （{@code canStep} 里读 {@code tickRate} 的那一刻）**把等待间隔折半**，
 * 见两侧加载器的 {@code AbstractAutocrafterOverclockMixin}。
 *
 * <p>两条安全约定：
 * <ul>
 *   <li><b>绝不制造 0 间隔</b>：折算结果最小是 1（每 tick 推进），不会除零；</li>
 *   <li><b>注入失败即静默失效</b>：mixin 声明 {@code require = 0}，将来 RS 改了方法或字段名，
 *       最坏情况是这张卡没效果，而不是游戏起不来。</li>
 * </ul>
 *
 * <p>代价走 RS 自己的账：{@link #getEnergyUsage()} 会被计入合成器的总耗电。
 */
public class OverclockUpgradeItem extends Item implements UpgradeItem {
    /**
     * 这张卡给宿主带来的额外耗电（FE/t），由 RS 的升级系统累加。
     */
    private static final long ENERGY_USAGE = 32L;

    public OverclockUpgradeItem() {
        super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC));
    }

    @Override
    public long getEnergyUsage() {
        return ENERGY_USAGE;
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
        lines.add(ContentNames.OVERCLOCK_UPGRADE_DESC);
        lines.add(ContentNames.OVERCLOCK_UPGRADE_EFFECT);
        lines.add(ContentNames.OVERCLOCK_UPGRADE_HINT);
    }
}
