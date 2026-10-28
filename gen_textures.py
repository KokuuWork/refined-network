"""Generate the 16x16 item textures for rswirelesscrafting.

Pure standard library (struct + zlib) so it runs anywhere without Pillow.
Re-run with:  python gen_textures.py

Art is authored as character grids so the shapes stay readable in source.
"""
import os
import struct
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(
    HERE, "rswirelesscrafting-common", "src", "main", "resources",
    "assets", "rswirelesscrafting", "textures", "item"
)

W = H = 16
CLEAR = (0, 0, 0, 0)


# --------------------------------------------------------------------- PNG out

def write_png(path, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        crc = zlib.crc32(tag + data) & 0xFFFFFFFF
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", crc)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


# ------------------------------------------------------------------ colour ops

def rgb(value):
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), 255)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(4))


# ------------------------------------------------------------------- painting

def canvas():
    return [[CLEAR] * W for _ in range(H)]


def paint(pixels, art, palette):
    for y, row in enumerate(art):
        for x, char in enumerate(row):
            colour = palette.get(char)
            if colour is not None:
                pixels[y][x] = colour


def put(pixels, x, y, colour):
    pixels[y][x] = colour


# ============================================================ wireless grid ==

# Antenna top right, chamfered body, screen showing a 2x2 crafting matrix.
GRID_ART = [
    "............s...",
    "............g...",
    "............g...",
    ".KKKKKKKKKKKKKK.",
    ".KllllllllllmmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKSSSnSSSSKmK.",
    ".KmKSSSnSSSSKmK.",
    ".KmKnnnnnnnnKmK.",
    ".KmKSSSnSSSSKmK.",
    ".KmKSSSnSSSSKmK.",
    ".KmKSSSnSSSSKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmmmmmsmsmmmmK.",
    ".KddddddddddddK.",
    ".KKKKKKKKKKKKKK.",
]

GRID_PALETTE_OFF = {
    "K": rgb("#23232A"),   # body outline
    "d": rgb("#3C3C46"),   # body shadow
    "m": rgb("#50505C"),   # body mid tone
    "l": rgb("#6C6C7A"),   # body highlight
    "g": rgb("#9E9EAC"),   # antenna mast
    "s": rgb("#B0B0BE"),   # antenna tip / status LEDs
    "S": rgb("#17171D"),   # screen, unlit
    "n": rgb("#2C2C36"),   # crafting matrix grid, unlit
}

GRID_PALETTE_ON = dict(GRID_PALETTE_OFF)
GRID_PALETTE_ON.update({
    "s": rgb("#6FE0FF"),
    "S": rgb("#12313E"),
    "n": rgb("#38BBDC"),
})

GLOW = rgb("#38BBDC")


def grid_texture(active):
    px = canvas()
    paint(px, GRID_ART, GRID_PALETTE_ON if active else GRID_PALETTE_OFF)
    if active:
        # A lit screen bleeds a little light onto the surrounding bezel.
        for x in range(3, 13):
            px[4][x] = mix(px[4][x], GLOW, 0.18)
        for y in range(5, 13):
            px[y][2] = mix(px[y][2], GLOW, 0.18)
    return px


# Same body and antenna as the crafting terminal, but the screen shows a 3x2 block of
# ghost pattern cells (plus an output row) instead of a crafting matrix.
PATTERN_ART = [
    "............s...",
    "............g...",
    "............g...",
    ".KKKKKKKKKKKKKK.",
    ".KllllllllllmmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKSSSSSSSSKmK.",
    ".KmKSSnnnnSSKmK.",
    ".KmKSSnnnnSSKmK.",
    ".KmKSSSSSSSSKmK.",
    ".KmKSSnnnnSSKmK.",
    ".KmKSSnnnnSSKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmmmmmsmsmmmmK.",
    ".KddddddddddddK.",
    ".KKKKKKKKKKKKKK.",
]


def pattern_grid_texture(active):
    px = canvas()
    paint(px, PATTERN_ART, GRID_PALETTE_ON if active else GRID_PALETTE_OFF)
    if active:
        for x in range(3, 13):
            px[4][x] = mix(px[4][x], GLOW, 0.18)
        for y in range(5, 13):
            px[y][2] = mix(px[y][2], GLOW, 0.18)
    return px


# =============================================================== amplifier tower

