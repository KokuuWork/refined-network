package com.kokuu.refinednetwork.common.configcard;

import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.amplifier.AmplifierRegistry;
import com.kokuu.refinednetwork.common.link.BoundTargetResolver;

import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.configurationcard.ConfigurationCardState;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 无线配置卡：**就地复制、批量粘贴**。
 *
 * <p>RS 原版配置卡要求你一个个方块点过去；矿场、产线、一排机器改一遍设置是纯体力活。
 * 这张卡把「粘贴」变成范围操作，而且**范围跟增幅塔挂钩**：站在塔的覆盖里，半径直接扩到塔的
 * 覆盖范围（上限见 {@link #MAX_RADIUS}）。于是增幅塔除了给终端打折，还多了一个用途——
 * 「这片区域归我管」。
 *
 * <p>用法：
 * <ol>
 *   <li>对着任意 RS 方块**右键** = 复制它的配置（与原版卡一致）；</li>
 *   <li>存了配置之后，右键另一个方块 = 粘贴给它；</li>
 *   <li>**潜行 + 右键** = 把配置批量刷给半径内**所有同类型**方块（层级方向也有范围，见
 *       {@link #VERTICAL_RANGE}）。</li>
 * </ol>
 *
 * <p>数据格式**直接复用 RS 原版的 {@code ConfigurationCardState} 数据组件**（公开 API），
 * 所以我们的卡和原版卡互相认：原版卡复制的配置我们能粘，反之亦然。
 *
 * <p><b>不额外收费</b>：它做的是一次性的设置搬运，不像终端那样按次消耗；真正的成本是增幅塔的
 * 常驻耗电——没塔就只能小批量。刻意不为它加电池/网络绑定，那会让一张便利卡变复杂。
 *
 * <p><b>本版不搬升级卡</b>：原版卡粘贴时会顺带把升级卡挪过去，我们没做（避免猜它的消耗语义），
 * 需要搬升级卡请用原版卡。
 */
public class WirelessConfigurationCardItem extends Item {
    /**
     * 不在增幅塔覆盖内时的基础批量半径（格）。
     */
    private static final int BASE_RADIUS = 8;
    /**
     * 批量半径上限：增幅塔默认覆盖 64 格，那会扫出两百万个坐标，一次点击扛不住。
     */
    private static final int MAX_RADIUS = 16;
    /**
     * 批量时的垂直范围（±格）：只在目标方块附近几层里找，避免整块立方体扫。
     */
    private static final int VERTICAL_RANGE = 4;
    /**
     * 右键找方块的射线长度（格）：比原版手长略长一点，方便对着机器点。
     */
    private static final double RAYCAST_REACH = 6.0D;

    public WirelessConfigurationCardItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        final HitResult hit = player.pick(RAYCAST_REACH, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        final BlockPos pos = blockHit.getBlockPos();
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ConfigurationCardTarget target)) {
            serverPlayer.displayClientMessage(ContentNames.CONFIG_CARD_HINT, false);
            return InteractionResultHolder.success(stack);
        }
        final HolderLookup.Provider registries = level.registryAccess();
        final ConfigurationCardState state = stack.get(DataComponents.INSTANCE.getConfigurationCardState());
        if (state == null) {
            copy(serverPlayer, stack, blockEntity, target, registries);
        } else if (player.isCrouching()) {
            pasteBatch(serverPlayer, level, pos, state, registries);
        } else {
            pasteOne(serverPlayer, blockEntity, target, state, registries);
        }
        return InteractionResultHolder.success(stack);
    }

    private static void copy(final ServerPlayer player,
                             final ItemStack stack,
                             final BlockEntity blockEntity,
                             final ConfigurationCardTarget target,
                             final HolderLookup.Provider registries) {
        if (!canAccess(player, blockEntity, BuiltinPermission.OPEN)) {
            RefinedStorageApi.INSTANCE.sendNoPermissionMessage(player, ContentNames.WIRELESS_CONFIGURATION_CARD);
            return;
        }
        final CompoundTag tag = new CompoundTag();
        target.writeConfiguration(tag, registries);
        stack.set(
            DataComponents.INSTANCE.getConfigurationCardState(),
            new ConfigurationCardState(blockEntity.getType(), tag, target.getUpgrades())
        );
        player.displayClientMessage(ContentNames.CONFIG_CARD_HINT, false);
    }

    private static void pasteOne(final ServerPlayer player,
                                 final BlockEntity blockEntity,
                                 final ConfigurationCardTarget target,
                                 final ConfigurationCardState state,
                                 final HolderLookup.Provider registries) {
        if (!sameType(blockEntity, state)) {
            player.displayClientMessage(ContentNames.CONFIG_CARD_WRONG_TYPE, false);
            return;
        }
        if (!canAccess(player, blockEntity, BuiltinPermission.BUILD)) {
            RefinedStorageApi.INSTANCE.sendNoPermissionMessage(player, ContentNames.WIRELESS_CONFIGURATION_CARD);
            return;
        }
        target.readConfiguration(state.config(), registries);
        player.displayClientMessage(ContentNames.CONFIG_CARD_PASTED_ONE, false);
    }

    private static void pasteBatch(final ServerPlayer player,
                                   final Level level,
                                   final BlockPos center,
                                   final ConfigurationCardState state,
                                   final HolderLookup.Provider registries) {
        final int coverage = AmplifierRegistry.coverageRadiusAt(level, player.getX(), player.getZ());
        final int radius = Math.min(MAX_RADIUS, Math.max(BASE_RADIUS, coverage));
        int count = 0;
        for (final BlockPos pos : BlockPos.betweenClosed(
            center.offset(-radius, -VERTICAL_RANGE, -radius),
            center.offset(radius, VERTICAL_RANGE, radius)
        )) {
            if (!(level.getBlockEntity(pos) instanceof ConfigurationCardTarget candidate)
                || !(candidate instanceof BlockEntity candidateEntity)
                || !sameType(candidateEntity, state)
                || !canAccess(player, candidateEntity, BuiltinPermission.BUILD)) {
                continue;
            }
            candidate.readConfiguration(state.config(), registries);
            count++;
        }
        player.displayClientMessage(
            Component.translatable(ContentNames.CONFIG_CARD_PASTED_BATCH_TRANSLATION_KEY, count),
            false
        );
    }

    private static boolean canAccess(final ServerPlayer player,
                                     final BlockEntity blockEntity,
                                     final Permission permission) {
        final GlobalPos target = GlobalPos.of(player.level().dimension(), blockEntity.getBlockPos());
        return BoundTargetResolver.resolveNetwork(player.level(), target)
            .map(network -> SecurityHelper.isAllowed(player, permission, network))
            .orElse(true);
    }

    private static boolean sameType(final BlockEntity blockEntity, final ConfigurationCardState state) {
        return blockEntity.getType() == state.blockEntityType();
    }
}
