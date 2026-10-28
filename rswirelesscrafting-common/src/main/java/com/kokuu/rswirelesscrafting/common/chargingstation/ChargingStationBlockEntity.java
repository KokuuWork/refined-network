package com.kokuu.rswirelesscrafting.common.chargingstation;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.link.LinkSecurity;
import com.kokuu.rswirelesscrafting.common.util.RateMeter;
import com.kokuu.rswirelesscrafting.common.util.SaturatedMath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
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
 * <p>能量模型是「网络 → 内部缓冲 → 充电动作」：充电站自带一个内部 FE 缓冲（容量
 * {@code externalCapacity}，界面展示的就是它），未满时按固定速率从接入的 RS 网络
 * （{@link EnergyNetworkComponent}）补电；网络之外的模组也能以标准 Forge Energy 直接
 * 充进同一缓冲。
 *
 * <p><b>基础待机损耗</b>：只要无线充电模块在运行，就按 {@code energyUsage} 每秒常驻扣电
 * （充电耗电另算）。可被两处开关关掉：① 配置总闸 {@code idleEnergyUsageEnabled}；
 * ② 红石信号——被红石充能即关闭无线充电并停扣待机损耗，作为省电开关。
 *
 * <p>连接面板的状态（3 有线槽 + 3 无线链接）由服务端每 tick 计算并写进 {@link ContainerData}，
 * 多人同时靠近时每个玩家独占一条无线链接，状态互不干扰。
 */
