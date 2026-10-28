package com.kokuu.rswirelesscrafting.common.monitor;

import com.kokuu.rswirelesscrafting.common.Menus;
import com.kokuu.rswirelesscrafting.common.link.BoundTargetResolver;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.rswirelesscrafting.common.util.LockedSlot;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 无线监控终端的界面：左侧一列「监控目标」槽，右侧实时显示**当前存量**与规则。
 *
 * <p>交互全部走原版通道，因此**不需要任何自定义数据包**：
 * <ul>
 *   <li>放物品 / 取物品走普通槽位同步（容器直接读写物品组件）；</li>
 *   <li>点阈值、点方向走 {@code clickMenuButton}（原版 {@code ServerboundContainerButtonClickPacket}）；</li>
 *   <li>当前存量走 {@code ContainerData}（原版 {@code ClientboundContainerSetDataPacket}）。</li>
 * </ul>
 *
 * <p>存量每 {@link #REFRESH_INTERVAL} tick 重算一次，属于「看得见就好」的刷新率，
 * 不会因为界面开着就持续压网络。
 */
public class WirelessMonitorContainerMenu extends AbstractContainerMenu {
    public static final int RULE_COUNT = MonitorRules.MAX_RULES;
    /** 每行 4 个 int：阈值低 32 位、阈值高 32 位、标志位、当前存量。 */
    public static final int DATA_PER_RULE = 4;
    public static final int DATA_SIZE = RULE_COUNT * DATA_PER_RULE;

    // 行几何：界面渲染与点击判定共用，避免两处各写一份导致错位。
    public static final int ROW_Y = 18;
    public static final int ROW_HEIGHT = 18;
    public static final int SLOT_X = 8;
    /** 当前存量右对齐到这条线。 */
    public static final int AMOUNT_RIGHT = 62;
    /** 阈值 / 目标数字区。 */
    public static final int THRESHOLD_X = 66;
    public static final int THRESHOLD_WIDTH = 48;
    /** 动作区（「提醒」/「补货」），点一下切换。 */
    public static final int MODE_X = 118;
    public static final int MODE_WIDTH = 50;

    // 点击动作（界面与菜单共用，必须一致）：0 = 阈值加大、1 = 阈值减小、2 = 切换「低于 / 高于」。
    /** 阈值加大一档。 */
    public static final int ACTION_THRESHOLD_UP = 0;
    /** 阈值减小一档。 */
    public static final int ACTION_THRESHOLD_DOWN = 1;
    /** 在「低于提醒 / 高于提醒」之间切换。 */
    public static final int ACTION_TOGGLE_DIRECTION = 2;
    /** 切换动作（提醒 ↔ 自动补货）。 */
    public static final int ACTION_TOGGLE_MODE = 3;
    /** 每行可用的动作数量（按钮 id = 行号 × 本值 + 动作）。 */
    public static final int ACTION_COUNT = 4;

    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 104;
    private static final int HOTBAR_Y = 158;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;
    private static final int REFRESH_INTERVAL = 10;
    private static final int FLAG_BELOW = 1;
    private static final int FLAG_PRESENT = 2;
    private static final int FLAG_RESTOCK = 4;
    private final SlotReference slotReference;
    private final Inventory playerInventory;
    /**
     * 正在被编辑的终端物品本身。菜单直接读写它身上的数据组件，所以必须保证它始终待在原槽位。
     */
    private final ItemStack terminalStack;
    private final MonitorRuleContainer rules;
    private final ContainerData data = new SimpleContainerData(DATA_SIZE);
    /**
     * 只有服务端需要去解析网络、算存量；客户端读同步过来的 {@link #data}。
     */
    private final boolean serverSide;
    private int refreshCountdown;

    public WirelessMonitorContainerMenu(final int syncId,
                                        final Inventory inventory,
                                        final SlotReference slotReference) {
        super(Menus.INSTANCE.getWirelessMonitor(), syncId);
        this.slotReference = slotReference;
        this.playerInventory = inventory;
        this.terminalStack = slotReference.resolve(inventory.player).orElse(ItemStack.EMPTY);
        this.rules = new MonitorRuleContainer(terminalStack);
        this.serverSide = !inventory.player.level().isClientSide();
        for (int i = 0; i < RULE_COUNT; i++) {
            addSlot(new MonitorSlot(rules, i, SLOT_X, ROW_Y + i * ROW_HEIGHT));
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    public WirelessMonitorContainerMenu(final int syncId,
                                        final Inventory inventory,
                                        final MonitorMenuData menuData) {
        this(syncId, inventory, menuData.slotReference());
    }

    private void addPlayerInventory(final Inventory inventory) {
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int column = 0; column < INVENTORY_COLUMNS; column++) {
                addPlayerSlot(
                    inventory,
                    column + row * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                    INVENTORY_X + column * 18,
                    INVENTORY_Y + row * 18
                );
            }
        }
        for (int column = 0; column < INVENTORY_COLUMNS; column++) {
            addPlayerSlot(inventory, column, INVENTORY_X + column * 18, HOTBAR_Y);
        }
    }

    private void addPlayerSlot(final Inventory inventory, final int inventoryIndex, final int x, final int y) {
        addSlot(slotReference.isDisabledSlot(inventoryIndex)
            ? new LockedSlot(inventory, inventoryIndex, x, y)
            : new Slot(inventory, inventoryIndex, x, y));
    }

    /**
     * 界面渲染需要的某个 int（见 {@link #DATA_PER_RULE} 的说明）。
     */
    public int getDataValue(final int index) {
        return data.get(index);
    }

    /**
     * 某行配置的阈值（从两个 int 拼回 long）。
     */
    public long getThreshold(final int slot) {
        final long low = data.get(slot * DATA_PER_RULE) & 0xFFFFFFFFL;
        final long high = data.get(slot * DATA_PER_RULE + 1);
        return (high << 32) | low;
    }

    public boolean isAlertWhenBelow(final int slot) {
        return (data.get(slot * DATA_PER_RULE + 2) & FLAG_BELOW) != 0;
    }

    public boolean hasRule(final int slot) {
        return (data.get(slot * DATA_PER_RULE + 2) & FLAG_PRESENT) != 0;
    }

    /**
     * 这一行的动作是不是「自动补货」（否则就是只提醒）。
     */
    public boolean isRestock(final int slot) {
        return (data.get(slot * DATA_PER_RULE + 2) & FLAG_RESTOCK) != 0;
    }

    public long getStoredAmount(final int slot) {
        return data.get(slot * DATA_PER_RULE + 3) & 0xFFFFFFFFL;
    }

    /**
     * 阈值 / 方向 / 存量的刷新：只在服务端算，客户端读同步值。
     */
    @Override
    public void broadcastChanges() {
        if (serverSide && refreshCountdown-- <= 0) {
            refreshCountdown = REFRESH_INTERVAL - 1;
            refreshData();
        }
        super.broadcastChanges();
    }

    private void refreshData() {
        final Network network = resolveNetwork();
        final var storages = network == null || !canRead(network)
            ? null
            : network.getComponent(StorageNetworkComponent.class);
        final var current = rules.rules();
        for (int slot = 0; slot < RULE_COUNT; slot++) {
            final MonitorRule rule = current.get(slot);
            final long threshold = rule.threshold();
            data.set(slot * DATA_PER_RULE, (int) (threshold & 0xFFFFFFFFL));
            data.set(slot * DATA_PER_RULE + 1, (int) (threshold >>> 32));
            int flags = 0;
            if (rule.alertWhenBelow()) {
                flags |= FLAG_BELOW;
            }
            if (!rule.isEmpty()) {
                flags |= FLAG_PRESENT;
            }
            if (rule.isRestock()) {
                flags |= FLAG_RESTOCK;
            }
            data.set(slot * DATA_PER_RULE + 2, flags);
            final long stored = !rule.isEmpty() && storages != null
                ? storages.get(ItemResource.ofItemStack(rule.item()))
                : 0L;
            data.set(slot * DATA_PER_RULE + 3, (int) Math.min(Integer.MAX_VALUE, stored));
        }
    }

    private boolean canRead(@Nullable final Network network) {
        return network == null || !(playerInventory.player instanceof ServerPlayer serverPlayer)
            || SecurityHelper.isAllowed(serverPlayer, BuiltinPermission.OPEN, network);
    }

    @Nullable
    private Network resolveNetwork() {
        final Level level = playerInventory.player.level();
        final GlobalPos bound = AbstractWirelessTerminalItem.getBoundLocation(terminalStack);
        if (bound == null) {
            return null;
        }
        return BoundTargetResolver.resolveNetwork(level, bound).orElse(null);
    }

    @Override
    public boolean clickMenuButton(final Player player, final int id) {
        if (id < 0 || id >= RULE_COUNT * ACTION_COUNT) {
            return false;
        }
        final int slot = id / ACTION_COUNT;
        final Network network = resolveNetwork();
        if (network != null && player instanceof ServerPlayer serverPlayer
            && !SecurityHelper.isAllowed(serverPlayer, BuiltinPermission.OPEN, network)) {
            return false;
        }
        switch (id % ACTION_COUNT) {
            case ACTION_THRESHOLD_UP -> rules.cycleThreshold(slot, true);
            case ACTION_THRESHOLD_DOWN -> rules.cycleThreshold(slot, false);
            case ACTION_TOGGLE_DIRECTION -> rules.toggleDirection(slot);
            case ACTION_TOGGLE_MODE -> rules.cycleAction(slot);
            default -> {
                return false;
            }
        }
        // 立刻回灌一次，客户端下一次收到同步时就是新值（不必等下一个刷新周期）。
        refreshCountdown = 0;
        refreshData();
        return true;
    }

    @Override
    public boolean stillValid(final Player player) {
        return slotReference.resolve(player)
            // 必须是同一个物品对象：被换掉 / 被拿走就立刻关掉界面，避免写回错误目标。
            .map(stack -> stack == terminalStack)
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
        if (index < RULE_COUNT) {
            if (!moveItemStackTo(stackInSlot, RULE_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stackInSlot, 0, RULE_COUNT, false)) {
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
     * 监控槽：每个槽只放一个物品（代表「盯着这东西」），取出来等于取消监控。
     */
    private static class MonitorSlot extends Slot {
        MonitorSlot(final Container container, final int slot, final int x, final int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
