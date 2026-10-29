#!/usr/bin/env python3
"""生成模组图标：与 Refined Storage 官方 logo 同一套语言。

  · 切角方形（45° chamfer），不用圆角
  · 左上偏蓝的青 → 右下偏绿的薄荷青，沿对角线渐变
  · 右侧一格按像素块拆散、向右拖出零星像素（RS 的 dissolve 手法）

输出
  assets/rswirelesscrafting/icon.png   512×512，Fabric / NeoForge 共用
  docs/images/mod-icon.png             同上（README 与 mcmod 用）
"""

import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT_MOD = os.path.join(
    ROOT, "rswirelesscrafting-common", "src", "main", "resources",
    "assets", "rswirelesscrafting", "icon.png",
)
OUT_DOC = os.path.join(ROOT, "docs", "images", "mod-icon.png")

SIZE = 512
S = 4  # 超采样

# 取自 RS 官方 logo-icon.png 的采样色
FROM = (6, 138, 196)
TO = (52, 242, 206)

CELL = 84
GAP = 14
X0 = 58
Y0 = 116
CUT = 11
PIX = 14  # 拆散用的小方格边长


def sc(v):
    return int(round(v * S))


def octagon(draw, x, y, size, cut=CUT, fill=255):
    pts = [
        (x + cut, y), (x + size - cut, y), (x + size, y + cut),
        (x + size, y + size - cut), (x + size - cut, y + size),
        (x + cut, y + size), (x, y + size - cut), (x, y + cut),
    ]
    draw.polygon([(sc(px), sc(py)) for px, py in pts], fill=fill)


def mask():
    m = Image.new("L", (sc(SIZE), sc(SIZE)), 0)
    d = ImageDraw.Draw(m)

    row_solid = (0.55, 0.80, 1.00)  # 越靠下越完整，整体向右上角散开
    steps = CELL // PIX
    for row, thr in enumerate(row_solid):
        y = Y0 + row * (CELL + GAP)
        for col in range(3):
            x = X0 + col * (CELL + GAP)
            if col < 2:
                octagon(d, x, y, CELL)
                continue
            # 第三列：沿对角线拆散，边缘干净、只留少量散点
            for iy in range(steps):
                for ix in range(steps):
                    u = ix / (steps - 1)
                    v = iy / (steps - 1)
                    strength = u * 0.9 + (1 - v) * 0.6
                    keep = min(max((thr - strength) * 5 + 0.5, 0.0), 1.0)
                    if random.Random(row * 1000 + iy * 100 + ix).random() < keep:
                        px = x + ix * PIX
                        py = y + iy * PIX
                        d.rectangle([sc(px), sc(py), sc(px + PIX), sc(py + PIX)], fill=255)
            # 向右拖出的零星像素
            for iy in range(steps):
                for k in range(1, 4):
                    if random.Random(7000 + row * 100 + iy * 10 + k).random() < 0.30 / k:
                        px = x + CELL + (k - 1) * PIX
                        py = y + iy * PIX
                        d.rectangle([sc(px), sc(py), sc(px + PIX), sc(py + PIX)], fill=255)
    return m


def gradient(w, h):
    img = Image.new("RGB", (w, h))
    px = img.load()
    for y in range(h):
        for x in range(0, w, 4):
            t = x / w * 0.5 + y / h * 0.5
            colour = tuple(int(round(FROM[i] + (TO[i] - FROM[i]) * t)) for i in range(3))
            for k in range(4):
                if x + k < w:
                    px[x + k, y] = colour
    return img


def build():
    canvas = Image.new("RGBA", (sc(SIZE), sc(SIZE)), (0, 0, 0, 0))
    fill = gradient(sc(SIZE), sc(SIZE)).convert("RGBA")
    fill.putalpha(mask())
    canvas.alpha_composite(fill)
    return canvas.resize((SIZE, SIZE), Image.LANCZOS)


def main():
    random.seed(20261029)
    icon = build()
    icon.save(OUT_MOD)
    print("wrote", OUT_MOD)
    icon.save(OUT_DOC)
    print("wrote", OUT_DOC)


if __name__ == "__main__":
    main()
