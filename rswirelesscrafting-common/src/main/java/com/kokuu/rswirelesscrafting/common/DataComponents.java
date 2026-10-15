package com.kokuu.rswirelesscrafting.common;

import com.kokuu.rswirelesscrafting.common.monitor.MonitorRule;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridState;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import static java.util.Objects.requireNonNull;

public final class DataComponents {
    public static final DataComponents INSTANCE = new DataComponents();

    @Nullable
    private Supplier<DataComponentType<WirelessCraftingGridState>> wirelessCraftingGridState;
    @Nullable
    private Supplier<DataComponentType<List<ItemStack>>> upgrades;
    @Nullable
    private Supplier<DataComponentType<CompoundTag>> patternGridState;
    @Nullable
    private Supplier<DataComponentType<List<MonitorRule>>> monitorRules;

    private DataComponents() {
    }

    public DataComponentType<WirelessCraftingGridState> getWirelessCraftingGridState() {
        return requireNonNull(wirelessCraftingGridState).get();
    }

    public void setWirelessCraftingGridState(
        @Nullable final Supplier<DataComponentType<WirelessCraftingGridState>> supplier
    ) {
        this.wirelessCraftingGridState = supplier;
    }

    public DataComponentType<CompoundTag> getPatternGridState() {
        return requireNonNull(patternGridState).get();
    }

    public void setPatternGridState(
        @Nullable final Supplier<DataComponentType<CompoundTag>> supplier
    ) {
        this.patternGridState = supplier;
    }

    public DataComponentType<List<MonitorRule>> getMonitorRules() {
        return requireNonNull(monitorRules).get();
    }

    public void setMonitorRules(
        @Nullable final Supplier<DataComponentType<List<MonitorRule>>> supplier
    ) {
        this.monitorRules = supplier;
    }

    public DataComponentType<List<ItemStack>> getUpgrades() {
        return requireNonNull(upgrades).get();
    }

    public void setUpgrades(
        @Nullable final Supplier<DataComponentType<List<ItemStack>>> supplier
    ) {
        this.upgrades = supplier;
    }
}
