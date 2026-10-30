package com.kokuu.refinednetwork.common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

/**
 * 方块设备「为什么没工作」的文案。
 *
 * <p>单独放一个类：{@code ContentNames} 已经贴着 checkstyle 的 700 行上限，
 * 再往里加常量会把整个模块的检查打挂。
 */
public final class DeviceDiagnosticsNames {
    public static final MutableComponent UNREACHABLE = line("device_unreachable");
    public static final MutableComponent NO_PERMISSION = line("device_no_permission");
    public static final MutableComponent NO_LOCAL_NETWORK = line("device_no_local_network");
    public static final MutableComponent SELF_BOUND = line("device_self_bound");
    public static final MutableComponent MOUNT_REJECTED = line("device_mount_rejected");
    public static final MutableComponent DISABLED = line("device_disabled");
    public static final MutableComponent SUSPENDED = line("device_suspended");
    public static final MutableComponent UNPAID = line("device_unpaid");

    private DeviceDiagnosticsNames() {
    }

    private static MutableComponent line(final String key) {
        return ModIdentifierUtil.createTranslation("gui", key).withStyle(ChatFormatting.GRAY);
    }
}
