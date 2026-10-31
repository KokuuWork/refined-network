#!/usr/bin/env python3
"""生成 MC百科「批量添加资料」JSON（1.13+ 格式，每行一条）。

格式依据（MC百科管理员给出的手动批量导入规范）：
  {"name","englishName","registerName","type","OredictList",
   "maxStackSize","maxDurability","smallIcon","largeIcon"}
  - 1.13+ 无 metadata 字段
  - OredictList 在 1.13+ 填物品标签，格式 "[a,b]"
  - 图标为 PNG 的 base64，不带 data:image/png;base64, 前缀

图标来源：本模组自己的纹理。物品直接放大；方块用等轴投影把
顶面 / 正面 / 侧面拼成一个方块图标（顶部最亮、正面次之、侧面最暗）。
"""

import base64
import io
import json
import os
import re
import sys
import struct
import zlib

ROOT = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(
    ROOT, "refinednetwork-common", "src", "main", "resources", "assets", "refinednetwork"
)
LANG_ZH = os.path.join(ASSETS, "lang", "zh_cn.json")
LANG_EN = os.path.join(ASSETS, "lang", "en_us.json")
OUT_DIR = os.environ.get("MCMOD_OUT_DIR") or os.path.join(ROOT, "docs", "mcmod")
ICON_DIR = os.path.join(OUT_DIR, "icons")

MOD_ID = "refinednetwork"


def mod_version():
    """从 NeoForge 模块的构建脚本读当前版本，避免文档里写死旧版本号。"""
    build = os.path.join(ROOT, "refinednetwork-neoforge", "build.gradle.kts")
    with open(build, encoding="utf-8") as f:
        match = re.search(r'^version = "([^"]+)"', f.read(), re.MULTILINE)
    return match.group(1) if match else "unknown"

# 逐条定义：注册名、资料类型、堆叠上限、图标来源
# icon: ("item", 相对 textures/item 的路径) 或 ("block", 正/侧/顶 三张图)
ENTRIES = [
    ("wireless_crafting_grid", "物品", 1, ("item", "wireless_crafting_grid/inactive.png")),
    ("wireless_pattern_grid", "物品", 1, ("item", "wireless_pattern_grid/inactive.png")),
    ("wireless_autocrafter_manager", "物品", 1, ("item", "wireless_autocrafter_manager/inactive.png")),
    ("wireless_monitor", "物品", 1, ("item", "wireless_monitor/inactive.png")),
    ("battery_upgrade", "物品", 64, ("item", "battery_upgrade.png")),
    ("energy_upgrade", "物品", 64, ("item", "energy_upgrade.png")),
    ("advanced_energy_upgrade", "物品", 64, ("item", "advanced_energy_upgrade.png")),
    ("range_upgrade", "物品", 64, ("item", "range_upgrade.png")),
    ("interdimensional_upgrade", "物品", 64, ("item", "interdimensional_upgrade.png")),
    ("remote_energy_upgrade", "物品", 64, ("item", "remote_energy_upgrade.png")),
    ("terminal_charging_station", "方块", 64,
     ("block", "terminal_charging_station.png", "terminal_charging_station_side.png",
      "terminal_charging_station_top.png")),
    ("wireless_amplifier", "方块", 64,
     ("block", "wireless_amplifier.png", "wireless_amplifier_side.png", "wireless_amplifier_top.png")),
    ("wireless_bridge", "方块", 64,
     ("block", "wireless_bridge.png", "wireless_bridge_side.png", "wireless_bridge_top.png")),
    ("wireless_energy_receiver", "方块", 64,
     ("block", "wireless_energy_receiver.png", "wireless_energy_receiver_side.png",
      "wireless_energy_receiver_top.png")),
    ("energy_monitor", "方块", 64,
     ("block", "energy_monitor.png", "energy_monitor_side.png", "energy_monitor_top.png")),
    ("smart_sorter", "方块", 64,
     ("block", "smart_sorter_front.png", "smart_sorter.png", "smart_sorter_top.png")),
    ("network_bridge", "方块", 64,
     ("block", "network_bridge.png", "network_bridge_side.png", "network_bridge_top.png")),
    ("wireless_connector", "工具", 1, ("item", "wireless_connector.png")),
    ("wireless_configuration_card", "工具", 1, ("item", "wireless_configuration_card.png")),
    ("overclock_upgrade", "物品", 64, ("item", "overclock_upgrade.png")),
    ("wireless_redstone_link", "方块", 64,
     ("block", "redstone_link.png", "redstone_link_side.png", "redstone_link_top.png")),
]

