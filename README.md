# 時計 · TimeLog

一个极简的 Android 时间记录应用：把一天画成一条**时间块**的时间轴，记录做了什么、从几点到几点；另有一页统计，可左右滑动查看。

项目使用 Kotlin + Jetpack Compose Material 3 + Room，严格 MVVM / 单向数据流。

> v2.2：新增统计页与左右滑动、备注搜索、JSON 导入导出；分类换成 24 色板；删除分类时一并删除事件；重叠记录以「正在编辑的那条」优先，整段覆盖前先确认；顶栏与「现在」横线的间距收紧。

![主界面](docs/screenshots/home.png)
![统计页](docs/screenshots/stats.png)
![记录弹窗](docs/screenshots/record-sheet.png)
![备注搜索](docs/screenshots/search.png)
![分类管理](docs/screenshots/categories.png)
![深色模式](docs/screenshots/home-dark.png)

## 技术栈

| 项目 | 版本 / 取值 |
| --- | --- |
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
| 测试 | JUnit 4.13.2（JVM 单元测试 3 个套件共 36 例） |
| UI / 架构 | Jetpack Compose + Material 3，MVVM + UDF |
| 依赖管理 | Gradle Version Catalog（`gradle/libs.versions.toml`） |
| 构建环境 | JDK 17，Android Studio 2026.1+ |

## 功能

- **时间块时间轴**：一天从 00:00 排到 24:00（今天排到「现在」），每个事件是一块有开始与结束的色块；块高随时长做温和缩放并夹在 58–208dp，长睡眠不会撑爆一屏。
- **两页左右滑动**：主界面（时间轴）与「统计」页在同一层，横向滑动或点顶栏的「時計 / 统计」切换；「完成」键只出现在时间轴页。
- **紧凑顶栏**：标题 23sp、页签 17sp，配合 `statusBarsPadding()`，不再顶到状态栏；右侧是搜索图标与「分类」。
- **时间只在交界处标注**：左侧 44dp 时间槽只在相邻两块的接缝处显示 `HH:mm`，块内不重复时间，视觉上像一条连续的时间带。
- **未记录自动填充**：没有事件覆盖的时间段由虚线「未分类」块补齐，超过一小时还会显示时长（如「1 小时 20 分」），保证一天永远连续、没有空洞；点这段空隙即可就地补记，弹窗会预填好起止时间。
- **横线贴近底部**：列表底部的「现在」横线紧贴在「完成」上方，留白只够呼吸，不再留出一大段空白。
- **正序 + 底部完成键**：列表按时间正序，最新记录在最下方；首次进入与新增记录后自动滚到底部「现在」处。
- **记录不需要标题**：弹窗里只有分类、开始/结束时间、可留空的备注；默认开始时间接在上一条记录的结束时间之后（12 小时内），结束时间取当前时间。
- **分类固定网格**：记录弹窗中的分类是一个固定显示的 FlowRow 网格（根 + 子 + 孙分类胶囊），点一下即选中。
- **默认分类**：工作（沟通、执行）→ 休息（睡眠、放空）→ 学习（输入、输出）→ 娱乐（沉浸、社交）→ 系统分类「未分类」。
- **24 色板**：分类颜色改为按二十四节气顺序生成的 24 种低饱和色；界面上只出现色块与圆点，不显示节气名称。默认分类的旧色在迁移时换成新色，用户自己挑过的颜色不动。
- **分类管理**：最多 3 层；新建顶级/子级、编辑名称与颜色、删除。删除分类会连同其子分类与这些分类下的**全部事件**一起删除（二次确认弹窗会写明将删除多少条事件）。
- **未分类只有一种**：「未记录的空隙」与「记录到系统分类未分类的事件」读的是同一个分类实体，名称与灰色完全一致，不会再出现两个来源不同的「未分类」。
- **重叠可编辑**：重叠的事件仍在时间轴上合并为一块展示，但块内**每条记录都能单独点击编辑**、长按删除，不再出现「整块不可点」。
- **保存时编辑优先**：把正在编辑的区间写进时间轴时，其它记录让路——只在一侧重叠就裁掉那一侧；横跨两侧就保留左侧、把右侧残余拆成新记录；被整个盖住的记录会先弹「时间重叠」确认框列出，点「仍要保存」才删除。
- **备注搜索**：顶栏放大镜进入整屏搜索页，输入关键字即时过滤备注（`LIKE`），结果按时间倒序列出分类路径与日期时段，点一条直接打开编辑。
- **统计页**：今天 / 本周 / 本月 / 全部四种范围，显示总时长、记录条数、按一级分类的用时横条，以及最近 7 天的迷你柱状图。
- **导入导出**：统计页底部「数据」卡片可把分类树与全部事件导出成 `时计-yyyy-MM-dd.json`；导入支持「合并导入」（同名同父分类复用，其余新建）与「覆盖导入」（清空后恢复，二次确认）。
- 长按事件删除（二次确认，对话框显示分类与时间段）。
- 跟随系统深色模式；Edge-to-edge，正确处理状态栏 / 导航栏 / IME Insets。
- 无复杂动画，所有写操作由 Room Flow 驱动界面刷新（时钟每分钟触发一次重新分块，让「现在」跟着走）。

