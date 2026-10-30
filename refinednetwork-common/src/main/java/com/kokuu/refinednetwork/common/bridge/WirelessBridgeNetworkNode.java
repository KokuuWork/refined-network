package com.kokuu.refinednetwork.common.bridge;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;

/**
 * 无线桥在 RS 网络里的节点：声明常驻耗电。
 *
 * <p>与充电站、增幅塔同理——STABLE API 的方块基类不会自动 {@code doWork()} 扣电，
 * 实际扣电由方块实体每 tick 手动发起，所以这里声明的是「数值来源」，不会双重扣电。
 */
public class WirelessBridgeNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;

    public WirelessBridgeNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
