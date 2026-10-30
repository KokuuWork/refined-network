package com.kokuu.refinednetwork.common.networkbridge;

import com.kokuu.refinednetwork.common.BlockEntities;
import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.connector.ConnectorTarget;
import com.kokuu.refinednetwork.common.link.BoundPosition;
import com.kokuu.refinednetwork.common.link.BoundTargetResolver;
import com.kokuu.refinednetwork.common.link.LinkDistanceEnergy;
import com.kokuu.refinednetwork.common.link.LinkSecurity;
import com.kokuu.refinednetwork.common.util.RateLimiter;
import com.kokuu.refinednetwork.common.util.RateMeter;
import com.kokuu.refinednetwork.common.util.SaturatedMath;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 网络桥接器：让**两张各自独立的 RS 网络互通**。
 *
 * <h2>和无线桥的区别</h2>
 * 无线桥是「把一个网络延伸过去」——据点的节点直接搬进远端网络，那边贴条总线就能当本地用。
 * 网络桥接器反过来：两张网**各自保持独立**（各自的控制器、权限、耗电统计都不动），
 * 只是让 A 网能看见并取用 B 网的库存与电量。想反向就再放一座指着回来的桥接器。
 *
 * <h2>怎么做到的</h2>
 * 节点同时实现 {@code StorageProvider} 与 {@code EnergyProvider}：
 * RS 网络的总存量是所有存储提供者之和、总电量是所有能量提供者之和，
 * 而 {@code EnergyNetworkComponent} **只有 extract、没有 insert**——
 * 所以「把电送给另一张网」也必须靠成为它的提供者，没有第二条路。
 * 两个提供者都转发到绑定的那张网络上（见 {@link BridgedStorage}）。
 *
 * <h2>限速</h2>
 * 转发前先扣每秒额度（{@link RateLimiter}）：{@code itemTransferRate} 件/秒、
 * {@code energyTransferRate} FE/s。超额的那部分直接返回 0，
 * 调用方会以为「那边满了 / 没货」——属于正常的失败语义，不会卡死界面。
 *
 * <h2>链路费</h2>
 * 按距离线性收（与手持终端同一套口径，见 {@link LinkDistanceEnergy}），
 * 从**本地网络**扣：跨网搬运不是免费的，距离仍然是成本。
 */
