package com.kokuu.rswirelesscrafting.common.networkbridge;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.energy.EnergyProvider;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageProvider;
import com.refinedmods.refinedstorage.api.storage.Storage;

/**
 * 网络桥接器在 RS 网络里的节点：同时是 {@link StorageProvider} 与 {@link EnergyProvider}。
 *
 * <p>这两个身份就是「两张网互通」的全部机制：
 * <ul>
 *   <li>网络的总存量 = 所有 {@code StorageProvider} 之和、总电量 = 所有
 *       {@code EnergyProvider} 之和（{@code StorageNetworkComponent} /
 *       {@code EnergyNetworkComponent} 原文）；</li>
 *   <li>而 {@code EnergyNetworkComponent} **只有 extract、没有 insert**——
 *       所以「把电送给另一张网」也必须靠成为它的提供者，没有第二条路。</li>
 * </ul>
 *
 * <p>于是节点一挂进本地网络，本地网络就同时「看见」并「取用」远端网络的库存与电量。
 * 两个方向都通（读得到、也取得动），这就是「互通」而不是「单向中继」。
 *
 * <p>方块实体由 {@link #attach} 在构造末期补进来——Java 不允许把 {@code this}
 * 传进 {@code super(...)}，而节点的提供者方法只会在「容器加入网络」时才被调用，
 * 那时候早已补好了。
 */
public class NetworkBridgeNetworkNode extends AbstractNetworkNode implements StorageProvider, EnergyProvider {
    @Nullable
    private NetworkBridgeBlockEntity blockEntity;
    @Nullable
    private BridgedStorage storage;

    public void attach(final NetworkBridgeBlockEntity bridge) {
        this.blockEntity = bridge;
    }

    @Override
    public long getEnergyUsage() {
        return 0;
    }

    @Override
    public Storage getStorage() {
        if (storage == null) {
            storage = new BridgedStorage(blockEntity);
        }
        return storage;
    }

    @Override
    public long getStored() {
        final NetworkBridgeBlockEntity current = blockEntity;
        return current == null ? 0L : current.getRemoteEnergyStored();
    }

    @Override
    public long getCapacity() {
        final NetworkBridgeBlockEntity current = blockEntity;
        return current == null ? 0L : current.getRemoteEnergyCapacity();
    }

    /**
     * 本地网络向我们要电：从远端网络抽，按桥的能量限速放行。
     */
    @Override
    public long extract(final long amount) {
        final NetworkBridgeBlockEntity current = blockEntity;
        return current == null ? 0L : current.extractRemoteEnergy(amount);
    }
}
