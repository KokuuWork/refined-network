#!/usr/bin/env python3
"""MC百科配图（4:1 横条版，1280×320 画布 → 1024×256），按正文板块拆分。

  01 特点      20 件内容一览
  02 终端      4 件终端与关键数值
  03 升级插件  6 张插件与装配方式
  04 设备工具  7 件设备 + 3 件工具 + 绑定三步
  05 合成表 A  10 张配方
  06 合成表 B  10 张配方
  07 能量耗电  倍率柱状图 + 单次费用

统一版式：左上竖线 + 标题（可带一句行内提示）、右上板块编号；正文一律横向排布，
不再堆行高，插入正文后只占一条。
"""

import glob
import json
import os
import zipfile

from PIL import Image, ImageDraw

import gen_mcmod_images as M
import gen_readme_images as G

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "docs", "images")
ICON = os.path.join(ROOT, "docs", "mcmod", "icons")
RECIPES = os.path.join(
    ROOT, "rswirelesscrafting-common", "src", "main", "resources", "data",
    "rswirelesscrafting", "recipe",
)
LANG_ZH = os.path.join(
    ROOT, "rswirelesscrafting-common", "src", "main", "resources", "assets",
    "rswirelesscrafting", "lang", "zh_cn.json",
)

W, H = 1280, 320
OUT_W, OUT_H = 1024, 256
MARGIN = 40

BG = (255, 255, 255)
CARD = (255, 255, 255)
BORDER = (226, 231, 237)
TILE = (246, 248, 250)
TILE_BORDER = (231, 235, 240)
INK = (28, 32, 38)
SUB = (104, 112, 124)
MUTED = (146, 154, 164)
ACCENT = (32, 132, 156)
DIVIDER = (233, 237, 242)

TOP = 72  # 正文起始线


def icon(name):
    return M.png(os.path.join(ICON, name + ".png"))


_icon_cache = {}


def jar_png(rel):
    with zipfile.ZipFile(os.environ["MC_CLIENT_JAR"]) as zf:
        try:
            data = zf.read(rel)
        except KeyError:
            return None
    tmp = os.path.join(ROOT, "_tmp_icon.png")
    with open(tmp, "wb") as f:
        f.write(data)
    im = M.png(tmp)
    os.remove(tmp)
    return im


TAGS = {
    "c:ender_pearls": "ender_pearl",
    "c:gems/quartz": "quartz",
    "c:glass_blocks": "glass",
    "c:ingots/copper": "copper_ingot",
    "c:ingots/netherite": "netherite_ingot",
}
RS_TAGS = {
    "refinedstorage:crafting_grids": "crafting_grid",
}


def placeholder():
    im = Image.new("RGBA", (32, 32), (240, 243, 246, 255))
    ImageDraw.Draw(im).text((16, 16), "?", anchor="mm", fill=(160, 168, 178, 255))
    return im


def rs_icon(name):
    """RS 的图标可能来自物品纹理、物品状态目录、方块纹理或方块模型目录。"""
    rs = os.environ["RS_TEXTURES"]

    def png(path):
        return M.png(path) if os.path.exists(path) else None

    hit = png(os.path.join(rs, "item", name + ".png"))
    if hit:
        return hit

    item_dir = os.path.join(rs, "item", name)
    if os.path.isdir(item_dir):
        for f in ("crafting.png", "inactive.png", "empty.png"):
            hit = png(os.path.join(item_dir, f))
            if hit:
                return hit
        pngs = sorted(glob.glob(os.path.join(item_dir, "*.png")))
        if pngs:
            return M.png(pngs[0])

    hit = png(os.path.join(rs, "block", name + ".png"))
    if hit:
        return hit

    block_dir = os.path.join(rs, "block", name)
    if os.path.isdir(block_dir):
        front_path = None
        for f in ("front.png", name + ".png"):
            if os.path.exists(os.path.join(block_dir, f)):
                front_path = os.path.join(block_dir, f)
                break
        if front_path is None:
            pngs = sorted(glob.glob(os.path.join(block_dir, "*.png")))
            front_path = pngs[0] if pngs else None
        if front_path:
            front = G.read_png(front_path)
            side = G.read_png(os.path.join(rs, "block", "machine_casing", "side.png")) \
                if os.path.exists(os.path.join(rs, "block", "machine_casing", "side.png")) else front
            top = G.read_png(os.path.join(rs, "block", "machine_casing", "end.png")) \
                if os.path.exists(os.path.join(rs, "block", "machine_casing", "end.png")) else front
            return M.grid_to_img(G.iso_sprite(front, side, top, 64))

    hit = png(os.path.join(rs, "block", "storage_block", name + ".png"))
    if hit:
        return hit

    # 线缆挂件（exporter / importer 之类）：用挂件边框兜底
    return png(os.path.join(rs, "block", "cable", "part_border.png"))


