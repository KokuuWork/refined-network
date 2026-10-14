package com.kokuu.rswirelesscrafting.common.chargingstation;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.DefaultEnergyUsage;
import com.kokuu.rswirelesscrafting.common.Platform;

import java.util.List;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 充电站：为玩家身上和槽位里的 FE 物品（含 Refined Storage 的全部无线终端、便携终端，
 * 以及本 mod 的无线合成终端）补电。
 *
 * <p>两条充电路径：
 * <ul>
 *   <li><b>有线</b>：物品放进 GUI 的槽位，每 tick 按 {@code wiredChargeRate} 快速充入；</li>
 *   <li><b>无线</b>：玩家站在 {@code wirelessRadius} 格内，背包里的物品每 tick 按
 *       {@code wirelessChargeRate} 自动缓充。</li>
 * </ul>
 *
 * <p>能量来源同样是两条，且与 RS 其他机械一致地优先使用网络：
 * <ol>
 *   <li>方块接入的 RS 网络（{@link EnergyNetworkComponent}）——主来源；</li>
 *   <li>内部 FE 缓冲——由其他模组以标准 Forge Energy 输入（注册在
 *       {@code Capabilities.EnergyStorage.BLOCK} 上），在网络取不到电时兜底，
 *       让充电站在纯 FE 整合包里也能工作。</li>
 * </ol>
 */
