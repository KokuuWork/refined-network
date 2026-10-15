package com.kokuu.rswirelesscrafting.common.patterngrid;

import com.kokuu.rswirelesscrafting.common.DataComponents;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * 「无线样板终端」背后那座**虚拟的**样板网格。
 *
 * <p>RS 2.0.7 没有把样板网格抽成接口（不像 {@code CraftingGrid} 那样为「方块 + 物品」双形态设计过），
 * 菜单的服务端构造更是硬绑 {@link PatternGridBlockEntity}。所以本模组不去复刻那 1400 行界面逻辑，
 * 而是**造一个脱离世界存在的真方块实体**，让 RS 自己的菜单、数据包、界面照常工作：
 *
 * <ul>
 *   <li><b>不注册幽灵节点</b>：方块状态传 {@code AIR}（没有方向属性），
 *       {@code setLevel} 内部的节点初始化会在「取不到方向」时提前返回，不会往世界里挂节点；</li>
 *   <li><b>不碰区块</b>：覆写 {@link #setChanged()}，把「标记脏」换成「把状态写回终端物品的数据组件」，
 *       否则原版实现会去 {@code level.blockEntityChanged(pos)} 加载坐标所在区块；</li>
 *   <li><b>能读配方</b>：{@code setLevel(player.level())} 让合成 / 切石 / 锻造的配方解析正常工作；</li>
 *   <li><b>能拿资源</b>：{@code mainNetworkNode.setNetwork(...)} 挂上终端绑定的网络，
 *       于是左侧资源栏、配方候选排序、存储读写全部走正常路径。</li>
 * </ul>
 *
 * <p>生命周期与一次开界面等长：界面打开时构造、状态从物品读入；之后任何编辑都经
 * {@link #setChanged()} 写回物品；界面关闭后对象被丢弃（它从未注册进世界，不会泄漏）。
 */
public class VirtualPatternGrid extends PatternGridBlockEntity {
    private final ItemStack stack;
    private final HolderLookup.Provider registries;
    /**
     * 灌入存档状态期间置位：{@code loadAdditional} 内部会触发 {@code setChanged}，
     * 若不拦住就会把「只加载了一半」的状态写回物品，把剩下的数据冲掉。
     */
    private boolean loading;

    public VirtualPatternGrid(final ItemStack stack, final Level level, @Nullable final Network network) {
        super(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        this.stack = stack;
        this.registries = level.registryAccess();
        setLevel(level);
        loadState();
        mainNetworkNode.setNetwork(network);
    }

    private void loadState() {
        final CompoundTag tag = stack.get(DataComponents.INSTANCE.getPatternGridState());
        if (tag == null) {
            return;
        }
        loading = true;
        try {
            loadAdditional(tag, registries);
        } finally {
            loading = false;
        }
    }

    /**
     * 所有编辑（摆配方、放样板、切类型、清空）最终都会走到这里。
     * 这里既不能走原版实现（会加载区块），也正好是「把状态写回物品」的天然时机。
     */
    @Override
    public void setChanged() {
        if (loading) {
            return;
        }
        final CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        stack.set(DataComponents.INSTANCE.getPatternGridState(), tag);
    }
}
