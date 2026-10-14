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


# ====================================================================== driver

def main():
    targets = [
        ("wireless_crafting_grid/inactive.png", grid_texture(False)),
        ("wireless_crafting_grid/active.png", grid_texture(True)),
        ("range_upgrade.png", range_texture()),
        ("interdimensional_upgrade.png", interdimensional_texture()),
    ]
    for name, pixels in targets:
        path = os.path.join(OUT, name)
        write_png(path, pixels)
        print("wrote", path)


if __name__ == "__main__":
    main()
