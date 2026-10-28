package com.kokuu.rswirelesscrafting.common.connector;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.DimensionNames;
import com.kokuu.rswirelesscrafting.common.link.BoundTargetResolver;
import com.kokuu.rswirelesscrafting.common.link.PairingClipboard;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 无线连接器：把「绑定」从方块右键里拆出来的专用工具。
 *
 * <p>为什么需要它：能量接收器、能量监控仪、两个桥本身都是**方块物品**，手持它们右键任何方块，
 * 原版都会优先「放置」，根本轮不到方块的右键逻辑；而方块自己的右键又要区分「查看状态」和
 * 「绑定」，一旦一排同款设备摆在一起，靠潜行 + 右键根本分不清绑的是哪一台。
 *
 * <p>所以绑定改由连接器敲：
 * <ol>
 *   <li>右键任意 RS 网络方块 = 记录接入点；</li>
 *   <li>右键本模组的设备方块 = 把它绑到刚才那个接入点；</li>
 *   <li>再右键一台**已绑定**的设备 = 解绑；</li>
 *   <li>潜行 + 右键 = 清掉手上的接入点记录。</li>
 * </ol>
 *
 * <p>敲哪一台就绑哪一台，和设备有几台无关。
 */
public class WirelessConnectorItem extends Item {
    public WirelessConnectorItem(final Item.Properties properties) {
        super(properties);
    }

    /**
     * 连接器敲击的统一入口，由两个加载器的「右键方块」事件转发过来（见各自的初始化器）。
     *
     * @param clearAnchor 玩家是否按着潜行键：潜行敲 = 清掉手上的接入点记录
     * @return 非 {@code PASS} 表示这次交互已被连接器处理完，事件应当取消
     */
    public InteractionResult apply(final ServerPlayer player,
                                    final Level level,
                                    final BlockPos pos,
                                    final boolean clearAnchor) {
        if (clearAnchor) {
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(ContentNames.CONNECTOR_CLEARED, false);
            return InteractionResult.SUCCESS;
        }
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ConnectorTarget target) {
            return operate(player, target, level, pos);
        }
        final GlobalPos anchor = GlobalPos.of(level.dimension(), pos);
        if (BoundTargetResolver.resolveNetwork(level, anchor).isEmpty()) {
            player.displayClientMessage(ContentNames.CONNECTOR_NOT_NETWORK, false);
            return InteractionResult.SUCCESS;
        }
        PairingClipboard.setAnchor(player, anchor);
        // 聊天栏把「设到哪儿」说全：维度 + 坐标，再告诉玩家下一步敲哪台。
        player.displayClientMessage(
            Component.translatable(
                ContentNames.CONNECTOR_ANCHOR_SET_KEY,
                DimensionNames.get(level.dimension()).withStyle(ChatFormatting.YELLOW),
                pos.getX(),
                pos.getY(),
                pos.getZ()
            ).withStyle(ChatFormatting.GREEN),
            false
        );
        return InteractionResult.SUCCESS;
    }

    /**
     * 对着设备敲：手上有接入点就绑，已经绑着就解，两样都没有就提示先记录接入点。
     */
    private static InteractionResult operate(final ServerPlayer player,
                                             final ConnectorTarget target,
                                             final Level level,
                                             final BlockPos pos) {
        final GlobalPos anchor = PairingClipboard.getAnchor(player)
            .orElse(null);
        if (anchor != null) {
            if (!target.bindTo(anchor, player)) {
                player.displayClientMessage(ContentNames.CONNECTOR_NO_PERMISSION, false);
                PairingClipboard.clearAnchor(player);
                return InteractionResult.SUCCESS;
            }
            PairingClipboard.clearAnchor(player);
            player.displayClientMessage(target.boundMessage(), false);
            if (level instanceof ServerLevel serverLevel) {
                // 撒一条电火花线，让「连到哪儿」肉眼可见。
                LinkEffect.flash(serverLevel, pos, anchor);
            }
            return InteractionResult.SUCCESS;
        }
        if (target.isBound()) {
            target.unbind();
            player.displayClientMessage(target.unboundMessage(), false);
            return InteractionResult.SUCCESS;
        }
        player.displayClientMessage(ContentNames.CONNECTOR_NEED_ANCHOR, false);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(ContentNames.CONNECTOR_DESC);
        lines.add(Component.empty());
        lines.add(ContentNames.CONNECTOR_USAGE);
    }
}
