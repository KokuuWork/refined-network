package com.kokuu.rswirelesscrafting.common.monitor;

import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.rswirelesscrafting.common.virtual.VirtualTerminalSession;

import java.util.List;
import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 无线监控终端：远程盯着网络里的几样东西，越过阈值就主动提醒。
 *
 * <p>它与其他终端的定位差别在于**什么时候工作**：合成 / 样板 / 管理器都是「你想用时点开」，
 * 而监控终端是**你不看它的时候它在看**——所以它有两套入口：
 * <ul>
 *   <li>右键打开界面（计费与其它终端同一档：开界面 5 FE × 距离倍率），配置关注列表；</li>
 *   <li>被玩家带着时，{@link MonitorEngine} 按自己的节奏检查规则并执行动作（提醒 / 自动补货）。</li>
 * </ul>
 *
 * <p>提醒通道刻意用最朴素的办法（服务端直接说话），不引入自定义数据包；界面交互也全部
 * 走原版按钮与 ContainerData——这件终端虽然界面是自写的，但网络层**一个新包都没有**。
 */
public class WirelessMonitorItem extends AbstractWirelessTerminalItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(WirelessMonitorItem.class);

    public WirelessMonitorItem(final Properties properties,
                               final EnergyItemHelper energyItemHelper,
                               final NetworkItemHelper networkItemHelper) {
        super(properties, energyItemHelper, networkItemHelper);
    }

    @Override
    protected Component getTerminalDescriptionLine() {
        return ContentNames.WIRELESS_MONITOR_DESC;
    }

    @Override
    protected Component getTerminalName() {
        return ContentNames.WIRELESS_MONITOR;
    }

    @Override
    protected void openTerminal(@Nullable final Component name,
                                final ServerPlayer player,
                                final SlotReference slotReference,
                                final NetworkItemContext context) {
        try {
            final ItemStack terminal = slotReference.resolve(player).orElse(ItemStack.EMPTY);
            final VirtualTerminalSession session = new VirtualTerminalSession(terminal, context);
            // 开界面与其它终端同口径、按距离倍率结算；付不起一分不扣、也不开。
            if (!session.tryCharge(Platform.getConfig().getWirelessCraftingGrid().getOpenEnergyUsage())) {
                player.displayClientMessage(ContentNames.TERMINAL_NOT_ENOUGH_ENERGY, false);
                return;
            }
            com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(
                player,
                new WirelessMonitorMenuProvider(slotReference)
            );
        } catch (final IllegalStateException | NullPointerException | ClassCastException | LinkageError e) {
            // 与其它终端一致：出错只写日志 + 给玩家一条提示，绝不把游戏带崩。
            LOGGER.error("无线监控终端打开失败", e);
            player.displayClientMessage(ContentNames.WIRELESS_MONITOR_FAILED, false);
        }
    }

    /**
     * 提醒检查的入口。物品在玩家身上（背包 / 快捷栏 / 副手 / 饰品栏）时都会走到这里，
     * 客户端侧直接跳过——网络与规则判断只在服务端做。
     */
    @Override
    public void inventoryTick(final ItemStack stack,
                              final Level level,
                              final Entity entity,
                              final int slotId,
                              final boolean isSelected) {
        if (level instanceof ServerLevel && entity instanceof ServerPlayer player) {
            MonitorEngine.tickTerminal(player, stack);
        }
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final List<MonitorRule> rules = MonitorRules.getRaw(stack);
        if (rules.isEmpty()) {
            lines.add(ContentNames.WIRELESS_MONITOR_NO_RULES);
            return;
        }
        final MutableComponent list = ContentNames.WIRELESS_MONITOR_RULES_TITLE.copy();
        for (final MonitorRule rule : rules) {
            list.append(Component.literal("\n  · ")).append(rule.item().getHoverName());
            list.append(Component.literal(" ")).append(rule.isRestock()
                ? ContentNames.MONITOR_RULE_RESTOCK.copy().withStyle(ChatFormatting.GREEN)
                : (rule.alertWhenBelow()
                    ? ContentNames.MONITOR_RULE_BELOW.copy()
                    : ContentNames.MONITOR_RULE_ABOVE.copy()));
            list.append(Component.literal(" "))
                .append(Component.literal(Long.toString(rule.threshold())).withStyle(ChatFormatting.YELLOW));
        }
        lines.add(list);
    }
}
