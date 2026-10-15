package com.kokuu.rswirelesscrafting.common.link;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * 「无线桥」那一侧的区块强加载登记表（引用计数）。
 *
 * <p>无线桥的据点在很远的地方（可能跨维度），要让它在玩家离开后继续工作，就必须把它的区块
 * 强加载起来 —— 这和 RS 原生「网络变送器 / 网络接收器」隧道要求两端区块加载是同一件事，
 * 区别是本模组把它做成**据点的内建行为**，玩家不用自己去装区块加载器。
 *
 * <p><b>为什么要引用计数</b>：原版 {@code ServerLevel#setChunkForced} 是按区块给的，
 * 同一个区块里有两座据点时，先拆掉的那座如果把区块释放了，另一座就被顺带卸载
 * ——所以按区块计数，只在 0 → 1 时真正强加载、1 → 0 时真正释放。
 *
 * <h2>🚨 铁律：强加载不依赖能量</h2>
 * 一旦做成「没电就撤销强加载」，就会死锁：断电 → 区块卸载 → 方块实体不再 tick → 永远回不来。
 * 所以强加载只在**配对成立期间**维持，断电只是让桥停止搬运，不做撤销。只有拆方块 / 解绑才释放。
 *
 * <p><b>已知残留</b>：如果带着已配对、还强加载着区块的据点把模组卸掉，那些区块的强加载记录会留在
 * 存档里（在世界的强制加载数据里）。所以 README 里提醒：**卸模组前先拆掉据点**。
 */
public final class LinkRegistry {
    private static final Map<ResourceKey<Level>, Map<Long, Integer>> HELD = new ConcurrentHashMap<>();

    private LinkRegistry() {
    }

    /**
     * 占住这个坐标所在区块的强加载名额。
     *
     * @return 这次调用是否**真正**触发了强加载（即从 0 变 1 的那一次）
     */
    public static boolean acquire(final ServerLevel level, final BlockPos pos) {
        final long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        final Map<Long, Integer> chunks = HELD.computeIfAbsent(
            level.dimension(),
            key -> new HashMap<>()
        );
        final int count = chunks.merge(chunkKey, 1, Integer::sum);
        if (count == 1) {
            level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
            return true;
        }
        return false;
    }

    /**
     * 释放一个名额；只有最后一个持有者释放时才真正撤销强加载。
     */
    public static void release(final ServerLevel level, final BlockPos pos) {
        final long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        final Map<Long, Integer> chunks = HELD.get(level.dimension());
        if (chunks == null) {
            return;
        }
        final Integer count = chunks.get(chunkKey);
        if (count == null) {
            return;
        }
        if (count <= 1) {
            chunks.remove(chunkKey);
            level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
        } else {
            chunks.put(chunkKey, count - 1);
        }
    }

    /**
     * 当前是否有人占着这个坐标所在区块（调试与 tooltip 用）。
     */
    public static boolean isHeld(final Level level, final BlockPos pos) {
        final Map<Long, Integer> chunks = HELD.get(level.dimension());
        if (chunks == null) {
            return false;
        }
        return chunks.containsKey(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
    }
}
