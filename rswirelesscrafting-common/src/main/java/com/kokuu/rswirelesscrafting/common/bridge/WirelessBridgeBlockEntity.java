package com.kokuu.rswirelesscrafting.common.bridge;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.link.BoundTargetResolver;
import com.kokuu.rswirelesscrafting.common.link.LinkRegistry;
import com.kokuu.rswirelesscrafting.common.link.LinkSecurity;
import com.kokuu.rswirelesscrafting.common.link.NetworkAttachment;
import com.kokuu.rswirelesscrafting.common.util.RateMeter;

import java.util.UUID;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 无线桥：**一个方块，两种身份**。
 *
 * <ul>
 *   <li>没配对（{@link #isEndpoint()} 为 {@code false}）：它是<b>基点</b>，只要接在网络里就行
 *       ——它本身不做事，只是「被指向」，把网络借给别人。</li>
 *   <li>配对之后：它是<b>据点</b>，把自己挂到基点的网络上（{@link NetworkAttachment}），
 *       于是**网络就延伸到了这里**：旁边贴 RS 原生的外部存储 / 输入总线 / 输出总线就都能用，
 *       和拉一根线过来等价。跨维度也一样。</li>
 * </ul>
 *
 * <p><b>自带区块保活</b>：据点会把自己与基点两端的区块都强加载起来（{@link LinkRegistry}），
 * 否则玩家一离开，远端区块卸载、桥就断了。RS 原生那对「网络变送器 / 网络接收器」要求玩家
 * 自己保证两端加载，这是我们和它的主要区别。
 *
 * <p><b>🚨 关于电的一条铁律</b>：欠费 / 红石挂起时**断的是链路，不是区块**。
 * 区块一旦被放掉，远端方块实体就不再 tick，谁也放不掉它也已经回不来了（死锁）——
 * 所以「省钱」只能靠断开搬运，不能靠撤销保活。恢复供电后下一秒会自动接上。
 *
 * <p><b>耗电</b>= 桥本身（默认 64 FE/s）+ **每保活一个区块的附加**（默认 16 FE/s，配对通常保两端 = 2 个）。
 * 保区块这件事是**明码计价**的，同时在开关上还留了配置总闸与红石挂起两条路。
 *
 * <p><b>节点归属</b>：进入远端网络前，`NetworkAttachment` 会先把节点从本地网络摘下，
 * 避免同一个容器同时留在两张网络里被重复计费。
 */
public class WirelessBridgeBlockEntity extends AbstractNetworkNodeContainerBlockEntity<WirelessBridgeNetworkNode> {
    private static final String TAG_TARGET_DIMENSION = "TargetDimension";
    private static final String TAG_TARGET_POS = "TargetPos";
    private static final String TAG_OWNER = "Owner";
    /**
     * 每 20 tick（1 秒）对齐一次：RS 的图重建可能把我们的容器踢掉，定期补比事件驱动更皮实。
     */
    private static final int SYNC_INTERVAL = 20;

    private final RateMeter meter = new RateMeter();

    @Nullable
    private GlobalPos target;
    @Nullable
    private UUID owner;
    @Nullable
    private NetworkAttachment attachment;
    private int syncCountdown;
    private boolean linked;
    private boolean suspended;
    private boolean unpaid;
    private boolean holdingOwnChunk;
    private boolean holdingTargetChunk;

    public WirelessBridgeBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getWirelessBridge(),
            pos,
            state,
            new WirelessBridgeNetworkNode(Platform.getConfig().getBridge().getEnergyUsage())
        );
    }

    /**
     * 是否已经配对（即是否为「据点」）。
     */
    public boolean isEndpoint() {
        return target != null;
    }

    @Nullable
    public GlobalPos getTarget() {
        return target;
    }

    /**
     * 这个据点是否真的连上了基点的网络（决定方块状态亮不亮）。
     */
    public boolean isLinked() {
        return linked;
    }

    public boolean bindTo(final GlobalPos anchor, final ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        final Network candidate = BoundTargetResolver.resolveNetwork(serverLevel, anchor).orElse(null);
        if (candidate != null && !canUse(player, candidate)) {
            return false;
        }
        owner = player.getUUID();
        detachLink();
        releaseTargetChunk(serverLevel);
        this.target = anchor;
        this.attachment = null;
        this.syncCountdown = 0;
        setLinked(false);
        setChanged();
        return true;
    }

    public void unbind() {
        detachLink();
        if (level instanceof ServerLevel serverLevel) {
            releaseTargetChunk(serverLevel);
        }
        this.target = null;
        this.owner = null;
        this.attachment = null;
        setLinked(false);
        setChanged();
    }

    private boolean canUse(final ServerPlayer player, final Network network) {
        return LinkSecurity.isAllowed(player, BuiltinPermission.OPEN, network)
            && LinkSecurity.isAllowed(player, BuiltinPermission.BUILD, network);
    }

    private boolean isAuthorized(final Network network) {
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return LinkSecurity.isAllowed(owner, BuiltinPermission.OPEN, network, serverLevel)
            && LinkSecurity.isAllowed(owner, BuiltinPermission.BUILD, network, serverLevel);
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Config.BridgeEntry config = Platform.getConfig().getBridge();
        if (target == null) {
            release(serverLevel);
            setLinked(false);
            suspended = false;
            unpaid = false;
            return;
        }
        // 区块保活**只看配置与配对状态**，绝不看电、也绝不看红石：
        // 一旦因为欠费/挂起把区块放掉，远端方块实体就不再 tick，桥永远回不来（死锁）。
        // 所以「省钱」只能靠断链路，不能靠撤区块。
        if (config.isChunkLoadingEnabled()) {
            acquire(serverLevel);
        }
        if (syncCountdown-- <= 0) {
            syncCountdown = SYNC_INTERVAL - 1;
            sync(serverLevel, config);
        }
        consume(config);
    }

    /**
     * 对齐一次：判断要不要（能不能）维持链路。区块保活不在这里处理。
     */
    private void sync(final ServerLevel serverLevel, final Config.BridgeEntry config) {
        suspended = serverLevel.hasNeighborSignal(worldPosition);
        if (!config.isEnabled() || suspended) {
            // 总开关关掉 / 红石挂起：断开链路（省电），区块照旧保活。
            unpaid = false;
            detachLink();
            setLinked(false);
            return;
        }
        final Network anchorNetwork = BoundTargetResolver.resolveNetwork(serverLevel, target).orElse(null);
        if (anchorNetwork == null || !isAuthorized(anchorNetwork)) {
            unpaid = false;
            detachLink();
            setLinked(false);
            return;
        }
        if (!canAfford(anchorNetwork, config)) {
            // 付不起这一档：断开链路，等它恢复。「先看余额再挂」而不是「挂了再扣」，
            // 避免欠费那一秒反复挂断。状态单独标出来，玩家右键就知道是「欠费」不是「没连上」。
            unpaid = true;
            detachLink();
            setLinked(false);
            return;
        }
        unpaid = false;
        attachment(serverLevel).ensureAttached();
        setLinked(attachment(serverLevel).isAttached());
    }

    private boolean canAfford(final Network anchorNetwork, final Config.BridgeEntry config) {
        return anchorNetwork.getComponent(EnergyNetworkComponent.class).getStored() >= perTick(config);
    }

    /**
     * 每秒总耗电 = 桥本身 + 每保活一个区块的附加。
     */
    private long energyPerSecond(final Config.BridgeEntry config) {
        long total = config.getEnergyUsage();
        if (holdingOwnChunk) {
            total += config.getChunkLoadingEnergyUsage();
        }
        if (holdingTargetChunk) {
            total += config.getChunkLoadingEnergyUsage();
        }
        return total;
    }

    private long perTick(final Config.BridgeEntry config) {
        return Math.max(1L, energyPerSecond(config) / 20L);
    }

    private void detachLink() {
        final NetworkAttachment existing = attachment;
        if (existing != null) {
            existing.detach();
        }
    }

    private NetworkAttachment attachment(final ServerLevel serverLevel) {
        final NetworkAttachment existing = attachment;
        if (existing != null && target != null && existing.getTarget().equals(target)) {
            return existing;
        }
        // containers 是 RS 方块实体基类自带的节点容器提供者，我们的主节点就装在它里面。
        final NetworkAttachment created = new NetworkAttachment(
            serverLevel,
            containers,
            target == null ? GlobalPos.of(serverLevel.dimension(), worldPosition) : target
        );
        attachment = created;
        return created;
    }

    private void acquire(final ServerLevel serverLevel) {
        if (!holdingOwnChunk) {
            LinkRegistry.acquire(serverLevel, worldPosition);
            holdingOwnChunk = true;
        }
        if (holdingTargetChunk || target == null) {
            return;
        }
        final ServerLevel targetLevel = serverLevel.getServer().getLevel(target.dimension());
        if (targetLevel != null) {
            LinkRegistry.acquire(targetLevel, target.pos());
            holdingTargetChunk = true;
        }
    }

    private void release(final ServerLevel serverLevel) {
        if (holdingOwnChunk) {
            LinkRegistry.release(serverLevel, worldPosition);
            holdingOwnChunk = false;
        }
        releaseTargetChunk(serverLevel);
    }

    private void releaseTargetChunk(final ServerLevel serverLevel) {
        if (!holdingTargetChunk || target == null) {
            return;
        }
        final ServerLevel targetLevel = serverLevel.getServer().getLevel(target.dimension());
        if (targetLevel != null) {
            LinkRegistry.release(targetLevel, target.pos());
        }
        holdingTargetChunk = false;
    }

    /**
     * 常驻耗电：只按当前**真正持有**的区块计数，能扣多少扣多少，扣不出来也不撤区块。
     */
    private void consume(final Config.BridgeEntry config) {
        if (!config.isEnabled() || !linked || suspended || unpaid) {
            return;
        }
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return;
        }
        final long amount = meter.next(energyPerSecond(config));
        if (amount > 0) {
            network.getComponent(EnergyNetworkComponent.class).extract(amount);
            setChanged();
        }
    }

    /**
     * 是否因为付不起运行费而停运（区块仍在保活，供电恢复即自动接上）。
     */
    public boolean isUnpaid() {
        return unpaid;
    }

    /**
     * 是否被红石信号临时挂起（挂起 = 不搬东西、不耗电，但区块照旧保活）。
     */
    public boolean isSuspended() {
        return suspended;
    }

    private void setLinked(final boolean value) {
        if (linked == value) {
            return;
        }
        linked = value;
        final BlockState state = getBlockState();
        if (state.hasProperty(WirelessBridgeBlock.LINKED)) {
            level.setBlockAndUpdate(worldPosition, state.setValue(WirelessBridgeBlock.LINKED, value));
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            final NetworkAttachment existing = attachment;
            if (existing != null) {
                existing.detach();
            }
            release(serverLevel);
        }
        super.setRemoved();
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        final GlobalPos current = target;
        if (current != null) {
            tag.putString(TAG_TARGET_DIMENSION, current.dimension().location().toString());
            tag.putLong(TAG_TARGET_POS, current.pos().asLong());
        }
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        final String dimension = tag.getString(TAG_TARGET_DIMENSION);
        if (dimension.isEmpty() || !tag.contains(TAG_TARGET_POS)) {
            target = null;
            return;
        }
        final ResourceLocation location = ResourceLocation.tryParse(dimension);
        if (location == null) {
            target = null;
            return;
        }
        target = GlobalPos.of(
            ResourceKey.create(Registries.DIMENSION, location),
            BlockPos.of(tag.getLong(TAG_TARGET_POS))
        );
    }
}