## 工程结构

```text
app/src/main/java/com/example/timelog/
├── TimeLogApplication.kt          # 手动 DI：database / repository 懒加载
├── MainActivity.kt                # edge-to-edge；紧凑顶栏 + HorizontalPager（时间轴/统计）+ 整屏浮层（搜索/分类）
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room v4，MIGRATION_1_2 / 2_3 / 3_4，onCreate 写入默认分类
│   │   ├── dao/CategoryDao.kt
│   │   ├── dao/EventDao.kt        # observeAll、searchByNote、getOverlapping、deleteByCategoryIds…
│   │   └── entity/CategoryEntity.kt, EventEntity.kt
│   ├── repository/TimeLogRepository.kt   # 时间轴/搜索 Flow、重叠保存、分类删除、快照与导入
│   ├── seed/DefaultCategories.kt
│   └── transfer/TransferJson.kt   # 备份 JSON 编解码（org.json，schema 1）
├── domain/
│   ├── OverlapResolver.kt         # 纯函数：重叠时的 update / clone / victims 方案
│   ├── Statistics.kt              # 纯函数：StatsBuilder 汇总总时长、分类用时、最近 7 天
│   └── model/
│       ├── Category.kt, Event.kt
│       ├── CategoryPalette.kt     # 24 色板（按节气顺序，界面不显示名称）
│       ├── LedgerSnapshot.kt      # 导入导出快照 + ImportMode
│       ├── Timeline.kt            # TimelineBuilder：分块、合并重叠、空隙补齐（纯函数）
│       └── CategoryTree.kt        # 树构建、路径、深度、子树、扁平化
├── ui/
│   ├── theme/Color.kt, Type.kt, Theme.kt
│   ├── home/HomeScreen.kt, HomeViewModel.kt, TimelineItem.kt
│   ├── record/RecordSheet.kt, RecordViewModel.kt, CategoryGrid.kt
│   ├── category/CategoryManageScreen.kt, CategoryManageViewModel.kt
│   ├── search/SearchScreen.kt, SearchViewModel.kt
│   ├── stats/StatisticsScreen.kt, StatisticsViewModel.kt
│   └── data/DataTransferSection.kt, DataTransferViewModel.kt
└── util/DateTimeFormat.kt
app/src/test/java/com/example/timelog/domain/
├── TimelineBuilderTest.kt         # 7 例
├── OverlapResolverTest.kt         # 12 例
└── StatisticsTest.kt              # 17 例
design/icons/                       # 5 个图标方案（A–E）+ overview.png + render.py
```

数据流：

```text
Room DAO(Flow) ──► TimeLogRepository.combine ──► ViewModel StateFlow<UiState>
                                                        │
Compose 事件回调 ──► ViewModel ──► Repository ──► Room ──┘（写库后 Flow 自动刷新）
```

- Composable 不访问 DAO；只接收 `UiState` 与回调。
- 时间分块（`TimelineBuilder`）、重叠求解（`resolveOverlap`）、统计（`StatsBuilder`）都是纯函数，可离线单元测试；ViewModel 只做映射与调度。
- 所有业务状态放在 `data class ...UiState` 中，Composable 里不散落业务 `remember`。

## 关键设计取舍

- **事件是「块」而不是「点」**。v2 把 `EventEntity` 改为 `startAt / endAt / note`，删掉 `title` 列：只有起止时间才能画出连续的一天，也只有连续的一天才有「未记录的空隙」可言。
- **数据库迁移**：
  - `MIGRATION_1_2`：旧事件按 30 分钟补全为区间，旧标题并入备注（两者都有则 `title / note`）。
  - `MIGRATION_2_3`：只调整默认种子的 4 个 `sortOrder`。
  - `MIGRATION_3_4`：把仍是**旧默认色**的 12 个种子分类换成 24 色板里的新色；用户改过的颜色用 `WHERE colorHex = 旧值` 保护，不会被覆盖。
