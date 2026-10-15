package com.kokuu.rswirelesscrafting.common.link;

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
    private final NetworkNodeContainerProvider provider;
    private final GlobalPos target;

    @Nullable
    private Network attached;

    public NetworkAttachment(final Level level,
                             final NetworkNodeContainerProvider provider,
                             final GlobalPos target) {
        this.level = level;
        this.provider = provider;
        this.target = target;
    }

    /**
     * 对齐一次：目标网络在 → 确保挂着（已在就只补掉队的）；目标网络没了 → 摘干净。
     *
     * @return 现在是否挂在网络上
     */
    public boolean ensureAttached() {
        final Network network = BoundTargetResolver.resolveNetwork(level, target).orElse(null);
        if (network == null) {
            detach();
            return false;
        }
        if (network != attached) {
            detach();
            attach(network);
            return true;
        }
        // 同一个网络：RS 的图重建可能已经把容器踢出去了，这里只补那些掉队的。
        for (final InWorldNetworkNodeContainer container : provider.getContainers()) {
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
        for (final InWorldNetworkNodeContainer container : provider.getContainers()) {
            network.removeContainer(container);
            if (container.getNode().getNetwork() == network) {
                container.getNode().setNetwork(null);
            }
        }
    }

    private void attach(final Network network) {
        for (final InWorldNetworkNodeContainer container : provider.getContainers()) {
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
