package com.kokuu.refinednetwork.common.energymonitor;

import com.kokuu.refinednetwork.common.BlockEntities;
import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.connector.ConnectorTarget;
import com.kokuu.refinednetwork.common.link.AdjacentNetworks;
import com.kokuu.refinednetwork.common.link.BoundPosition;
import com.kokuu.refinednetwork.common.link.BoundTargetResolver;
import com.kokuu.refinednetwork.common.link.LinkSecurity;
import com.kokuu.refinednetwork.common.util.CompactNumber;
import com.kokuu.refinednetwork.common.util.RateMeter;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 能量监控仪：让 RS 网络的能量第一次变得**可观测**。
 *
 * <h2>它解决什么问题</h2>
 * RS2 本体没有任何查看网络能量的手段——存量、容量、此刻是进电还是出电、还能撑多久，
 * 一概看不到。玩家只能凭「机器突然停了」反推没电了。本方块就是这块仪表。
 *
 * <h2>为什么它不是网络节点</h2>
 * 它**只读**：不需要向网络提供存储或能量，也不该被算进网络的耗电统计。
 * 所以这里不继承 RS 的方块实体基类、不建节点容器，只在自己要看的网络上
 * 读 {@link EnergyNetworkComponent}。这样也顺带避开了「节点同时待在本网与远端网」
 * 那类重复计入的麻烦。
 *
 * <h2>两种用法</h2>
 * <ul>
 *   <li><b>摆在网络里</b>：贴着线缆或任意 RS 网络方块放，自动盯着那张网；</li>
 *   <li><b>绑定到远处</b>：潜行右键剪贴板里的接入点（和无线桥同一套仪式），
 *       于是人在主基地也能看矿场的网络电量。</li>
 * </ul>
 *
 * <h2>输出</h2>
 * 比较器输出 = 电量充盈度（0–15）；低于阈值亮红灯；右键在聊天栏报
 * 存量 / 容量 / 净收支 / 预计耗尽时间。
 */
public class EnergyMonitorBlockEntity extends BlockEntity implements ConnectorTarget {
    private static final String TAG_OWNER = "Owner";
    /**
     * 「显示屏」同步用的短键：只给客户端渲染文字用，和存档标签分开。
     */
    private static final String TAG_DISPLAY_STORED = "ds";
    private static final String TAG_DISPLAY_CAPACITY = "dc";
    private static final String TAG_DISPLAY_RATE = "dr";
    private static final String TAG_DISPLAY_LINKED = "dl";
    /**
     * 多少 tick 往客户端推一次数值（10 tick = 0.5 秒，肉眼看起来是连续的）。
     */
    private static final long DISPLAY_SYNC_INTERVAL = 10L;

    private final RateMeter feeMeter = new RateMeter();

    @Nullable
    private GlobalPos target;
    @Nullable
    private UUID owner;
    private long stored;
    private long capacity;
    /** 净收支，单位 FE/s（正 = 在进电，负 = 在出电）。 */
    private long netRate;
    private int sampleCountdown;
    private long lastSampleStored;
    private long lastSampleGameTime = -1L;
    @Nullable
    private Network sampledNetwork;
    private boolean linked;
    private boolean low;

