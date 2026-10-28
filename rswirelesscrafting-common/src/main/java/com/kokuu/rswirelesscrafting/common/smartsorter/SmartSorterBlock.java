package com.kokuu.rswirelesscrafting.common.smartsorter;

import com.kokuu.rswirelesscrafting.common.ContentNames;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 智能分拣站方块。
 *
 * <p>两个方块状态：
 * <ul>
 *   <li>{@link BlockStateProperties#HORIZONTAL_FACING}——物品往哪边走（和 RS 原版输出总线一样，
 *       摆放时朝着目标容器）；</li>
 *   <li>{@link #ACTIVE}——是否正在搬运，玩家一眼看出它有没有在干活。</li>
 * </ul>
 *
 * <p>接入 RS 网络靠 {@code AbstractNetworkNodeContainerBlockEntity} 的默认连接策略
 * （六向自动连接），贴在线缆旁即可。
 */
public class SmartSorterBlock extends BaseEntityBlock {
    public static final MapCodec<SmartSorterBlock> CODEC = simpleCodec(SmartSorterBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public SmartSorterBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        // 输出侧朝向玩家所看的方向：把分拣站放在容器前，物品就会送进容器。
        return defaultBlockState().setValue(
            BlockStateProperties.HORIZONTAL_FACING,
            context.getHorizontalDirection()
        );
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(
            BlockStateProperties.HORIZONTAL_FACING,
            rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING))
        );
    }

    @Override
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.setValue(
            BlockStateProperties.HORIZONTAL_FACING,
            mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING))
        );
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
        return new SmartSorterBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(final Level level,
                            final BlockPos pos,
                            final BlockState state,
                            @Nullable final LivingEntity placer,
                            final ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player
            && level.getBlockEntity(pos) instanceof SmartSorterBlockEntity sorter) {
            sorter.setOwner(player.getUUID());
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
            if (blockEntity instanceof SmartSorterBlockEntity sorter) {
                sorter.tick();
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
            && level.getBlockEntity(pos) instanceof SmartSorterBlockEntity sorter) {
            if (!sorter.canOpen(serverPlayer)) {
                RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(serverPlayer, ContentNames.SMART_SORTER);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            sorter.setOwner(serverPlayer.getUUID());
            // 必须走 RS 的 MenuOpener：它会识别 ExtendedMenuProvider 并把坐标随包发出去。
            // 直接用 serverPlayer.openMenu 会丢掉附加数据，客户端构造菜单时拿不到坐标。
            com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
                serverPlayer,
                sorter
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * 拆方块时把过滤槽里的代表物品掉出来——玩家不会因为拆方块丢配置。
     */
    @Override
    protected void onRemove(final BlockState state,
                            final Level level,
                            final BlockPos pos,
                            final BlockState newState,
                            final boolean moved) {
        if (!state.is(newState.getBlock())
            && level.getBlockEntity(pos) instanceof SmartSorterBlockEntity sorter) {
            Containers.dropContents(level, pos, sorter);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