# A lattice mast with three signal arcs on top and a widening base: "this block throws
# signal around". Drawn on a 16x16 canvas and used as a cube_all texture.
AMPLIFIER_ART = [
    "................",
    "......A.A.......",
    ".....A...A......",
    "......A.A.......",
    ".......m........",
    ".......m........",
    "......AmA.......",
    ".......m........",
    ".......m........",
    ".......m........",
    ".......m........",
    "......mmm.......",
    ".....mMMMm......",
    "....mMMMMMm.....",
    "...MMMMMMMMM....",
    "..MMMMMMMMMMM...",
]

AMPLIFIER_PALETTE_OFF = {
    "A": rgb("#3C414C"),   # signal arcs, unpowered
    "m": rgb("#7A8090"),   # mast
    "M": rgb("#5A6270"),   # base plating
}

AMPLIFIER_PALETTE_ON = dict(AMPLIFIER_PALETTE_OFF)
AMPLIFIER_PALETTE_ON.update({
    "A": rgb("#38BBDC"),   # signal arcs, lit
})


def amplifier_texture(active):
    px = canvas()
    paint(px, AMPLIFIER_ART, AMPLIFIER_PALETTE_ON if active else AMPLIFIER_PALETTE_OFF)
    return px


# ================================================================ hexagon cards

# Symmetric pointed hexagon; RS uses the same silhouette for its upgrade cards.
HEX_ROWS = {
    1: (7, 8), 2: (6, 9), 3: (5, 10), 4: (4, 11), 5: (3, 12),
    6: (2, 13), 7: (2, 13), 8: (2, 13), 9: (2, 13),
    10: (3, 12), 11: (4, 11), 12: (5, 10), 13: (6, 9), 14: (7, 8),
}


def hex_card(edge, light, dark):
    px = canvas()
    ys = sorted(HEX_ROWS)
    span = ys[-1] - ys[0]
    for y in ys:
        x0, x1 = HEX_ROWS[y]
        for x in range(x0, x1 + 1):
            if x == x0 or x == x1 or y == ys[0] or y == ys[-1]:
                px[y][x] = edge
                continue
            base = mix(light, dark, (y - ys[0]) / span)
            if x == x0 + 1:
                base = mix(base, light, 0.45)   # inner-left highlight
            elif x == x1 - 1:
                base = mix(base, dark, 0.45)    # inner-right shadow
            px[y][x] = base
    return px


def overclock_texture():
    """Two stacked chevrons pointing up: 'faster'. Warm copper, because it runs hot."""
    px = hex_card(rgb("#3A1410"), rgb("#C4522A"), rgb("#8A2F1A"))
    glyph, hot = rgb("#FFC48A"), rgb("#FFF0D8")
    rows = [
        "..AA..",
        ".AAAA.",
        "AA..AA",
        "..AA..",
        ".AAAA.",
        "AA..AA",
    ]
    for i, row in enumerate(rows):
        for j, ch in enumerate(row):
            if ch == "A":
                put(px, 5 + j, 4 + i, glyph if i < 3 else hot)
    return px


def range_texture():
    """Concentric signal rings: the card's whole effect is 'ignore distance'."""
    px = hex_card(rgb("#12303C"), rgb("#3E93B4"), rgb("#1B4457"))
    ring, core = rgb("#7FD8F2"), rgb("#C4F2FF")
    for x in range(5, 11):
        put(px, x, 5, ring)
        put(px, x, 10, ring)
    for y in range(5, 11):
        put(px, 5, y, ring)
        put(px, 10, y, ring)
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
        put(px, x, y, core)
    return px


def interdimensional_texture():
    """A door frame with a dark shaft: passage into another dimension."""
    px = hex_card(rgb("#180C2C"), rgb("#6E45AD"), rgb("#3A2260"))
    frame, shallow, deep, spark = (
        rgb("#B08CEC"), rgb("#251344"), rgb("#10061E"), rgb("#D6B8FF")
    )
    for x in range(5, 11):
        put(px, x, 4, frame)
        put(px, x, 10, frame)
    for y in range(5, 10):
        put(px, 5, y, frame)
        put(px, 10, y, frame)
    for y in range(5, 10):
        for x in range(6, 10):
            put(px, x, y, shallow)
    for y in range(5, 10):
        for x in range(7, 9):
            put(px, x, y, deep)
    put(px, 7, 6, spark)
    put(px, 8, 8, spark)
    return px


