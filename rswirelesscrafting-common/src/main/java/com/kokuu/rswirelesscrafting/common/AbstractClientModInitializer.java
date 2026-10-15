package com.kokuu.rswirelesscrafting.common;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractClientModInitializer {
    protected static void handleInputEvents() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        final KeyMapping openWirelessTerminal = KeyMappings.INSTANCE.getOpenWirelessCraftingGrid();
        while (openWirelessTerminal != null && openWirelessTerminal.consumeClick()) {
            // 一个快捷键伺候所有无线终端（背包 / 饰品栏里的任意一件），不逼玩家记第二组按键。
            RefinedStorageApi.INSTANCE.useSlotReferencedItem(
                player,
                Items.INSTANCE.getWirelessCraftingGrid(),
                Items.INSTANCE.getWirelessPatternGrid()
            );
        }
    }
}
