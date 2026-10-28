package com.kokuu.rswirelesscrafting.common.energyreceiver;

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
 * 无线能量接收器方块。外形只有「亮 / 灭」（{@link #LINKED}）：
 * 亮着 = 已经把电送进绑定的网络了，玩家不用打开任何界面就能看出来。
 *
 * <p>右键交互全部走聊天栏，省一个 GUI：
 * <ul>
 *   <li><b>潜行 + 右键</b>：已绑定 → 解绑；剪贴板里有坐标 → 绑到那个网络；都没有 →
 *       把自己记成「接入点」，供更远处的接收器指向。</li>
 *   <li><b>直接右键</b>：报告状态（缓冲 / 容量 / 连向哪里 / 每秒链路费）。</li>
 * </ul>
 *
 * <p>「指着哪个网络」复用无线的 {@link PairingClipboard} 剪贴板：先在**接在网络里**的方块旁
 * 潜行右键（无线桥的基点、或者另一座已连上的接收器），再回到这里潜行右键完成绑定。
 * 跨维度同样成立——记的是 {@link GlobalPos}。
 */
public class WirelessEnergyReceiverBlock extends BaseEntityBlock {
    public static final MapCodec<WirelessEnergyReceiverBlock> CODEC =
        simpleCodec(WirelessEnergyReceiverBlock::new);
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    public WirelessEnergyReceiverBlock(final Properties properties) {
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
        return new WirelessEnergyReceiverBlockEntity(pos, state);
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
            if (blockEntity instanceof WirelessEnergyReceiverBlockEntity receiver) {
                receiver.tick();
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
            && level.getBlockEntity(pos) instanceof WirelessEnergyReceiverBlockEntity receiver) {
            if (player.isCrouching()) {
                crouchUse(serverPlayer, receiver);
            } else {
                report(serverPlayer, receiver);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void crouchUse(final ServerPlayer player, final WirelessEnergyReceiverBlockEntity receiver) {
        if (receiver.isEndpoint()) {
            receiver.unbind();
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(ContentNames.ENERGY_RECEIVER_UNBOUND, false);
            return;
        }
        if (PairingClipboard.getAnchor(player).isPresent()) {
            final GlobalPos anchor = PairingClipboard.getAnchor(player).orElseThrow();
            if (!receiver.bindTo(anchor, player)) {
                RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(
                    player,
                    ContentNames.WIRELESS_ENERGY_RECEIVER
                );
                PairingClipboard.clearAnchor(player);
                return;
            }
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(ContentNames.ENERGY_RECEIVER_BOUND, false);
            return;
        }
        player.displayClientMessage(ContentNames.LINK_ANCHOR_REQUIRED, false);
    }

    private static void report(final ServerPlayer player, final WirelessEnergyReceiverBlockEntity receiver) {
        player.displayClientMessage(receiver.statusLine(), false);
        player.displayClientMessage(
            Component.translatable(
                ContentNames.ENERGY_RECEIVER_BUFFER_TRANSLATION_KEY,
                receiver.getBuffer(),
                receiver.getCapacity()
            ),
            false
        );
    }
}
