"""Generate the 16x16 textures for rswirelesscrafting.

Pure standard library (struct + zlib) so it runs anywhere without Pillow.
Re-run with:  python gen_textures.py

Block system - "one casing, six faces":
  casing_frame(tone) -> panel(tone) -> contents. Every face of every block uses
  the same rim (1px dark border + 1px mid-grey ring + phase-identical plate
  noise) and the same recessed panel at x3..12 / y3..12. Light direction is
  carried by tone (top lightest, sides mid, bottom darkest) rather than a baked
  bevel, so one side texture works on all four sides. Side motifs reuse the
  front's structural x-coordinates, so a machine's skeleton wraps the corner.

Style guide - "RS2 heritage, wireless-crafting identity":
- Palette is anchored on Refined Storage 2: pure neutral greys (no blue
  tint) for every body/casing, dark charcoal inset screens with a lighter
  frame, and colour reserved for small status elements.
- Our own identity on top: a cyan signal ramp (#93F0FF lit / #38BBDC core)
  used ONLY for wireless/network energy, plus one screen glyph per terminal
  and one vivid glyph hue per upgrade card - so the set stays recognisable
  as "this mod" while reading as RS2 at a glance.
- 1px outline around item silhouettes; blocks use the RS2 casing recipe:
  dark 1px border, top-left lit bevel, subtle deterministic noise.
- State language: cyan = linked/active, mid-grey = idle, red = low power,
  white = powered, amber reserved for alerts only.
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


def put(pixels, x, y, colour):
    pixels[y][x] = colour


# ---------------------------------------------------------- unified palette
# Anchored on RS2 2.0.7 textures (side.png, machine_casing, wireless_grid,
# upgrade cards). Pure neutral greys, low contrast, tiny colour accents.

OUTLINE = rgb("#2C2C2C")      # item silhouette outline (RS2 card outline)

# item body ramp (RS2 wireless_grid handheld: #BCBCBC ... #474747)
BODY_HI = rgb("#BCBCBC")      # top/left specular edge
BODY_L = rgb("#8F8F8F")       # lit body edge
BODY_M = rgb("#6E6E6E")       # body field
BODY_D = rgb("#5C5C5C")       # body shade
BODY_S = rgb("#474747")       # bezel / deep shade

# RS2 screen recipe
SCREEN_OFF = rgb("#343434")   # unlit screen (RS2 inactive)
SCREEN_SHEEN = rgb("#3E3E3E")  # top sheen row on a screen
FRAME = rgb("#5C5C5C")        # screen/cutout frame (RS2 relay/grid)
PANEL = rgb("#3D3D3D")        # inset panel interior (RS2 grid front)
PANEL_DEEP = rgb("#2E2E2E")   # recessed shadow inside a panel

# signal colours (our identity, lit tone borrowed from RS2's #93F0FF screens)
CYAN_LIT = rgb("#93F0FF")     # brightest lit element
CYAN_CORE = rgb("#38BBDC")    # primary wireless/network cyan
CYAN_DIM = rgb("#1E6E86")     # dimmed signal line

WHITE = rgb("#E8E8E8")        # powered state
RED = rgb("#E10005")          # low power / alert (RS2 regulator red)
AMBER = rgb("#FBC304")        # alerts only (RS2 fortune amber)

# block casing recipe (RS2 side.png / machine_casing)
CAS_BORDER = rgb("#3E3E3E")   # 1px outer border
CAS_HI = rgb("#8A8A8A")       # top/left bevel
CAS_FIELD = rgb("#747474")    # plate field
CAS_NOISE_D = rgb("#6C6C6C")  # deterministic noise, darker
CAS_NOISE_L = rgb("#7C7C7C")  # deterministic noise, lighter
CAS_SHADE = rgb("#525252")    # bottom/right bevel

# glyph tones
GLYPH_ON = CYAN_LIT           # lit glyph
GLYPH_ON2 = CYAN_CORE         # secondary lit glyph
GLYPH_ONL = CYAN_DIM          # lit grid line
GLYPH_OFF = rgb("#5A5A5A")    # unlit glyph
GLYPH_OFF_HI = rgb("#6E6E6E")  # unlit glyph highlight
GLYPH_OFF_LO = rgb("#424242")  # unlit grid line


# ============================================================ terminal shell ==
#
# Every handheld terminal (crafting grid, pattern grid, monitor, manager)
# shares the RS2 wireless_grid silhouette: compact grey tablet, thin
# antenna mast at the top right, dark charcoal screen and a button band.
# Recognition: screen glyph + lit/unlit screen content.

def terminal_shell(active, glyph_fn):
    px = canvas()
    # antenna: LED tip + mast (RS2 puts it at x=10)
    put(px, 10, 0, CYAN_LIT if active else BODY_L)
    put(px, 10, 1, BODY_D)
    put(px, 10, 2, BODY_D)
    if active:
        put(px, 9, 0, mix(CYAN_LIT, CLEAR, 0.6))
        put(px, 11, 0, mix(CYAN_LIT, CLEAR, 0.6))
    # body outline: x3..12, y3..13
    for x in range(3, 13):
        put(px, x, 3, OUTLINE)
        put(px, x, 13, OUTLINE)
    for y in range(3, 14):
        put(px, 3, y, OUTLINE)
        put(px, 12, y, OUTLINE)
    # chamfer corners
    put(px, 3, 3, CLEAR)
    put(px, 12, 3, CLEAR)
    put(px, 3, 13, CLEAR)
    put(px, 12, 13, CLEAR)
    put(px, 4, 3, OUTLINE)
    put(px, 11, 3, OUTLINE)
    put(px, 4, 13, OUTLINE)
    put(px, 11, 13, OUTLINE)
    # body fill, lit from the top-left
    for y in range(4, 13):
        for x in range(4, 12):
            if y == 4:
                c = BODY_HI
            elif y == 12:
                c = BODY_S
            elif x == 4:
                c = BODY_L
            elif x == 11:
                c = BODY_S
            else:
                c = BODY_M
            put(px, x, y, c)
    # screen bezel ring (x4..11, y5..10 screen inside)
    for x in range(4, 12):
        put(px, x, 4, BODY_S)
        put(px, x, 11, BODY_S)
    for y in range(5, 11):
        put(px, 4, y, BODY_S)
        put(px, 11, y, BODY_S)
    # screen
    for y in range(5, 11):
        for x in range(5, 11):
            put(px, x, y, SCREEN_OFF)
    for x in range(5, 11):
        put(px, x, 5, SCREEN_SHEEN)
    # screen contents
    glyph_fn(px, active)
    # button band with a status LED
    for x in range(5, 11):
        put(px, x, 12, BODY_D if x % 2 == 0 else BODY_S)
    put(px, 5, 12, CYAN_LIT if active else rgb("#3E3E3E"))
    return px


# ------------------------------------------------------------- screen glyphs
# Screen area: x5..10, y5..10 (6x6 usable). Kept from our previous design so
# the terminals stay recognisable, recoloured to the RS2 dark-screen recipe.

def glyph_crafting(px, active):
    """2x2 crafting matrix with cross grid lines."""
    hi = GLYPH_ON if active else GLYPH_OFF_HI
    on = GLYPH_ON2 if active else GLYPH_OFF
    line = GLYPH_ONL if active else GLYPH_OFF_LO
    for (ox, oy) in ((5, 6), (8, 6), (5, 9), (8, 9)):
        for dy in range(2):
            for dx in range(2):
                put(px, ox + dx, oy + dy, hi if (ox, oy) == (5, 6) else on)
    for y in range(5, 11):
        put(px, 7, y, line)
    for x in range(5, 11):
        put(px, x, 8, line)


def glyph_pattern(px, active):
    """3x3 pattern dots feeding a solid result block."""
    dot = GLYPH_ON if active else GLYPH_OFF_HI
    dim = GLYPH_ON2 if active else GLYPH_OFF
    for gy in (5, 7, 9):
        for gx in (5, 7, 9):
            put(px, gx, gy, dot if (gx, gy) != (7, 7) else dim)
    put(px, 10, 7, GLYPH_ONL if active else GLYPH_OFF_LO)
    for y in (9, 10):
        for x in (10, 11):
            put(px, x, y, dot)


def glyph_monitor(px, active):
    """A short watch list with one amber-flagged row."""
    row = GLYPH_ON2 if active else GLYPH_OFF
    bullet = GLYPH_ON if active else GLYPH_OFF_HI
    for y in (6, 8, 10):
        for x in range(6, 11):
            put(px, x, y, row)
        put(px, 5, y, bullet)
    if active:
        put(px, 10, 8, AMBER)


def glyph_manager(px, active):
    """Four crafter bays linked by a central bus."""
    on = GLYPH_ON if active else GLYPH_OFF
    bus = GLYPH_ONL if active else GLYPH_OFF_LO
    for (ox, oy) in ((5, 5), (9, 5), (5, 8), (9, 8)):
        for dy in range(2):
            for dx in range(2):
                put(px, ox + dx, oy + dy, on)
    for y in range(5, 11):
        put(px, 7, y, bus)
        put(px, 8, y, bus)


# ------------------------------------------------------- terminal textures

def wireless_crafting_grid_texture(active):
    return terminal_shell(active, glyph_crafting)


def wireless_pattern_grid_texture(active):
    return terminal_shell(active, glyph_pattern)


def wireless_monitor_texture(active):
    return terminal_shell(active, glyph_monitor)


def wireless_autocrafter_manager_texture(active):
    return terminal_shell(active, glyph_manager)


# ================================================================ hexagon cards
#
# RS2 upgrade card recipe: neutral grey body with a top-light ramp
# (#CBCBCB -> #515151) and a 1px #2C2C2C outline. Our identity: one vivid
# glyph hue per card (RS2 does exactly this with its coloured glyphs).

HEX_ROWS = {
    1: (7, 8), 2: (6, 9), 3: (5, 10), 4: (4, 11), 5: (3, 12),
    6: (2, 13), 7: (2, 13), 8: (2, 13), 9: (2, 13),
    10: (3, 12), 11: (4, 11), 12: (5, 10), 13: (6, 9), 14: (7, 8),
}


def hex_card():
    px = canvas()
    light, dark = rgb("#CBCBCB"), rgb("#515151")
    filled = [[False] * W for _ in range(H)]
    ys = sorted(HEX_ROWS)
    span = ys[-1] - ys[0]
    for y in ys:
        x0, x1 = HEX_ROWS[y]
        for x in range(x0, x1 + 1):
            filled[y][x] = True
            base = mix(light, dark, (y - ys[0]) / span)
            if x == x0 + 1 and y < ys[0] + 6:
                base = mix(base, rgb("#E4E4E4"), 0.55)   # specular, top-left
            elif x == x1 - 1 and y > ys[-1] - 6:
                base = mix(base, dark, 0.35)             # inner-right shadow
            px[y][x] = base
    # unified 1px outline around the silhouette
    def inside(x, y):
        return 0 <= x < W and 0 <= y < H and filled[y][x]
    for y in range(H):
        for x in range(W):
            if filled[y][x] and not (inside(x - 1, y) and inside(x + 1, y)
                                     and inside(x, y - 1) and inside(x, y + 1)):
                px[y][x] = OUTLINE
    return px


def overclock_texture():
    """Two stacked chevrons pointing up: 'faster'. Warm copper glyph."""
    px = hex_card()
    glyph, hot = rgb("#FF9D5C"), rgb("#FFD0A0")
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
    px = hex_card()
    ring, core = rgb("#58D0E8"), rgb("#C8F4FC")
    for dx in range(-3, 4):
        for dy in range(-3, 4):
            d = abs(dx) + abs(dy)
            if d == 3:
                put(px, 8 + dx, 8 + dy, ring)
            elif d <= 1:
                put(px, 8 + dx, 8 + dy, core)
    return px


def interdimensional_texture():
    """A door frame with a dark shaft: passage into another dimension."""
    px = hex_card()
    frame, shallow, deep, spark = (
        rgb("#B088F0"), rgb("#2A1650"), rgb("#140A28"), rgb("#E8D8FF")
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
    px = hex_card()
    shell, cap, level, empty = (
        rgb("#A8E8B8"), rgb("#E4FFE8"), rgb("#50E084"), rgb("#88ECA0")
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


def _bolt(px, colour, glow):
    for x in (8, 9):
        put(px, x, 4, colour)
    for x in (7, 8):
        put(px, x, 5, colour)
    for x in (6, 7):
        put(px, x, 6, colour)
    for x in (7, 8, 9):
        put(px, x, 7, colour)
    for x in (8, 9):
        put(px, x, 8, colour)
    for x in (7, 8):
        put(px, x, 9, colour)
    for x in (6, 7):
        put(px, x, 10, colour)
    put(px, 9, 5, glow)
    put(px, 7, 8, glow)


def remote_energy_texture():
    """A bolt: the card pipes network energy over any distance. RS2 amber."""
    px = hex_card()
    _bolt(px, AMBER, rgb("#FFE890"))
    return px


def energy_texture():
    """A fat down arrow over a divide bar: the distance rate is halved.
    White glyph on the plain grey card - the entry-level upgrade."""
    px = hex_card()
    arrow, tip, bar = rgb("#D8D8D8"), rgb("#FFFFFF"), rgb("#9E9E9E")
    for y in range(4, 7):
        put(px, 7, y, arrow)
        put(px, 8, y, arrow)
    for x in range(6, 10):
        put(px, x, 7, arrow)
    for x in range(5, 11):
        put(px, x, 8, arrow)
    put(px, 7, 8, tip)
    put(px, 8, 8, tip)
    for x in range(5, 11):
        put(px, x, 11, bar)
    return px


def advanced_energy_texture():
    """A bolt with a slash: the card's effect is 'distance energy removed'.
    RS2 regulator red."""
    px = hex_card()
    bolt, slash = rgb("#FF7078"), rgb("#FFFFFF")
    _bolt(px, bolt, rgb("#FFD0D4"))
    for x in (9, 8, 7, 6, 5):
        put(px, x, 14 - x, slash)
    return px


# ======================================================== configuration card
#
# A grey smart card with an inset data panel and signal arcs converging
# onto it from above: "settings, delivered".

def configuration_card_texture():
    px = canvas()
    # converging signal arcs
    for y in range(2, 7):
        for dx in (0, 1):
            put(px, y + dx, y, CYAN_CORE)
            put(px, 14 - y + dx, y, CYAN_CORE)
    # card body outline (x2..13, y8..15)
    for x in range(2, 14):
        put(px, x, 8, OUTLINE)
        put(px, x, 15, OUTLINE)
    for y in range(8, 16):
        put(px, 2, y, OUTLINE)
        put(px, 13, y, OUTLINE)
    # body fill, lit from the top-left
    for y in range(9, 15):
        for x in range(3, 13):
            if y == 9:
                c = BODY_HI
            elif y == 14:
                c = BODY_S
            elif x == 3:
                c = BODY_L if y < 12 else BODY_M
            elif x == 12:
                c = BODY_S
            else:
                c = BODY_M
            put(px, x, y, c)
    # inset data panel (x4..11, y10..13)
    for x in range(4, 12):
        put(px, x, 10, BODY_S)
        put(px, x, 13, BODY_S)
    for y in range(10, 14):
        put(px, 4, y, BODY_S)
        put(px, 11, y, BODY_S)
    for y in range(11, 13):
        for x in range(5, 11):
            put(px, x, y, PANEL_DEEP if y == 11 else rgb("#242424"))
    # data stripes
    for x in (5, 7, 9):
        put(px, x, 11, CYAN_CORE)
    put(px, 6, 12, CYAN_LIT)
    put(px, 8, 12, CYAN_DIM)
    return px


# ================================================================ block texture
#
# ONE CASING, SIX FACES OF THE SAME CONSTRUCTION.
#
# Every face is built in the same three steps, so the cube reads as a single
# machined object instead of six unrelated tiles:
#
#   1. casing_frame(tone) - identical rim on all faces: 1px dark border, 1px
#      mid-grey ring, then the plate field with a deterministic noise pattern
#      that is phase-identical on every face (so grain lines up across edges).
#      The rim is symmetric (no directional highlight) because one side texture
#      is shared by north/south/east/west and must not look "lit from nowhere".
#   2. panel(tone)        - one recessed panel, always at x3..12 / y3..12, with
#      the same frame and the same 1px shadow on the top/left inner edge.
#   3. contents           - the functional element, drawn inside 3..12.
#
# Tone carries the light direction instead of a baked bevel: top panel is the
# lightest, sides sit in the middle, bottom is the darkest. That gives a clear
# top -> sides -> bottom gradation while every face keeps the same recipe.
#
# Continuity across edges comes from structure, not decoration: side motifs
# reuse the exact x coordinates of the front's structural parts (mast, pylons,
# pedestals, conduits), so a machine's skeleton appears to wrap around the
# corner. Vent slots use one shared rhythm on side and top faces.

CAS_RING = rgb("#7E7E7E")      # second ring of the rim (continuous on edges)
PANEL_TOP_IN = rgb("#575757")  # panel interior, lit face
PANEL_BOT_IN = rgb("#313131")  # panel interior, shadowed face
PANEL_EDGE = rgb("#3A3A3A")    # inner shadow of the recess
PANEL_SIDE_IN = rgb("#4C4C4C")  # panel interior, sides
PART_LO = rgb("#3A3A3A")        # structural shadow (reads on the panel)
PART_EDGE = rgb("#2C2C2C")      # structural outline
BORE = rgb("#2C2C2C")          # deep hole inside a panel
SLOT = rgb("#1E1E1E")          # slot / opening


def casing_frame(tone="side"):
    """The shared casing rim + plate field. tone: top / side / bottom."""
    px = canvas()
    if tone == "top":
        field = mix(CAS_FIELD, rgb("#8E8E8E"), 0.20)
        border, ring = CAS_BORDER, rgb("#828282")
    elif tone == "bottom":
        field = mix(CAS_FIELD, rgb("#3E3E3E"), 0.26)
        border, ring = rgb("#343434"), rgb("#666666")
    else:
        field, border, ring = CAS_FIELD, CAS_BORDER, CAS_RING
    for y in range(H):
        for x in range(W):
            c = field
            if (x * 5 + y * 3) % 9 == 0:
                c = mix(field, CAS_NOISE_D, 0.5)
            elif (x * 3 + y * 7) % 13 == 0:
                c = mix(field, CAS_NOISE_L, 0.5)
            px[y][x] = c
    for i in (0, 15):
        for k in range(W):
            px[i][k] = border
            px[k][i] = border
    for i in (1, 14):
        for k in range(1, 15):
            px[i][k] = ring
            px[k][i] = ring
    return px


def panel(px, tone="side"):
    """The shared recessed panel: frame at 2..13, interior at 3..12."""
    frame = rgb("#6A6A6A") if tone != "bottom" else rgb("#4E4E4E")
    for x in range(2, 14):
        px[2][x] = PANEL_EDGE
        px[13][x] = frame
    for y in range(2, 14):
        px[y][2] = PANEL_EDGE
        px[y][13] = frame
    px[13][2] = frame
    px[2][13] = frame
    fill = {"top": PANEL_TOP_IN, "side": PANEL_SIDE_IN, "bottom": PANEL_BOT_IN}[tone]
    for y in range(3, 13):
        for x in range(3, 13):
            put(px, x, y, fill)
    return fill


def panel_fill(px, x0, y0, x1, y1, colour=PANEL):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(px, x, y, colour)


def signal_arcs(px, cx, cy, radii, colour):
    """Nested upside-down arcs (diamond metric) above a point: a broadcast."""
    for r in radii:
        for dx in range(-r, r + 1):
            dy = r - abs(dx)
            put(px, cx + dx, cy - dy, colour)


def indicator(px, x, y, colour):
    """A 1px LED seated in a small dark slot, so it reads as a lamp, not a speck."""
    for i in (-1, 0, 1):
        put(px, x + i, y, rgb("#333333"))
    put(px, x, y, colour)


def vent_slots(px):
    """Three two-pixel slots. Shared by the monitor side and its top face."""
    for y in (5, 7, 9):
        for x in range(4, 12):
            put(px, x, y, rgb("#262626"))
            put(px, x, y + 1, rgb("#3C3C3C"))


# ================================================================ charging station

def terminal_charging_station_texture(powered=True, wireless=True):
    """A battery with a broadcast dome above it, both on the x=7 axis.

    Powered fills the battery bright and gives the dome a lit dot; wireless
    turns the dome cyan. Two small lamps on the frame lip repeat the states."""
    px = casing_frame("side")
    panel(px, "side")
    body = WHITE if powered else rgb("#6E6E6E")
    inner = rgb("#9E9E9E") if powered else rgb("#3A3A3A")
    wifi = CYAN_CORE if wireless else rgb("#5A5A5A")

    # broadcast dome: 3px cap, two shoulders, one dot
    for x in (6, 7, 8):
        put(px, x, 3, wifi)
    put(px, 5, 4, wifi)
    put(px, 9, 4, wifi)
    put(px, 7, 5, wifi)

    # battery: 7x5 body with a 1px terminal nub
    for x in range(4, 11):
        put(px, x, 7, body)
        put(px, x, 11, body)
    for y in range(7, 12):
        put(px, 4, y, body)
        put(px, 10, y, body)
    for y in range(8, 11):
        for x in range(5, 10):
            put(px, x, y, inner)
    put(px, 11, 8, body)
    put(px, 11, 9, body)
    if powered:
        for x in range(5, 10):
            put(px, x, 8, WHITE)

    # power lamp and wireless lamp, stacked on the frame lip
    indicator(px, 12, 3, CYAN_LIT if powered else rgb("#3E3E3E"))
    indicator(px, 12, 11, CYAN_CORE if wireless else rgb("#3E3E3E"))
    return px


# =============================================================== amplifier tower

def amplifier_texture(active):
    """Broadcast arcs over a lattice mast, all on the x=7/8 axis."""
    px = casing_frame("side")
    panel(px, "side")
    arc = CYAN_CORE if active else rgb("#6E6E6E")
    lit = CYAN_LIT if active else rgb("#8A8A8A")
    # one wide broadcast chevron: 2px apex, one step wider per row
    for x in (7, 8):
        put(px, x, 3, lit)
    for x in (6, 9):
        put(px, x, 4, arc)
    for x in (5, 10):
        put(px, x, 5, arc)
    for x in (4, 11):
        put(px, x, 6, arc)
    # mast: two pixels with a lit left edge, held by stepped guy wires
    for y in range(7, 12):
        put(px, 7, y, CAS_HI)
        put(px, 8, y, rgb("#5C5C5C"))
    for i, y in enumerate((9, 10)):
        put(px, 6 - i, y, rgb("#3E3E3E"))
        put(px, 9 + i, y, rgb("#3E3E3E"))
    for x in range(5, 11):
        put(px, x, 12, PART_LO)
    return px


# ==================================================================== bridge

def bridge_texture(linked):
    """Two pylons at the outer edges with a status beam between them."""
    px = casing_frame("side")
    panel(px, "side")
    beam = CYAN_CORE if linked else rgb("#6E6E6E")
    tip = CYAN_LIT if linked else rgb("#8A8A8A")
    for y in range(4, 12):
        put(px, 3, y, CAS_HI)
        put(px, 4, y, rgb("#5C5C5C"))
        put(px, 11, y, rgb("#5C5C5C"))
        put(px, 12, y, CAS_HI)
    for x in range(3, 6):
        put(px, x, 3, PART_LO)
    for x in range(10, 13):
        put(px, x, 3, PART_LO)
    put(px, 3, 4, tip)
    put(px, 12, 4, tip)
    for x in range(5, 11):
        put(px, x, 7, beam)
        put(px, x, 8, beam)
    if linked:
        put(px, 6, 7, CYAN_LIT)
        put(px, 9, 8, CYAN_LIT)
    for x in range(3, 13):
        put(px, x, 12, PART_LO)
    return px


# ============================================================ energy receiver

def energy_receiver_texture(linked):
    """Intake dish as a real trapezoid bowl, signal entering from above."""
    px = casing_frame("side")
    panel(px, "side")
    ray = CYAN_CORE if linked else rgb("#6E6E6E")
    bolt = CYAN_LIT if linked else rgb("#8A8A8A")
    # incoming signal, centred above the bowl
    put(px, 7, 3, ray)
    put(px, 8, 3, ray)
    # rim
    for x in range(4, 12):
        put(px, x, 4, CAS_HI)
    # trapezoid bowl: the walls step inwards row by row
    for i, (x0, x1) in enumerate(((4, 11), (5, 10), (5, 10), (6, 9), (7, 8))):
        y = 5 + i
        put(px, x0, y, CAS_HI)
        put(px, x1, y, rgb("#5C5C5C"))
        for x in range(x0 + 1, x1):
            put(px, x, y, rgb("#242424"))
    # bolt seated in the bowl
    for y in (6, 7):
        for x in (7, 8):
            put(px, x, y, bolt)
    if linked:
        put(px, 7, 5, CYAN_LIT)
    # pedestal and foot
    for y in (10, 11):
        put(px, 7, y, CAS_HI)
        put(px, 8, y, PART_LO)
    for x in range(5, 11):
        put(px, x, 12, PART_LO)
    return px


# ============================================================= energy monitor

def energy_monitor_texture(linked, low):
    """Charge bars on a screen that sits on the same panel grid as the rest."""
    px = casing_frame("side")
    panel(px, "side")
    if not linked:
        scr, bar, led = SCREEN_OFF, None, rgb("#4A4A4A")
    elif low:
        scr, bar, led = rgb("#3A1A1C"), rgb("#F06060"), rgb("#FF8080")
    else:
        scr, bar, led = rgb("#2E2E2E"), CYAN_CORE, CYAN_LIT
    for y in range(4, 12):
        for x in range(4, 12):
            put(px, x, y, scr)
    for x in range(4, 12):
        put(px, x, 4, rgb("#3A3A3A"))
    if bar is None:
        for x in range(5, 11):
            put(px, x, 7, rgb("#3E3E3E"))
            put(px, x, 8, rgb("#3E3E3E"))
    else:
        y_top, y_bot = (11, 11) if low else (6, 11)
        for x0 in (4, 7, 10):
            if low:
                for y in range(6, 11):
                    put(px, x0, y, rgb("#4A2028"))
                    put(px, x0 + 1, y, rgb("#4A2028"))
            for y in range(y_top, y_bot + 1):
                put(px, x0, y, bar)
                put(px, x0 + 1, y, bar)
            if low:
                put(px, x0, 11, rgb("#FFB0B0"))
                put(px, x0 + 1, 11, rgb("#FFB0B0"))
            else:
                put(px, x0, 6, CYAN_LIT)
                put(px, x0 + 1, 6, CYAN_LIT)
        for x in range(4, 12):
            put(px, x, 11, rgb("#A03030") if low else CYAN_DIM)
    put(px, 11, 4, led)
    return px


# ================================================================ smart sorter

def smart_sorter_texture(active):
    """Hopper throat with the filter matrix, vents on the shared rhythm."""
    px = casing_frame("side")
    panel(px, "side")
    dot = CYAN_LIT if active else rgb("#5A5A5A")
    for x in range(5, 11):
        put(px, x, 4, rgb("#3E3E3E"))
        put(px, x, 9, rgb("#3E3E3E"))
    for y in range(4, 10):
        put(px, 5, y, rgb("#3E3E3E"))
        put(px, 10, y, rgb("#3E3E3E"))
    panel_fill(px, 6, 5, 9, 8, BORE)
    for (x, y) in ((6, 5), (9, 5), (6, 8), (9, 8)):
        put(px, x, y, dot)
    for y in (9, 10):
        for x in range(4, 12):
            put(px, x, y, rgb("#2A2A2A") if y == 9 else rgb("#3A3A3A"))
    return px


def smart_sorter_front_texture(active):
    """A hopper funnel dropping into an arrow: items go in the top, out the face."""
    px = casing_frame("side")
    panel(px, "side")
    chute = CYAN_CORE if active else rgb("#6E6E6E")
    tip = CYAN_LIT if active else rgb("#8A8A8A")
    # funnel mouth: steel rim, dark throat
    for x in range(4, 12):
        put(px, x, 3, rgb("#5C5C5C"))
        put(px, x, 4, rgb("#3E3E3E"))
    for x in range(5, 11):
        put(px, x, 4, BORE)
    # stepped walls narrowing to the spout
    for i, (x0, x1) in enumerate(((4, 11), (5, 10), (6, 9))):
        y = 5 + i
        put(px, x0, y, rgb("#6E6E6E"))
        put(px, x1, y, rgb("#4A4A4A"))
        for x in range(x0 + 1, x1):
            put(px, x, y, BORE)
    # spout and arrow head
    for y in (8, 9):
        put(px, 7, y, chute)
        put(px, 8, y, chute)
    for x in (6, 9):
        put(px, x, 10, chute)
    for x in (7, 8):
        put(px, x, 10, tip)
        put(px, x, 11, tip)
    return px


# ============================================================== network bridge


# ============================================================ redstone link

RED_CORE = rgb("#8E2F2F")     # 红石信号：未点亮的深红
RED_LIT = rgb("#FF5A4A")      # 点亮时的红


def redstone_link_texture(on):
    """Front face: a square lamp window with the case's pins running in from top and bottom."""
    px = casing_frame("side")
    panel(px, "side")
    body = RED_LIT if on else RED_CORE
    rim = rgb("#C2403A") if on else rgb("#5C3A3A")
    for y in range(4, 12):
        for x in range(4, 12):
            if x in (4, 11) or y in (4, 11):
                c = rim
            elif on and (x + y) % 4 == 0:
                c = mix(body, rgb("#FFE8DC"), 0.45)
            else:
                c = body
            put(px, x, y, c)
    for y in range(1, 4):
        put(px, 7, y, rim)
        put(px, 8, y, rim)
    for y in range(12, 15):
        put(px, 7, y, rim)
        put(px, 8, y, rim)
    return px


