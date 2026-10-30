package com.kokuu.refinednetwork.common.chargingstation;

import com.kokuu.refinednetwork.common.ContentNames;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
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
 * 终端充电站方块。接入 RS 网络靠 {@code AbstractNetworkNodeContainerBlockEntity} 的默认连接策略
 * （六向自动连接），因此贴在线缆旁即可与网络互通。
 *
 * <p>带两个独立方块状态，把外形拆成两种亮灭：
 * <ul>
 *   <li>{@link #POWERED}——是否通电（有能量来源），电源指示灯亮/灭；</li>
 *   <li>{@link #WIRELESS}——无线充电是否开启（红石信号可关掉），无线指示灯亮/灭。</li>
 * </ul>
 * 两者组合成 4 种形态，玩家不用打开界面就能分别看出供电与无线状态。
 */
public class ChargingStationBlock extends BaseEntityBlock {
    public static final MapCodec<ChargingStationBlock> CODEC = simpleCodec(ChargingStationBlock::new);

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty WIRELESS = BooleanProperty.create("wireless");

    public ChargingStationBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getDefaultState());
    }

    private BlockState getDefaultState() {
        return getStateDefinition().any().setValue(POWERED, true).setValue(WIRELESS, true);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, WIRELESS);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * {@code BaseEntityBlock} 默认返回 {@link RenderShape#INVISIBLE}（原版方块实体都是自绘的，
     * 如箱子、熔炉），所以带方块实体又想用普通 json 模型的方块必须显式声明用模型渲染，
     * 否则方块放下后完全不画，直接看穿到后面的世界。
     */
    @Override
    protected RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ChargingStationBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(final Level level,
                            final BlockPos pos,
                            final BlockState state,
                            @Nullable final LivingEntity placer,
                            final ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player
            && level.getBlockEntity(pos) instanceof ChargingStationBlockEntity station) {
            station.setOwner(player.getUUID());
        }
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
            if (blockEntity instanceof ChargingStationBlockEntity station) {
                station.tick();
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state,
                                               final Level level,
                                               final BlockPos pos,
                                               final Player player,
                                               final BlockHitResult hitResult) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChargingStationBlockEntity station)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (!station.canOpen(serverPlayer)) {
                RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(
                    serverPlayer,
                    ContentNames.TERMINAL_CHARGING_STATION
                );
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            station.setOwner(serverPlayer.getUUID());
            com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
                serverPlayer,
                station
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(final BlockState state,
                            final Level level,
                            final BlockPos pos,
                            final BlockState newState,
                            final boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChargingStationBlockEntity station) {
                Containers.dropContents(level, pos, station);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
