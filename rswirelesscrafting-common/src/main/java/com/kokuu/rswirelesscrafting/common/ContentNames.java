package com.kokuu.rswirelesscrafting.common;

import net.minecraft.ChatFormatting;
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

    // Tooltips. These are stored fully styled: MutableComponent#withStyle mutates in place,
    // so styling has to be applied once here rather than at every append site.
    public static final String WIRELESS_CRAFTING_GRID_BOUND_TRANSLATION_KEY = createTranslationKey(
        "item",
        "wireless_crafting_grid.bound"
    );
    public static final MutableComponent WIRELESS_CRAFTING_GRID_DESC = createTranslation(
        "item",
        "wireless_crafting_grid.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_UNBOUND = createTranslation(
        "item",
        "wireless_crafting_grid.unbound"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_UPGRADES = createTranslation(
        "item",
        "wireless_crafting_grid.upgrades"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_INSTALL_HINT = createTranslation(
        "item",
        "wireless_crafting_grid.install_hint"
    ).withStyle(ChatFormatting.DARK_GRAY);

    public static final MutableComponent UPGRADE_INSTALL_HINT = createTranslation(
        "item",
        "upgrade.install_hint"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent RANGE_UPGRADE_DESC = createTranslation(
        "item",
        "range_upgrade.desc"
    ).withStyle(ChatFormatting.AQUA);
    public static final MutableComponent RANGE_UPGRADE_REQUIRES = createTranslation(
        "item",
        "range_upgrade.requires"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent INTERDIMENSIONAL_UPGRADE_DESC = createTranslation(
        "item",
        "interdimensional_upgrade.desc"
    ).withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final MutableComponent INTERDIMENSIONAL_UPGRADE_REQUIRES = createTranslation(
        "item",
        "interdimensional_upgrade.requires"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final String INTERDIMENSIONAL_UPGRADE_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "item",
        "interdimensional_upgrade.energy"
    );

    public static final MutableComponent DIMENSION_OVERWORLD = createTranslation(
        "item",
        "dimension.overworld"
    );
    public static final MutableComponent DIMENSION_NETHER = createTranslation(
        "item",
        "dimension.nether"
    );
    public static final MutableComponent DIMENSION_END = createTranslation(
        "item",
        "dimension.end"
    );

    private ContentNames() {
    }
}
