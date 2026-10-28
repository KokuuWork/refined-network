package com.kokuu.rswirelesscrafting.neoforge;

import com.kokuu.rswirelesscrafting.common.energyreceiver.WirelessEnergyReceiverBlockEntity;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 把无线能量接收器的 FE 缓冲暴露成标准 Forge Energy 消费端，
 * 让其他模组的能量方块（线缆、能量立方等）可以直接给它供电。
 */
public record WirelessEnergyReceiverEnergyStorage(WirelessEnergyReceiverBlockEntity blockEntity)
    implements IEnergyStorage {
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
        return (int) Math.min(blockEntity.getBuffer(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(blockEntity.getCapacity(), Integer.MAX_VALUE);
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
