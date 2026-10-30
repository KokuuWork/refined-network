package com.kokuu.refinednetwork.common.upgrade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 直接读写「无线终端物品的 {@code upgrades} 数据组件」的容器视图。
 *
 * <p>升级插件不再写死在合成配方里，而是像插件一样随时插拔：这个容器把 4 个槽位映射到
 * 物品身上的升级列表，装配 = 放入槽位，拆卸 = 取出槽位，物品本体不需要重新合成。
 */
public class UpgradeContainer implements Container {
    private final ItemStack gridStack;
    private final int size = Upgrades.MAX_UPGRADES;

    public UpgradeContainer(final ItemStack gridStack) {
        this.gridStack = gridStack;
    }

    /**
     * 读取一个定长（{@value Upgrades#MAX_UPGRADES}）的升级列表快照，尾部用空栈补齐。
     */
    private List<ItemStack> snapshot() {
        final List<ItemStack> upgrades = new ArrayList<>(Upgrades.getUpgradeSlots(gridStack));
        if (upgrades.size() > size) {
            return new ArrayList<>(upgrades.subList(0, size));
        }
        while (upgrades.size() < size) {
            upgrades.add(ItemStack.EMPTY);
        }
        return upgrades;
    }

    /**
     * 写回组件时保留固定槽位：空槽留在原位，移除前一个插件不会把后面的插件左移。
     *
     * <p>悬停提示与计数走 {@link Upgrades#getUpgrades}，那里会过滤空槽，所以组件里存空位
     * 不会让玩家看到「空气插件」。
     */
    private void commit(final List<ItemStack> upgrades) {
        final List<ItemStack> slots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final ItemStack stack = i < upgrades.size() ? upgrades.get(i) : ItemStack.EMPTY;
            slots.add(stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
        if (slots.stream().allMatch(ItemStack::isEmpty)) {
            gridStack.remove(Upgrades.getUpgradesComponent());
        } else {
            gridStack.set(Upgrades.getUpgradesComponent(), slots);
        }
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return Upgrades.getUpgrades(gridStack).isEmpty();
    }

    @Override
    public ItemStack getItem(final int slot) {
        return snapshot().get(slot);
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        final List<ItemStack> upgrades = snapshot();
        final ItemStack current = upgrades.get(slot);
        if (current.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        final ItemStack taken = current.copyWithCount(Math.min(amount, current.getCount()));
        upgrades.set(slot, ItemStack.EMPTY);
        commit(upgrades);
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slot) {
        return removeItem(slot, getMaxStackSize());
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        final List<ItemStack> upgrades = snapshot();
        upgrades.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        commit(upgrades);
    }

    @Override
    public void setChanged() {
        // 改动已直接落在物品数据组件上，无需额外处理。
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        commit(List.of());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
