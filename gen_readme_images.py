"""Generate the README illustrations (pure stdlib PNG in/out, no Pillow).

Sources
-------
- Vanilla item/block textures: read straight out of the 1.21.1 client jar
  (zipfile), since 1.19+ keeps them inside the jar rather than in .minecraft/assets.
- Refined Storage textures: read from a checkout of the RS sources.
- This addon's own textures: read from its resources folder.

Outputs go to docs/images/.
"""
import os
import struct
import zlib
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
OUT_DIR = os.path.join(HERE, "docs", "images")

# Both sources live outside this repository, so point at them with env vars:
#   MC_CLIENT_JAR  the 1.21.1 client jar (its assets/ tree holds the vanilla art)
#   RS_TEXTURES    .../refinedstorage-common/src/main/resources/assets/refinedstorage/textures
MC_JAR = os.environ.get("MC_CLIENT_JAR")
RS_TEXTURES = os.environ.get("RS_TEXTURES")

OWN_TEXTURES = os.path.join(
    HERE, "rswirelesscrafting-common", "src", "main", "resources",
    "assets", "rswirelesscrafting", "textures", "item",
)

CLEAR = (0, 0, 0, 0)

SLOT_FILL = (139, 139, 139, 255)
SLOT_EDGE = (55, 55, 55, 255)
SLOT_LIGHT = (255, 255, 255, 40)
RESULT_FILL = (139, 139, 139, 255)
ARROW = (70, 70, 70, 255)

# ------------------------------------------------------------------ PNG codec

