package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.upgrade.InterdimensionalUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.RangeUpgradeItem;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridItem;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Items {
    public static final Items INSTANCE = new Items();

    @Nullable
    private Supplier<WirelessCraftingGridItem> wirelessCraftingGrid;
    @Nullable
    private Supplier<RangeUpgradeItem> rangeUpgrade;
    @Nullable
    private Supplier<InterdimensionalUpgradeItem> interdimensionalUpgrade;

    private Items() {
    }

    public WirelessCraftingGridItem getWirelessCraftingGrid() {
        return requireNonNull(wirelessCraftingGrid).get();
    }

    public void setWirelessCraftingGrid(final Supplier<WirelessCraftingGridItem> supplier) {
        this.wirelessCraftingGrid = supplier;
    }

    public RangeUpgradeItem getRangeUpgrade() {
        return requireNonNull(rangeUpgrade).get();
    }

    public void setRangeUpgrade(final Supplier<RangeUpgradeItem> supplier) {
        this.rangeUpgrade = supplier;
    }

    public InterdimensionalUpgradeItem getInterdimensionalUpgrade() {
        return requireNonNull(interdimensionalUpgrade).get();
    }

    public void setInterdimensionalUpgrade(final Supplier<InterdimensionalUpgradeItem> supplier) {
        this.interdimensionalUpgrade = supplier;
    }
}
