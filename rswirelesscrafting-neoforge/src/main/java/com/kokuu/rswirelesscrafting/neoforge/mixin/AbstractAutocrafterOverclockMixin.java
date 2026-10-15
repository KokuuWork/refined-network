package com.kokuu.rswirelesscrafting.neoforge.mixin;

import com.kokuu.rswirelesscrafting.common.upgrade.OverclockUpgrades;

import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterBlockEntity;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 超频卡的**实际效果**：在 RS 判断「这一 tick 能不能推进」时，把它读到的等待间隔
 * （tickRate 字段）按插卡数量折半。
 *
 * <p>刻意只替换「读 tickRate 这个字段」这一处，而不是改写 canStep 的返回值——
 * 于是机器是否激活、是否轮到链式合成器处理等判断**全部保持原样**。换句话说，
 * 这张卡只会让**本来就在工作的**合成器跑快，不会让没电的机器凭空动起来。
 *
 * <p>require = 0：RS 将来改了方法名或字段名时，注入安静失效（卡变成没效果），
 * 而不是让游戏起不来。
 *
 * <p>两侧加载器各有一份（包名不同），代码完全一致。
 */
@Mixin(AutocrafterBlockEntity.class)
public abstract class AbstractAutocrafterOverclockMixin {
    @Shadow
    @Final
    private UpgradeContainer upgradeContainer;

    @Shadow
    private int tickRate;

    @Redirect(
        method = "canStep",
        at = @At(
            value = "FIELD",
            target = "Lcom/refinedmods/refinedstorage/common/autocrafting/autocrafter/"
                + "AutocrafterBlockEntity;tickRate:I"
        ),
        require = 0,
        remap = false
    )
    private int rswirelesscraftingOverclockTickRate(final AutocrafterBlockEntity instance) {
        final int cards = OverclockUpgrades.countIn(upgradeContainer);
        if (cards <= 0) {
            return tickRate;
        }
        return OverclockUpgrades.effectiveTickRate(tickRate, cards);
    }
}
