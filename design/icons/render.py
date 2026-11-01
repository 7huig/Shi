
# -*- coding: utf-8 -*-
"""時計 launcher icon concepts - flat vector, 4x supersampled, LANCZOS downscale."""
import os, math
from PIL import Image, ImageDraw, ImageFont

OUT = os.path.dirname(os.path.abspath(__file__))
W, S = 512, 4
C = W * S

PAPER     = (251, 250, 247)
INK       = (30, 33, 31)
PINE      = (47, 93, 80)
PINE_SOFT = (230, 239, 234)
CLAY      = (180, 101, 74)
HAIR      = (228, 225, 218)
OCHRE     = (192, 122, 43)
MUTED     = (108, 110, 106)

SERIF = r"C:/Windows/Fonts/yumindb.ttf"
SANSB = r"C:/Windows/Fonts/msyhbd.ttc"
SANS  = r"C:/Windows/Fonts/msyh.ttc"

def P(*v):
    return [float(x) * S for x in v]

def new(bg=PAPER):
    return Image.new("RGB", (C, C), bg)

def glyph_mask(ch, em_px, path):
    f = ImageFont.truetype(path, int(round(em_px)))
    tmp = Image.new("L", (int(em_px * 2) + 80, int(em_px * 2) + 80), 0)
    ImageDraw.Draw(tmp).text((em_px, em_px), ch, font=f, fill=255)
    bb = tmp.getbbox()
    return tmp.crop(bb)

def glyph_ink_target(ch, target_h_512, path):
    m = glyph_mask(ch, 1000, path)
    return 1000.0 * target_h_512 * S / m.size[1]

def draw_glyph(im, ch, cx, cy, target_h_512, fill, path=SERIF):
    em = glyph_ink_target(ch, target_h_512, path)
    m = glyph_mask(ch, em, path)
    w, h = m.size
    x = int(round(cx * S - w / 2.0))
    y = int(round(cy * S - h / 2.0))
    im.paste(Image.new("RGB", (w, h), fill), (x, y), m)
    return w / S, h / S

def rline(d, p1, p2, fill, w):
    d.line([p1, p2], fill=fill, width=int(round(w * S)))
    r = w * S / 2.0
    for x, y in (p1, p2):
        d.ellipse([x - r, y - r, x + r, y + r], fill=fill)

def vline(d, p1, p2, fill, w):
    rline(d, P(*p1), P(*p2), fill, w)

def circle(d, cx, cy, r, fill=None, outline=None, width=0):
    d.ellipse(P(cx - r, cy - r, cx + r, cy + r), fill=fill, outline=outline,
              width=int(round(width * S)))

def rrect(d, x0, y0, x1, y1, r, fill=None, outline=None, width=0):
    d.rounded_rectangle(P(x0, y0, x1, y1), radius=r * S, fill=fill,
                        outline=outline, width=int(round(width * S)))

# ---------------------------------------------------------------- A: 墨時 monogram
def make_A():
    im = new(PINE)
    draw_glyph(im, "時", 256, 256, 306, PAPER, SERIF)
    return im

# ---------------------------------------------------------------- B: minimal clock
def make_B():
    im = new(PAPER)
    d = ImageDraw.Draw(im)
    cx = cy = 256.0
    circle(d, cx, cy, 142, outline=INK, width=12)
    a2 = math.radians(118)                      # clay second hand, with tail
    vline(d, (cx - 30 * math.cos(a2), cy - 30 * math.sin(a2)),
             (cx + 131 * math.cos(a2), cy + 131 * math.sin(a2)), CLAY, 4.5)
    a = math.radians(-150)                      # hour hand -> 10 o'clock
    vline(d, (cx, cy), (cx + 75 * math.cos(a), cy + 75 * math.sin(a)), INK, 14)
    a = math.radians(-30)                       # minute hand -> 2 o'clock
    vline(d, (cx, cy), (cx + 113 * math.cos(a), cy + 113 * math.sin(a)), INK, 12)
    circle(d, cx, cy, 10.5, fill=INK)
    circle(d, cx, cy, 4.0, fill=CLAY)
    return im

