package com.kokuu.rswirelesscrafting.common.chargingstation;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;

import java.util.ArrayList;
import java.util.Comparator;
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
 * 终端充电站：为「精致存储」的全部便携终端 / 无线终端以及本 mod 的无线合成终端补电。
 *
 * <p>两条充电路径：
 * <ul>
 *   <li><b>有线</b>：物品放进 GUI 的 3 个槽位，各自按 {@code wiredChargeRate} 快速充入；</li>
 *   <li><b>无线</b>：玩家站在 {@code wirelessRadius} 格内，背包里的物品按
 *       {@code wirelessChargeRate} 自动缓充。同一玩家同时最多充 3 个设备，充满一个后
 *       自动轮到下一个；同屏最多服务 3 名玩家，按离站距离由近到远各占一条无线链接。</li>
 * </ul>
 *
 * <p>配置里的能量值一律以<b>每秒（FE/s）</b>计，方块内部再按 20 tick/s 摊到每 tick 上
 * （{@code 200 FE/s → 10 FE/t}、{@code 20 FE/s → 1 FE/t}）。除不尽的部分交给
 * {@link RateMeter} 累积到下一 tick，配成 7 这种数也不会漏电。
 *
 * <p>能量来源优先用接入的 RS 网络（{@link EnergyNetworkComponent}），取不到电时回落到
 * 内部 FE 缓冲（由其他模组以标准 Forge Energy 输入）。
 *
 * <p>连接面板的状态（3 有线槽 + 3 无线链接）由服务端每 tick 计算并写进 {@link ContainerData}，
 * 多人同时靠近时每个玩家独占一条无线链接，状态互不干扰。
 */