def ingredient_icon(spec):
    key = json.dumps(spec, sort_keys=True, ensure_ascii=False)
    if key in _icon_cache:
        return _icon_cache[key]
    im = None
    if "item" in spec:
        ns, name = spec["item"].split(":", 1)
        if ns == "rswirelesscrafting":
            im = icon(name)
        elif ns == "refinedstorage":
            im = rs_icon(name)
        elif ns == "minecraft":
            im = jar_png("assets/minecraft/textures/item/%s.png" % name)
            if im is None:
                im = jar_png("assets/minecraft/textures/block/%s.png" % name)
    elif spec["tag"] in RS_TAGS:
        im = rs_icon(RS_TAGS[spec["tag"]])
    elif spec["tag"] in TAGS:
        name = TAGS[spec["tag"]]
        im = jar_png("assets/minecraft/textures/item/%s.png" % name)
        if im is None:
            im = jar_png("assets/minecraft/textures/block/%s.png" % name)
    if im is None:
        im = placeholder()
    _icon_cache[key] = im
    return im


def zh_names():
    data = json.load(open(LANG_ZH, encoding="utf-8"))
    names = {}
    for key, value in data.items():
        if key.startswith("item.rswirelesscrafting.") and key.count(".") == 2:
            names[key.split(".")[2]] = value
        elif key.startswith("block.rswirelesscrafting.") and key.count(".") == 2:
            names.setdefault(key.split(".")[2], value)
    return names


# ------------------------------------------------------------------ 版式

def text_width(d, value, size, bold=False):
    return d.textlength(value, font=M.font(size, bold)) / M.S


def frame(kicker, title, hint=None):
    img = M.new_canvas(W, H)
    img.paste(Image.new("RGBA", img.size, BG + (255,)), (0, 0))
    d = ImageDraw.Draw(img)

    d.rectangle([M.sc(MARGIN), M.sc(26), M.sc(MARGIN + 4), M.sc(62)], fill=ACCENT)
    M.text(d, (MARGIN + 16, 20), title, 24, INK, bold=True)
    if hint:
        M.text(d, (MARGIN + 30 + text_width(d, title, 24, True), 28), hint, 12, MUTED)
    M.text(d, (W - MARGIN, 28), kicker, 12, MUTED, anchor="ra")
    return img, d


def chip(img, x, y, w, h, ic, name, note, pad=10, icon_size=40):
    """横条卡：方形图标 + 右侧两行字。"""
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + w), M.sc(y + h)],
                        radius=M.sc(10), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
    img.alpha_composite(ic.resize((M.sc(icon_size), M.sc(icon_size)), Image.NEAREST),
                        (M.sc(x + pad), M.sc(y + (h - icon_size) / 2)))
    tx = x + pad + icon_size + 10
    M.text(d, (tx, y + h / 2 - 15), name, 13, INK, bold=True)
    if note:
        M.text(d, (tx, y + h / 2 + 1), note, 11, SUB)


def save(img, path):
    img.convert("RGB").resize((OUT_W, OUT_H), Image.LANCZOS).save(path)
    print("wrote", path)


def leader(d, x0, x1, y, colour=(224, 230, 237)):
    """标签与数值之间的浅色引导点线。"""
    x = x0
    while x < x1:
        d.line([M.sc(x), M.sc(y), M.sc(min(x + 2, x1)), M.sc(y)], fill=colour, width=M.sc(1))
        x += 6


