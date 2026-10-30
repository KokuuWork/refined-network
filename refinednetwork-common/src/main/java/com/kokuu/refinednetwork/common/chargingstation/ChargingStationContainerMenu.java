package com.kokuu.refinednetwork.common.chargingstation;

import com.kokuu.refinednetwork.common.Menus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChargingStationContainerMenu extends AbstractContainerMenu {
    public static final int WIRED_SLOT_COUNT = 3;
    public static final int DATA_SIZE = 10;

    // 连接面板：3 个有线槽（放物品）+ 3 个无线链接灯。坐标对界面渲染公开，
    // 供 ChargingStationScreen 画状态灯时对齐（避免两处各写一份坐标导致错位）。
    public static final int[] WIRED_SLOT_X = {44, 80, 116};
    public static final int WIRED_SLOT_Y = 17;
    public static final int[] WIRELESS_LIGHT_X = {44, 80, 116};
    public static final int WIRELESS_LIGHT_Y = 40;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;

    private final Container container;
    private final ContainerData data;

    /**
     * 服务端构造：直接绑定方块实体与其实时数据。
     */
    public ChargingStationContainerMenu(final int syncId,
                                        final Inventory playerInventory,
                                        final Container container,
                                        final ContainerData data) {
        super(Menus.INSTANCE.getChargingStation(), syncId);
        this.container = container;
        this.data = data;
        for (int i = 0; i < WIRED_SLOT_X.length; i++) {
            addSlot(new Slot(container, i, WIRED_SLOT_X[i], WIRED_SLOT_Y));
        }
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    /**
     * 客户端构造：坐标由服务端随界面打开数据下发，方块实体从本地世界解析。
     */
    public ChargingStationContainerMenu(final int syncId,
                                        final Inventory playerInventory,
                                        final ChargingStationData chargingStationData) {
        this(
            syncId,
            playerInventory,
            resolveContainer(playerInventory, chargingStationData.pos()),
            new SimpleContainerData(DATA_SIZE)
        );
    }

    private static Container resolveContainer(final Inventory playerInventory, final BlockPos pos) {
        final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ChargingStationBlockEntity station) {
            return station;
        }
        return new SimpleContainer(WIRED_SLOT_COUNT);
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

    public int getDataValue(final int index) {
        return data.get(index);
    }

    @Override
    public boolean stillValid(final Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        final Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        final ItemStack stackInSlot = slot.getItem();
        final ItemStack original = stackInSlot.copy();
        if (index < WIRED_SLOT_COUNT) {
            if (!moveItemStackTo(stackInSlot, WIRED_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stackInSlot, 0, WIRED_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
