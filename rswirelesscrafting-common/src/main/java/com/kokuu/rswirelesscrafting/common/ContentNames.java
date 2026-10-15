package com.kokuu.rswirelesscrafting.common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.MOD_ID;
import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createTranslation;
import static com.kokuu.rswirelesscrafting.common.ModIdentifierUtil.createTranslationKey;

public final class ContentNames {
    public static final String MOD_TRANSLATION_KEY = "mod." + MOD_ID;
    public static final String OPEN_WIRELESS_CRAFTING_GRID_TRANSLATION_KEY = createTranslationKey(
        "key",
        "open_wireless_crafting_grid"
    );
    public static final MutableComponent WIRELESS_CRAFTING_GRID = createTranslation(
        "item",
        "wireless_crafting_grid"
    );
    public static final MutableComponent WIRELESS_PATTERN_GRID = createTranslation(
        "item",
        "wireless_pattern_grid"
    );
    public static final MutableComponent RANGE_UPGRADE = createTranslation(
        "item",
        "range_upgrade"
    );
    public static final MutableComponent BATTERY_UPGRADE = createTranslation(
        "item",
        "battery_upgrade"
    );
    public static final MutableComponent INTERDIMENSIONAL_UPGRADE = createTranslation(
        "item",
        "interdimensional_upgrade"
    );
    public static final MutableComponent REMOTE_ENERGY_UPGRADE = createTranslation(
        "item",
        "remote_energy_upgrade"
    );
    public static final MutableComponent ENERGY_UPGRADE = createTranslation(
        "item",
        "energy_upgrade"
    );
    public static final MutableComponent ADVANCED_ENERGY_UPGRADE = createTranslation(
        "item",
        "advanced_energy_upgrade"
    );
    public static final MutableComponent TERMINAL_CHARGING_STATION = createTranslation(
        "block",
        "terminal_charging_station"
    );
    public static final MutableComponent WIRELESS_PATTERN_GRID_DESC = createTranslation(
        "item",
        "wireless_pattern_grid.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_AMPLIFIER = createTranslation(
        "block",
        "wireless_amplifier"
    );
    public static final MutableComponent WIRELESS_AMPLIFIER_DESC = createTranslation(
        "block",
        "wireless_amplifier.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final String AMPLIFIER_STATUS_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_amplifier.status"
    );
    public static final MutableComponent AMPLIFIER_ACTIVE = createTranslation(
        "block",
        "wireless_amplifier.status.active"
    ).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent AMPLIFIER_INACTIVE = createTranslation(
        "block",
        "wireless_amplifier.status.inactive"
    ).withStyle(ChatFormatting.GRAY);
    public static final String AMPLIFIER_RADIUS_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_amplifier.radius"
    );
    public static final String AMPLIFIER_MULTIPLIER_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_amplifier.multiplier"
    );
    public static final String AMPLIFIER_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_amplifier.energy"
    );
    public static final MutableComponent AMPLIFIER_REDSTONE_HINT = createTranslation(
        "block",
        "wireless_amplifier.redstone"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_MONITOR = createTranslation(
        "item",
        "wireless_monitor"
    );
    public static final MutableComponent WIRELESS_MONITOR_DESC = createTranslation(
        "item",
        "wireless_monitor.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_MONITOR_NO_RULES = createTranslation(
        "item",
        "wireless_monitor.no_rules"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_MONITOR_RULES_TITLE = createTranslation(
        "item",
        "wireless_monitor.rules"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_MONITOR_FAILED = createTranslation(
        "gui",
        "wireless_monitor_failed"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent MONITOR_EMPTY_HINT = createTranslation(
        "gui",
        "monitor_empty_hint"
    );
    public static final MutableComponent MONITOR_RULE_BELOW = createTranslation(
        "gui",
        "monitor_rule_below"
    );
    public static final MutableComponent MONITOR_RULE_ABOVE = createTranslation(
        "gui",
        "monitor_rule_above"
    );
    public static final String MONITOR_RULE_TOOLTIP_TRANSLATION_KEY = createTranslationKey(
        "gui",
        "monitor_rule_tooltip"
    );
    public static final String MONITOR_ALERT_BELOW_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "monitor_alert_below"
    );
    public static final String MONITOR_ALERT_ABOVE_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "monitor_alert_above"
    );
    public static final MutableComponent WIRELESS_CONFIGURATION_CARD = createTranslation(
        "item",
        "wireless_configuration_card"
    );
    public static final MutableComponent WIRELESS_CONFIGURATION_CARD_DESC = createTranslation(
        "item",
        "wireless_configuration_card.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_CONFIGURATION_CARD_HINT_COPY = createTranslation(
        "item",
        "wireless_configuration_card.hint.copy"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_CONFIGURATION_CARD_HINT_PASTE = createTranslation(
        "item",
        "wireless_configuration_card.hint.paste"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent CONFIG_CARD_HINT = createTranslation(
        "msg",
        "config_card.hint"
    ).withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent CONFIG_CARD_WRONG_TYPE = createTranslation(
        "msg",
        "config_card.wrong_type"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent CONFIG_CARD_PASTED_ONE = createTranslation(
        "msg",
        "config_card.pasted_one"
    ).withStyle(ChatFormatting.GREEN);
    public static final String CONFIG_CARD_PASTED_BATCH_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "config_card.pasted_batch"
    );
    public static final MutableComponent WIRELESS_BRIDGE = createTranslation(
        "block",
        "wireless_bridge"
    );
    public static final MutableComponent WIRELESS_BRIDGE_DESC = createTranslation(
        "block",
        "wireless_bridge.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_BRIDGE_ANCHOR_HINT = createTranslation(
        "block",
        "wireless_bridge.hint.anchor"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_BRIDGE_ENDPOINT_HINT = createTranslation(
        "block",
        "wireless_bridge.hint.endpoint"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_BRIDGE_CHUNK_HINT = createTranslation(
        "block",
        "wireless_bridge.hint.chunk"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_BRIDGE_USAGE_HINT = createTranslation(
        "block",
        "wireless_bridge.hint.usage"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final String BRIDGE_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_bridge.energy"
    );
    public static final String BRIDGE_CHUNK_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_bridge.energy.chunk"
    );
    public static final MutableComponent WIRELESS_BRIDGE_REDSTONE_HINT = createTranslation(
        "block",
        "wireless_bridge.hint.redstone"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent BRIDGE_UNPAID = createTranslation(
        "msg",
        "bridge.unpaid"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent BRIDGE_SUSPENDED = createTranslation(
        "msg",
        "bridge.suspended"
    ).withStyle(ChatFormatting.YELLOW);
    public static final String BRIDGE_STATUS_ANCHOR_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "bridge.status.anchor"
    );
    public static final String BRIDGE_STATUS_ENDPOINT_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "bridge.status.endpoint"
    );
    public static final MutableComponent BRIDGE_ANCHOR_HINT = createTranslation(
        "msg",
        "bridge.anchor_hint"
    );
    public static final MutableComponent BRIDGE_ONLINE = createTranslation(
        "msg",
        "bridge.online"
    ).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent BRIDGE_OFFLINE = createTranslation(
        "msg",
        "bridge.offline"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent BRIDGE_ANCHOR_SET = createTranslation(
        "msg",
        "bridge.anchor_set"
    ).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent BRIDGE_UNBOUND = createTranslation(
        "msg",
        "bridge.unbound"
    ).withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent WIRELESS_AUTOCRAFTER_MANAGER = createTranslation(
        "item",
        "wireless_autocrafter_manager"
    );
    public static final MutableComponent WIRELESS_AUTOCRAFTER_MANAGER_DESC = createTranslation(
        "item",
        "wireless_autocrafter_manager.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_AUTOCRAFTER_MANAGER_FAILED = createTranslation(
        "gui",
        "wireless_autocrafter_manager_failed"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent TERMINAL_NOT_ENOUGH_ENERGY = createTranslation(
        "gui",
        "terminal_not_enough_energy"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent WIRELESS_PATTERN_GRID_FAILED = createTranslation(
        "gui",
        "wireless_pattern_grid_failed"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_UPGRADES_TITLE = createTranslation(
        "gui",
        "wireless_crafting_grid_upgrades"
    );

    // Tooltips. These are stored fully styled: MutableComponent#withStyle mutates in place,
    // so styling has to be applied once here rather than at every append site.
    public static final String HUD_ENERGY_TRANSLATION_KEY = createTranslationKey("hud", "energy");
    public static final String WIRELESS_CRAFTING_GRID_BOUND_TRANSLATION_KEY = createTranslationKey(
        "item",
        "wireless_crafting_grid.bound"
    );
    public static final String WIRELESS_CRAFTING_GRID_DISTANCE_TRANSLATION_KEY = createTranslationKey(
        "item",
        "wireless_crafting_grid.distance"
    );
    public static final String WIRELESS_CRAFTING_GRID_DISTANCE_INTERDIMENSIONAL_TRANSLATION_KEY = createTranslationKey(
        "item",
        "wireless_crafting_grid.distance_interdimensional"
    );
    public static final MutableComponent WIRELESS_CRAFTING_GRID_DESC = createTranslation(
        "item",
        "wireless_crafting_grid.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_UNBOUND = createTranslation(
        "item",
        "wireless_crafting_grid.unbound"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_UPGRADES = createTranslation(
        "item",
        "wireless_crafting_grid.upgrades"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_INSTALL_HINT = createTranslation(
        "item",
        "wireless_crafting_grid.install_hint"
    ).withStyle(ChatFormatting.DARK_GRAY);

    public static final MutableComponent UPGRADE_INSTALL_HINT = createTranslation(
        "item",
        "upgrade.install_hint"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent RANGE_UPGRADE_DESC = createTranslation(
        "item",
        "range_upgrade.desc"
    ).withStyle(ChatFormatting.AQUA);
    public static final MutableComponent BATTERY_UPGRADE_DESC = createTranslation(
        "item",
        "battery_upgrade.desc"
    ).withStyle(ChatFormatting.GREEN);
    public static final String BATTERY_UPGRADE_CAPACITY_TRANSLATION_KEY = createTranslationKey(
        "item",
        "battery_upgrade.capacity"
    );
    public static final MutableComponent RANGE_UPGRADE_REQUIRES = createTranslation(
        "item",
        "range_upgrade.requires"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent INTERDIMENSIONAL_UPGRADE_DESC = createTranslation(
        "item",
        "interdimensional_upgrade.desc"
    ).withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final MutableComponent INTERDIMENSIONAL_UPGRADE_REQUIRES = createTranslation(
        "item",
        "interdimensional_upgrade.requires"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final String INTERDIMENSIONAL_UPGRADE_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "item",
        "interdimensional_upgrade.energy"
    );
    public static final MutableComponent REMOTE_ENERGY_UPGRADE_DESC = createTranslation(
        "item",
        "remote_energy_upgrade.desc"
    ).withStyle(ChatFormatting.GOLD);
    public static final MutableComponent REMOTE_ENERGY_UPGRADE_REQUIRES = createTranslation(
        "item",
        "remote_energy_upgrade.requires"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent ENERGY_UPGRADE_DESC = createTranslation(
        "item",
        "energy_upgrade.desc"
    ).withStyle(ChatFormatting.YELLOW);
    public static final String ENERGY_UPGRADE_DIVISOR_TRANSLATION_KEY = createTranslationKey(
        "item",
        "energy_upgrade.divisor"
    );
    public static final MutableComponent ADVANCED_ENERGY_UPGRADE_DESC = createTranslation(
        "item",
        "advanced_energy_upgrade.desc"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent ADVANCED_ENERGY_UPGRADE_REQUIRES = createTranslation(
        "item",
        "advanced_energy_upgrade.requires"
    ).withStyle(ChatFormatting.DARK_GRAY);

    // 充电站：界面内的两条能量条提示，以及方块物品上的动态数值行
    // （数值随配置变化，所以只在这里放 key，文案在各处按当前配置拼装）。
    public static final MutableComponent CHARGING_STATION_HELP = createTranslation(
        "block",
        "charging_station.help"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent CHARGING_STATION_BUFFER_INFO = createTranslation(
        "gui",
        "charging_station.buffer_info"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent CHARGING_STATION_WIRED_LABEL = createTranslation(
        "gui",
        "charging_station.wired_label"
    );
    public static final MutableComponent CHARGING_STATION_WIRELESS_LABEL = createTranslation(
        "gui",
        "charging_station.wireless_label"
    );
    public static final MutableComponent CHARGING_STATION_WIRELESS_CAPACITY_INFO = createTranslation(
        "block",
        "charging_station.wireless_capacity"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final String CHARGING_STATION_WIRED_TRANSLATION_KEY = createTranslationKey(
        "block",
        "charging_station.wired"
    );
    public static final String CHARGING_STATION_WIRELESS_TRANSLATION_KEY = createTranslationKey(
        "block",
        "charging_station.wireless"
    );
    public static final String CHARGING_STATION_IDLE_TRANSLATION_KEY = createTranslationKey(
        "block",
        "charging_station.idle"
    );
    public static final MutableComponent CHARGING_STATION_IDLE_DISABLED = createTranslation(
        "block",
        "charging_station.idle_disabled"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent CHARGING_STATION_REDSTONE = createTranslation(
        "block",
        "charging_station.redstone"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final String CHARGING_STATION_BUFFER_TRANSLATION_KEY = createTranslationKey(
        "block",
        "charging_station.buffer"
    );

    public static final MutableComponent DIMENSION_OVERWORLD = createTranslation(
        "item",
        "dimension.overworld"
    );
    public static final MutableComponent DIMENSION_NETHER = createTranslation(
        "item",
        "dimension.nether"
    );
    public static final MutableComponent DIMENSION_END = createTranslation(
        "item",
        "dimension.end"
    );

    private ContentNames() {
    }
}
