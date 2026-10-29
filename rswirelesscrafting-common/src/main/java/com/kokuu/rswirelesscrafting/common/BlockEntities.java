package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlockEntity;
import com.kokuu.rswirelesscrafting.common.bridge.WirelessBridgeBlockEntity;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockEntity;
import com.kokuu.rswirelesscrafting.common.energymonitor.EnergyMonitorBlockEntity;
import com.kokuu.rswirelesscrafting.common.energyreceiver.WirelessEnergyReceiverBlockEntity;
import com.kokuu.rswirelesscrafting.common.networkbridge.NetworkBridgeBlockEntity;
import com.kokuu.rswirelesscrafting.common.redstonelink.RedstoneLinkBlockEntity;
import com.kokuu.rswirelesscrafting.common.smartsorter.SmartSorterBlockEntity;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntityType;

import static java.util.Objects.requireNonNull;

public final class BlockEntities {
    public static final BlockEntities INSTANCE = new BlockEntities();

    @Nullable
    private Supplier<BlockEntityType<ChargingStationBlockEntity>> chargingStation;
    @Nullable
    private Supplier<BlockEntityType<AmplifierBlockEntity>> wirelessAmplifier;
    @Nullable
    private Supplier<BlockEntityType<WirelessBridgeBlockEntity>> wirelessBridge;
    @Nullable
    private Supplier<BlockEntityType<WirelessEnergyReceiverBlockEntity>> wirelessEnergyReceiver;
    @Nullable
    private Supplier<BlockEntityType<EnergyMonitorBlockEntity>> energyMonitor;
    @Nullable
    private Supplier<BlockEntityType<SmartSorterBlockEntity>> smartSorter;
    @Nullable
    private Supplier<BlockEntityType<NetworkBridgeBlockEntity>> networkBridge;
    private Supplier<BlockEntityType<RedstoneLinkBlockEntity>> redstoneLink;

    private BlockEntities() {
    }

    public BlockEntityType<ChargingStationBlockEntity> getChargingStation() {
        return requireNonNull(chargingStation).get();
    }

    public void setChargingStation(final Supplier<BlockEntityType<ChargingStationBlockEntity>> supplier) {
        this.chargingStation = supplier;
    }

    public BlockEntityType<AmplifierBlockEntity> getWirelessAmplifier() {
        return requireNonNull(wirelessAmplifier).get();
    }

    public void setWirelessAmplifier(final Supplier<BlockEntityType<AmplifierBlockEntity>> supplier) {
        this.wirelessAmplifier = supplier;
    }

    public BlockEntityType<WirelessBridgeBlockEntity> getWirelessBridge() {
        return requireNonNull(wirelessBridge).get();
    }

    public void setWirelessBridge(final Supplier<BlockEntityType<WirelessBridgeBlockEntity>> supplier) {
        this.wirelessBridge = supplier;
    }

    public BlockEntityType<WirelessEnergyReceiverBlockEntity> getWirelessEnergyReceiver() {
        return requireNonNull(wirelessEnergyReceiver).get();
    }

    public void setWirelessEnergyReceiver(final Supplier<BlockEntityType<WirelessEnergyReceiverBlockEntity>> supplier) {
        this.wirelessEnergyReceiver = supplier;
    }

    public BlockEntityType<EnergyMonitorBlockEntity> getEnergyMonitor() {
        return requireNonNull(energyMonitor).get();
    }

    public void setEnergyMonitor(final Supplier<BlockEntityType<EnergyMonitorBlockEntity>> supplier) {
        this.energyMonitor = supplier;
    }

    public BlockEntityType<SmartSorterBlockEntity> getSmartSorter() {
        return requireNonNull(smartSorter).get();
    }

    public void setSmartSorter(final Supplier<BlockEntityType<SmartSorterBlockEntity>> supplier) {
        this.smartSorter = supplier;
    }

    public BlockEntityType<NetworkBridgeBlockEntity> getNetworkBridge() {
        return requireNonNull(networkBridge).get();
    }

    public void setNetworkBridge(final Supplier<BlockEntityType<NetworkBridgeBlockEntity>> supplier) {
        this.networkBridge = supplier;
    }

    public BlockEntityType<RedstoneLinkBlockEntity> getRedstoneLink() {
        return requireNonNull(redstoneLink).get();
    }

    public void setRedstoneLink(final Supplier<BlockEntityType<RedstoneLinkBlockEntity>> supplier) {
        this.redstoneLink = supplier;
    }
}
