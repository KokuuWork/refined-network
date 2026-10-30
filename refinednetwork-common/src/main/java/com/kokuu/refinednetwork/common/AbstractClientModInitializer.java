package com.kokuu.refinednetwork.common;

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
            // 一个快捷键伺候全部无线终端（背包 / 饰品栏里的任意一件），不逼玩家记第二组按键。
            // 顺序即优先级：同时带多件时按「合成 → 样板 → 管理器 → 监控」取第一件，
            // 想开另一台就把它挪到更靠前的槽位。
            RefinedStorageApi.INSTANCE.useSlotReferencedItem(
                player,
                Items.INSTANCE.getWirelessCraftingGrid(),
                Items.INSTANCE.getWirelessPatternGrid(),
                Items.INSTANCE.getWirelessAutocrafterManager(),
                Items.INSTANCE.getWirelessMonitor()
            );
        }
    }
}
