#!/usr/bin/env python3
"""按 MC百科正文标题生成配图（6 张，浅色卡片风格）。

每张对应一个板块：
  01 总览（特点）      概念 + 20 件内容全展示
  02 终端              4 件终端与关键数值
  03 升级插件          6 张插件效果 + 装配方式
  04 设备与工具        7 件设备数值 + 3 件工具 + 绑定三步
  05 合成表            20 张配方
  06 能量与耗电        倍率柱状图 + 单次费用
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

MUTED = M.MUTED
SUB = M.SUB
INK = M.INK
ACCENT = M.ACCENT
BORDER = M.BORDER
CARD = M.CARD
TILE = M.TILE
TILE_BORDER = M.TILE_BORDER


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
    "c:ender_pearls": ("minecraft", "ender_pearl"),
    "c:gems/quartz": ("minecraft", "quartz"),
    "c:glass_blocks": ("minecraft", "glass"),
    "c:ingots/copper": ("minecraft", "copper_ingot"),
    "c:ingots/netherite": ("minecraft", "netherite_ingot"),
}

RS_SPECIAL = {
    "machine_casing": "block/machine_casing/side.png",
    "crafting_grid": "block/crafting_grid/front.png",
}


def placeholder():
    im = Image.new("RGBA", (32, 32), (238, 241, 245, 255))
    d = ImageDraw.Draw(im)
    d.text((16, 16), "?", anchor="mm", fill=(160, 168, 178, 255))
    return im


def ingredient_icon(spec):
    key = json.dumps(spec, sort_keys=True, ensure_ascii=False)
    if key in _icon_cache:
        return _icon_cache[key]
    im = None
    if "item" in spec:
        iid = spec["item"]
        ns, name = iid.split(":", 1)
        if ns == "rswirelesscrafting":
            im = icon(name)
        elif ns == "refinedstorage":
            rs = os.environ["RS_TEXTURES"]
            for rel in (RS_SPECIAL.get(name), "item/%s.png" % name, "block/%s.png" % name):
                if not rel:
                    continue
                path = os.path.join(rs, rel)
                if os.path.exists(path):
                    im = M.png(path)
                    break
        elif ns == "minecraft":
            im = jar_png("assets/minecraft/textures/item/%s.png" % name)
            if im is None:
                im = jar_png("assets/minecraft/textures/block/%s.png" % name)
    else:
        tag = spec["tag"]
        if tag in TAGS:
            ns, name = TAGS[tag]
            im = jar_png("assets/minecraft/textures/item/%s.png" % name) or \
                jar_png("assets/minecraft/textures/block/%s.png" % name)
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


def cell(img, x, y, size, icon_img, name, note, name_size=14, note_size=12):
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + size), M.sc(y + size)],
                        radius=M.sc(12), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
    pad = size * 0.16
    inner = int(round((size - 2 * pad) * M.S))
    img.alpha_composite(icon_img.resize((inner, inner), Image.NEAREST),
                        (M.sc(x + pad), M.sc(y + pad)))
    if name:
        M.text(d, (x + size / 2, y + size + 8), name, name_size, INK, bold=True, anchor="ma")
    if note:
        M.text(d, (x + size / 2, y + size + 8 + name_size + 6), note, note_size, SUB, anchor="ma")


ITEMS = [
    ("wireless_crafting_grid", "无线合成终端", "终端"),
    ("wireless_pattern_grid", "无线样板终端", "终端"),
    ("wireless_autocrafter_manager", "自动合成仓管理器", "终端"),
    ("wireless_monitor", "无线监控终端", "终端"),
    ("battery_upgrade", "电池升级插件", "插件"),
    ("energy_upgrade", "初级能耗插件", "插件"),
    ("advanced_energy_upgrade", "高级能耗插件", "插件"),
    ("range_upgrade", "距离升级插件", "插件"),
    ("interdimensional_upgrade", "维度升级插件", "插件"),
    ("remote_energy_upgrade", "远程输电插件", "插件"),
    ("terminal_charging_station", "终端充电站", "设备"),
    ("wireless_amplifier", "无线增幅塔", "设备"),
    ("wireless_bridge", "无线桥", "设备"),
    ("wireless_energy_receiver", "能量接收器", "设备"),
    ("energy_monitor", "能量监控仪", "设备"),
    ("smart_sorter", "智能分拣站", "设备"),
    ("network_bridge", "网络桥接器", "设备"),
    ("wireless_connector", "无线连接器", "工具"),
    ("wireless_configuration_card", "无线配置卡", "工具"),
    ("overclock_upgrade", "超频升级卡", "工具"),
]


# ------------------------------------------------------------------ 01 总览

def overview(path):
    w, h = 1200, 1230
    img = M.new_canvas(w, h)
    M.card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)

    M.text(d, (56, 40), "无线合成终端", 30, INK, bold=True)
    M.text(d, (56, 90), "精致存储的无线合成附属 · 20 件内容 · 绑定一次，之后在任何地方打开网络合成界面", 16, SUB)

    size = 140
    y = 150
    xs = [96, 528, 960]
    cell(img, xs[0], y, size, icon("wireless_crafting_grid"), "无线合成终端", "手持使用")
    cell(img, xs[1], y, size, M.rs_cube("transmitter", size * 0.7), "无线访问点", "网络的入口")
    cell(img, xs[2], y, size, M.rs_cube("controller", size * 0.7), "控制器", "网络本体")
    mid = y + size / 2
    M.arrow(d, xs[0] + size + 26, mid, xs[1] - 26)
    M.arrow(d, xs[1] + size + 26, mid, xs[2] - 26)

    d.line([M.sc(56), M.sc(392), M.sc(w - 56), M.sc(392)], fill=BORDER, width=M.sc(1))
    M.text(d, (56, 410), "全部内容", 18, INK, bold=True)
    M.text(d, (w - 56, 414), "终端 4 · 升级插件 6 · 方块设备 7 · 工具 3", 13, MUTED, anchor="ra")

    cols = 5
    gap = 16
    cw = (w - 112 - gap * (cols - 1)) / cols
    ch = 168
    y0 = 452
    for i, (key, name, kind) in enumerate(ITEMS):
        r, c = divmod(i, cols)
        x = 56 + c * (cw + gap)
        yy = y0 + r * (ch + gap)
        cell(img, x, yy, 104, icon(key), name, kind, name_size=14, note_size=12)

    M.text(d, (56, h - 44),
           "终端按次计费；方块设备用无线连接器绑定；插件装在终端的 4 个槽位里，随时可拆。",
           15, MUTED)
    M.save(img, path)


# ------------------------------------------------------------------ 02 终端

def terminals(path):
    w, h = 1200, 560
    img = M.new_canvas(w, h)
    M.card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    M.text(d, (56, 40), "四件终端", 28, INK, bold=True)
    M.text(d, (56, 86), "都能远程打开，插件槽与绑定方式完全相同；容量与单次费用一致", 15, SUB)

    data = [
        ("wireless_crafting_grid", "无线合成终端", ["容量 400 FE · 插件槽 4", "合成 / 自动合成"]),
        ("wireless_pattern_grid", "无线样板终端", ["4 种样板类型", "编写自动化样板"]),
        ("wireless_autocrafter_manager", "自动合成仓管理器", ["全网范围", "查看 / 移动样板"]),
        ("wireless_monitor", "无线监控终端", ["最多 4 条规则", "阈值提醒 / 自动补货"]),
    ]
    cw, gap = 268, 20
    x = 56
    for key, name, lines in data:
        d.rounded_rectangle([M.sc(x), M.sc(160), M.sc(x + cw), M.sc(430)],
                            radius=M.sc(14), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
        img.alpha_composite(icon(key).resize((M.sc(112), M.sc(112)), Image.NEAREST),
                            (M.sc(x + (cw - 112) / 2), M.sc(186)))
        M.text(d, (x + cw / 2, 318), name, 17, INK, bold=True, anchor="ma")
        M.text(d, (x + cw / 2, 352), lines[0], 13, SUB, anchor="ma")
        M.text(d, (x + cw / 2, 380), lines[1], 13, ACCENT, anchor="ma")
        x += cw + gap

    M.text(d, (56, h - 74),
           "打开界面 5 FE · 手动合成 20 FE · 自动合成 40 FE · 清空矩阵 5 FE（基础值，乘以距离倍率）",
           14, MUTED)
    M.save(img, path)


# ------------------------------------------------------------------ 03 插件

def upgrades(path):
    w, h = 1200, 590
    img = M.new_canvas(w, h)
    M.card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    M.text(d, (56, 40), "六张升级插件", 28, INK, bold=True)
    M.text(d, (56, 86), "装在终端的 4 个槽位里，拖入即装、拖出即卸，不用重做终端", 15, SUB)

    data = [
        ("battery_upgrade", "电池升级插件", "每张 +1000 FE 容量", "可叠加"),
        ("energy_upgrade", "初级能耗插件", "距离倍率减半", "最低 100%"),
        ("advanced_energy_upgrade", "高级能耗插件", "同维度倍率归 100%", "跨维度不变"),
        ("range_upgrade", "距离升级插件", "同维度不限距离", "终局配方"),
        ("interdimensional_upgrade", "维度升级插件", "解锁跨维度访问", "需变送器 + 接收器"),
        ("remote_energy_upgrade", "远程输电插件", "扣电走绑定网络", "不足回落到电池"),
    ]
    cw, chh = 356, 150
    gap = 20
    for i, (key, name, eff, note) in enumerate(data):
        r, c = divmod(i, 3)
        x = 56 + c * (cw + gap)
        y = 140 + r * (chh + gap)
        d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + cw), M.sc(y + chh)],
                            radius=M.sc(14), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
        img.alpha_composite(icon(key).resize((M.sc(64), M.sc(64)), Image.NEAREST),
                            (M.sc(x + 20), M.sc(y + 24)))
        M.text(d, (x + 100, y + 26), name, 16, INK, bold=True)
        M.text(d, (x + 100, y + 58), eff, 13, SUB)
        M.text(d, (x + 100, y + 86), note, 13, ACCENT)

    M.text(d, (56, 512), "装配方式", 15, INK, bold=True)
    M.text(d, (56, 540), "手持任意无线终端（含精致存储原生的无线终端）→ 潜行 + 右键点击 → 4 个槽位，插件拖入即生效。",
           14, MUTED)
    M.save(img, path)


# ------------------------------------------------------------------ 04 设备与工具

def devices_tools(path):
    w, h = 1240, 700
    img = M.new_canvas(w, h)
    M.card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    M.text(d, (56, 40), "七件方块设备", 26, INK, bold=True)
    M.text(d, (56, 84), "全部接入同一张精致存储网络，用线缆或无线桥连过去即可", 14, SUB)

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
    gap = 12
    x = 56
    y = 130
    for key, name, note in devices:
        cell(img, x, y, size, icon(key), name, note, name_size=13, note_size=11)
        x += size + gap

    d.line([M.sc(56), M.sc(392), M.sc(w - 56), M.sc(392)], fill=BORDER, width=M.sc(1))
    M.text(d, (56, 412), "三件工具", 22, INK, bold=True)

    tools = [
        ("wireless_connector", "无线连接器", "绑定方块设备"),
        ("wireless_configuration_card", "无线配置卡", "复制 RS 方块设置"),
        ("overclock_upgrade", "超频升级卡", "等待间隔减半 · 最多 4 张"),
    ]
    x = 56
    y = 462
    for key, name, note in tools:
        cell(img, x, y, 120, icon(key), name, note, name_size=13, note_size=11)
        x += 120 + 16

    bx = 520
    M.text(d, (bx, 470), "绑定方块设备的方式", 15, INK, bold=True)
    steps = [
        "① 手持连接器，右键任意精致存储网络方块，记录接入点",
        "② 右键要绑定的设备，完成绑定；两端亮起光环与粒子线",
        "③ 再次右键已绑定的设备即解绑；潜行右键清除记录",
    ]
    for i, line in enumerate(steps):
        M.text(d, (bx, 502 + i * 34), line, 13, SUB)

    M.text(d, (56, h - 58),
           "方块设备不能手持右键绑定 —— 它们是方块物品，右键会先放置。所以绑定统一走连接器。",
           13, MUTED)
    M.save(img, path)


# ------------------------------------------------------------------ 05 合成表

def recipe_card(img, x, y, w, h, recipe, name):
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([M.sc(x), M.sc(y), M.sc(x + w), M.sc(y + h)],
                        radius=M.sc(12), fill=CARD, outline=BORDER, width=M.sc(1))
    M.text(d, (x + 14, y + 12), name, 14, INK, bold=True)

    shaped = "pattern" in recipe
    cell_size = 32
    gap = 4
    grid_x = x + 14
    grid_y = y + 44
    if shaped:
        key = recipe["key"]
        for r, row in enumerate(recipe["pattern"]):
            for c, ch in enumerate(row):
                if ch == " ":
                    continue
                spec = key[ch]
                gx = grid_x + c * (cell_size + gap)
                gy = grid_y + r * (cell_size + gap)
                d.rounded_rectangle([M.sc(gx), M.sc(gy), M.sc(gx + cell_size), M.sc(gy + cell_size)],
                                    radius=M.sc(4), fill=TILE)
                img.alpha_composite(
                    ingredient_icon(spec).resize((M.sc(cell_size - 4), M.sc(cell_size - 4)),
                                                 Image.NEAREST),
                    (M.sc(gx + 2), M.sc(gy + 2)),
                )
        grid_w = 3 * cell_size + 2 * gap
    else:
        specs = recipe["ingredients"]
        grid_w = 0
        for i, spec in enumerate(specs[:4]):
            gx = grid_x + i * (cell_size + gap)
            d.rounded_rectangle([M.sc(gx), M.sc(grid_y), M.sc(gx + cell_size),
                                 M.sc(grid_y + cell_size)], radius=M.sc(4), fill=TILE)
            img.alpha_composite(
                ingredient_icon(spec).resize((M.sc(cell_size - 4), M.sc(cell_size - 4)),
                                             Image.NEAREST),
                (M.sc(gx + 2), M.sc(grid_y + 2)),
            )
            grid_w = (i + 1) * (cell_size + gap) - gap

    ax = grid_x + grid_w + 12
    M.arrow(d, ax, grid_y + cell_size, ax + 26)
    rx = ax + 36
    rs = 40
    d.rounded_rectangle([M.sc(rx), M.sc(grid_y - 4), M.sc(rx + rs), M.sc(grid_y - 4 + rs)],
                        radius=M.sc(6), fill=TILE, outline=TILE_BORDER, width=M.sc(1))
    img.alpha_composite(icon(recipe["result"]["id"].split(":")[1]).resize((M.sc(rs - 6), M.sc(rs - 6)),
                                                                         Image.NEAREST),
                        (M.sc(rx + 3), M.sc(grid_y - 1)))


def recipes(path):
    files = sorted(glob.glob(os.path.join(RECIPES, "*.json")))
    names = zh_names()
    recipes_data = []
    for f in files:
        data = json.load(open(f, encoding="utf-8"))
        rid = os.path.basename(f)[:-5]
        recipes_data.append((data, names.get(rid, rid)))

    order = [item[0] for item in ITEMS]
    recipes_data.sort(key=lambda pair: order.index(pair[0]["result"]["id"].split(":")[1]))

    cols = 4
    gap = 18
    cw = (1200 - 112 - gap * (cols - 1)) / cols
    ch = 150
    rows = (len(recipes_data) + cols - 1) // cols
    h = 130 + rows * (ch + gap) + 50
    w = 1200
    img = M.new_canvas(w, h)
    M.card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    M.text(d, (56, 40), "合成表", 28, INK, bold=True)
    M.text(d, (56, 86), "20 件内容全部为有序合成，材料均为精致存储与后期材料", 15, SUB)

    for i, (data, name) in enumerate(recipes_data):
        r, c = divmod(i, cols)
        x = 56 + c * (cw + gap)
        y = 130 + r * (ch + gap)
        recipe_card(img, x, y, cw, ch, data, name)

    M.text(d, (56, h - 56), "配方材料取自模组数据文件，与游戏内一致。", 13, MUTED)
    M.save(img, path)


# ------------------------------------------------------------------ 06 能量

def energy(path):
    w, h = 980, 660
    img = M.new_canvas(w, h)
    M.card(img, (24, 24, w - 24, h - 24), radius=20)
    d = ImageDraw.Draw(img)
    M.text(d, (56, 40), "能量与耗电", 28, INK, bold=True)
    M.text(d, (56, 86), "按次收费，不按在线时间；每次动作 = 基础费用 × 距离倍率", 15, SUB)

    left, right, top, bottom = 120, 600, 170, 420
    values = [1, 2, 4, 8, 16]
    labels = ["16", "32", "64", "128", "256"]
    for i in range(5):
        y = bottom - (bottom - top) * i / 4
        d.line([M.sc(left), M.sc(y), M.sc(right), M.sc(y)], fill=(236, 239, 243), width=M.sc(1))
    d.line([M.sc(left), M.sc(top), M.sc(left), M.sc(bottom)], fill=(200, 206, 214), width=M.sc(1))
    d.line([M.sc(left), M.sc(bottom), M.sc(right), M.sc(bottom)], fill=(200, 206, 214), width=M.sc(1))

    bar_w = 56
    slot = (right - left) / 5
    for i, v in enumerate(values):
        bh = (bottom - top - 12) * v / 16
        x0 = left + slot * i + (slot - bar_w) / 2
        y0 = bottom - bh
        d.rounded_rectangle([M.sc(x0), M.sc(y0), M.sc(x0 + bar_w), M.sc(bottom)],
                            radius=M.sc(4), fill=ACCENT)
        M.text(d, (x0 + bar_w / 2, y0 - 26), "%d×" % v, 15, INK, bold=True, anchor="ma")
        M.text(d, (x0 + bar_w / 2, bottom + 10), labels[i], 12, SUB, anchor="ma")
    M.text(d, ((left + right) / 2, bottom + 40), "与网络的距离（格）", 13, SUB, anchor="ma")

    fx = 650
    M.text(d, (fx, 170), "单次基础费用", 15, INK, bold=True)
    fees = [
        ("打开界面", "5 FE"),
        ("手动合成", "20 FE"),
        ("自动合成", "40 FE"),
        ("清空矩阵", "5 FE"),
        ("配方转移", "5 FE"),
        ("样板产出一张", "40 FE"),
    ]
    for i, (label, value) in enumerate(fees):
        yy = 204 + i * 30
        M.text(d, (fx, yy), label, 13, SUB)
        M.text(d, (w - 56, yy), value, 13, INK, bold=True, anchor="ra")

    M.text(d, (fx, 400), "终端容量 400 FE", 13, SUB)
    M.text(d, (fx, 426), "电池插件每张 +1000 FE", 13, SUB)

    M.text(d, (56, h - 88), "跨维度固定 16 倍；增幅塔半径 64 格内倍率减半；能耗插件可再降。", 14, MUTED)
    M.text(d, (56, h - 60), "倍率与实际扣费使用同一套公式，终端悬停提示会实时显示当前倍率。", 14, MUTED)
    M.save(img, path)


def main():
    overview(os.path.join(OUT, "mcmod-01-overview.png"))
    terminals(os.path.join(OUT, "mcmod-02-terminals.png"))
    upgrades(os.path.join(OUT, "mcmod-03-upgrades.png"))
    devices_tools(os.path.join(OUT, "mcmod-04-devices-tools.png"))
    recipes(os.path.join(OUT, "mcmod-05-recipes.png"))
    energy(os.path.join(OUT, "mcmod-06-energy.png"))


if __name__ == "__main__":
    main()
