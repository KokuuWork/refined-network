package com.kokuu.refinednetwork.common.monitor;

import com.kokuu.refinednetwork.common.ContentNames;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * 打开「无线监控终端」界面：把终端所在的槽位引用下发给客户端，客户端据此解析出那件终端。
 *
 * <p>这里的菜单是**本模组自己的**（不像其它终端复用 RS 界面）——监控规则是我们独有的概念，
 * RS 没有对应界面可借。
 */
public class WirelessMonitorMenuProvider implements ExtendedMenuProvider<MonitorMenuData> {
    private final SlotReference slotReference;

    public WirelessMonitorMenuProvider(final SlotReference slotReference) {
        this.slotReference = slotReference;
    }

    @Override
    public MonitorMenuData getMenuData() {
        return new MonitorMenuData(slotReference);
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, MonitorMenuData> getMenuCodec() {
        return MonitorMenuData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.WIRELESS_MONITOR;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId,
                                            final Inventory inventory,
                                            final Player player) {
        return new WirelessMonitorContainerMenu(syncId, inventory, slotReference);
    }
}
