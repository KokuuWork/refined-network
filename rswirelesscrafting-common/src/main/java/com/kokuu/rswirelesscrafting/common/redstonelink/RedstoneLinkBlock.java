package com.kokuu.rswirelesscrafting.common.redstonelink;

import com.kokuu.rswirelesscrafting.common.link.PairingClipboard;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
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
 * 无线红石端点。
 *
 * <p>两个同款方块配对，信号从输入端传到输出端，跨维度也成立。交互沿用无线桥的那一套
 * （无界面，全部走聊天栏）：
 * <ul>
 *   <li><b>潜行 + 右键（未配对）</b>：把自己记成输入端。</li>
 *   <li><b>潜行 + 右键（已有记录）</b>：把自己配成输出端，指向刚才那个输入端。</li>
 *   <li><b>潜行 + 右键（已配对）</b>：解绑，变回普通端点。</li>
 *   <li><b>直接右键</b>：报告状态（输入端 / 输出端以及连向哪里）。</li>
 * </ul>
 *
 * <p>{@link #POWERED} 既是外观（亮 / 灭），也是对外输出：{@link #isSignalSource()} 为真时，
 * 红石线会把这块方块当作 15 级电源。
 */
public class RedstoneLinkBlock extends BaseEntityBlock {
    public static final MapCodec<RedstoneLinkBlock> CODEC = simpleCodec(RedstoneLinkBlock::new);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public RedstoneLinkBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
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
        return new RedstoneLinkBlockEntity(pos, state);
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
            if (blockEntity instanceof RedstoneLinkBlockEntity link) {
                link.tick();
            }
        };
    }

    @Override
    protected boolean isSignalSource(final BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(final BlockState state, final BlockGetter level, final BlockPos pos,
                            final Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(final BlockState state, final BlockGetter level, final BlockPos pos,
                                  final Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state,
                                               final Level level,
                                               final BlockPos pos,
                                               final Player player,
                                               final BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof RedstoneLinkBlockEntity link) {
            if (player.isCrouching()) {
                crouchUse(serverPlayer, level, pos, link);
            } else {
                report(serverPlayer, link);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void crouchUse(final ServerPlayer player,
                                  final Level level,
                                  final BlockPos pos,
                                  final RedstoneLinkBlockEntity link) {
        if (link.isEndpoint()) {
            link.unbind();
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(RedstoneLinkNames.UNBOUND, false);
            return;
        }
        final var anchor = PairingClipboard.getAnchor(player);
        if (anchor.isEmpty()) {
            PairingClipboard.setAnchor(player, GlobalPos.of(level.dimension(), pos));
            player.displayClientMessage(RedstoneLinkNames.ANCHOR_SET, false);
            return;
        }
        final GlobalPos source = anchor.orElseThrow();
        if (!(level.getServer().getLevel(source.dimension()) instanceof ServerLevel sourceLevel)
            || !(sourceLevel.getBlockState(source.pos()).getBlock() instanceof RedstoneLinkBlock)) {
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(RedstoneLinkNames.NOT_A_LINK, false);
            return;
        }
        link.bindTo(source, player);
        PairingClipboard.clearAnchor(player);
        player.displayClientMessage(RedstoneLinkNames.BOUND, false);
    }

    private static void report(final ServerPlayer player, final RedstoneLinkBlockEntity link) {
        final GlobalPos target = link.getTarget();
        if (target == null) {
            player.displayClientMessage(RedstoneLinkNames.STATUS_SOURCE, false);
            return;
        }
        player.displayClientMessage(
            Component.translatable(RedstoneLinkNames.STATUS_SINK_KEY, target.pos().toShortString()),
            false
        );
    }
}
