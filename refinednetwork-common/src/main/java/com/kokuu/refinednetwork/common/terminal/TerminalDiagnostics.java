package com.kokuu.refinednetwork.common.terminal;

import com.kokuu.refinednetwork.common.ModIdentifierUtil;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.upgrade.Upgrades;
import com.kokuu.refinednetwork.common.wirelesscraftinggrid.ExtendedWirelessNetworkItemContext;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 终端打不开时，给出**一个**最主要的原因，而不是只丢一句「没接入网络」。
 *
 * <p>只做单目标判定：不扫描网络、不主动加载区块、不泄露其他网络信息。
 * 判定顺序与访问准入一致：绑定 → 加载 → 维度 → 距离。
 */
public final class TerminalDiagnostics {
    public static final MutableComponent UNBOUND = line("diag_unbound");
    public static final MutableComponent UNLOADED = line("diag_unloaded");
    public static final MutableComponent NEED_DIMENSION = line("diag_need_dimension");
    public static final MutableComponent NEED_RANGE = line("diag_need_range");
    public static final MutableComponent OK = line("diag_ok");

    private TerminalDiagnostics() {
    }

    private static MutableComponent line(final String key) {
        return ModIdentifierUtil.createTranslation("gui", key).withStyle(ChatFormatting.GRAY);
    }

    /**
     * 当前阻断访问的主要原因；全部满足时返回「已就绪」。
     */
    public static Component blockingReason(final ServerPlayer player, final ItemStack stack) {
        final GlobalPos bound = AbstractWirelessTerminalItem.getBoundLocation(stack);
        if (bound == null) {
            return UNBOUND;
        }
        final ServerLevel target = player.getServer().getLevel(bound.dimension());
        if (target == null || !target.isLoaded(bound.pos())) {
            return UNLOADED;
        }
        return accessReason(player, stack, bound);
    }

    private static Component accessReason(final ServerPlayer player, final ItemStack stack, final GlobalPos bound) {
        if (!bound.dimension().equals(player.level().dimension())) {
            return Upgrades.hasInterdimensionalUpgrade(stack) ? OK : NEED_DIMENSION;
        }
        final double distance = ExtendedWirelessNetworkItemContext.distanceToBound(stack, player);
        final int baseRange = Math.max(1, Platform.getConfig().getDistanceEnergy().getBaseRange());
        if (distance > baseRange && !Upgrades.hasRangeUpgrade(stack)) {
            return NEED_RANGE;
        }
        return OK;
    }
}
