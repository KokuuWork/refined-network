package com.kokuu.refinednetwork.common.link;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 「这个方块旁边是哪张网络」的公共查询。
 *
 * <p>RS 的网络方块有两条互斥的公开路径，必须都试：
 * <ul>
 *   <li>{@link NetworkItemTargetBlockEntity}——RS 自己的方块实体（控制器、线缆、机器）走这条；</li>
 *   <li>{@link NetworkNodeContainerProvider}——容器直接实现在方块实体上的方块（本模组的设备、部分
 *       第三方模组）走这条。</li>
 * </ul>
 * 只查后者会让「贴着控制器」永远读不到网络。
 */
public final class AdjacentNetworks {
    private static final Direction[] DIRECTIONS = Direction.values();

    private AdjacentNetworks() {
    }

    /**
     * 六邻域里任意一张网络；旁边没有网络时返回 {@code null}。
     */
    @Nullable
    public static Network find(final Level level, final BlockPos pos) {
        for (final Direction direction : DIRECTIONS) {
            final Network network = at(level, pos.relative(direction));
            if (network != null) {
                return network;
            }
        }
        return null;
    }

    @Nullable
    private static Network at(final Level level, final BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof NetworkItemTargetBlockEntity itemTarget) {
            final Network network = itemTarget.getNetworkForItem();
            if (network != null) {
                return network;
            }
        }
        if (blockEntity instanceof NetworkNodeContainerProvider provider) {
            for (final var container : provider.getContainers()) {
                final Network network = container.getNode().getNetwork();
                if (network != null) {
                    return network;
                }
            }
        }
        return null;
    }
}