    public EnergyMonitorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getEnergyMonitor(), pos, state);
    }

    // ------------------------------------------------------------- 状态查询

    public boolean isEndpoint() {
        return target != null;
    }

    @Nullable
    public GlobalPos getTarget() {
        return target;
    }

    public boolean isLinked() {
        return linked;
    }

    public boolean isLow() {
        return low;
    }

    public long getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getNetRate() {
        return netRate;
    }

    /**
     * 按当前净收支估算还能撑多久（秒）；在进电或收支平衡时返回 -1。
     */
    public long getSecondsUntilEmpty() {
        if (netRate >= 0 || stored <= 0) {
            return -1;
        }
        return stored / -netRate;
    }

    /**
     * 方块上方那块「显示屏」要写的内容：电量 / 净收支 / 预计耗尽。
     *
     * <p>客户端方块实体一样能调——{@link #getUpdateTag()} 会把这三个数同步过去，
     * 渲染器（{@code EnergyMonitorRenderer}）直接拿这几行画。
     */
    public List<Component> displayLines() {
        if (!linked) {
            return List.of();
        }
        final long seconds = getSecondsUntilEmpty();
        return List.of(
            Component.literal(CompactNumber.format(stored) + " / " + CompactNumber.format(capacity) + " FE"),
            Component.literal((netRate >= 0 ? "+" : "") + CompactNumber.format(netRate) + " FE/s"),
            Component.literal(seconds < 0 ? "--" : seconds + "s")
        );
    }

    /**
     * 比较器输出（0–15）：按电量充盈度线性映射。
     */
    public int getAnalogOutput() {
        if (capacity <= 0) {
            return 0;
        }
        return (int) Math.max(0, Math.min(15, stored * 15.0 / capacity));
    }

    // ------------------------------------------------------------- 绑定

    public boolean bindTo(final GlobalPos anchor, final ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        final Network candidate = BoundTargetResolver.resolveNetwork(serverLevel, anchor).orElse(null);
        if (candidate != null && !LinkSecurity.isAllowed(player, BuiltinPermission.OPEN, candidate)) {
            return false;
        }
        owner = player.getUUID();
        this.target = anchor;
        this.sampleCountdown = 0;
        this.lastSampleGameTime = -1L;
        setChanged();
        return true;
    }

    public void unbind() {
        this.target = null;
        this.owner = null;
        this.lastSampleGameTime = -1L;
        setChanged();
    }

    @Override
    public boolean isBound() {
        return isEndpoint();
    }

    @Override
    public Component boundMessage() {
        return Component.translatable(
            ContentNames.ENERGY_MONITOR_BOUND_TRANSLATION_KEY,
            target == null ? "?" : target.pos().toShortString()
        );
    }

    @Override
    public Component unboundMessage() {
        return ContentNames.ENERGY_MONITOR_UNBOUND;
    }

    public void setOwner(final UUID playerId) {
        if (!playerId.equals(owner)) {
            owner = playerId;
            setChanged();
        }
    }

    private boolean canRead(final Network network) {
        if (owner == null) {
            // 未绑定且没有放置者的旧方块保持原行为；新放置/绑定都会记录 owner。
            return target == null;
        }
        return level instanceof ServerLevel serverLevel
            && LinkSecurity.isAllowed(owner, BuiltinPermission.OPEN, network, serverLevel);
    }

    // ------------------------------------------------------------- tick

    public void tick() {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        final Config.EnergyMonitorEntry config = Platform.getConfig().getEnergyMonitor();
        if (!config.isEnabled()) {
            read(null);
            lastSampleGameTime = -1L;
            updateState(false, false);
            return;
        }
        final Network network = resolveNetwork();
        if (network != null && !canRead(network)) {
            read(null);
            updateState(false, false);
            return;
        }
        read(network);
        final boolean newLinked = network != null;
        final boolean newLow = newLinked && isBelowThreshold(config);
        updateState(newLinked, newLow);
        if (newLinked) {
            consumeFee(network, config);
        }
        if (sampleCountdown-- <= 0) {
            sampleCountdown = Math.max(1, config.getSampleInterval()) - 1;
            sample();
        }
        if (level.getGameTime() % DISPLAY_SYNC_INTERVAL == 0L) {
            syncDisplay();
        }
    }

    /**
     * 把数值推给客户端，方块上方的牌子才会跟着变。
     */
    private void syncDisplay() {
        if (level == null || level.isClientSide()) {
            return;
        }
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    /**
     * 绑定着的看绑定的那张网；没绑定的就贴着线缆看本地这张网。
     */
    @Nullable
    private Network resolveNetwork() {
        final GlobalPos current = target;
        if (current != null) {
            return BoundTargetResolver.resolveNetwork(level, current).orElse(null);
        }
        return adjacentNetwork();
    }

    /**
     * 六邻域里随便找一张 RS 网络：监控仪贴在网络里就等于盯上它。
     *
     * <p>「RS 自己的方块实体实现 {@code NetworkItemTargetBlockEntity}、容器只挂在方块能力上」
     * 这套细节统一收在 {@link AdjacentNetworks} 里，能量接收器也在用同一份逻辑。
     */
    @Nullable
    private Network adjacentNetwork() {
        return AdjacentNetworks.find(level, worldPosition);
    }

    /**
     * 本方块六邻域里是否已经贴着一张网络（「摆在网络里」这种用法，不需要绑定接入点）。
     */
    public boolean hasLocalNetwork() {
        return adjacentNetwork() != null;
    }

    private void read(@Nullable final Network network) {
        if (network == null) {
            stored = 0;
            capacity = 0;
            netRate = 0;
            sampledNetwork = null;
            lastSampleGameTime = -1L;
            return;
        }
        if (network != sampledNetwork) {
            sampledNetwork = network;
            lastSampleGameTime = -1L;
        }
        final EnergyNetworkComponent energy = network.getComponent(EnergyNetworkComponent.class);
        stored = energy.getStored();
        capacity = energy.getCapacity();
    }

    private boolean isBelowThreshold(final Config.EnergyMonitorEntry config) {
        if (capacity <= 0) {
            return true;
        }
        return stored / (double) capacity * 100.0 <= Math.max(0, config.getLowThresholdPercent());
    }

    /**
     * 净收支采样：每次采样把这段区间的增量折算成「每秒」。
     *
     * <p>刻意用瞬时值而不是滑动平均——玩家要看的就是「此刻在进电还是在出电」，
     * 抹平了反而看不出机器启停那一下的跳变。
     */
    private void sample() {
        if (!linked) {
            netRate = 0;
            lastSampleGameTime = -1L;
            return;
        }
        final long now = level.getGameTime();
        if (lastSampleGameTime >= 0L) {
            final long elapsed = Math.max(1L, now - lastSampleGameTime);
            netRate = (long) (((double) stored - lastSampleStored) * 20.0 / elapsed);
        } else {
            // 第一次采样只建立基线，下一次才有真实的时间窗口。
            netRate = 0;
        }
        lastSampleStored = stored;
        lastSampleGameTime = now;
    }

    private void consumeFee(@Nullable final Network network, final Config.EnergyMonitorEntry config) {
        if (network == null) {
            return;
        }
        final long fee = feeMeter.next(config.getEnergyUsage());
        if (fee > 0) {
            network.getComponent(EnergyNetworkComponent.class).extract(fee);
            setChanged();
        }
    }

    private void updateState(final boolean newLinked, final boolean newLow) {
        if (linked == newLinked && low == newLow) {
            return;
        }
        linked = newLinked;
        low = newLow;
        final BlockState state = getBlockState();
        if (state.hasProperty(EnergyMonitorBlock.LINKED)) {
            level.setBlockAndUpdate(
                worldPosition,
                state.setValue(EnergyMonitorBlock.LINKED, newLinked)
                    .setValue(EnergyMonitorBlock.LOW, newLow)
            );
        }
    }

    // ------------------------------------------------------------- 状态文案

    /**
     * 右键自报的状态行，由方块拼进聊天栏。
     */
    public Component statusLine() {
        if (!linked) {
            return ContentNames.ENERGY_MONITOR_UNLINKED;
        }
        final long seconds = getSecondsUntilEmpty();
        return Component.translatable(
            ContentNames.ENERGY_MONITOR_STATUS_TRANSLATION_KEY,
            stored,
            capacity,
            netRate,
            seconds < 0 ? ContentNames.ENERGY_MONITOR_NOT_DRAINING : seconds
        );
    }

    // ------------------------------------------------------------- 生命周期

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        final CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_DISPLAY_STORED, stored);
        tag.putLong(TAG_DISPLAY_CAPACITY, capacity);
        tag.putLong(TAG_DISPLAY_RATE, netRate);
        tag.putBoolean(TAG_DISPLAY_LINKED, linked);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        BoundPosition.save(tag, target);
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        target = BoundPosition.load(tag);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        if (tag.contains(TAG_DISPLAY_LINKED)) {
            stored = tag.getLong(TAG_DISPLAY_STORED);
            capacity = tag.getLong(TAG_DISPLAY_CAPACITY);
            netRate = tag.getLong(TAG_DISPLAY_RATE);
            linked = tag.getBoolean(TAG_DISPLAY_LINKED);
        }
    }
}
