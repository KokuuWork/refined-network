package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.upgrade.Upgrades;

import java.util.Optional;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.networking.NetworkReceiverBlockEntity;
import com.refinedmods.refinedstorage.common.networking.WirelessTransmitterBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 本 mod 的核心扩展：在 RS 标准无线网络上下文之上，实现三种能力——
 *
 * <ul>
 *   <li><b>距离耗电</b>：无线终端离绑定网络越远，每次动作消耗的能量越高
 *       （分段 ×1 / ×2 / ×4 / ×8，跨维度 ×1.5；初级能耗插件减半、高级能耗插件归 1）；</li>
 *   <li><b>卡片放宽</b>：范围插件（同维无限）、维度插件（跨维隧道）绕过 RS 原生距离/维度校验；</li>
 *   <li><b>远程输电</b>：远程输电插件直接从所绑定网络抽电，而不是扣物品电池。</li>
 * </ul>
 *
 * <p>RS 原生的距离/维度校验位于 {@code NetworkItemPlayerValidator}（由 Wireless Transmitter 注册），
 * 而非 {@code resolveNetwork} 本身。因此本实现采用「标准逻辑优先、卡片兜底放宽」策略：
 * <ul>
 *   <li>范围内（同维度 + 距离 OK）→ 完全走 RS 标准逻辑，零风险；</li>
 *   <li>超距离但有<b>范围插件</b> → 跳过距离校验，直接取 Wireless Transmitter 暴露的网络；</li>
 *   <li>跨维度且有<b>维度插件</b> → 跳过维度校验，直接取 Network Receiver 暴露的家庭网络（隧道）。</li>
 * </ul>
 *
 * <p>注意：{@code force=true} 时无条件委托 RS 标准实现，因为 RS 用 force 语义
 * 在玩家走远后仍能移除 grid watcher，不能被卡片逻辑改变。
 *
 * <p>{@link #wrap} 是统一的入口：从物品的 {@code upgrades} 数据组件读取全部插件，
 * 供本 mod 的合成终端与 mixin 接入的 RS 本体无线终端（无线终端 / 无线合成监视器）共用。
 */
public class ExtendedWirelessNetworkItemContext implements NetworkItemContext {
    private final ItemStack stack;
    private final ServerPlayer player;
    private final SlotReference slotReference;
    private final boolean rangeUpgrade;
    private final boolean interdimensional;
    private final boolean remoteEnergy;
    private final boolean basicEnergy;
    private final boolean advancedEnergy;
    private final NetworkItemContext standard;

    public ExtendedWirelessNetworkItemContext(final ItemStack stack,
                                              final ServerPlayer player,
                                              final SlotReference slotReference,
                                              final boolean rangeUpgrade,
                                              final boolean interdimensional,
                                              final boolean remoteEnergy,
                                              final boolean basicEnergy,
                                              final boolean advancedEnergy) {
        this.stack = stack;
        this.player = player;
        this.slotReference = slotReference;
        this.rangeUpgrade = rangeUpgrade;
        this.interdimensional = interdimensional;
        this.remoteEnergy = remoteEnergy;
        this.basicEnergy = basicEnergy;
        this.advancedEnergy = advancedEnergy;
        this.standard = RefinedStorageApi.INSTANCE
            .getNetworkItemHelper()
            .createContext(stack, player, slotReference);
    }

    /**
     * 统一工厂：读取物品上已装配的全部插件，构造一个距离感知 + 卡片放宽的上下文。
     */
    public static NetworkItemContext wrap(final ItemStack stack,
                                          final ServerPlayer player,
                                          final SlotReference slotReference) {
        return new ExtendedWirelessNetworkItemContext(
            stack,
            player,
            slotReference,
            Upgrades.hasRangeUpgrade(stack),
            Upgrades.hasInterdimensionalUpgrade(stack),
            Upgrades.hasRemoteEnergyUpgrade(stack),
            Upgrades.hasEnergyUpgrade(stack),
            Upgrades.hasAdvancedEnergyUpgrade(stack)
        );
    }

    @Override
    public Optional<Network> resolveNetwork(final boolean force) {
        // 1) 标准逻辑优先：覆盖「同维度 + 发射器范围内」的正常情况。
        //    force=true 时标准实现会跳过全部校验直接返回网络，此处同样适用。
        final Optional<Network> standardResult = standard.resolveNetwork(force);
        if (standardResult.isPresent()) {
            return standardResult;
        }
        // 2) force 时标准已尽力（方块不存在/未绑定），不再尝试卡片放宽。
        if (force) {
            return Optional.empty();
        }
        // 3) 卡片放宽：定位绑定方块，按类型 + 卡片配置决定是否放行。
        return resolveNetworkWithUpgrades();
    }

    private Optional<Network> resolveNetworkWithUpgrades() {
        final GlobalPos location = stack.get(DataComponents.INSTANCE.getNetworkLocation());
        if (location == null) {
            return Optional.empty();
        }
        final ServerLevel level = player.getServer().getLevel(location.dimension());
        final BlockEntity blockEntity = level == null ? null : level.getBlockEntity(location.pos());
        // 维度插件：绑 Network Receiver → 走 Transmitter/Receiver 隧道（跨维度）
        // 范围插件：绑 Wireless Transmitter → 无视距离（同维度无限范围）
        if (blockEntity instanceof NetworkItemTargetBlockEntity target && isTargetAllowedByUpgrades(blockEntity)) {
            return Optional.ofNullable(target.getNetworkForItem());
        }
        return Optional.empty();
    }

    private boolean isTargetAllowedByUpgrades(final BlockEntity blockEntity) {
        if (blockEntity instanceof NetworkReceiverBlockEntity) {
            return interdimensional;
        }
        return blockEntity instanceof WirelessTransmitterBlockEntity && rangeUpgrade;
    }

    /**
     * 装了输电插件时，「是否有电」由所绑定网络决定，而不是物品自身电池。
     */
    @Override
    public boolean isActive() {
        if (!remoteEnergy) {
            return standard.isActive();
        }
        return resolveNetwork()
            .map(network -> !RefinedStorageApi.INSTANCE.isEnergyRequired()
                || network.getComponent(EnergyNetworkComponent.class).getStored() > 0)
            .orElse(false);
    }

    @Override
    public void drainEnergy(final long amount) {
        final long cost = Math.max(1L, amount * distanceMultiplierPercent() / 100);
        if (remoteEnergy && payFromNetwork(cost)) {
            return;
        }
        standard.drainEnergy(cost);
    }

    /**
     * 计算「距离 + 维度 + 能耗插件」叠加后的能量倍率（百分比整数）。
     *
     * <ul>
     *   <li>跨维度 → 直接套 {@code interdimensionalMultiplier}，距离失去意义；</li>
     *   <li>同维度 → 按水平距离落入 10 / 100 / 1000 格分段，取 ×1 / ×2 / ×4 / ×8；</li>
     *   <li>初级能耗插件 → 距离倍率减半（向上取整，最低 100%）；</li>
     *   <li>高级能耗插件 → 距离倍率归 100%（跨维度倍率仍保留）。</li>
     * </ul>
     */
    private long distanceMultiplierPercent() {
        final GlobalPos location = stack.get(DataComponents.INSTANCE.getNetworkLocation());
        final Config.DistanceEnergyEntry config = Platform.getConfig().getDistanceEnergy();
        if (location != null && !location.dimension().equals(player.level().dimension())) {
            return config.getInterdimensionalMultiplier();
        }
        final long multiplier = location == null ? 100L : distanceTierMultiplier(config, location);
        if (advancedEnergy) {
            return 100L;
        }
        if (basicEnergy) {
            final int divisor = Math.max(1, config.getBasicEnergyDivisor());
            return Math.max(100L, (multiplier + divisor - 1) / divisor);
        }
        return multiplier;
    }

    private int distanceTierMultiplier(final Config.DistanceEnergyEntry config, final GlobalPos location) {
        final double dx = player.getX() - (location.pos().getX() + 0.5);
        final double dz = player.getZ() - (location.pos().getZ() + 0.5);
        final double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= config.getTier1Range()) {
            return config.getTier1Multiplier();
        }
        if (distance <= config.getTier2Range()) {
            return config.getTier2Multiplier();
        }
        if (distance <= config.getTier3Range()) {
            return config.getTier3Multiplier();
        }
        return config.getTier4Multiplier();
    }

    /**
     * @return 网络是否足额支付了这笔能量；否（未绑定 / 网络无电）时由调用方回落到物品电池。
     */
    private boolean payFromNetwork(final long cost) {
        return resolveNetwork()
            .map(network -> network.getComponent(EnergyNetworkComponent.class).extract(cost) >= cost)
            .orElse(false);
    }
}
