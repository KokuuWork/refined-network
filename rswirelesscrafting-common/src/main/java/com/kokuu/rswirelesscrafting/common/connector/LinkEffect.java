package com.kokuu.rswirelesscrafting.common.connector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * 连接器绑定成功的视觉反馈：在设备与接入点之间撒一条电火花线。
 *
 * <p>为什么要这个：绑定是「看不见的动作」——点一下，聊天栏弹一行字，玩家并不知道这两台设备
 * 究竟连到了哪里。撒一条线过去，位置关系一眼就清楚。
 *
 * <p>全部用原版粒子（{@link ParticleTypes#ELECTRIC_SPARK}）实现，服务端单向广播，
 * 不需要自定义数据包，也不需要客户端代码。跨维度没有同一条直线可画，只在设备端打一圈火花。
 */
public final class LinkEffect {
    /**
     * 一条线最多撒多少个点（避开超远距离一次刷爆粒子）。
     */
    private static final int MAX_POINTS = 160;
    /**
     * 相邻两点的间距（格）。约 0.35 格一个点，看起来是连续的一条线而不是一串珠子。
     */
    private static final double POINT_SPACING = 0.35;
    /**
     * 光环半径（格）与点数。
     */
    private static final double RING_RADIUS = 0.85;
    private static final int RING_POINTS = 20;

    private LinkEffect() {
    }

    /**
     * 在 {@code from} 与 {@code to} 之间画一条短暂的连线。
     */
    public static void flash(final ServerLevel level, final BlockPos from, final GlobalPos to) {
        // 两端各画一圈光环：距离再近（甚至贴着放）也能看见这次绑定确实发生了。
        ring(level, from);
        if (!to.dimension().equals(level.dimension())) {
            // 跨世界没有一条连续的直线可画，只能让设备端亮起来 + 靠聊天栏报坐标。
            level.playSound(null, from, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.5F);
            return;
        }
        final BlockPos target = to.pos();
        final double distance = Math.sqrt(from.distSqr(target));
        final int points = (int) Math.max(2L, Math.min(MAX_POINTS, Math.round(distance / POINT_SPACING)));
        for (int i = 0; i <= points; i++) {
            final double t = i / (double) points;
            level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                Mth.lerp(t, from.getX() + 0.5, target.getX() + 0.5),
                Mth.lerp(t, from.getY() + 0.5, target.getY() + 0.5),
                Mth.lerp(t, from.getZ() + 0.5, target.getZ() + 0.5),
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
        ring(level, target);
        level.playSound(null, from, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.5F);
    }

    /**
     * 方块周围一圈光环（16 个点）。粒子选 {@code END_ROD}，寿命比电火花长，拉近看也不会一闪就没。
     */
    private static void ring(final ServerLevel level, final BlockPos pos) {
        final double cx = pos.getX() + 0.5;
        final double cy = pos.getY() + 0.55;
        final double cz = pos.getZ() + 0.5;
        for (int i = 0; i < RING_POINTS; i++) {
            final double angle = i * Math.PI * 2.0 / RING_POINTS;
            level.sendParticles(
                ParticleTypes.END_ROD,
                cx + Math.cos(angle) * RING_RADIUS,
                cy,
                cz + Math.sin(angle) * RING_RADIUS,
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }
}
