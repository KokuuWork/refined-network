package com.kokuu.rswirelesscrafting.common.chargingstation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 打开充电站界面时从服务端发往客户端的附加数据（方块坐标）。
 */
public record ChargingStationData(BlockPos pos) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargingStationData> STREAM_CODEC =
        StreamCodec.of(
            (buffer, data) -> buffer.writeBlockPos(data.pos()),
            buffer -> new ChargingStationData(buffer.readBlockPos())
        );
}