ITEMS = [
    ("wireless_crafting_grid", "无线合成终端", "远程合成"),
    ("wireless_pattern_grid", "无线样板终端", "编写样板"),
    ("wireless_autocrafter_manager", "自动合成仓管理器", "管理全网"),
    ("wireless_monitor", "无线监控终端", "阈值提醒"),
    ("battery_upgrade", "电池升级插件", "+1000 FE"),
    ("energy_upgrade", "初级能耗插件", "倍率减半"),
    ("advanced_energy_upgrade", "高级能耗插件", "归 100%"),
    ("range_upgrade", "距离升级插件", "不限距离"),
    ("interdimensional_upgrade", "维度升级插件", "跨维度"),
    ("remote_energy_upgrade", "远程输电插件", "走网络扣电"),
    ("terminal_charging_station", "终端充电站", "3 槽·128 FE/s"),
    ("wireless_amplifier", "无线增幅塔", "64 格内减半"),
    ("wireless_bridge", "无线桥", "延伸网络"),
    ("wireless_energy_receiver", "能量接收器", "跨网输电"),
    ("energy_monitor", "能量监控仪", "方块上显示"),
    ("smart_sorter", "智能分拣站", "3×3 过滤"),
    ("network_bridge", "网络桥接器", "两网互通"),
    ("wireless_connector", "无线连接器", "绑定设备"),
    ("wireless_configuration_card", "无线配置卡", "复制设置"),
    ("overclock_upgrade", "超频升级卡", "间隔减半"),
]


# ------------------------------------------------------------------ 01

def overview(path):
    img, d = frame("01 · 特点", "全部内容", hint="20 件：4 件终端 · 6 张插件 · 7 件设备 · 3 件工具")
    cols = 5
    gap_x, gap_y = 16, 6
    cw = (W - MARGIN * 2 - gap_x * (cols - 1)) / cols
    for i, (key, name, note) in enumerate(ITEMS):
        r, c = divmod(i, cols)
        chip(img, MARGIN + c * (cw + gap_x), TOP + r * 58, cw, 52,
             icon(key), name, note)
    save(img, path)


# ------------------------------------------------------------------ 02

def terminals(path):
    img, d = frame("02 · 终端", "四件终端", hint="绑定一次，之后在任何地方打开")

    data = [
        ("wireless_crafting_grid", "无线合成终端", "容量 400 FE · 4 槽", "合成 / 自动合成"),
        ("wireless_pattern_grid", "无线样板终端", "4 种样板类型", "编写自动化样板"),
        ("wireless_autocrafter_manager", "自动合成仓管理器", "全网范围", "查看 / 移动样板"),
        ("wireless_monitor", "无线监控终端", "最多 4 条规则", "阈值提醒 / 自动补货"),
    ]
    cw, gap = 282, 24
    top, bottom = 76, 226
    x = MARGIN
    for key, name, line1, line2 in data:
        d.rounded_rectangle([M.sc(x), M.sc(top), M.sc(x + cw), M.sc(bottom)],
                            radius=M.sc(14), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
        img.alpha_composite(icon(key).resize((M.sc(104), M.sc(104)), Image.NEAREST),
                            (M.sc(x + 18), M.sc(top + 23)))
        M.text(d, (x + 132, top + 26), name, 16, INK, bold=True)
        M.text(d, (x + 132, top + 58), line1, 13, SUB)
        M.text(d, (x + 132, top + 86), line2, 13, ACCENT)
        x += cw + gap

    chips = [("终端容量", "400 FE"), ("插件槽位", "4 个"), ("单次费用", "5 – 40 FE")]
    cx = MARGIN
    for label, value in chips:
        d.rounded_rectangle([M.sc(cx), M.sc(244), M.sc(cx + 372), M.sc(298)],
                            radius=M.sc(12), fill=(249, 251, 253),
                            outline=TILE_BORDER, width=M.sc(1))
        M.text(d, (cx + 22, 262), label, 12, SUB)
        M.text(d, (cx + 350, 258), value, 17, ACCENT, bold=True, anchor="ra")
        cx += 396
    save(img, path)


# ------------------------------------------------------------------ 03

def upgrades(path):
    img, d = frame("03 · 升级插件", "六张升级插件", hint="装配：手持终端潜行 + 右键，4 个槽位，拖入即装")

    data = [
        ("battery_upgrade", "电池升级插件", "每张 +1000 FE 容量", "可叠加"),
        ("energy_upgrade", "初级能耗插件", "距离倍率减半", "最低 100%"),
        ("advanced_energy_upgrade", "高级能耗插件", "同维度倍率归 100%", "跨维度不变"),
        ("range_upgrade", "距离升级插件", "同维度不限距离", "终局配方"),
        ("interdimensional_upgrade", "维度升级插件", "解锁跨维度访问", "需变送器 + 接收器"),
        ("remote_energy_upgrade", "远程输电插件", "扣电走绑定网络", "不足回落到电池"),
    ]
    cw, ch = 386, 104
    gap = 20
    for i, (key, name, eff, note) in enumerate(data):
        r, c = divmod(i, 3)
        x = MARGIN + c * (cw + gap)
        y = 76 + r * 114
        d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + cw), M.sc(y + ch)],
                            radius=M.sc(14), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
        img.alpha_composite(icon(key).resize((M.sc(60), M.sc(60)), Image.NEAREST),
                            (M.sc(x + 20), M.sc(y + 22)))
        M.text(d, (x + 96, y + 16), name, 16, INK, bold=True)
        M.text(d, (x + 96, y + 48), eff, 13, SUB)
        M.text(d, (x + 96, y + 74), note, 12, ACCENT)
    save(img, path)


