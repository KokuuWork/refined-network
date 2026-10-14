package com.kokuu.rswirelesscrafting.common;

import net.minecraft.resources.ResourceLocation;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createIdentifier;

public final class ContentIds {
    public static final ResourceLocation WIRELESS_CRAFTING_GRID = createIdentifier("wireless_crafting_grid");
    public static final ResourceLocation WIRELESS_CRAFTING_GRID_UPGRADES =
        createIdentifier("wireless_crafting_grid_upgrades");
    public static final ResourceLocation RANGE_UPGRADE = createIdentifier("range_upgrade");
    public static final ResourceLocation BATTERY_UPGRADE = createIdentifier("battery_upgrade");
    public static final ResourceLocation INTERDIMENSIONAL_UPGRADE = createIdentifier("interdimensional_upgrade");
    public static final ResourceLocation REMOTE_ENERGY_UPGRADE = createIdentifier("remote_energy_upgrade");
    public static final ResourceLocation AIR_COOLED_CHARGING_STATION =
        createIdentifier("air_cooled_charging_station");
    public static final ResourceLocation LIQUID_COOLED_CHARGING_STATION =
        createIdentifier("liquid_cooled_charging_station");
    // 两种充电站共用同一个方块实体类型与同一个界面，所以这两个 id 只有一份。
    public static final ResourceLocation CHARGING_STATION = createIdentifier("charging_station");
    public static final ResourceLocation UPGRADES = createIdentifier("upgrades");

    private ContentIds() {
    }
}
