#!/usr/bin/env python3
"""合成 MC百科配图（不依赖游戏内截图，全部用模组纹理拼）。

运行前需要与 gen_readme_images.py 相同的环境变量：
  MC_CLIENT_JAR  the 1.21.1 client jar
  RS_TEXTURES    .../refinedstorage-common/src/main/resources/assets/refinedstorage/textures
"""

import os

import gen_readme_images as G

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "docs", "images")
RS = os.environ.get("RS_TEXTURES") or ""

BG = (24, 28, 34, 255)
PANEL = (31, 36, 43, 255)
EDGE = (60, 68, 78, 255)
CYAN = (86, 190, 214, 255)
DIM = (96, 106, 118, 255)
SOFT = (44, 51, 60, 255)
WHITE = (245, 247, 249, 255)
INK = (32, 38, 46, 255)
GRID = (214, 219, 226, 255)


def panel(px, x0, y0, x1, y1, fill=PANEL, border=EDGE):
    G.rect(px, x0, y0, x1, y1, fill)
    G.frame(px, x0, y0, x1, y1, border)


def dot(px, x, y, colour, r=1):
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if 0 <= y + dy < len(px) and 0 <= x + dx < len(px[0]):
                px[y + dy][x + dx] = colour


def dashed(px, x0, y0, x1, y1, colour, dash=10, gap=8, width=2):
    dx = x1 - x0
    dy = y1 - y0
    steps = int(max(abs(dx), abs(dy)))
    if steps == 0:
        return
    for i in range(steps + 1):
        if (i // dash) % 2:
            continue
        t = i / steps
        x = int(x0 + dx * t)
        y = int(y0 + dy * t)
        for w in range(width):
            if 0 <= y + w < len(px) and 0 <= x < len(px[0]):
                px[y + w][x] = colour


def ring(px, cx, cy, radius, colour):
    steps = max(24, radius * 6)
    for i in range(steps):
        a = i * 6.283185 / steps
        x = int(cx + radius * __import__("math").cos(a))
        y = int(cy + radius * __import__("math").sin(a))
        if 0 <= y < len(px) and 0 <= x < len(px[0]):
            px[y][x] = colour


def rs_cube(name, size=192):
    """用 RS 的机壳 + 面纹理拼一个等轴方块图标。"""
    base = G.read_png(os.path.join(RS, "block", "machine_casing", "end.png"))
    side = G.read_png(os.path.join(RS, "block", "machine_casing", "side.png"))
    if name == "controller":
        front = G.read_png(os.path.join(RS, "block", "controller", "on.png"))
    elif name == "transmitter":
        front = G.overlay(base, G.read_png(os.path.join(
            RS, "block", "wireless_transmitter", "cutouts", "cyan.png")))
    elif name == "crafting_grid":
        front = G.overlay(base, G.read_png(os.path.join(
            RS, "block", "crafting_grid", "front.png")))
    else:
        raise KeyError(name)
    return G.iso_sprite(front, side, base, size)


DIGITS = {
    "0": ["111", "101", "101", "101", "111"],
    "1": ["010", "110", "010", "010", "111"],
    "2": ["111", "001", "111", "100", "111"],
    "3": ["111", "001", "111", "001", "111"],
    "4": ["101", "101", "111", "001", "001"],
    "5": ["111", "100", "111", "001", "111"],
    "6": ["111", "100", "111", "101", "111"],
    "7": ["111", "001", "001", "001", "001"],
    "8": ["111", "101", "111", "101", "111"],
    "9": ["111", "101", "111", "001", "111"],
    "x": ["000", "101", "010", "101", "000"],
}


def text(px, x, y, value, colour, scale=2):
    """3x5 数字字体，够画坐标标注。"""
    cursor = x
    for ch in value:
        glyph = DIGITS.get(ch)
        if glyph is None:
            cursor += 4 * scale
            continue
        for gy, row in enumerate(glyph):
            for gx, bit in enumerate(row):
                if bit != "1":
                    continue
                for dy in range(scale):
                    for dx in range(scale):
                        px[y + gy * scale + dy][cursor + gx * scale + dx] = colour
        cursor += 4 * scale
    return cursor


def sprite_at(assets, key, px, x, y, size):
    G.blit_fit(px, G.sprite(assets, key, size), x, y, size)


def arrow(px, x, y, length, colour):
    G.draw_arrow(px, x, y, length, colour)


# ------------------------------------------------------------------ figures

def feature_hero(assets, path):
    """特点节头图：终端 —— 无线访问点 —— 网络（控制器）。"""
    w, h = 1200, 420
    px = G.canvas(w, h, BG)
    panel(px, 0, 0, w - 1, h - 1, PANEL)

    cell = 200
    y = 110
    xs = [56, 500, 944]

    G.slot(px, xs[0], y, cell, 11)
    sprite_at(assets, "grid_on", px, xs[0] + 4, y + 4, 192)

    G.slot(px, xs[1], y, cell, 11)
    G.blit_fit(px, rs_cube("transmitter", 192), xs[1] + 4, y + 4, 192)

    G.slot(px, xs[2], y, cell, 11)
    G.blit_fit(px, rs_cube("controller", 192), xs[2] + 4, y + 4, 192)

    y_mid = y + cell // 2
    dashed(px, xs[0] + cell + 18, y_mid, xs[1] - 18, y_mid, CYAN, dash=12, gap=9, width=3)
    dashed(px, xs[1] + cell + 18, y_mid, xs[2] - 18, y_mid, CYAN, dash=12, gap=9, width=3)
    for i in range(4):
        dot(px, xs[0] + cell + 34 + i * 40, y_mid, CYAN, 2)
        dot(px, xs[1] + cell + 34 + i * 40, y_mid, CYAN, 2)
    G.write_png(path, px)
    print("wrote", path)


def terminal_kinds(assets, path):
    """四件终端并排。"""
    w, h = 1200, 300
    px = G.canvas(w, h, BG)
    panel(px, 0, 0, w - 1, h - 1, PANEL)
    keys = ["grid_on", "pattern_on", "manager_on", "monitor_on"]
    cell = 200
    gap = 40
    total = len(keys) * cell + (len(keys) - 1) * gap
    x = (w - total) // 2
    for key in keys:
        y = (h - cell) // 2
        G.slot(px, x, y, cell, 11)
        sprite_at(assets, key, px, x + 4, y + 4, 192)
        x += cell + gap
    G.write_png(path, px)
    print("wrote", path)


def connector_flow(assets, path):
    """绑定流程：网络方块 → 连接器 → 设备。"""
    w, h = 1100, 300
    px = G.canvas(w, h, BG)
    panel(px, 0, 0, w - 1, h - 1, PANEL)
    cell = 192
    gap = 120
    xs = [(w - (cell * 3 + gap * 2)) // 2 + i * (cell + gap) for i in range(3)]
    y = (h - cell) // 2
    G.slot(px, xs[0], y, cell, 10)
    G.blit_fit(px, rs_cube("controller", 184), xs[0] + 4, y + 4, 184)
    G.slot(px, xs[1], y, cell, 10)
    sprite_at(assets, "connector", px, xs[1] + 4, y + 4, 184)
    G.slot(px, xs[2], y, cell, 10)
    sprite_at(assets, "charging", px, xs[2] + 4, y + 4, 184)
    mid_y = y + cell // 2
    for i in range(2):
        start = xs[i] + cell + 16
        arrow(px, start, mid_y, gap - 32, CYAN)
    G.write_png(path, px)
    print("wrote", path)


def energy_curve(assets, path):
    """距离倍率柱状图：16 / 32 / 64 / 128 / 256 格。"""
    del assets
    w, h = 900, 440
    px = G.canvas(w, h, WHITE)
    panel(px, 0, 0, w - 1, h - 1, WHITE, GRID)

    left, right, top, bottom = 90, w - 50, 50, h - 70
    values = [1, 2, 4, 8, 16]
    peak = 16
    # 水平网格线
    for i in range(5):
        y = bottom - int((bottom - top) * i / 4)
        G.rect(px, left, y, right, y, GRID)
    G.rect(px, left, top, left + 2, bottom, INK)
    G.rect(px, left, bottom - 2, right, bottom, INK)

    bar_w = 90
    spacing = (right - left - 30) // 5
    labels = ["16", "32", "64", "128", "256"]
    for i, v in enumerate(values):
        bh = int((bottom - top - 10) * v / peak)
        x0 = left + spacing * i + (spacing - bar_w) // 2
        y0 = bottom - bh
        G.rect(px, x0, y0, x0 + bar_w, bottom - 2, CYAN)
        G.rect(px, x0, y0, x0 + bar_w, y0 + 3, (140, 220, 235, 255))
        mult = ("%dx" % v)
        text(px, x0 + bar_w // 2 - len(mult) * 6, y0 - 32, mult, INK, 3)
        text(px, x0 + bar_w // 2 - len(labels[i]) * 6, bottom + 14, labels[i], INK, 3)
    G.write_png(path, px)
    print("wrote", path)


def devices_row(assets, path):
    """七件方块设备一行。"""
    w, h = 1240, 300
    px = G.canvas(w, h, BG)
    panel(px, 0, 0, w - 1, h - 1, PANEL)
    keys = ["charging", "amplifier", "bridge_on", "receiver", "emon", "sorter", "netbridge"]
    cell = 148
    gap = 16
    total = len(keys) * cell + (len(keys) - 1) * gap
    x = (w - total) // 2
    for key in keys:
        y = (h - cell) // 2
        G.slot(px, x, y, cell, 8)
        sprite_at(assets, key, px, x + 4, y + 4, 140)
        x += cell + gap
    G.write_png(path, px)
    print("wrote", path)


def bridge_link(assets, path):
    """无线桥：两端配对 + 跨维连接。"""
    w, h = 1000, 360
    px = G.canvas(w, h, BG)
    panel(px, 0, 0, w - 1, h - 1, PANEL)
    cell = 176
    y = (h - cell) // 2
    left_x, right_x = 90, w - 90 - cell
    G.slot(px, left_x, y, cell, 9)
    sprite_at(assets, "bridge_off", px, left_x + 4, y + 4, 168)
    G.slot(px, right_x, y, cell, 9)
    sprite_at(assets, "bridge_on", px, right_x + 4, y + 4, 168)

    y_mid = y + cell // 2
    dashed(px, left_x + cell + 24, y_mid, right_x - 24, y_mid, CYAN, dash=14, gap=10, width=3)
    for i in range(5):
        dot(px, left_x + cell + 60 + i * 90, y_mid, CYAN, 2)
    ring(px, w // 2, y_mid, 40, CYAN)
    G.write_png(path, px)
    print("wrote", path)


def monitor_display(assets, path):
    """能量监控仪：方块 + 上方悬浮三行数字。"""
    w, h = 760, 470
    px = G.canvas(w, h, BG)
    panel(px, 0, 0, w - 1, h - 1, PANEL)

    # 悬浮屏
    sx0, sy0, sx1, sy1 = 130, 44, w - 130, 214
    G.rect(px, sx0, sy0, sx1, sy1, (14, 18, 23, 255))
    G.frame(px, sx0, sy0, sx1, sy1, CYAN)
    bars = [(36, 250), (36, 170), (36, 120)]
    by = sy0 + 24
    for x_off, length in bars:
        G.rect(px, sx0 + x_off, by, sx0 + x_off + length, by + 20, (150, 214, 228, 255))
        by += 42

    # 方块本体
    cell = 200
    bx = (w - cell) // 2
    byy = 250
    G.slot(px, bx, byy, cell, 10)
    sprite_at(assets, "emon", px, bx + 4, byy + 4, 192)

    # 连接线
    dashed(px, w // 2, sy1 + 4, w // 2, byy - 4, CYAN, dash=6, gap=6, width=2)
    G.write_png(path, px)
    print("wrote", path)


def main():
    assets = G.load_assets()
    os.makedirs(OUT, exist_ok=True)
    feature_hero(assets, os.path.join(OUT, "mcmod-hero.png"))
    terminal_kinds(assets, os.path.join(OUT, "mcmod-terminals.png"))
    connector_flow(assets, os.path.join(OUT, "mcmod-connector-flow.png"))
    energy_curve(assets, os.path.join(OUT, "mcmod-energy-curve.png"))
    devices_row(assets, os.path.join(OUT, "mcmod-devices.png"))
    bridge_link(assets, os.path.join(OUT, "mcmod-bridge.png"))
    monitor_display(assets, os.path.join(OUT, "mcmod-monitor.png"))


if __name__ == "__main__":
    main()
