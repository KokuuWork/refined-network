package com.kokuu.rswirelesscrafting.common.amplifier;

import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Config;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.util.RateMeter;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 无线增幅塔：把「离开网络代价昂贵」这件事变成可以投资的基础设施。
 *
 * <p>塔接入 RS 网络、常驻耗电，只要它还亮着（{@link AmplifierBlock#ACTIVE}），
 * **覆盖半径内**的无线终端距离倍率就打折（默认对折）。跨维度不受影响——那条线留给维度升级卡。
 *
 * <p>实现上刻意做得很轻：塔只维护自己的「激活」方块状态与耗电，距离计算那边查的是
 * {@link AmplifierRegistry}（两端各自在区块加载时登记）+ 方块状态，**不跑同步包、不扫世界**。
 */
public class AmplifierBlockEntity extends AbstractNetworkNodeContainerBlockEntity<AmplifierNetworkNode> {
    private final RateMeter meter = new RateMeter();
    private boolean registered;

    public AmplifierBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getWirelessAmplifier(),
            pos,
            state,
            new AmplifierNetworkNode(Platform.getConfig().getAmplifier().getEnergyUsage())
        );
    }

    /**
     * 方块实体挂到世界上时登记坐标。{@code setLevel} 是服务端与客户端共同的生命周期钩子
     * （1.21.1 的 {@code BlockEntity} 并没有 {@code onLoad} / {@code onChunkUnloaded}），
     * 所以两侧的登记表都会填上，距离倍率在两边算出的结果天然一致。
     */
    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (level != null) {
            AmplifierRegistry.add(level, worldPosition);
            registered = true;
        }
    }

    @Override
    public void setRemoved() {
        unregister();
        super.setRemoved();
    }

    private void unregister() {
        if (registered && level != null) {
            AmplifierRegistry.remove(level, worldPosition);
            registered = false;
        }
    }

    public void tick() {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        final Config.AmplifierEntry config = Platform.getConfig().getAmplifier();
        final Network network = mainNetworkNode.getNetwork();
        final boolean active = config.isEnabled() && network != null && !level.hasNeighborSignal(worldPosition);
        updateActive(active);
        if (active) {
            consume(network, meter.next(config.getEnergyUsage()));
        }
    }

    private void updateActive(final boolean active) {
        final BlockState state = getBlockState();
        if (state.getValue(AmplifierBlock.ACTIVE) != active) {
            // 方块状态是「激活与否」唯一的同步通道：客户端靠它判断塔有没有在罩着自己。
            level.setBlockAndUpdate(worldPosition, state.setValue(AmplifierBlock.ACTIVE, active));
        }
    }

    private void consume(final Network network, final long amount) {
        if (amount <= 0) {
            return;
        }
        network.getComponent(EnergyNetworkComponent.class).extract(amount);
        setChanged();
    }

    /**
     * 供右键查看状态用：塔是否正在工作。
     */
    public boolean isActive() {
        return getBlockState().getValue(AmplifierBlock.ACTIVE);
    }

    /**
     * 供右键查看状态用：本塔覆盖半径。
     */
    public static int coverageRadius() {
        return Platform.getConfig().getAmplifier().getRadius();
    }
}
