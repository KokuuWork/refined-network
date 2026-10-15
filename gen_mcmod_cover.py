"""生成 MC 百科（mcmod.cn）模组封面，尺寸 240x150。

只用 Python 标准库（zlib + struct），不需要 Pillow。
素材取自本项目的 16x16 物品 / 方块纹理，按 2 倍最近邻放大保持像素风。
"""

import struct
import zlib

WIDTH = 240
HEIGHT = 150

TEX_DIR = "rswirelesscrafting-common/src/main/resources/assets/rswirelesscrafting/textures"
OUTPUT = "docs/images/mcmod-cover-240x150.png"

BG = (27, 32, 41)
GRID = (36, 43, 54)
ACCENT = (64, 200, 255)
TEXT = (222, 232, 242)
DIM = (128, 145, 165)

# 封面里的 8 个图标：前 7 个是物品，最后一个是方块。
ITEMS = [
    ("item/wireless_crafting_grid/active.png", "无线合成终端"),
    ("item/battery_upgrade.png", "电池升级插件"),
    ("item/energy_upgrade.png", "初级能耗插件"),
    ("item/advanced_energy_upgrade.png", "高级能耗插件"),
    ("item/range_upgrade.png", "距离升级插件"),
    ("item/interdimensional_upgrade.png", "维度升级插件"),
    ("item/remote_energy_upgrade.png", "远程输电插件"),
    ("block/terminal_charging_station.png", "终端充电站"),
]

FONT = {
    "A": ["01110", "10001", "10001", "11111", "10001", "10001", "10001"],
    "B": ["11110", "10001", "10001", "11110", "10001", "10001", "11110"],
    "C": ["01110", "10001", "10000", "10000", "10000", "10001", "01110"],
    "D": ["11110", "10001", "10001", "10001", "10001", "10001", "11110"],
    "E": ["11111", "10000", "10000", "11110", "10000", "10000", "11111"],
    "F": ["11111", "10000", "10000", "11110", "10000", "10000", "10000"],
    "G": ["01110", "10001", "10000", "10111", "10001", "10001", "01110"],
    "H": ["10001", "10001", "10001", "11111", "10001", "10001", "10001"],
    "I": ["11111", "00100", "00100", "00100", "00100", "00100", "11111"],
    "J": ["00111", "00010", "00010", "00010", "00010", "10010", "01100"],
    "K": ["10001", "10010", "10100", "11000", "10100", "10010", "10001"],
    "L": ["10000", "10000", "10000", "10000", "10000", "10000", "11111"],
    "M": ["10001", "11011", "10101", "10101", "10001", "10001", "10001"],
    "N": ["10001", "11001", "10101", "10011", "10001", "10001", "10001"],
    "O": ["01110", "10001", "10001", "10001", "10001", "10001", "01110"],
    "P": ["11110", "10001", "10001", "11110", "10000", "10000", "10000"],
    "Q": ["01110", "10001", "10001", "10001", "10101", "10011", "01101"],
    "R": ["11110", "10001", "10001", "11110", "10100", "10010", "10001"],
    "S": ["01111", "10000", "10000", "01110", "00001", "00001", "11110"],
    "T": ["11111", "00100", "00100", "00100", "00100", "00100", "00100"],
    "U": ["10001", "10001", "10001", "10001", "10001", "10001", "01110"],
    "V": ["10001", "10001", "10001", "10001", "10001", "01010", "00100"],
    "W": ["10001", "10001", "10001", "10101", "10101", "11011", "10001"],
    "X": ["10001", "10001", "01010", "00100", "01010", "10001", "10001"],
    "Y": ["10001", "10001", "01010", "00100", "00100", "00100", "00100"],
    "Z": ["11111", "00001", "00010", "00100", "01000", "10000", "11111"],
    " ": ["00000", "00000", "00000", "00000", "00000", "00000", "00000"],
    "-": ["00000", "00000", "00000", "11111", "00000", "00000", "00000"],
    ".": ["00000", "00000", "00000", "00000", "00000", "01100", "01100"],
}


def read_png(path):
    """解码 8 位 RGBA、非隔行的 PNG，返回 (width, height, bytearray RGBA)。"""
    data = open(path, "rb").read()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("not a png: " + path)
    pos = 8
    width = height = 0
    idat = bytearray()
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        ctag = data[pos + 4:pos + 8]
        body = data[pos + 8:pos + 8 + length]
        if ctag == b"IHDR":
            width, height, depth, color, _, _, interlace = struct.unpack(">IIBBBBB", body)
            if (depth, color, interlace) != (8, 6, 0):
                raise ValueError("unsupported png (%d/%d/%d): %s" % (depth, color, interlace, path))
        elif ctag == b"IDAT":
            idat += body
        elif ctag == b"IEND":
            break
        pos += 12 + length

    raw = zlib.decompress(bytes(idat))
    stride = width * 4
    out = bytearray(width * height * 4)
    prev = bytearray(stride)
    p = 0
    for y in range(height):
        ft = raw[p]
        p += 1
        line = bytearray(raw[p:p + stride])
        p += stride
        if ft == 1:
            for i in range(4, stride):
                line[i] = (line[i] + line[i - 4]) & 0xFF
        elif ft == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 0xFF
        elif ft == 3:
            for i in range(stride):
                left = line[i - 4] if i >= 4 else 0
                line[i] = (line[i] + ((left + prev[i]) >> 1)) & 0xFF
        elif ft == 4:
            for i in range(stride):
                a = line[i - 4] if i >= 4 else 0
                b = prev[i]
                c = prev[i - 4] if i >= 4 else 0
                pa = abs(b - c)
                pb = abs(a - c)
                pc = abs(a + b - 2 * c)
                pred = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pred) & 0xFF
        out[y * stride:(y + 1) * stride] = line
        prev = line
    return width, height, out