# ------------------------------------------------------------------ 04

def devices_tools(path):
    img, d = frame("04 · 设备与工具", "七件设备 · 三件工具",
                   hint="方块设备用无线连接器绑定，绑定后与终端共用同一张网络的电量")

    devices = [
        ("terminal_charging_station", "终端充电站", "3 槽·128 FE/s"),
        ("wireless_amplifier", "无线增幅塔", "64 格内减半"),
        ("wireless_bridge", "无线桥", "延伸网络"),
        ("wireless_energy_receiver", "能量接收器", "跨网输电"),
        ("energy_monitor", "能量监控仪", "方块上显示"),
        ("smart_sorter", "智能分拣站", "3×3 过滤"),
        ("network_bridge", "网络桥接器", "两网互通"),
    ]
    w, h, gap = 162, 92, 10
    x = (W - (len(devices) * w + (len(devices) - 1) * gap)) / 2
    for key, name, note in devices:
        chip(img, x, 74, w, h, icon(key), name, note, icon_size=44)
        x += w + gap

    d.line([M.sc(MARGIN), M.sc(180), M.sc(W - MARGIN), M.sc(180)], fill=DIVIDER, width=M.sc(1))

    tools = [
        ("wireless_connector", "无线连接器", "绑定设备"),
        ("wireless_configuration_card", "无线配置卡", "复制设置"),
        ("overclock_upgrade", "超频升级卡", "间隔减半"),
    ]
    x = MARGIN
    for key, name, note in tools:
        chip(img, x, 196, w, h, icon(key), name, note, icon_size=44)
        x += w + gap

    bx = 596
    M.text(d, (bx, 194), "绑定方块设备的方式", 14, INK, bold=True)
    steps = [
        "① 连接器右键网络方块，记录接入点",
        "② 右键要绑定的设备，两端亮起光环与粒子线",
        "③ 再次右键解绑；潜行右键清除记录",
    ]
    for i, line in enumerate(steps):
        M.text(d, (bx, 222 + i * 26), line, 12, SUB)
    save(img, path)


# ------------------------------------------------------------------ 05/06

