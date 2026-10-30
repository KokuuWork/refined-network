package com.kokuu.refinednetwork.common.energyreceiver;

import com.kokuu.refinednetwork.common.BlockEntities;
import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.DeviceDiagnosticsNames;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.connector.ConnectorTarget;
import com.kokuu.refinednetwork.common.link.AdjacentNetworks;
import com.kokuu.refinednetwork.common.link.BoundPosition;
import com.kokuu.refinednetwork.common.link.BoundTargetResolver;
import com.kokuu.refinednetwork.common.link.LinkDistanceEnergy;
import com.kokuu.refinednetwork.common.link.LinkSecurity;
import com.kokuu.refinednetwork.common.link.NetworkMount;
import com.kokuu.refinednetwork.common.util.RateMeter;
import com.kokuu.refinednetwork.common.util.SaturatedMath;

import java.util.UUID;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 无线能量接收器：**从绑定的网络取电，喂给旁边这张网络**——一根不用铺的电缆。
 *
 * <h2>它解决什么问题</h2>
 * RS 网络唯一的能量入口是控制器，所以「主基地有大电厂、矿场只有空控制器」这种情况，
 * 以前只能拉一条能量线过去。本方块让这件事变成无线：
 * <ol>
 *   <li>把它贴着**要用电的那张网络**放（旁边有控制器或线缆即可）；</li>
 *   <li>用无线连接器把它绑到**有电的那张网络**（比如主基地的控制器）。</li>
 * </ol>
 * 于是它每秒从那头抽一点电存进内部缓冲，同时以 {@code EnergyProvider} 的身份待在本地网络里，
 * 缓冲里的电就是本地网络能用的电。
 *
 * <h2>为什么只挂一边</h2>
 * RS 网络的总电量是所有能量提供者之和。这个节点只挂**本地网络**（{@link NetworkMount}），
 * 它从不把自己挂进供电方那张网——否则同一份缓冲会被两张网同时计入，直接把电刷出来。
 *
 * <h2>链路费与带宽</h2>
 * 带宽 = 容量的 1/{@link #TRANSFER_SECONDS} 每秒，并按距离倍率**变慢**（越远越慢，跨维度 1/16）。
 * 链路费按距离线性收（{@link LinkDistanceEnergy}），从**供电方那张网**扣——远端不是白给。
 */
public class WirelessEnergyReceiverBlockEntity extends BlockEntity implements ConnectorTarget {
    private static final String TAG_BUFFER = "Buffer";
    private static final String TAG_OWNER = "Owner";
    /**
     * 每 20 tick（1 秒）对齐一次：RS 的图重建可能把我们的容器踢掉，定期补比事件驱动更皮实。
     */
    private static final int SYNC_INTERVAL = 20;
    /**
     * 满速时「充满内部缓冲」需要的秒数：带宽 = 容量 / 这个数。
     */
    private static final long TRANSFER_SECONDS = 10;

    private final WirelessEnergyReceiverNetworkNode node;
    private final InWorldNetworkNodeContainer container;
    private final NetworkMount mount;
    private final RateMeter feeMeter = new RateMeter();

    @Nullable
    private GlobalPos target;
    @Nullable
    private UUID owner;
    /**
     * 供电方那张网（绑定的网络）。每 {@link #SYNC_INTERVAL} tick 重解析一次。
     */
    @Nullable
    private Network remoteNetwork;
    private long buffer;
    private int syncCountdown;
    private boolean linked;
    /**
     * 最近一次对齐失败的原因；只用于右键状态行，不参与任何逻辑判定。
     */
    private OfflineReason offlineReason = OfflineReason.NONE;

    public WirelessEnergyReceiverBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getWirelessEnergyReceiver(), pos, state);
        this.node = new WirelessEnergyReceiverNetworkNode(this);
        this.container = RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, node).build();
        this.mount = new NetworkMount(container);
    }

    // ------------------------------------------------------------- 状态查询

    public boolean isEndpoint() {
        return target != null;
    }

    @Nullable
    public GlobalPos getTarget() {
        return target;
    }

    /**
     * 是否真的挂上了绑定网络（决定方块亮不亮）。
     */
    public boolean isLinked() {
        return linked;
    }

    public long getBuffer() {
        return buffer;
    }

    public long getCapacity() {
        return Platform.getConfig().getEnergyReceiver().getCapacity();
    }

    /**
     * 当前每秒链路费（供 tooltip 与右键状态展示）。未绑定时为 0。
     */
    public long getFeePerSecond() {
        final GlobalPos current = target;
        if (current == null || level == null) {
            return 0;
        }
        return feePerSecond();
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
        detachLink();
        this.target = anchor;
        this.syncCountdown = 0;
        setLinked(false);
        setChanged();
        return true;
    }

    public void unbind() {
        detachLink();
        this.target = null;
        this.owner = null;
        setLinked(false);
        setChanged();
    }

    @Override
    public boolean isBound() {
        return isEndpoint();
    }

    @Override
    public Component boundMessage() {
        return ContentNames.ENERGY_RECEIVER_BOUND;
    }

    @Override
    public Component unboundMessage() {
        return ContentNames.ENERGY_RECEIVER_UNBOUND;
    }

    private boolean isAuthorized(final Network network) {
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return LinkSecurity.isAllowed(owner, BuiltinPermission.OPEN, network, serverLevel);
    }

    // ------------------------------------------------------------- tick

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Config.EnergyReceiverEntry config = Platform.getConfig().getEnergyReceiver();
        final GlobalPos current = target;
        if (current == null) {
            detachLink();
            setLinked(false);
            return;
        }
        if (!config.isEnabled() || serverLevel.hasNeighborSignal(worldPosition)) {
            // 配置总闸关掉 / 红石挂起：断开链路、停止收费，缓冲里的电留着。
            detachLink();
            setLinked(false);
            return;
        }
        if (syncCountdown-- <= 0) {
            syncCountdown = SYNC_INTERVAL - 1;
            sync(serverLevel);
        }
        if (linked) {
            // 挂载每 20 tick 对齐一次，抽电是每 tick 连续的。
            pull();
            chargeFee();
        }
    }

    private void sync(final ServerLevel serverLevel) {
        final Network remote = BoundTargetResolver.resolveNetwork(serverLevel, target).orElse(null);
        if (remote == null) {
            fail(OfflineReason.UNREACHABLE);
            return;
        }
        if (!isAuthorized(remote)) {
            fail(OfflineReason.NO_PERMISSION);
            return;
        }
        final Network local = AdjacentNetworks.find(serverLevel, worldPosition);
        if (local == null) {
            fail(OfflineReason.NO_LOCAL_NETWORK);
            return;
        }
        if (local == remote) {
            // 绑定的就是自己旁边这张网：等于自己给自己充电，一点新电都不会产生。
            fail(OfflineReason.SELF_BOUND);
            return;
        }
        remoteNetwork = remote;
        mount.mount(local);
        setLinked(mount.isMounted());
        offlineReason = mount.isMounted() ? OfflineReason.NONE : OfflineReason.MOUNT_REJECTED;
    }

    private void fail(final OfflineReason reason) {
        detachLink();
        setLinked(false);
        offlineReason = reason;
    }

    /**
     * 从供电方那张网抽电，存进内部缓冲；缓冲再以 {@code EnergyProvider} 的身份供本地网络使用。
     */
    private void pull() {
        final Network remote = remoteNetwork;
        if (remote == null || buffer >= getCapacity()) {
            return;
        }
        final long want = Math.min(getCapacity() - buffer, transferPerTick());
        if (want <= 0) {
            return;
        }
        final EnergyNetworkComponent energy = remote.getComponent(EnergyNetworkComponent.class);
        final long stored = energy.getStored();
        if (stored <= 0) {
            return;
        }
        final long gained = energy.extract(Math.min(want, stored));
        if (gained > 0) {
            buffer = Math.min(getCapacity(), buffer + gained);
            setChanged();
        }
    }

    /**
     * 每 tick 的搬运上限：容量 / {@link #TRANSFER_SECONDS} / 20，再按距离倍率**变慢**
     * （越远越慢，跨维度倍率 16 就是 1/16 带宽）。
     */
    private long transferPerTick() {
        final GlobalPos current = target;
        if (current == null || level == null) {
            return 0;
        }
        final long perSecond = Math.max(1L, getCapacity() / TRANSFER_SECONDS);
        final long percent = Math.max(100L, LinkDistanceEnergy.multiplierPercent(level, worldPosition, current));
        return Math.max(1L, perSecond * 100L / percent / 20L);
    }

    /**
     * 链路费从**供电方那张网**扣：电是它出的，钱也该它出。
     */
    private void chargeFee() {
        final Network remote = remoteNetwork;
        if (remote == null) {
            return;
        }
        final long fee = feeMeter.next(feePerSecond());
        if (fee > 0) {
            remote.getComponent(EnergyNetworkComponent.class).extract(fee);
            setChanged();
        }
    }

    private long feePerSecond() {
        final GlobalPos current = target;
        if (current == null || level == null) {
            return 0;
        }
        final long percent = LinkDistanceEnergy.multiplierPercent(level, worldPosition, current);
        return Math.max(1L, SaturatedMath.multiplyPercent(
            Platform.getConfig().getEnergyReceiver().getEnergyUsage(),
            percent
        ));
    }

    private void detachLink() {
        mount.detach();
        remoteNetwork = null;
    }

    private void setLinked(final boolean value) {
        if (linked == value) {
            return;
        }
        linked = value;
        final BlockState state = getBlockState();
        if (state.hasProperty(WirelessEnergyReceiverBlock.LINKED)) {
            level.setBlockAndUpdate(worldPosition, state.setValue(WirelessEnergyReceiverBlock.LINKED, value));
        }
    }

    // ------------------------------------------------------ 外部 FE 输入

    /**
     * 接收其他模组以标准 Forge Energy 输入的能量，存进内部缓冲。
     *
     * @return 实际接收量
     */
    public long receiveExternalEnergy(final long amount, final boolean simulate) {
        if (amount <= 0 || !canReceiveExternalEnergy()) {
            return 0;
        }
        final long accepted = Math.min(amount, Math.max(0, getCapacity() - buffer));
        if (!simulate && accepted > 0) {
            buffer += accepted;
            setChanged();
        }
        return accepted;
    }

    /**
     * Fabric 事务回滚时恢复快照；不把回滚路径伪装成一次新的外部充能。
     */
    public void restoreBuffer(final long value) {
        buffer = Math.max(0L, Math.min(value, getCapacity()));
        setChanged();
    }

    private boolean canReceiveExternalEnergy() {
        return level instanceof ServerLevel serverLevel
            && Platform.getConfig().getEnergyReceiver().isEnabled()
            && !serverLevel.hasNeighborSignal(worldPosition);
    }

    /**
     * 网络向本节点要电（{@code EnergyProvider#extract}）。
     *
     * @return 实际交出的电量
     */
    public long drainBuffer(final long amount) {
        if (amount <= 0 || buffer <= 0) {
            return 0;
        }
        final long given = Math.min(amount, buffer);
        buffer -= given;
        setChanged();
        return given;
    }

    // ------------------------------------------------------------- 状态文案

    /**
     * 右键自报的状态行（含位置与链路费），由方块拼进聊天栏。
     */
    public Component statusLine() {
        final GlobalPos current = target;
        if (current == null) {
            return ContentNames.ENERGY_RECEIVER_UNBOUND;
        }
        final Component status = Component.translatable(
            ContentNames.ENERGY_RECEIVER_STATUS_TRANSLATION_KEY,
            current.pos().toShortString(),
            linked ? ContentNames.ENERGY_RECEIVER_ONLINE : ContentNames.ENERGY_RECEIVER_OFFLINE,
            getFeePerSecond()
        );
        return linked ? status : status.copy().append(Component.literal("\n")).append(offlineReason.describe());
    }

    // ------------------------------------------------------------- 生命周期

    @Override
    public void setRemoved() {
        detachLink();
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong(TAG_BUFFER, buffer);
        BoundPosition.save(tag, target);
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // 旧存档可能在容量下调前存入了更大的电量，clamp 到当前容量，避免显示超容量。
        buffer = Math.min(tag.getLong(TAG_BUFFER), getCapacity());
        target = BoundPosition.load(tag);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
    }

    /**
     * 未工作的原因，仅用于状态行展示。
     */
    public enum OfflineReason {
        NONE,
        UNREACHABLE,
        NO_PERMISSION,
        NO_LOCAL_NETWORK,
        SELF_BOUND,
        MOUNT_REJECTED;

        Component describe() {
            return switch (this) {
                case UNREACHABLE -> DeviceDiagnosticsNames.UNREACHABLE;
                case NO_PERMISSION -> DeviceDiagnosticsNames.NO_PERMISSION;
                case NO_LOCAL_NETWORK -> DeviceDiagnosticsNames.NO_LOCAL_NETWORK;
                case SELF_BOUND -> DeviceDiagnosticsNames.SELF_BOUND;
                case MOUNT_REJECTED -> DeviceDiagnosticsNames.MOUNT_REJECTED;
                case NONE -> Component.empty();
            };
        }
    }
}
