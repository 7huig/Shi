# -*- coding: utf-8 -*-
"""生成 Android 自适应图标前景 PNG，以及目检预览 .tools/icon-preview.png。

用法（仓库根目录）：
    python design/icons/build_launcher_assets.py

前景：108 dp 透明画布 + 纸白「時」，几何完全复用 render.py（与 icon-A.png 母版同源，
字形墨迹高 38 dp），输出 5 档 density 到 app/src/main/res/drawable-<density>/。
背景：@color/ic_launcher_background (#2F5D50) 由 values/colors.xml 提供，mipmap-anydpi-v26 不变。

预览：1400x1010 逻辑画布（2x 采样后缩小，圆环/文字均有抗锯齿）——
  * 浅色桌面 #E9E7E2 与深色桌面 #171A18 上的 72 dp 圆形遮罩效果
  * 72 dp 遮罩圈、66 dp 安全圈、字形墨迹外框与四角
  * 96 / 64 / 48 px 实际尺寸，以及 96 px 三倍最近邻放大（检查小尺寸清晰度）
"""

import math
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
if HERE not in sys.path:
    sys.path.insert(0, HERE)

from PIL import Image, ImageDraw, ImageFont

import render as R

ROOT = os.path.dirname(os.path.dirname(HERE))
RES = os.path.join(ROOT, "app", "src", "main", "res")

DENSITIES = (("mdpi", 1.0), ("hdpi", 1.5), ("xhdpi", 2.0), ("xxhdpi", 3.0), ("xxxhdpi", 4.0))
FOREGROUND_SS = 8  # 前景超采样倍数（108 dp -> 864 px 再缩放）

PAGE_BG = (0xFB, 0xFA, 0xF7)
LIGHT_DESK = (0xE9, 0xE7, 0xE2)
DARK_DESK = (0x17, 0x1A, 0x18)
INK = (0x24, 0x29, 0x25)
INK_SOFT = (0x5C, 0x63, 0x5D)
SAFE_RING = (0xD2, 0x8B, 0x24)
BBOX_LINE = (0x2E, 0x8F, 0xB0)
RING_LIGHT = (0x84, 0x91, 0x88)
RING_DARK = (0xD8, 0xE0, 0xDB)
RING_ON_PINE = (0xE8, 0xEE, 0xE9)
BORDER_LIGHT = (0xD8, 0xD4, 0xCB)
BORDER_DARK = (0x2C, 0x33, 0x2E)

_HI_MASTER = None


def hi_master():
    """2048 px 超采样母版（108 dp 画布，松绿底 + 纸白「時」）。"""
    global _HI_MASTER
    if _HI_MASTER is None:
        _HI_MASTER = R._hi_master()
    return _HI_MASTER


def circular_icon(size_px):
    """按 72 dp 可见圆裁切母版，得到 launcher 实际所见（RGBA）。"""
    hi = hi_master().convert("RGBA")
    side = hi.size[0]
    mask = Image.new("L", (side, side), 0)
    radius = R.VISIBLE_DP / R.CANVAS_DP * side / 2.0
    center = side / 2.0
    ImageDraw.Draw(mask).ellipse(
        [center - radius, center - radius, center + radius, center + radius], fill=255
    )
    hi.putalpha(mask)
    return hi.resize((size_px, size_px), Image.LANCZOS)


def foreground(size_px):
    """108 dp 前景画布（透明底 + 纸白字形），按目标像素直接超采样渲染。"""
    hi_px = int(round(size_px * FOREGROUND_SS))
    layer, _ = R.glyph_layer(hi_px, R.GLYPH_DP * hi_px / R.CANVAS_DP)
    return layer.resize((size_px, size_px), Image.LANCZOS)


# ---------------------------------------------------------------- 预览绘制小工具
SC = 2  # 预览超采样倍数


def S(v):
    return int(round(v * SC))


def _font(size, bold=False):
    names = ("segoeuib.ttf", "segoeui.ttf") if bold else ("segoeui.ttf", "segoeuib.ttf")
    roots = (r"C:/Windows/Fonts", "/System/Library/Fonts", "/usr/share/fonts/truetype/dejavu")
    for root in roots:
        for name in names:
            path = os.path.join(root, name)
            if os.path.exists(path):
                return ImageFont.truetype(path, S(size))
    return ImageFont.load_default()


def text(draw, xy, txt, size, fill, bold=False, anchor="la"):
    draw.text((S(xy[0]), S(xy[1])), txt, font=_font(size, bold), fill=fill, anchor=anchor)


def dashed_ring(draw, cx, cy, radius, color, width=2, dash=9.0, gap=7.0):
    circumference = 2.0 * math.pi * radius
    count = max(12, int(round(circumference / (dash + gap))))
    step = 360.0 / count
    frac = dash / (dash + gap)
    box = [S(cx - radius), S(cy - radius), S(cx + radius), S(cy + radius)]
    for i in range(count):
        start = i * step
        draw.arc(box, start=start, end=start + step * frac, fill=color, width=S(width))