COS30 = 0.8660254037844387
SIN30 = 0.5


# ---------------- PNG 读写 ----------------

def read_png(path):
    data = open(path, "rb").read()
    pos = 8
    width = height = 0
    color_type = 6
    idat = b""
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        tag = data[pos + 4:pos + 8]
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if tag == b"IHDR":
            width, height, _, color_type = struct.unpack(">IIBB", body[:10])
        elif tag == b"IDAT":
            idat += body
    raw = zlib.decompress(idat)
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color_type]
    stride = width * channels
    out = []
    prev = bytearray(stride)
    i = 0
    for _ in range(height):
        ftype = raw[i]
        i += 1
        line = bytearray(raw[i:i + stride])
        i += stride
        if ftype == 0:
            pass
        elif ftype == 1:
            for x in range(channels, stride):
                line[x] = (line[x] + line[x - channels]) & 0xFF
        elif ftype == 2:
            for x in range(stride):
                line[x] = (line[x] + prev[x]) & 0xFF
        elif ftype == 3:
            for x in range(stride):
                left = line[x - channels] if x >= channels else 0
                line[x] = (line[x] + ((left + prev[x]) >> 1)) & 0xFF
        elif ftype == 4:
            for x in range(stride):
                a = line[x - channels] if x >= channels else 0
                b = prev[x]
                c = prev[x - channels] if x >= channels else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pred = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[x] = (line[x] + pred) & 0xFF
        else:
            raise ValueError("unsupported filter %d" % ftype)
        rows = []
        for x in range(width):
            px = line[x * channels:(x + 1) * channels]
            if channels == 4:
                rows.append(tuple(px))
            elif channels == 3:
                rows.append((px[0], px[1], px[2], 255))
            else:
                v = px[0]
                rows.append((v, v, v, 255))
        out.append(rows)
        prev = line
    return width, height, out


def write_png(path, rows):
    height = len(rows)
    width = len(rows[0])
    buf = bytearray()
    for row in rows:
        buf.append(0)
        for px in row:
            buf.extend(px)

    def chunk(tag, body):
        return (struct.pack(">I", len(body)) + tag + body
                + struct.pack(">I", zlib.crc32(tag + body) & 0xFFFFFFFF))

    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(bytes(buf), 9))
           + chunk(b"IEND", b""))
    with open(path, "wb") as f:
        f.write(png)
    return png


# ---------------- 图标 ----------------