def redstone_link_top(on):
    """Top face: the lamp seen from above, ringed by case screws."""
    px = casing_frame("side")
    panel(px, "side")
    body = RED_LIT if on else RED_CORE
    for y in range(6, 10):
        for x in range(6, 10):
            put(px, x, y, body)
    for x in (2, 13):
        put(px, x, 2, rgb("#5C3A3A"))
        put(px, x, 13, rgb("#5C3A3A"))
    return px

def network_bridge_texture(linked):
    """Two port housings with live sockets, beam between them."""
    px = casing_frame("side")
    panel(px, "side")
    beam = CYAN_CORE if linked else rgb("#6E6E6E")
    led = CYAN_LIT if linked else rgb("#8A8A8A")
    for x0 in (3, 9):
        for y in range(4, 12):
            for x in range(x0, x0 + 4):
                if y == 4:
                    c = CAS_HI
                elif y == 11:
                    c = rgb("#5C5C5C")
                elif x == x0:
                    c = CAS_HI
                elif x == x0 + 3:
                    c = PART_LO
                else:
                    c = CAS_FIELD
                put(px, x, y, c)
        rim = CYAN_DIM if linked else rgb("#3A3A3A")
        for y in range(7, 10):
            put(px, x0 + 1, y, BORE)
            put(px, x0 + 2, y, BORE)
        for x in (x0 + 1, x0 + 2):
            put(px, x, 7, rim)
            put(px, x, 9, rim)
        put(px, x0 + 1, 6, led)
        put(px, x0 + 2, 6, mix(led, CAS_FIELD, 0.4))
    for y in (7, 8):
        put(px, 7, y, beam)
        put(px, 8, y, beam)
    if linked:
        put(px, 7, 7, CYAN_LIT)
    for x in range(3, 13):
        put(px, x, 12, PART_LO)
    return px


