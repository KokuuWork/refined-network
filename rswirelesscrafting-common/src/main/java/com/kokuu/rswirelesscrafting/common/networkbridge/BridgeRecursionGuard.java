package com.kokuu.rswirelesscrafting.common.networkbridge;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;

/**
 * 桥接转发的共享递归守卫。
 *
 * <p>RS 的网络能量/存储组件会遍历自己的提供者；如果两张网络互相桥接，单次读取会沿着
 * A → B → A 走回来。这里的集合按当前服务器线程保存正在读取的网络，任何一层再次进入
 * 同一张网络就返回 null，让调用方把桥标记为不可用的环路，而不是递归到栈溢出。
 */
final class BridgeRecursionGuard {
    private static final ThreadLocal<Set<Network>> ACTIVE = ThreadLocal.withInitial(HashSet::new);

    private BridgeRecursionGuard() {
    }

    @Nullable
    static <T> T run(final Network network, final Supplier<T> action) {
        final Set<Network> active = ACTIVE.get();
        if (!active.add(network)) {
            return null;
        }
        try {
            return action.get();
        } finally {
            active.remove(network);
            if (active.isEmpty()) {
                ACTIVE.remove();
            }
        }
    }
}