public class ChargingStationBlockEntity extends AbstractNetworkNodeContainerBlockEntity<ChargingStationNetworkNode>
    implements Container, ExtendedMenuProvider<ChargingStationData> {

    private static final int WIRED_SLOT_COUNT = 3;
    private static final int WIRELESS_LINK_COUNT = 3;
    private static final int DEVICES_PER_LINK = 3;
    private static final int DATA_SIZE = 10;
    private static final String EXTERNAL_ENERGY_TAG = "ExternalEnergy";
    private static final String ITEMS_TAG = "Items";

    private final NonNullList<ItemStack> items = NonNullList.withSize(WIRED_SLOT_COUNT, ItemStack.EMPTY);
    private final RateMeter[] wiredMeters = {new RateMeter(), new RateMeter(), new RateMeter()};
    private final RateMeter wirelessMeter = new RateMeter();
    private final RateMeter idleMeter = new RateMeter();
    private final ChargeStatus[] wiredStatus = {
        ChargeStatus.IDLE, ChargeStatus.IDLE, ChargeStatus.IDLE
    };
    private final boolean[] wirelessActive = new boolean[WIRELESS_LINK_COUNT];
    private final long externalCapacity;

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
        this.externalCapacity = Platform.getConfig().getChargingStation().getExternalCapacity();
    }

    private Config.ChargingStationEntry entry() {
        return Platform.getConfig().getChargingStation();
    }

    // ---------------------------------------------------------------- tick

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Config.ChargingStationEntry config = entry();
        refreshNetworkEnergy();
        consumeEnergy(idleMeter.next(config.getEnergyUsage()));
        chargeWiredSlots(config);
        chargeWireless(serverLevel, config);
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

    private void chargeWiredSlots(final Config.ChargingStationEntry config) {
        for (int slot = 0; slot < WIRED_SLOT_COUNT; slot++) {
            wiredStatus[slot] = chargeWiredSlot(slot, config);
        }
    }

    private ChargeStatus chargeWiredSlot(final int slot, final Config.ChargingStationEntry config) {
        final ItemStack stack = items.get(slot);
        if (stack.isEmpty()) {
            return ChargeStatus.IDLE;
        }
        final long budget = wiredMeters[slot].next(config.getWiredChargeRate());
        final long charged = budget > 0 ? chargeItem(stack, budget) : 0;
        if (charged > 0) {
            return ChargeStatus.CHARGING;
        }
        return isFull(stack) ? ChargeStatus.FULL : ChargeStatus.IDLE;
    }

    // ------------------------------------------------------------ 无线充电

    private void chargeWireless(final ServerLevel serverLevel, final Config.ChargingStationEntry config) {
        final int radius = config.getWirelessRadius();
        final long perDeviceBudget = wirelessMeter.next(config.getWirelessChargeRate());
        if (radius <= 0 || perDeviceBudget <= 0) {
            clearWirelessLinks();
            return;
        }
        final List<ServerPlayer> nearby = nearbyPlayersByDistance(serverLevel, radius);
        for (int link = 0; link < WIRELESS_LINK_COUNT; link++) {
            wirelessActive[link] = link < nearby.size()
                && chargePlayerDevices(nearby.get(link), perDeviceBudget);
        }
    }

    /**
     * 半径内的玩家按离站距离由近到远排序，最靠近的 {@link #WIRELESS_LINK_COUNT} 名玩家各占一条链接。
     */
    private List<ServerPlayer> nearbyPlayersByDistance(final ServerLevel level, final int radius) {
        final double radiusSquared = (double) radius * radius;
        final double centerX = worldPosition.getX() + 0.5;
        final double centerY = worldPosition.getY() + 0.5;
        final double centerZ = worldPosition.getZ() + 0.5;
        final List<ServerPlayer> nearby = new ArrayList<>();
        for (final ServerPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (player.distanceToSqr(centerX, centerY, centerZ) <= radiusSquared) {
                nearby.add(player);
            }
        }
        nearby.sort(Comparator.comparingDouble(player -> player.distanceToSqr(centerX, centerY, centerZ)));
        return nearby;
    }

    /**
     * 为一名玩家背包里的 FE 设备充电：同一 tick 最多同时充 {@link #DEVICES_PER_LINK} 个，
     * 已充满的自动跳过，于是「先同时充 3 个、充满后轮到后续设备」自然成立。
     *
     * @return 本 tick 是否真的充入了能量（用于点亮该条无线链接）
     */
    private boolean chargePlayerDevices(final ServerPlayer player, final long perDeviceBudget) {
        final Inventory inventory = player.getInventory();
        boolean anyCharged = false;
        int devicesCharged = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            final long charged = chargeItem(stack, perDeviceBudget);
            if (charged <= 0) {
                continue;
            }
            anyCharged = true;
            devicesCharged++;
            if (devicesCharged >= DEVICES_PER_LINK) {
                break;
            }
        }
        if (anyCharged) {
            inventory.setChanged();
        }
        return anyCharged;
    }

    private void clearWirelessLinks() {
        for (int link = 0; link < WIRELESS_LINK_COUNT; link++) {
            wirelessActive[link] = false;
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

    private boolean isFull(final ItemStack stack) {
        final Platform.ItemEnergyAccess access = Platform.getItemEnergy(stack).orElse(null);
        return access != null && access.getCapacity() > 0 && access.getStored() >= access.getCapacity();
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
        return ContentNames.TERMINAL_CHARGING_STATION;
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
        return WIRED_SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (final ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
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
     * 把配置里的「每秒」额度摊到每 tick 上。
     *
     * <p>Minecraft 是每秒 20 tick，而 {@code 每秒额度 / 20} 常常除不尽。直接整除会持续漏掉
     * 那一小截，所以这里把余数留在 {@code carry} 里顺延到下一 tick，长期平均恰好等于配置值。
     */
    private static final class RateMeter {
        private static final long TICKS_PER_SECOND = 20;

        private long carry;

        /**
         * 推进一 tick。
         *
         * @return 本 tick 可用的额度
         */
        long next(final long perSecond) {
            final long total = carry + Math.max(0, perSecond);
            carry = total % TICKS_PER_SECOND;
            return total / TICKS_PER_SECOND;
        }
    }

    /**
     * 同步给界面的实时数值：网络存量 / 网络容量 / 外部 FE 缓冲 / 缓冲上限，
     * 以及 3 个有线槽状态（{@link ChargeStatus#ordinal()}）与 3 条无线链接的开关（0/1）。
     */
    private class ChargingStationDataSlots implements ContainerData {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(networkStored, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(networkCapacity, Integer.MAX_VALUE);
                case 2 -> (int) Math.min(externalEnergy, Integer.MAX_VALUE);
                case 3 -> (int) Math.min(externalCapacity, Integer.MAX_VALUE);
                case 4, 5, 6 -> wiredStatus[index - 4].ordinal();
                case 7, 8, 9 -> wirelessActive[index - 7] ? 1 : 0;
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
