package com.kokuu.rswirelesscrafting.common.bridge;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * 配对用的「剪贴板」：玩家潜行 + 右键一座**基点**时，把它的坐标记在这里；
 * 之后到远处潜行 + 右键另一座桥，就把那个坐标写进它，配对完成。
 *
 * <p>刻意不引入配对物品（RS 用网络卡），因为「潜行右键两次」这个仪式更轻、不会占用背包，
 * 而且**跨维度也成立**——记的是 {@link GlobalPos}，不是视线内的方块。
 *
 * <p>内存态、每个玩家一格、服务器重启即清空：配对一旦完成就写进据点的存档了，
 * 这份剪贴板只是配对过程中的临时东西。
 */
public final class BridgePairing {
    private static final Map<UUID, GlobalPos> PENDING = new ConcurrentHashMap<>();

    private BridgePairing() {
    }

    public static void setAnchor(final ServerPlayer player, final GlobalPos anchor) {
        PENDING.put(player.getUUID(), anchor);
    }

    public static Optional<GlobalPos> getAnchor(final ServerPlayer player) {
        return Optional.ofNullable(PENDING.get(player.getUUID()));
    }

    public static void clearAnchor(final ServerPlayer player) {
        PENDING.remove(player.getUUID());
    }
}
