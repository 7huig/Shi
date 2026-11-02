# -*- coding: utf-8 -*-
"""時 · Shi — 「墨時」launcher icon 母版渲染器（唯一方案）。

母版即 Android 自适应图标的 108 dp 画布：512 px ↔ 108 dp（1 dp = 512/108 px）。
「時」用 Noto Serif SC SemiBold（SIL OFL-1.1）渲染，墨迹高 38 dp、按墨迹外框居中。
实测墨迹宽高比 0.9859（字体本身接近方形，并非早期估计的 1.26），38 dp 高时四角到画布中心
≈ 26.68 dp，落在 66 dp 安全圆（半径 33 dp）内并留 ≈ 6.32 dp 余量；即使按「半径 31.5 dp」
的检查线也还有 ≈ 4.82 dp。因此 launcher 的圆形/圆角遮罩不会切到任何笔画。

用法（仓库根目录）：
    python design/icons/render.py

产物：
    design/icons/icon-A.png        512×512  松绿底 + 纸白「時」（= 108 dp 画布母版）
    design/icons/icon-A-round.png  512×512  72 dp 圆形遮罩预览（圆外透明，即 launcher 实际所见）

启动器前景 PNG 由 build_launcher_assets.py 直接从这个模块的几何与字形渲染函数生成。
"""

import math
import os

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))

# ---------------------------------------------------------------- 自适应图标几何（dp）
CANVAS_DP = 108.0       # 自适应图标前景画布
VISIBLE_DP = 72.0       # launcher 遮罩内可见区域直径
SAFE_DP = 66.0          # 关键内容安全区圆直径（官方建议）
GLYPH_DP = 38.0         # 「時」墨迹高度
MARGIN_DP = 1.5         # 四角至少离安全圆 1.5 dp（检查线半径 = SAFE_DP/2 - MARGIN_DP）
MASTER_PX = 512         # 母版像素尺寸：512 px = 108 dp
SUPERSAMPLE = 4         # 母版超采样倍数

PAPER = (0xFB, 0xFA, 0xF7)
PINE = (0x2F, 0x5D, 0x50)
GLYPH = "時"

# 字体全部使用 SIL Open Font License 1.1 的 Noto（思源）系列，可自由使用与再分发；
# 字体不随仓库分发，缺失时回退到其它常见系统字体。
#   Noto Serif SC — https://github.com/notofonts/noto-cjk (OFL-1.1)
#   Noto Sans SC  — https://github.com/notofonts/noto-cjk (OFL-1.1)
SERIF_CANDIDATES = (
    r"C:/Windows/Fonts/NotoSerifSC-VF.ttf",
    "/System/Library/Fonts/NotoSerifSC-VF.ttf",
    "/usr/share/fonts/opentype/noto/NotoSerifCJK-Regular.ttc",
)
SANS_CANDIDATES = (
    r"C:/Windows/Fonts/NotoSansSC-VF.ttf",
    "/System/Library/Fonts/NotoSansSC-VF.ttf",
    "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
)


def _pick(candidates, label):
    for path in candidates:
        if os.path.exists(path):
            return path
    raise SystemExit("找不到可用字体 %s，请安装 Noto CJK：" % label + " | ".join(candidates))


SERIF = _pick(SERIF_CANDIDATES, "Noto Serif SC")
SANS = _pick(SANS_CANDIDATES, "Noto Sans SC")

_font_cache = {}


def load_font(path, px):
    """加载字体；可变字体按用途取字重（衬线 = SemiBold，无衬线 = Regular）。"""
    key = (path, max(1, int(round(px))))
    font = _font_cache.get(key)
    if font is not None:
        return font
    font = ImageFont.truetype(path, key[1])
    try:
        font.set_variation_by_name("SemiBold" if path == SERIF else "Regular")
    except Exception:
        pass
    _font_cache[key] = font
    return font


def glyph_mask(em_px):
    """把「時」渲染成紧贴墨迹外框的 L 蒙版（超大画布，避免笔画被画布裁掉）。"""
    font = load_font(SERIF, em_px)
    side = int(round(em_px * 2.4)) + 8
    tmp = Image.new("L", (side, side), 0)
    ImageDraw.Draw(tmp).text((side / 2.0, side / 2.0), GLYPH, font=font, fill=255, anchor="mm")
    bbox = tmp.getbbox()
    if bbox is None:  # pragma: no cover - 字体损坏时才会发生
        raise SystemExit("字体渲染不出「時」：" + SERIF)
    return tmp.crop(bbox)


_ratio = None


def ink_ratio():
    """墨迹宽高比（width / height），首次调用时以 1000 px 渲染测得。"""
    global _ratio
    if _ratio is None:
        mask = glyph_mask(1000)
        _ratio = mask.size[0] / mask.size[1]
    return _ratio


def glyph_at_ink_height(ink_px):
    """返回墨迹高度约为 ink_px 的字形蒙版（L）。"""
    em = 1000.0
    for _ in range(3):
        mask = glyph_mask(em)
        em *= ink_px / mask.size[1]
    return glyph_mask(em)


