package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlock;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    @Nullable
    private Supplier<ChargingStationBlock> airCooledChargingStation;
    @Nullable
    private Supplier<ChargingStationBlock> liquidCooledChargingStation;

    private Blocks() {
    }

    public ChargingStationBlock getAirCooledChargingStation() {
        return requireNonNull(airCooledChargingStation).get();
    }

    public void setAirCooledChargingStation(final Supplier<ChargingStationBlock> supplier) {
        this.airCooledChargingStation = supplier;
    }

    public ChargingStationBlock getLiquidCooledChargingStation() {
        return requireNonNull(liquidCooledChargingStation).get();
    }

    public void setLiquidCooledChargingStation(final Supplier<ChargingStationBlock> supplier) {
        this.liquidCooledChargingStation = supplier;
    }
}
