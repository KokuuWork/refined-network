package com.kokuu.rswirelesscrafting.common;

import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void addItems(final Consumer<ItemStack> consumer) {
        consumer.accept(Items.INSTANCE.getWirelessCraftingGrid().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getWirelessPatternGrid().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getWirelessAutocrafterManager().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getBatteryUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getRangeUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getInterdimensionalUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getRemoteEnergyUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getEnergyUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getAdvancedEnergyUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getTerminalChargingStationBlockItem().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getWirelessAmplifierBlockItem().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getWirelessBridgeBlockItem().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getWirelessConfigurationCard().getDefaultInstance());
    }
}
