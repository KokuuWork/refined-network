package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceFactory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 打开无线合成终端的升级装配界面时下发的数据：终端所在的槽位引用。
 */
public record UpgradeMenuData(SlotReference slotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeMenuData> STREAM_CODEC =
        StreamCodec.composite(
            SlotReferenceFactory.STREAM_CODEC,
            UpgradeMenuData::slotReference,
            UpgradeMenuData::new
        );
}
