package com.kokuu.rswirelesscrafting.common.smartsorter;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;

/**
 * 智能分拣站在 RS 网络里的节点：零常驻耗电。
 *
 * <p>与充电站、增幅塔同理——STABLE API 的方块基类不会自动 {@code doWork()} 扣电，
 * 实际扣电由方块实体按「搬走几组」动态发起，所以这里声明 0，不会双重扣电。
 */
public class SmartSorterNetworkNode extends AbstractNetworkNode {
    @Override
    public long getEnergyUsage() {
        return 0;
    }
}
