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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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

    /**
     * 电池容量 = 基础容量 + 已装配的电池升级卡数量 × 单卡加成。
     *
     * <p>每次查询能量能力都会重新计算，所以装配 / 拆卸电池升级卡后容量立刻生效，
     * 无需重新合成终端。
     */
    public static EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(getEnergyCapacity(stack));
        return RefinedStorageApi.INSTANCE.asItemEnergyStorage(energyStorage, stack);
    }

    public static long getEnergyCapacity(final ItemStack stack) {
        final long base = Platform.getConfig().getWirelessCraftingGrid().getEnergyCapacity();
        final long perUpgrade = Platform.getConfig().getWirelessCraftingGrid().getBatteryUpgradeCapacity();
        return Math.max(1L, base + (long) Upgrades.countBatteryUpgrades(stack) * perUpgrade);
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
        final Component distanceLine = createDistanceLine(stack, Platform.getClientPlayer());
        if (distanceLine != null) {
            lines.add(distanceLine);
        }
        final List<ItemStack> upgrades = Upgrades.getUpgrades(stack);
        if (!upgrades.isEmpty()) {
            lines.add(createInstalledUpgradesLine(upgrades));
        }
        lines.add(ContentNames.WIRELESS_CRAFTING_GRID_INSTALL_HINT);
    }

    public static Component createBoundLine(final ItemStack stack) {
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

    public static Component createInstalledUpgradesLine(final List<ItemStack> upgrades) {
        final MutableComponent line = ContentNames.WIRELESS_CRAFTING_GRID_UPGRADES.copy();
        for (final ItemStack upgrade : upgrades) {
            line.append(Component.literal("\n  · "));
            line.append(upgrade.getHoverName().copy().withStyle(Upgrades.getColor(upgrade)));
        }
        return line;
    }

    /**
     * 生成一行「距离 + 收费倍率」提示（实时，仅客户端有玩家上下文时）。
     * 未绑定或无客户端玩家时返回 {@code null}（不显示），避免在服务端里报错或冗余。
     * 倍率已包含距离 / 跨维度 / 能耗插件三者的叠加，与实际扣费完全一致。
     */
    @Nullable
    public static Component createDistanceLine(final ItemStack stack, @Nullable final Player player) {
        if (player == null) {
            return null;
        }
        final GlobalPos location = stack.get(
            com.refinedmods.refinedstorage.common.content.DataComponents.INSTANCE.getNetworkLocation()
        );
        if (location == null) {
            return null;
        }
        final long multiplierPercent = ExtendedWirelessNetworkItemContext.distanceMultiplierPercent(stack, player);
        if (!location.dimension().equals(player.level().dimension())) {
            return Component.translatable(
                ContentNames.WIRELESS_CRAFTING_GRID_DISTANCE_INTERDIMENSIONAL_TRANSLATION_KEY,
                formatMultiplier(multiplierPercent)
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        final double distance = ExtendedWirelessNetworkItemContext.distanceToBound(stack, player);
        return Component.translatable(
            ContentNames.WIRELESS_CRAFTING_GRID_DISTANCE_TRANSLATION_KEY,
            Math.round(distance),
            formatMultiplier(multiplierPercent)
        ).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static String formatMultiplier(final long percent) {
        if (percent % 100 == 0) {
            return Long.toString(percent / 100);
        }
        return Double.toString(percent / 100.0);
    }

    /**
     * ★ 核心挂载点：覆写 3 参 use，恒注入自定义 NetworkItemContext，
     * 由 ExtendedWirelessNetworkItemContext 实现「距离耗电」、
     * 「范围插件 → 同维度无限范围」、「维度插件 → 跨维度走 Network Transmitter/Receiver 隧道」与
     * 「输电插件 → 直接消费所绑定网络里的能量」。
     */
    @Override
    public void use(final ServerPlayer player,
                    final ItemStack stack,
                    final SlotReference slotReference) {
        final ExtendedWirelessNetworkItemContext context =
            ExtendedWirelessNetworkItemContext.wrap(stack, player, slotReference);
        use(stack.get(DataComponents.CUSTOM_NAME), player, slotReference, context);
    }

    /**
     * 潜行 + 右键打开「升级装配」界面（直接插拔升级卡）；普通右键照旧打开合成界面。
     */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level level,
                                                  final Player player,
                                                  final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (player instanceof ServerPlayer serverPlayer) {
                final SlotReference slotReference =
                    RefinedStorageApi.INSTANCE.createInventorySlotReference(player, hand);
                com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
                    serverPlayer,
                    new WirelessCraftingGridUpgradeMenuProvider(slotReference)
                );
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
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
        final ExtendedWirelessNetworkItemContext extendedContext =
            (ExtendedWirelessNetworkItemContext) context;
        // 开界面本身就要钱：付不起就别开，否则界面会在「没注册 watcher」的半开状态下被关掉，
        // RS 的 removeWatcher 会抛异常（断线时甚至崩服）。
        if (!extendedContext.canAfford(Platform.getConfig().getWirelessCraftingGrid().getOpenEnergyUsage())) {
            player.displayClientMessage(ContentNames.TERMINAL_NOT_ENOUGH_ENERGY, false);
            return;
        }
        final CraftingGrid craftingGrid = new WirelessCraftingGrid(player, extendedContext, slotReference);
        final Component correctedName = requireNonNullElse(name, ContentNames.WIRELESS_CRAFTING_GRID);
        final var provider = new WirelessCraftingGridExtendedMenuProvider(correctedName, craftingGrid, slotReference);
        com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(player, provider);
    }
}
