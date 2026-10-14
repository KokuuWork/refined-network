package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.upgrade.Upgrades;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static java.util.Objects.requireNonNullElse;

public class WirelessCraftingGridItem extends AbstractNetworkEnergyItem {

    public WirelessCraftingGridItem(final EnergyItemHelper energyItemHelper,
                                    final NetworkItemHelper networkItemHelper) {
        super(
            new Item.Properties().stacksTo(1),
            energyItemHelper,
            networkItemHelper
        );
    }

    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Math.clamp(
                Platform.getConfig().getWirelessCraftingGrid().getEnergyCapacity(),
                1,
                Long.MAX_VALUE
            )
        );
        return RefinedStorageApi.INSTANCE.asItemEnergyStorage(energyStorage, stack);
    }

    /**
     * ★ 核心挂载点：覆写 3 参 use，恒注入自定义 NetworkItemContext，
     * 由 ExtendedWirelessNetworkItemContext 实现「范围卡 → 同维度无限范围」与
     * 「维度卡 → 跨维度走 Network Transmitter/Receiver 隧道」。
     */
    @Override
    public void use(final ServerPlayer player,
                    final ItemStack stack,
                    final SlotReference slotReference) {
        final NetworkItemContext context = new ExtendedWirelessNetworkItemContext(
            stack,
            player,
            slotReference,
            Upgrades.hasRangeUpgrade(stack),
            Upgrades.hasInterdimensionalUpgrade(stack)
        );
        use(stack.get(DataComponents.CUSTOM_NAME), player, slotReference, context);
    }

    @Override
    protected void use(@Nullable final Component name,
                       final ServerPlayer player,
                       final SlotReference slotReference,
                       final NetworkItemContext context) {
        final boolean isAllowed = context.resolveNetwork()
            .map(network -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, network))
            .orElse(true);
        if (!isAllowed) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.WIRELESS_CRAFTING_GRID);
            return;
        }
        final CraftingGrid craftingGrid = new WirelessCraftingGrid(player, context, slotReference);
        final Component correctedName = requireNonNullElse(name, ContentNames.WIRELESS_CRAFTING_GRID);
        final var provider = new WirelessCraftingGridExtendedMenuProvider(correctedName, craftingGrid, slotReference);
        com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(player, provider);
    }
}
