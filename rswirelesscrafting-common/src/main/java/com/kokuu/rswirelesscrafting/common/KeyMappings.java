package com.kokuu.rswirelesscrafting.common;

import javax.annotation.Nullable;

import net.minecraft.client.KeyMapping;

public final class KeyMappings {
    public static final KeyMappings INSTANCE = new KeyMappings();

    @Nullable
    private KeyMapping openWirelessCraftingGrid;

    private KeyMappings() {
    }

    @Nullable
    public KeyMapping getOpenWirelessCraftingGrid() {
        return openWirelessCraftingGrid;
    }

    public void setOpenWirelessCraftingGrid(@Nullable final KeyMapping openWirelessCraftingGrid) {
        this.openWirelessCraftingGrid = openWirelessCraftingGrid;
    }
}
