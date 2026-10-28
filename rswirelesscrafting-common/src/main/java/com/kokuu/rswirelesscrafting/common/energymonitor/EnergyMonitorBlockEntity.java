package com.kokuu.rswirelesscrafting.common.energymonitor;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.link.BoundPosition;
import com.kokuu.rswirelesscrafting.common.link.BoundTargetResolver;
import com.kokuu.rswirelesscrafting.common.link.LinkSecurity;
import com.kokuu.rswirelesscrafting.common.util.RateMeter;

import java.util.UUID;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
public class EnergyMonitorBlockEntity extends BlockEntity {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final String TAG_OWNER = "Owner";

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
     */
    @Nullable
    private Network adjacentNetwork() {
        for (final Direction direction : DIRECTIONS) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                instanceof NetworkNodeContainerProvider provider) {
                for (final var container : provider.getContainers()) {
                    final Network network = container.getNode().getNetwork();
                    if (network != null) {
                        return network;
                    }
                }
            }
        }
        return null;
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
    }
}
