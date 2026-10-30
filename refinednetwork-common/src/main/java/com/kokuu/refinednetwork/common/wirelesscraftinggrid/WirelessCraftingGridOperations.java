package com.kokuu.refinednetwork.common.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManager;
import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.common.Platform;

class WirelessCraftingGridOperations implements GridOperations {
    private final GridOperations delegate;
    private final ExtendedWirelessNetworkItemContext context;
    private final GridWatcherManager watchers;

    WirelessCraftingGridOperations(final GridOperations delegate,
                                   final ExtendedWirelessNetworkItemContext context,
                                   final GridWatcherManager watchers) {
        this.delegate = delegate;
        this.context = context;
        this.watchers = watchers;
    }

    @Override
    public boolean extract(final ResourceKey resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage destination) {
        final long cost = Platform.INSTANCE.getConfig().getWirelessGrid().getExtractEnergyUsage();
        if (!context.canAfford(cost)) {
            return false;
        }
        final boolean success = delegate.extract(resource, extractMode, destination);
        if (success) {
            drain(cost);
        }
        return success;
    }

    @Override
    public boolean insert(final ResourceKey resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage source) {
        final long cost = Platform.INSTANCE.getConfig().getWirelessGrid().getInsertEnergyUsage();
        if (!context.canAfford(cost)) {
            return false;
        }
        final boolean success = delegate.insert(resource, insertMode, source);
        if (success) {
            drain(cost);
        }
        return success;
    }

    private void drain(final long amount) {
        final boolean wasActive = context.isActive();
        context.drainEnergy(amount);
        final boolean isActive = context.isActive();
        if (wasActive != isActive) {
            watchers.activeChanged(isActive);
        }
    }
}