def dashed_rect(draw, box, color, width=2, dash=10.0, gap=7.0):
    x0, y0, x1, y1 = box
    for x in _dash_positions(x0, x1):
        draw.line([S(x), S(y0), S(min(x + dash, x1)), S(y0)], fill=color, width=S(width))
        draw.line([S(x), S(y1), S(min(x + dash, x1)), S(y1)], fill=color, width=S(width))
    for y in _dash_positions(y0, y1):
        draw.line([S(x0), S(y), S(x0), S(min(y + dash, y1))], fill=color, width=S(width))
        draw.line([S(x1), S(y), S(x1), S(min(y + dash, y1))], fill=color, width=S(width))


def _dash_positions(a, b):
    out = []
    v = a
    while v < b:
        out.append(v)
        v += 17.0
    return out


def panel(draw, box, fill, border):
    draw.rounded_rectangle(
        [S(box[0]), S(box[1]), S(box[2]), S(box[3])], radius=S(14), fill=fill, outline=border, width=S(1)
    )


def draw_on_desktop(image, draw, x, y, size_px, desk_dark):
    """在桌面底色块中绘制 72 dp 圆形图标 + 72 dp 遮罩圈 + 66 dp 安全圈。"""
    icon = circular_icon(S(size_px))
    image.paste(icon, (S(x), S(y)), icon)
    cx, cy = x + size_px / 2.0, y + size_px / 2.0
    r_mask = size_px / 2.0
    r_safe = size_px / 2.0 * R.SAFE_DP / R.VISIBLE_DP
    dashed_ring(draw, cx, cy, r_mask, RING_DARK if desk_dark else RING_LIGHT, width=1.5, dash=8, gap=6)
    dashed_ring(draw, cx, cy, r_safe, SAFE_RING, width=2.0, dash=8, gap=6)


