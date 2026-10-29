#!/usr/bin/env python3
"""生成 MC百科配图（浅色卡片风格，带中文标注）。

图标来源：模组自身纹理；RS 方块用机壳 + 面纹理拼等轴立方体。
文字用微软雅黑，2 倍超采样后缩回，保证边缘干净。

运行前需要：
  MC_CLIENT_JAR  1.21.1 客户端 jar
  RS_TEXTURES    refinedstorage 的 textures 目录
"""

import os

from PIL import Image, ImageDraw, ImageFont

import gen_readme_images as G

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "docs", "images")
RS = os.environ.get("RS_TEXTURES") or ""

FONT_REG = "C:/Windows/Fonts/msyh.ttc"
FONT_BOLD = "C:/Windows/Fonts/msyhbd.ttc"

S = 2  # 超采样倍数

BG = (247, 248, 250)
CARD = (255, 255, 255)
BORDER = (228, 232, 238)
TILE = (244, 246, 249)
TILE_BORDER = (230, 234, 240)
INK = (31, 35, 40)
SUB = (98, 106, 118)
MUTED = (140, 148, 160)
ACCENT = (44, 143, 168)
SHADOW = (17, 24, 39, 12)


def sc(v):
    return int(round(v * S))


def font(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REG, sc(size))


def text(draw, xy, value, size, colour, bold=False, anchor="la"):
    draw.text((sc(xy[0]), sc(xy[1])), value, font=font(size, bold), fill=colour, anchor=anchor)


def card(img, box, radius=16, fill=CARD):
    x0, y0, x1, y1 = [sc(v) for v in box]
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([x0, y0 + sc(3), x1, y1 + sc(3)], radius=sc(radius), fill=SHADOW)
    d.rounded_rectangle([x0, y0, x1, y1], radius=sc(radius), fill=fill, outline=BORDER, width=sc(1))


def tile(img, x, y, size, icon_img, name, desc=None, name_size=19, desc_size=14):
    """一个浅色方块 + 图标 + 下方标题（可选一行小字）。"""
    d = ImageDraw.Draw(img)
    d.rounded_rectangle(
        [sc(x), sc(y), sc(x + size), sc(y + size)], radius=sc(14),
        fill=TILE, outline=TILE_BORDER, width=sc(1),
    )
    pad = size * 0.14
    inner = int(round((size - 2 * pad) * S))
    im = icon_img.resize((inner, inner), Image.NEAREST)
    img.alpha_composite(im, (sc(x + pad), sc(y + pad)))

    cy = y + size + 12
    if name:
        text(d, (x + size / 2, cy), name, name_size, INK, bold=True, anchor="ma")
    if desc:
        cy += name_size + 8
        text(d, (x + size / 2, cy), desc, desc_size, SUB, anchor="ma")
    return cy + desc_size


def grid_to_img(rows):
    h = len(rows)
    w = len(rows[0])
    im = Image.new("RGBA", (w, h))
    im.putdata([tuple(c) for row in rows for c in row])
    return im


def png(path):
    return Image.open(path).convert("RGBA")


def rs_cube(name, size):
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
    return grid_to_img(G.iso_sprite(front, side, base, int(size * S)))


def new_canvas(w, h):
    return Image.new("RGBA", (sc(w), sc(h)), BG + (255,))