def battery_texture():
    """A cell with a bright charge level: the card's effect is 'bigger battery'."""
    px = hex_card(rgb("#0E2A16"), rgb("#4CAF6A"), rgb("#1E5631"))
    shell, cap, level, empty = (
        rgb("#9BE8B4"), rgb("#D8FFE6"), rgb("#48E07E"), rgb("#7CFFA8")
    )
    for y in range(4, 13):
        put(px, 6, y, shell)
        put(px, 10, y, shell)
    for x in range(6, 11):
        put(px, x, 4, shell)
        put(px, x, 12, shell)
    for x in range(7, 10):
        put(px, x, 3, cap)
    for y in range(9, 12):
        for x in range(7, 10):
            put(px, x, y, level)
    for x in range(7, 10):
        put(px, x, 6, empty)
    put(px, 7, 7, empty)
    put(px, 9, 7, empty)
    return px


def remote_energy_texture():
    """A bolt: the card pipes network energy over any distance."""
    px = hex_card(rgb("#3A2405"), rgb("#D9A441"), rgb("#7A5312"))
    bolt, glow = rgb("#FFE9A8"), rgb("#FFF8DC")
    for x in (8, 9):
        put(px, x, 4, bolt)
    for x in (7, 8):
        put(px, x, 5, bolt)
    for x in (6, 7):
        put(px, x, 6, bolt)
    for x in (7, 8, 9):
        put(px, x, 7, bolt)
    for x in (8, 9):
        put(px, x, 8, bolt)
    for x in (7, 8):
        put(px, x, 9, bolt)
    for x in (6, 7):
        put(px, x, 10, bolt)
    put(px, 9, 5, glow)
    put(px, 7, 8, glow)
    return px


def energy_texture():
    """A downward arrow: the card's effect is 'distance energy halved'."""
    px = hex_card(rgb("#3A2E05"), rgb("#D9B441"), rgb("#7A6312"))
    arrow, tip = rgb("#FFE9A8"), rgb("#FFF8DC")
    for x in range(5, 11):
        put(px, x, 4, arrow)
    for x in range(6, 10):
        put(px, x, 5, arrow)
    for x in range(7, 9):
        put(px, x, 6, arrow)
    for y in range(7, 11):
        put(px, 7, y, arrow)
        put(px, 8, y, arrow)
    put(px, 7, 10, tip)
    put(px, 8, 10, tip)
    return px


def advanced_energy_texture():
    """A bolt with a slash: the card's effect is 'distance energy removed'."""
    px = hex_card(rgb("#3A0A0A"), rgb("#D94A4A"), rgb("#7A1A1A"))
    bolt, slash, glow = rgb("#FFB0B0"), rgb("#FFE0E0"), rgb("#FFF0F0")
    for x in (8, 9):
        put(px, x, 4, bolt)
    for x in (7, 8):
        put(px, x, 5, bolt)
    for x in (6, 7):
        put(px, x, 6, bolt)
    for x in (7, 8, 9):
        put(px, x, 7, bolt)
    for x in (8, 9):
        put(px, x, 8, bolt)
    for x in (7, 8):
        put(px, x, 9, bolt)
    for x in (9, 8, 7, 6, 5):
        put(px, x, 14 - x, slash)
    put(px, 9, 5, glow)
    put(px, 6, 6, glow)
    return px


# ================================================================ block texture

