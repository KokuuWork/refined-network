package com.kokuu.refinednetwork.common;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class ModIdentifierUtil {
    public static final String MOD_ID = "refinednetwork";

    private ModIdentifierUtil() {
    }

    public static ResourceLocation createIdentifier(final String value) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, value);
    }

    public static MutableComponent createTranslation(final String category, final String value) {
        return Component.translatable(createTranslationKey(category, value));
    }

    public static String createTranslationKey(final String category, final String value) {
        return String.format("%s.%s.%s", category, MOD_ID, value);
    }
}
