package com.kokuu.refinednetwork.common.virtual;

import com.kokuu.refinednetwork.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.refinednetwork.common.upgrade.Upgrades;

import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 一次「无线终端界面」会话期间的能量结算——所有借用 RS 界面 / 逻辑的终端共用这一份。
 *
 * <p>终端物品的 {@link NetworkItemContext} 在打开界面时创建、随这次会话存活；终端在整段会话里
 * 都握着这块上下文，所以扣电与终端其它动作走**完全同一条通道**：同样吃距离倍率、同样受
 * 能耗插件与远程输电插件影响。把结算收敛到这里，是为了避免每个终端各写一份（距离倍率
 * 与取电顺序一旦分叉，就会出现「同类操作收费不同」这种最难解释的 bug）。
 *
 * <p><b>付不起就不扣</b>：返回 {@code false} 且**不做任何扣电**，由调用方把这次动作当作没发生
 * （例如样板终端会把状态回滚）。刻意不采用「有多少扣多少」，否则终端只剩 1 FE 时也能白嫖。
 *
 * <p>计费口径由调用方给：开界面 5、造样板 40（自动合成档）、造一张样板 / 移动一次样板……
 * 具体数值来自配置，这里只负责「能不能付、付了就扣」。
 */
public class VirtualTerminalSession {
    private final ItemStack stack;
    private final Player player;
    private final NetworkItemContext context;

    public VirtualTerminalSession(final ItemStack stack, final Player player, final NetworkItemContext context) {
        this.stack = stack;
        this.player = player;
        this.context = context;
    }

    /**
     * 终端物品本身（会话期间一直有效）。
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * 试着付掉一笔电费：付得起就扣掉并返回 {@code true}，付不起则**一分不扣**。
     *
     * @param baseCost 基础耗电（不含距离倍率；倍率由上下文内部套用）
     */
    public boolean tryCharge(final long baseCost) {
        if (!canAfford(baseCost)) {
            return false;
        }
        context.drainEnergy(baseCost);
        return true;
    }

    /**
     * 装远程输电插件时优先看绑定网络的存量，不足时与扣电路径一样回落到终端电池；
     * 与 {@code ExtendedWirelessNetworkItemContext#canAfford} 保持同一口径。
     *
     * <p>必须先看余额再扣：网络在电量不足时也会把剩下的那点抽走，直接判
     * 「扣回来的够不够」会让网络白扣一份、余额再从电池补一次（双重收费）。
     */
    public boolean canAfford(final long baseCost) {
        if (!RefinedStorageApi.INSTANCE.isEnergyRequired()) {
            return true;
        }
        final long actualCost = AbstractWirelessTerminalItem.actionCost(stack, player, baseCost);
        final boolean networkCovers = Upgrades.hasRemoteEnergyUpgrade(stack)
            && context.resolveNetwork()
                .map(network -> network.getComponent(EnergyNetworkComponent.class).getStored() >= actualCost)
                .orElse(false);
        return networkCovers || AbstractWirelessTerminalItem.getStoredEnergy(stack) >= actualCost;
    }
}
