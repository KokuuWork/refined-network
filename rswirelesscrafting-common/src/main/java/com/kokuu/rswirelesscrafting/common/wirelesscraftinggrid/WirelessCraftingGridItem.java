package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import static java.util.Objects.requireNonNullElse;

/**
 * 无线合成终端：远程访问网络并远程合成。
 *
 * <p>通用的部分（电池容量、绑定 / 距离悬停提示、插件装配界面、距离感知上下文注入）都在
 * {@link AbstractWirelessTerminalItem} 里，这里只负责「点开后打开合成网格」这一件事。
 */
public class WirelessCraftingGridItem extends AbstractWirelessTerminalItem {

    public WirelessCraftingGridItem(final EnergyItemHelper energyItemHelper,
                                    final NetworkItemHelper networkItemHelper) {
        super(
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
            energyItemHelper,
            networkItemHelper
        );
    }

    @Override
    protected Component getTerminalDescriptionLine() {
        return ContentNames.WIRELESS_CRAFTING_GRID_DESC;
    }

    @Override
    protected Component getTerminalName() {
        return ContentNames.WIRELESS_CRAFTING_GRID;
    }

    @Override
    protected void openTerminal(final Component name,
                                final ServerPlayer player,
                                final SlotReference slotReference,
                                final NetworkItemContext context) {
        final boolean isAllowed = context.resolveNetwork()
            .map(network -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, network))
            .orElse(true);
        if (!isAllowed) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, getTerminalName());
            return;
        }
        final ExtendedWirelessNetworkItemContext extendedContext =
            (ExtendedWirelessNetworkItemContext) context;
        // 开界面本身就要钱：付不起就别开，否则界面会在「没注册 watcher」的半开状态下被关掉，
        // RS 的 removeWatcher 会抛异常（断线时甚至崩服）。
        if (!extendedContext.canAfford(Platform.getConfig().getWirelessCraftingGrid().getOpenEnergyUsage())) {
            player.displayClientMessage(ContentNames.TERMINAL_NOT_ENOUGH_ENERGY, false);
            return;
        }
        final CraftingGrid craftingGrid = new WirelessCraftingGrid(player, extendedContext, slotReference);
        final Component correctedName = requireNonNullElse(name, getTerminalName());
        final var provider = new WirelessCraftingGridExtendedMenuProvider(correctedName, craftingGrid, slotReference);
        com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(player, provider);
    }
}
