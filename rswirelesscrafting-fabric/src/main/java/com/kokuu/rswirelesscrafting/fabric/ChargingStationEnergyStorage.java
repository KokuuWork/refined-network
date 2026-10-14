package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

/**
 * 把充电站的内部 FE 缓冲暴露成 Team Reborn Energy 的消费端，
 * 让其他模组的能量方块（线缆、能量立方等）可以直接给它供电。
 */
public record ChargingStationEnergyStorage(ChargingStationBlockEntity blockEntity) implements EnergyStorage {
    @Override
    public long insert(final long maxAmount, final TransactionContext transaction) {
        return blockEntity.receiveExternalEnergy(maxAmount, false);
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
}