# ---------------------------------------------------------------- C: timeline blocks
def make_C():
    im = new(PAPER)
    d = ImageDraw.Draw(im)
    bars = [(PINE, 268, 52), (CLAY, 196, 78), (OCHRE, 228, 50)]
    gap = 24.0
    total = sum(h for _, _, h in bars) + gap * (len(bars) - 1)
    y = 256 - total / 2.0
    left = 256 - 134.0
    for col, bw, bh in bars:
        rrect(d, left, y, left + bw, y + bh, bh / 2.0, fill=col)
        y += bh + gap
    return im

# ---------------------------------------------------------------- D: hanko seal
def make_D():
    im = new(PAPER)
    d = ImageDraw.Draw(im)
    half = 165.0
    rrect(d, 256 - half, 256 - half, 256 + half, 256 + half, 18, fill=CLAY)
    inset = 16.0
    rrect(d, 256 - half + inset, 256 - half + inset,
          256 + half - inset, 256 + half - inset, 9,
          outline=PAPER, width=3.2)
    draw_glyph(im, "時", 256, 256, 230, PAPER, SANSB)
    return im

# ---------------------------------------------------------------- E: sun-moon arc
def recenter(im, bg):
    from PIL import ImageChops
    bb = ImageChops.difference(im, Image.new("RGB", im.size, bg)).getbbox()
    dx = im.size[0] / 2.0 - (bb[0] + bb[2]) / 2.0
    dy = im.size[1] / 2.0 - (bb[1] + bb[3]) / 2.0
    return im.transform(im.size, Image.AFFINE, (1, 0, -dx, 0, 1, -dy),
                        resample=Image.BICUBIC, fillcolor=bg)

def make_E():
    im = new(PINE_SOFT)
    d = ImageDraw.Draw(im)
    cx, cy, r = 256.0, 318.0, 140.0
    d.arc(P(cx - r, cy - r, cx + r, cy + r), 180, 360, fill=INK,
          width=int(round(12 * S)))
    circle(d, cx - r, cy, 16, fill=INK)       # moon
    circle(d, cx + r, cy, 25, fill=CLAY)      # sun
    return recenter(im, PINE_SOFT)

MAKERS = {"A": make_A, "B": make_B, "C": make_C, "D": make_D, "E": make_E}
NAMES = [("A", "墨時", "「時」字徽"),
         ("B", "素時計", "极简表盘"),
         ("C", "時流", "时间块"),
         ("D", "朱印", "朱色印章"),
         ("E", "日月弧", "昼夜之弧")]

def hi_circle_mask():
    m = Image.new("L", (C, C), 0)
    ImageDraw.Draw(m).ellipse([0, 0, C - 1, C - 1], fill=255)
    return m

MASK_HI = hi_circle_mask()

def save_icons():
    for k, fn in MAKERS.items():
        im = fn()
        im.resize((W, W), Image.LANCZOS).save(os.path.join(OUT, "icon-%s.png" % k))
        r = im.convert("RGBA")
        r.putalpha(MASK_HI)
        r.resize((W, W), Image.LANCZOS).save(os.path.join(OUT, "icon-%s-round.png" % k))

def fnt(path, px):
    return ImageFont.truetype(path, int(round(px)))

def ctext(d, xy, txt, font, fill):
    bb = d.textbbox((0, 0), txt, font=font)
    d.text((xy[0] - (bb[2] - bb[0]) / 2.0 - bb[0],
            xy[1] - (bb[3] - bb[1]) / 2.0 - bb[1]), txt, font=font, fill=fill)

def rtext(d, xy, txt, font, fill):
    bb = d.textbbox((0, 0), txt, font=font)
    d.text((xy[0] - (bb[2] - bb[0]) - bb[0], xy[1] - bb[1]), txt, font=font, fill=fill)