# ============================================================ multi-face blocks
#
# Side motifs deliberately repeat the front's structural coordinates, so the
# machine's skeleton wraps around the edge: the amplifier mast, the bridge
# pylons, the receiver pedestal and the network-bridge housings all sit at the
# same x positions on the side face as on the front face.

def casing_side(motif, led=None):
    px = casing_frame("side")
    panel(px, "side")
    motif(px)
    if led is not None:
        indicator(px, 12, 3, led)
    return px


def casing_bottom():
    """Shared base: darkest tone, cable slot, dim corner feet."""
    px = casing_frame("bottom")
    panel(px, "bottom")
    panel_fill(px, 7, 3, 8, 5, SLOT)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        put(px, x, y, rgb("#2E2E2E"))
    return px


# ------------------------------------------------------------ side motifs

def motif_lattice(px):
    """Amplifier side: the same mast as the front, seen edge-on."""
    for x in range(4, 12):
        put(px, x, 12, PART_LO)
    for x in range(5, 11):
        put(px, x, 6, PART_LO)
    for y in range(7, 12):
        put(px, 7, y, CAS_HI)
        put(px, 8, y, rgb("#5C5C5C"))
        put(px, 6, y, rgb("#3E3E3E"))
        put(px, 9, y, rgb("#3E3E3E"))
    put(px, 7, 5, CAS_HI)
    put(px, 8, 5, CAS_HI)


