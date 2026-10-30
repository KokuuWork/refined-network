package com.kokuu.refinednetwork.common.smartsorter;

import com.kokuu.refinednetwork.common.BlockEntities;
import com.kokuu.refinednetwork.common.Config;
import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.link.LinkSecurity;
import com.kokuu.refinednetwork.common.util.RateMeter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 智能分拣站：把网络里**匹配过滤规则**的物品，搬进它面前的那个容器。
 *
 * <h2>和 RS 原版输出总线的区别</h2>
 * 原版总线的过滤槽只能按**具体物品**白名单——想分走「所有木头」得把每种原木各填一格。
 * 本方块多一种 {@link SorterMatchMode#TAG 标签}匹配：填一个代表物品，
 * 它所在的全部物品标签都会被匹配，一格就能分走一整类。
 *
 * <h2>搬运口径</h2>
 * <ul>
 *   <li>速率按 {@code stacksPerSecond} 上限摊到每 tick（{@link RateMeter} 累积余数，配 7 也不漏）；</li>
 *   <li>每搬走一组收 {@code energyPerStack} FE，**搬不动不收钱**；</li>
 *   <li>先问目标容器能收多少，再按这个数从网络取——绝不会出现「取出来了却塞不下」把物品吞掉；</li>
 *   <li>红石信号 = 停机（和原版总线的红石模式一致）。</li>
 * </ul>
 */
public class SmartSorterBlockEntity extends AbstractNetworkNodeContainerBlockEntity<SmartSorterNetworkNode>
    implements Container, ExtendedMenuProvider<SmartSorterMenuData> {
    public static final int FILTER_COUNT = 9;
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_MODE = "Mode";
    private static final String TAG_OWNER = "Owner";
    /** 界面数据：0 = 匹配方式序号，1 = 当前网络里命中的过滤规则数。 */
    private static final int DATA_SIZE = 2;
    private static final int DATA_MODE = 0;
    private static final int DATA_MATCHES = 1;
    /** 每 10 tick 刷新一次「命中规则数」，属于看得见就好的刷新率。 */
    private static final int REFRESH_INTERVAL = 10;

    private final NonNullList<ItemStack> filters =
        NonNullList.withSize(FILTER_COUNT, ItemStack.EMPTY);
    /** 标签模式的轮转游标，避免总是搬列表里的第一种物品。 */
    private final int[] matchOffsets = new int[FILTER_COUNT];
    private final RateMeter transferMeter = new RateMeter();
    private final ContainerData containerData = new SmartSorterDataSlots();
    private final Actor actor = () -> ContentNames.SMART_SORTER.getString();

    private SorterMatchMode mode = SorterMatchMode.EXACT;
    @Nullable
    private UUID owner;
    private int refreshCountdown;
    private int matchCount;

    public SmartSorterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getSmartSorter(),
            pos,
            state,
            new SmartSorterNetworkNode()
        );
    }

    // ------------------------------------------------------------- 状态查询

    public SorterMatchMode getMode() {
        return mode;
    }

    public boolean canOpen(final Player player) {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }
        return LinkSecurity.isAllowed(serverPlayer, BuiltinPermission.OPEN, network)
            && LinkSecurity.isAllowed(serverPlayer, BuiltinPermission.EXTRACT, network);
    }

    public void setOwner(final UUID playerId) {
        if (!playerId.equals(owner)) {
            owner = playerId;
            setChanged();
        }
    }

    private boolean isAuthorized(final Network network) {
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return LinkSecurity.isAllowed(owner, BuiltinPermission.EXTRACT, network, serverLevel);
    }

    public void cycleMode() {
        mode = mode.next();
        setChanged();
        refreshCountdown = 0;
        refreshData();
    }

    public int getMatchCount() {
        return matchCount;
    }

    public ItemStack getFilter(final int slot) {
        return filters.get(slot);
    }

    public Direction getOutputSide() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    // ------------------------------------------------------------- tick

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Config.SmartSorterEntry config = Platform.getConfig().getSmartSorter();
        final Network network = mainNetworkNode.getNetwork();
        if (!config.isEnabled() || network == null || serverLevel.hasNeighborSignal(worldPosition)
            || !isAuthorized(network)) {
            updateActive(false);
            matchCount = 0;
            return;
        }
        updateActive(true);
        final long budget = transferMeter.next(config.getStacksPerSecond());
        if (budget > 0) {
            move(serverLevel, network, config, budget);
        }
        if (refreshCountdown-- <= 0) {
            refreshCountdown = REFRESH_INTERVAL - 1;
            refreshData();
        }
    }

    /**
     * 按过滤规则搬物品：每条规则每次最多搬一组，速率上限由调用方给。
     */
    private void move(final ServerLevel serverLevel,
                      final Network network,
                      final Config.SmartSorterEntry config,
                      final long budget) {
        final var storage = network.getComponent(StorageNetworkComponent.class);
        final Container target = outputTarget(serverLevel);
        if (target == null) {
            return;
        }
        long remaining = budget;
        for (int slot = 0; slot < FILTER_COUNT && remaining > 0; slot++) {
            final ItemStack filter = filters.get(slot);
            if (!filter.isEmpty() && moveOne(network, config, target, storage, filter, slot)) {
                remaining--;
            }
        }
    }

    private boolean moveOne(final Network network,
                            final Config.SmartSorterEntry config,
                            final Container target,
                            final StorageNetworkComponent storage,
                            final ItemStack filter,
                            final int slot) {
        final ResourceAmount match = nextMatch(storage, filter, slot);
        if (match == null) {
            return false;
        }
        final ItemStack stack = ((ItemResource) match.resource())
            .toItemStack(Math.min(match.amount(), (long) Integer.MAX_VALUE));
        final int accepted = acceptedAmount(target, stack);
        if (accepted <= 0 || !canAfford(network, config)) {
            return false;
        }
        final long extracted = storage.extract(match.resource(), accepted, Action.EXECUTE, actor);
        if (extracted <= 0) {
            return false;
        }
        final ItemStack toMove = stack.copyWithCount((int) extracted);
        final int inserted = insertInto(target, toMove);
        final long leftover = extracted - inserted;
        if (leftover > 0) {
            // 容器在两次调用之间改变了状态：能退多少退多少，至少不静默吞掉差额。
            storage.insert(match.resource(), leftover, Action.EXECUTE, actor);
        }
        if (inserted > 0) {
            charge(network, config);
        }
        return inserted > 0;
    }

    @Nullable
    private ResourceAmount nextMatch(final StorageNetworkComponent storage, final ItemStack filter, final int slot) {
        final List<ResourceAmount> matches = new ArrayList<>();
        for (final ResourceAmount candidate : storage.getAll()) {
            if (candidate.resource() instanceof ItemResource itemResource && matches(itemResource, filter)) {
                matches.add(candidate);
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        final int index = matchOffsets[slot] % matches.size();
        matchOffsets[slot] = (index + 1) % matches.size();
        return matches.get(index);
    }

    @Nullable
    private ResourceAmount findMatch(final StorageNetworkComponent storage, final ItemStack filter) {
        for (final ResourceAmount candidate : storage.getAll()) {
            if (!(candidate.resource() instanceof ItemResource itemResource)) {
                continue;
            }
            if (matches(itemResource, filter)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean matches(final ItemResource candidate, final ItemStack filter) {
        if (mode == SorterMatchMode.EXACT) {
            return ItemResource.ofItemStack(filter).equals(candidate);
        }
        // 标签匹配：过滤器所在标签里的任何一员，都算同类。
        for (final var tag : ItemResource.ofItemStack(filter).getTags()) {
            for (final var entry : tag.resources()) {
                if (entry.equals(candidate)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 它面对着的那一格是不是容器。只认原版 {@link Container}——
     * 箱子、漏斗、熔炉、以及任何走容器接口的模组方块都覆盖。
     */
    @Nullable
    private Container outputTarget(final ServerLevel serverLevel) {
        final BlockPos targetPos = worldPosition.relative(getOutputSide());
        return serverLevel.getBlockEntity(targetPos) instanceof Container container ? container : null;
    }

    /**
     * 目标容器还吃得下多少件（先问再搬，绝不会把物品吞掉）。
     */
    private int acceptedAmount(final Container target, final ItemStack stack) {
        int accepted = 0;
        for (int slot = 0; slot < target.getContainerSize(); slot++) {
            if (!canPlaceAt(target, slot, stack)) {
                continue;
            }
            final int slotLimit = Math.min(stack.getMaxStackSize(), target.getMaxStackSize(stack));
            final ItemStack existing = target.getItem(slot);
            if (existing.isEmpty()) {
                accepted += Math.min(slotLimit, stack.getCount() - accepted);
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                final int room = Math.max(0, slotLimit - existing.getCount());
                accepted += Math.min(room, stack.getCount() - accepted);
            }
            if (accepted >= stack.getCount()) {
                return accepted;
            }
        }
        return accepted;
    }

    private int insertInto(final Container target, final ItemStack stack) {
        int left = stack.getCount();
        for (int slot = 0; slot < target.getContainerSize() && left > 0; slot++) {
            if (!canPlaceAt(target, slot, stack)) {
                continue;
            }
            final int slotLimit = Math.min(stack.getMaxStackSize(), target.getMaxStackSize(stack));
            final ItemStack existing = target.getItem(slot);
            if (existing.isEmpty()) {
                final int placedCount = Math.min(left, slotLimit);
                if (placedCount > 0) {
                    target.setItem(slot, stack.copyWithCount(placedCount));
                    left -= placedCount;
                }
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                final int room = Math.min(slotLimit - existing.getCount(), left);
                if (room > 0) {
                    existing.grow(room);
                    target.setItem(slot, existing);
                    left -= room;
                }
            }
        }
        target.setChanged();
        return stack.getCount() - left;
    }

    private boolean canPlaceAt(final Container target, final int slot, final ItemStack stack) {
        if (target instanceof WorldlyContainer worldly) {
            return worldly.canPlaceItemThroughFace(slot, stack, getOutputSide().getOpposite());
        }
        return target.canPlaceItem(slot, stack);
    }

    private boolean canAfford(final Network network, final Config.SmartSorterEntry config) {
        final long cost = config.getEnergyPerStack();
        return cost <= 0 || network.getComponent(EnergyNetworkComponent.class).getStored() >= cost;
    }

    private void charge(final Network network, final Config.SmartSorterEntry config) {
        final long cost = config.getEnergyPerStack();
        if (cost > 0) {
            network.getComponent(EnergyNetworkComponent.class).extract(cost);
        }
    }

    private void updateActive(final boolean active) {
        final BlockState state = getBlockState();
        if (state.hasProperty(SmartSorterBlock.ACTIVE)
            && state.getValue(SmartSorterBlock.ACTIVE) != active) {
            level.setBlockAndUpdate(worldPosition, state.setValue(SmartSorterBlock.ACTIVE, active));
        }
    }

    // ------------------------------------------------------------- 界面数据

    private void refreshData() {
        final Network network = mainNetworkNode.getNetwork();
        containerData.set(DATA_MODE, mode.ordinal());
        int matches = 0;
        if (network != null) {
            final var storage = network.getComponent(StorageNetworkComponent.class);
            for (int slot = 0; slot < FILTER_COUNT; slot++) {
                if (findMatch(storage, filters.get(slot)) != null) {
                    matches++;
                }
            }
        }
        matchCount = matches;
        containerData.set(DATA_MATCHES, matches);
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    // ------------------------------------------------------------- 菜单

    @Override
    public Component getDisplayName() {
        return ContentNames.SMART_SORTER;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId,
                                            final Inventory inventory,
                                            final Player player) {
        return new SmartSorterContainerMenu(syncId, inventory, this, containerData);
    }

    @Override
    public SmartSorterMenuData getMenuData() {
        return new SmartSorterMenuData(worldPosition);
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, SmartSorterMenuData> getMenuCodec() {
        return SmartSorterMenuData.STREAM_CODEC;
    }

    // ------------------------------------------------------------- Container

    @Override
    public int getContainerSize() {
        return FILTER_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (final ItemStack stack : filters) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(final int slot) {
        return filters.get(slot);
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        final ItemStack result = ContainerHelper.removeItem(filters, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slot) {
        return ContainerHelper.takeItem(filters, slot);
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        filters.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(final Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        filters.clear();
        setChanged();
    }

    // ------------------------------------------------------------- 掉落

    // ------------------------------------------------------------- NBT

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
        final CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < filters.size(); i++) {
            if (!filters.get(i).isEmpty()) {
                itemsTag.put("Slot" + i, filters.get(i).save(registries));
            }
        }
        tag.put(TAG_ITEMS, itemsTag);
        tag.putString(TAG_MODE, mode.getSerializedName());
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        final CompoundTag itemsTag = tag.getCompound(TAG_ITEMS);
        for (int i = 0; i < filters.size(); i++) {
            final int slot = i;
            final String key = "Slot" + slot;
            if (itemsTag.contains(key)) {
                ItemStack.parse(registries, itemsTag.getCompound(key))
                    .ifPresent(stack -> filters.set(slot, stack));
            }
        }
        SorterMatchMode.byName(tag.getString(TAG_MODE)).ifPresent(loaded -> mode = loaded);
    }

    /**
     * 同步给界面：匹配方式 + 命中种类数。只读，客户端不回写。
     */
    private class SmartSorterDataSlots implements ContainerData {
        @Override
        public int get(final int index) {
            return switch (index) {
                case DATA_MODE -> mode.ordinal();
                case DATA_MATCHES -> matchCount;
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            // 只读数据，客户端不回写。
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    }
}