def terminal_charging_station_texture(powered=True, wireless=True):
    """Machine casing with a dark inset panel, a glowing bolt, and two indicator
    lights: the bolt shows whether the station is powered, and a small corner
    lamp shows whether wireless charging is enabled (turned off by redstone).

    Neutral grey casing — the terminal's whole job is pushing energy.
    """
    px = canvas()
    base, shade, light = rgb("#8E8E8E"), rgb("#6A6A6A"), rgb("#B4B4B4")
    fin, fin_light = rgb("#5A5A5A"), rgb("#C2C2C2")
    if powered:
        bolt, bolt_glow = rgb("#5FE0F5"), rgb("#C9F7FF")
    else:
        bolt, bolt_glow = rgb("#3E3E3E"), rgb("#333333")
    if wireless:
        wifi = rgb("#34E0C8")
    else:
        wifi = rgb("#3A3A3A")
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[y][x] = shade
            elif x == 1 or y == 1:
                px[y][x] = light
            elif (x + y) % 4 == 0:
                px[y][x] = mix(base, shade, 0.35)
            else:
                px[y][x] = base
    # side cooling fins
    for y in range(3, 13):
        px[y][2] = fin if y % 2 == 0 else fin_light
        px[y][13] = fin if y % 2 == 0 else fin_light
    panel, panel_edge = rgb("#33323C"), rgb("#22212A")
    for y in range(3, 13):
        for x in range(3, 13):
            px[y][x] = panel
    for x in range(3, 13):
        px[3][x] = panel_edge
        px[12][x] = panel_edge
    for y in range(3, 13):
        px[y][3] = panel_edge
        px[y][12] = panel_edge
    # wireless indicator lamp (top-right corner of the panel, 2x2)
    for y in (4, 5):
        for x in (10, 11):
            px[y][x] = wifi
    # bolt (powered indicator)
    for x in (8, 9):
        put(px, x, 5, bolt)
    for x in (7, 8):
        put(px, x, 6, bolt)
    for x in (6, 7):
        put(px, x, 7, bolt)
    for x in (7, 8, 9):
        put(px, x, 8, bolt)
    for x in (8, 9):
        put(px, x, 9, bolt)
    for x in (7, 8):
        put(px, x, 10, bolt)
    put(px, 9, 6, bolt_glow)
    put(px, 7, 9, bolt_glow)
    return px


# ============================================================ autocrafter manager
#
# 与其它终端共用外壳，屏幕上是 2x2 组「合成仓格子」（每组 2x2 个点）——
# 一眼看出「这个管的是好几个仓」，而不是一块样板。

MANAGER_ART = [
    "............s...",
    "............g...",
    "............g...",
    ".KKKKKKKKKKKKKK.",
    ".KllllllllllmmK.",
    ".KmmmmmmmmmmmmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKnKnKKnKnKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKnKnKKnKnKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmmmmmsmsmmmmK.",
    ".KddddddddddddK.",
    ".KKKKKKKKKKKKKK.",
]

MANAGER_PALETTE_OFF = {
    "K": rgb("#2E323B"),
    "m": rgb("#5A6270"),
    "l": rgb("#3F5C6B"),
    "n": rgb("#6E7784"),
    "s": rgb("#3C414C"),
    "g": rgb("#7A8090"),
    "d": rgb("#3F444E"),
}

MANAGER_PALETTE_ON = dict(MANAGER_PALETTE_OFF)
MANAGER_PALETTE_ON.update({
    "l": rgb("#2E6E86"),
    "n": rgb("#38BBDC"),
    "s": rgb("#38BBDC"),
})


def manager_texture(active):
    px = canvas()
    paint(px, MANAGER_ART, MANAGER_PALETTE_ON if active else MANAGER_PALETTE_OFF)
    return px


# ==================================================================== bridge
#
# Two arcs leaning towards each other over a plinth: a link between two places.

BRIDGE_ART = [
    "................",
    "................",
    "..A..........A..",
    "..AA........AA..",
    "...AA......AA...",
    "....AA....AA....",
    ".....AA..AA.....",
    "......AAAA......",
    ".......AA.......",
    ".......AA.......",
    ".......mm.......",
    "....mMMMMMm.....",
    "...MMMMMMMMM....",
    "..MMMMMMMMMMM...",
    "..MMMMMMMMMMM...",
    "................",
]

BRIDGE_PALETTE_OFF = {
    "A": rgb("#3C414C"),
    "m": rgb("#7A8090"),
    "M": rgb("#5A6270"),
}

BRIDGE_PALETTE_ON = dict(BRIDGE_PALETTE_OFF)
BRIDGE_PALETTE_ON.update({"A": rgb("#38BBDC")})


def bridge_texture(linked):
    px = canvas()
    paint(px, BRIDGE_ART, BRIDGE_PALETTE_ON if linked else BRIDGE_PALETTE_OFF)
    return px


# ============================================================ energy receiver
#
# An intake dish opening to the sky with energy rays converging into it, and a
# bolt sitting in the bowl: "far-away power lands here, then goes into the network".

RECEIVER_ART = [
    "................",
    "..A........A....",
    "...A......A.....",
    "....A....A......",
    ".....A..A.......",
    "......AA........",
    "....mmmmmmmm....",
    "...mMMMMMMMMm...",
    "..mMMMBBBBMMMm..",
    "..mMMMBBBBMMMm..",
    "..mMMMBBBBMMMm..",
    "...mMMMMMMMMm...",
    "....mMMMMMMm....",
    ".....mmmmmm.....",
    "................",
    "................",
]