def make_overview():
    SC = 2
    OW, OH = 1720, 860
    bg = (242, 239, 233)
    im = Image.new("RGB", (OW * SC, OH * SC), bg)
    d = ImageDraw.Draw(im)

    def T(v):
        return int(round(v * SC))

    f_title = fnt(SERIF, 46 * SC)
    f_sub   = fnt(SANS, 21 * SC)
    f_note  = fnt(SANS, 17 * SC)
    f_let   = fnt(SANSB, 40 * SC)
    f_name  = fnt(SERIF, 30 * SC)
    f_cap   = fnt(SANS, 21 * SC)

    # header
    bb = d.textbbox((0, 0), "時計", font=f_title)
    d.text((T(76), T(42) - bb[1]), "時計", font=f_title, fill=INK)
    tw = bb[2] - bb[0]
    d.text((T(76) + tw + T(20), T(66) - f_sub.getbbox("cg")[1]),
           "launcher icon concepts", font=f_sub, fill=MUTED)
    d.text((T(76), T(112)), "五个方向 · five directions", font=f_note, fill=MUTED)
    d.line([T(76), T(148), T(OW - 76), T(148)], fill=HAIR, width=T(2))

    # five icon columns
    cellw, cy, dia = 320.0, 330.0, 216.0
    x0 = (OW - cellw * 5) / 2.0
    icons = {k: Image.open(os.path.join(OUT, "icon-%s-round.png" % k)).convert("RGBA")
             for k in "ABCDE"}
    for i, (letter, nm, cap) in enumerate(NAMES):
        cx = x0 + cellw * i + cellw / 2.0
        ic = icons[letter].resize((T(dia), T(dia)), Image.LANCZOS)
        im.paste(ic, (T(cx - dia / 2.0), T(cy - dia / 2.0)), ic)
        d.ellipse([T(cx - dia / 2.0), T(cy - dia / 2.0),
                   T(cx + dia / 2.0), T(cy + dia / 2.0)],
                  outline=HAIR, width=T(2))
        ctext(d, (T(cx), T(cy + dia / 2.0 + 44)), letter, f_let, INK)
        ctext(d, (T(cx), T(cy + dia / 2.0 + 92)), nm, f_name, INK)
        ctext(d, (T(cx), T(cy + dia / 2.0 + 130)), cap, f_cap, MUTED)

    # 96 px band
    d.line([T(76), T(612), T(OW - 76), T(612)], fill=HAIR, width=T(2))
    ctext(d, (T(OW / 2.0), T(640)), "96 px  ·  手机主屏尺寸预览", f_note, MUTED)
    for i, (letter, _, _) in enumerate(NAMES):
        cx = x0 + cellw * i + cellw / 2.0
        ic = icons[letter].resize((T(96), T(96)), Image.LANCZOS)
        im.paste(ic, (T(cx - 48), T(662)), ic)

    ctext(d, (T(OW / 2.0), T(812)),
          "時計  ·  washi paper / ink / pine / clay  ·  512 px masters", f_note, MUTED)

    im.resize((OW, OH), Image.LANCZOS).save(os.path.join(OUT, "overview.png"))

def make_checks():
    for bgcol, tag in [((255, 255, 255), "light"), ((23, 26, 24), "dark")]:
        strip = Image.new("RGB", (5 * 120, 120), bgcol)
        for i, k in enumerate("ABCDE"):
            ic = Image.open(os.path.join(OUT, "icon-%s-round.png" % k)).convert("RGBA")
            ic = ic.resize((96, 96), Image.LANCZOS)
            strip.paste(ic, (i * 120 + 12, 12), ic)
        strip.save(os.path.join(OUT, "_check96-%s.png" % tag))

if __name__ == "__main__":
    save_icons()
    make_overview()
    make_checks()
    print("rendered")