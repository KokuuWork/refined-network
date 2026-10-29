#!/usr/bin/env python3
"""生成模组图标：teal 渐变圆角块 + 无线格（3×3 缺一格）+ 信号弧。

输出
  assets/rswirelesscrafting/icon.png   512×512，Fabric / NeoForge 共用
  docs/images/mod-icon.png             同上（README 与 mcmod 用）
"""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT_MOD = os.path.join(
    ROOT, "rswirelesscrafting-common", "src", "main", "resources",
    "assets", "rswirelesscrafting", "icon.png",
)
OUT_DOC = os.path.join(ROOT, "docs", "images", "mod-icon.png")

SIZE = 512
S = 4  # 超采样

TOP = (79, 209, 224)
BOTTOM = (18, 116, 146)


def sc(v):
    return int(round(v * S))


def gradient(w, h, top, bottom):
    img = Image.new("RGB", (1, h))
    px = img.load()
    for y in range(h):
        t = y / max(h - 1, 1)
        px[0, y] = tuple(int(round(top[i] + (bottom[i] - top[i]) * t)) for i in range(3))
    return img.resize((w, h), Image.BILINEAR)


def build():
    canvas = Image.new("RGBA", (sc(SIZE), sc(SIZE)), (0, 0, 0, 0))
    mask = Image.new("L", (sc(SIZE), sc(SIZE)), 0)
    d = ImageDraw.Draw(mask)

    # 底板
    d.rounded_rectangle([sc(56), sc(56), sc(456), sc(456)], radius=sc(92), fill=255)

    # 挖出 3×3 格子，右上角那格留给信号弧
    cell = 72
    centers = (152, 256, 360)
    for cy in centers:
        for cx in centers:
            if cx == 360 and cy == 152:
                continue
            d.rounded_rectangle(
                [sc(cx - cell / 2), sc(cy - cell / 2), sc(cx + cell / 2), sc(cy + cell / 2)],
                radius=sc(19), fill=0)

    # 挖出两段信号弧（圆心落在右上那格的外角）
    ax, ay = 376, 172
    d.ellipse([sc(ax - 12), sc(ay - 12), sc(ax + 12), sc(ay + 12)], fill=0)
    for radius, width in ((32, 19), (60, 19)):
        d.arc([sc(ax - radius), sc(ay - radius), sc(ax + radius), sc(ay + radius)],
              start=-104, end=-14, fill=0, width=sc(width))

    fill = gradient(sc(SIZE), sc(SIZE), TOP, BOTTOM).convert("RGBA")
    fill.putalpha(mask)
    canvas.alpha_composite(fill)
    return canvas.resize((SIZE, SIZE), Image.LANCZOS)


def main():
    icon = build()
    icon.save(OUT_MOD)
    print("wrote", OUT_MOD)
    icon.save(OUT_DOC)
    print("wrote", OUT_DOC)
    for size in (16, 32, 64):
        icon.resize((size, size), Image.LANCZOS).save(
            os.path.join(ROOT, "_preview_%d.png" % size))


if __name__ == "__main__":
    main()
