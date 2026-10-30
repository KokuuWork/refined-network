package com.kokuu.refinednetwork.common.networkbridge;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage.api.storage.composite.ParentComposite;

/**
 * 把另一张网络的存储呈献给本地网络，同时维护 RS 的复合存储缓存。
 *
 * <p>RS 在添加存储源时会把 {@code getAll()} 复制进缓存，之后网格和自动合成读取的是缓存，
 * 不是每次重新调用 {@code getAll()}。所以桥接器必须实现 {@link CompositeAwareChild}，
 * 在远端变化时把差量写回父级，否则会出现“桥接已连接但网格为空/保留幽灵库存”。
 */
public class BridgedStorage implements CompositeAwareChild {
    private final NetworkBridgeBlockEntity blockEntity;
    private final Set<ParentComposite> parents = new HashSet<>();
    private final Map<ResourceKey, Long> published = new HashMap<>();
    private long lastCacheRefresh = Long.MIN_VALUE;
    private boolean forwarding;

    public BridgedStorage(@Nullable final NetworkBridgeBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite parentComposite) {
        parents.add(parentComposite);
        invalidateCache();
        refreshCache();
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite parentComposite) {
        parents.remove(parentComposite);
        invalidateCache();
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        refreshCache();
        return published.entrySet().stream()
            .map(entry -> new ResourceAmount(entry.getKey(), entry.getValue()))
            .toList();
    }

    @Override
    public long getStored() {
        refreshCache();
        return published.values().stream().mapToLong(Long::longValue).sum();
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (amount <= 0) {
            return 0;
        }
        final long budget = blockEntity == null ? 0L : blockEntity.remainingItemBudget();
        if (budget <= 0) {
            return 0;
        }
        final Long moved = forward(target -> target.insert(resource, Math.min(amount, budget), action, actor));
        if (moved == null || moved <= 0) {
            return 0;
        }
        if (action == Action.EXECUTE) {
            blockEntity.consumeItemBudget(moved);
            invalidateCache();
            refreshCache();
        }
        return moved;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (amount <= 0) {
            return 0;
        }
        final long budget = blockEntity == null ? 0L : blockEntity.remainingItemBudget();
        if (budget <= 0) {
            return 0;
        }
        final Long moved = forward(target -> target.extract(resource, Math.min(amount, budget), action, actor));
        if (moved == null || moved <= 0) {
            return 0;
        }
        if (action == Action.EXECUTE) {
            blockEntity.consumeItemBudget(moved);
            invalidateCache();
            refreshCache();
        }
        return moved;
    }

    @Override
    public Amount compositeInsert(final ResourceKey resource,
                                  final long amount,
                                  final Action action,
                                  final Actor actor) {
        // refreshCache 已经把实际变化写回父级，CompositeStorage 不应再按 moved 二次记账。
        return new Amount(insert(resource, amount, action, actor), 0);
    }

    @Override
    public Amount compositeExtract(final ResourceKey resource,
                                   final long amount,
                                   final Action action,
                                   final Actor actor) {
        return new Amount(extract(resource, amount, action, actor), 0);
    }

    /**
     * 主动对账：远端库存可能在本地没有任何读写的情况下独立变化，而父级缓存只认差量通知。
     * 由方块实体按固定周期调用，保证即使没人读这张桥，本地列表也会在有限时间内收敛。
     */
    public void pollRemote() {
        invalidateCache();
        refreshCache();
    }

    private void invalidateCache() {
        lastCacheRefresh = Long.MIN_VALUE;
    }

    private void refreshCache() {
        if (forwarding || blockEntity == null) {
            return;
        }
        final long now = blockEntity.getGameTime();
        if (now == lastCacheRefresh) {
            return;
        }
        lastCacheRefresh = now;
        final Map<ResourceKey, Long> current = snapshot();
        for (final Map.Entry<ResourceKey, Long> old : published.entrySet()) {
            final long delta = old.getValue() - current.getOrDefault(old.getKey(), 0L);
            if (delta > 0) {
                removeFromParents(old.getKey(), delta);
            } else if (delta < 0) {
                addToParents(old.getKey(), -delta);
            }
        }
        for (final Map.Entry<ResourceKey, Long> entry : current.entrySet()) {
            if (!published.containsKey(entry.getKey())) {
                addToParents(entry.getKey(), entry.getValue());
            }
        }
        published.clear();
        published.putAll(current);
    }

    private Map<ResourceKey, Long> snapshot() {
        final Collection<ResourceAmount> all = forward(Storage::getAll);
        if (all == null) {
            return Map.of();
        }
        final Map<ResourceKey, Long> result = new HashMap<>();
        for (final ResourceAmount amount : all) {
            if (amount.amount() > 0) {
                result.merge(amount.resource(), amount.amount(), Long::sum);
            }
        }
        return result;
    }

    private void addToParents(final ResourceKey resource, final long amount) {
        for (final ParentComposite parent : parents) {
            parent.addToCache(resource, amount);
        }
    }

    private void removeFromParents(final ResourceKey resource, final long amount) {
        for (final ParentComposite parent : parents) {
            parent.removeFromCache(resource, amount);
        }
    }

    @Nullable
    private <T> T forward(final Function<Storage, T> action) {
        if (blockEntity == null) {
            return null;
        }
        if (forwarding) {
            blockEntity.markCyclic();
            return null;
        }
        return forwardToNetwork(action);
    }

    @Nullable
    private <T> T forwardToNetwork(final Function<Storage, T> action) {
        final Network network = blockEntity.getRemoteNetwork();
        if (network == null) {
            return null;
        }
        forwarding = true;
        try {
            final T result = BridgeRecursionGuard.run(
                network,
                () -> action.apply(network.getComponent(StorageNetworkComponent.class))
            );
            if (result == null || blockEntity.isCyclic()) {
                blockEntity.markCyclic();
                return null;
            }
            return result;
        } finally {
            forwarding = false;
        }
    }
}