def motif_pylon(px):
    """Bridge side: the two pylons, seen edge-on at the same x positions."""
    for y in range(4, 12):
        put(px, 3, y, CAS_HI)
        put(px, 4, y, rgb("#5C5C5C"))
        put(px, 11, y, rgb("#5C5C5C"))
        put(px, 12, y, CAS_HI)
    for x in range(3, 6):
        put(px, x, 3, PART_LO)
    for x in range(10, 13):
        put(px, x, 3, PART_LO)
    for x in range(3, 13):
        put(px, x, 12, PART_LO)


def motif_pedestal(px):
    """Receiver side: dish rim edge-on above the support column."""
    for x in range(4, 12):
        put(px, x, 5, CAS_HI)
    for y in range(6, 12):
        put(px, 7, y, CAS_HI)
        put(px, 8, y, rgb("#5C5C5C"))
    for x in range(5, 11):
        put(px, x, 12, PART_LO)


def motif_ports(px):
    """Network bridge side: the two conduits, seen edge-on."""
    for y in range(4, 12):
        put(px, 4, y, rgb("#3E3E3E"))
        put(px, 5, y, CAS_HI)
        put(px, 6, y, rgb("#5C5C5C"))
        put(px, 9, y, rgb("#5C5C5C"))
        put(px, 10, y, rgb("#3E3E3E"))
    for y in (4, 11):
        for x in range(4, 11):
            put(px, x, y, PART_LO)
    for x in (6, 9):
        put(px, x, 4, CAS_HI)
    for x in range(3, 13):
        put(px, x, 12, PART_LO)


