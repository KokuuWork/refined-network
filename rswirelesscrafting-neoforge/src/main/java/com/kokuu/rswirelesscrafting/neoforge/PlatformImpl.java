package com.kokuu.rswirelesscrafting.neoforge;

import com.kokuu.rswirelesscrafting.common.Platform;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * NeoForge 侧把 Forge Energy 物品能力桥接给 common 模块。
 *
 * <p>任何注册了 {@code Capabilities.EnergyStorage.ITEM} 的物品（Refined Storage 的全部无线终端、
 * 便携终端、本 mod 的无线合成终端，以及其他 mod 的电池/装备）都会被识别，因此充电站天然通用。
 */
public final class PlatformImpl implements Platform.ItemEnergyProvider {
    @Override
    public Optional<Platform.ItemEnergyAccess> get(final ItemStack stack) {
        return Optional.ofNullable(stack.getCapability(Capabilities.EnergyStorage.ITEM))
            .map(ForgeEnergyAccess::new);
    }

    private record ForgeEnergyAccess(net.neoforged.neoforge.energy.IEnergyStorage storage)
        implements Platform.ItemEnergyAccess {
        @Override
        public long getStored() {
            return storage.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return storage.getMaxEnergyStored();
        }

        @Override
        public long receive(final long amount, final boolean simulate) {
            if (amount <= 0) {
                return 0;
            }
            return storage.receiveEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
        }
    }
}
