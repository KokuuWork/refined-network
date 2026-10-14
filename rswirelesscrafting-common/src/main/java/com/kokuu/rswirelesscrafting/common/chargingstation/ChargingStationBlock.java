package com.kokuu.rswirelesscrafting.common.chargingstation;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 充电站方块。接入 RS 网络靠 {@code AbstractNetworkNodeContainerBlockEntity} 的默认连接策略
 * （六向自动连接），因此贴在线缆旁即可与网络互通。
 *
 * <p>400V 风冷与 800V 液冷是同一个类、同一个方块实体类型的两份实例，只有 {@link #getTier()}
 * 不同；方块实体据此选择自己该读哪一节配置。
 */
public class ChargingStationBlock extends BaseEntityBlock {
    public static final MapCodec<ChargingStationBlock> CODEC = simpleCodec(ChargingStationBlock::new);

    private final ChargingStationTier tier;

    public ChargingStationBlock(final Properties properties) {
        this(properties, ChargingStationTier.AIR_COOLED);
    }

    public ChargingStationBlock(final Properties properties, final ChargingStationTier tier) {
        super(properties);
        this.tier = tier;
    }

    public ChargingStationTier getTier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ChargingStationBlockEntity(pos, state);
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