def recipe_card(img, x, y, w, h, recipe, name):
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + w), M.sc(y + h)],
                        radius=M.sc(10), fill=CARD, outline=BORDER, width=M.sc(1))
    M.text(d, (x + 12, y + 3), name, 12, INK, bold=True)

    cell, gap = 26, 3
    gx0, gy0 = x + 12, y + 18
    for r, row in enumerate(recipe["pattern"]):
        for c, ch in enumerate(row):
            if ch == " ":
                continue
            gx = gx0 + c * (cell + gap)
            gy = gy0 + r * (cell + gap)
            d.rounded_rectangle([M.sc(gx), M.sc(gy), M.sc(gx + cell), M.sc(gy + cell)],
                                radius=M.sc(4), fill=TILE)
            img.alpha_composite(
                ingredient_icon(recipe["key"][ch]).resize(
                    (M.sc(cell - 4), M.sc(cell - 4)), Image.NEAREST),
                (M.sc(gx + 2), M.sc(gy + 2)))

    grid_w = 3 * cell + 2 * gap
    cy = gy0 + cell * 1.5
    ax = gx0 + grid_w + 6
    M.arrow(d, ax, cy, ax + 18)
    rx = ax + 22
    d.rounded_rectangle([M.sc(rx), M.sc(cy - 19), M.sc(rx + 38), M.sc(cy + 19)],
                        radius=M.sc(7), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
    img.alpha_composite(
        icon(recipe["result"]["id"].split(":")[1]).resize((M.sc(32), M.sc(32)), Image.NEAREST),
        (M.sc(rx + 3), M.sc(cy - 16)))

    materials = len({json.dumps(v, sort_keys=True) for v in recipe["key"].values()})
    M.text(d, (rx + 46, cy - 7), "%d 种材料" % materials, 11, MUTED)


def recipes(path, part):
    files = sorted(glob.glob(os.path.join(RECIPES, "*.json")))
    names = zh_names()
    order = [item[0] for item in ITEMS]
    data = []
    for f in files:
        rid = os.path.basename(f)[:-5]
        data.append((json.load(open(f, encoding="utf-8")), names.get(rid, rid)))
    data.sort(key=lambda pair: order.index(pair[0]["result"]["id"].split(":")[1]))
    chunk = data[part * 10:(part + 1) * 10]

    img, d = frame("05 · 合成表 %s" % ("A" if part == 0 else "B"),
                   "合成表" + ("（一）" if part == 0 else "（二）"),
                   hint="有序合成，全部后期配方；无线连接器以 RS 扳手为核心")
    cols, gap = 5, 16
    cw = (W - MARGIN * 2 - gap * (cols - 1)) / cols
    for i, (recipe, name) in enumerate(chunk):
        r, c = divmod(i, cols)
        recipe_card(img, MARGIN + c * (cw + gap), 74 + r * 116, cw, 108, recipe, name)
    save(img, path)


# ------------------------------------------------------------------ 07

def energy(path):
    img, d = frame("07 · 能量与耗电", "能量与耗电", hint="距离越远越贵，跨维度固定 16 倍")

    left, right, top, bottom = 110, 640, 92, 250
    values = [1, 2, 4, 8, 16]
    labels = ["16", "32", "64", "128", "256"]
    for i in range(5):
        y = bottom - (bottom - top) * i / 4
        d.line([M.sc(left), M.sc(y), M.sc(right), M.sc(y)], fill=(238, 241, 245), width=M.sc(1))
    d.line([M.sc(left), M.sc(top), M.sc(left), M.sc(bottom)], fill=(204, 210, 218), width=M.sc(1))
    d.line([M.sc(left), M.sc(bottom), M.sc(right), M.sc(bottom)], fill=(204, 210, 218), width=M.sc(1))

    bar_w = 52
    slot = (right - left) / 5
    for i, v in enumerate(values):
        bh = (bottom - top - 12) * v / 16
        x0 = left + slot * i + (slot - bar_w) / 2
        y0 = bottom - bh
        d.rounded_rectangle([M.sc(x0), M.sc(y0), M.sc(x0 + bar_w), M.sc(bottom)],
                            radius=M.sc(4), fill=ACCENT)
        M.text(d, (x0 + bar_w / 2, y0 - 20), "%d×" % v, 12, INK, bold=True, anchor="ma")
        M.text(d, (x0 + bar_w / 2, bottom + 8), labels[i], 12, SUB, anchor="ma")
    M.text(d, (left - 14, top - 12), "倍率", 11, MUTED, anchor="ra")
    M.text(d, ((left + right) / 2, bottom + 32), "与网络的距离（格）", 12, SUB, anchor="ma")

    fx = 700
    M.text(d, (fx, 92), "单次基础费用", 13, INK, bold=True)
    fees = [("打开界面", "5 FE"), ("手动合成", "20 FE"), ("自动合成", "40 FE"),
            ("清空矩阵", "5 FE"), ("配方转移", "5 FE"), ("样板产出一张", "40 FE")]
    for i, (label, value) in enumerate(fees):
        yy = 120 + i * 26
        M.text(d, (fx, yy), label, 12, SUB)
        leader(d, fx + text_width(d, label, 12) + 10, W - MARGIN - 46, yy + 8)
        M.text(d, (W - MARGIN, yy), value, 12, INK, bold=True, anchor="ra")

    M.text(d, (fx, 278), "终端容量 400 FE · 电池插件每张 +1000 FE · 增幅塔 128 FE/s", 11, MUTED)
    save(img, path)


def main():
    overview(os.path.join(OUT, "mcmod-01-overview.png"))
    terminals(os.path.join(OUT, "mcmod-02-terminals.png"))
    upgrades(os.path.join(OUT, "mcmod-03-upgrades.png"))
    devices_tools(os.path.join(OUT, "mcmod-04-devices-tools.png"))
    recipes(os.path.join(OUT, "mcmod-05-recipes-a.png"), 0)
    recipes(os.path.join(OUT, "mcmod-06-recipes-b.png"), 1)
    energy(os.path.join(OUT, "mcmod-07-energy.png"))


if __name__ == "__main__":
    main()