def scale_item(tex_rows, size):
    h = len(tex_rows)
    w = len(tex_rows[0])
    out = []
    for y in range(size):
        sy = min(h - 1, y * h // size)
        row = []
        for x in range(size):
            sx = min(w - 1, x * w // size)
            row.append(tex_rows[sy][sx])
        out.append(row)
    return out


def project(pt):
    x, y, z = pt
    return ((x - z) * COS30, (x + z) * SIN30 - y)


def draw_triangle(canvas, tri, uvs, tex, shade, size):
    """tri: 三个屏幕点（已含边距）；uvs: 三个 (u,v)。"""
    xs = [p[0] for p in tri]
    ys = [p[1] for p in tri]
    x0 = max(0, int(min(xs)))
    x1 = min(size - 1, int(max(xs)) + 1)
    y0 = max(0, int(min(ys)))
    y1 = min(size - 1, int(max(ys)) + 1)
    ax, ay = tri[0]
    bx, by = tri[1]
    cx, cy = tri[2]
    denom = (by - cy) * (ax - cx) + (cx - bx) * (ay - cy)
    if abs(denom) < 1e-9:
        return
    th = len(tex)
    tw = len(tex[0])
    for py in range(y0, y1 + 1):
        for px in range(x0, x1 + 1):
            sx = px + 0.5
            sy = py + 0.5
            w0 = ((by - cy) * (sx - cx) + (cx - bx) * (sy - cy)) / denom
            w1 = ((cy - ay) * (sx - cx) + (ax - cx) * (sy - cy)) / denom
            w2 = 1.0 - w0 - w1
            eps = -0.002
            if w0 < eps or w1 < eps or w2 < eps:
                continue
            u = w0 * uvs[0][0] + w1 * uvs[1][0] + w2 * uvs[2][0]
            v = w0 * uvs[0][1] + w1 * uvs[1][1] + w2 * uvs[2][1]
            tx = min(tw - 1, max(0, int(u * tw)))
            ty = min(th - 1, max(0, int(v * th)))
            r, g, b, a = tex[ty][tx]
            if a == 0:
                continue
            canvas[py][px] = (
                int(r * shade), int(g * shade), int(b * shade), a
            )


def render_block(front_path, side_path, top_path, size=64):
    _, _, front = read_png(front_path)
    _, _, side = read_png(side_path)
    _, _, top = read_png(top_path)

    ss = 4
    work = size * ss
    # 投影包围盒：x∈[-0.866,0.866]，y∈[-1,1]，留 6% 边距
    span_x = 2 * COS30
    span_y = 2.0
    margin = 0.06
    scale = min(work / (span_x * (1 + 2 * margin)), work / (span_y * (1 + 2 * margin)))
    off_x = work / 2.0
    off_y = work / 2.0

    def to_screen(pt):
        sx, sy = project(pt)
        return (off_x + sx * scale, off_y + sy * scale)

    canvas = [[(0, 0, 0, 0)] * work for _ in range(work)]

    # 顶面
    top_pts = [(0, 1, 0), (1, 1, 0), (1, 1, 1), (0, 1, 1)]
    # 正面（z=1）
    front_pts = [(0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)]
    # 右侧面（x=1）
    side_pts = [(1, 0, 0), (1, 0, 1), (1, 1, 1), (1, 1, 0)]

    def face(pts3, tex, shade):
        p = [to_screen(v) for v in pts3]
        uv = [(0.0, 1.0), (1.0, 1.0), (1.0, 0.0), (0.0, 0.0)]
        draw_triangle(canvas, [p[0], p[1], p[2]], [uv[0], uv[1], uv[2]], tex, shade, work)
        draw_triangle(canvas, [p[0], p[2], p[3]], [uv[0], uv[2], uv[3]], tex, shade, work)

    face(top_pts, top, 1.0)
    face(front_pts, front, 0.86)
    face(side_pts, side, 0.62)

    # 降采样
    out = []
    for y in range(size):
        row = []
        for x in range(size):
            r = g = b = a = 0
            for dy in range(ss):
                for dx in range(ss):
                    px = canvas[y * ss + dy][x * ss + dx]
                    r += px[0] * px[3]
                    g += px[1] * px[3]
                    b += px[2] * px[3]
                    a += px[3]
            n = ss * ss
            if a == 0:
                row.append((0, 0, 0, 0))
            else:
                row.append((r // a, g // a, b // a, a // n))
        out.append(row)
    return out


def build_icon(spec, size):
    if spec[0] == "item":
        _, _, tex = read_png(os.path.join(ASSETS, "textures", "item", spec[1]))
        return scale_item(tex, size)
    _, front, side, top = spec
    block_dir = os.path.join(ASSETS, "textures", "block")
    # 方块图标统一按 64 渲染，再按需缩放
    base = render_block(os.path.join(block_dir, front),
                        os.path.join(block_dir, side),
                        os.path.join(block_dir, top), 64)
    return base if size == 64 else scale_item(base, size)


def png_bytes(rows):
    buf = io.BytesIO()
    height = len(rows)
    width = len(rows[0])
    raw = bytearray()
    for row in rows:
        raw.append(0)
        for px in row:
            raw.extend(px)

    def chunk(tag, body):
        return (struct.pack(">I", len(body)) + tag + body
                + struct.pack(">I", zlib.crc32(tag + body) & 0xFFFFFFFF))

    buf.write(b"\x89PNG\r\n\x1a\n")
    buf.write(chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
    buf.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
    buf.write(chunk(b"IEND", b""))
    return buf.getvalue()


# ---------------- 主流程 ----------------

def main():
    zh = json.load(open(LANG_ZH, encoding="utf-8"))
    en = json.load(open(LANG_EN, encoding="utf-8"))
    os.makedirs(ICON_DIR, exist_ok=True)

    lines = []
    table = []
    for reg, kind, stack, icon_spec in ENTRIES:
        name = zh.get("item.%s.%s" % (MOD_ID, reg)) or zh["block.%s.%s" % (MOD_ID, reg)]
        english = en.get("item.%s.%s" % (MOD_ID, reg)) or en["block.%s.%s" % (MOD_ID, reg)]
        large = build_icon(icon_spec, 64)
        small = scale_item(large, 32)
        write_png(os.path.join(ICON_DIR, reg + ".png"), large)
        write_png(os.path.join(ICON_DIR, reg + "_32.png"), small)
        entry = {
            "name": name,
            "englishName": english,
            "registerName": "%s:%s" % (MOD_ID, reg),
            "type": kind,
            "OredictList": "[]",
            "maxStackSize": stack,
            "maxDurability": 0,
            "smallIcon": base64.b64encode(png_bytes(small)).decode("ascii"),
            "largeIcon": base64.b64encode(png_bytes(large)).decode("ascii"),
        }
        lines.append(json.dumps(entry, ensure_ascii=False))
        table.append((name, english, "%s:%s" % (MOD_ID, reg), kind, stack))
        print("built", reg, name)

    json_path = os.path.join(OUT_DIR, "mcmod-items.json")
    with open(json_path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")

    md = []
    md.append("# MC百科 资料条目提交包\n")
    version = mod_version()
    counts = {}
    for _, _, _, kind, _ in table:
        counts[kind] = counts.get(kind, 0) + 1
    summary = "、".join("%s %d" % (kind, counts[kind]) for kind in sorted(counts))
    md.append("适用版本：**%s**（Minecraft 1.21.1，NeoForge / Fabric）。" % version)
    md.append("共 %d 条资料：%s。\n" % (len(table), summary))
    md.append("## 文件说明\n")
    md.append("| 文件 | 用途 |")
    md.append("|---|---|")
    md.append("| `mcmod-items.json` | 批量导入用。**每行一个 JSON**，直接整份贴进站内批量导入 |")
    md.append("| `icons/<注册名>.png` | 64×64 图标（JSON 里已内嵌，此目录供核对或替换） |")
    md.append("| `icons/<注册名>_32.png` | 32×32 小图标 |\n")
    md.append("## 提交步骤\n")
    md.append("1. 打开 [批量添加资料 JSON 收纳帖](https://bbs.mcmod.cn/thread-1281-1-1.html)，按帖子格式回帖：模组名称、站内 ID（31918）、模组版本 `%s`、游戏版本 `1.21.1 NeoForge`、导出工具填「手动整理」。" % version)
    md.append("2. 附上 `mcmod-items.json`。每行一条，导入器会按 `I:` 计数。")
    md.append("3. 导入后按类型批量调整：把 7 个方块设备从「方块」改为「机器」（如果站内分类更合适）。")
    md.append("4. 合成表单独提交，用 RecipeDumper 或 IconRenderer 导出，本站格式为 `R:` 行。\n")
    md.append("## 条目清单\n")
    md.append("| 名称 | 英文名 | 注册名 | 类型 | 堆叠 |")
    md.append("|---|---|---|---|---|")
    for name, english, reg, kind, stack in table:
        md.append("| %s | %s | `%s` | %s | %d |" % (name, english, reg, kind, stack))
    md.append("\n## 图标说明\n")
    md.append("- 物品图标取自模组自带纹理，按最近邻放大到 32 / 64，保持像素风；终端使用「未绑定」状态的外观。")
    md.append("- 方块图标由顶面 / 正面 / 侧面纹理做等轴投影合成，**不是游戏内真实渲染**。若站内审核要求实机渲染图，用 IconRenderer 导出一份替换 `icons/` 下的同名文件即可，JSON 结构不变。")
    md.append("- 图标为 PNG 的 base64，**不带** `data:image/png;base64,` 前缀（站内格式要求）。\n")
    md.append("## 字段说明\n")
    md.append("| 字段 | 取值 |")
    md.append("|---|---|")
    md.append("| `registerName` | `refinednetwork:<注册名>`，与游戏内一致 |")
    md.append("| `englishName` | 取 `en_us.json`，未自行翻译 |")
    md.append("| `OredictList` | `[]`，本模组未给这些物品加物品标签 |")
    md.append("| `maxStackSize` | 终端 / 工具为 1，插件与方块为 64（取自 `Item.Properties`） |")
    md.append("| `maxDurability` | `0`，全部无耐久 |")
    md.append("\n> 1.13+ 格式不含 `metadata` 字段，导入器会忽略旧字段。\n")

    # 默认写到 generated 文件：资料条目.md 是人工维护的提交稿，直接覆盖会丢掉勘误。
    force = "--force" in sys.argv
    md_path = os.path.join(OUT_DIR, "资料条目.md" if force else "资料条目.generated.md")
    with open(md_path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(md))

    print("wrote", json_path)
    print("wrote", md_path)
    if not force:
        print("提示：默认不覆盖人工维护的 资料条目.md；确认要覆盖时加 --force")


if __name__ == "__main__":
    main()