def motif_vents(px):
    """Monitor side: the same slot rhythm the top face uses."""
    vent_slots(px)


def motif_cable(px):
    """Charging station side: cable conduit on the x=7/8 axis."""
    for y in range(3, 13):
        put(px, 7, y, CAS_HI if y % 2 else rgb("#5C5C5C"))
        put(px, 8, y, rgb("#5C5C5C") if y % 2 else CAS_HI)
    for y in (5, 9):
        put(px, 6, y, rgb("#3A3A3A"))
        put(px, 9, y, rgb("#3A3A3A"))


# ------------------------------------------------------------ top designs
#
# Every top follows the same plan: shared frame + shared panel + one centred
# functional part, so the six tops read as one family.

def amplifier_top(active):
    px = casing_frame("top")
    panel(px, "top")
    for x in range(6, 10):
        put(px, x, 6, PART_LO)
        put(px, x, 9, PART_LO)
    for y in range(6, 10):
        put(px, 6, y, PART_LO)
        put(px, 9, y, PART_LO)
    panel_fill(px, 7, 7, 8, 8, BORE)
    indicator(px, 11, 4, CYAN_LIT if active else rgb("#4A4A4A"))
    return px


def bridge_top(linked):
    px = casing_frame("top")
    panel(px, "top")
    beam = CYAN_CORE if linked else rgb("#4A4A4A")
    for x0 in (3, 11):
        for y in range(6, 9):
            for x in range(x0, x0 + 2):
                put(px, x, y, PART_LO)
        put(px, x0, 6, CAS_HI)
    for x in range(5, 11):
        put(px, x, 7, beam)
        put(px, x, 8, beam)
    if linked:
        put(px, 7, 7, CYAN_LIT)
    return px


