package com.kokuu.rswirelesscrafting.common.link;

import java.util.Optional;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 「绑定位置 → 那张网络」的统一解析器。
 *
 * <p>无线桥、无线配置卡、无线上行/下行这些东西都只认一套绑定语义，和终端完全一致——
 * 玩家心里的模型只有一条：<b>绑定 = 我在用那个东西</b>。
 *
 * <p>RS 没有公开「按坐标取网络」的 API，但两条公开路径合起来就够用了：
 * <ol>
 *   <li>{@link NetworkItemTargetBlockEntity}——网络接收器与无线访问点都实现了它，
 *       正是「物品绑过去就能拿网络」的那条路；</li>
 *   <li>{@link NetworkNodeContainerProvider}——任何 RS 网络方块（控制器、线缆、机器）都是它，
 *       从它托管的一个节点上就能问出所在网络。</li>
 * </ol>
 */
public final class BoundTargetResolver {
    private BoundTargetResolver() {
    }

    /**
     * 解析绑定位置上的网络。
     *
     * <p><b>只处理同维度</b>：跨维度不走这条路（那是无线桥的活，那边靠链路注册表两端配对）。
     *
     * @param level  绑定方块所在的世界（调用方已确认它与 {@code target} 同维度）
     * @param target 绑定位置
     * @return 该位置上的网络；方块不见了 / 不是 RS 网络方块 / 孤岛网络（没有控制器）时为空
     */
    public static Optional<Network> resolveNetwork(final Level level, final GlobalPos target) {
        if (!level.dimension().equals(target.dimension())) {
            return Optional.empty();
        }
        final BlockEntity blockEntity = level.getBlockEntity(target.pos());
        if (blockEntity instanceof NetworkItemTargetBlockEntity itemTarget) {
            return Optional.ofNullable(itemTarget.getNetworkForItem());
        }
        if (blockEntity instanceof NetworkNodeContainerProvider provider) {
            // 网络方块托管多个节点（比如线缆两端），任取一个能问出网络就够。
            for (final InWorldNetworkNodeContainer container : provider.getContainers()) {
                final Network network = container.getNode().getNetwork();
                if (network != null) {
                    return Optional.of(network);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 该位置现在是否还有「能给出网络」的方块（用于判断绑定是否失效）。
     */
    public static boolean isTargetPresent(final Level level, final GlobalPos target) {
        if (!level.dimension().equals(target.dimension())) {
            return false;
        }
        return level.getBlockEntity(target.pos()) instanceof NetworkItemTargetBlockEntity
            || level.getBlockEntity(target.pos()) instanceof NetworkNodeContainerProvider;
    }
}
