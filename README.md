# 時 · Shi

> 一个极简的 Android 时间记录应用：把一天画成一条由**时间块**组成的时间轴，再把时间按分类层级画成**多层环图**。

**纯本地 · 无账号 · 不联网 · 无广告。** 分类与记录只存在手机本地的 SQLite 里，随时可导出成 JSON 备份。

> 感谢 **DeepSeek-V4.1-Flash** 与 **DeepSeek Harness** 在开发过程中提供的帮助。

[![Android CI](https://github.com/7huig/Shi/actions/workflows/android.yml/badge.svg)](https://github.com/7huig/Shi/actions/workflows/android.yml)
[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![minSdk 29](https://img.shields.io/badge/minSdk-29-brightgreen.svg)](https://developer.android.com/about/versions/android-10)

[下载最新 Release](https://github.com/7huig/Shi/releases/latest) · [验收清单与 FAQ](docs/QA.md) · [English](#english)

## 截图

| 主界面「時記」 | 统计「時計」 |
|:---:|:---:|
| <img src="docs/screenshots/home.webp" width="280" alt="时间轴：今天的记录"> | <img src="docs/screenshots/stats.webp" width="280" alt="统计：多层环图与分类用时"> |
| **记录一段** | **分类编辑** |
| <img src="docs/screenshots/record-sheet.webp" width="280" alt="记录弹窗"> | <img src="docs/screenshots/category-editor.webp" width="280" alt="分类编辑：4×6 节气色板"> |
| **分类管理（可拖动排序）** | **备注搜索** |
| <img src="docs/screenshots/categories.webp" width="280" alt="分类管理"> | <img src="docs/screenshots/search.webp" width="280" alt="备注搜索与时间总和"> |

深色模式见 [home-dark](docs/screenshots/home-dark.webp)、[stats-dark](docs/screenshots/stats-dark.webp)。

## 功能

- **时间块时间轴**：一天从 00:00 排到 24:00（今天排到「现在」），每个事件是一块有起止时间的色块；块高随时长温和缩放并夹在 62–220dp。
- **两页横滑**：主界面「時記」（时间轴）与「時計」（统计）同层，横滑或点顶栏页签切换。
- **未记录自动填充**：没有事件覆盖的时间段由虚线「未分类」块补齐，点空隙就地补记，弹窗预填起止时间。
- **记录弹窗**：只需选分类、调起止时间、可留空的备注；纵向排版紧凑，不堆小标题。
- **分类树**：最多 3 层，24 节气色板（春 / 夏 / 秋 / 冬 4 行 × 6 色）；**按住右侧手柄上下拖动即可调整同级顺序**。
- **重叠可编辑**：重叠事件合并成一块展示，块内每条仍可单独点按编辑、长按删除；保存时「正在编辑的这条」优先，其它记录就地裁剪或拆分。
- **备注搜索**：顶栏放大镜进入整屏搜索，输入即时过滤，并在结果顶部显示**命中记录的时间总和**。
- **统计**：日 / 周 / 月 / 年 / 自定义五种窗口，可前后翻页；外 / 中 / 内三层同心环对应根 / 子 / 孙分类，中心显示总时长与条数。
- **分类下钻**：在「分类用时」里点一级分类，即可只看该分类下一级的环图与用时，面包屑可逐级返回。
- **分类用时与环比**：按一级分类列出用时、次数与相对上一周期的增减；环比只统计到「同一进度」，进行中的周期不会永远显示下降。
- **导入导出**：统计页底部可导出 `時-yyyy-MM-dd.json`，导入支持合并与覆盖。
- 跟随系统深色模式，Edge-to-edge；所有写操作由 Room Flow 驱动刷新，时钟每分钟重新分块，让「现在」跟着走。

## 技术栈

| 项目 | 版本 / 取值 |
| --- | --- |
| 应用版本 | 0.2.0（versionCode 7） |
| Android | minSdk 29 · targetSdk 31 · compileSdk 36 |
| 语言与工具 | Kotlin 2.2.10 · Gradle 9.1.0 · AGP 9.0.1 · JDK 17 |
| UI | Jetpack Compose（BOM 2025.08.01）· Material 3 |
| 数据 | Room 2.7.2（KSP，数据库 v4）· Coroutines 1.10.2 |
| 架构 | MVVM + 单向数据流，纯函数 domain 层 |
| 测试 | JUnit 4.13.2，3 个套件共 53 例 JVM 单元测试 |

## 快速开始

前置：JDK 17、Android SDK Platform 36、Build Tools 36.0.0。

```bash
git clone https://github.com/7huig/Shi.git
cd Shi

# 单元测试（TimelineBuilder 7 + OverlapResolver 12 + Statistics 34 = 53 例）
./gradlew testDebugUnitTest

# 调试包
./gradlew assembleDebug

# 安装到已连接的设备 / 模拟器（API 29+）
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.huig7.shi/.MainActivity
```

Windows 下用 `gradlew.bat`。本项目在 API 31 的 Pixel 6 模拟器（1080×2400）上验证。

### 构建 release 包

release 开启 R8 与资源收缩，APK 约 **2.9 MB**（debug 版约 28 MB）。签名配置从仓库根目录的 `keystore.properties` 读取（**不要提交**，已在 `.gitignore` 中）：

```properties
storeFile=release-keystore.jks
storePassword=…
keyAlias=shi
keyPassword=…
```

没有该文件也能 `assembleRelease`，只是产物未签名。keystore 与密码请离线备份——丢失后无法再覆盖更新已发布的 APK。

## 工程结构

```text
app/src/main/java/io/github/huig7/shi/
├── MainActivity.kt                # edge-to-edge；「時記 / 時計」+ Pager + FAB + 整屏浮层
├── TimeLogApplication.kt          # 手动 DI：database / repository 懒加载
├── data/
│   ├── local/                     # Room v4：AppDatabase、DAO、Entity、迁移
│   ├── repository/TimeLogRepository.kt
│   ├── seed/DefaultCategories.kt
│   └── transfer/TransferJson.kt   # 备份 JSON 编解码
├── domain/                        # 纯函数：TimelineBuilder、OverlapResolver、Statistics、CategoryTree
├── ui/                            # theme / home / record / category / search / stats / data
└── util/DateTimeFormat.kt
app/src/test/java/io/github/huig7/shi/domain/   # 53 个 JVM 单元测试
design/icons/                       # 单一图标方案 + 可复现渲染脚本
```

数据流：`Room DAO(Flow) → Repository → ViewModel StateFlow<UiState> → Compose`；写操作沿原路返回，写库后 Flow 自动刷新界面。Composable 不访问 DAO，业务状态只放在 `UiState` 里。

## 图标

只有一个方案：**墨時**——松绿底 `#2F5D50` + 纸白「時」，不画时钟、不加渐变或阴影。

「時」用 [Noto Serif SC](https://github.com/notofonts/noto-cjk)（SIL Open Font License 1.1）渲染，字体不随仓库分发。字形按 108dp 画布的 **66dp 安全圆**校验（墨迹四角到中心 26.68dp），确保在任何 launcher 遮罩下都不会被切角。

```bash
python design/icons/render.py               # 512px 母版 + 几何校验
python design/icons/build_launcher_assets.py # 5 档自适应图标前景
```

细节与几何数据见 [design/icons/README.md](design/icons/README.md)。

## 已知限制

- 分类最多 3 层；统计环图最多展开 3 层，更深分类的时长只计入祖先。
- 事件与分类都是硬删除，无回收站 / 撤销；分类名称未做同级重名校验。
- 「未分类」填充段由展示层推导，可点按补记，但不能直接长按删除。
- 搜索只匹配备注，不含分类名与日期条件。
- 统计把重叠事件各算各的，总时长可能大于所选范围的墙钟时长。
- 未做平板 / 横屏专属布局；targetSdk 仍为 31（侧载自用，上架 Google Play 需升到 API 36）。

更多验收细节与常见问题见 [docs/QA.md](docs/QA.md)。

## English

**Shi (時)** is a minimal, local-only Android time logger. A day is drawn as a vertical timeline of time blocks; a second page turns the same data into a multi-ring chart per category hierarchy, with day / week / month / year / custom windows. Categories can be drilled into, reordered by dragging, and notes are searchable with a total-time summary.

Kotlin 2.2 · Jetpack Compose (Material 3) · Room · MVVM/UDF · pure-function domain logic covered by 53 JVM unit tests. No network permission, no account, no ads.

```bash
./gradlew testDebugUnitTest assembleDebug
```

Requires Android 10 (API 29)+. Licensed under [Apache-2.0](LICENSE).

## License

[Apache License 2.0](LICENSE) © 2026 [7huig](https://github.com/7huig)
