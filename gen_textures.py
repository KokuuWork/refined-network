"""Generate placeholder 16x16 item textures for rswirelesscrafting (pure stdlib PNG writer)."""
import struct
import zlib
import os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                   "rswirelesscrafting-common", "src", "main", "resources",
                   "assets", "rswirelesscrafting", "textures", "item")


def write_png(path, pixels):
    """pixels: list of rows, each row a list of (r,g,b,a) tuples."""
    height = len(pixels)
    width = len(pixels[0])
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter type: none
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        crc = zlib.crc32(tag + data) & 0xFFFFFFFF
        return c + struct.pack(">I", crc)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


def canvas():
    return [[(0, 0, 0, 0) for _ in range(16)] for _ in range(16)]


def fill(px, color):
    for y in range(16):
        for x in range(16):
            px[y][x] = color


def border(px, color, thickness=1):
    for i in range(16):
        for t in range(thickness):
            px[t][i] = color
            px[15 - t][i] = color
            px[i][t] = color
            px[i][15 - t] = color


def rect(px, x0, y0, x1, y1, color):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[y][x] = color


def grid_icon(base, line, glow):
    px = canvas()
    fill(px, base)
    border(px, tuple(int(c * 0.6) for c in base[:3]) + (255,))
    # crafting matrix frame
    rect(px, 2, 2, 13, 13, line)
    rect(px, 3, 3, 12, 12, base)
    # 2x2 matrix slots
    for (sx, sy) in ((4, 4), (9, 4), (4, 9), (9, 9)):
        rect(px, sx, sy, sx + 2, sy + 2, glow)
    return px


def range_icon():
    px = canvas()
    fill(px, (46, 92, 46, 255))          # deep green base
    border(px, (28, 58, 28, 255))
    # concentric range rings
    for (r, c) in ((6, (120, 220, 120, 255)), (3, (200, 255, 200, 255))):
        rect(px, 8 - r, 8 - r, 8 + r, 8 + r, (0, 0, 0, 0))
        for i in range(-r, r + 1):
            px[8 - r][8 + i] = c
            px[8 + r][8 + i] = c
            px[8 + i][8 - r] = c
            px[8 + i][8 + r] = c
    px[8][8] = (255, 255, 255, 255)      # transmitter dot
    return px


def dim_icon():
    px = canvas()
    fill(px, (74, 46, 110, 255))         # ender purple base
    border(px, (44, 26, 68, 255))
    # portal frame
    rect(px, 3, 2, 12, 13, (30, 18, 46, 255))
    rect(px, 4, 3, 11, 12, (126, 87, 194, 255))
    # swirl
    for (x, y) in ((6, 6), (9, 6), (6, 9), (9, 9)):
        px[y][x] = (220, 190, 255, 255)
    px[7][7] = (255, 255, 255, 255)
    px[8][8] = (255, 255, 255, 255)
    return px


def main():
    os.makedirs(os.path.join(OUT, "wireless_crafting_grid"), exist_ok=True)
    write_png(os.path.join(OUT, "wireless_crafting_grid", "active.png"),
              grid_icon((96, 84, 140, 255), (58, 48, 88, 255), (176, 156, 224, 255)))
    write_png(os.path.join(OUT, "wireless_crafting_grid", "inactive.png"),
              grid_icon((92, 92, 96, 255), (52, 52, 56, 255), (140, 140, 148, 255)))
    write_png(os.path.join(OUT, "range_upgrade.png"), range_icon())
    write_png(os.path.join(OUT, "interdimensional_upgrade.png"), dim_icon())
    print("generated 4 textures in", OUT)


if __name__ == "__main__":
    main()
