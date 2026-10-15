package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlockEntity;
import com.kokuu.rswirelesscrafting.common.bridge.WirelessBridgeBlockEntity;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockEntity;

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
}
