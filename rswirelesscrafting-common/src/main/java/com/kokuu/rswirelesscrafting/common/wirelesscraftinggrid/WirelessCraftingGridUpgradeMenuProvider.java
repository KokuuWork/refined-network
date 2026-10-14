package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.ContentNames;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class WirelessCraftingGridUpgradeMenuProvider implements ExtendedMenuProvider<UpgradeMenuData> {
    private final SlotReference slotReference;

    public WirelessCraftingGridUpgradeMenuProvider(final SlotReference slotReference) {
        this.slotReference = slotReference;
    }

    @Override
    public UpgradeMenuData getMenuData() {
        return new UpgradeMenuData(slotReference);
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, UpgradeMenuData> getMenuCodec() {
        return UpgradeMenuData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.WIRELESS_CRAFTING_GRID_UPGRADES_TITLE;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId,
                                            final Inventory inventory,
                                            final Player player) {
        return new WirelessCraftingGridUpgradeContainerMenu(syncId, inventory, slotReference);
    }
}