def read_png_bytes(data):
    """Decode the subset of PNG used by Minecraft assets: 8-bit RGBA and
    4/8-bit palette images (Minecraft ships its own textures 4-bit indexed)."""
    w, h = struct.unpack(">II", data[16:24])
    bit_depth, colour_type = data[24], data[25]

    pos, idat, plte, trns = 8, b"", None, None
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        tag = data[pos + 4:pos + 8]
        body = data[pos + 8:pos + 8 + length]
        if tag == b"IDAT":
            idat += body
        elif tag == b"PLTE":
            plte = body
        elif tag == b"tRNS":
            trns = body
        pos += 12 + length
    raw = zlib.decompress(idat)

    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[colour_type]
    bpp = max(1, channels * bit_depth // 8)
    stride = (w * channels * bit_depth + 7) // 8

    rows, prev, i = [], bytearray(stride), 0
    for _ in range(h):
        filt = raw[i]
        i += 1
        line = bytearray(raw[i:i + stride])
        i += stride
        if filt == 1:
            for x in range(bpp, stride):
                line[x] = (line[x] + line[x - bpp]) & 0xFF
        elif filt == 2:
            for x in range(stride):
                line[x] = (line[x] + prev[x]) & 0xFF
        elif filt == 3:
            for x in range(stride):
                left = line[x - bpp] if x >= bpp else 0
                line[x] = (line[x] + (left + prev[x]) // 2) & 0xFF
        elif filt == 4:
            for x in range(stride):
                a = line[x - bpp] if x >= bpp else 0
                b = prev[x]
                c = prev[x - bpp] if x >= bpp else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pred = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[x] = (line[x] + pred) & 0xFF
        prev = line

        row = []
        if colour_type == 3:
            if plte is None:
                raise ValueError("indexed PNG without a palette")
            for x in range(w):
                if bit_depth == 8:
                    idx = line[x]
                elif bit_depth == 4:
                    byte = line[x // 2]
                    idx = (byte >> 4) if x % 2 == 0 else (byte & 0x0F)
                else:
                    raise ValueError(f"unsupported bit depth {bit_depth}")
                alpha = trns[idx] if trns is not None and idx < len(trns) else 255
                row.append((plte[idx * 3], plte[idx * 3 + 1], plte[idx * 3 + 2], alpha))
        else:
            for x in range(w):
                o = x * bpp
                if colour_type == 6:
                    row.append((line[o], line[o + 1], line[o + 2], line[o + 3]))
                elif colour_type == 2:
                    row.append((line[o], line[o + 1], line[o + 2], 255))
                elif colour_type == 0:
                    row.append((line[o],) * 3 + (255,))
                elif colour_type == 4:
                    row.append((line[o],) * 3 + (line[o + 1],))
        rows.append(row)
    return rows


def read_png(path):
    with open(path, "rb") as f:
        return read_png_bytes(f.read())


def read_zip_png(zf, name):
    return read_png_bytes(zf.read(name))


def write_png(path, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        crc = zlib.crc32(tag + data) & 0xFFFFFFFF
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", crc)

    height, width = len(pixels), len(pixels[0])
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


# ---------------------------------------------------------------- canvas ops

def canvas(w, h, fill=CLEAR):
    return [[fill] * w for _ in range(h)]


def rect(px, x0, y0, x1, y1, colour):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[y][x] = colour


def frame(px, x0, y0, x1, y1, colour):
    for x in range(x0, x1 + 1):
        px[y0][x] = colour
        px[y1][x] = colour
    for y in range(y0, y1 + 1):
        px[y][x0] = colour
        px[y][x1] = colour


def blit(px, sprite, x, y, scale):
    for sy, row in enumerate(sprite):
        for sx, colour in enumerate(row):
            if colour[3] == 0:
                continue
            for dy in range(scale):
                for dx in range(scale):
                    px[y + sy * scale + dy][x + sx * scale + dx] = colour


def overlay(base, layer):
    """Merge an RS cutout over its base texture, the way the block model stacks them."""
    out = [list(row) for row in base]
    for y, row in enumerate(layer):
        for x, colour in enumerate(row):
            if colour[3] > 0:
                out[y][x] = colour
    return out


def draw_arrow(px, x, y, length, colour):
    head = 7
    shaft_end = x + length - head
    for i in range(shaft_end - x):
        for dy in (-1, 0, 1):
            px[y + dy][x + i] = colour
    for i in range(head):
        span = max(head - 1 - i, 0)
        for dy in range(-span - 1, span + 2):
            px[y + dy][shaft_end + i] = colour


def slot(px, x, y, size, scale):
    """One Minecraft-style inventory slot holding a scaled 16x16 sprite."""
    rect(px, x, y, x + size - 1, y + size - 1, SLOT_FILL)
    frame(px, x, y, x + size - 1, y + size - 1, SLOT_EDGE)
    inner = size - 2 * scale
    offset = x + (size - inner) // 2
    return offset, y + (size - inner) // 2


# ================================================================== assets

def load_assets():
    if not MC_JAR or not os.path.exists(MC_JAR):
        raise SystemExit("set MC_CLIENT_JAR to the 1.21.1 client jar")
    if not RS_TEXTURES or not os.path.isdir(RS_TEXTURES):
        raise SystemExit("set RS_TEXTURES to the refinedstorage textures folder")

    assets = {}
    with zipfile.ZipFile(MC_JAR) as zf:
        for key, path in {
            "ender_pearl": "assets/minecraft/textures/item/ender_pearl.png",
            "nether_star": "assets/minecraft/textures/item/nether_star.png",
            "heart_of_the_sea": "assets/minecraft/textures/item/heart_of_the_sea.png",
            "elytra": "assets/minecraft/textures/item/elytra.png",
            "netherite_ingot": "assets/minecraft/textures/item/netherite_ingot.png",
            "quartz": "assets/minecraft/textures/item/quartz.png",
            "copper_ingot": "assets/minecraft/textures/item/copper_ingot.png",
            # Lightning rods are blocks, so their art only exists under textures/block.
            "lightning_rod": "assets/minecraft/textures/block/lightning_rod.png",
            "beacon": "assets/minecraft/textures/block/beacon.png",
            "redstone_block": "assets/minecraft/textures/block/redstone_block.png",
            "copper_block": "assets/minecraft/textures/block/copper_block.png",
            "blue_ice": "assets/minecraft/textures/block/blue_ice.png",
            "crafting_table": "assets/minecraft/textures/block/crafting_table_front.png",
        }.items():
            assets[key] = read_zip_png(zf, path)

    for key, path in {
        "quartz_enriched_iron": "item/quartz_enriched_iron.png",
        "advanced_processor": "item/advanced_processor.png",
        "basic_processor": "item/basic_processor.png",
        "machine_casing": "block/machine_casing/side.png",
        "blank_pattern": "item/pattern/empty.png",
        "1k_storage_block": "block/storage_block/1k_storage_block.png",
        "4k_storage_block": "block/storage_block/4k_storage_block.png",
        "64k_storage_block": "block/storage_block/64k_storage_block.png",
    }.items():
        assets[key] = read_png(os.path.join(RS_TEXTURES, path))

    # The crafting grid face is a bare screen frame; put it on a casing so it
    # reads as a device rather than an empty box.
    grid = read_png(os.path.join(RS_TEXTURES, "block", "machine_casing", "end.png"))
    overlay(grid, read_png(os.path.join(RS_TEXTURES, "block", "crafting_grid", "front.png")))
    assets["crafting_grid"] = grid

    # The transmitter and receiver are drawn as a base plate plus a coloured
    # antenna cutout; stack them so the recipe icon reads as the whole block.
    receiver_base = read_png(
        os.path.join(RS_TEXTURES, "block", "network_receiver", "base.png")
    )
    assets["network_receiver"] = overlay(
        receiver_base,
        read_png(os.path.join(RS_TEXTURES, "block", "network_receiver", "cutouts", "gray.png")),
    )
    # The transmitter ships only antenna textures — its body is a machine casing
    # in the block model — so compose the full device for the icon.
    transmitter = read_png(os.path.join(RS_TEXTURES, "block", "machine_casing", "end.png"))
    overlay(transmitter, read_png(os.path.join(RS_TEXTURES, "block", "wireless_transmitter",
                                               "base.png")))
    overlay(transmitter, read_png(os.path.join(RS_TEXTURES, "block", "wireless_transmitter",
                                               "cutouts", "gray.png")))
    assets["wireless_transmitter"] = transmitter

    # The storage monitor is a plain casing with a screen face.
    storage_monitor = read_png(os.path.join(RS_TEXTURES, "block", "machine_casing", "end.png"))
    overlay(storage_monitor, read_png(os.path.join(RS_TEXTURES, "block", "storage_monitor",
                                                   "front.png")))
    assets["storage_monitor"] = storage_monitor

    # The autocrafter manager is likewise a casing plus a coloured cutout face.
    manager_icon = read_png(os.path.join(RS_TEXTURES, "block", "machine_casing", "end.png"))
    overlay(manager_icon, read_png(os.path.join(RS_TEXTURES, "block", "autocrafter_manager",
                                                   "back.png")))
    overlay(manager_icon, read_png(os.path.join(RS_TEXTURES, "block", "autocrafter_manager",
                                                "cutouts", "gray.png")))
    assets["autocrafter_manager"] = manager_icon

    for key, path in {
        "grid_off": "wireless_crafting_grid/inactive.png",
        "grid_on": "wireless_crafting_grid/active.png",
        "pattern_off": "wireless_pattern_grid/inactive.png",
        "pattern_on": "wireless_pattern_grid/active.png",
        "manager_off": "wireless_autocrafter_manager/inactive.png",
        "manager_on": "wireless_autocrafter_manager/active.png",
        "monitor_off": "wireless_monitor/inactive.png",
        "monitor_on": "wireless_monitor/active.png",
        "card": "wireless_configuration_card.png",
        "battery": "battery_upgrade.png",
        "range": "range_upgrade.png",
        "interdim": "interdimensional_upgrade.png",
        "remote": "remote_energy_upgrade.png",
        "energy": "energy_upgrade.png",
        "advanced": "advanced_energy_upgrade.png",
    }.items():
        assets[key] = read_png(os.path.join(OWN_TEXTURES, path))

    assets["charging"] = read_png(
        os.path.join(OWN_TEXTURES, "..", "block", "terminal_charging_station.png")
    )
    assets["amplifier"] = read_png(
        os.path.join(OWN_TEXTURES, "..", "block", "wireless_amplifier_active.png")
    )
    assets["bridge_off"] = read_png(
        os.path.join(OWN_TEXTURES, "..", "block", "wireless_bridge.png")
    )
    assets["bridge_on"] = read_png(
        os.path.join(OWN_TEXTURES, "..", "block", "wireless_bridge_linked.png")
    )
    return assets


# ================================================================== figures

def items_figure(assets, path):
    order = ["grid_off", "grid_on", "pattern_off", "pattern_on", "manager_off", "manager_on",
             "battery", "energy", "advanced", "range", "interdim", "remote", "charging",
             "amplifier", "bridge_on", "card", "monitor_on"]
    scale = 8
    cell = 16 * scale + 8          # slot size, leaving a 4px inset for the sprite
    gap = 16
    margin = 16
    width = margin * 2 + cell * len(order) + gap * (len(order) - 1)
    height = margin * 2 + cell
    px = canvas(width, height)
    for i, key in enumerate(order):
        x = margin + i * (cell + gap)
        y = margin
        slot(px, x, y, cell, scale)
        blit(px, assets[key], x + 4, y + 4, scale)
    write_png(path, px)
    print("wrote", path)


def single_item_figure(assets, key, path):
    """One 8x inventory slot, for use as an inline icon next to a table row."""
    scale = 8
    cell = 16 * scale + 8
    px = canvas(cell, cell)
    slot(px, 0, 0, cell, scale)
    blit(px, assets[key], 4, 4, scale)
    write_png(path, px)
    print("wrote", path)


def recipe_figure(assets, path, pattern, result_key):
    """A 3x3 crafting grid, an arrow, and the resulting item."""
    scale = 3
    cell = 16 * scale + 8          # slot size
    gap = 2
    grid = 3 * cell + 2 * gap
    arrow_w = 24
    margin = 8
    width = margin * 2 + grid + arrow_w + cell
    height = margin * 2 + grid
    px = canvas(width, height)

    for r in range(3):
        for c in range(3):
            x = margin + c * (cell + gap)
            y = margin + r * (cell + gap)
            slot(px, x, y, cell, scale)
            key = pattern[r][c]
            if key:
                blit(px, assets[key], x + 4, y + 4, scale)

    ay = margin + grid // 2
    ax = margin + grid + 5
    draw_arrow(px, ax, ay, arrow_w - 2, ARROW)

    rx = width - margin - cell
    slot(px, rx, margin + (grid - cell) // 2, cell, scale)
    blit(px, assets[result_key], rx + 4, margin + (grid - cell) // 2 + 4, scale)
    write_png(path, px)
    print("wrote", path)


GRID_OFF = None
GRID_ON = None
RANGE = None
INTERDIM = None
E = "ender_pearl"
Q = "quartz_enriched_iron"
A = "advanced_processor"
M = "machine_casing"
G = "crafting_grid"
PT = "blank_pattern"
H = "heart_of_the_sea"
T = "wireless_transmitter"
R = "network_receiver"
X = "64k_storage_block"
B = "beacon"
N = "netherite_ingot"
S = "nether_star"
Y = "elytra"
QZ = "quartz"
RD = "redstone_block"
L1 = "1k_storage_block"
L4 = "4k_storage_block"
LR = "lightning_rod"
CB = "copper_block"
CU = "copper_ingot"
BI = "blue_ice"
BP = "basic_processor"
BAT = "battery"
REMOTE = "remote"
CHARGING = "charging"


def main():
    assets = load_assets()
    os.makedirs(OUT_DIR, exist_ok=True)

    items_figure(assets, os.path.join(OUT_DIR, "items.png"))
    for key, name in (("grid_on", "item-grid.png"),
                      ("pattern_on", "item-wireless-pattern-grid.png"),
                      ("amplifier", "item-wireless-amplifier.png"),
                      ("manager_on", "item-wireless-autocrafter-manager.png"),
                      ("bridge_on", "item-wireless-bridge.png"),
                      ("card", "item-wireless-configuration-card.png"),
                      ("monitor_on", "item-wireless-monitor.png"),
                      ("battery", "item-battery.png"),
                      ("energy", "item-energy.png"),
                      ("advanced", "item-advanced-energy.png"),
                      ("range", "item-range.png"),
                      ("interdim", "item-interdimensional.png"),
                      ("remote", "item-remote-energy.png"),
                      ("charging", "item-terminal-charging-station.png")):
        single_item_figure(assets, key, os.path.join(OUT_DIR, name))

    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-grid.png"),
        [[Q, A, Q], [E, G, E], [Q, M, Q]], "grid_off",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-wireless-pattern-grid.png"),
        [[Q, A, Q], [PT, "grid_off", PT], [Q, M, Q]], "pattern_off",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-wireless-amplifier.png"),
        [[Q, "wireless_transmitter", Q], [X, S, X], [Q, M, Q]], "amplifier",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-wireless-autocrafter-manager.png"),
        [[Q, T, Q], ["pattern_off", "autocrafter_manager", "pattern_off"], [Q, S, Q]], "manager_on",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-wireless-bridge.png"),
        [[Q, T, Q], [R, S, R], [Q, X, Q]], "bridge_on",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-wireless-monitor.png"),
        [[Q, A, Q], ["storage_monitor", "grid_off", "storage_monitor"], [Q, X, Q]], "monitor_on",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-battery.png"),
        [[E, A, E], [RD, L1, RD], [E, A, E]], "battery",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-energy.png"),
        [[CU, RD, CU], [E, BP, E], [CU, RD, CU]], "energy",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-advanced-energy.png"),
        [[Q, A, Q], [L4, "energy", L4], [Q, A, Q]], "advanced",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-range.png"),
        [[E, H, E], [T, S, R], [E, X, E]], "range",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-interdimensional.png"),
        [[E, B, X], [N, S, N], [E, Y, E]], "interdim",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-remote-energy.png"),
        [[CB, "advanced", CB], [X, S, X], [CB, LR, CB]], "remote",
    )
    recipe_figure(
        assets, os.path.join(OUT_DIR, "recipe-terminal-charging-station.png"),
        [[QZ, RD, QZ], [M, A, M], [QZ, RD, QZ]], CHARGING,
    )


if __name__ == "__main__":
    main()
