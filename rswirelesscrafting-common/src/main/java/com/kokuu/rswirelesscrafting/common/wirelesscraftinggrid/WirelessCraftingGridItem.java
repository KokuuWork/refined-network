package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.DimensionNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.upgrade.Upgrades;

import java.util.List;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import static java.util.Objects.requireNonNullElse;

public class WirelessCraftingGridItem extends AbstractNetworkEnergyItem {

    public WirelessCraftingGridItem(final EnergyItemHelper energyItemHelper,
                                    final NetworkItemHelper networkItemHelper) {
        super(
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
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

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(ContentNames.WIRELESS_CRAFTING_GRID_DESC);
        lines.add(Component.empty());
        lines.add(createBoundLine(stack));
        final List<ItemStack> upgrades = Upgrades.getUpgrades(stack);
        if (!upgrades.isEmpty()) {
            lines.add(createInstalledUpgradesLine(upgrades));
        }
        lines.add(ContentNames.WIRELESS_CRAFTING_GRID_INSTALL_HINT);
    }

    private static Component createBoundLine(final ItemStack stack) {
        final GlobalPos location = stack.get(
            com.refinedmods.refinedstorage.common.content.DataComponents.INSTANCE.getNetworkLocation()
        );
        if (location == null) {
            return ContentNames.WIRELESS_CRAFTING_GRID_UNBOUND;
        }
        return Component.translatable(
            ContentNames.WIRELESS_CRAFTING_GRID_BOUND_TRANSLATION_KEY,
            DimensionNames.get(location.dimension()).withStyle(ChatFormatting.YELLOW),
            location.pos().getX(),
            location.pos().getY(),
            location.pos().getZ()
        ).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static Component createInstalledUpgradesLine(final List<ItemStack> upgrades) {
        final MutableComponent line = ContentNames.WIRELESS_CRAFTING_GRID_UPGRADES.copy();
        for (int i = 0; i < upgrades.size(); i++) {
            if (i > 0) {
                line.append(Component.literal(", "));
            }
            line.append(upgrades.get(i).getHoverName());
        }
        return line;
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
