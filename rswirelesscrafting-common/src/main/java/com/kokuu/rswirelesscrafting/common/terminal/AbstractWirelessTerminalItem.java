package com.kokuu.rswirelesscrafting.common.terminal;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.DimensionNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.upgrade.Upgrades;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.ExtendedWirelessNetworkItemContext;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridUpgradeMenuProvider;

import java.util.List;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 所有「无线终端」的公共父类。
 *
 * <p>这里集中了每件无线终端都要有、且必须完全一致的那部分行为，避免第二、第三件终端各写一份：
 * <ul>
 *   <li><b>能量能力</b>：电池容量 = 配置基础容量 + 已装配电池插件数量 × 单卡加成（装卸即生效）；</li>
 *   <li><b>悬停提示</b>：绑定位置、当前距离与收费倍率、已安装插件、装配提示；</li>
 *   <li><b>插件装配界面</b>：潜行 + 右键打开 4 槽装配界面；</li>
 *   <li><b>统一注入点</b>：3 参 {@code use} 注入距离感知的 {@link ExtendedWirelessNetworkItemContext}。</li>
 * </ul>
 *
 * <p>各终端只需实现「点开时打开什么界面」（{@link #openTerminal}）与两条文案
 * （{@link #getTerminalDescriptionLine}、{@link #getTerminalName}）。
 */
public abstract class AbstractWirelessTerminalItem extends AbstractNetworkEnergyItem {

    protected AbstractWirelessTerminalItem(final Item.Properties properties,
                                          final EnergyItemHelper energyItemHelper,
                                          final NetworkItemHelper networkItemHelper) {
        super(properties, energyItemHelper, networkItemHelper);
    }

    /**
     * 悬停提示里紧跟原版提示的说明行（各终端不同，例如「远程访问网络并远程合成」）。
     */
    protected abstract Component getTerminalDescriptionLine();

    /**
     * 无权限向玩家报错时显示的终端名称。
     */
    protected abstract Component getTerminalName();

    /**
     * 真正打开该终端自己的界面。{@code name} 为玩家给物品自定义的名称（可为 null，表示用默认名）。
     */
    protected abstract void openTerminal(@Nullable Component name,
                                         ServerPlayer player,
                                         SlotReference slotReference,
                                         NetworkItemContext context);

    /**
     * RS 在解析完网络上下文后调用的挂载点。各终端不直接覆写它，而是实现 {@link #openTerminal}，
     * 这样「注入距离感知上下文」这件事只在本基类里发生一次。
     */
    @Override
    protected void use(@Nullable final Component name,
                       final ServerPlayer player,
                       final SlotReference slotReference,
                       final NetworkItemContext context) {
        openTerminal(name, player, slotReference, context);
    }

    /**
     * ★ 核心挂载点：覆写 3 参 use，恒注入自定义 NetworkItemContext，
     * 由 {@link ExtendedWirelessNetworkItemContext} 实现「距离耗电」、
     * 「范围插件 → 同维度无限范围」、「维度插件 → 跨维度走 Network Transmitter/Receiver 隧道」与
     * 「输电插件 → 直接消费所绑定网络里的能量」。
     */
    @Override
    public void use(final ServerPlayer player,
                    final ItemStack stack,
                    final SlotReference slotReference) {
        use(
            stack.get(DataComponents.CUSTOM_NAME),
            player,
            slotReference,
            ExtendedWirelessNetworkItemContext.wrap(stack, player, slotReference)
        );
    }

    /**
     * 潜行 + 右键打开「插件装配」界面（直接插拔插件）；普通右键交给子类打开终端本体界面。
     */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level level,
                                                  final Player player,
                                                  final InteractionHand hand) {
        if (!player.isCrouching()) {
            return super.use(level, player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            openUpgradePanel(serverPlayer, hand);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    /**
     * 电池容量 = 配置基础容量 + 已装配的电池升级卡数量 × 单卡加成。
     *
     * <p>每次查询能量能力都会重新计算，所以装配 / 拆卸电池升级卡后容量立刻生效，
     * 无需重新合成终端。
     */
    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(getEnergyCapacity(stack));
        return RefinedStorageApi.INSTANCE.asItemEnergyStorage(energyStorage, stack);
    }

    public static long getEnergyCapacity(final ItemStack stack) {
        final long base = Platform.getConfig().getWirelessCraftingGrid().getEnergyCapacity();
        final long perUpgrade = Platform.getConfig().getWirelessCraftingGrid().getBatteryUpgradeCapacity();
        return Math.max(1L, base + (long) Upgrades.countBatteryUpgrades(stack) * perUpgrade);
    }

    /**
     * 打开插件装配界面。终端的 3 参 {@code use} 与作用于 RS 原生无线终端的 mixin 共用此处，
     * 保证本模组的终端与 RS 原生终端行为一致。
     */
    public static void openUpgradePanel(final ServerPlayer player, final InteractionHand hand) {
        final SlotReference slotReference =
            RefinedStorageApi.INSTANCE.createInventorySlotReference(player, hand);
        com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
            player,
            new WirelessCraftingGridUpgradeMenuProvider(slotReference)
        );
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(getTerminalDescriptionLine());
        lines.add(Component.empty());
        appendTerminalInfo(stack, lines, Platform.getClientPlayer());
    }

    /**
     * 追加「绑定位置 + 距离倍率 + 已安装插件 + 装配提示」，供本模组各终端与
     * RS 原生无线终端的悬停提示共用（调用方自行决定前面是否补一行空行）。
     */
    public static void appendTerminalInfo(final ItemStack stack,
                                          final List<Component> lines,
                                          @Nullable final Player player) {
        lines.add(createBoundLine(stack));
        final Component distanceLine = createDistanceLine(stack, player);
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
}
