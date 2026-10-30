package com.kokuu.refinednetwork.common;

import com.kokuu.refinednetwork.common.amplifier.AmplifierBlock;
import com.kokuu.refinednetwork.common.bridge.WirelessBridgeBlock;
import com.kokuu.refinednetwork.common.chargingstation.ChargingStationBlock;
import com.kokuu.refinednetwork.common.energymonitor.EnergyMonitorBlock;
import com.kokuu.refinednetwork.common.energyreceiver.WirelessEnergyReceiverBlock;
import com.kokuu.refinednetwork.common.networkbridge.NetworkBridgeBlock;
import com.kokuu.refinednetwork.common.redstonelink.RedstoneLinkBlock;
import com.kokuu.refinednetwork.common.smartsorter.SmartSorterBlock;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    @Nullable
    private Supplier<ChargingStationBlock> terminalChargingStation;
    @Nullable
    private Supplier<AmplifierBlock> wirelessAmplifier;
    @Nullable
    private Supplier<WirelessBridgeBlock> wirelessBridge;
    @Nullable
    private Supplier<WirelessEnergyReceiverBlock> wirelessEnergyReceiver;
    @Nullable
    private Supplier<EnergyMonitorBlock> energyMonitor;
    @Nullable
    private Supplier<SmartSorterBlock> smartSorter;
    @Nullable
    private Supplier<NetworkBridgeBlock> networkBridge;
    @Nullable
    private Supplier<RedstoneLinkBlock> redstoneLink;

    private Blocks() {
    }

    public ChargingStationBlock getTerminalChargingStation() {
        return requireNonNull(terminalChargingStation).get();
    }

    public void setTerminalChargingStation(final Supplier<ChargingStationBlock> supplier) {
        this.terminalChargingStation = supplier;
    }

    public AmplifierBlock getWirelessAmplifier() {
        return requireNonNull(wirelessAmplifier).get();
    }

    public void setWirelessAmplifier(final Supplier<AmplifierBlock> supplier) {
        this.wirelessAmplifier = supplier;
    }

    public WirelessBridgeBlock getWirelessBridge() {
        return requireNonNull(wirelessBridge).get();
    }

    public void setWirelessBridge(final Supplier<WirelessBridgeBlock> supplier) {
        this.wirelessBridge = supplier;
    }

    public WirelessEnergyReceiverBlock getWirelessEnergyReceiver() {
        return requireNonNull(wirelessEnergyReceiver).get();
    }

    public void setWirelessEnergyReceiver(final Supplier<WirelessEnergyReceiverBlock> supplier) {
        this.wirelessEnergyReceiver = supplier;
    }

    public EnergyMonitorBlock getEnergyMonitor() {
        return requireNonNull(energyMonitor).get();
    }

    public void setEnergyMonitor(final Supplier<EnergyMonitorBlock> supplier) {
        this.energyMonitor = supplier;
    }

    public SmartSorterBlock getSmartSorter() {
        return requireNonNull(smartSorter).get();
    }

    public void setSmartSorter(final Supplier<SmartSorterBlock> supplier) {
        this.smartSorter = supplier;
    }

    public NetworkBridgeBlock getNetworkBridge() {
        return requireNonNull(networkBridge).get();
    }

    public void setNetworkBridge(final Supplier<NetworkBridgeBlock> supplier) {
        this.networkBridge = supplier;
    }

    public RedstoneLinkBlock getRedstoneLink() {
        return requireNonNull(redstoneLink).get();
    }

    public void setRedstoneLink(final Supplier<RedstoneLinkBlock> supplier) {
        this.redstoneLink = supplier;
    }
}
