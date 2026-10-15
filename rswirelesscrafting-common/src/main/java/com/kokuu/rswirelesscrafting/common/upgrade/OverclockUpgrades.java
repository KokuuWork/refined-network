package com.kokuu.rswirelesscrafting.common.upgrade;

import com.kokuu.rswirelesscrafting.common.Items;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * 超频卡与 RS 升级系统的接线。
 *
 * <p>注册进 RS 的升级注册表之后，原版自动合成器的升级槽就会**接受**这张卡、
 * 按它的 {@link com.refinedmods.refinedstorage.common.api.upgrade.UpgradeItem#getEnergyUsage()}
 * 计入耗电，并在工具提示里把它列出来——全走公开 API，不碰 RS 的槽位逻辑。
 */
public final class OverclockUpgrades {
    /**
     * 一台机器最多插几张（与原版速度升级卡的上限一致，便于理解）。
     */
    public static final int MAX_PER_MACHINE = 4;

    private OverclockUpgrades() {
    }

    /**
     * 在初始化时调用一次：把这件升级卡登记为「自动合成器可用的升级」。
     */
    public static void register() {
        RefinedStorageApi.INSTANCE.getUpgradeRegistry()
            .forDestination(UpgradeDestinations.AUTOCRAFTER)
            .add(Items.INSTANCE.getOverclockUpgrade(), MAX_PER_MACHINE);
    }

    /**
     * 数出一个升级容器里插了几张超频卡。
     *
     * <p>刻意**不**走 RS 的 {@code getAmount}：那个方法按「注册过的升级物品」建索引，
     * 依赖它的内部实现；直接遍历 4 个槽更简单、也更不可能被 RS 的改动带偏。
     */
    public static int countIn(final Container container) {
        final ItemStack overclock = Items.INSTANCE.getOverclockUpgrade().getDefaultInstance();
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            final ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, overclock)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 把「基础等待间隔」按插卡数量折半。每张卡折半一次，**最低 1**（即每 tick 都能推进）。
     *
     * <p>用折半而不是直接减一，是因为 RS 的基础间隔跨度很大（没插速度卡时是 10 tick），
     * 减一几乎感觉不到；折半一眼就能看出快了多少。
     */
    public static int effectiveTickRate(final int baseTickRate, final int cards) {
        int rate = Math.max(1, baseTickRate);
        for (int i = 0; i < cards; i++) {
            rate = Math.max(1, rate / 2);
        }
        return rate;
    }
}
