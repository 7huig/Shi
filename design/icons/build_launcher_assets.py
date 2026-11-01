"""从 design/icons/icon-A.png 生成 Android 自适应图标前景（透明背景 + 纸白「時」）。

用法（仓库根目录）：
    python design/icons/build_launcher_assets.py

前景画布为 108dp（自适应图标标准），字形高度按母版比例取 43dp（约可见圆形直径 72dp 的 59%），
背景由 res/values/colors.xml 的 ic_launcher_background (#2F5D50) 提供。
"""

import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "design", "icons", "icon-A.png")
RES = os.path.join(ROOT, "app", "src", "main", "res")

PINE = (0x2F, 0x5D, 0x50)
PAPER = (0xFB, 0xFA, 0xF7)
GLYPH_DP = 43.0       # 字形高度（dp）
CANVAS_DP = 108.0     # 自适应图标前景画布
SUPERSAMPLE = 8       # 母版渲染倍数

DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}


def glyph_layer() -> Image.Image:
    im = Image.open(SRC).convert("RGB")
    w, h = im.size
    px = im.load()
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    op = out.load()
    pine_lum = 0.2126 * PINE[0] + 0.7152 * PINE[1] + 0.0722 * PINE[2]
    paper_lum = 0.2126 * PAPER[0] + 0.7152 * PAPER[1] + 0.0722 * PAPER[2]
    for y in range(h):
        for x in range(w):
            r, g, b = px[x, y]
            lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
            alpha = (lum - pine_lum) / (paper_lum - pine_lum)
            alpha = max(0.0, min(1.0, alpha))
            if alpha > 0.0:
                op[x, y] = PAPER + (int(round(alpha * 255)),)
    return out


def main() -> None:
    glyph = glyph_layer()
    bbox = glyph.getbbox()
    glyph = glyph.crop(bbox)
    gw, gh = glyph.size

    canvas_px = int(round(CANVAS_DP * SUPERSAMPLE))
    glyph_h_px = int(round(GLYPH_DP * SUPERSAMPLE))
    glyph_w_px = int(round(glyph_h_px * gw / gh))
    master = glyph.resize((glyph_w_px, glyph_h_px), Image.LANCZOS)

    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    canvas.paste(master, ((canvas_px - glyph_w_px) // 2, (canvas_px - glyph_h_px) // 2), master)

    for name, scale in DENSITIES.items():
        size = int(round(CANVAS_DP * scale))
        out_dir = os.path.join(RES, "drawable-" + name)
        os.makedirs(out_dir, exist_ok=True)
        img = canvas.resize((size, size), Image.LANCZOS)
        img.save(os.path.join(out_dir, "ic_launcher_foreground.png"))
        print("wrote", os.path.join("drawable-" + name, "ic_launcher_foreground.png"), size)

    # 预览：108dp 画布 + 72dp 圆形遮罩，浅/深两种桌面底色
    preview = Image.new("RGB", (760, 320), (0xE9, 0xE7, 0xE2))
    draw = ImageDraw.Draw(preview)
    for i, bg in enumerate([(0xE9, 0xE7, 0xE2), (0x17, 0x1A, 0x18)]):
        block = Image.new("RGB", (380, 320), bg)
        icon_bg = Image.new("RGBA", (288, 288), PINE + (255,))
        icon = Image.alpha_composite(icon_bg, canvas.resize((288, 288), Image.LANCZOS))
        mask = Image.new("L", (288, 288), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, 287, 287), fill=255)
        block.paste(icon, (46, 16), mask)
        preview.paste(block, (i * 380, 0))
    preview.save(os.path.join(ROOT, ".tools", "icon-preview.png"))
    print("wrote .tools/icon-preview.png")


if __name__ == "__main__":
    main()
