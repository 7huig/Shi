# 時 · Shi — launcher icon（单一方案：墨時）

本目录只保留 **一个** 图标方案：**墨時**。松绿底 + 纸白「時」，不画时钟、不加渐变或阴影。
「時」即「时间」本身，把这个字做成图标，是应用最直接的自我介绍。

> 历史上曾有 A–E 五套概念稿（墨時 / 素時計 / 時流 / 朱印 / 日月弧）与 `overview.png` 拼版，
> 现已按「只保留一个方案」的要求删除，仅保留本方案与可复现脚本。

## 设计

| 项 | 值 |
|----|----|
| 字形 | 「時」，Noto Serif SC **SemiBold**（思源宋体，衬线体，横细竖粗，有书写感） |
| 纸白 | `#FBFAF7` |
| 松绿 | `#2F5D50`（与 `res/values/colors.xml` 的 `ic_launcher_background` 一致） |
| 排布 | 字形按**墨迹外框**（不是字身框）在 108 dp 画布上居中 |
| 风格 | 纯色平涂，无渐变、无投影、无描边 |

## 自适应图标几何

Android 自适应图标的前景是 108 × 108 dp 画布，launcher 只会露出中间 72 dp 的圆（或圆角方），
官方要求关键内容落在 **66 dp 安全圆**内。本方案的程序化校验结果：

| 量 | 数值 |
|----|------|
| 画布 | 108 dp |
| 可见遮罩 | 72 dp 圆形 |
| 安全圆 | 66 dp（半径 33 dp） |
| 字形墨迹高 | **38.00 dp** |
| 字形墨迹宽 | **37.46 dp**（实测宽高比 0.9859，字形本身接近方形） |
| 墨迹四角到画布中心 | **26.68 dp** |
| 四角距安全圆边缘 | **6.32 dp**（要求 ≥ 1.5 dp） |
| 距 31.5 dp 检查线 | **4.82 dp**（检查线 = 33 − 1.5 dp） |

`render.py` 在运行时执行 `check()`：若四角距离 > 31.5 dp 直接以非零状态退出，
因此「字形被遮罩切到」不会再悄悄发生。按此几何，字形高上限约 **44.8 dp**；
当前取 38 dp 是刻意留足余量（不同厂商 launcher 的圆角/圆形遮罩并不完全一致）。

前景资源（由 `build_launcher_assets.py` 直接由字体渲染，不经过母版提取）：

| density | 前景尺寸 | 字形墨迹高 |
|---------|----------|------------|
| mdpi (1×) | 108 px | 38 px |
| hdpi (1.5×) | 162 px | 57 px |
| xhdpi (2×) | 216 px | 76 px |
| xxhdpi (3×) | 324 px | 114 px |
| xxxhdpi (4×) | 432 px | 152 px |

输出位置：`app/src/main/res/drawable-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.png`。
背景不单独出图：`mipmap-anydpi-v26/ic_launcher.xml`（及 `ic_launcher_round.xml`）里
`<background android:drawable="@color/ic_launcher_background"/>` 提供松绿底色。

## 重新生成

在仓库根目录、需要 Python 3 + Pillow（开发时用 Python 3.10 / Pillow 12.3）：

```bash
# 1) 512 px 母版 + 72 dp 圆形预览（同时打印几何校验结果）
python design/icons/render.py

# 2) 5 档前景 PNG + 目检预览 .tools/icon-preview.png
python design/icons/build_launcher_assets.py
```

两个脚本共用 `render.py` 里的几何常量，改字号只需改一处：

```python
# render.py
GLYPH_DP = 38.0   # 字形墨迹高度；四角 = 0.7021 × GLYPH_DP，须 ≤ 31.5 dp（即 ≤ 44.8 dp）
```

字体按以下顺序从系统加载（不随仓库分发）：

- `C:/Windows/Fonts/NotoSerifSC-VF.ttf`（Windows）
- `/System/Library/Fonts/NotoSerifSC-VF.ttf`（macOS）
- `/usr/share/fonts/opentype/noto/NotoSerifCJK-Regular.ttc`（Linux）

找不到字体时脚本会给出安装提示并退出，不会静默回退到其它字形。

## 目检

`build_launcher_assets.py` 会生成 `.tools/icon-preview.png`（1400 × 1080）：

- 浅色桌面 `#E9E7E2` 与深色桌面 `#171A18` 上的 72 dp 圆形遮罩效果；
- 橙色虚线为 66 dp 安全圆，白色/灰色虚线为 72 dp 可见圆；
- 108 dp 画布 3× 局部：青色虚线 = 字形墨迹外框，四角有标记点；
- 96 / 64 / 48 px 实际尺寸，以及 96 px 的 2× 最近邻放大（检查小尺寸下的笔画是否糊）。

## 文件

| 文件 | 说明 |
|------|------|
| `icon-A.png` | 512 × 512 母版（松绿底 + 纸白「時」，即 108 dp 画布的 1:1 预览） |
| `icon-A-round.png` | 512 × 512 圆形预览：按 72 dp 可见圆裁切，圆外透明（launcher 实际所见） |
| `render.py` | 母版渲染器：字体加载、字形蒙版、几何常量与安全区校验、像素级实测 |
| `build_launcher_assets.py` | 由字体直接渲染 5 档 `ic_launcher_foreground.png`，并生成 `.tools/icon-preview.png` |

## 字体与许可

- **Noto Serif SC**（思源宋体）来自 [notofonts/noto-cjk](https://github.com/notofonts/noto-cjk)，
  采用 [SIL Open Font License 1.1](https://openfontlicense.org/)：可自由使用、修改、再分发，
  包括商用；字体文件本身不随本仓库分发，仅在本机渲染时使用。
- 本目录的图标产物（PNG）属于项目资产，随项目以 Apache-2.0 发布。

## 为什么旧图标会被遮住

旧版前景直接由 512 px 母版提取纸白 alpha 再缩放，字形高 43 dp、但母版的墨迹外框是
365 × 296 px（宽高比 1.233，属于**被裁平的失真比例**：旧渲染器的画布太小，
把「時」底部笔画裁掉了）。结果：

1. 字形底部（「日」下横、「寸」的竖钩）在母版里就是断的；
2. 由它缩放出的前景，四角到画布中心 33.6–34.1 dp，**超过 66 dp 安全圆半径 33 dp**，
   被 launcher 的圆形/圆角遮罩切角。

新版本用 2.4 em 大的画布 + `anchor="mm"` 居中渲染，得到完整墨迹 910 × 923 px @ 1000 em，
再按真实比例生成；四角 26.68 dp，距安全圆 6.32 dp，上述两个问题同时消失。
