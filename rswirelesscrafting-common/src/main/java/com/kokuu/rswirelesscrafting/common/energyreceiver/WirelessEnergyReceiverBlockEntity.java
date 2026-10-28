package com.kokuu.rswirelesscrafting.common.energyreceiver;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.link.BoundPosition;
import com.kokuu.rswirelesscrafting.common.link.BoundTargetResolver;
import com.kokuu.rswirelesscrafting.common.link.LinkDistanceEnergy;
import com.kokuu.rswirelesscrafting.common.link.LinkSecurity;
import com.kokuu.rswirelesscrafting.common.link.NetworkAttachment;
import com.kokuu.rswirelesscrafting.common.util.RateMeter;
import com.kokuu.rswirelesscrafting.common.util.SaturatedMath;

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
 * 无线能量接收器：把**远处**的 FE 发电接进 RS 网络。
 *
 * <h2>它解决什么问题</h2>
 * RS 网络唯一的能量入口是控制器（能量能力只注册在控制器和便携终端上），
 * 所以「下界岩浆机 → 主基地网络」只能拉一条能量线回来。本方块让这件事变成无线：
 * 贴在发电机旁边收电，再以「网络能量提供者」的身份把电送进绑定的那张网络。
 *
 * <h2>怎么做到的</h2>
 * RS 网络的总电量是所有 {@code EnergyProvider} 之和，而 {@code EnergyNetworkComponent}
 * **只提供 extract、不提供 insert**——所以「给网络充电」唯一的正道就是成为它的提供者。
 * 我们把节点挂进绑定的网络（{@link NetworkAttachment}），它的缓冲就算进了网络电量池，
 * 网络里任何机器用电都会回头向它 {@code extract}。
 *
 * <h2>🚨 为什么不继承 RS 的方块实体基类</h2>
 * 基类会在 {@code clearRemoved()} 里把容器交给 RS 的建图器，于是方块只要挨着线缆就会
 * **自动加入本地网络**。对一个会向网络供电的节点来说，同时待在本网和远端网里意味着
 * 同一份缓冲被两张网络同时计入——那是刷电漏洞。所以这里自己造容器
 * （{@code createNetworkNodeContainer}），只往绑定的网络里挂，从根上杜绝重复计入。
 *
 * <h2>链路费</h2>
 * 按距离线性收（与手持终端同一套口径，见 {@link LinkDistanceEnergy}）：基础值
 * {@code energyUsage} FE/s，越远越贵，跨维度按维度倍率，增幅塔覆盖内打折。
 * 费用从**绑定的网络**里扣——所以远端发电不是白嫖的，距离仍然是成本。
 */
public class WirelessEnergyReceiverBlockEntity extends BlockEntity {
    private static final String TAG_BUFFER = "Buffer";
    private static final String TAG_OWNER = "Owner";
    /**
     * 每 20 tick（1 秒）对齐一次：RS 的图重建可能把我们的容器踢掉，定期补比事件驱动更皮实。
     */
    private static final int SYNC_INTERVAL = 20;

    private final WirelessEnergyReceiverNetworkNode node;
    private final InWorldNetworkNodeContainer container;
    private final RateMeter feeMeter = new RateMeter();

    @Nullable
    private GlobalPos target;
    @Nullable
    private UUID owner;
    @Nullable
    private NetworkAttachment attachment;
    private long buffer;
    private int syncCountdown;
    private boolean linked;

    public WirelessEnergyReceiverBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getWirelessEnergyReceiver(), pos, state);
        this.node = new WirelessEnergyReceiverNetworkNode(this);
        this.container = RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, node).build();
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
        this.attachment = null;
        this.syncCountdown = 0;
        setLinked(false);
        setChanged();
        return true;
    }

    public void unbind() {
        detachLink();
        this.target = null;
        this.owner = null;
        this.attachment = null;
        setLinked(false);
        setChanged();
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
            sync(serverLevel, config);
        }
        if (linked) {
            chargeFee(config);
        }
    }

    private void sync(final ServerLevel serverLevel, final Config.EnergyReceiverEntry config) {
        final Network anchorNetwork = BoundTargetResolver.resolveNetwork(serverLevel, target).orElse(null);
        if (anchorNetwork == null || !isAuthorized(anchorNetwork)) {
            detachLink();
            setLinked(false);
            return;
        }
        attachment(serverLevel).ensureAttached();
        setLinked(attachment(serverLevel).isAttached());
    }

    /**
     * 链路费从**绑定的网络**扣：扣不出来也照常挂着（缓冲里的电网络照样能用），
     * 只是这一秒不收钱——和无线桥「先看余额再挂」不同，这里没有必要把链路断掉，
     * 因为断链路等于把电掐了，比不收钱更糟。
     */
    private void chargeFee(final Config.EnergyReceiverEntry config) {
        final NetworkAttachment current = attachment;
        if (current == null) {
            return;
        }
        final Network network = current.getAttachedNetwork();
        if (network == null) {
            return;
        }
        final long fee = feeMeter.next(feePerSecond());
        if (fee > 0) {
            network.getComponent(EnergyNetworkComponent.class).extract(fee);
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

    private NetworkAttachment attachment(final ServerLevel serverLevel) {
        final NetworkAttachment existing = attachment;
        if (existing != null && target != null && existing.getTarget().equals(target)) {
            return existing;
        }
        final NetworkAttachment created = new NetworkAttachment(
            serverLevel,
            container,
            target == null ? GlobalPos.of(serverLevel.dimension(), worldPosition) : target
        );
        attachment = created;
        return created;
    }

    private void detachLink() {
        final NetworkAttachment existing = attachment;
        if (existing != null) {
            existing.detach();
        }
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
        return Component.translatable(
            ContentNames.ENERGY_RECEIVER_STATUS_TRANSLATION_KEY,
            current.pos().toShortString(),
            linked ? ContentNames.ENERGY_RECEIVER_ONLINE : ContentNames.ENERGY_RECEIVER_OFFLINE,
            getFeePerSecond()
        );
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
}
