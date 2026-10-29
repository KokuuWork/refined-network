#!/usr/bin/env python3
"""MC百科配图（16:9，1280×720），按正文板块拆分。

  01 特点      概念 + 20 件内容全展示
  02 终端      4 件终端与关键数值
  03 升级插件  6 张插件与装配方式
  04 设备工具  7 件设备数值 + 3 件工具 + 绑定三步
  05 合成表 A  10 张配方
  06 合成表 B  10 张配方
  07 能量耗电  倍率柱状图 + 单次费用

统一版式：左上标题块（竖线 + 标题 + 副标题）、右上板块编号、底部细线与一行注脚。
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

W, H = 1280, 720
MARGIN = 48

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
RS_SPECIAL = {
    "machine_casing": "block/machine_casing/side.png",
    "crafting_grid": "block/crafting_grid/front.png",
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

def frame(kicker, title, sub, note):
    img = M.new_canvas(W, H)
    img.paste(Image.new("RGBA", img.size, BG + (255,)), (0, 0))
    d = ImageDraw.Draw(img)

    d.rectangle([M.sc(MARGIN), M.sc(54), M.sc(MARGIN + 4), M.sc(102)], fill=ACCENT)
    M.text(d, (MARGIN + 20, 50), title, 32, INK, bold=True)
    M.text(d, (MARGIN + 20, 96), sub, 15, SUB)
    M.text(d, (W - MARGIN, 58), kicker, 13, MUTED, anchor="ra")

    d.line([M.sc(MARGIN), M.sc(H - 62), M.sc(W - MARGIN), M.sc(H - 62)],
           fill=DIVIDER, width=M.sc(1))
    M.text(d, (MARGIN, H - 46), note, 13, MUTED)
    return img, d


def tile(img, x, y, size, icon_img, name, note, name_size=14, note_size=11, pad_ratio=0.16):
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + size), M.sc(y + size)],
                        radius=M.sc(12), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
    pad = size * pad_ratio
    inner = int(round((size - 2 * pad) * M.S))
    img.alpha_composite(icon_img.resize((inner, inner), Image.NEAREST),
                        (M.sc(x + pad), M.sc(y + pad)))
    if name:
        M.text(d, (x + size / 2, y + size + 8), name, name_size, INK, bold=True, anchor="ma")
    if note:
        M.text(d, (x + size / 2, y + size + 10 + name_size + 5), note, note_size, SUB, anchor="ma")


def save(img, path):
    img.convert("RGB").resize((W, H), Image.LANCZOS).save(path)
    print("wrote", path)


ITEMS = [
    ("wireless_crafting_grid", "无线合成终端"),
    ("wireless_pattern_grid", "无线样板终端"),
    ("wireless_autocrafter_manager", "自动合成仓管理器"),
    ("wireless_monitor", "无线监控终端"),
    ("battery_upgrade", "电池升级插件"),
    ("energy_upgrade", "初级能耗插件"),
    ("advanced_energy_upgrade", "高级能耗插件"),
    ("range_upgrade", "距离升级插件"),
    ("interdimensional_upgrade", "维度升级插件"),
    ("remote_energy_upgrade", "远程输电插件"),
    ("terminal_charging_station", "终端充电站"),
    ("wireless_amplifier", "无线增幅塔"),
    ("wireless_bridge", "无线桥"),
    ("wireless_energy_receiver", "能量接收器"),
    ("energy_monitor", "能量监控仪"),
    ("smart_sorter", "智能分拣站"),
    ("network_bridge", "网络桥接器"),
    ("wireless_connector", "无线连接器"),
    ("wireless_configuration_card", "无线配置卡"),
    ("overclock_upgrade", "超频升级卡"),
]


# ------------------------------------------------------------------ 01

def overview(path):
    img, d = frame("01 · 特点", "无线合成终端",
                   "精致存储的无线合成附属 · 绑定一次，之后在任何地方打开网络合成界面",
                   "终端按次计费；方块设备用无线连接器绑定；插件装在终端的 4 个槽位里，随时可拆。")

    size = 116
    y = 138
    xs = [96, 542, 988]
    tile(img, xs[0], y, size, icon("wireless_crafting_grid"), "无线合成终端", "手持使用")
    tile(img, xs[1], y, size, M.rs_cube("transmitter", size * 0.7), "无线访问点", "网络的入口")
    tile(img, xs[2], y, size, M.rs_cube("controller", size * 0.7), "控制器", "网络本体")
    mid = y + size / 2
    M.arrow(d, xs[0] + size + 40, mid, xs[1] - 40)
    M.arrow(d, xs[1] + size + 40, mid, xs[2] - 40)

    d.line([M.sc(MARGIN), M.sc(318), M.sc(W - MARGIN), M.sc(318)], fill=DIVIDER, width=M.sc(1))
    M.text(d, (MARGIN, 332), "全部内容", 17, INK, bold=True)
    M.text(d, (W - MARGIN, 336), "终端 4 · 升级插件 6 · 方块设备 7 · 工具 3", 12, MUTED, anchor="ra")

    cols = 10
    gap = 10
    cw = (W - MARGIN * 2 - gap * (cols - 1)) / cols
    for i, (key, name) in enumerate(ITEMS):
        r, c = divmod(i, cols)
        x = MARGIN + c * (cw + gap)
        y = 368 + r * 128
        tile(img, x + (cw - 100) / 2, y, 100, icon(key), name, None, name_size=12)
    save(img, path)


# ------------------------------------------------------------------ 02

def terminals(path):
    img, d = frame("02 · 终端", "四件终端",
                   "都能远程打开，插件槽与绑定方式完全相同；容量与单次费用一致",
                   "打开界面 5 FE · 手动合成 20 FE · 自动合成 40 FE · 清空矩阵 5 FE（基础值，乘以距离倍率）")

    data = [
        ("wireless_crafting_grid", "无线合成终端", "容量 400 FE · 插件槽 4", "合成 / 自动合成"),
        ("wireless_pattern_grid", "无线样板终端", "4 种样板类型", "编写自动化样板"),
        ("wireless_autocrafter_manager", "自动合成仓管理器", "全网范围", "查看 / 移动样板"),
        ("wireless_monitor", "无线监控终端", "最多 4 条规则", "阈值提醒 / 自动补货"),
    ]
    cw, gap = 282, 24
    x = MARGIN
    for key, name, line1, line2 in data:
        d.rounded_rectangle([M.sc(x), M.sc(136), M.sc(x + cw), M.sc(474)],
                            radius=M.sc(16), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
        img.alpha_composite(icon(key).resize((M.sc(156), M.sc(156)), Image.NEAREST),
                            (M.sc(x + (cw - 156) / 2), M.sc(170)))
        M.text(d, (x + cw / 2, 356), name, 18, INK, bold=True, anchor="ma")
        M.text(d, (x + cw / 2, 394), line1, 13, SUB, anchor="ma")
        M.text(d, (x + cw / 2, 422), line2, 13, ACCENT, anchor="ma")
        x += cw + gap

    chips = [("终端容量", "400 FE"), ("插件槽位", "4 个"), ("单次费用", "5 – 40 FE")]
    cx = MARGIN
    for label, value in chips:
        d.rounded_rectangle([M.sc(cx), M.sc(516), M.sc(cx + 372), M.sc(590)],
                            radius=M.sc(14), fill=(249, 251, 253),
                            outline=TILE_BORDER, width=M.sc(1))
        M.text(d, (cx + 24, 538), label, 13, SUB)
        M.text(d, (cx + 348, 532), value, 20, ACCENT, bold=True, anchor="ra")
        cx += 396
    save(img, path)


# ------------------------------------------------------------------ 03

def upgrades(path):
    img, d = frame("03 · 升级插件", "六张升级插件",
                   "装在终端的 4 个槽位里，拖入即装、拖出即卸，不用重做终端",
                   "装配方式：手持任意无线终端（含精致存储原生的无线终端）→ 潜行 + 右键点击 → 4 个槽位。")

    data = [
        ("battery_upgrade", "电池升级插件", "每张 +1000 FE 容量", "可叠加"),
        ("energy_upgrade", "初级能耗插件", "距离倍率减半", "最低 100%"),
        ("advanced_energy_upgrade", "高级能耗插件", "同维度倍率归 100%", "跨维度不变"),
        ("range_upgrade", "距离升级插件", "同维度不限距离", "终局配方"),
        ("interdimensional_upgrade", "维度升级插件", "解锁跨维度访问", "需变送器 + 接收器"),
        ("remote_energy_upgrade", "远程输电插件", "扣电走绑定网络", "不足回落到电池"),
    ]
    cw, ch = 392, 186
    gap = 20
    for i, (key, name, eff, note) in enumerate(data):
        r, c = divmod(i, 3)
        x = MARGIN + c * (cw + gap)
        y = 142 + r * (ch + 24)
        d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + cw), M.sc(y + ch)],
                            radius=M.sc(16), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
        img.alpha_composite(icon(key).resize((M.sc(76), M.sc(76)), Image.NEAREST),
                            (M.sc(x + 24), M.sc(y + 28)))
        M.text(d, (x + 120, y + 30), name, 17, INK, bold=True)
        M.text(d, (x + 120, y + 66), eff, 14, SUB)
        M.text(d, (x + 120, y + 96), note, 13, ACCENT)
    save(img, path)


# ------------------------------------------------------------------ 04

def devices_tools(path):
    img, d = frame("04 · 设备与工具", "七件设备 · 三件工具",
                   "设备全部接入同一张精致存储网络；工具负责绑定、复制设置与超频",
                   "方块设备不能手持右键绑定 —— 它们是方块物品，右键会先放置，所以绑定统一走连接器。")

    devices = [
        ("terminal_charging_station", "终端充电站", "3 槽 · 待机 128 FE/s"),
        ("wireless_amplifier", "无线增幅塔", "半径 64 格 · 128 FE/s"),
        ("wireless_bridge", "无线桥", "64 + 512 FE/s 每区块"),
        ("wireless_energy_receiver", "能量接收器", "带宽 容量 1/10 每秒"),
        ("energy_monitor", "能量监控仪", "48 格显示 · 0.5 秒刷新"),
        ("smart_sorter", "智能分拣站", "3×3 过滤 · 按物品/标签"),
        ("network_bridge", "网络桥接器", "两张网络互通"),
    ]
    size = 140
    gap = 17
    total = len(devices) * size + (len(devices) - 1) * gap
    x = (W - total) / 2
    for key, name, note in devices:
        tile(img, x, 146, size, icon(key), name, note, name_size=13, note_size=11)
        x += size + gap

    d.line([M.sc(MARGIN), M.sc(360), M.sc(W - MARGIN), M.sc(360)], fill=DIVIDER, width=M.sc(1))

    tools = [
        ("wireless_connector", "无线连接器", "绑定方块设备"),
        ("wireless_configuration_card", "无线配置卡", "复制 RS 方块设置"),
        ("overclock_upgrade", "超频升级卡", "间隔减半 · 最多 4 张"),
    ]
    x = MARGIN
    for key, name, note in tools:
        tile(img, x, 400, 132, icon(key), name, note, name_size=13, note_size=11)
        x += 132 + 20

    bx = 520
    M.text(d, (bx, 404), "绑定方块设备的方式", 15, INK, bold=True)
    steps = [
        "① 手持连接器，右键任意精致存储网络方块，记录接入点",
        "② 右键要绑定的设备，完成绑定；两端亮起光环与粒子线",
        "③ 再次右键已绑定的设备即解绑；潜行右键清除记录",
    ]
    for i, line in enumerate(steps):
        M.text(d, (bx, 440 + i * 40), line, 13, SUB)
    save(img, path)


# ------------------------------------------------------------------ 05/06

def recipe_card(img, x, y, w, h, recipe, name):
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + w), M.sc(y + h)],
                        radius=M.sc(14), fill=CARD, outline=BORDER, width=M.sc(1))
    M.text(d, (x + 16, y + 14), name, 15, INK, bold=True)

    cell_size, gap = 34, 4
    gx0, gy0 = x + 16, y + 56
    for r, row in enumerate(recipe["pattern"]):
        for c, ch in enumerate(row):
            if ch == " ":
                continue
            gx = gx0 + c * (cell_size + gap)
            gy = gy0 + r * (cell_size + gap)
            d.rounded_rectangle([M.sc(gx), M.sc(gy), M.sc(gx + cell_size), M.sc(gy + cell_size)],
                                radius=M.sc(5), fill=TILE)
            img.alpha_composite(
                ingredient_icon(recipe["key"][ch]).resize(
                    (M.sc(cell_size - 6), M.sc(cell_size - 6)), Image.NEAREST),
                (M.sc(gx + 3), M.sc(gy + 3)))

    grid_w = 3 * cell_size + 2 * gap
    cy = gy0 + cell_size * 1.5
    ax = gx0 + grid_w + 8
    M.arrow(d, ax, cy, ax + 22)
    rx = ax + 26
    d.rounded_rectangle([M.sc(rx), M.sc(cy - 21), M.sc(rx + 42), M.sc(cy + 21)],
                        radius=M.sc(8), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
    img.alpha_composite(
        icon(recipe["result"]["id"].split(":")[1]).resize((M.sc(36), M.sc(36)), Image.NEAREST),
        (M.sc(rx + 3), M.sc(cy - 18)))

    materials = len({json.dumps(v, sort_keys=True) for v in recipe["key"].values()})
    M.text(d, (x + 16, y + h - 34), "有序合成 · %d 种材料" % materials, 11, MUTED)


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
                   "20 件内容全部为有序合成，材料取自精致存储与后期材料；顺序与「全部内容」一致",
                   "材料图标取自模组数据文件，与游戏内配方一致。")
    cols, gap = 5, 16
    cw = (W - MARGIN * 2 - gap * (cols - 1)) / cols
    for i, (recipe, name) in enumerate(chunk):
        r, c = divmod(i, cols)
        recipe_card(img, MARGIN + c * (cw + gap), 150 + r * 244, cw, 226, recipe, name)
    save(img, path)


# ------------------------------------------------------------------ 07

def energy(path):
    img, d = frame("07 · 能量与耗电", "能量与耗电",
                   "按次收费，不按在线时间；每次动作 = 基础费用 × 距离倍率",
                   "跨维度固定 16 倍；增幅塔半径 64 格内倍率减半；能耗插件可再降。倍率与实际扣费同一套公式。")

    left, right, top, bottom = 110, 700, 180, 520
    values = [1, 2, 4, 8, 16]
    labels = ["16", "32", "64", "128", "256"]
    for i in range(5):
        y = bottom - (bottom - top) * i / 4
        d.line([M.sc(left), M.sc(y), M.sc(right), M.sc(y)], fill=(238, 241, 245), width=M.sc(1))
    d.line([M.sc(left), M.sc(top), M.sc(left), M.sc(bottom)], fill=(204, 210, 218), width=M.sc(1))
    d.line([M.sc(left), M.sc(bottom), M.sc(right), M.sc(bottom)], fill=(204, 210, 218), width=M.sc(1))

    bar_w = 62
    slot = (right - left) / 5
    for i, v in enumerate(values):
        bh = (bottom - top - 14) * v / 16
        x0 = left + slot * i + (slot - bar_w) / 2
        y0 = bottom - bh
        d.rounded_rectangle([M.sc(x0), M.sc(y0), M.sc(x0 + bar_w), M.sc(bottom)],
                            radius=M.sc(5), fill=ACCENT)
        M.text(d, (x0 + bar_w / 2, y0 - 28), "%d×" % v, 16, INK, bold=True, anchor="ma")
        M.text(d, (x0 + bar_w / 2, bottom + 10), labels[i], 12, SUB, anchor="ma")
    M.text(d, ((left + right) / 2, bottom + 42), "与网络的距离（格）", 13, SUB, anchor="ma")

    fx = 760
    M.text(d, (fx, 180), "单次基础费用", 16, INK, bold=True)
    fees = [("打开界面", "5 FE"), ("手动合成", "20 FE"), ("自动合成", "40 FE"),
            ("清空矩阵", "5 FE"), ("配方转移", "5 FE"), ("样板产出一张", "40 FE")]
    for i, (label, value) in enumerate(fees):
        yy = 218 + i * 34
        M.text(d, (fx, yy), label, 13, SUB)
        M.text(d, (W - MARGIN, yy), value, 13, INK, bold=True, anchor="ra")

    M.text(d, (fx, 440), "终端容量 400 FE", 13, SUB)
    M.text(d, (fx, 468), "电池插件每张 +1000 FE", 13, SUB)
    M.text(d, (fx, 496), "增幅塔 128 FE/s · 桥 64 FE/s", 13, SUB)
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
