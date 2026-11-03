# 時 · Shi

> 纯本地、无账号、无联网权限的 Android 时间记录器：用时间块和多层环图记录、复盘一天。

[![Release](https://img.shields.io/badge/release-0.2.1-2F5D50)](https://github.com/7huig/Shi/releases/tag/v0.2.1)
[![Android CI](https://github.com/7huig/Shi/actions/workflows/android.yml/badge.svg)](https://github.com/7huig/Shi/actions/workflows/android.yml)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![minSdk 29](https://img.shields.io/badge/Android-10%2B-brightgreen.svg)](https://developer.android.com/about/versions/android-10)

**[下载 Shi 0.2.1 APK](https://github.com/7huig/Shi/releases/download/v0.2.1/Shi-0.2.1.apk)** · Android 10+ · 约 2.9 MB · 无广告

[全部版本与校验值](https://github.com/7huig/Shi/releases) · [English Quick Start](#english-quick-start) · [验收与 FAQ](docs/QA.md)

> 数据只保存在本机。**卸载或清除应用数据会删除记录，请先导出 JSON 备份。**

## 记录一天，也看清一天

| 时间块时间轴 | 可交互的多层环图 |
|:---:|:---:|
| <img src="docs/screenshots/home.webp" width="320" alt="时间轴：时间块、未记录空隙与松绿新建按钮"> | <img src="docs/screenshots/stats.webp" width="320" alt="统计：环图弧段与分类用时联动"> |

- **时间块时间轴**：连续展示记录与空白，点空隙即可补记；重叠记录仍可分别编辑。
- **统计与下钻**：日／周／月／年／自定义窗口，环图选中、分类联动、层级下钻与环比。
- **轻量记录**：三级分类、节气色板、拖动排序、备注搜索与命中总时长。
- **本地备份**：导出 JSON；合并导入识别重复事件，也可覆盖恢复。没有账号、云端同步或网络权限。

## 安装与数据

1. 下载上方 APK，按 Android 提示允许本次侧载安装。
2. 覆盖升级必须使用同一签名；升级前建议先导出 JSON。不要混用不同签名的安装包。
3. 应用不申请网络或存储权限，系统云备份与设备迁移也已排除；换机请手动导出备份。
4. 已知边界与验收记录见 [QA / FAQ](docs/QA.md)。

<details>
<summary>更多截图</summary>

| 记录表单 | 分类编辑 | 分类管理 | 备注搜索 |
|:---:|:---:|:---:|:---:|
| <img src="docs/screenshots/record-sheet.webp" width="220" alt="记录表单：分类层级、开始结束与时长"> | <img src="docs/screenshots/category-editor.webp" width="220" alt="分类编辑：节气色板"> | <img src="docs/screenshots/categories.webp" width="220" alt="分类管理：拖动排序"> | <img src="docs/screenshots/search.webp" width="220" alt="备注搜索与时间总和"> |

深色模式：[时间轴](docs/screenshots/home-dark.webp) · [统计](docs/screenshots/stats-dark.webp)

</details>

## Roadmap

**Now**：本地数据可靠性、手机端记录与统计体验、Android 10+ 侧载使用。

**Next**（候选方向，不承诺时间）：targetSdk 36、删除撤销、数据库迁移与 UI 自动化测试、平板／横屏布局。欢迎通过 [Issue](https://github.com/7huig/Shi/issues) 讨论优先级。

**Not planned**：账号体系、云端同步、广告与订阅。

## 技术栈与质量

| 项目 | 版本 / 取值 |
| --- | --- |
| 应用版本 | 0.2.1（versionCode 8） |
| Android | minSdk 29 · targetSdk 31 · compileSdk 36 |
| 语言与工具 | Kotlin 2.2.10 · Gradle 9.1.0 · AGP 9.0.1 · JDK 17 |
| UI | Jetpack Compose（BOM 2025.08.01）· Material 3 |
| 数据 | Room 2.7.2（数据库 v5）· Coroutines 1.10.2 |
| 架构 | MVVM + 单向数据流，纯函数 domain 层 |
| 验证 | 58 例 JVM 单元测试 · CI test / lint / assemble · Pixel 6 API 31 手动验收 |

## 从源码构建

前置：JDK 17、Android SDK Platform 36、Build Tools 36.0.0。

```bash
git clone https://github.com/7huig/Shi.git
cd Shi

./gradlew testDebugUnitTest lintDebug assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.huig7.shi/.MainActivity
```

Windows 使用 `gradlew.bat`。Release 开启 R8 与资源收缩；如需签名，在仓库根目录配置**不提交到 Git** 的 `keystore.properties`：

```properties
storeFile=release-keystore.jks
storePassword=…
keyAlias=shi
keyPassword=…
```

没有该文件也能构建未签名 Release。贡献代码请阅读 [CONTRIBUTING](CONTRIBUTING.md)。

## 工程结构

```text
app/src/main/java/io/github/huig7/shi/
├── MainActivity.kt                # 「時記 / 時計」+ Pager + 整屏浮层
├── TimeLogApplication.kt          # 手动 DI
├── data/                          # Room、Repository、种子分类、JSON 备份
├── domain/                        # 时间轴、重叠、统计、分类树等纯函数
├── ui/                            # home / record / stats / category / search / data
└── util/DateTimeFormat.kt
app/src/test/java/io/github/huig7/shi/domain/   # 58 个 JVM 单元测试
design/icons/                       # 墨時图标与可复现渲染脚本
```

## 已知限制

- 分类与环图最多展开 3 层，更深分类的时长只计入祖先。
- 删除为硬删除，暂无回收站或撤销；分类名称不做同级重名校验。
- 搜索匹配备注，不含分类名或日期条件；重叠事件分别累计，总时长可能大于墙钟时长。
- 未做平板／横屏专属布局；targetSdk 仍为 31，上架 Google Play 前需升级并重新验证。

## English Quick Start

**Shi (時)** is a local-only Android time logger with a continuous block timeline and interactive multi-ring statistics. No account, ads, network permission, or cloud sync.

**[Download Shi 0.2.1 APK](https://github.com/7huig/Shi/releases/download/v0.2.1/Shi-0.2.1.apk)** · Android 10+ · about 2.9 MB · [Release notes & checksum](https://github.com/7huig/Shi/releases/tag/v0.2.1)

- Record and edit time blocks, fill gaps, and search notes with total duration.
- Explore day / week / month / year / custom windows, select ring segments, and drill into categories.
- Export JSON before uninstalling or switching devices; local data is erased with app data.

Known limits: three category levels, no undo or cloud sync, phone-first layout, targetSdk 31. See [QA / FAQ](docs/QA.md) and [source build instructions](#从源码构建).

## License

[Apache License 2.0](LICENSE) © 2026 [7huig](https://github.com/7huig)

感谢 **DeepSeek-V4.1-Flash** 与 **DeepSeek Harness** 在开发过程中提供的帮助。「墨時」图标使用 Noto Serif SC（SIL OFL 1.1）渲染，字体不随仓库分发。
