package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.Platform;

import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.grid.ExtractTransaction;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.world.entity.player.Player;

class WirelessCraftingGridExtractTransaction implements ExtractTransaction {
    private final NetworkItemContext context;
    private final ExtractTransaction delegate;

    WirelessCraftingGridExtractTransaction(final NetworkItemContext context, final ExtractTransaction delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    public boolean extract(final ItemResource itemResource, final Player player) {
        return delegate.extract(itemResource, player);
    }

    @Override
    public void close() {
        delegate.close();
        context.drainEnergy(Platform.getConfig().getWirelessCraftingGrid().getCraftingEnergyUsage());
    }
}
