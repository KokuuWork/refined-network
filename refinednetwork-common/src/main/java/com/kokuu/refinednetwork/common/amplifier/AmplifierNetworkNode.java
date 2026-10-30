package com.kokuu.refinednetwork.common.amplifier;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;

/**
 * 增幅塔在 RS 网络里的节点：只声明常驻耗电。
 *
 * <p>与充电站同理——STABLE API 的 {@code AbstractNetworkNodeContainerBlockEntity} 不会自动
 * {@code doWork()} 扣电，实际扣电由方块实体每 tick 手动发起，所以这里声明的是「数值来源」，
 * 不会出现双重扣电。
 */
public class AmplifierNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;

    public AmplifierNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