public class NetworkBridgeBlockEntity extends AbstractNetworkNodeContainerBlockEntity<NetworkBridgeNetworkNode>
    implements NetworkNodeContainerProvider, NetworkItemTargetBlockEntity, ConnectorTarget {
    private static final int SYNC_INTERVAL = 20;
    private static final String TAG_OWNER = "Owner";

    private final RateLimiter itemLimiter;
    private final RateLimiter energyLimiter;
    private final RateMeter feeMeter = new RateMeter();

    @Nullable
    private GlobalPos target;
    @Nullable
    private UUID owner;
    @Nullable
    private Network remote;
    private int syncCountdown;
    private boolean linked;
    /** 检测到桥接环后永久停用本次绑定，直到玩家重新绑定或切换配置。 */
    private boolean cyclic;

    public NetworkBridgeBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getNetworkBridge(), pos, state, new NetworkBridgeNetworkNode());
        final Config.NetworkBridgeEntry config = Platform.getConfig().getNetworkBridge();
        this.itemLimiter = new RateLimiter(config.getItemTransferRate());
        this.energyLimiter = new RateLimiter(config.getEnergyTransferRate());
        // Java 不允许把 this 传进 super(...)，所以节点在构造末期才补上对方块实体的引用。
        // 节点的提供者方法只会在「容器加入网络」时被调用，那时候早就补好了。
        mainNetworkNode.attach(this);
    }

    @Override
    public Set<InWorldNetworkNodeContainer> getContainers() {
        return containers.getContainers();
    }

    @Override
    public void addContainer(final InWorldNetworkNodeContainer container) {
        containers.addContainer(container);
    }

    @Override
    public boolean canBuild(final ServerPlayer player) {
        return true;
    }

    @Nullable
    @Override
    public Network getNetworkForItem() {
        return mainNetworkNode.getNetwork();
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

    public long getGameTime() {
        return level == null ? 0L : level.getGameTime();
    }

    RateLimiter getItemLimiter() {
        updateLimiters();
        return itemLimiter;
    }

    private void updateLimiters() {
        final Config.NetworkBridgeEntry config = Platform.getConfig().getNetworkBridge();
        itemLimiter.setPerSecond(config.getItemTransferRate());
        energyLimiter.setPerSecond(config.getEnergyTransferRate());
    }

    /**
     * 物品侧这一秒还剩多少可搬运的额度。
     */
    public long remainingItemBudget() {
        updateLimiters();
        return itemLimiter.remaining(getGameTime());
    }

    /**
     * 记下一笔真正放行的搬运量。
     */
    public void consumeItemBudget(final long amount) {
        updateLimiters();
        itemLimiter.consume(amount, getGameTime());
    }

    /**
     * 被转发的那张网络（每 {@link #SYNC_INTERVAL} tick 重新解析一次）。
     */
    @Nullable
    public Network getRemoteNetwork() {
        return cyclic ? null : remote;
    }

    boolean isCyclic() {
        return cyclic;
    }

    /**
     * 由能量/存储转发路径调用：把环标记出来，后续读取一律返回空，避免重复计入。
     * 不在这里直接改方块状态，避免在 RS 遍历提供者的过程中触发图重建。
     */
    void markCyclic() {
        cyclic = true;
        remote = null;
    }

    // ------------------------------------------------------------- 绑定

    public boolean bindTo(final GlobalPos anchor, final ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        final Network candidate = BoundTargetResolver.resolveNetwork(serverLevel, anchor).orElse(null);
        if (candidate != null && !canTransfer(player, candidate)) {
            return false;
        }
        this.owner = player.getUUID();
        this.target = anchor;
        this.remote = null;
        this.cyclic = false;
        this.syncCountdown = 0;
        setChanged();
        return true;
    }

    public void unbind() {
        this.target = null;
        this.owner = null;
        this.remote = null;
        this.cyclic = false;
        setChanged();
    }

    @Override
    public boolean isBound() {
        return isEndpoint();
    }

    @Override
    public Component boundMessage() {
        return Component.translatable(
            ContentNames.NETWORK_BRIDGE_BOUND_TRANSLATION_KEY,
            target == null ? "?" : target.pos().toShortString()
        );
    }

    @Override
    public Component unboundMessage() {
        return ContentNames.NETWORK_BRIDGE_UNBOUND;
    }

    private boolean canTransfer(final ServerPlayer player, final Network network) {
        return LinkSecurity.isAllowed(player, BuiltinPermission.EXTRACT, network)
            && LinkSecurity.isAllowed(player, BuiltinPermission.INSERT, network);
    }

    private boolean isAuthorized(final Network network) {
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return LinkSecurity.isAllowed(owner, BuiltinPermission.EXTRACT, network, serverLevel)
            && LinkSecurity.isAllowed(owner, BuiltinPermission.INSERT, network, serverLevel);
    }

    // ------------------------------------------------------------- tick

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Config.NetworkBridgeEntry config = Platform.getConfig().getNetworkBridge();
        final GlobalPos current = target;
        if (current == null) {
            remote = null;
            cyclic = false;
            setLinked(false);
            return;
        }
        if (!config.isEnabled() || serverLevel.hasNeighborSignal(worldPosition)) {
            // 配置总闸 / 红石挂起：断开与远端网络的联系，停止转发与收费。
            remote = null;
            cyclic = false;
            setLinked(false);
            return;
        }
        if (syncCountdown-- <= 0) {
            syncCountdown = SYNC_INTERVAL - 1;
            sync(serverLevel, current);
        }
        setLinked(remote != null && !cyclic);
        if (linked && !chargeFee(config)) {
            // 欠费时断开转发，保留下一次同步重试；不向玩家提供免费桥接。
            remote = null;
            setLinked(false);
        }
    }

    private void sync(final ServerLevel serverLevel, final GlobalPos current) {
        final Network candidate = BoundTargetResolver.resolveNetwork(serverLevel, current).orElse(null);
        if (candidate != null && candidate == mainNetworkNode.getNetwork()) {
            // 自指桥必然让网络读取自己的提供者，直接拒绝，不进入递归。
            remote = null;
            cyclic = true;
        } else if (!cyclic) {
            remote = candidate != null && isAuthorized(candidate) ? candidate : null;
        }
    }

    /**
     * 链路费从**本地网络**扣。余额不足时本次同步失败，下一周期重新尝试。
     */
    private boolean chargeFee(final Config.NetworkBridgeEntry config) {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return false;
        }
        final long fee = feeMeter.next(feePerSecond());
        if (fee <= 0) {
            return true;
        }
        final var energy = network.getComponent(EnergyNetworkComponent.class);
        if (energy.getStored() < fee || energy.extract(fee) != fee) {
            return false;
        }
        setChanged();
        return true;
    }

    private long feePerSecond() {
        final GlobalPos current = target;
        if (current == null || level == null) {
            return 0;
        }
        final long percent = LinkDistanceEnergy.multiplierPercent(level, worldPosition, current);
        return Math.max(1L, SaturatedMath.multiplyPercent(
            Platform.getConfig().getNetworkBridge().getEnergyUsage(),
            percent
        ));
    }

    // -------------------------------------------------- 能量侧转发（供节点调用）

    public long getRemoteEnergyStored() {
        final Network current = getRemoteNetwork();
        if (current == null) {
            return 0;
        }
        final Long stored = BridgeRecursionGuard.run(
            current,
            () -> current.getComponent(EnergyNetworkComponent.class).getStored()
        );
        if (stored == null || cyclic) {
            markCyclic();
            return 0;
        }
        return stored;
    }

    public long getRemoteEnergyCapacity() {
        final Network current = getRemoteNetwork();
        if (current == null) {
            return 0;
        }
        final Long capacity = BridgeRecursionGuard.run(
            current,
            () -> current.getComponent(EnergyNetworkComponent.class).getCapacity()
        );
        if (capacity == null || cyclic) {
            markCyclic();
            return 0;
        }
        return capacity;
    }

    /**
     * 本地网络向远端网络要电：按桥的能量限速放行。
     */
    public long extractRemoteEnergy(final long amount) {
        final Network current = getRemoteNetwork();
        if (current == null || amount <= 0) {
            return 0;
        }
        updateLimiters();
        final long budget = energyLimiter.remaining(getGameTime());
        if (budget <= 0) {
            return 0;
        }
        final Long moved = BridgeRecursionGuard.run(
            current,
            () -> current.getComponent(EnergyNetworkComponent.class).extract(Math.min(amount, budget))
        );
        if (moved == null || cyclic) {
            markCyclic();
            return 0;
        }
        if (moved > 0) {
            energyLimiter.consume(moved, getGameTime());
        }
        return moved;
    }

    // ------------------------------------------------------------- 状态文案

    /**
     * 右键自报的状态行，由方块拼进聊天栏。
     */
    public Component statusLine() {
        final GlobalPos current = target;
        if (current == null) {
            return ContentNames.NETWORK_BRIDGE_UNBOUND;
        }
        return Component.translatable(
            ContentNames.NETWORK_BRIDGE_STATUS_TRANSLATION_KEY,
            current.pos().toShortString(),
            cyclic
                ? ContentNames.NETWORK_BRIDGE_CYCLE
                : linked ? ContentNames.NETWORK_BRIDGE_ONLINE : ContentNames.NETWORK_BRIDGE_OFFLINE,
            Platform.getConfig().getNetworkBridge().getItemTransferRate(),
            Platform.getConfig().getNetworkBridge().getEnergyTransferRate(),
            feePerSecond()
        );
    }

    // ------------------------------------------------------------- 生命周期

    private void setLinked(final boolean value) {
        if (linked == value) {
            return;
        }
        linked = value;
        final BlockState state = getBlockState();
        if (state.hasProperty(NetworkBridgeBlock.LINKED)) {
            level.setBlockAndUpdate(worldPosition, state.setValue(NetworkBridgeBlock.LINKED, value));
        }
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
    }
}