RECEIVER_PALETTE_OFF = {
    "A": rgb("#3C414C"),   # incoming rays, idle
    "m": rgb("#7A8090"),   # dish rim
    "M": rgb("#5A6270"),   # dish bowl
    "B": rgb("#3C414C"),   # energy bolt, idle
}

RECEIVER_PALETTE_ON = dict(RECEIVER_PALETTE_OFF)
RECEIVER_PALETTE_ON.update({
    "A": rgb("#38BBDC"),   # incoming rays, catching power
    "B": rgb("#5FE0F5"),   # energy bolt, lit
})


def energy_receiver_texture(linked):
    px = canvas()
    paint(px, RECEIVER_ART, RECEIVER_PALETTE_ON if linked else RECEIVER_PALETTE_OFF)
    return px


# ============================================================= energy monitor
#
# A gauge face with three bars: full when the network is healthy, a short red
# stub when it runs low. The unlinked state shows a dead screen.

MONITOR_ART = [
    "................",
    "................",
    "...KKKKKKKKKKK..",
    "...KnnnnnnnnnK..",
    "...KnBBnBBnBBK..",
    "...KnBBnBBnBBK..",
    "...KnBBnBBnBBK..",
    "...KnBBnBBnBBK..",
    "...KnBBnBBnBBK..",
    "...KnBBnBBnBBK..",
    "...KnBBnBBnBBK..",
    "...KnBBnBBnBBK..",
    "...KnnnnnnnnnK..",
    "...KKKKKKKKKKK..",
    "................",
    "................",
]

MONITOR_LOW_ART = [
    "................",
    "................",
    "...KKKKKKKKKKK..",
    "...KnnnnnnnnnK..",
    "...KnnnnnnnnnK..",
    "...KnnnnnnnnnK..",
    "...KnnnnnnnnnK..",
    "...KnnnnnnnnnK..",
    "...KnnnnnnnnnK..",
    "...KnnnnnnnnnK..",
    "...KnnnnnnnnnK..",
    "...KnBBnBBnBBK..",
    "...KnnnnnnnnnK..",
    "...KKKKKKKKKKK..",
    "................",
    "................",
]

MONITOR_PALETTE_UNLINKED = {
    "K": rgb("#2E323B"),
    "n": rgb("#3C414C"),
    "B": rgb("#3C414C"),
}

MONITOR_PALETTE_LINKED = dict(MONITOR_PALETTE_UNLINKED)
MONITOR_PALETTE_LINKED.update({
    "n": rgb("#6E7784"),
    "B": rgb("#38BBDC"),
})

MONITOR_PALETTE_LOW = dict(MONITOR_PALETTE_LINKED)
MONITOR_PALETTE_LOW.update({"B": rgb("#D94A4A")})


def energy_monitor_texture(linked, low):
    if not linked:
        palette = MONITOR_PALETTE_UNLINKED
        art = MONITOR_ART
    elif low:
        palette = MONITOR_PALETTE_LOW
        art = MONITOR_LOW_ART
    else:
        palette = MONITOR_PALETTE_LINKED
        art = MONITOR_ART
    px = canvas()
    paint(px, art, palette)
    return px


# ================================================================ smart sorter
#
# A hopper throat over a machine body with a lit intake slot: items come in the
# top, get checked against the filters, and leave through the slot.

SORTER_ART = [
    "................",
    "................",
    "...KKKKKKKKKK...",
    "...KmmmmmmmmK...",
    "...KmKKKKKKmK...",
    "...KmKnnnnKmK...",
    "...KmKKKKKKmK...",
    "..KKKKKKKKKKKK..",
    "..KmmmmmmmmmmK..",
    "..KmKKKKKKKKmK..",
    "..KmKnnnnnnKmK..",
    "..KmKKKKKKKKmK..",
    "..KmmmmmmmmmmK..",
    "..KKKKKKKKKKKK..",
    "................",
    "................",
]

SORTER_PALETTE = {
    "K": rgb("#2E323B"),
    "m": rgb("#5A6270"),
    "n": rgb("#3C414C"),
}

SORTER_PALETTE_ACTIVE = dict(SORTER_PALETTE)
SORTER_PALETTE_ACTIVE.update({"n": rgb("#38BBDC")})