def write_png(path, width, height, pixels):
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        raw += pixels[y * width * 4:(y + 1) * width * 4]

    def chunk(tag, body):
        return (struct.pack(">I", len(body)) + tag + body
                + struct.pack(">I", zlib.crc32(tag + body) & 0xFFFFFFFF))

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr)
           + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b""))
    open(path, "wb").write(png)


class Canvas:
    def __init__(self):
        self.px = bytearray(WIDTH * HEIGHT * 4)
        for i in range(WIDTH * HEIGHT):
            self.px[i * 4:i * 4 + 4] = bytes(BG + (255,))

    def blend(self, x, y, color, alpha=255):
        if x < 0 or y < 0 or x >= WIDTH or y >= HEIGHT or alpha <= 0:
            return
        i = (y * WIDTH + x) * 4
        a = alpha / 255.0
        for c in range(3):
            self.px[i + c] = int(self.px[i + c] * (1 - a) + color[c] * a)

    def fill_rect(self, x, y, w, h, color, alpha=255):
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                self.blend(xx, yy, color, alpha)

    def draw_text(self, x, y, text, color, scale=1):
        cx = x
        for ch in text:
            glyph = FONT.get(ch.upper(), FONT[" "])
            for gy, row in enumerate(glyph):
                for gx, bit in enumerate(row):
                    if bit == "1":
                        self.fill_rect(cx + gx * scale, y + gy * scale, scale, scale, color)
            cx += 6 * scale
        return cx - x

    def draw_icon(self, x, y, pixels, size, scale):
        """最近邻放大绘制 16x16 像素图标（带 1px 深色描边以在深背景上更清晰）。"""
        for sy in range(size):
            for sx in range(size):
                i = (sy * size + sx) * 4
                r, g, b, a = pixels[i], pixels[i + 1], pixels[i + 2], pixels[i + 3]
                if a == 0:
                    continue
                for dy in range(scale):
                    for dx in range(scale):
                        self.blend(x + sx * scale + dx, y + sy * scale + dy, (r, g, b), a)


def main():
    canvas = Canvas()

    # 背景：细网格，模拟 RS 的终端格栅。
    for y in range(HEIGHT):
        if y % 8 == 0:
            for x in range(WIDTH):
                canvas.blend(x, y, GRID)
    for x in range(WIDTH):
        if x % 8 == 0:
            for y in range(HEIGHT):
                canvas.blend(x, y, GRID)

    # 顶部 / 底部强调条。
    canvas.fill_rect(0, 0, WIDTH, 3, ACCENT)
    canvas.fill_rect(0, HEIGHT - 3, WIDTH, 3, ACCENT, 160)

    # 标题。
    title = "RSWC"
    tw = len(title) * 6 * 3 - 3
    canvas.draw_text((WIDTH - tw) // 2, 12, title, TEXT, 3)

    # 副标题。
    sub = "RS2 WIRELESS CRAFTING"
    sw = len(sub) * 6 - 1
    canvas.draw_text((WIDTH - sw) // 2, 128, sub, DIM, 1)

    # 8 个图标：4 列 x 2 行，2 倍放大。
    scale = 2
    size = 16
    icon = size * scale
    gap_x, gap_y = 10, 10
    grid_w = 4 * icon + 3 * gap_x
    grid_h = 2 * icon + gap_y
    ox = (WIDTH - grid_w) // 2
    oy = 44

    for index, (rel, _name) in enumerate(ITEMS):
        w, h, pixels = read_png("%s/%s" % (TEX_DIR, rel))
        col, row = index % 4, index // 4
        x = ox + col * (icon + gap_x)
        y = oy + row * (icon + gap_y)
        # 图标底衬，让透明像素多的图标也能被看见。
        canvas.fill_rect(x - 2, y - 2, icon + 4, icon + 4, (48, 58, 72), 200)
        canvas.draw_icon(x, y, pixels, w if w == size else size, scale)

    write_png(OUTPUT, WIDTH, HEIGHT, canvas.px)
    print("wrote %s (%dx%d)" % (OUTPUT, WIDTH, HEIGHT))


if __name__ == "__main__":
    main()
