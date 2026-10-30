package com.kokuu.refinednetwork.fabric;

import com.kokuu.refinednetwork.common.energyreceiver.WirelessEnergyReceiverBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/**
 * 把无线能量接收器的 FE 缓冲暴露成 Team Reborn Energy 的消费端。
 *
 * <p>能量写入必须参与事务：嵌套事务或外层事务回滚时，缓冲恢复到事务前快照，
 * 不能因为「先写了再说」凭空产生能量。
 */
public final class WirelessEnergyReceiverEnergyStorage extends SnapshotParticipant<Long>
    implements EnergyStorage {
    private final WirelessEnergyReceiverBlockEntity blockEntity;

    public WirelessEnergyReceiverEnergyStorage(final WirelessEnergyReceiverBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public long insert(final long maxAmount, final TransactionContext transaction) {
        final long accepted = blockEntity.receiveExternalEnergy(maxAmount, true);
        if (accepted > 0) {
            updateSnapshots(transaction);
            blockEntity.receiveExternalEnergy(accepted, false);
        }
        return accepted;
    }

    @Override
    public long extract(final long maxAmount, final TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getAmount() {
        return blockEntity.getBuffer();
    }

    @Override
    public long getCapacity() {
        return blockEntity.getCapacity();
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    protected Long createSnapshot() {
        return blockEntity.getBuffer();
    }

    @Override
    protected void readSnapshot(final Long snapshot) {
        blockEntity.restoreBuffer(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        blockEntity.setChanged();
    }
}
