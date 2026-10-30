"""Create blockstate / model / loot / recipe JSON for the four new blocks.

Also backfills the loot tables the wireless amplifier and wireless bridge were
missing (they dropped nothing when broken).
"""
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(
    HERE, "refinednetwork-common", "src", "main", "resources",
    "assets", "refinednetwork"
)
DATA = os.path.join(
    HERE, "refinednetwork-common", "src", "main", "resources",
    "data", "refinednetwork"
)


def dump(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("wrote", path)


def cube_all(name):
    return {
        "parent": "block/cube_all",
        "textures": {"all": "refinednetwork:block/" + name},
    }


# --------------------------------------------------------------- blockstates

dump(os.path.join(ASSETS, "blockstates", "wireless_energy_receiver.json"), {
    "variants": {
        "linked=false": {"model": "refinednetwork:block/wireless_energy_receiver"},
        "linked=true": {"model": "refinednetwork:block/wireless_energy_receiver_linked"},
    }
})

dump(os.path.join(ASSETS, "blockstates", "energy_monitor.json"), {
    "variants": {
        "linked=false": {"model": "refinednetwork:block/energy_monitor"},
        "linked=true,low=false": {"model": "refinednetwork:block/energy_monitor_linked"},
        "linked=true,low=true": {"model": "refinednetwork:block/energy_monitor_low"},
    }
})

dump(os.path.join(ASSETS, "blockstates", "network_bridge.json"), {
    "variants": {
        "linked=false": {"model": "refinednetwork:block/network_bridge"},
        "linked=true": {"model": "refinednetwork:block/network_bridge_linked"},
    }
})

sorter_variants = {}
for facing, yaw in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    for active in ("false", "true"):
        model = "smart_sorter_active" if active == "true" else "smart_sorter"
        sorter_variants["facing=%s,active=%s" % (facing, active)] = {
            "model": "refinednetwork:block/" + model,
            "y": yaw,
        }
dump(os.path.join(ASSETS, "blockstates", "smart_sorter.json"), {"variants": sorter_variants})

# ------------------------------------------------------------- block models

for name in (
    "wireless_energy_receiver",
    "wireless_energy_receiver_linked",
    "energy_monitor",
    "energy_monitor_linked",
    "energy_monitor_low",
    "smart_sorter",
    "smart_sorter_active",
    "network_bridge",
    "network_bridge_linked",
):
    dump(os.path.join(ASSETS, "models", "block", name + ".json"), cube_all(name))

# -------------------------------------------------------------- item models

for name in (
    "wireless_energy_receiver",
    "energy_monitor",
    "smart_sorter",
    "network_bridge",
):
    dump(os.path.join(ASSETS, "models", "item", name + ".json"), {
        "parent": "refinednetwork:block/" + name
    })

# -------------------------------------------------------------- loot tables

for name in (
    "wireless_amplifier",
    "wireless_bridge",
    "wireless_energy_receiver",
    "energy_monitor",
    "smart_sorter",
    "network_bridge",
    "wireless_redstone_link",
):
    dump(os.path.join(DATA, "loot_table", "blocks", name + ".json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "bonus_rolls": 0,
            "entries": [{
                "type": "minecraft:item",
                "name": "refinednetwork:" + name,
            }],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })

# ------------------------------------------------------------------ recipes

dump(os.path.join(DATA, "recipe", "wireless_energy_receiver.json"), {
    "type": "minecraft:crafting_shaped",
    "pattern": ["RCR", "TMT", "RCR"],
    "key": {
        "R": {"item": "minecraft:redstone_block"},
        "C": {"item": "refinedstorage:controller"},
        "T": {"item": "refinedstorage:wireless_transmitter"},
        "M": {"item": "refinedstorage:machine_casing"},
    },
    "result": {"id": "refinednetwork:wireless_energy_receiver"},
})

dump(os.path.join(DATA, "recipe", "energy_monitor.json"), {
    "type": "minecraft:crafting_shaped",
    "pattern": ["QGQ", "AMA", "QGQ"],
    "key": {
        "Q": {"item": "refinedstorage:quartz_enriched_iron"},
        "G": {"tag": "c:glass_blocks"},
        "A": {"item": "refinedstorage:advanced_processor"},
        "M": {"item": "refinedstorage:machine_casing"},
    },
    "result": {"id": "refinednetwork:energy_monitor"},
})

dump(os.path.join(DATA, "recipe", "smart_sorter.json"), {
    "type": "minecraft:crafting_shaped",
    "pattern": ["QEQ", "CMC", "QAQ"],
    "key": {
        "Q": {"item": "refinedstorage:quartz_enriched_iron"},
        "E": {"item": "refinedstorage:exporter"},
        "C": {"item": "refinedstorage:construction_core"},
        "M": {"item": "refinedstorage:machine_casing"},
        "A": {"item": "refinedstorage:advanced_processor"},
    },
    "result": {"id": "refinednetwork:smart_sorter"},
})

dump(os.path.join(DATA, "recipe", "network_bridge.json"), {
    "type": "minecraft:crafting_shaped",
    "pattern": ["QWQ", "TCT", "QXQ"],
    "key": {
        "Q": {"item": "refinedstorage:quartz_enriched_iron"},
        "W": {"item": "refinedstorage:wireless_transmitter"},
        "T": {"item": "refinedstorage:network_transmitter"},
        "C": {"item": "refinedstorage:controller"},
        "X": {"item": "refinedstorage:64k_storage_block"},
    },
    "result": {"id": "refinednetwork:network_bridge"},
})