SORTER_FRONT_ART = [
    "................",
    "...KKKKKKKKKK...",
    "...KmmmmmmmmK...",
    "...KmKKKKKKmK...",
    "...KmKnnnnKmK...",
    "...KmKKKKKKmK...",
    "...KmmmmmmmmK...",
    "...KmmmAAmmmK...",
    "...KmmmmAAmmK...",
    "...KmmmmmAAmK...",
    "...KmmmmAAmmK...",
    "...KmmmAAmmmK...",
    "...KmmmmmmmmK...",
    "...KKKKKKKKKK...",
    "................",
    "................",
]
SORTER_FRONT_PALETTE = dict(SORTER_PALETTE)
SORTER_FRONT_PALETTE.update({"A": rgb("#8995A6")})
SORTER_FRONT_PALETTE_ACTIVE = dict(SORTER_FRONT_PALETTE)
SORTER_FRONT_PALETTE_ACTIVE.update({"A": rgb("#38BBDC")})


def smart_sorter_front_texture(active):
    px = canvas()
    paint(px, SORTER_FRONT_ART,
          SORTER_FRONT_PALETTE_ACTIVE if active else SORTER_FRONT_PALETTE)
    return px


def smart_sorter_texture(active):
    px = canvas()
    paint(px, SORTER_ART, SORTER_PALETTE_ACTIVE if active else SORTER_PALETTE)
    return px


# ============================================================== network bridge
#
# Two ports on a plinth with a beam running between them: two separate networks,
# joined through this block.

NETWORK_BRIDGE_ART = [
    "................",
    "................",
    "..KKK......KKK..",
    "..KmK......KmK..",
    "..KmK..AA..KmK..",
    "..KmK.AAAA.KmK..",
    "..KmK..AA..KmK..",
    "..KmK......KmK..",
    "..KKK......KKK..",
    "...MMMMMMMMMM...",
    "...MMMMMMMMMM...",
    "....MMMMMMMM....",
    ".....MMMMMM.....",
    "................",
    "................",
    "................",
]

NETWORK_BRIDGE_PALETTE_OFF = {
    "A": rgb("#3C414C"),   # link beam, idle
    "m": rgb("#3C414C"),   # port interior
    "K": rgb("#2E323B"),   # port frame
    "M": rgb("#5A6270"),   # plinth
}

NETWORK_BRIDGE_PALETTE_ON = dict(NETWORK_BRIDGE_PALETTE_OFF)
NETWORK_BRIDGE_PALETTE_ON.update({
    "A": rgb("#38BBDC"),   # link beam, live
    "m": rgb("#6E7784"),   # port interior, powered
})


def network_bridge_texture(linked):
    px = canvas()
    paint(px, NETWORK_BRIDGE_ART,
          NETWORK_BRIDGE_PALETTE_ON if linked else NETWORK_BRIDGE_PALETTE_OFF)
    return px


# ======================================================== configuration card
#
# A card at the bottom with a pair of signal arcs above it: "settings, delivered".

CARD_ART = [
    "................",
    "................",
    "..A..........A..",
    "...AA......AA...",
    "....AA....AA....",
    ".....AA..AA.....",
    "......AAAA......",
    "................",
    "..KKKKKKKKKKKK..",
    "..KllllllllllK..",
    "..KlKKKKKKKKlK..",
    "..KlKnnnnnnKlK..",
    "..KlKnnnnnnKlK..",
    "..KlKKKKKKKKlK..",
    "..KllllllllllK..",
    "..KKKKKKKKKKKK..",
]

CARD_PALETTE = {
    "A": rgb("#38BBDC"),
    "K": rgb("#2E323B"),
    "l": rgb("#3F5C6B"),
    "n": rgb("#6E7784"),
}


def configuration_card_texture():
    px = canvas()
    paint(px, CARD_ART, CARD_PALETTE)
    return px


# =================================================================== monitor
#
# Same shell as the other terminals; the screen shows a short watch list with one
# row flagged (the amber mark) - "it is telling you something".

MONITOR_ART = [
    "............s...",
    "............g...",
    "............g...",
    ".KKKKKKKKKKKKKK.",
    ".KllllllllllmmK.",
    ".KmmmmmmmmmmmmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKnnnnnnnnKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKnnnnnAAAKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmKnnnnnnnnKmK.",
    ".KmKKKKKKKKKKmK.",
    ".KmmmmmsmsmmmmK.",
    ".KddddddddddddK.",
    ".KKKKKKKKKKKKKK.",
]

