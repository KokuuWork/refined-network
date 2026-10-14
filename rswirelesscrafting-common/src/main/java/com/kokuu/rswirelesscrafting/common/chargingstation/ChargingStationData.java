package com.kokuu.rswirelesscrafting.common.chargingstation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 打开充电站界面时从服务端发往客户端的附加数据：方块坐标 + 档位。
 * 档位决定客户端画哪一张背景（风冷 / 液冷）。
 */
public record ChargingStationData(BlockPos pos, int tier) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargingStationData> STREAM_CODEC =
        StreamCodec.of(
            (buffer, data) -> {
                buffer.writeBlockPos(data.pos());
                buffer.writeVarInt(data.tier());
            },
            buffer -> new ChargingStationData(buffer.readBlockPos(), buffer.readVarInt())
        );
}
