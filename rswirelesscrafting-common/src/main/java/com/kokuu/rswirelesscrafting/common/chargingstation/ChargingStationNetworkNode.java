package com.kokuu.rswirelesscrafting.common.chargingstation;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;

/**
 * 充电站在 RS 网络里的节点。本身只声明「待机耗电」，真正的抽电动作由方块实体按需发起
 * （有线充入多少、无线充入多少，都是动态的，不能只靠固定的 getEnergyUsage）。
 */
public class ChargingStationNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;

    public ChargingStationNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
