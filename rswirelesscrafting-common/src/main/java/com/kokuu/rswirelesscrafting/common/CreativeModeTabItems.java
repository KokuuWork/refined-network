package com.kokuu.rswirelesscrafting.common;

import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void addItems(final Consumer<ItemStack> consumer) {
        consumer.accept(Items.INSTANCE.getWirelessCraftingGrid().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getBatteryUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getRangeUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getInterdimensionalUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getRemoteEnergyUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getAirCooledChargingStationBlockItem().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getLiquidCooledChargingStationBlockItem().getDefaultInstance());
    }
}
