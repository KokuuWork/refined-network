package com.kokuu.rswirelesscrafting.common.bridge;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.link.LinkRegistry;
import com.kokuu.rswirelesscrafting.common.link.NetworkAttachment;
import com.kokuu.rswirelesscrafting.common.util.RateMeter;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
 * <p><b>🚨 关于电的一条铁律</b>：<b>绝不会因为没电而断开</b>。断开 → 区块释放 → 方块实体不再 tick
 * → 再也没机会重新连上（死锁）。所以耗电是「能扣多少扣多少」的常驻负载，桥本身只在
 * 拆掉 / 解绑时释放区块。
 *
 * <p><b>一个已知边界</b>：据点如果**正好贴着线缆**，RS 的图会把它连到身边那张网络上，
 * 而这里又把它挂到基点的网络上，两边会轮流改写节点归属（每秒翻转一次）。实际用法里据点都在
 * 很远的地方、身边没有线缆，所以不处理；真要贴着线缆放，那就没必要用桥了。
 */
public class WirelessBridgeBlockEntity extends AbstractNetworkNodeContainerBlockEntity<WirelessBridgeNetworkNode> {
    private static final String TAG_TARGET_DIMENSION = "TargetDimension";
    private static final String TAG_TARGET_POS = "TargetPos";
    /**
     * 每 20 tick（1 秒）对齐一次：RS 的图重建可能把我们的容器踢掉，定期补比事件驱动更皮实。
     */
    private static final int SYNC_INTERVAL = 20;

    private final RateMeter meter = new RateMeter();

    @Nullable
    private GlobalPos target;
    @Nullable
    private NetworkAttachment attachment;
    private int syncCountdown;
    private boolean linked;
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

    public void bindTo(final GlobalPos anchor) {
        this.target = anchor;
        this.attachment = null;
        this.syncCountdown = 0;
        setChanged();
    }

    public void unbind() {
        this.target = null;
        this.attachment = null;
        setChanged();
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Config.BridgeEntry config = Platform.getConfig().getBridge();
        if (!config.isEnabled() || target == null) {
            // 基点（或功能被配置关掉）：什么都没连，确保不占着区块。
            release(serverLevel);
            setLinked(false);
            return;
        }
        if (syncCountdown-- <= 0) {
            syncCountdown = SYNC_INTERVAL - 1;
            sync(serverLevel, config);
        }
        consume();
    }

    /**
     * 对齐一次：两端区块保活 + 把自己挂到基点的网络上。
     */
    private void sync(final ServerLevel serverLevel, final Config.BridgeEntry config) {
        if (config.isChunkLoadingEnabled()) {
            acquire(serverLevel);
        }
        attachment(serverLevel).ensureAttached();
        setLinked(attachment(serverLevel).isAttached());
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
        if (holdingTargetChunk && target != null) {
            final MinecraftServer server = serverLevel.getServer();
            final ServerLevel targetLevel = server.getLevel(target.dimension());
            if (targetLevel != null) {
                LinkRegistry.release(targetLevel, target.pos());
            }
            holdingTargetChunk = false;
        }
    }

    /**
     * 常驻耗电：能扣多少扣多少，**绝不因为欠费而断开**（见类注释里的死锁说明）。
     */
    private void consume() {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return;
        }
        final long amount = meter.next(Platform.getConfig().getBridge().getEnergyUsage());
        if (amount > 0) {
            network.getComponent(EnergyNetworkComponent.class).extract(amount);
            setChanged();
        }
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
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
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