- **未记录段只在展示层生成**，不写数据库；它由「一天从 00:00 到 24:00（今天到 now）」减去事件区间得到。空隙里的「未分类」与系统分类「未分类」共用同一份 `Category`（id 5），名称和灰色都来自数据库，撤销了 v2.1 里两套写死文案的割裂。
- **重叠合并成一块，但块内逐条可点**。v2.1 用整块点击，合并块因此失去编辑入口；v2.2 把点击目标下沉到块内每一行（分类 + 备注），点击回填的是那一条事件。
- **保存时「正在编辑的这条」优先**（`OverlapResolver`）。区间按半开 `[start, end)`：首尾相接不算重叠。其它记录：
  - 只在一侧超出 → 原地裁剪到不重叠的那一段（`OverlapUpdate`，静默生效）；
  - 横跨目标区间两侧 → 保留左侧，右侧残余复制成一条新记录（`OverlapClone`，保留原备注/分类/创建时间）；
  - 被目标区间整个盖住 → 进入 `victims`，先弹「时间重叠」确认框列出时间与备注，用户点「仍要保存」才删。
- **删除分类 = 连事件一起删**（用户明确要求）。事件外键是 `RESTRICT`，所以事务里先 `deleteByCategoryIds(subtreeIds)` 再删分类，子分类由自引用外键 `CASCADE` 清理；确认弹窗实时显示「其中 N 条事件会一并删除」。
- **24 色只给颜色，不给名字**。色板 `SolarTermColors` 按节气顺序排 24 个低饱和色值，编辑弹窗排成 FlowRow 圆点，界面任何位置都不出现节气文字。
- **紧凑顶栏与分页外壳**。v2.1 的「時計」是 30sp 且直接贴状态栏；v2.2 用 `statusBarsPadding` + 23sp，并把「统计」作为同级页签；页面用 `HorizontalPager` 承载，滑动与点页签两种切换都成立。「完成」只在时间轴页显示，避免统计页底部出现无意义按钮。
- **统计是纯函数**。`StatsBuilder.build(events, period, now, zone)` 只依赖入参；总时长按每条事件裁剪后分别累加（重叠各算各的，与分类明细口径一致），分类按一级分类聚合，最近 7 天固定从今天回溯。ViewModel 只是把 `repository.timeline` 与每分钟时钟 combine 起来。
- **导入导出不新增依赖**。备份用 Android 自带 `org.json` 编解码，schema 固定为 1；`MERGE` 按「同名 + 同父」复用分类、事件通过旧 ID→新 ID 映射写入，`REPLACE` 先清空事件与分类再按父先子后的顺序重建。文件读写在 SAF 里用 `CreateDocument / OpenDocument` 完成，不申请存储权限。
- **块高做压缩而非严格等比**：24 小时严格等比会得到 1440dp 的列表，一屏看不到一小时。用 `54 + 分钟 × 0.55` 并夹到 58–208dp，保留「长块长、短块短」的节奏感，同时一天大约 1.5–2 屏。
- **记录弹窗去掉标题**。高频录入场景里标题是负担；分类已经表达了「这是什么」，备注保留自由描述。
- **弹窗形态：ModalBottomSheet**。记录是高频、单手操作，底部弹出更贴近拇指热区；分类、删除、导入方式仍用 AlertDialog。
- **手动依赖注入**。项目有 6 个 ViewModel 和 1 个 Repository，通过 `TimeLogApplication` 懒加载单例 + `viewModelFactory` 注入即可；引入 Hilt 会增加 KSP 处理与样板代码，不符合极简目标。
- **时间处理**：业务时间统一存 epoch millis，展示与转换使用系统默认时区；Material3 DatePicker 的毫秒按 UTC 日界线处理，`DateTimeFormat.startOfDayUtcMillis / dateFromUtcMillis` 专门做这层转换。
- **图标选定 A 墨時并已落到资源**。5 个方向在 `design/icons/` 下（Pillow 可复现渲染，4× 超采样 + LANCZOS，遵守自适应图标安全区）；`build_launcher_assets.py` 从 `icon-A.png` 抽出纸白「時」生成 108dp 透明前景，背景沿用 `ic_launcher_background` 的松绿 `#2F5D50`。

## 图标方案

五个方向的对照图：[design/icons/overview.png](design/icons/overview.png)

| # | 名称 | 概念 |
| --- | --- | --- |
| A | 墨時 | 松绿底 + 纸白「時」字，字即图标 |
| B | 素時計 | 极简表盘：墨色圆圈 + 时针分针 + 一点陶土色秒针 |
| C | 時流 | 三块不同高度的圆角时间块，把「一天的节奏」画出来 |
| D | 朱印 | 陶土色印章，纸白「時」+ 内框，像给一天盖戳 |
| E | 日月弧 | 从墨色月点到陶土色日点的一条细弧，一天即一弧 |

**已采用 A（墨時）**：前景 = 纸白「時」，背景 = 松绿，已生成到 `app/src/main/res/drawable-*dpi/ic_launcher_foreground.png`。
其余四个方案与 `overview.png` 保留作设计记录，重新生成前景执行：

```bash
python design/icons/build_launcher_assets.py
```

