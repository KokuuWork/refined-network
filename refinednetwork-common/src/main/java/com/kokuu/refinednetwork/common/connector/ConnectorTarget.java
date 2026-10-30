package com.kokuu.refinednetwork.common.connector;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 「能被无线连接器敲的方块设备」。
 *
 * <p>能量接收器、能量监控仪、无线桥、网络桥接器本质上是同一件事：把一个方块指到另一张网络上。
 * 以前这套绑定藏在方块自己的右键里，结果和设备「本身是方块物品」这件事打架——手持方块右键只会
 * 放置。现在统一由 {@link WirelessConnectorItem} 敲击完成，方块这边只需要暴露这几个方法。
 */
public interface ConnectorTarget {
    /**
     * 是否已经指到某张网络上。
     */
    boolean isBound();

    /**
     * 把本设备指到 {@code anchor} 所在的网络。没有权限时返回 {@code false}。
     */
    boolean bindTo(GlobalPos anchor, ServerPlayer player);

    /**
     * 解除绑定。
     */
    void unbind();

    /**
     * 绑定成功的提示。
     */
    Component boundMessage();

    /**
     * 解绑成功的提示。
     */
    Component unboundMessage();
}
