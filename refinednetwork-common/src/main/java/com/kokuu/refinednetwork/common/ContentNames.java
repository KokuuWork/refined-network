package com.kokuu.refinednetwork.common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

import static com.kokuu.refinednetwork.common.ModIdentifierUtil.MOD_ID;
import static com.kokuu.refinednetwork.common.ModIdentifierUtil.createTranslation;
import static com.kokuu.refinednetwork.common.ModIdentifierUtil.createTranslationKey;

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
    public static final MutableComponent OVERCLOCK_UPGRADE = createTranslation(
        "item",
        "overclock_upgrade"
    );
    public static final MutableComponent OVERCLOCK_UPGRADE_DESC = createTranslation(
        "item",
        "overclock_upgrade.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent OVERCLOCK_UPGRADE_EFFECT = createTranslation(
        "item",
        "overclock_upgrade.effect"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent OVERCLOCK_UPGRADE_HINT = createTranslation(
        "item",
        "overclock_upgrade.hint"
    ).withStyle(ChatFormatting.DARK_GRAY);
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
    public static final MutableComponent MONITOR_ACTION_ALERT = createTranslation(
        "gui",
        "monitor_action_alert"
    );
    public static final MutableComponent MONITOR_ACTION_RESTOCK = createTranslation(
        "gui",
        "monitor_action_restock"
    );
    public static final MutableComponent MONITOR_RULE_RESTOCK = createTranslation(
        "gui",
        "monitor_rule_restock"
    );
    public static final String MONITOR_TARGET_TOOLTIP_TRANSLATION_KEY = createTranslationKey(
        "gui",
        "monitor_target_tooltip"
    );
    public static final String MONITOR_MODE_TOOLTIP_TRANSLATION_KEY = createTranslationKey(
        "gui",
        "monitor_mode_tooltip"
    );
    public static final String MONITOR_RESTOCK_STARTED_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "monitor_restock_started"
    );
    public static final String MONITOR_RESTOCK_NO_PATTERN_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "monitor_restock_no_pattern"
    );
    public static final String MONITOR_RESTOCK_MISSING_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "monitor_restock_missing"
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
    public static final MutableComponent BRIDGE_BOUND = createTranslation(
        "msg",
        "bridge.bound"
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
    public static final MutableComponent WIRELESS_AUTOCRAFTER_MANAGER_FAILED =
        createTranslation("gui", "wireless_autocrafter_manager_failed").withStyle(ChatFormatting.RED);
    public static final MutableComponent TERMINAL_NOT_ENOUGH_ENERGY =
        createTranslation("gui", "terminal_not_enough_energy").withStyle(ChatFormatting.RED);
    /** 没接入网络时打开终端：只剩 3×3 合成台，不读网络也不扣电。 */
    public static final MutableComponent TERMINAL_OFFLINE =
        createTranslation("gui", "terminal_offline").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent WIRELESS_PATTERN_GRID_FAILED =
        createTranslation("gui", "wireless_pattern_grid_failed").withStyle(ChatFormatting.RED);
    public static final MutableComponent WIRELESS_CRAFTING_GRID_UPGRADES_TITLE = createTranslation(
        "gui",
        "wireless_crafting_grid_upgrades"
    );

    // ------------------------------------------------------------- 无线能量接收器

    public static final MutableComponent WIRELESS_ENERGY_RECEIVER = createTranslation(
        "block",
        "wireless_energy_receiver"
    );
    public static final MutableComponent WIRELESS_ENERGY_RECEIVER_DESC = createTranslation(
        "block",
        "wireless_energy_receiver.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_ENERGY_RECEIVER_HINT_BIND = createTranslation(
        "block",
        "wireless_energy_receiver.hint.bind"
    ).withStyle(ChatFormatting.GRAY);
    public static final String WIRELESS_ENERGY_RECEIVER_CAPACITY_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_energy_receiver.capacity"
    );
    public static final String WIRELESS_ENERGY_RECEIVER_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_energy_receiver.energy"
    );
    public static final String ENERGY_RECEIVER_BUFFER_TRANSLATION_KEY = createTranslationKey(
        "block",
        "wireless_energy_receiver.buffer"
    );
    public static final MutableComponent WIRELESS_ENERGY_RECEIVER_HINT_REDSTONE = createTranslation(
        "block",
        "wireless_energy_receiver.hint.redstone"
    ).withStyle(ChatFormatting.DARK_GRAY);
    public static final MutableComponent ENERGY_RECEIVER_UNBOUND = createTranslation(
        "msg",
        "energy_receiver.unbound"
    ).withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent ENERGY_RECEIVER_BOUND = createTranslation(
        "msg",
        "energy_receiver.bound"
    ).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent ENERGY_RECEIVER_ANCHOR_SET = createTranslation(
        "msg",
        "energy_receiver.anchor_set"
    ).withStyle(ChatFormatting.GREEN);
    public static final String ENERGY_RECEIVER_STATUS_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "energy_receiver.status"
    );
    public static final MutableComponent ENERGY_RECEIVER_ONLINE = createTranslation(
        "msg",
        "energy_receiver.online"
    ).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent ENERGY_RECEIVER_OFFLINE = createTranslation(
        "msg",
        "energy_receiver.offline"
    ).withStyle(ChatFormatting.RED);

    // ------------------------------------------------------------- 能量监控仪

    public static final MutableComponent ENERGY_MONITOR = createTranslation(
        "block",
        "energy_monitor"
    );
    public static final MutableComponent WIRELESS_ENERGY_MONITOR_DESC = createTranslation(
        "block",
        "energy_monitor.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_ENERGY_MONITOR_HINT_PLACE = createTranslation(
        "block",
        "energy_monitor.hint.place"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent WIRELESS_ENERGY_MONITOR_HINT_BIND = createTranslation(
        "block",
        "energy_monitor.hint.bind"
    ).withStyle(ChatFormatting.GRAY);
    public static final String WIRELESS_ENERGY_MONITOR_THRESHOLD_TRANSLATION_KEY = createTranslationKey(
        "block",
        "energy_monitor.threshold"
    );
    public static final String WIRELESS_ENERGY_MONITOR_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "block",
        "energy_monitor.energy"
    );
    public static final MutableComponent ENERGY_MONITOR_UNBOUND = createTranslation(
        "msg",
        "energy_monitor.unbound"
    ).withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent ENERGY_MONITOR_ANCHOR_SET = createTranslation(
        "msg",
        "energy_monitor.anchor_set"
    ).withStyle(ChatFormatting.GREEN);
    public static final String ENERGY_MONITOR_BOUND_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "energy_monitor.bound"
    );
    public static final MutableComponent ENERGY_MONITOR_UNLINKED = createTranslation(
        "msg",
        "energy_monitor.unlinked"
    ).withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent ENERGY_MONITOR_LOCAL_NETWORK = createTranslation(
        "msg",
        "energy_monitor.local_network"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent ENERGY_MONITOR_NOT_DRAINING = createTranslation(
        "msg",
        "energy_monitor.not_draining"
    ).withStyle(ChatFormatting.GREEN);
    public static final String ENERGY_MONITOR_STATUS_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "energy_monitor.status"
    );

    // ------------------------------------------------------------- 智能分拣站

    public static final MutableComponent SMART_SORTER = createTranslation(
        "block",
        "smart_sorter"
    );
    public static final MutableComponent SMART_SORTER_DESC = createTranslation(
        "block",
        "smart_sorter.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent SMART_SORTER_HINT_PLACE = createTranslation(
        "block",
        "smart_sorter.hint.place"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent SMART_SORTER_HINT_FILTER = createTranslation(
        "block",
        "smart_sorter.hint.filter"
    ).withStyle(ChatFormatting.GRAY);
    public static final String SMART_SORTER_RATE_TRANSLATION_KEY = createTranslationKey(
        "block",
        "smart_sorter.rate"
    );
    public static final String SMART_SORTER_ENERGY_TRANSLATION_KEY = createTranslationKey(
        "block",
        "smart_sorter.energy"
    );
    public static final MutableComponent SMART_SORTER_MODE_EXACT = createTranslation(
        "gui",
        "smart_sorter.mode.exact"
    );
    public static final MutableComponent SMART_SORTER_MODE_TAG = createTranslation(
        "gui",
        "smart_sorter.mode.tag"
    );
    public static final String SMART_SORTER_MODE_TOOLTIP_TRANSLATION_KEY = createTranslationKey(
        "gui",
        "smart_sorter.mode_tooltip"
    );
    public static final String SMART_SORTER_MATCHES_TRANSLATION_KEY = createTranslationKey(
        "gui",
        "smart_sorter.matches"
    );

    // ------------------------------------------------------------- 网络桥接器

    public static final MutableComponent NETWORK_BRIDGE = createTranslation(
        "block",
        "network_bridge"
    );
    public static final MutableComponent NETWORK_BRIDGE_DESC = createTranslation(
        "block",
        "network_bridge.desc"
    ).withStyle(ChatFormatting.GRAY);
    public static final MutableComponent NETWORK_BRIDGE_HINT_BIND = createTranslation(
        "block",
        "network_bridge.hint.bind"
    ).withStyle(ChatFormatting.GRAY);
    public static final String NETWORK_BRIDGE_ITEM_RATE_TRANSLATION_KEY = createTranslationKey(
        "block",
        "network_bridge.item_rate"
    );
    public static final String NETWORK_BRIDGE_ENERGY_RATE_TRANSLATION_KEY = createTranslationKey(
        "block",
        "network_bridge.energy_rate"
    );
    public static final String NETWORK_BRIDGE_FEE_TRANSLATION_KEY = createTranslationKey(
        "block",
        "network_bridge.fee"
    );
    public static final MutableComponent NETWORK_BRIDGE_UNBOUND = createTranslation(
        "msg",
        "network_bridge.unbound"
    ).withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent NETWORK_BRIDGE_ANCHOR_SET = createTranslation(
        "msg",
        "network_bridge.anchor_set"
    ).withStyle(ChatFormatting.GREEN);
    public static final String NETWORK_BRIDGE_BOUND_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "network_bridge.bound"
    );
    public static final String NETWORK_BRIDGE_STATUS_TRANSLATION_KEY = createTranslationKey(
        "msg",
        "network_bridge.status"
    );
    public static final MutableComponent NETWORK_BRIDGE_ONLINE = createTranslation(
        "msg",
        "network_bridge.online"
    ).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent NETWORK_BRIDGE_CYCLE = createTranslation(
        "msg",
        "network_bridge.cycle"
    ).withStyle(ChatFormatting.RED);
    public static final MutableComponent NETWORK_BRIDGE_OFFLINE = createTranslation(
        "msg",
        "network_bridge.offline"
    ).withStyle(ChatFormatting.RED);

    public static final MutableComponent LINK_ANCHOR_REQUIRED = createTranslation(
        "msg",
        "link.anchor_required"
    ).withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent LINK_ANCHOR_SET = createTranslation(
        "msg",
        "link.anchor_set"
    ).withStyle(ChatFormatting.GREEN);

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

    public static final MutableComponent CONNECTOR_DESC =
        createTranslation("item", "wireless_connector.desc").withStyle(ChatFormatting.GRAY);
    public static final MutableComponent CONNECTOR_USAGE =
        createTranslation("item", "wireless_connector.hint").withStyle(ChatFormatting.DARK_GRAY);
    public static final String CONNECTOR_ANCHOR_SET_KEY = createTranslationKey("msg", "connector.anchor_set");
    public static final MutableComponent CONNECTOR_CLEARED =
        createTranslation("msg", "connector.cleared").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent CONNECTOR_NEED_ANCHOR =
        createTranslation("msg", "connector.need_anchor").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent CONNECTOR_NOT_NETWORK =
        createTranslation("msg", "connector.not_network").withStyle(ChatFormatting.RED);
    public static final MutableComponent CONNECTOR_NO_PERMISSION =
        createTranslation("msg", "connector.no_permission").withStyle(ChatFormatting.RED);

    private ContentNames() {
    }
}
