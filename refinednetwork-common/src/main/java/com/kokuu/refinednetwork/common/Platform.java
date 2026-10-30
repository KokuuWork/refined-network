package com.kokuu.refinednetwork.common;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static java.util.Objects.requireNonNull;

public final class Platform {
    @Nullable
    private static Supplier<Config> configProvider = null;
    @Nullable
    private static ItemEnergyProvider itemEnergyProvider = null;
    @Nullable
    private static ClientPlayerProvider clientPlayerProvider = null;

    private Platform() {
    }

    public static void setConfigProvider(final Supplier<Config> configProvider) {
        Platform.configProvider = configProvider;
    }

    public static void setItemEnergyProvider(final ItemEnergyProvider itemEnergyProvider) {
        Platform.itemEnergyProvider = itemEnergyProvider;
    }

    public static void setClientPlayerProvider(final ClientPlayerProvider clientPlayerProvider) {
        Platform.clientPlayerProvider = clientPlayerProvider;
    }

    public static Config getConfig() {
        return requireNonNull(configProvider, "Config isn't loaded yet").get();
    }

    /**
     * 客户端玩家（用于 tooltip 里实时计算距离/收费）。服务端或客户端尚未就绪时返回 null，
     * 调用方须按「无法获取玩家」优雅降级（不显示距离信息）。
     */
    @Nullable
    public static Player getClientPlayer() {
        return clientPlayerProvider == null ? null : clientPlayerProvider.get();
    }

    /**
     * 读取物品自身携带的能量（Forge Energy）。平台无关：各 loader 用各自的能量能力实现。
     *
     * @return 该物品的能量访问器；物品不携带能量时为空
     */
    public static Optional<ItemEnergyAccess> getItemEnergy(final ItemStack stack) {
        return requireNonNull(itemEnergyProvider, "Item energy provider isn't loaded yet").get(stack);
    }

    /**
     * 把「物品携带的能量」抽象出来，避免 common 模块直接依赖 loader 专属 API。
     */
    public interface ItemEnergyAccess {
        long getStored();

        long getCapacity();

        /**
         * @return 实际（或模拟）接收到的能量
         */
        long receive(long amount, boolean simulate);
    }

    public interface ItemEnergyProvider {
        Optional<ItemEnergyAccess> get(ItemStack stack);
    }

    public interface ClientPlayerProvider {
        @Nullable
        Player get();
    }
}

