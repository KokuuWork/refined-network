package com.kokuu.rswirelesscrafting.fabric.mixin;

import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.ExtendedWirelessNetworkItemContext;

import java.util.List;

import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把本 mod 的「距离耗电 + 升级插件」能力注入到 RS 原生的手持无线终端。
 *
 * <p>RS 的 {@code WirelessGridItem}（无线终端）与 {@code WirelessAutocraftingMonitorItem}
 * （无线合成监视器）都继承 {@code AbstractNetworkEnergyItem}，且都没有覆写 3 参
 * {@code use(ServerPlayer, ItemStack, SlotReference)}。因此在这里：
 * <ul>
 *   <li>用 {@link ExtendedWirelessNetworkItemContext#wrap} 包一层上下文，让距离耗电与
 *       全部升级插件对这两个原生设备同样生效；</li>
 *   <li>潜行 + 右键时打开插件装配界面（本 mod 的合成终端自己覆写了 use，不受此影响）；</li>
 *   <li>悬停提示里追加「已安装插件」列表。</li>
 * </ul>
 */
@Mixin(AbstractNetworkEnergyItem.class)
public abstract class AbstractNetworkEnergyItemMixin {

    @Shadow
    protected abstract void use(Component name,
                                ServerPlayer player,
                                SlotReference slotReference,
                                NetworkItemContext context);

    /**
     * 包一层距离感知上下文，替代 RS 默认创建的 {@link NetworkItemContext}。
     */
    @Inject(
        method = "use(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;"
            + "Lcom/refinedmods/refinedstorage/common/api/support/slotreference/SlotReference;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void wrapContext(final ServerPlayer player,
                             final ItemStack stack,
                             final SlotReference slotReference,
                             final CallbackInfo ci) {
        final NetworkItemContext context = ExtendedWirelessNetworkItemContext.wrap(stack, player, slotReference);
        use(stack.get(DataComponents.CUSTOM_NAME), player, slotReference, context);
        ci.cancel();
    }

    /**
     * 潜行 + 右键打开插件装配界面。
     */
    @Inject(
        method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;"
            + "Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void sneakOpenUpgradeBay(final Level level,
                                     final Player player,
                                     final InteractionHand hand,
                                     final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!player.isCrouching() || !(player instanceof ServerPlayer serverPlayer) || level.getServer() == null) {
            return;
        }
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return;
        }
        AbstractWirelessTerminalItem.openUpgradePanel(serverPlayer, hand);
        cir.setReturnValue(InteractionResultHolder.consume(stack));
    }

    /**
     * 在 RS 原生无线终端的悬停提示末尾追加与本 mod 合成终端一致的信息：
     * 绑定位置（维度 + 坐标）、已安装插件列表、潜行装配提示。
     */
    @Inject(
        method = "appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;"
            + "Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V",
        at = @At("TAIL")
    )
    private void appendWirelessInfo(final ItemStack stack,
                                    final Item.TooltipContext tooltipContext,
                                    final List<Component> tooltip,
                                    final TooltipFlag flag,
                                    final CallbackInfo ci) {
        if (stack.getItem() instanceof AbstractWirelessTerminalItem) {
            return;
        }
        tooltip.add(Component.empty());
        AbstractWirelessTerminalItem.appendTerminalInfo(
            stack,
            tooltip,
            com.kokuu.rswirelesscrafting.common.Platform.getClientPlayer()
        );
    }
}
