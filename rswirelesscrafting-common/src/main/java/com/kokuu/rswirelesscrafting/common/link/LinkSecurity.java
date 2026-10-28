package com.kokuu.rswirelesscrafting.common.link;

import java.util.UUID;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.security.PlayerSecurityActor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 桥接设备的统一权限检查。
 *
 * <p>RS 的便捷 API 需要在线玩家；桥接器在玩家离线后仍会 tick，所以同时提供 UUID 路径。
 * 在线时走 {@link SecurityHelper}（保留 OP 特例），离线时走 RS 的 SecurityActor，
 * 让安全管理器的允许/拒绝规则继续生效，而不是把设备当成无条件可信的系统账户。
 */
public final class LinkSecurity {
    private LinkSecurity() {
    }

    public static boolean isAllowed(final ServerPlayer player, final Permission permission, final Network network) {
        return SecurityHelper.isAllowed(player, permission, network);
    }

    public static boolean isAllowed(final UUID playerId,
                                    final Permission permission,
                                    final Network network,
                                    final ServerLevel level) {
        final ServerPlayer online = level.getServer().getPlayerList().getPlayer(playerId);
        if (online != null) {
            return isAllowed(online, permission, network);
        }
        return network.getComponent(SecurityNetworkComponent.class)
            .isAllowed(permission, new PlayerSecurityActor(playerId));
    }
}
