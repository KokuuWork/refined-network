package com.kokuu.refinednetwork.common.energymonitor;

import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.link.PairingClipboard;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 能量监控仪方块。三个外形状态，都是「看一眼就知道网怎么样」：
 * <ul>
 *   <li>灭 = 没找到网络（没贴线缆、也没绑定）；</li>
 *   <li>亮 = 网络正常；</li>
 *   <li>红 = 电量低于阈值（{@code lowThresholdPercent}）。</li>
 * </ul>
 *
 * <p>比较器输出按电量充盈度 0–15，所以它也能直接当「网络电量传感器」拉红石线用。
 *
 * <p>右键交互走聊天栏：潜行右键 = 绑定 / 解绑，直接右键 = 报一遍存量、容量、净收支、预计耗尽。
 */
public class EnergyMonitorBlock extends BaseEntityBlock {
    public static final MapCodec<EnergyMonitorBlock> CODEC = simpleCodec(EnergyMonitorBlock::new);
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");
    public static final BooleanProperty LOW = BooleanProperty.create("low");

    public EnergyMonitorBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
            .setValue(LINKED, false)
            .setValue(LOW, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LINKED, LOW);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        return level.getBlockEntity(pos) instanceof EnergyMonitorBlockEntity monitor
            ? monitor.getAnalogOutput()
            : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new EnergyMonitorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(final Level level,
                            final BlockPos pos,
                            final BlockState state,
                            @Nullable final LivingEntity placer,
                            final ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof Player player)
            || !(level.getBlockEntity(pos) instanceof EnergyMonitorBlockEntity monitor)) {
            return;
        }
        monitor.setOwner(player.getUUID());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof EnergyMonitorBlockEntity monitor) {
                monitor.tick();
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state,
                                               final Level level,
                                               final BlockPos pos,
                                               final Player player,
                                               final BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof EnergyMonitorBlockEntity monitor) {
            if (player.isCrouching()) {
                crouchUse(serverPlayer, monitor);
            } else {
                serverPlayer.displayClientMessage(monitor.statusLine(), false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void crouchUse(final ServerPlayer player, final EnergyMonitorBlockEntity monitor) {
        if (monitor.isEndpoint()) {
            monitor.unbind();
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(ContentNames.ENERGY_MONITOR_UNBOUND, false);
            return;
        }
        if (PairingClipboard.getAnchor(player).isPresent()) {
            final GlobalPos anchor = PairingClipboard.getAnchor(player).orElseThrow();
            if (!monitor.bindTo(anchor, player)) {
                RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.ENERGY_MONITOR);
                PairingClipboard.clearAnchor(player);
                return;
            }
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(
                Component.translatable(
                    ContentNames.ENERGY_MONITOR_BOUND_TRANSLATION_KEY,
                    monitor.getTarget() == null ? "?" : monitor.getTarget().pos().toShortString()
                ),
                false
            );
            return;
        }
        if (monitor.hasLocalNetwork()) {
            // 已经贴着网络了，这时候再要求「先记录接入点」纯属误导。
            player.displayClientMessage(ContentNames.ENERGY_MONITOR_LOCAL_NETWORK, false);
            return;
        }
        player.displayClientMessage(ContentNames.LINK_ANCHOR_REQUIRED, false);
    }

}
