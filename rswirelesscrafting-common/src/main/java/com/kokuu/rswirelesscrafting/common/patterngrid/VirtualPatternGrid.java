package com.kokuu.rswirelesscrafting.common.patterngrid;

import com.kokuu.rswirelesscrafting.common.DataComponents;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.virtual.VirtualTerminalSession;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
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
 * <p><b>扣电与回滚</b>：不需要给 RS 的菜单打 mixin —— 所有编辑最终都会触发 {@link #setChanged()}，
 * 于是这里对「输出槽里是否冒出了新样板」做一次前后指纹比对：冒出来了就扣一次造样板的电，
 * 付不起就把状态回滚到上一次保存的样子（样板从输出槽消失），两个加载器行为完全一致。
 *
 * <p>生命周期与一次开界面等长：界面打开时构造、状态从物品读入；之后每次编辑都写回物品；
 * 界面关闭后对象被丢弃（它从未注册进世界，不会泄漏）。
 */
public class VirtualPatternGrid extends PatternGridBlockEntity {
    /**
     * RS 存档里样板输出槽的键名（{@code PatternGridBlockEntity} 的同名私有常量）。
     */
    private static final String TAG_PATTERN_OUTPUT = "pattern_output";

    private final ItemStack stack;
    private final Player player;
    private final SlotReference slotReference;
    private final HolderLookup.Provider registries;
    private final VirtualTerminalSession session;
    /**
     * 「空输出槽」的序列化指纹：只有与它不同，才说明这次真的产出了样板。
     */
    private final String emptyOutputFingerprint;
    private String outputFingerprint;
    private CompoundTag lastSavedState;
    /**
     * 灌入存档状态期间置位：{@code loadAdditional} 内部会触发 {@code setChanged}，
     * 若不拦住就会把「只加载了一半」的状态写回物品，把剩下的数据冲掉。
     */
    private boolean loading;

    public VirtualPatternGrid(final ItemStack stack,
                              final Player player,
                              final SlotReference slotReference,
                              final Level level,
                              @Nullable final Network network,
                              final VirtualTerminalSession session) {
        super(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        this.stack = stack;
        this.player = player;
        this.slotReference = slotReference;
        this.registries = level.registryAccess();
        this.session = session;
        this.emptyOutputFingerprint = outputFingerprintOf(serialize());
        setLevel(level);
        loadState();
        // 载入之后的输出槽状态作为基线：玩家上次没取走的样板不会被重复计费。
        this.outputFingerprint = outputFingerprintOf(serialize());
        this.lastSavedState = serialize();
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
     * 所有编辑（摆配方、放样板、切类型、清空、产出样板）最终都会走到这里。
     * 这里既不能走原版实现（会加载区块），也正好是「写回物品 + 结算造样板」的天然时机。
     */
    @Override
    public void setChanged() {
        if (loading) {
            return;
        }
        if (!isTerminalValid()) {
            player.containerMenu.removed(player);
            player.containerMenu = player.inventoryMenu;
            return;
        }
        final CompoundTag state = serialize();
        final String fingerprint = outputFingerprintOf(state);
        final boolean produced = !fingerprint.equals(outputFingerprint)
            && !fingerprint.equals(emptyOutputFingerprint);
        // 产出一张样板按「自动合成」档计价——它服务的是自动化本身。
        final long cost = Platform.getConfig().getWirelessCraftingGrid().getAutocraftingEnergyUsage();
        if (produced && !session.tryCharge(cost)) {
            // 电量不足：这次动作当作没发生，把上一次保存的状态恢复回去。
            restore(lastSavedState);
            return;
        }
        outputFingerprint = fingerprint;
        lastSavedState = state;
        stack.set(DataComponents.INSTANCE.getPatternGridState(), state);
    }

    private boolean isTerminalValid() {
        return slotReference.resolve(player).map(current -> current == stack).orElse(false);
    }

    private CompoundTag serialize() {
        final CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    private void restore(final CompoundTag state) {
        loading = true;
        try {
            loadAdditional(state, registries);
        } finally {
            loading = false;
        }
    }

    private static String outputFingerprintOf(final CompoundTag state) {
        return state.getCompound(TAG_PATTERN_OUTPUT).toString();
    }
}
