package com.kokuu.rswirelesscrafting.common.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 锁死的玩家背包槽位：看得见、拿不动、也放不进东西。
 *
 * <p>用在「正在编辑玩家身上某件物品」的界面里——必须固定那件物品，否则玩家能在编辑过程中
 * 把它拿走 / 拖走 / 换掉，之后的写回就落到脱离背包的旧对象上了。
 */
public class LockedSlot extends Slot {
    public LockedSlot(final Container container, final int slot, final int x, final int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPickup(final Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return false;
    }
}
