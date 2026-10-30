package com.kokuu.refinednetwork.fabric.mixin;

import com.kokuu.refinednetwork.common.virtual.VirtualMenuHolder;

import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerContainerMenu;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 RS 的「自动合成管理器」菜单能配上**虚拟方块实体**（无线合成管理器要用）。
 *
 * <p>该菜单原本用 {@code Container.stillValidBlockEntity(autocrafterManager, player)} 判有效性，
 * 其中的 {@code level.getBlockEntity(pos) != be} 恒等检查会让虚拟方块实体一开菜单就被关掉。
 * 这里照抄 RS 自家无线菜单的写法（{@code WirelessAutocraftingMonitorContainerMenu} 直接
 * {@code return true}），只在我们打过标记的菜单上放行，方块版行为一字不变。
 *
 * <p>{@code stillValid} 是 MC 自己的方法，名字在映射表里，所以 Fabric 侧的注解处理器也收得下。
 */
@Mixin(AutocrafterManagerContainerMenu.class)
public abstract class AbstractAutocrafterManagerContainerMenuMixin implements VirtualMenuHolder {
    @Unique
    private boolean refinednetworkVirtual;

    @Override
    public void refinednetworkMarkVirtual() {
        this.refinednetworkVirtual = true;
    }

    @Override
    public boolean refinednetworkIsVirtual() {
        return this.refinednetworkVirtual;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void refinednetworkStillValid(final Player player, final CallbackInfoReturnable<Boolean> cir) {
        if (this.refinednetworkVirtual) {
            cir.setReturnValue(true);
        }
    }
}
