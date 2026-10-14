package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlock;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    @Nullable
    private Supplier<ChargingStationBlock> terminalChargingStation;

    private Blocks() {
    }

    public ChargingStationBlock getTerminalChargingStation() {
        return requireNonNull(terminalChargingStation).get();
    }

    public void setTerminalChargingStation(final Supplier<ChargingStationBlock> supplier) {
        this.terminalChargingStation = supplier;
    }
}
