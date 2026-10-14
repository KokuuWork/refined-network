package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.Platform;

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
 * 本 mod 的核心扩展：在 RS 标准无线网络上下文之上，支持两种升级卡放宽「同维度 + 距离」校验。
 *
 * <p>RS 原生的距离/维度校验位于 {@code NetworkItemPlayerValidator}（由 Wireless Transmitter 注册），
 * 而非 {@code resolveNetwork} 本身。因此本实现采用「标准逻辑优先、卡片兜底放宽」策略：
 * <ul>
 *   <li>范围内（同维度 + 距离 OK）→ 完全走 RS 标准逻辑，零风险；</li>
 *   <li>超距离但有<b>范围卡</b> → 跳过距离校验，直接取 Wireless Transmitter 暴露的网络；</li>
 *   <li>跨维度且有<b>维度卡</b> → 跳过维度校验，直接取 Network Receiver 暴露的家庭网络（隧道）。</li>
 * </ul>
 *
 * <p>此外，装了<b>远距离输电卡</b>时，能量不再从物品自身电池扣，而是直接从所绑定的 RS 网络抽；
 * 若网络抽不出电（网络无电 / 未绑定），回落到物品电池兜底，行为与未装卡一致。
 *
 * <p>注意：{@code force=true} 时无条件委托 RS 标准实现，因为 RS 用 force 语义
 * 在玩家走远后仍能移除 grid watcher，不能被卡片逻辑改变。
 */
public class ExtendedWirelessNetworkItemContext implements NetworkItemContext {
    private final ItemStack stack;
    private final ServerPlayer player;
    private final SlotReference slotReference;
    private final boolean rangeUpgrade;
    private final boolean interdimensional;
    private final boolean remoteEnergy;
    private final NetworkItemContext standard;

    public ExtendedWirelessNetworkItemContext(final ItemStack stack,
                                              final ServerPlayer player,
                                              final SlotReference slotReference,
                                              final boolean rangeUpgrade,
                                              final boolean interdimensional,
                                              final boolean remoteEnergy) {
        this.stack = stack;
        this.player = player;
        this.slotReference = slotReference;
        this.rangeUpgrade = rangeUpgrade;
        this.interdimensional = interdimensional;
        this.remoteEnergy = remoteEnergy;
        this.standard = RefinedStorageApi.INSTANCE
            .getNetworkItemHelper()
            .createContext(stack, player, slotReference);
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
        // 维度卡：绑 Network Receiver → 走 Transmitter/Receiver 隧道（跨维度）
        // 范围卡：绑 Wireless Transmitter → 无视距离（同维度无限范围）
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
     * 装了输电卡时，「是否有电」由所绑定网络决定，而不是物品自身电池。
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
        final long cost = isInterdimensional()
            ? amount * Platform.getConfig().getWirelessCraftingGrid().getInterdimensionalEnergyMultiplier() / 100
            : amount;
        if (remoteEnergy && payFromNetwork(cost)) {
            return;
        }
        standard.drainEnergy(cost);
    }

    /**
     * @return 网络是否足额支付了这笔能量；否（未绑定 / 网络无电）时由调用方回落到物品电池。
     */
    private boolean payFromNetwork(final long cost) {
        return resolveNetwork()
            .map(network -> network.getComponent(EnergyNetworkComponent.class).extract(cost) >= cost)
            .orElse(false);
    }

    /**
     * 仅在「插了维度卡 且 绑定维度 != 玩家当前维度」时视为跨维度，才加能量价。
     */
    private boolean isInterdimensional() {
        if (!interdimensional) {
            return false;
        }
        final GlobalPos location = stack.get(DataComponents.INSTANCE.getNetworkLocation());
        return location != null && !location.dimension().equals(player.level().dimension());
    }
}
