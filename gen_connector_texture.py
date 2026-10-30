"""Generate the wireless connector icon (16x16), styled after RS2's own wrench.

斜向开口扳手：头部在左上（开口朝左上），手柄伸向右下，青蓝色系 + 深色描边。
Pure standard library so it runs anywhere.
"""
import math
import os
import struct
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(
    HERE, "refinednetwork-common", "src", "main", "resources",
    "assets", "refinednetwork", "textures", "item", "wireless_connector.png"
)

W = H = 16
CLEAR = (0, 0, 0, 0)


def rgb(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


OUTLINE = rgb("#2C2C2C")
STEEL_L = rgb("#93F0FF")
STEEL = rgb("#38BBDC")
STEEL_D = rgb("#1F7E96")


def write_png(path, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        crc = zlib.crc32(tag + data) & 0xFFFFFFFF
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", crc)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def seg_dist(px, py, ax, ay, bx, by):
    vx, vy = bx - ax, by - ay
    wx, wy = px - ax, py - ay
    if vx == 0 and vy == 0:
        return math.hypot(wx, wy)
    t = max(0.0, min(1.0, (wx * vx + wy * vy) / (vx * vx + vy * vy)))
    return math.hypot(px - (ax + t * vx), py - (ay + t * vy))


def build():
    px = [[CLEAR] * W for _ in range(H)]
    head_cx, head_cy = 4.8, 4.8
    handle_a = (6.5, 6.5)
    handle_b = (12.0, 12.0)

    solid = [[False] * W for _ in range(H)]
    for y in range(H):
        for x in range(W):
            cx, cy = x + 0.5, y + 0.5
            d = math.hypot(cx - head_cx, cy - head_cy)
            # 头部：C 形环，开口朝左上那一块挖掉
            in_head = 2.3 <= d <= 3.6 and not (cx <= head_cx and cy <= head_cy)
            in_handle = seg_dist(cx, cy, *handle_a, *handle_b) <= 1.05
            solid[y][x] = in_head or in_handle

    # 着色：左上受光、右下背光
    for y in range(H):
        for x in range(W):
            if not solid[y][x]:
                continue
            shade = (x + 0.5 - head_cx) + (y + 0.5 - head_cy)
            if shade < -1.5:
                px[y][x] = STEEL_L
            elif shade > 2.0:
                px[y][x] = STEEL_D
            else:
                px[y][x] = STEEL

    # 描边：与实心像素相邻的透明像素
    for y in range(H):
        for x in range(W):
            if solid[y][x]:
                continue
            if any(
                0 <= x + dx < W and 0 <= y + dy < H and solid[y + dy][x + dx]
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
            ):
                px[y][x] = OUTLINE

    return px


if __name__ == "__main__":
    write_png(OUT, build())
    print("wrote", OUT)
