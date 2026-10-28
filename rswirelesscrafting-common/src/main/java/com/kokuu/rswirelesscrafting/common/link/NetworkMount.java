package com.kokuu.rswirelesscrafting.common.link;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;

/**
 * 把一个**自己造的**容器挂到指定的那张网络上。
 *
 * <p>和 {@link NetworkAttachment} 的区别只有一个：这里的网络是现成的 {@link Network} 对象
 * （例如「旁边这张网」），不靠坐标反查。无线能量接收器需要这个——它要挂的是**身边的本地网络**，
 * 而它绑定的那张远端网络只是取电源。
 *
 * <p>RS 的挂载就是两句（{@code NetworkBuilderImpl} 原文）：{@code node.setNetwork(network)}
 * 让节点能反查网络，{@code network.addContainer(container)} 让网络看得见这个容器
 * （能量池、耗电统计都是遍历图里的容器算出来的）。
 */
public final class NetworkMount {
    private final InWorldNetworkNodeContainer container;

    @Nullable
    private Network attached;

    public NetworkMount(final InWorldNetworkNodeContainer container) {
        this.container = container;
    }

    /**
     * 当前挂在哪张网上；没挂上时为 {@code null}。
     */
    @Nullable
    public Network getAttached() {
        return attached;
    }

    public boolean isMounted() {
        return attached != null;
    }

    /**
     * 对齐到 {@code network}：换网先摘干净，同一张网只补掉队的容器（幂等，可以每 tick 调）。
     */
    public void mount(final Network network) {
        if (attached != network) {
            detach();
            attached = network;
        }
        if (container.getNode().getNetwork() != network) {
            container.getNode().setNetwork(network);
            network.addContainer(container);
        }
    }

    /**
     * 从当前网络摘下。拆方块 / 解绑 / 关配置时必须调用，否则网络里会留下幽灵容器，
     * 继续白占能量池。
     */
    public void detach() {
        final Network network = attached;
        attached = null;
        if (network == null) {
            return;
        }
        network.removeContainer(container);
        if (container.getNode().getNetwork() == network) {
            container.getNode().setNetwork(null);
        }
    }
}
