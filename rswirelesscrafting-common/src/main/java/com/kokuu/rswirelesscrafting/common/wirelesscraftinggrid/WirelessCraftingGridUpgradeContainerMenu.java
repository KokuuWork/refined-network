package com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid;

import com.kokuu.rswirelesscrafting.common.Menus;
import com.kokuu.rswirelesscrafting.common.upgrade.UpgradeContainer;
import com.kokuu.rswirelesscrafting.common.upgrade.Upgrades;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 无线合成终端的「升级装配」界面：4 个槽位直接映射到终端物品身上的升级组件，
 * 装配与拆卸都是即时的，物品本体不需要重新合成。
 */
public class WirelessCraftingGridUpgradeContainerMenu extends AbstractContainerMenu {
    private static final int UPGRADE_SLOT_COUNT = Upgrades.MAX_UPGRADES;
    private static final int UPGRADE_SLOT_X = 52;
    private static final int UPGRADE_SLOT_Y = 30;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;

    private final SlotReference slotReference;

    public WirelessCraftingGridUpgradeContainerMenu(final int syncId,
                                                    final Inventory playerInventory,
                                                    final SlotReference slotReference) {
        super(Menus.INSTANCE.getWirelessCraftingGridUpgrades(), syncId);
        this.slotReference = slotReference;
        final ItemStack gridStack = slotReference.resolve(playerInventory.player).orElse(ItemStack.EMPTY);
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

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int column = 0; column < INVENTORY_COLUMNS; column++) {
                addSlot(new Slot(
                    playerInventory,
                    column + row * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                    INVENTORY_X + column * 18,
                    INVENTORY_Y + row * 18
                ));
            }
        }
        for (int column = 0; column < INVENTORY_COLUMNS; column++) {
            addSlot(new Slot(playerInventory, column, INVENTORY_X + column * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return slotReference.resolve(player).isPresent();
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        final Slot slot = slots.get(index);
        if (!slot.hasItem()) {
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
