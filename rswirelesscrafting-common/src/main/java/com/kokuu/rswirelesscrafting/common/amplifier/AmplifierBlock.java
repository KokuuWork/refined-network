package com.kokuu.rswirelesscrafting.common.amplifier;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * 无线增幅塔方块。接在 RS 网络上、常驻耗电，覆盖半径内的无线终端距离倍率打折。
 *
 * <p>外形只有「亮 / 灭」两态（{@link #ACTIVE}）：亮着 = 正在罩着这一片，
 * 玩家不用打开任何界面就能看出塔有没有在工作。红石信号可以让它临时停机省电。
 */
public class AmplifierBlock extends BaseEntityBlock {
    public static final MapCodec<AmplifierBlock> CODEC = simpleCodec(AmplifierBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public AmplifierBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getDefaultState());
    }

    private BlockState getDefaultState() {
        return getStateDefinition().any().setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new AmplifierBlockEntity(pos, state);
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
            if (blockEntity instanceof AmplifierBlockEntity amplifier) {
                amplifier.tick();
            }
        };
    }

    /**
     * 右键自报状态：塔没有界面，「它到底有没有在工作」得能一眼查出来。
     */
    @Override
    protected InteractionResult useWithoutItem(final BlockState state,
                                               final Level level,
                                               final BlockPos pos,
                                               final Player player,
                                               final BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof AmplifierBlockEntity amplifier) {
            serverPlayer.displayClientMessage(
                Component.translatable(
                    ContentNames.AMPLIFIER_STATUS_TRANSLATION_KEY,
                    amplifier.isActive() ? ContentNames.AMPLIFIER_ACTIVE : ContentNames.AMPLIFIER_INACTIVE,
                    AmplifierBlockEntity.coverageRadius(),
                    Platform.getConfig().getAmplifier().getMultiplierPercent() / 100.0
                ),
                false
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
