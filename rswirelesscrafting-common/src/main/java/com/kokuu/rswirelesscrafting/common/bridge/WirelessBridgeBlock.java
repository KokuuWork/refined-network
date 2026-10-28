package com.kokuu.rswirelesscrafting.common.bridge;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.link.PairingClipboard;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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
 * 无线桥方块。两种身份共用一个外形，只用一个 {@link #LINKED} 表示「真的连上了」：
 * 灭 = 基点（或配对后暂时连不上），亮 = 据点已接入基点的网络。
 *
 * <p>右键交互（无界面，全部走聊天栏，省一个 GUI）：
 * <ul>
 *   <li><b>潜行 + 右键</b>：还没配对 → 记下自己作为**基点**；已配对 → **解绑**（变回基点）。</li>
 *   <li><b>直接右键</b>：报告状态（基点：在不在网络里；据点：连向哪里、区块保活开没开）。</li>
 * </ul>
 */
public class WirelessBridgeBlock extends BaseEntityBlock {
    public static final MapCodec<WirelessBridgeBlock> CODEC = simpleCodec(WirelessBridgeBlock::new);
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    public WirelessBridgeBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(LINKED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LINKED);
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
        return new WirelessBridgeBlockEntity(pos, state);
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
            if (blockEntity instanceof WirelessBridgeBlockEntity bridge) {
                bridge.tick();
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
            && level.getBlockEntity(pos) instanceof WirelessBridgeBlockEntity bridge) {
            if (player.isCrouching()) {
                crouchUse(serverPlayer, bridge);
            } else {
                report(serverPlayer, bridge);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void crouchUse(final ServerPlayer player, final WirelessBridgeBlockEntity bridge) {
        if (bridge.isEndpoint()) {
            bridge.unbind();
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(ContentNames.BRIDGE_UNBOUND, false);
            return;
        }
        if (PairingClipboard.getAnchor(player).isPresent()) {
            final GlobalPos anchor = PairingClipboard.getAnchor(player).orElseThrow();
            if (!bridge.bindTo(anchor, player)) {
                RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.WIRELESS_BRIDGE);
                PairingClipboard.clearAnchor(player);
                return;
            }
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(ContentNames.BRIDGE_BOUND, false);
            return;
        }
        PairingClipboard.setAnchor(player, GlobalPos.of(player.level().dimension(), bridge.getBlockPos()));
        player.displayClientMessage(ContentNames.BRIDGE_ANCHOR_SET, false);
    }

    private static Component statusOf(final WirelessBridgeBlockEntity bridge) {
        if (bridge.isSuspended()) {
            return ContentNames.BRIDGE_SUSPENDED;
        }
        if (bridge.isUnpaid()) {
            return ContentNames.BRIDGE_UNPAID;
        }
        return bridge.isLinked() ? ContentNames.BRIDGE_ONLINE : ContentNames.BRIDGE_OFFLINE;
    }

    private static void report(final ServerPlayer player, final WirelessBridgeBlockEntity bridge) {
        final GlobalPos target = bridge.getTarget();
        if (target == null) {
            player.displayClientMessage(
                Component.translatable(
                    ContentNames.BRIDGE_STATUS_ANCHOR_TRANSLATION_KEY,
                    ContentNames.BRIDGE_ANCHOR_HINT
                ),
                false
            );
            return;
        }
        player.displayClientMessage(
            Component.translatable(
                ContentNames.BRIDGE_STATUS_ENDPOINT_TRANSLATION_KEY,
                target.pos().toShortString(),
                statusOf(bridge)
            ),
            false
        );
    }
}