public class ChargingStationBlockEntity extends AbstractNetworkNodeContainerBlockEntity<ChargingStationNetworkNode>
    implements Container, ExtendedMenuProvider<ChargingStationData> {

    private static final int SLOT_COUNT = 1;
    private static final int DATA_SIZE = 4;
    private static final String EXTERNAL_ENERGY_TAG = "ExternalEnergy";
    private static final String ITEMS_TAG = "Items";

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final long externalCapacity = DefaultEnergyUsage.CHARGING_STATION_EXTERNAL_CAPACITY;

    private long externalEnergy;
    private long networkStored;
    private long networkCapacity;

    private final ContainerData containerData = new ChargingStationDataSlots();

    public ChargingStationBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getChargingStation(),
            pos,
            state,
            new ChargingStationNetworkNode(Platform.getConfig().getChargingStation().getEnergyUsage())
        );
    }

    // ---------------------------------------------------------------- tick

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        refreshNetworkEnergy();
        // 待机耗电：与 RS 其他机械一样，先在网络上扣掉自身开销。
        consumeEnergy(mainNetworkNode.getEnergyUsage());
        chargeWiredSlot();
        chargeNearbyPlayers(serverLevel);
    }

    private void refreshNetworkEnergy() {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            networkStored = 0;
            networkCapacity = 0;
            return;
        }
        final EnergyNetworkComponent energy = network.getComponent(EnergyNetworkComponent.class);
        networkStored = energy.getStored();
        networkCapacity = energy.getCapacity();
    }

    // ------------------------------------------------------------ 有线充电

    private void chargeWiredSlot() {
        final ItemStack stack = items.get(0);
        if (stack.isEmpty()) {
            return;
        }
        if (chargeItem(stack, Platform.getConfig().getChargingStation().getWiredChargeRate()) > 0) {
            setChanged();
        }
    }

    // ------------------------------------------------------------ 无线充电

    private void chargeNearbyPlayers(final ServerLevel serverLevel) {
        final int radius = Platform.getConfig().getChargingStation().getWirelessRadius();
        final long rate = Platform.getConfig().getChargingStation().getWirelessChargeRate();
        if (radius <= 0 || rate <= 0) {
            return;
        }
        final double radiusSquared = (double) radius * radius;
        final List<ServerPlayer> players = serverLevel.players();
        for (final ServerPlayer player : players) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
            ) > radiusSquared) {
                continue;
            }
            chargeInventory(player, rate);
        }
    }

    private void chargeInventory(final ServerPlayer player, final long budget) {
        final Inventory inventory = player.getInventory();
        long remaining = budget;
        boolean changed = false;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            final long charged = chargeItem(stack, remaining);
            if (charged > 0) {
                remaining -= charged;
                changed = true;
            }
        }
        if (changed) {
            inventory.setChanged();
        }
    }

    // -------------------------------------------------------------- 核心充电

    /**
     * @return 实际充入物品的能量
     */
    private long chargeItem(final ItemStack stack, final long rate) {
        long given = 0;
        final Platform.ItemEnergyAccess access =
            rate > 0 ? Platform.getItemEnergy(stack).orElse(null) : null;
        if (access != null) {
            given = deliver(access, simulateAccepted(access, rate));
        }
        return given;
    }

    /**
     * 先问物品愿意接收多少（模拟），避免从网络白抽电。
     */
    private long simulateAccepted(final Platform.ItemEnergyAccess access, final long rate) {
        final long missing = access.getCapacity() - access.getStored();
        return missing <= 0 ? 0 : access.receive(Math.min(rate, missing), true);
    }

    private long deliver(final Platform.ItemEnergyAccess access, final long accepted) {
        final long available = accepted > 0 ? availableEnergy() : 0;
        if (available <= 0) {
            return 0;
        }
        final long given = access.receive(Math.min(accepted, available), false);
        if (given > 0) {
            consumeEnergy(given);
        }
        return given;
    }

    // ------------------------------------------------------------ 能量账本

    private long availableEnergy() {
        long available = externalEnergy;
        final Network network = mainNetworkNode.getNetwork();
        if (network != null) {
            available += Math.max(0, network.getComponent(EnergyNetworkComponent.class).getStored());
        }
        return available;
    }

    /**
     * 优先扣网络能量，不足部分由内部 FE 缓冲补齐。
     */
    private void consumeEnergy(final long amount) {
        if (amount <= 0) {
            return;
        }
        long remaining = amount;
        final Network network = mainNetworkNode.getNetwork();
        if (network != null) {
            final EnergyNetworkComponent energy = network.getComponent(EnergyNetworkComponent.class);
            if (energy.getStored() > 0) {
                remaining -= energy.extract(remaining);
                networkStored = energy.getStored();
            }
        }
        if (remaining > 0 && externalEnergy > 0) {
            externalEnergy -= Math.min(remaining, externalEnergy);
            setChanged();
        }
    }

    // -------------------------------------------------- 外部 FE 输入（兼容层）

    /**
     * 接收其他模组以标准 Forge Energy 输入的能量，存入内部缓冲。
     *
     * @return 实际接收量
     */
    public long receiveExternalEnergy(final long amount, final boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        final long accepted = Math.min(amount, Math.max(0, externalCapacity - externalEnergy));
        if (!simulate && accepted > 0) {
            externalEnergy += accepted;
            setChanged();
        }
        return accepted;
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    public long getExternalEnergy() {
        return externalEnergy;
    }

    public long getExternalCapacity() {
        return externalCapacity;
    }

    // ---------------------------------------------------------------- 菜单

    @Override
    public Component getDisplayName() {
        return ContentNames.CHARGING_STATION;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId,
                                            final Inventory inventory,
                                            final Player player) {
        return new ChargingStationContainerMenu(syncId, inventory, this, containerData);
    }

    @Override
    public ChargingStationData getMenuData() {
        return new ChargingStationData(worldPosition);
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ChargingStationData> getMenuCodec() {
        return ChargingStationData.STREAM_CODEC;
    }

    // -------------------------------------------------------------- Container

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(final int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        final ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(final Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    // ------------------------------------------------------------------ NBT

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong(EXTERNAL_ENERGY_TAG, externalEnergy);
        final CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                itemsTag.put("Slot" + i, items.get(i).save(registries));
            }
        }
        tag.put(ITEMS_TAG, itemsTag);
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        externalEnergy = tag.getLong(EXTERNAL_ENERGY_TAG);
        final CompoundTag itemsTag = tag.getCompound(ITEMS_TAG);
        for (int i = 0; i < items.size(); i++) {
            final int slot = i;
            final String key = "Slot" + slot;
            if (itemsTag.contains(key)) {
                ItemStack.parse(registries, itemsTag.getCompound(key))
                    .ifPresent(stack -> items.set(slot, stack));
            }
        }
    }

    /**
     * 同步给界面的实时数值：网络存量 / 网络容量 / 外部 FE 缓冲 / 缓冲上限。
     */
    private class ChargingStationDataSlots implements ContainerData {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(networkStored, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(networkCapacity, Integer.MAX_VALUE);
                case 2 -> (int) Math.min(externalEnergy, Integer.MAX_VALUE);
                case 3 -> (int) Math.min(externalCapacity, Integer.MAX_VALUE);
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            // 只读数据，客户端不回写。
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    }
}
