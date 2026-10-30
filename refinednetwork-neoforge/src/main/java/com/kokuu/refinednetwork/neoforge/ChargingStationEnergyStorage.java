package com.kokuu.refinednetwork.neoforge;

import com.kokuu.refinednetwork.common.chargingstation.ChargingStationBlockEntity;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 把充电站的内部 FE 缓冲暴露成标准 Forge Energy 消费端，
 * 让其他模组的能量方块（线缆、能量立方等）可以直接给它供电。
 */
public record ChargingStationEnergyStorage(ChargingStationBlockEntity blockEntity) implements IEnergyStorage {
    @Override
    public int receiveEnergy(final int toReceive, final boolean simulate) {
        final long accepted = blockEntity.receiveExternalEnergy(toReceive, simulate);
        return (int) Math.min(accepted, Integer.MAX_VALUE);
    }

    @Override
    public int extractEnergy(final int toExtract, final boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(blockEntity.getExternalEnergy(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(blockEntity.getExternalCapacity(), Integer.MAX_VALUE);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