调色板：纸 `#FBFAF7`、墨 `#1E211F`、松 `#2F5D50`、松浅 `#E6EFEA`、陶土 `#B4654A`、赭 `#C07A2B`、hairline `#E4E1DA`。

## 构建与运行

前置：JDK 17、Android SDK Platform 36、Build Tools 36.0.0，Android Studio 2026.1+。

1. 打开项目根目录，确认 `local.properties` 指向本机 SDK：

   ```properties
   sdk.dir=D\:\\Android\\Sdk
   ```

   `local.properties` 已在 `.gitignore` 中排除，不要提交。

2. 命令行构建与测试：

   ```bash
   ./gradlew testDebugUnitTest assembleDebug
   ```

   Windows PowerShell / CMD 可用 `gradlew.bat`。产物：

   ```text
   app/build/outputs/apk/debug/app-debug.apk
   dist/時計-2.2.apk        # 另存的演示安装包
   ```

3. 启动模拟器（任意 API 29–36 镜像，项目在 API 31 的 Pixel 6 上验证）并安装：

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.example.timelog/.MainActivity
   ```

4. 也可以在 Android Studio 中直接 Run 'app'；AGP 9 要求同步使用 JDK 17。

重新生成图标稿（可选，需要 Pillow + 中文字体）：

```bash
python design/icons/render.py
```

## 手动验收清单

以下均在 API 31 Pixel 6 模拟器（1080×2400）上实际点按验证：

- [x] 空库冷启动：紧凑顶栏、「時計 / 统计」页签、搜索图标、「分类」；空白状态 + 底部「完成」。
- [x] 左右滑动在时间轴与统计页之间切换，点页签同样生效；「完成」只出现在时间轴页。
- [x] 「現在」横线紧贴「完成」按钮（间距约 16dp），不再有一大段空白。
- [x] 分类页不再显示「N 个子分类」；编辑弹窗为 24 个色块（7+7+7+3 排布，无节气名称）。
- [x] 点「完成」→ 分类固定网格；开始时间自动接上一条结束时间，备注可留空。
- [x] 保存后新块出现在底部、「现在」上方，列表自动滚到底；时间轴按 00:00 → 现在铺满，空隙为虚线「未分类」。
- [x] 合并展示的重叠块内，每条记录都能分别点开编辑（分别打开「读文档」「喝茶」）。
- [x] 编辑记录把结束时间延长到盖住另一条记录 → 弹「时间重叠」列出被整段覆盖的记录 → 「仍要保存」后旧记录消失、新区间落库。
- [x] 只在一侧重叠时不弹窗，旧记录被自动裁剪；横跨两侧时右侧残余保留为独立记录。
- [x] 备注搜索：输入 `kyo` 命中「Kyoto trip 规划」，显示分类路径与日期时段，点开进入编辑。
- [x] 统计页「今天 / 本周 / 本月 / 全部」切换，总时长、条数、分类用时横条、最近 7 天柱状图与数据一致（今天 8 小时 20 分 / 4 条）。
- [x] 导出到 SAF：`时计-2026-11-01.json`（约 3.6KB，schema 1，中文与颜色正确）；合并导入事件增加并提示「已导入」；覆盖导入清空后恢复原样。
- [x] 删除分类连同事件：临时分类「测试」（含子分类与 1 条事件）删除后，确认弹窗写明「其中 1 条事件会一并删除」，数据库里分类与事件一并消失且外键检查为空。
- [x] 分类页提示语为「分类最多 3 层。删除分类时，其中的事件会一并删除。」
- [x] 深色模式切换后背景、文字、块底色、虚线、按钮均正确。
- [x] 旧版本数据库（v3）升级到 v4：默认分类换新色、用户自定义色保留、记录完整。
- [x] `am force-stop` 后重启，数据完整保留。
- [x] `./gradlew testDebugUnitTest assembleDebug --console=plain --offline` 全绿（TimelineBuilder 7 + OverlapResolver 12 + Statistics 17 = 36 例）。

## 已知限制

- 分类最多 3 层；不支持拖拽排序，同级按创建顺序（`sortOrder`）排列。
- 事件删除与分类删除都是硬删除，无回收站 / 撤销。
- 分类名称未做同级重名校验。
- 「未分类」填充段是展示层推导的，本身不是数据库记录；可点按补记，但不能直接长按删除。
- 搜索只查备注，不含分类名与日期条件。
- 统计把重叠事件各算各的，因此总时长可能大于所选范围的墙钟时长（与分类明细口径一致）。
- 合并导入按原样追加事件：同一份备份导入两次会得到两份事件（不做去重）；如需干净恢复请用覆盖导入。
- 升级用户不会自动获得新分类体系（保留原分类树），需要时在分类管理里手动加。
- 未做平板 / 横屏专属布局，界面按手机竖屏设计。
