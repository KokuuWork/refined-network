package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.Menus;
import com.kokuu.rswirelesscrafting.common.upgrade.UpgradeContainer;
import com.kokuu.rswirelesscrafting.common.upgrade.Upgrades;
import com.kokuu.rswirelesscrafting.common.util.LockedSlot;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 无线终端的「插件装配」界面：4 个槽位直接映射到终端物品身上的升级组件，
 * 装配与拆卸都是即时的，物品本体不需要重新合成。
 */
public class WirelessCraftingGridUpgradeContainerMenu extends AbstractContainerMenu {
    private static final int UPGRADE_SLOT_COUNT = Upgrades.MAX_UPGRADES;
    // 4 个槽宽 72，居中即起点 53；背景图画的凹陷框在 (x-1, y-1)。
    private static final int UPGRADE_SLOT_X = 53;
    private static final int UPGRADE_SLOT_Y = 31;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;

    private final SlotReference slotReference;
    /**
     * 正在被编辑的终端物品本身。菜单直接读写它身上的数据组件，所以必须保证它始终
     * 待在原槽位——一旦被玩家拿走，后续写回就会落到脱离背包的旧对象上。
     */
    private final ItemStack gridStack;

    public WirelessCraftingGridUpgradeContainerMenu(final int syncId,
                                                    final Inventory playerInventory,
                                                    final SlotReference slotReference) {
        super(Menus.INSTANCE.getWirelessCraftingGridUpgrades(), syncId);
        this.slotReference = slotReference;
        this.gridStack = slotReference.resolve(playerInventory.player).orElse(ItemStack.EMPTY);
        final Container container = new UpgradeContainer(gridStack);
        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            addSlot(new UpgradeSlot(container, i, UPGRADE_SLOT_X + i * 18, UPGRADE_SLOT_Y));
        }
        addPlayerInventory(playerInventory);
    }

    public WirelessCraftingGridUpgradeContainerMenu(final int syncId,
                                                    final Inventory playerInventory,
                                                    final UpgradeMenuData data) {
        this(syncId, playerInventory, data.slotReference());
    }

    /**
     * 玩家背包槽位：终端所在的那一格会被锁死（{@link LockedSlot}），
     * 否则玩家能在装配界面里把正在编辑的终端直接拿走 / 拖走 / 换掉。
     */
    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int column = 0; column < INVENTORY_COLUMNS; column++) {
                addPlayerSlot(
                    playerInventory,
                    column + row * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                    INVENTORY_X + column * 18,
                    INVENTORY_Y + row * 18
                );
            }
        }
        for (int column = 0; column < INVENTORY_COLUMNS; column++) {
            addPlayerSlot(playerInventory, column, INVENTORY_X + column * 18, HOTBAR_Y);
        }
    }

    private void addPlayerSlot(final Inventory playerInventory, final int inventoryIndex, final int x, final int y) {
        addSlot(slotReference.isDisabledSlot(inventoryIndex)
            ? new LockedSlot(playerInventory, inventoryIndex, x, y)
            : new Slot(playerInventory, inventoryIndex, x, y));
    }

    @Override
    public boolean stillValid(final Player player) {
        return slotReference.resolve(player)
            // 必须是同一个物品对象：被换掉 / 被拿走就立刻关掉界面，避免写回错误目标。
            .map(stack -> stack == gridStack)
            .orElse(false);
    }

    /**
     * 数字键 1–9 与快捷栏交换同样是「移动终端」，必须一并拦掉。
     */
    @Override
    public void clicked(final int id, final int dragType, final ClickType clickType, final Player player) {
        if (clickType == ClickType.SWAP && slotReference.isDisabledSlot(dragType)) {
            return;
        }
        super.clicked(id, dragType, clickType, player);
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        if (slot instanceof LockedSlot) {
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        final Slot slot = slots.get(index);
        if (!slot.hasItem() || slot instanceof LockedSlot) {
            return ItemStack.EMPTY;
        }
        final ItemStack stackInSlot = slot.getItem();
        final ItemStack original = stackInSlot.copy();
        if (index < UPGRADE_SLOT_COUNT) {
            if (!moveItemStackTo(stackInSlot, UPGRADE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stackInSlot, 0, UPGRADE_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    /**
     * 升级槽只接受升级卡。
     */
    private static class UpgradeSlot extends Slot {
        UpgradeSlot(final Container container, final int slot, final int x, final int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return Upgrades.isUpgrade(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
