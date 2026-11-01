# 時 · Shi

> 极简的 Android 时间记录应用：把一天画成一条**时间块**组成的时间轴，把时间按分类层级画成**多层环图**。

**纯本地 · 无账号 · 不联网 · 无广告。** 分类与记录只存在手机本地的 SQLite 里，随时可以导出成 JSON 备份。

> 感谢 **DeepSeek-V4.1-Flash** 与 **DeepSeek Harness** 在开发过程中提供的帮助。

[![Android CI](https://github.com/7huig/Shi/actions/workflows/android.yml/badge.svg)](https://github.com/7huig/Shi/actions/workflows/android.yml)
[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![minSdk 29](https://img.shields.io/badge/minSdk-29-brightgreen.svg)](https://developer.android.com/about/versions/android-10)
[![Kotlin 2.2](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)

[下载最新 Release](https://github.com/7huig/Shi/releases/latest) · [验收清单与 FAQ](docs/QA.md) · [English](#english)

## 截图

| 主界面「時記」 | 统计「時計」 |
|:---:|:---:|
| <img src="docs/screenshots/home.webp" width="300" alt="主界面：一天的时间块"> | <img src="docs/screenshots/stats.webp" width="300" alt="统计：多层环图与分类用时"> |
| **记录一段** | **节气色板** |
| <img src="docs/screenshots/record-sheet.webp" width="300" alt="记录弹窗"> | <img src="docs/screenshots/category-editor.webp" width="300" alt="分类编辑：4×6 节气色板"> |
| **分类管理** | **备注搜索** |
| <img src="docs/screenshots/categories.webp" width="300" alt="分类管理"> | <img src="docs/screenshots/search.webp" width="300" alt="备注搜索"> |

深色模式、日 / 周 / 月 / 年 / 自定义五种统计视图见 [docs/screenshots](docs/screenshots)。

## 功能

- **时间块时间轴**：一天从 00:00 排到 24:00（今天排到「现在」），每个事件是一块有开始与结束的色块；块高随时长温和缩放并夹在 62–220dp，长睡眠不会撑爆一屏。
- **两页左右滑动**：主界面「時記」（时间轴）与「時計」（统计）在同一层，横向滑动或点顶栏页签切换。
- **右下角「+」**：62dp 蓝底白加号悬浮圆钮，避让导航栏；列表底部预留 96dp，最后一条记录不会被遮住。
- **整块可点**：点记录块任意位置打开块内第一条事件的编辑；块内每条记录仍可单独点按 / 长按，内层优先。
- **未记录自动填充**：没有事件覆盖的时间段由虚线「未分类」块补齐，超过一小时显示时长；点空隙即可就地补记，弹窗预填起止时间。
- **记录不需要标题**：弹窗里只有分类、开始 / 结束时间、可留空的备注；默认开始时间接在上一条记录的结束时间之后（12 小时内），结束时间取当前时间。
- **分类固定网格**：记录弹窗中的分类是固定显示的 FlowRow 网格（根 + 子 + 孙），点一下即选中。
- **节气色板（4 × 6）**：24 色按「春 / 夏 / 秋 / 冬」分成 4 行、每行 6 个色块，下方标注节气名（立春 → 大寒）；选中的色块有 2dp 描边。
- **分类管理**：最多 3 层；新建顶级 / 子级、编辑名称与颜色、删除。删除分类会连同子分类与这些分类下的**全部事件**一起删除（二次确认会写明将删除多少条事件）。
- **重叠可编辑**：重叠的事件在时间轴上合并为一块展示，但块内每条记录都能单独点击编辑、长按删除；保存时「正在编辑的这条」优先，其它记录就地裁剪、拆分或（确认后）让位。
- **备注搜索**：顶栏放大镜进入整屏搜索页，输入关键字即时过滤备注，结果按时间倒序，点一条直接打开编辑。
- **统计查看方式**：日 / 周 / 月 / 年 / 自定义五种；周从周一开始，可前后翻页；自定义用两个日期选择器选闭区间。
- **多层环形堆叠图**：总时长按分类层级画成最多三层的同心圆环（根 → 子 → 孙），中心显示总时长与记录条数。
- **分类用时与环比**：按一级分类列出用时与细进度条，附「N 次」与相对上一周期的增减比例；环比只统计到「同一进度」，进行中的周期不会因为还没过完而永远显示下降。
- **导入导出**：统计页底部可把分类树与全部事件导出成 `時-yyyy-MM-dd.json`；导入支持「合并导入」与「覆盖导入」（二次确认）。
- 跟随系统深色模式；Edge-to-edge，正确处理状态栏 / 导航栏 / IME Insets。
- 无复杂动画；所有写操作由 Room Flow 驱动界面刷新，时钟每分钟触发一次重新分块，让「现在」跟着走。

## 技术栈

| 项目 | 版本 / 取值 |
| --- | --- |
| 应用版本 | 0.1.0（versionCode 6） |
| compileSdk | 36（Android 16） |
| targetSdk | 31（Android 12） |
| minSdk | 29（Android 10） |
| Android Gradle Plugin | 9.0.1 |
| Gradle | 9.1.0 |
| SDK Build Tools | 36.0.0 |
| Kotlin | 2.2.10（AGP 9 内置 Kotlin，不额外应用 kotlin-android） |
| Compose Compiler | org.jetbrains.kotlin.plugin.compose 2.2.10 |
| Compose BOM | 2025.08.01（Compose 1.9.0 / Material3 1.3.2） |
| Room | 2.7.2（KSP 2.3.6，数据库 v4） |
| Coroutines | 1.10.2 |
| 测试 | JUnit 4.13.2（JVM 单元测试 3 个套件共 45 例） |
| UI / 架构 | Jetpack Compose + Material 3，MVVM + UDF |
| 依赖管理 | Gradle Version Catalog（`gradle/libs.versions.toml`） |
| 构建环境 | JDK 17，Android Studio 2026.1+ |

## 快速开始

前置：JDK 17、Android SDK Platform 36、Build Tools 36.0.0，Android Studio 2026.1+。

```bash
git clone https://github.com/7huig/Shi.git
cd Shi
```

用 Android Studio 打开项目根目录即可（首次同步会自动生成 `local.properties`）。命令行构建与测试：

```bash
# 单元测试（TimelineBuilder 7 + OverlapResolver 12 + Statistics 26 = 45 例）
./gradlew testDebugUnitTest

# 调试包
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk

# 安装到已连接的设备 / 模拟器（API 29+）
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.huig7.shi/.MainActivity
```

Windows 下可用 `gradlew.bat`。本项目在 API 31 的 Pixel 6 模拟器（1080×2400）上验证。

### 构建 release 包

release 开启 R8 代码压缩与资源收缩，APK 约 **2.9 MB**（debug 版因未裁剪 + Compose tooling 约 28 MB）。

签名配置从仓库根目录的 `keystore.properties` 读取（**不要提交**，已在 `.gitignore` 中）：

```properties
storeFile=release-keystore.jks
storePassword=…
keyAlias=shi
keyPassword=…
```

没有该文件时仍可 `assembleRelease`，只是产物未签名。发布前先备份 keystore 与密码——丢失后无法再覆盖更新已发布的 APK。

## 工程结构

```text
app/src/main/java/com/example/timelog/
├── TimeLogApplication.kt          # 手动 DI：database / repository 懒加载
├── MainActivity.kt                # edge-to-edge；「時記 / 時計」顶栏 + HorizontalPager + FAB + 整屏浮层
├── data/
│   ├── local/                     # Room v4：AppDatabase、DAO、Entity、MIGRATION_1_2 / 2_3 / 3_4
│   ├── repository/TimeLogRepository.kt
│   ├── seed/DefaultCategories.kt
│   └── transfer/TransferJson.kt   # 备份 JSON 编解码（org.json，schema 1）
├── domain/                        # 纯函数：OverlapResolver、Statistics、TimelineBuilder、CategoryTree
├── ui/                            # theme / home / record / category / search / stats / data
└── util/DateTimeFormat.kt
app/src/test/java/com/example/timelog/domain/   # 45 个 JVM 单元测试
design/icons/                       # 5 个图标方案 + overview.png + 可复现渲染脚本
```

数据流：

```text
Room DAO(Flow) ──► TimeLogRepository.combine ──► ViewModel StateFlow<UiState>
                                                        │
Compose 事件回调 ──► ViewModel ──► Repository ──► Room ──┘（写库后 Flow 自动刷新）
```

- Composable 不访问 DAO；只接收 `UiState` 与回调。
- 时间分块、重叠求解、统计都是纯函数，可离线单元测试；ViewModel 只做映射与调度。
- 所有业务状态放在 `data class ...UiState` 中，Composable 里不散落业务 `remember`。

## 关键设计取舍

- **事件是「块」而不是「点」**。`EventEntity` 只有 `startAt / endAt / note`，没有标题：只有起止时间才能画出连续的一天，也只有连续的一天才有「未记录的空隙」可言。
- **未记录段只在展示层生成**，不写数据库；它由「一天 00:00–24:00（今天到 now）」减去事件区间得到，与系统分类「未分类」共用同一份 Category。
- **保存时「正在编辑的这条」优先**（`OverlapResolver`）。区间按半开 `[start, end)`：首尾相接不算重叠；只在一侧超出就原地裁剪，横跨两侧则保留左侧、右侧残余拆成新记录，被整个盖住的记录先弹「时间重叠」确认。
- **删除分类 = 连事件一起删**。事件外键是 `RESTRICT`，事务里先 `deleteByCategoryIds(subtreeIds)` 再删分类，子分类由自引用外键 `CASCADE` 清理。
- **统计是纯函数**。`StatsBuilder.build(events, tree, window, now, zone)` 只依赖入参；窗口、翻页、环比全部由 `StatsWindows` 计算，不读系统时钟。
- **环比对齐已过时长**：上一周期只统计到 `previousStart + (now - currentStart)`；上一周期为 0 时显示「新增」。
- **环图只画三层**：更深分类的时长已计入祖先，不再单独展开；空周期画 0.3 alpha 的空轨道。
- **导入导出不新增依赖**：用 Android 自带 `org.json`，schema 固定为 1；SAF 的 `CreateDocument / OpenDocument` 读写，不申请存储权限。
- **块高做压缩而非严格等比**：`58 + 分钟 × 0.60 + 额外行 × 28` 并夹到 62–220dp，保留「长块长、短块短」的节奏感。
- **手动依赖注入**：6 个 ViewModel + 1 个 Repository，用 `TimeLogApplication` 懒加载单例 + `viewModelFactory` 注入即可；Hilt 的样板代码不符合极简目标。
- **数据库迁移**：`MIGRATION_1_2` 把旧事件补成区间并把标题并入备注；`MIGRATION_3_4` 只替换仍是旧默认色的种子分类，用户改过的颜色不会被覆盖。

## 图标

五个方向的对照图：[design/icons/overview.png](design/icons/overview.png)

| # | 名称 | 概念 |
| --- | --- | --- |
| A | 墨時 | 松绿底 + 纸白「時」字，字即图标 |
| B | 素時計 | 极简表盘：墨色圆圈 + 时针分针 + 一点陶土色秒针 |
| C | 時流 | 三块不同高度的圆角时间块，把「一天的节奏」画出来 |
| D | 朱印 | 陶土色印章，纸白「時」+ 内框，像给一天盖戳 |
| E | 日月弧 | 从墨色月点到陶土色日点的一条细弧，一天即一弧 |

**已采用 A（墨時）**：前景 = 纸白「時」，背景 = 松绿 `#2F5D50`。图标母版用 [Noto Serif SC / Noto Sans SC](https://github.com/notofonts/noto-cjk)（SIL Open Font License 1.1）渲染，字体本身不随仓库分发。

重新生成图标稿与启动器资源（需要 Pillow 与上述字体）：

```bash
python design/icons/render.py
python design/icons/build_launcher_assets.py
```

调色板：纸 `#FBFAF7`、墨 `#1E211F`、松 `#2F5D50`、松浅 `#E6EFEA`、陶土 `#B4654A`、赭 `#C07A2B`、hairline `#E4E1DA`；动作蓝 `#2F6FE4`。

## 已知限制

- 分类最多 3 层；不支持拖拽排序，同级按创建顺序排列。
- 环形图最多展开 3 层，更深分类的时长只计入祖先。
- 自定义时间段允许选未来日期；窗口在将来时显示「这段时间还没有记录」。
- 事件删除与分类删除都是硬删除，无回收站 / 撤销。
- 分类名称未做同级重名校验。
- 「未分类」填充段是展示层推导的，本身不是数据库记录；可点按补记，但不能直接长按删除。
- 搜索只查备注，不含分类名与日期条件。
- 统计把重叠事件各算各的，因此总时长可能大于所选范围的墙钟时长。
- 合并导入按原样追加事件，同一份备份导入两次会得到两份事件；需要干净恢复请用覆盖导入。
- 未做平板 / 横屏专属布局，界面按手机竖屏设计。
- targetSdk 仍为 31：个人自用、侧载安装没问题；若上架 Google Play，需按政策升级到 API 36。

更多验收细节与常见问题见 [docs/QA.md](docs/QA.md)。

## English

**Shi (時)** is a minimal Android time-logging app. A day is drawn as a vertical timeline of time blocks; a second page turns the same data into a multi-ring chart per category hierarchy, with day / week / month / year / custom windows.

- **Local-only & private** — no account, no ads, no `INTERNET` permission. Everything lives in a local SQLite database and can be exported as JSON.
- **Timeline blocks** — each event is a colored block with a start and end; unrecorded gaps are filled in the UI, and tapping a gap logs time there directly.
- **Category tree** — up to three levels, 24 seasonal colors, delete cascades with a confirmation.
- **Stats** — three-level ring chart, per-category usage with previous-period comparison; the comparison window is clipped to the same elapsed progress.
- **Stack** — Kotlin 2.2, Jetpack Compose (Material 3), Room, MVVM / UDF, pure-function domain logic covered by 45 JVM unit tests.

Build with JDK 17 and Android SDK 36:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Requires Android 10 (API 29) or newer. Release builds are minified with R8 (~2.9 MB). Licensed under [Apache-2.0](LICENSE).

## License

[Apache License 2.0](LICENSE) © 2026 [7huig](https://github.com/7huig)
