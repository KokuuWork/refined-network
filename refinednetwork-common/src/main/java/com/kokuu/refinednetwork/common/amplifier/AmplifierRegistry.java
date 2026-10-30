package com.kokuu.refinednetwork.common.amplifier;

import com.kokuu.refinednetwork.common.Blocks;
import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.Platform;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「无线增幅塔」的位置登记表：哪些坐标上站着增幅塔。
 *
 * <p>登记表**两端都填**——方块实体在区块加载时登记、卸载时注销，服务端与客户端都一样。
 * 于是距离倍率的计算（服务端扣费用一次、客户端 tooltip / HUD 显示再用一次）
 * 在两边得到<b>完全相同</b>的结果，不需要为显示专门跑一条同步通道。
 *
 * <p>「这座塔是否是激活状态」不看方块实体（客户端那边它不 tick），而是看**方块状态**
 * ——{@link AmplifierBlock#ACTIVE} 是服务端算好写进去、随区块自动同步到客户端的，
 * 天然两侧一致。
 */
public final class AmplifierRegistry {
    private static final Map<Level, Set<BlockPos>> AMPLIFIERS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private AmplifierRegistry() {
    }

    public static void add(final Level level, final BlockPos pos) {
        AMPLIFIERS.computeIfAbsent(level, key -> new CopyOnWriteArraySet<>()).add(pos.immutable());
    }

    public static void remove(final Level level, final BlockPos pos) {
        final Set<BlockPos> positions = AMPLIFIERS.get(level);
        if (positions == null) {
            return;
        }
        positions.remove(pos);
        if (positions.isEmpty()) {
            AMPLIFIERS.remove(level, positions);
        }
    }

    /**
     * 把距离倍率交给增幅塔打折：玩家落在任一座**激活**的塔的覆盖范围内就生效，否则原样返回。
     *
     * <p>只影响同维度——跨维度本来就套固定倍率，那条线留给维度升级卡，不给塔打折扣。
     *
     * @param multiplierPercent 未打折前的距离倍率（百分比）
     * @return 打折后的倍率；没有任何塔罩着时等于入参
     */
    public static long applyAmplifier(final Level level,
                                      final double x,
                                      final double z,
                                      final long multiplierPercent) {
        final Config.AmplifierEntry config = Platform.getConfig().getAmplifier();
        if (!config.isEnabled() || multiplierPercent <= 100L) {
            return multiplierPercent;
        }
        final Set<BlockPos> positions = AMPLIFIERS.get(level);
        if (positions == null || positions.isEmpty()) {
            return multiplierPercent;
        }
        final double radius = config.getRadius();
        final double radiusSquared = radius * radius;
        for (final BlockPos pos : positions) {
            if (!level.getBlockState(pos).is(Blocks.INSTANCE.getWirelessAmplifier())) {
                // 自清理：方块已被拆掉 / 区块早已卸载时没来得及注销的残留坐标。
                positions.remove(pos);
                continue;
            }
            if (!isActive(level, pos)) {
                continue;
            }
            final double dx = x - (pos.getX() + 0.5);
            final double dz = z - (pos.getZ() + 0.5);
            if (dx * dx + dz * dz <= radiusSquared) {
                return Math.max(100L, multiplierPercent * config.getMultiplierPercent() / 100L);
            }
        }
        return multiplierPercent;
    }

    /**
     * 这个位置被增幅塔罩着的话，返回塔的覆盖半径（格）；没被罩着返回 0。
     *
     * <p>给无线配置卡用：站进塔的覆盖里，批量半径就从基础值扩到塔的半径——
     * 让「增幅塔」除了给终端打折，还能表示「这片区域归我管」。
     */
    public static int coverageRadiusAt(final Level level, final double x, final double z) {
        final Config.AmplifierEntry config = Platform.getConfig().getAmplifier();
        if (!config.isEnabled()) {
            return 0;
        }
        final Set<BlockPos> positions = AMPLIFIERS.get(level);
        if (positions == null || positions.isEmpty()) {
            return 0;
        }
        final double radius = config.getRadius();
        final double radiusSquared = radius * radius;
        for (final BlockPos pos : positions) {
            if (!isActive(level, pos)) {
                continue;
            }
            final double dx = x - (pos.getX() + 0.5);
            final double dz = z - (pos.getZ() + 0.5);
            if (dx * dx + dz * dz <= radiusSquared) {
                return config.getRadius();
            }
        }
        return 0;
    }

    private static boolean isActive(final Level level, final BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        return state.is(Blocks.INSTANCE.getWirelessAmplifier())
            && state.getValue(AmplifierBlock.ACTIVE);
    }
}
