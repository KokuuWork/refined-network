package com.kokuu.refinednetwork.common.virtual;

import java.util.function.BiFunction;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「虚拟方块实体」的公共搭法。
 *
 * <p>虚拟方块实体 = <b>脱离世界存在</b>的一个 RS 方块实体，只为「借它的界面 / 借它的逻辑」而存在。
 * 已有的样板终端就是这条路（{@code VirtualPatternGrid}），无线合成管理器继续用它。三条铁律：
 *
 * <ol>
 *   <li><b>用对应方块的<b>真实默认状态</b>构造</b>：RS 2.0.9 起会校验「BE 类型 ↔ 方块状态」，
 *       AIR 会抛 {@code Invalid block entity}；幽灵节点改为在子类把
 *       {@code initialize(ServerLevel, Direction)} 覆写成空来阻止。</li>
 *   <li><b>覆写 {@code setChanged()}</b>：原版实现会去 {@code level.blockEntityChanged(pos)} 加载坐标所在区块。
 *       虚拟方块实体要么覆写成空，要么（像我方样板终端那样）把「状态变了」重定向到
 *       「把状态写回物品的数据组件」。</li>
 *   <li><b>入网只 {@link #joinNetwork} 一句</b>：脱离世界的对象不属于任何网络图，
 *       所以<b>不需要</b> {@code NetworkAttachment} 那套「addContainer + removeContainer」生命周期
 *       —— 只要告诉节点「你的网络是这张」就够了。</li>
 * </ol>
 */
public final class VirtualBlockEntities {
    private VirtualBlockEntities() {
    }

    /**
     * 造一个脱离世界的方块实体实例。
     *
     * @param factory 该方块实体的构造器引用，签名形如 {@code (BlockPos, BlockState) -> T}
     */
    public static <T extends BlockEntity> T createDetached(
        final BlockState state,
        final BiFunction<BlockPos, BlockState, T> factory
    ) {
        return factory.apply(BlockPos.ZERO, state);
    }

    /**
     * 让虚拟方块实体「能查网络」。
     *
     * <p>与真实方块不同，这里不做 {@code network.addContainer(...)}：虚拟对象不参与网络图，
     * 它只是借一个网络引用去查资源、拿存储。
     *
     * @param network 终端绑定的那张网络；传 {@code null} 即断开（玩家没绑 / 网络没了）
     */
    public static void joinNetwork(final AbstractNetworkNode node, @Nullable final Network network) {
        node.setNetwork(network);
    }

    /**
     * 让虚拟方块实体「有一个世界」——配方解析、维度判断之类都需要它。
     *
     * <p>注意传的是<b>玩家所在的世界</b>，不是坐标所在的世界：实例本身不在世界里。
     */
    public static void attachLevel(final BlockEntity blockEntity, final Level level) {
        blockEntity.setLevel(level);
    }
}