def save(img, path):
    img.convert("RGB").resize((img.width // S, img.height // S), Image.LANCZOS).save(path)
    print("wrote", path)


def header(draw, title, sub, x=56, y=38):
    text(draw, (x, y), title, 30, INK, bold=True)
    text(draw, (x, y + 50), sub, 16, SUB)


def arrow(draw, x0, y0, x1, colour=ACCENT):
    """细虚线 + 右向箭头。"""
    dash, gap = 9, 7
    x = x0
    while x < x1 - 12:
        draw.line([sc(x), sc(y0), sc(min(x + dash, x1 - 12)), sc(y0)], fill=colour, width=sc(2))
        x += dash + gap
    draw.polygon(
        [(sc(x1), sc(y0)), (sc(x1 - 9), sc(y0 - 6)), (sc(x1 - 9), sc(y0 + 6))],
        fill=colour,
    )


def icon_path(key):
    return os.path.join(ROOT, "docs", "mcmod", "icons", key + ".png")


# ------------------------------------------------------------------ 图 1

def hero(path):
    w, h = 1200, 520
    img = new_canvas(w, h)
    card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)

    header(d, "无线合成终端", "绑定一次，之后在任何地方打开网络的合成界面")

    size = 168
    y = 172
    xs = [96, 486, 876]
    tile(img, xs[0], y, size, png(icon_path("wireless_crafting_grid")), "无线合成终端", "手持使用")
    tile(img, xs[1], y, size, rs_cube("transmitter", size * 0.72), "无线访问点", "网络的入口")
    tile(img, xs[2], y, size, rs_cube("controller", size * 0.72), "控制器", "网络本体")

    mid = y + size / 2
    arrow(d, xs[0] + size + 22, mid, xs[1] - 22)
    arrow(d, xs[1] + size + 22, mid, xs[2] - 22)

    text(d, (56, h - 72), "终端按次计费：距离越远，单次操作耗电越高；跨维度固定 16 倍。", 15, MUTED)
    save(img, path)


# ------------------------------------------------------------------ 图 2

def terminals(path):
    w, h = 1220, 470
    img = new_canvas(w, h)
    card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    header(d, "四件终端", "都能远程打开，插件槽与绑定方式完全相同")

    items = [
        ("wireless_crafting_grid", "无线合成终端", "合成与自动合成"),
        ("wireless_pattern_grid", "无线样板终端", "编写自动化样板"),
        ("wireless_autocrafter_manager", "自动合成仓管理器", "管理全网样板"),
        ("wireless_monitor", "无线监控终端", "阈值提醒与补货"),
    ]
    size = 170
    gap = 26
    total = len(items) * size + (len(items) - 1) * gap
    x = (w - total) / 2
    for key, name, desc in items:
        tile(img, x, 168, size, png(icon_path(key)), name, desc)
        x += size + gap
    save(img, path)


# ------------------------------------------------------------------ 图 3

def connector_flow(path):
    w, h = 1160, 500
    img = new_canvas(w, h)
    card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    header(d, "绑定方块设备", "方块设备不能手持右键绑定，要先用无线连接器记录接入点")

    size = 160
    y = 164
    xs = [72, 438, 806]
    steps = [
        (rs_cube("controller", size * 0.72), "① 右键网络方块", "记录接入点"),
        (png(icon_path("wireless_connector")), "② 连接器右键设备", "完成绑定"),
        (png(icon_path("terminal_charging_station")), "③ 两端亮起光环", "同维度有粒子连线"),
    ]
    for (icon, name, desc), x in zip(steps, xs):
        tile(img, x, y, size, icon, name, desc, name_size=17, desc_size=14)

    mid = y + size / 2
    arrow(d, xs[0] + size + 18, mid, xs[1] - 18)
    arrow(d, xs[1] + size + 18, mid, xs[2] - 18)

    text(d, (56, h - 62), "接入点记录会留在连接器里，可以连续绑定多台设备；再次右键已绑定的设备即解绑。",
         15, MUTED)
    save(img, path)


# ------------------------------------------------------------------ 图 4

def devices(path):
    w, h = 1280, 430
    img = new_canvas(w, h)
    card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    header(d, "七件方块设备", "全部接入同一个精致存储网络，用线缆或无线桥连过去即可")

    items = [
        ("terminal_charging_station", "终端充电站", "给终端补电"),
        ("wireless_amplifier", "无线增幅塔", "范围内耗电减半"),
        ("wireless_bridge", "无线桥", "把网络延伸出去"),
        ("wireless_energy_receiver", "能量接收器", "跨网络输电"),
        ("energy_monitor", "能量监控仪", "方块上显示电量"),
        ("smart_sorter", "智能分拣站", "按规则输出物品"),
        ("network_bridge", "网络桥接器", "两张网络互通"),
    ]
    size = 146
    gap = 14
    total = len(items) * size + (len(items) - 1) * gap
    x = (w - total) / 2
    for key, name, desc in items:
        tile(img, x, 156, size, png(icon_path(key)), name, desc, name_size=16, desc_size=13)
        x += size + gap
    save(img, path)


# ------------------------------------------------------------------ 图 5

def bridge(path):
    w, h = 1040, 510
    img = new_canvas(w, h)
    card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    header(d, "无线桥", "把一张网络延伸到远处，支持跨维度")

    size = 160
    y = 168
    left, right = 84, w - 84 - size
    bridge_icon = png(icon_path("wireless_bridge"))
    tile(img, left, y, size, bridge_icon, "基点", "接在原来那张网络上", name_size=17, desc_size=14)
    tile(img, right, y, size, bridge_icon, "据点", "把网络延伸到这里", name_size=17, desc_size=14)

    mid = y + size / 2
    arrow(d, left + size + 24, mid, right - 24)
    text(d, (w / 2, mid - 36), "配对后自动加载两端区块", 15, ACCENT, anchor="ma")

    text(d, (56, h - 64), "耗电 = 桥本身 64 FE/s + 每个保活区块 512 FE/s；欠费时保留区块，恢复供电后自动接回。",
         15, MUTED)
    save(img, path)


# ------------------------------------------------------------------ 图 6

def monitor(path):
    w, h = 820, 680
    img = new_canvas(w, h)
    card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    header(d, "能量监控仪", "方块上方直接显示，不用右键")

    px0, py0, px1, py1 = 96, 160, w - 96, 330
    d.rounded_rectangle([sc(px0), sc(py0), sc(px1), sc(py1)], radius=sc(12),
                        fill=(20, 26, 33), outline=(56, 66, 78), width=sc(1))
    lines = [
        ("12.3K / 100K FE", "电量 / 容量", (150, 214, 228)),
        ("+120 FE/s", "净收支", (140, 214, 160)),
        ("843s 后耗尽", "预计耗尽", (226, 214, 150)),
    ]
    ly = py0 + 22
    for value, label, colour in lines:
        text(d, (px0 + 28, ly), value, 21, colour, bold=True)
        text(d, (px1 - 28, ly + 5), label, 14, (150, 160, 172), anchor="ra")
        ly += 46

    size = 180
    bx = (w - size) / 2
    by = 372
    tile(img, bx, by, size, png(icon_path("energy_monitor")), "", None)
    text(d, (w / 2, by + size + 14), "能量监控仪", 19, INK, bold=True, anchor="ma")

    text(d, (56, h - 60), "48 格内可见，每 0.5 秒刷新；右键也能在聊天栏看同样的数据。", 15, MUTED)
    save(img, path)


# ------------------------------------------------------------------ 图 7

def energy(path):
    w, h = 980, 630
    img = new_canvas(w, h)
    card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    header(d, "距离越远，单次操作越贵", "基础值：打开界面 5 FE、手动合成 20 FE、自动合成 40 FE")

    left, right, top, bottom = 130, w - 80, 176, 452
    values = [1, 2, 4, 8, 16]
    labels = ["16", "32", "64", "128", "256"]
    peak = 16

    for i in range(5):
        y = bottom - (bottom - top) * i / 4
        d.line([sc(left), sc(y), sc(right), sc(y)], fill=(236, 239, 243), width=sc(1))
    d.line([sc(left), sc(top), sc(left), sc(bottom)], fill=(200, 206, 214), width=sc(1))
    d.line([sc(left), sc(bottom), sc(right), sc(bottom)], fill=(200, 206, 214), width=sc(1))

    bar_w = 86
    slot = (right - left) / 5
    for i, v in enumerate(values):
        bh = (bottom - top - 14) * v / peak
        x0 = left + slot * i + (slot - bar_w) / 2
        y0 = bottom - bh
        d.rounded_rectangle([sc(x0), sc(y0), sc(x0 + bar_w), sc(bottom)], radius=sc(4), fill=ACCENT)
        text(d, (x0 + bar_w / 2, y0 - 30), "%d×" % v, 18, INK, bold=True, anchor="ma")
        text(d, (x0 + bar_w / 2, bottom + 12), labels[i], 15, SUB, anchor="ma")

    text(d, (left - 16, top - 18), "倍率", 14, MUTED, anchor="ra")
    text(d, ((left + right) / 2, bottom + 46), "与网络的距离（格）", 15, SUB, anchor="ma")
    text(d, (56, h - 60), "跨维度固定 16 倍；增幅塔范围内减半，能耗插件可再降。", 15, MUTED)
    save(img, path)


def main():
    hero(os.path.join(OUT, "mcmod-hero.png"))
    terminals(os.path.join(OUT, "mcmod-terminals.png"))
    connector_flow(os.path.join(OUT, "mcmod-connector-flow.png"))
    devices(os.path.join(OUT, "mcmod-devices.png"))
    bridge(os.path.join(OUT, "mcmod-bridge.png"))
    monitor(os.path.join(OUT, "mcmod-monitor.png"))
    energy(os.path.join(OUT, "mcmod-energy-curve.png"))


if __name__ == "__main__":
    main()
