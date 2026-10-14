package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlock;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    @Nullable
    private Supplier<ChargingStationBlock> chargingStation;

    private Blocks() {
    }

    public ChargingStationBlock getChargingStation() {
        return requireNonNull(chargingStation).get();
    }

    public void setChargingStation(final Supplier<ChargingStationBlock> supplier) {
        this.chargingStation = supplier;
    }
}
