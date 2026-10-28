package com.kokuu.rswirelesscrafting.common.upgrade;

import com.kokuu.rswirelesscrafting.common.DataComponents;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createIdentifier;

public final class Upgrades {
    public static final TagKey<Item> RANGE_UPGRADES =
        TagKey.create(Registries.ITEM, createIdentifier("range_upgrades"));
    public static final TagKey<Item> BATTERY_UPGRADES =
        TagKey.create(Registries.ITEM, createIdentifier("battery_upgrades"));
    public static final TagKey<Item> INTERDIMENSIONAL_UPGRADES =
        TagKey.create(Registries.ITEM, createIdentifier("interdimensional_upgrades"));
    public static final TagKey<Item> REMOTE_ENERGY_UPGRADES =
        TagKey.create(Registries.ITEM, createIdentifier("remote_energy_upgrades"));
    public static final TagKey<Item> ENERGY_UPGRADES =
        TagKey.create(Registries.ITEM, createIdentifier("energy_upgrades"));
    public static final TagKey<Item> ADVANCED_ENERGY_UPGRADES =
        TagKey.create(Registries.ITEM, createIdentifier("advanced_energy_upgrades"));
    public static final int MAX_UPGRADES = 4;

    private static final List<TagKey<Item>> ALL_UPGRADE_TAGS = List.of(
        RANGE_UPGRADES,
        BATTERY_UPGRADES,
        INTERDIMENSIONAL_UPGRADES,
        REMOTE_ENERGY_UPGRADES,
        ENERGY_UPGRADES,
        ADVANCED_ENERGY_UPGRADES
    );

    private Upgrades() {
    }

    /**
     * 是否为可装配到无线终端上的升级插件（任意一种）。
     */
    public static boolean isUpgrade(final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return ALL_UPGRADE_TAGS.stream().anyMatch(stack::is);
    }

    public static boolean hasRangeUpgrade(final ItemStack stack) {
        return hasUpgrade(stack, RANGE_UPGRADES);
    }

    public static boolean hasBatteryUpgrade(final ItemStack stack) {
        return hasUpgrade(stack, BATTERY_UPGRADES);
    }

    public static int countBatteryUpgrades(final ItemStack stack) {
        return countUpgrades(stack, BATTERY_UPGRADES);
    }

    public static boolean hasInterdimensionalUpgrade(final ItemStack stack) {
        return hasUpgrade(stack, INTERDIMENSIONAL_UPGRADES);
    }

    /**
     * Whether the grid should draw its energy from the bound network instead of its own battery.
     */
    public static boolean hasRemoteEnergyUpgrade(final ItemStack stack) {
        return hasUpgrade(stack, REMOTE_ENERGY_UPGRADES);
    }

    public static boolean hasEnergyUpgrade(final ItemStack stack) {
        return hasUpgrade(stack, ENERGY_UPGRADES);
    }

    public static boolean hasAdvancedEnergyUpgrade(final ItemStack stack) {
        return hasUpgrade(stack, ADVANCED_ENERGY_UPGRADES);
    }

    private static boolean hasUpgrade(final ItemStack stack, final TagKey<Item> tag) {
        return getUpgrades(stack).stream().anyMatch(upgrade -> upgrade.is(tag));
    }

    public static int countUpgrades(final ItemStack stack, final TagKey<Item> tag) {
        return (int) getUpgrades(stack).stream().filter(upgrade -> upgrade.is(tag)).count();
    }

    public static List<ItemStack> getUpgrades(final ItemStack stack) {
        final List<ItemStack> all = stack.getOrDefault(DataComponents.INSTANCE.getUpgrades(), List.of());
        return new ArrayList<>(all.subList(0, Math.min(MAX_UPGRADES, all.size())));
    }

    /**
     * 每类升级插件的主题色，用于把终端悬停提示里「已安装插件」的名称高亮出来。
     * 与各插件自身描述文字的颜色保持一致。
     */
    public static ChatFormatting getColor(final ItemStack stack) {
        ChatFormatting color = ChatFormatting.YELLOW;
        if (stack.is(RANGE_UPGRADES)) {
            color = ChatFormatting.AQUA;
        } else if (stack.is(BATTERY_UPGRADES)) {
            color = ChatFormatting.GREEN;
        } else if (stack.is(INTERDIMENSIONAL_UPGRADES)) {
            color = ChatFormatting.LIGHT_PURPLE;
        } else if (stack.is(REMOTE_ENERGY_UPGRADES)) {
            color = ChatFormatting.GOLD;
        } else if (stack.is(ENERGY_UPGRADES)) {
            color = ChatFormatting.YELLOW;
        } else if (stack.is(ADVANCED_ENERGY_UPGRADES)) {
            color = ChatFormatting.RED;
        }
        return color;
    }

    public static DataComponentType<List<ItemStack>> getUpgradesComponent() {
        return DataComponents.INSTANCE.getUpgrades();
    }
}
