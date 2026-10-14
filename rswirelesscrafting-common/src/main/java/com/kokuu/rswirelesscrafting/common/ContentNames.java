package com.kokuu.rswirelesscrafting.common;

import net.minecraft.network.chat.MutableComponent;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.MOD_ID;
import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createTranslation;
import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createTranslationKey;

public final class ContentNames {
    public static final String MOD_TRANSLATION_KEY = "mod." + MOD_ID;
    public static final String OPEN_WIRELESS_CRAFTING_GRID_TRANSLATION_KEY = createTranslationKey(
        "key",
        "open_wireless_crafting_grid"
    );
    public static final MutableComponent WIRELESS_CRAFTING_GRID = createTranslation(
        "item",
        "wireless_crafting_grid"
    );
    public static final MutableComponent RANGE_UPGRADE = createTranslation(
        "item",
        "range_upgrade"
    );
    public static final MutableComponent INTERDIMENSIONAL_UPGRADE = createTranslation(
        "item",
        "interdimensional_upgrade"
    );

    private ContentNames() {
    }
}
