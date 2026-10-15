package com.kokuu.rswirelesscrafting.common.patterngrid;

import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.rswirelesscrafting.common.upgrade.Upgrades;

import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import net.minecraft.world.item.ItemStack;

/**
 * 一次「无线样板终端界面」会话期间的能量结算。
 *
 * <p>终端物品的 {@link NetworkItemContext} 在打开界面时创建、随这次会话存活；终端在整段会话里
 * 都握着这块上下文，所以扣电与终端其它动作走完全同一条通道：同样吃距离倍率、同样受能耗插件与
 * 远程输电插件影响。
 *
 * <p>付不起就**不扣**并返回 {@code false}，由调用方把这次动作当作没发生（见
 * {@link VirtualPatternGrid#setChanged()} 的回滚）。不采用「有多少扣多少」，否则终端只剩 1 FE
 * 时也能白嫖一张样板。
 */
public class WirelessPatternGridSession {
    private final ItemStack stack;
    private final NetworkItemContext context;

    public WirelessPatternGridSession(final ItemStack stack, final NetworkItemContext context) {
        this.stack = stack;
        this.context = context;
    }

    /**
     * 产出一张样板：按自动合成档计价——它服务的是自动化本身。
     */
    public boolean tryChargeCreatePattern() {
        return tryCharge(Platform.getConfig().getWirelessCraftingGrid().getAutocraftingEnergyUsage());
    }

    private boolean tryCharge(final long amount) {
        if (!canAfford(amount)) {
            return false;
        }
        context.drainEnergy(amount);
        return true;
    }

    /**
     * 装远程输电插件时看绑定网络的存量，否则看终端自身电池——与实际扣电的取电顺序一致。
     */
    private boolean canAfford(final long amount) {
        if (Upgrades.hasRemoteEnergyUpgrade(stack)) {
            return context.resolveNetwork()
                .map(network -> network.getComponent(EnergyNetworkComponent.class).getStored() >= amount)
                .orElse(false);
        }
        return AbstractWirelessTerminalItem.getStoredEnergy(stack) >= amount;
    }
}