def build_preview(path):
    """生成目检用 .tools/icon-preview.png（浅/深桌面、遮罩圈、安全圈、实尺寸）。"""
    W, H = 1400, 1080
    image = Image.new("RGB", (W * SC, H * SC), PAGE_BG)
    draw = ImageDraw.Draw(image)
    m = R.metrics()

    # ---- 页眉（中文用 Noto Serif SC，不能用 Segoe UI，否则「時」是豆腐块）
    title_font = R.load_font(R.SERIF, S(38))
    draw.text((S(40), S(34)), "時 · Shi — launcher icon", font=title_font, fill=INK, anchor="la")
    text(
        draw,
        (42, 100),
        "single scheme “Mo Shi”   ·   canvas 108 dp   ·   visible 72 dp   ·   safe 66 dp   ·   glyph 38 dp",
        19,
        INK_SOFT,
    )
    draw.line([S(40), S(140), S(W - 40), S(140)], fill=(0xDD, 0xD8, 0xCE), width=S(1))

    # ---- 三个面板：浅桌面 / 深桌面 / 108 dp 画布规格
    panel_y, panel_h = 164, 480
    panel(draw, (40, panel_y, 480, panel_y + panel_h), LIGHT_DESK, BORDER_LIGHT)
    panel(draw, (500, panel_y, 940, panel_y + panel_h), DARK_DESK, BORDER_DARK)
    panel(draw, (960, panel_y, 1360, panel_y + panel_h), PAGE_BG, BORDER_LIGHT)

    icon_px = 288
    draw_on_desktop(image, draw, 40 + (440 - icon_px) / 2.0, panel_y + 26, icon_px, False)
    draw_on_desktop(image, draw, 500 + (440 - icon_px) / 2.0, panel_y + 26, icon_px, True)

    text(draw, (56, panel_y + 330), "light desktop  #E9E7E2", 16, INK, bold=True)
    text(draw, (56, panel_y + 356), "amber dashed = 66 dp safe circle", 14, INK_SOFT)
    text(draw, (56, panel_y + 380), "grey dashed = 72 dp visible mask", 14, INK_SOFT)
    text(draw, (56, panel_y + 418), "72 dp circle mask, 1:1 proportions", 14, INK_SOFT)

    text(draw, (516, panel_y + 330), "dark desktop  #171A18", 16, (0xDD, 0xE3, 0xDF), bold=True)
    text(draw, (516, panel_y + 356), "amber dashed = 66 dp safe circle", 14, (0xB9, 0xC4, 0xBE))
    text(draw, (516, panel_y + 380), "white dashed = 72 dp visible mask", 14, (0xB9, 0xC4, 0xBE))
    text(draw, (516, panel_y + 418), "72 dp circle mask, 1:1 proportions", 14, (0xB9, 0xC4, 0xBE))

    # 规格面板：108 dp 画布（3x）+ 遮罩圈 + 安全圈 + 墨迹外框
    side_px = 108.0 * 3.0
    cx, cy = 960 + 220.0, panel_y + 26 + side_px / 2.0
    canvas = hi_master().resize((S(side_px), S(side_px)), Image.LANCZOS)
    image.paste(canvas, (S(cx - side_px / 2.0), S(cy - side_px / 2.0)))
    draw.rectangle(
        [S(cx - side_px / 2.0), S(cy - side_px / 2.0), S(cx + side_px / 2.0), S(cy + side_px / 2.0)],
        outline=(0x6E, 0x83, 0x78),
        width=S(1),
    )
    dashed_ring(draw, cx, cy, side_px / 2.0 * 72.0 / 108.0, RING_ON_PINE, width=1.5, dash=7, gap=5)
    dashed_ring(draw, cx, cy, side_px / 2.0 * 66.0 / 108.0, SAFE_RING, width=2.0, dash=7, gap=5)
    gw = m["width_dp"] * 3.0
    gh = m["glyph_dp"] * 3.0
    dashed_rect(draw, (cx - gw / 2.0, cy - gh / 2.0, cx + gw / 2.0, cy + gh / 2.0), BBOX_LINE, width=1.5, dash=9, gap=6)
    for bx, by in (
        (cx - gw / 2, cy - gh / 2),
        (cx + gw / 2, cy - gh / 2),
        (cx - gw / 2, cy + gh / 2),
        (cx + gw / 2, cy + gh / 2),
    ):
        draw.ellipse([S(bx - 3), S(by - 3), S(bx + 3), S(by + 3)], fill=BBOX_LINE)
    text(draw, (976, panel_y + 344), "108 dp canvas (3x), full bleed", 14, INK_SOFT)
    text(draw, (976, panel_y + 368), "cyan = glyph ink box %.1f x %.1f dp" % (m["width_dp"], m["glyph_dp"]), 14, INK_SOFT)
    text(draw, (976, panel_y + 392), "amber = 66 dp safe circle", 14, INK_SOFT)
    text(draw, (976, panel_y + 416), "white dashed = 72 dp visible mask", 14, INK_SOFT)

    # ---- 底部：实际尺寸 + 清晰度检查 + 数值
    text(draw, (40, 676), "actual size (1x)", 22, INK, bold=True)

    cards = ((40, 96), (188, 64), (336, 48))
    for x, size in cards:
        panel(draw, (x, 716, x + 128, 896), (0xF1, 0xEF, 0xE9), BORDER_LIGHT)
        icon = circular_icon(S(size))
        image.paste(icon, (S(x + (128 - size) / 2.0), S(716 + 30)), icon)
        text(draw, (x + 64, 858), "%d px" % size, 15, INK_SOFT, anchor="mm")

    zoom_px = 96 * 2
    panel(draw, (520, 716, 520 + zoom_px + 28, 716 + zoom_px + 60), (0xF1, 0xEF, 0xE9), BORDER_LIGHT)
    big = circular_icon(96).resize((S(zoom_px), S(zoom_px)), Image.NEAREST)
    image.paste(big, (S(534), S(730)), big)
    text(draw, (534, 730 + zoom_px + 12), "96 px @2x nearest (pixel check)", 14, INK_SOFT)

    x0, y0 = 800, 672
    text(draw, (x0, y0), "measured geometry", 22, INK, bold=True)
    lines = (
        "glyph ink       %.2f x %.2f dp   (w/h %.4f)" % (m["width_dp"], m["glyph_dp"], m["ratio"]),
        "corners         %.2f dp from canvas centre" % m["corner_dp"],
        "safe circle     r 33.00 dp  ->  margin %.2f dp" % m["margin_safe_dp"],
        "check line      r %.2f dp  ->  margin %.2f dp   PASS" % (m["limit_dp"], m["margin_limit_dp"]),
        "max ink radius  25.25 dp (512 px master)",
        "foreground px   108 / 162 / 216 / 324 / 432",
        "background      #2F5D50 (colors.xml)",
        "typeface        Noto Serif SC SemiBold, OFL-1.1",
    )
    for i, line in enumerate(lines):
        text(draw, (x0, y0 + 40 + i * 30), line, 16, INK_SOFT)

    image.resize((W, H), Image.LANCZOS).save(path)
    return path


def main():
    R.check(verbose=True)

    for density, scale in DENSITIES:
        size = int(round(R.CANVAS_DP * scale))
        img = foreground(size)
        out_dir = os.path.join(RES, "drawable-" + density)
        os.makedirs(out_dir, exist_ok=True)
        out = os.path.join(out_dir, "ic_launcher_foreground.png")
        img.save(out)
        m = R.measure_rgba(img)
        print(
            "wrote drawable-%-9s ic_launcher_foreground.png  %3d px  ink %.2f x %.2f dp  corner %.2f dp  max r %.2f dp"
            % (density, size, m["w_dp"], m["h_dp"], m["corner_dp"], m["ink_radius_dp"])
        )

    preview = build_preview(os.path.join(ROOT, ".tools", "icon-preview.png"))
    print("wrote .tools/icon-preview.png")


if __name__ == "__main__":
    main()
