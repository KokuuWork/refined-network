package com.kokuu.refinednetwork.common.link;

import java.util.Set;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;

/**
 * 把一个**方块实体**挂到绑定的那张网络上——无线桥「据点」那一侧的地基。
 *
 * <p>RS 自己加节点就是两句（{@code NetworkBuilderImpl} 原文）：
 * <pre>
 *     entryNode.setNetwork(newNetwork);
 *     newNetwork.addContainer(entry);
 * </pre>
 * 两句都做才是完整的「入网」：前者让节点能反过来查网络，后者才让网络**看见**这个容器
 * （网络的存储层、耗电统计都是遍历图里的容器得到的）。
 *
 * <p>挂上去之后，节点若实现了 {@code StorageProvider}（公开 API），网络会在
 * <b>加入 / 移除时自动</b>把它的 {@code getStorage()} 收进或摘出存储组件——
 * 我们不需要、也不允许手动去碰 {@code RootStorage}。
 *
 * <p><b>用法</b>：方块实体持有一个实例，在
 * <ol>
 *   <li>绑定变化时调 {@link #ensureAttached()}；</li>
 *   <li>每若干 tick 再调一次 {@link #ensureAttached()}（RS 的图重建可能把我们的容器踢掉，
 *       它是幂等的，掉了会补）；</li>
 *   <li>{@code setRemoved()} 里**必须**调 {@link #detach()}，否则网络里会留下幽灵容器
 *       ——继续白占存储、白扣电。</li>
 * </ol>
 */
public final class NetworkAttachment {
    private final Level level;
    private final Set<InWorldNetworkNodeContainer> containers;
    private final GlobalPos target;

    @Nullable
    private Network attached;

    public NetworkAttachment(final Level level,
                             final NetworkNodeContainerProvider provider,
                             final GlobalPos target) {
        this(level, provider.getContainers(), target);
    }

    /**
     * 用一个**自己造好的**容器挂载，而不是复用方块实体基类那一套。
     *
     * <p>为什么要这条路：基类会在 {@code clearRemoved()} 里把容器交给 RS 的建图器，
     * 于是方块只要挨着线缆就会**自动加入本地网络**。对「节点会向网络提供能量 / 存储」的
     * 方块（无线能量接收器、网络桥接器）来说，同时待在本网和远端网里意味着
     * 能量被重复计入——那是刷电漏洞。自己造容器、只往绑定的网络里挂，就绕开了这一点。
     */
    public NetworkAttachment(final Level level,
                             final InWorldNetworkNodeContainer container,
                             final GlobalPos target) {
        this(level, Set.of(container), target);
    }

    private NetworkAttachment(final Level level,
                              final Set<InWorldNetworkNodeContainer> containers,
                              final GlobalPos target) {
        this.level = level;
        this.containers = containers;
        this.target = target;
    }

    /**
     * 对齐一次：目标网络在 → 确保挂着（已在就只补掉队的）；目标网络没了 → 摘干净。
     *
     * @return 现在是否挂在网络上
     */
    public GlobalPos getTarget() {
        return target;
    }

    public boolean ensureAttached() {
        final Network network = BoundTargetResolver.resolveNetwork(level, target).orElse(null);
        if (network == null) {
            detach();
            return false;
        }
        detachFromOtherNetworks(network);
        if (network != attached) {
            detach();
            attach(network);
            return true;
        }
        // 同一个网络：RS 的图重建可能已经把容器踢出去了，这里只补那些掉队的。
        for (final InWorldNetworkNodeContainer container : containers) {
            if (container.getNode().getNetwork() != network) {
                container.getNode().setNetwork(network);
                network.addContainer(container);
            }
        }
        return true;
    }

    public void detach() {
        final Network network = attached;
        attached = null;
        if (network == null) {
            return;
        }
        for (final InWorldNetworkNodeContainer container : containers) {
            network.removeContainer(container);
            if (container.getNode().getNetwork() == network) {
                container.getNode().setNetwork(null);
            }
        }
    }

    private void detachFromOtherNetworks(final Network destination) {
        for (final InWorldNetworkNodeContainer container : containers) {
            final Network current = container.getNode().getNetwork();
            if (current != null && current != destination) {
                current.removeContainer(container);
                container.getNode().setNetwork(null);
            }
        }
    }

    private void attach(final Network network) {
        for (final InWorldNetworkNodeContainer container : containers) {
            container.getNode().setNetwork(network);
            network.addContainer(container);
        }
        attached = network;
    }

    @Nullable
    public Network getAttachedNetwork() {
        return attached;
    }

    public boolean isAttached() {
        return attached != null;
    }
}
