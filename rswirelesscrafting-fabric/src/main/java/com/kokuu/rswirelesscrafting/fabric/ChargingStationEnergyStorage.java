package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/**
 * 把充电站的内部 FE 缓冲暴露成 Team Reborn Energy 的消费端。
 * 与无线能量接收器共用事务语义：回滚不能把输入的能量留在缓冲里。
 */
public final class ChargingStationEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    private final ChargingStationBlockEntity blockEntity;

    public ChargingStationEnergyStorage(final ChargingStationBlockEntity blockEntity) {
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
        return blockEntity.getExternalEnergy();
    }

    @Override
    public long getCapacity() {
        return blockEntity.getExternalCapacity();
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    protected Long createSnapshot() {
        return blockEntity.getExternalEnergy();
    }

    @Override
    protected void readSnapshot(final Long snapshot) {
        blockEntity.restoreExternalEnergy(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        blockEntity.setChanged();
    }
}
