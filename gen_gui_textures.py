"""Generate the GUI background textures for rswirelesscrafting.

Pure standard library, same PNG writer approach as gen_textures.py.
Re-run with:  python gen_gui_textures.py
"""
import os
import struct
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(
    HERE, "rswirelesscrafting-common", "src", "main", "resources",
    "assets", "rswirelesscrafting", "textures", "gui"
)

WIDTH = 176
HEIGHT = 166

BG = (198, 198, 198)
BG_LIGHT = (255, 255, 255)
BG_SHADE = (85, 85, 85)
SLOT_BG = (139, 139, 139)
SLOT_DARK = (55, 55, 55)


def write_png(path, pixels):
    height = len(pixels)
    width = len(pixels[0])
    raw = b""
    for row in pixels:
        raw += b"\x00" + b"".join(struct.pack("BBB", *pixel[:3]) for pixel in row)
    compressed = zlib.compress(raw, 9)

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", compressed) + chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(png)


def canvas(width=WIDTH, height=HEIGHT):
    return [[BG for _ in range(width)] for _ in range(height)]


def frame(pixels):
    for x in range(WIDTH):
        pixels[0][x] = BG_LIGHT
        pixels[1][x] = BG_LIGHT
        pixels[HEIGHT - 2][x] = BG_SHADE
        pixels[HEIGHT - 1][x] = BG_SHADE
    for y in range(HEIGHT):
        pixels[y][0] = BG_LIGHT
        pixels[y][1] = BG_LIGHT
        pixels[y][WIDTH - 2] = BG_SHADE
        pixels[y][WIDTH - 1] = BG_SHADE


def slot(pixels, x, y, size=18):
    """Standard vanilla-style inset slot with a 1px bevel."""
    for row in range(size):
        for column in range(size):
            pixels[y + row][x + column] = SLOT_BG
    for column in range(size):
        pixels[y][x + column] = SLOT_DARK
        pixels[y + size - 1][x + column] = BG_LIGHT
    for row in range(size):
        pixels[y + row][x] = SLOT_DARK
        pixels[y + row][x + size - 1] = BG_LIGHT


def panel(pixels, x, y, width, height):
    """Recessed bar used for the two energy readouts."""
    for row in range(height):
        for column in range(width):
            pixels[y + row][x + column] = SLOT_DARK


def upgrades_gui():
    """4 upgrade slots in a row + player inventory."""
    pixels = canvas()
    frame(pixels)
    for index in range(4):
        slot(pixels, 52 + index * 18, 30)
    # inventory label separator line
    for x in range(7, WIDTH - 7):
        pixels[82][x] = BG_SHADE
    for row in range(3):
        for column in range(9):
            slot(pixels, 7 + column * 18, 83 + row * 18)
    for column in range(9):
        slot(pixels, 7 + column * 18, 141)
    return pixels


def terminal_charging_station_gui():
    """3 wired slots + 3 wireless link lights + network energy bar + inventory.

    Slot frames are drawn at (menu coordinate - 1), the vanilla convention: a
    menu slot at (x, y) renders a 16x16 icon at x..x+15, so its 18x18 recess
    has to sit at x-1..x+16. The wireless "slots" are indicator-only, so they
    reuse the same recessed square purely for the visuals.

    Menu coordinates (must match ChargingStationContainerMenu / Screen):
        wired slots   y=17, x=44/80/116
        wireless      y=40, x=44/80/116
        network bar   (26, 61) 124x10
    """
    accent = (60, 170, 210)
    pixels = canvas()
    frame(pixels)
    for x in (44, 80, 116):
        slot(pixels, x - 1, 17 - 1)
        slot(pixels, x - 1, 40 - 1)
    panel(pixels, 26, 61, 124, 10)
    for x in range(25, 151):
        pixels[60][x] = accent
        pixels[70][x] = accent
    for y in range(60, 71):
        pixels[y][25] = accent
        pixels[y][150] = accent
    for x in range(7, WIDTH - 7):
        pixels[82][x] = BG_SHADE
    for row in range(3):
        for column in range(9):
            slot(pixels, 7 + column * 18, 83 + row * 18)
    for column in range(9):
        slot(pixels, 7 + column * 18, 141)
    return pixels


def main():
    targets = [
        ("wireless_crafting_grid_upgrades.png", upgrades_gui()),
        ("terminal_charging_station.png", terminal_charging_station_gui()),
    ]
    for name, pixels in targets:
        path = os.path.join(OUT, name)
        write_png(path, pixels)
        print("wrote", path)


if __name__ == "__main__":
    main()
