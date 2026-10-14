package com.kokuu.rswirelesscrafting.common;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Display name for a dimension, mirroring how Refined Storage labels network locations.
 * The vanilla dimensions get translated names; anything else falls back to the
 * standard {@code dimension.<namespace>.<path>} translation key.
 */
public final class DimensionNames {
    private DimensionNames() {
    }

    public static MutableComponent get(final ResourceKey<Level> dimension) {
        if (dimension == Level.OVERWORLD) {
            return ContentNames.DIMENSION_OVERWORLD.copy();
        }
        if (dimension == Level.NETHER) {
            return ContentNames.DIMENSION_NETHER.copy();
        }
        if (dimension == Level.END) {
            return ContentNames.DIMENSION_END.copy();
        }
        return Component.translatable(
            "dimension." + dimension.location().getNamespace() + "." + dimension.location().getPath()
        );
    }
}
