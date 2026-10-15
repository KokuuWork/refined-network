package com.kokuu.rswirelesscrafting.common.monitor;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceFactory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 打开监控终端界面时下发的数据：终端所在的槽位引用。
 * 客户端据此从本地背包解析出那件终端（也就是规则列表的载体）。
 */
public record MonitorMenuData(SlotReference slotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MonitorMenuData> STREAM_CODEC =
        StreamCodec.composite(
            SlotReferenceFactory.STREAM_CODEC,
            MonitorMenuData::slotReference,
            MonitorMenuData::new
        );
}
