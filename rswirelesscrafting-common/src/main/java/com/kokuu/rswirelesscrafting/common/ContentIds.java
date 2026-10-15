package com.kokuu.rswirelesscrafting.common;

import net.minecraft.resources.ResourceLocation;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createIdentifier;

public final class ContentIds {
    public static final ResourceLocation WIRELESS_CRAFTING_GRID = createIdentifier("wireless_crafting_grid");
    public static final ResourceLocation WIRELESS_PATTERN_GRID = createIdentifier("wireless_pattern_grid");
    public static final ResourceLocation WIRELESS_AUTOCRAFTER_MANAGER =
        createIdentifier("wireless_autocrafter_manager");
    public static final ResourceLocation WIRELESS_BRIDGE = createIdentifier("wireless_bridge");
    public static final ResourceLocation WIRELESS_CONFIGURATION_CARD =
        createIdentifier("wireless_configuration_card");
    public static final ResourceLocation WIRELESS_MONITOR = createIdentifier("wireless_monitor");
    public static final ResourceLocation WIRELESS_CRAFTING_GRID_UPGRADES =
        createIdentifier("wireless_crafting_grid_upgrades");
    public static final ResourceLocation RANGE_UPGRADE = createIdentifier("range_upgrade");
    public static final ResourceLocation BATTERY_UPGRADE = createIdentifier("battery_upgrade");
    public static final ResourceLocation INTERDIMENSIONAL_UPGRADE = createIdentifier("interdimensional_upgrade");
    public static final ResourceLocation REMOTE_ENERGY_UPGRADE = createIdentifier("remote_energy_upgrade");
    public static final ResourceLocation ENERGY_UPGRADE = createIdentifier("energy_upgrade");
    public static final ResourceLocation ADVANCED_ENERGY_UPGRADE = createIdentifier("advanced_energy_upgrade");
    public static final ResourceLocation TERMINAL_CHARGING_STATION =
        createIdentifier("terminal_charging_station");
    public static final ResourceLocation WIRELESS_AMPLIFIER = createIdentifier("wireless_amplifier");
    // 增幅塔方块与方块实体类型共用同一个 id；充电站方块实体类型与界面也共用同一个 id。
    public static final ResourceLocation CHARGING_STATION = createIdentifier("charging_station");
    public static final ResourceLocation UPGRADES = createIdentifier("upgrades");

    private ContentIds() {
    }
}
