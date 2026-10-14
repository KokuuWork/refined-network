package com.kokuu.rswirelesscrafting.common;

import net.minecraft.resources.ResourceLocation;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createIdentifier;

public final class ContentIds {
    public static final ResourceLocation WIRELESS_CRAFTING_GRID = createIdentifier("wireless_crafting_grid");
    public static final ResourceLocation RANGE_UPGRADE = createIdentifier("range_upgrade");
    public static final ResourceLocation INTERDIMENSIONAL_UPGRADE = createIdentifier("interdimensional_upgrade");
    public static final ResourceLocation UPGRADES = createIdentifier("upgrades");

    private ContentIds() {
    }
}
