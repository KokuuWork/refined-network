package com.kokuu.refinednetwork.common.link;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * 方块实体的「绑定位置」存取。
 *
 * <p>无线桥、无线能量接收器、网络桥接器都要记住「我指着哪个坐标」，用的是同一套
 * {@link GlobalPos}（维度 + 坐标）语义，所以 NBT 的键名与解析逻辑只写这一份。
 */
public final class BoundPosition {
    /**
     * 旧存档里无线桥用的键名，保持一致以免已放下的桥解绑。
     */
    public static final String TAG_TARGET_DIMENSION = "TargetDimension";
    public static final String TAG_TARGET_POS = "TargetPos";

    private BoundPosition() {
    }

    public static void save(@Nullable final CompoundTag tag, @Nullable final GlobalPos target) {
        if (target == null) {
            if (tag != null) {
                tag.remove(TAG_TARGET_DIMENSION);
                tag.remove(TAG_TARGET_POS);
            }
            return;
        }
        tag.putString(TAG_TARGET_DIMENSION, target.dimension().location().toString());
        tag.putLong(TAG_TARGET_POS, target.pos().asLong());
    }

    @Nullable
    public static GlobalPos load(final CompoundTag tag) {
        final String dimension = tag.getString(TAG_TARGET_DIMENSION);
        if (dimension.isEmpty() || !tag.contains(TAG_TARGET_POS)) {
            return null;
        }
        final ResourceLocation location = ResourceLocation.tryParse(dimension);
        if (location == null) {
            return null;
        }
        return GlobalPos.of(
            ResourceKey.create(Registries.DIMENSION, location),
            BlockPos.of(tag.getLong(TAG_TARGET_POS))
        );
    }
}