def glyph_layer(size_px, glyph_h_px, color=PAPER):
    """透明 size_px 画布 + 居中的单色字形（RGBA），返回 (图层, 字形像素尺寸)。"""
    mask = glyph_at_ink_height(glyph_h_px)
    layer = Image.new("RGBA", (size_px, size_px), (0, 0, 0, 0))
    fill = Image.new("RGBA", mask.size, tuple(color) + (255,))
    layer.paste(fill, ((size_px - mask.size[0]) // 2, (size_px - mask.size[1]) // 2), mask)
    return layer, mask.size


def metrics():
    """墨迹与安全区的解析几何（单位 dp）。"""
    ratio = ink_ratio()
    width_dp = GLYPH_DP * ratio
    diagonal_dp = math.hypot(GLYPH_DP, width_dp)
    corner_dp = diagonal_dp / 2.0
    safe_radius_dp = SAFE_DP / 2.0
    limit_dp = safe_radius_dp - MARGIN_DP
    return {
        "ratio": ratio,
        "glyph_dp": GLYPH_DP,
        "width_dp": width_dp,
        "diagonal_dp": diagonal_dp,
        "corner_dp": corner_dp,
        "safe_radius_dp": safe_radius_dp,
        "limit_dp": limit_dp,
        "margin_safe_dp": safe_radius_dp - corner_dp,
        "margin_limit_dp": limit_dp - corner_dp,
        "ok": corner_dp <= limit_dp,
    }


def check(verbose=True):
    """程序化校验：四角到中心 ≤ 33 dp - 1.5 dp = 31.5 dp，不满足直接失败。"""
    m = metrics()
    if not m["ok"]:
        raise SystemExit(
            "「時」超出安全区：四角距中心 %.2f dp > 允许 %.2f dp"
            % (m["corner_dp"], m["limit_dp"])
        )
    if verbose:
        print(
            "glyph 時: %.2f x %.2f dp (w/h %.4f), diagonal %.2f dp, corner radius %.2f dp"
            % (m["width_dp"], m["glyph_dp"], m["ratio"], m["diagonal_dp"], m["corner_dp"])
        )
        print(
            "safe circle %.0f dp (r %.1f): margin %.2f dp; margin over %.2f dp check line %.2f dp  OK"
            % (SAFE_DP, m["safe_radius_dp"], m["margin_safe_dp"], m["limit_dp"], m["margin_limit_dp"])
        )
    return m


def _hi_master():
    """2048 px 超采样母版（RGB，松绿底 + 纸白「時」）。"""
    hi = MASTER_PX * SUPERSAMPLE
    image = Image.new("RGB", (hi, hi), PINE)
    layer, _ = glyph_layer(hi, GLYPH_DP * hi / CANVAS_DP)
    image.paste(layer, (0, 0), layer)
    return image


def make_master():
    """512 px 母版 = 108 dp 画布。"""
    return _hi_master().resize((MASTER_PX, MASTER_PX), Image.LANCZOS)


def make_round():
    """512 px 圆形预览：按 72 dp 可见区裁圆，圆外透明。"""
    hi = MASTER_PX * SUPERSAMPLE
    image = _hi_master().convert("RGBA")
    mask = Image.new("L", (hi, hi), 0)
    radius = VISIBLE_DP / CANVAS_DP * hi / 2.0
    center = hi / 2.0
    ImageDraw.Draw(mask).ellipse(
        [center - radius, center - radius, center + radius, center + radius], fill=255
    )
    image.putalpha(mask)
    return image.resize((MASTER_PX, MASTER_PX), Image.LANCZOS)


# ---------------------------------------------------------------- 像素级测量（供校验/报告）
def _finish_measure(mask_fn, w, h):
    """遍历像素求墨迹外框与到画布中心的最大距离（px）与 dp 值。"""
    cx, cy = w / 2.0, h / 2.0
    minx, miny, maxx, maxy = w, h, -1, -1
    r2max = -1.0
    for y in range(h):
        for x in range(w):
            if not mask_fn(x, y):
                continue
            if x < minx:
                minx = x
            if y < miny:
                miny = y
            if x > maxx:
                maxx = x
            if y > maxy:
                maxy = y
            dx = x + 0.5 - cx
            dy = y + 0.5 - cy
            r2 = dx * dx + dy * dy
            if r2 > r2max:
                r2max = r2
    scale = CANVAS_DP / w
    corner = 0.0
    if maxx >= 0:
        for px in (minx, maxx):
            for py in (miny, maxy):
                corner = max(corner, math.hypot(px + 0.5 - cx, py + 0.5 - cy))
    return {
        "w_dp": (maxx - minx + 1) * scale if maxx >= 0 else 0.0,
        "h_dp": (maxy - miny + 1) * scale if maxy >= 0 else 0.0,
        "corner_dp": corner * scale,
        "ink_radius_dp": math.sqrt(r2max) * scale if r2max >= 0 else 0.0,
    }


def measure_rgba(im, alpha_min=8):
    """测量自适应图标前景（RGBA）里的字形墨迹。"""
    w, h = im.size
    alpha = im.getchannel("A").load()
    return _finish_measure(lambda x, y: alpha[x, y] >= alpha_min, w, h)


def measure_on_pine(im, diff_min=12):
    """测量松绿底母版里的纸白墨迹（用于母版像素级复核）。"""
    w, h = im.size
    px = im.convert("RGB").load()
    pr, pg, pb = PINE

    def is_ink(x, y):
        r, g, b = px[x, y]
        return abs(r - pr) + abs(g - pg) + abs(b - pb) > diff_min

    return _finish_measure(is_ink, w, h)


def main():
    m = check()
    master = make_master()
    master_path = os.path.join(HERE, "icon-A.png")
    master.save(master_path)
    round_path = os.path.join(HERE, "icon-A-round.png")
    make_round().save(round_path)

    raster = measure_on_pine(master)
    print(
        "master 512 px = 108 dp: ink %.2f x %.2f dp, corner radius %.2f dp, max ink radius %.2f dp"
        % (raster["w_dp"], raster["h_dp"], raster["corner_dp"], raster["ink_radius_dp"])
    )
    print("wrote design/icons/icon-A.png, design/icons/icon-A-round.png")
    return m


if __name__ == "__main__":
    main()
