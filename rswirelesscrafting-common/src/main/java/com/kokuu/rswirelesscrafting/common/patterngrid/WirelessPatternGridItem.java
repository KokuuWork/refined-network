package com.kokuu.rswirelesscrafting.common.patterngrid;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;

import java.util.Optional;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import static java.util.Objects.requireNonNullElse;

/**
 * 无线样板终端：远程配置自动化样板，不用再跑回基地的样板网格。
 *
 * <p>通用行为（电池容量、绑定 / 距离悬停提示、插件装配界面、距离感知上下文）继承自
 * {@link AbstractWirelessTerminalItem}；这里只负责「拿终端物品 + 绑定网络，交给
 * {@link VirtualPatternGrid} 与 RS 原版样板网格菜单」。
 */
public class WirelessPatternGridItem extends AbstractWirelessTerminalItem {

    public WirelessPatternGridItem(final EnergyItemHelper energyItemHelper,
                                   final NetworkItemHelper networkItemHelper) {
        super(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
            energyItemHelper,
            networkItemHelper
        );
    }

    @Override
    protected Component getTerminalDescriptionLine() {
        return ContentNames.WIRELESS_PATTERN_GRID_DESC;
    }

    @Override
    protected Component getTerminalName() {
        return ContentNames.WIRELESS_PATTERN_GRID;
    }

    @Override
    protected void openTerminal(@Nullable final Component name,
                                final ServerPlayer player,
                                final SlotReference slotReference,
                                final NetworkItemContext context) {
        final Optional<ItemStack> resolved = slotReference.resolve(player);
        if (resolved.isEmpty() || resolved.get().isEmpty()) {
            return;
        }
        final Optional<Network> network = context.resolveNetwork();
        final boolean isAllowed = network
            .map(found -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, found))
            .orElse(true);
        if (!isAllowed) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, getTerminalName());
            return;
        }
        // 开界面本身就要钱：与无线合成终端同口径，按距离倍率结算。
        context.drainEnergy(Platform.getConfig().getWirelessCraftingGrid().getOpenEnergyUsage());
        final VirtualPatternGrid grid = new VirtualPatternGrid(
            resolved.get(),
            player.level(),
            network.orElse(null),
            new WirelessPatternGridSession(resolved.get(), context)
        );
        com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
            player,
            new WirelessPatternGridMenuProvider(grid, requireNonNullElse(name, getTerminalName()))
        );
    }
}