def receiver_top(linked):
    px = casing_frame("top")
    panel(px, "top")
    for x in range(5, 11):
        put(px, x, 5, CAS_HI)
        put(px, x, 10, CAS_HI)
    for y in range(5, 11):
        put(px, 5, y, CAS_HI)
        put(px, 10, y, CAS_HI)
    panel_fill(px, 6, 6, 9, 9, BORE)
    bolt = CYAN_LIT if linked else rgb("#8A8A8A")
    for y in (7, 8):
        for x in (7, 8):
            put(px, x, y, bolt)
    if linked:
        for i in range(2):
            put(px, 3 + i, 3 + i, CYAN_CORE)
            put(px, 12 - i, 3 + i, CYAN_CORE)
            put(px, 3 + i, 12 - i, CYAN_CORE)
            put(px, 12 - i, 12 - i, CYAN_CORE)
    return px


def netbr_top(linked):
    px = casing_frame("top")
    panel(px, "top")
    for x0 in (4, 9):
        for y in range(5, 10):
            for x in range(x0, x0 + 3):
                edge = y in (5, 9) or x in (x0, x0 + 2)
                put(px, x, y, PART_LO if edge else CAS_FIELD)
        for y in (7, 8):
            put(px, x0 + 1, y, BORE)
    beam = CYAN_CORE if linked else rgb("#3A3A3A")
    for y in (7, 8):
        put(px, 7, y, beam)
        put(px, 8, y, beam)
    if linked:
        put(px, 7, 7, CYAN_LIT)
    return px


