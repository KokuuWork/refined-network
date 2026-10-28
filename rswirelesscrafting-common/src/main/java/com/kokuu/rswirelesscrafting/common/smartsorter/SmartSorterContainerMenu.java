package com.kokuu.rswirelesscrafting.common.smartsorter;

import com.kokuu.rswirelesscrafting.common.Menus;

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

/**
 * 智能分拣站的界面：3×3 过滤槽 + 一个切换匹配方式的按钮。
 *
 * <p>交互全部走原版通道，**零自定义数据包**：
 * <ul>
 *   <li>放 / 取代表物品走普通槽位同步（容器直接读写方块实体的 NBT）；</li>
 *   <li>切匹配方式走 {@code clickMenuButton}（原版 {@code ServerboundContainerButtonClickPacket}）；</li>
 *   <li>匹配方式与命中种类数走 {@link ContainerData}。</li>
 * </ul>
 *
 * <p>过滤槽每格只放**一个**物品当「代表」——和 RS 原版过滤槽的语义一致。
 */
public class SmartSorterContainerMenu extends AbstractContainerMenu {
    public static final int ACTION_TOGGLE_MODE = 0;
    public static final int FILTER_COUNT = SmartSorterBlockEntity.FILTER_COUNT;
    public static final int DATA_SIZE = 2;

    // 3×3 过滤槽与匹配方式按钮的几何。坐标对界面渲染公开，
    // 供 SmartSorterScreen 画按钮时对齐（避免两处各写一份坐标导致错位）。
    public static final int FILTER_X = 62;
    public static final int FILTER_Y = 18;
    public static final int FILTER_STEP = 18;
    public static final int MODE_X = 8;
    public static final int MODE_Y = 18;
    public static final int MODE_WIDTH = 50;
    public static final int MODE_HEIGHT = 20;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 104;
    private static final int HOTBAR_Y = 158;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;

    private final Container container;
    private final ContainerData data;

    /**
     * 服务端构造：直接绑定方块实体与其实时数据。
     */
    public SmartSorterContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final Container container,
                                    final ContainerData data) {
        super(Menus.INSTANCE.getSmartSorter(), syncId);
        this.container = container;
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new FilterSlot(container, row * 3 + column, filterX(column), filterY(row)));
            }
        }
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    /**
     * 客户端构造：坐标由服务端随界面打开数据下发，方块实体从本地世界解析。
     */
    public SmartSorterContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final SmartSorterMenuData menuData) {
        this(
            syncId,
            playerInventory,
            resolveContainer(playerInventory, menuData.pos()),
            new SimpleContainerData(DATA_SIZE)
        );
    }

    private static Container resolveContainer(final Inventory playerInventory, final BlockPos pos) {
        final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SmartSorterBlockEntity sorter) {
            return sorter;
        }
        // 极端情况下客户端还没拿到方块实体（跨维度 / 刚加载）：给个空壳，
        // 界面照样能开、能关，只是那一刻看不到过滤内容。
        return new SimpleContainer(FILTER_COUNT);
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

    public static int filterX(final int column) {
        return FILTER_X + column * FILTER_STEP;
    }

    public static int filterY(final int row) {
        return FILTER_Y + row * FILTER_STEP;
    }

    public SorterMatchMode getMode() {
        final SorterMatchMode[] all = SorterMatchMode.values();
        final int ordinal = data.get(0);
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : SorterMatchMode.EXACT;
    }

    /**
     * 当前网络里被这些过滤规则命中的规则数（每 {@code REFRESH_INTERVAL} tick 刷新一次）。
     */
    public int getMatchCount() {
        return data.get(1);
    }

    @Override
    public boolean clickMenuButton(final Player player, final int id) {
        if (id != ACTION_TOGGLE_MODE) {
            return false;
        }
        if (container instanceof SmartSorterBlockEntity sorter
            && sorter.stillValid(player)
            && sorter.canOpen(player)) {
            sorter.cycleMode();
        }
        return true;
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
        if (index < FILTER_COUNT) {
            if (!moveItemStackTo(stackInSlot, FILTER_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stackInSlot, 0, FILTER_COUNT, false)) {
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
     * 过滤槽：每格只放一个「代表物品」。
     */
    private static class FilterSlot extends Slot {
        FilterSlot(final Container container, final int slot, final int x, final int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