MONITOR_PALETTE_OFF = {
    "K": rgb("#2E323B"),
    "m": rgb("#5A6270"),
    "l": rgb("#3F5C6B"),
    "s": rgb("#3C414C"),
    "g": rgb("#7A8090"),
    "d": rgb("#3F444E"),
    "n": rgb("#6E7784"),
    "A": rgb("#3C414C"),
}

MONITOR_PALETTE_ON = dict(MONITOR_PALETTE_OFF)
MONITOR_PALETTE_ON.update({
    "l": rgb("#2E6E86"),
    "s": rgb("#38BBDC"),
    "n": rgb("#8E97A6"),
    "A": rgb("#E0A030"),
})


def monitor_texture(active):
    px = canvas()
    paint(px, MONITOR_ART, MONITOR_PALETTE_ON if active else MONITOR_PALETTE_OFF)
    return px


# ====================================================================== driver

def main():
    targets = [
        ("wireless_crafting_grid/inactive.png", grid_texture(False)),
        ("wireless_crafting_grid/active.png", grid_texture(True)),
        ("wireless_pattern_grid/inactive.png", pattern_grid_texture(False)),
        ("wireless_pattern_grid/active.png", pattern_grid_texture(True)),
        ("wireless_configuration_card.png", configuration_card_texture()),
        ("wireless_monitor/inactive.png", monitor_texture(False)),
        ("wireless_monitor/active.png", monitor_texture(True)),
        ("wireless_autocrafter_manager/inactive.png", manager_texture(False)),
        ("wireless_autocrafter_manager/active.png", manager_texture(True)),
        ("range_upgrade.png", range_texture()),
        ("overclock_upgrade.png", overclock_texture()),
        ("battery_upgrade.png", battery_texture()),
        ("interdimensional_upgrade.png", interdimensional_texture()),
        ("remote_energy_upgrade.png", remote_energy_texture()),
        ("energy_upgrade.png", energy_texture()),
        ("advanced_energy_upgrade.png", advanced_energy_texture()),
    ]
    for name, pixels in targets:
        path = os.path.join(OUT, name)
        write_png(path, pixels)
        print("wrote", path)

    block_out = os.path.join(HERE, "rswirelesscrafting-common", "src", "main", "resources",
                             "assets", "rswirelesscrafting", "textures", "block")
    os.makedirs(block_out, exist_ok=True)
    amplifier_textures = [
        ("wireless_amplifier.png", amplifier_texture(False)),
        ("wireless_amplifier_active.png", amplifier_texture(True)),
    ]
    for name, pixels in amplifier_textures:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)

    bridge_textures = [
        ("wireless_bridge.png", bridge_texture(False)),
        ("wireless_bridge_linked.png", bridge_texture(True)),
    ]
    for name, pixels in bridge_textures:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)

    station_textures = [
        ("terminal_charging_station.png", terminal_charging_station_texture(True, True)),
        ("terminal_charging_station_wireless_off.png", terminal_charging_station_texture(True, False)),
        ("terminal_charging_station_unpowered.png", terminal_charging_station_texture(False, True)),
        ("terminal_charging_station_unpowered_wireless_off.png",
         terminal_charging_station_texture(False, False)),
    ]
    for name, pixels in station_textures:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)

    receiver_textures = [
        ("wireless_energy_receiver.png", energy_receiver_texture(False)),
        ("wireless_energy_receiver_linked.png", energy_receiver_texture(True)),
    ]
    for name, pixels in receiver_textures:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)

    monitor_textures = [
        ("energy_monitor.png", energy_monitor_texture(False, False)),
        ("energy_monitor_linked.png", energy_monitor_texture(True, False)),
        ("energy_monitor_low.png", energy_monitor_texture(True, True)),
    ]
    for name, pixels in monitor_textures:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)

    sorter_textures = [
        ("smart_sorter.png", smart_sorter_texture(False)),
        ("smart_sorter_active.png", smart_sorter_texture(True)),
        ("smart_sorter_front.png", smart_sorter_front_texture(False)),
        ("smart_sorter_front_active.png", smart_sorter_front_texture(True)),
    ]
    for name, pixels in sorter_textures:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)

    network_bridge_textures = [
        ("network_bridge.png", network_bridge_texture(False)),
        ("network_bridge_linked.png", network_bridge_texture(True)),
    ]
    for name, pixels in network_bridge_textures:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)


if __name__ == "__main__":
    main()