def emon_top(linked, low):
    px = casing_frame("top")
    panel(px, "top")
    vent_slots(px)
    led = rgb("#4A4A4A") if not linked else (rgb("#FF8080") if low else CYAN_LIT)
    indicator(px, 11, 4, led)
    return px


def station_top(wireless):
    px = casing_frame("top")
    panel(px, "top")
    col = CYAN_CORE if wireless else rgb("#5A5A5A")
    glow = CYAN_LIT if wireless else rgb("#7A7A7A")
    for x in range(5, 11):
        put(px, x, 6, col)
        put(px, x, 9, col)
    for y in range(6, 10):
        put(px, 5, y, col)
        put(px, 10, y, col)
    for x in (7, 8):
        put(px, x, 6, rgb("#6E6E6E") if wireless else rgb("#5A5A5A"))
    panel_fill(px, 7, 7, 8, 8, glow)
    indicator(px, 11, 4, CYAN_CORE if wireless else rgb("#4A4A4A"))
    return px


def sorter_top(active):
    px = casing_frame("top")
    panel(px, "top")
    panel_fill(px, 6, 5, 9, 8, BORE)
    dot = CYAN_LIT if active else rgb("#5A5A5A")
    for (x, y) in ((6, 5), (9, 5), (6, 8), (9, 8)):
        put(px, x, y, dot)
    for x in range(5, 11):
        put(px, x, 4, rgb("#3C3C3C"))
        put(px, x, 10, rgb("#3C3C3C"))
    return px


