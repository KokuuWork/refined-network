package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlock;
import com.kokuu.rswirelesscrafting.common.bridge.WirelessBridgeBlock;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlock;

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
}
