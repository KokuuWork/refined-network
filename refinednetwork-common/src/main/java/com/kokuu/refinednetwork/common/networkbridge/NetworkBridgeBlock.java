package com.kokuu.refinednetwork.common.networkbridge;

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
 * 网络桥接器方块。外形只有「亮 / 灭」（{@link #LINKED}）：
 * 亮着 = 已经和绑定的那张网互通了。
 *
 * <p>右键交互走聊天栏：
 * <ul>
 *   <li><b>潜行 + 右键</b>：还没绑定 → 把剪贴板里的坐标设为绑定目标；已绑定 → 解绑。</li>
 *   <li><b>直接右键</b>：报告状态（连向哪里、互通速率上限、每秒链路费）。</li>
 * </ul>
 *
 * <p>「指着哪张网」复用 {@link PairingClipboard} 剪贴板：先在目标网络里的方块旁潜行右键，
 * 再回到这里潜行右键。跨维度同样成立。
 */
public class NetworkBridgeBlock extends BaseEntityBlock {
    public static final MapCodec<NetworkBridgeBlock> CODEC = simpleCodec(NetworkBridgeBlock::new);
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    public NetworkBridgeBlock(final Properties properties) {
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
        return new NetworkBridgeBlockEntity(pos, state);
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
            if (blockEntity instanceof NetworkBridgeBlockEntity bridge) {
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
            && level.getBlockEntity(pos) instanceof NetworkBridgeBlockEntity bridge) {
            if (player.isCrouching()) {
                crouchUse(serverPlayer, bridge);
            } else {
                report(serverPlayer, bridge);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void crouchUse(final ServerPlayer player, final NetworkBridgeBlockEntity bridge) {
        if (bridge.isEndpoint()) {
            bridge.unbind();
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(ContentNames.NETWORK_BRIDGE_UNBOUND, false);
            return;
        }
        if (PairingClipboard.getAnchor(player).isPresent()) {
            final GlobalPos anchor = PairingClipboard.getAnchor(player).orElseThrow();
            if (!bridge.bindTo(anchor, player)) {
                RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.NETWORK_BRIDGE);
                PairingClipboard.clearAnchor(player);
                return;
            }
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(
                Component.translatable(
                    ContentNames.NETWORK_BRIDGE_BOUND_TRANSLATION_KEY,
                    bridge.getTarget() == null ? "?" : bridge.getTarget().pos().toShortString()
                ),
                false
            );
            return;
        }
        PairingClipboard.setAnchor(
            player,
            GlobalPos.of(player.level().dimension(), bridge.getBlockPos())
        );
        player.displayClientMessage(ContentNames.NETWORK_BRIDGE_ANCHOR_SET, false);
    }

    private static void report(final ServerPlayer player, final NetworkBridgeBlockEntity bridge) {
        player.displayClientMessage(bridge.statusLine(), false);
    }
}
