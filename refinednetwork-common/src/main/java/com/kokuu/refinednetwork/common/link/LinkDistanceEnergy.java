package com.kokuu.refinednetwork.common.link;

import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.amplifier.AmplifierRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;

/**
 * 「方块 ↔ 绑定位置」的距离倍率——和手持终端那套口径完全一致，只是把参考点从玩家换成了方块。
 *
 * <p>无线桥、无线能量接收器这类「隔着距离用别处的网络」的方块，链路费都按这个倍率缩放：
 * 距离越远越贵，跨维度直接套固定倍率，站在增幅塔覆盖范围内还能打折。
 * 因为复用的是同一份 {@link Config.DistanceEnergyEntry}，玩家在终端 tooltip 上看到的
 * 「距离倍率」和这些方块实际扣的倍率永远是同一个数。
 */
public final class LinkDistanceEnergy {
    private LinkDistanceEnergy() {
    }

    /**
     * @param level  方块所在世界
     * @param from   方块自身坐标
     * @param target 绑定位置
     * @return 倍率百分比（≥100）
     */
    public static long multiplierPercent(final Level level, final BlockPos from, final GlobalPos target) {
        final Config.DistanceEnergyEntry config = Platform.getConfig().getDistanceEnergy();
        if (!level.dimension().equals(target.dimension())) {
            return config.getInterdimensionalMultiplier();
        }
        final double dx = from.getX() + 0.5 - (target.pos().getX() + 0.5);
        final double dz = from.getZ() + 0.5 - (target.pos().getZ() + 0.5);
        return AmplifierRegistry.applyAmplifier(level, from.getX() + 0.5, from.getZ() + 0.5, linear(config, dx, dz));
    }

    private static long linear(final Config.DistanceEnergyEntry config, final double dx, final double dz) {
        final double distance = Math.sqrt(dx * dx + dz * dz);
        final int baseRange = Math.max(1, config.getBaseRange());
        if (distance <= baseRange) {
            return 100L;
        }
        return Math.max(100L, Math.round(100.0 * distance / baseRange));
    }
}
