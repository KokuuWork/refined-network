package com.kokuu.refinednetwork.common.energyreceiver;

import com.refinedmods.refinedstorage.api.network.energy.EnergyProvider;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;

/**
 * 无线能量接收器在 RS 网络里的节点。
 *
 * <p>它同时是 {@link EnergyProvider}——这正是「把远处发电机的电无线注入网络」的关键：
 * RS 网络的总电量就是**所有能量提供者之和**（{@code EnergyNetworkComponentImpl} 原文），
 * 所以只要我们的节点被挂进那张网络，它的缓冲就直接算进网络电量池，
 * 网络里任何机器要用电时，网络会回头找各个提供者 {@code extract}，我们的实现就从缓冲里出。
 *
 * <p><b>没有 insert 这条路</b>：{@code EnergyNetworkComponent} 只暴露
 * {@code extract}，没有对应的注入方法，所以「给网络充电」唯一正经做法就是成为它的提供者。
 *
 * <p>耗电不在这里声明：链路费按距离算、由方块实体自己扣，
 * 写成固定的 {@code getEnergyUsage()} 会既不能反映距离、又会在节点被挂到别处时失灵。
 */
public class WirelessEnergyReceiverNetworkNode extends AbstractNetworkNode implements EnergyProvider {
    private final WirelessEnergyReceiverBlockEntity blockEntity;

    public WirelessEnergyReceiverNetworkNode(final WirelessEnergyReceiverBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public long getEnergyUsage() {
        return 0;
    }

    @Override
    public long getStored() {
        return blockEntity.getBuffer();
    }

    @Override
    public long getCapacity() {
        return blockEntity.getCapacity();
    }

    /**
     * 网络向我们要电：从缓冲里出，出多少算多少。
     *
     * <p>注意**不**在这里向外部发电机拉电——拉电是方块实体每一 tick 做的事
     * （外部模组按标准能量能力推进），提供者只负责把已有的缓冲交出去。
     */
    @Override
    public long extract(final long amount) {
        return blockEntity.drainBuffer(amount);
    }
}