public class ChargingStationBlockEntity extends AbstractNetworkNodeContainerBlockEntity<ChargingStationNetworkNode>
    implements Container, ExtendedMenuProvider<ChargingStationData> {

    private static final int WIRED_SLOT_COUNT = 3;
    // 3 个无线链接位 = 全局 3 个「设备名额」：3 名玩家各占 1 个，1 名玩家独占 3 个。
    private static final int WIRELESS_LINK_COUNT = 3;
    private static final int DATA_SIZE = 10;
    private static final int WIRED_EFFICIENCY_PERCENT = 100;
    // 缓冲从网络补电的速率（FE/s）：略高于满负荷（待机 128 + 有线 600 + 无线 60 ≈ 788），
    // 正常供电时缓冲常满，网络断电后缓冲掉电、恢复供电再慢慢补回。
    private static final long BUFFER_FILL_RATE_PER_SECOND = 1000;
    private static final String EXTERNAL_ENERGY_TAG = "ExternalEnergy";
    private static final String ITEMS_TAG = "Items";
    private static final String OWNER_TAG = "Owner";
    // 无线充电每 10 tick 扫描一次（扫描一次就按 10 tick 的量充，效果与每 tick 相同、开销降 90%）。
    // 服务器上玩家多、充电站多时，每 tick 全量遍历附近玩家背包会成为热点。
    private static final int WIRELESS_SCAN_INTERVAL = 10;
    // 方块状态（通电 / 无线）两次写入之间的最小间隔（tick）。
    // 网络只剩几 FE 时缓冲会在「空 / 有一点点」之间抖动，无限流就会每 tick 广播一次方块更新，
    // 连带邻居红石更新，多人环境下是实打实的包风暴。
    private static final int BLOCK_STATE_UPDATE_INTERVAL = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(WIRED_SLOT_COUNT, ItemStack.EMPTY);
    private final RateMeter[] wiredMeters = {new RateMeter(), new RateMeter(), new RateMeter()};
    private final RateMeter wirelessMeter = new RateMeter();
    private final RateMeter idleMeter = new RateMeter();
    private final RateMeter bufferFillMeter = new RateMeter();
    private final ChargeStatus[] wiredStatus = {
        ChargeStatus.IDLE, ChargeStatus.IDLE, ChargeStatus.IDLE
    };
    private final boolean[] wirelessActive = new boolean[WIRELESS_LINK_COUNT];

    private long externalEnergy;
    @Nullable
    private UUID owner;
    private long networkStored;
    private long networkCapacity;
    private int wirelessScanCountdown;
    private int blockStateCooldown;

    private final ContainerData containerData = new ChargingStationDataSlots();

    public ChargingStationBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getChargingStation(),
            pos,
            state,
            new ChargingStationNetworkNode(Platform.getConfig().getChargingStation().getEnergyUsage())
        );
    }

    private Config.ChargingStationEntry entry() {
        return Platform.getConfig().getChargingStation();
    }

    public boolean canOpen(final ServerPlayer player) {
        final Network network = mainNetworkNode.getNetwork();
        return network == null || (LinkSecurity.isAllowed(player, BuiltinPermission.OPEN, network)
            && LinkSecurity.isAllowed(player, BuiltinPermission.EXTRACT, network));
    }

    public void setOwner(final UUID playerId) {
        if (!playerId.equals(owner)) {
            owner = playerId;
            setChanged();
        }
    }

    private boolean isChargingAllowed() {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null || owner == null) {
            return true;
        }
        return level instanceof ServerLevel serverLevel
            && LinkSecurity.isAllowed(owner, BuiltinPermission.OPEN, network, serverLevel)
            && LinkSecurity.isAllowed(owner, BuiltinPermission.EXTRACT, network, serverLevel);
    }

    // ---------------------------------------------------------------- tick

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Config.ChargingStationEntry config = entry();
        if (!isChargingAllowed()) {
            networkStored = 0;
            networkCapacity = 0;
            updateBlockState(false, false);
            clearWirelessLinks();
            return;
        }
        refreshNetworkEnergy();
        refillBufferFromNetwork();
        final boolean wirelessEnabled = !isRedstoneDisabling();
        final boolean powered = networkStored > 0 || externalEnergy > 0;
        updateBlockState(powered, wirelessEnabled);
        if (wirelessEnabled && config.isIdleEnergyUsageEnabled()) {
            consumeEnergy(idleMeter.next(config.getEnergyUsage()));
        }
        chargeWiredSlots(config);
        if (!wirelessEnabled) {
            // 未扫描的 tick 保留上一次的链接灯状态，避免灯闪烁。
            clearWirelessLinks();
            wirelessScanCountdown = 0;
            return;
        }
        // 倒数到 0 才扫描。注意重置值必须是 INTERVAL - 1：倒数那一次本身也占一个 tick，
        // 写成 INTERVAL 会让实际间隔变成 11 tick、充电速率被悄悄压低约 9%。
        if (wirelessScanCountdown > 0) {
            wirelessScanCountdown--;
            return;
        }
        wirelessScanCountdown = WIRELESS_SCAN_INTERVAL - 1;
        chargeWireless(serverLevel, config, WIRELESS_SCAN_INTERVAL);
    }

    /**
     * 红石信号是「无线充电」的省电开关：被红石充能时关闭无线充电，并停扣基础待机损耗
     * （有线槽仍正常工作）。玩家拉下拉杆即可关掉无线模块、省下待机损耗。
     */
    private boolean isRedstoneDisabling() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    /**
     * 把「通电（有无能量来源）」与「无线是否开启」同步到两个方块状态，
     * 外形上的电源灯与无线灯各自亮/灭。只在状态实际变化时写一次，避免每 tick 无谓刷新。
     */
    private void updateBlockState(final boolean powered, final boolean wirelessEnabled) {
        if (level == null) {
            return;
        }
        if (blockStateCooldown > 0) {
            // 冷却中：不写方块（抖动期的中间态直接跳过），冷却结束后自然补上最新状态。
            blockStateCooldown--;
            return;
        }
        final BlockState state = getBlockState();
        final boolean poweredChanged = state.getValue(ChargingStationBlock.POWERED) != powered;
        final boolean wirelessChanged = state.getValue(ChargingStationBlock.WIRELESS) != wirelessEnabled;
        if (poweredChanged || wirelessChanged) {
            level.setBlockAndUpdate(
                worldPosition,
                state.setValue(ChargingStationBlock.POWERED, powered)
                    .setValue(ChargingStationBlock.WIRELESS, wirelessEnabled)
            );
            blockStateCooldown = BLOCK_STATE_UPDATE_INTERVAL;
        }
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

    /**
     * 缓冲未满时按 {@link #BUFFER_FILL_RATE_PER_SECOND} 的速率从网络抽电补进内部缓冲。
     * 充电站的能量来源是「网络 → 内部缓冲 → 充电动作」，缓冲是界面展示的唯一电量池。
     */
    private void refillBufferFromNetwork() {
        final long missing = getExternalCapacity() - externalEnergy;
        if (missing <= 0) {
            return;
        }
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return;
        }
        final EnergyNetworkComponent energy = network.getComponent(EnergyNetworkComponent.class);
        if (energy.getStored() <= 0) {
            return;
        }
        final long budget = bufferFillMeter.next(BUFFER_FILL_RATE_PER_SECOND);
        if (budget <= 0) {
            return;
        }
        final long extracted = energy.extract(Math.min(budget, missing));
        if (extracted > 0) {
            externalEnergy += extracted;
            setChanged();
        }
        networkStored = energy.getStored();
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
        final long charged = budget > 0 ? chargeItem(stack, budget, WIRED_EFFICIENCY_PERCENT) : 0;
        if (charged > 0) {
            return ChargeStatus.CHARGING;
        }
        return isFull(stack) ? ChargeStatus.FULL : ChargeStatus.IDLE;
    }

    // ------------------------------------------------------------ 无线充电

    /**
     * @param intervalTicks 距上次扫描过了多少 tick；本轮按这个时长一次性发放充电额度，
     *                      所以降频不会改变实际充电速率。
     */
    private void chargeWireless(final ServerLevel serverLevel,
                                final Config.ChargingStationEntry config,
                                final int intervalTicks) {
        final int radius = config.getWirelessRadius();
        final long perDeviceBudget = wirelessMeter.next(
            SaturatedMath.multiply(config.getWirelessChargeRate(), intervalTicks)
        );
        clearWirelessLinks();
        if (radius <= 0 || perDeviceBudget <= 0) {
            return;
        }
        final List<ServerPlayer> nearby = nearbyPlayersByDistance(serverLevel, radius);
        if (nearby.isEmpty()) {
            return;
        }
        // 全局只有 {@link #WIRELESS_LINK_COUNT} 个设备名额，按「每名玩家先拿 1 个、有余再轮」
        // 的次序分配给按距离排序的玩家：3 名玩家各充 1 个设备，1 名玩家独占 3 个。
        final int playerCount = Math.min(nearby.size(), WIRELESS_LINK_COUNT);
        final int[] quota = new int[playerCount];
        for (int link = 0; link < WIRELESS_LINK_COUNT; link++) {
            quota[link % playerCount]++;
        }
        final boolean[][] charged = new boolean[playerCount][];
        final int efficiencyPercent = config.getWirelessChargeEfficiencyPercent();
        for (int i = 0; i < playerCount; i++) {
            charged[i] = chargePlayerDevices(nearby.get(i), perDeviceBudget, efficiencyPercent, quota[i]);
        }
        int link = 0;
        for (int i = 0; i < playerCount; i++) {
            for (int device = 0; device < quota[i]; device++) {
                wirelessActive[link++] = charged[i][device];
            }
        }
    }

    /**
     * 半径内的玩家按离站距离由近到远排序，最靠近的 {@link #WIRELESS_LINK_COUNT} 名玩家参与名额分配。
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
     * 为一名玩家背包里的 FE 设备充电：本轮最多同时充 {@code maxDevices} 个，已充满的自动跳过，
     * 于是「先同时充 N 个、充满后轮到后续设备」自然成立。
     *
     * @return 每个设备名额是否真的充入了能量（用于逐个点亮无线链接灯）
     */
    private boolean[] chargePlayerDevices(final ServerPlayer player,
                                          final long perDeviceBudget,
                                          final int efficiencyPercent,
                                          final int maxDevices) {
        final boolean[] charged = new boolean[maxDevices];
        final Inventory inventory = player.getInventory();
        boolean anyCharged = false;
        int devicesCharged = 0;
        for (int slot = 0; slot < inventory.getContainerSize() && devicesCharged < maxDevices; slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            final long given = chargeItem(stack, perDeviceBudget, efficiencyPercent);
            if (given <= 0) {
                continue;
            }
            charged[devicesCharged] = true;
            devicesCharged++;
            anyCharged = true;
        }
        if (anyCharged) {
            inventory.setChanged();
        }
        return charged;
    }

    private void clearWirelessLinks() {
        for (int link = 0; link < WIRELESS_LINK_COUNT; link++) {
            wirelessActive[link] = false;
        }
    }

    // -------------------------------------------------------------- 核心充电

    /**
     * @param efficiencyPercent 充电效率（百分比）：充入设备 {@code given} 需要消耗
     *                          {@code given * 100 / efficiencyPercent} 的能量（向上取整），
     *                          差额作为传输损耗。100 = 无损耗（有线）。
     * @return 实际充入物品的能量
     */
    private long chargeItem(final ItemStack stack, final long rate, final int efficiencyPercent) {
        long given = 0;
        final Platform.ItemEnergyAccess access =
            rate > 0 ? Platform.getItemEnergy(stack).orElse(null) : null;
        if (access != null) {
            given = deliver(access, simulateAccepted(access, rate), efficiencyPercent);
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

    /**
     * 把充入量按效率折算成实际消耗后充入。
     *
     * <p>先按可用能量折算「最多能充多少」（{@code available * efficiency / 100}，向下取整，
     * 保证不超支），再真正充入并按 {@link #costFor(long, int)} 扣掉对应消耗（向上取整，
     * 防止低效率时 0 损耗白嫖）。
     */
    private long deliver(final Platform.ItemEnergyAccess access,
                         final long accepted,
                         final int efficiencyPercent) {
        if (accepted <= 0) {
            return 0;
        }
        final long available = availableEnergy();
        if (available <= 0) {
            return 0;
        }
        final long payable = Math.min(accepted, SaturatedMath.multiply(available, efficiencyPercent) / 100);
        if (payable <= 0) {
            return 0;
        }
        final long given = access.receive(payable, false);
        if (given > 0) {
            consumeEnergy(costFor(given, efficiencyPercent));
        }
        return given;
    }

    /**
     * 充入 {@code given} 能量需要消耗多少网络能量（按效率折算，向上取整）。
     */
    private long costFor(final long given, final int efficiencyPercent) {
        if (efficiencyPercent >= 100) {
            return given;
        }
        return SaturatedMath.add(SaturatedMath.multiply(given, 100), efficiencyPercent - 1) / efficiencyPercent;
    }

    // ------------------------------------------------------------ 能量账本

    private long availableEnergy() {
        return externalEnergy;
    }

    /**
     * 从内部缓冲扣电。缓冲未满时会由 {@link #refillBufferFromNetwork()} 从网络补回。
     */
    private void consumeEnergy(final long amount) {
        if (amount <= 0 || externalEnergy <= 0) {
            return;
        }
        externalEnergy -= Math.min(amount, externalEnergy);
        setChanged();
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
        final long accepted = Math.min(amount, Math.max(0, getExternalCapacity() - externalEnergy));
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

    /** Fabric 能量事务回滚时恢复外部缓冲。 */
    public void restoreExternalEnergy(final long value) {
        externalEnergy = Math.max(0L, Math.min(value, getExternalCapacity()));
        setChanged();
    }

    public long getExternalCapacity() {
        return Platform.getConfig().getChargingStation().getExternalCapacity();
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
        if (owner != null) {
            tag.putUUID(OWNER_TAG, owner);
        }
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
        owner = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
        // 旧存档可能在容量下调前（externalCapacity 10000→4000）存入了更大的能量值，
        // 这里必须 clamp 到当前容量，否则界面会显示「超容量」的异常电量。
        externalEnergy = Math.min(tag.getLong(EXTERNAL_ENERGY_TAG), getExternalCapacity());
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
     * 同步给界面的实时数值：内部缓冲存量 / 缓冲容量（界面能量条）、网络存量 / 网络容量
     * （暂不展示，保留），以及 3 个有线槽状态（{@link ChargeStatus#ordinal()}）与
     * 3 条无线链接的开关（0/1）。
     */
    private class ChargingStationDataSlots implements ContainerData {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(externalEnergy, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(getExternalCapacity(), Integer.MAX_VALUE);
                case 2 -> (int) Math.min(networkStored, Integer.MAX_VALUE);
                case 3 -> (int) Math.min(networkCapacity, Integer.MAX_VALUE);
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
