package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.Platform;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.context.ConstantContainerItemContext;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric 侧把 Team Reborn Energy 物品能力桥接给 common 模块。
 *
 * <p>任何注册了 {@code EnergyStorage.ITEM} 的物品（Refined Storage 的全部无线终端、
 * 便携终端、本 mod 的无线合成终端，以及其他 mod 的电池/装备）都会被识别，充电站天然通用。
 */
public final class PlatformImpl implements Platform.ItemEnergyProvider {
    @Override
    public Optional<Platform.ItemEnergyAccess> get(final ItemStack stack) {
        final ConstantContainerItemContext ctx = new ConstantContainerItemContext(
            ItemVariant.of(stack),
            stack.getCount()
        );
        return Optional.ofNullable(team.reborn.energy.api.EnergyStorage.ITEM.find(stack, ctx))
            .map(RebornEnergyAccess::new);
    }

    private record RebornEnergyAccess(team.reborn.energy.api.EnergyStorage storage)
        implements Platform.ItemEnergyAccess {
        @Override
        public long getStored() {
            return storage.getAmount();
        }

        @Override
        public long getCapacity() {
            return storage.getCapacity();
        }

        @Override
        public long receive(final long amount, final boolean simulate) {
            if (amount <= 0) {
                return 0;
            }
            try (Transaction tx = Transaction.openOuter()) {
                final long received = storage.insert(amount, tx);
                if (!simulate) {
                    tx.commit();
                }
                return received;
            }
        }
    }
}