def sorter_bottom():
    px = casing_bottom()
    panel_fill(px, 6, 6, 9, 8, SLOT)
    for x in (7, 8):
        put(px, x, 5, rgb("#3C3C3C"))
    return px



# ====================================================================== driver

def main():
    targets = [
        ("wireless_crafting_grid/inactive.png", wireless_crafting_grid_texture(False)),
        ("wireless_crafting_grid/active.png", wireless_crafting_grid_texture(True)),
        ("wireless_pattern_grid/inactive.png", wireless_pattern_grid_texture(False)),
        ("wireless_pattern_grid/active.png", wireless_pattern_grid_texture(True)),
        ("wireless_configuration_card.png", configuration_card_texture()),
        ("wireless_monitor/inactive.png", wireless_monitor_texture(False)),
        ("wireless_monitor/active.png", wireless_monitor_texture(True)),
        ("wireless_autocrafter_manager/inactive.png", wireless_autocrafter_manager_texture(False)),
        ("wireless_autocrafter_manager/active.png", wireless_autocrafter_manager_texture(True)),
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

    # ------------------------------------------------------ multi-face sets
    LED_OFF = rgb("#4A4A4A")
    face_sets = []

    for active in (False, True):
        s = "_active" if active else ""
        led = CYAN_LIT if active else LED_OFF
        face_sets += [
            (f"wireless_amplifier{s}_side.png", casing_side(motif_lattice, led)),
            (f"wireless_amplifier{s}_top.png", amplifier_top(active)),
        ]
    face_sets.append(("wireless_amplifier_bottom.png", casing_bottom()))

    for linked in (False, True):
        s = "_linked" if linked else ""
        led = CYAN_LIT if linked else LED_OFF
        face_sets += [
            (f"wireless_bridge{s}_side.png", casing_side(motif_pylon, led)),
            (f"wireless_bridge{s}_top.png", bridge_top(linked)),
        ]
    face_sets.append(("wireless_bridge_bottom.png", casing_bottom()))

    for linked in (False, True):
        s = "_linked" if linked else ""
        led = CYAN_LIT if linked else LED_OFF
        face_sets += [
            (f"wireless_energy_receiver{s}_side.png", casing_side(motif_pedestal, led)),
            (f"wireless_energy_receiver{s}_top.png", receiver_top(linked)),
        ]
    face_sets.append(("wireless_energy_receiver_bottom.png", casing_bottom()))

    for linked in (False, True):
        s = "_linked" if linked else ""
        led = CYAN_LIT if linked else LED_OFF
        face_sets += [
            (f"network_bridge{s}_side.png", casing_side(motif_ports, led)),
            (f"network_bridge{s}_top.png", netbr_top(linked)),
        ]
    face_sets.append(("network_bridge_bottom.png", casing_bottom()))

    for on in (False, True):
        s = "_on" if on else ""
        face_sets += [
            (f"redstone_link{s}_side.png", casing_side(motif_lattice, RED_LIT if on else LED_OFF)),
            (f"redstone_link{s}_top.png", redstone_link_top(on)),
        ]
    face_sets += [
        ("redstone_link.png", redstone_link_texture(False)),
        ("redstone_link_on.png", redstone_link_texture(True)),
    ]
    face_sets.append(("redstone_link_bottom.png", casing_bottom()))

    for linked, low in ((False, False), (True, False), (True, True)):
        s = "_low" if low else ("_linked" if linked else "")
        led = LED_OFF if not linked else (rgb("#FF8080") if low else CYAN_LIT)
        face_sets += [
            (f"energy_monitor{s}_side.png", casing_side(motif_vents, led)),
            (f"energy_monitor{s}_top.png", emon_top(linked, low)),
        ]
    face_sets.append(("energy_monitor_bottom.png", casing_bottom()))

    for powered, wireless in ((True, True), (True, False), (False, True), (False, False)):
        s = ("_unpowered" if not powered else "") + ("" if wireless else "_wireless_off")
        led = CYAN_LIT if powered else LED_OFF
        face_sets += [
            (f"terminal_charging_station{s}_side.png", casing_side(motif_cable, led)),
            (f"terminal_charging_station{s}_top.png", station_top(wireless)),
        ]
    face_sets.append(("terminal_charging_station_bottom.png", casing_bottom()))

    for active in (False, True):
        s = "_active" if active else ""
        face_sets.append((f"smart_sorter{s}_top.png", sorter_top(active)))
    face_sets.append(("smart_sorter_bottom.png", sorter_bottom()))

    for name, pixels in face_sets:
        path = os.path.join(block_out, name)
        write_png(path, pixels)
        print("wrote", path)


if __name__ == "__main__":
    main()
