# 時計 · TimeLog

一个极简的 Android 时间记录应用：把一天画成一条**时间块**的时间轴，记录做了什么、从几点到几点。
项目使用 Kotlin + Jetpack Compose Material 3 + Room，严格 MVVM / 单向数据流。

> 本版由「时间流」更名为「時計」，时间轴由「单点事件 + 倒序列表」改为「有始有终的事件块 + 正序时间轴」。

![主界面](docs/screenshots/home.png)
![记录弹窗](docs/screenshots/record-sheet.png)
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
| Room | 2.7.2（KSP 2.3.6，数据库 v2） |
| Coroutines | 1.10.2 |
| 测试 | JUnit 4.13.2（JVM 单元测试：TimelineBuilder） |
| UI / 架构 | Jetpack Compose + Material 3，MVVM + UDF |
| 依赖管理 | Gradle Version Catalog（`gradle/libs.versions.toml`） |
| 构建环境 | JDK 17，Android Studio 2026.1+ |

## 功能

- **时间块时间轴**：一天从 00:00 排到 24:00（今天排到「现在」），每个事件是一块有开始与结束的色块；块高随时长做温和缩放并夹在 58–208dp，长睡眠不会撑爆一屏。
- **时间只在交界处标注**：左侧 44dp 时间槽只在相邻两块的接缝处显示 `HH:mm`，块内不重复时间，视觉上像一条连续的时间带。
- **未记录自动填充**：没有事件覆盖的时间段由虚线「未分类」块补齐，超过一小时还会显示时长（如「1 小时 20 分」），保证一天永远连续、没有空洞；点这段空隙即可就地补记，弹窗会预填好起止时间。
- **正序 + 底部完成键**：列表按时间正序，最新记录在最下方、紧贴「完成」按钮；首次进入与新增记录后自动滚动到底部「现在」处。
- **记录不需要标题**：弹窗里只有分类、开始/结束时间、可留空的备注；默认开始时间接在上一条记录的结束时间之后（12 小时内），结束时间取当前时间。
- **分类固定网格**：记录弹窗中的分类不再放在可滚动小框里，而是一个固定显示的 FlowRow 网格（根 + 子 + 孙分类胶囊），点一下即选中。
- **默认分类**：工作（沟通、执行）→ 休息（睡眠、放空）→ 学习（输入、输出）→ 娱乐（沉浸、社交）→ 系统分类「未分类」。
- **分类管理**：最多 3 层；新建顶级/子级、编辑名称与颜色、删除；删除分类时其子树事件迁移到「未分类」，不丢记录。
- 点击事件编辑（回填分类、起止时间、备注），长按事件删除（二次确认，对话框显示分类与时间段）。
- 跟随系统深色模式；Edge-to-edge，正确处理状态栏 / 导航栏 / IME Insets。
- 无复杂动画，所有写操作由 Room Flow 驱动界面刷新（时钟每分钟触发一次重新分块，让「现在」跟着走）。

## 工程结构

```text
app/src/main/java/com/example/timelog/
├── TimeLogApplication.kt          # 手动 DI：database / repository 懒加载
├── MainActivity.kt                # edge-to-edge、三个 ViewModel、页面与弹窗调度
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room v2，MIGRATION_1_2，onCreate 写入默认分类
│   │   ├── dao/CategoryDao.kt
│   │   ├── dao/EventDao.kt        # observeAll ORDER BY startAt ASC, id ASC；latestEnd()
│   │   └── entity/CategoryEntity.kt, EventEntity.kt
│   ├── repository/TimeLogRepository.kt
│   └── seed/DefaultCategories.kt
├── domain/model/
│   ├── Category.kt
│   ├── Event.kt                   # Event（startAt/endAt）+ TimelineEvent（已组装分类路径与颜色）
│   ├── Timeline.kt                # TimelineBuilder：分块、合并重叠、空隙补齐（纯函数）
│   └── CategoryTree.kt            # 树构建、路径、深度、子树、扁平化
├── ui/
│   ├── theme/Color.kt, Type.kt, Theme.kt
│   ├── home/HomeScreen.kt, HomeViewModel.kt, TimelineItem.kt
│   ├── record/RecordSheet.kt, RecordViewModel.kt, CategoryGrid.kt
│   └── category/CategoryManageScreen.kt, CategoryManageViewModel.kt
└── util/DateTimeFormat.kt
app/src/test/java/com/example/timelog/domain/TimelineBuilderTest.kt
design/icons/                       # 5 个图标方案（A–E）+ overview.png + render.py
```

数据流：

```text
Room DAO(Flow) ──► TimeLogRepository.combine ──► ViewModel StateFlow<UiState>
                                                        │
Compose 事件回调 ──► ViewModel ──► Repository ──► Room ──┘（写库后 Flow 自动刷新）
```

- Composable 不访问 DAO；只接收 `UiState` 与回调。
- 时间分块逻辑在 `domain/model/Timeline.kt`（`TimelineBuilder.build`），是纯函数，可离线单元测试；ViewModel 只负责把 Flow 结果映射成 `HomeDay`。
- 分类树在 Repository / domain 层构建，UI 只消费 `CategoryTree`。
- 所有业务状态放在 `data class ...UiState` 中，Composable 里不散落业务 `remember`。

## 关键设计取舍

- **事件是「块」而不是「点」**。v2 把 `EventEntity` 改为 `startAt / endAt / note`，删掉 `title` 列。理由是：只有起止时间才能画出连续的一天，也只有连续的一天才有「未记录的空隙」可言。
- **数据库 v2 迁移（`MIGRATION_1_2`）**：建新表 → 用旧 `timestamp` 作 `startAt`、`timestamp + 30 分钟` 作 `endAt` → 旧标题并入备注（`note` 为空用 `title`，两者都有则 `title || " / " || note`）→ 删旧表改名 → 重建两个索引。已在真机流程中用旧版本 APK 建库再覆盖安装验证，旧记录完整保留。
- **旧分类保持不变**。首装才写入新分类（工作/休息/学习/娱乐），升级用户继续用自己已有的分类树，避免迁移误删用户数据；「未分类」（ID 5，`isSystem`）始终兜底。
- **v2→v3 只调顺序**：把默认种子的「沟通/执行」「沉浸/社交」两对换了 `sortOrder`，迁移只更新这 4 个固定 ID 且要求名称仍是默认值，用户改过名的分类不会被覆盖。
- **未记录段只在展示层生成**，不写数据库。它由 `TimelineBuilder` 用「一天从 00:00 到 24:00（今天到 now）」减去事件区间得到，所以删除/修改事件后自动重算，也不会污染真实记录。
- **重叠事件合并成一块**：同一时间段里的多条记录合成一个块，块内逐条列出，避免互相遮挡；首尾相接（end == 下一条 start）不算重叠，各占一块。
- **块高做压缩而非严格等比**：24 小时严格等比会得到 1440dp 的列表，一屏看不到一小时。用 `54 + 分钟 × 0.55` 并夹到 58–208dp，保留「长块长、短块短」的节奏感，同时一天大约 1.5–2 屏。
- **正序 + 自动滚到底**。倒序列表会让新记录出现在顶部，与底部「完成」键距离很远；正序把「现在」固定在列表末端，符合「刚做完就记一笔」的动作顺序。自动滚动只在首次进入与数据变化时发生，用户向上翻历史时不会每分钟被拽回底部（`lastRecordedAt` 变化才触发）。
- **记录弹窗去掉标题**。高频录入场景里标题是负担；分类已经表达了「这是什么」，备注保留自由描述。默认开始时间接上一条记录的结束时间，连续记录只需点分类再点保存。
- **分类改为固定网格**。旧版分类选择器是 224dp 高的可滚动盒子，选一个深层分类要先滚动；新版把整棵树一次性铺开成胶囊网格，全部可见、点一下即可。弹窗整体仍可滚动，只作为小屏幕的兜底。
- **弹窗形态：ModalBottomSheet**。记录是高频、单手操作，底部弹出更贴近拇指热区。分类编辑和删除确认仍用 AlertDialog。
- **手动依赖注入**。项目只有 3 个 ViewModel 和 1 个 Repository，通过 `TimeLogApplication` 懒加载单例 + `viewModelFactory` 注入即可；引入 Hilt 会增加 KSP 处理与样板代码，不符合极简目标。
- **时间处理**：业务时间统一存 epoch millis，展示与转换使用系统默认时区；Material3 DatePicker 的毫秒按 UTC 日界线处理，`DateTimeFormat.startOfDayUtcMillis / dateFromUtcMillis` 专门做这层转换，避免时区偏移一天。
- **图标选定 A 墨時并已落到资源**。5 个方向在 `design/icons/` 下（Pillow 可复现渲染，4× 超采样 + LANCZOS，遵守自适应图标安全区）；`build_launcher_assets.py` 从 `icon-A.png` 抽出纸白「時」生成 108dp 透明前景（5 个密度档，字形高度 43dp ≈ 可见圆直径的 59%），背景沿用 `ic_launcher_background` 的松绿 `#2F5D50`，启动器图标与圆形图标共用。

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

- [x] 空库冷启动：标题「時計」、无「共 N 条记录」、居中空状态 + 底部「完成」。
- [x] 点「完成」→ 弹窗分类为固定网格（工作/执行/沟通、休息/睡眠/放空、学习/输入/输出、娱乐/社交/沉浸、未分类全部可见，无需滚动）。
- [x] 弹窗无标题字段，备注可留空；开始时间自动接上一条结束时间，结束时间为当前时间。
- [x] 保存后新块出现在列表底部、「现在」上方，列表自动滚到底。
- [x] 时间轴按 00:00 → 现在铺满：每个事件是块，块间空隙是虚线「未分类」块，时间只出现在交界处。
- [x] 记录 3.5 小时与 40 分钟的块，高度有明显差别但都不会超过一屏。
- [x] 点击块进入编辑（分类/起止时间/备注均回填），保存后原位刷新。
- [x] 长按块 → 删除确认（显示「分类 15:20–16:40」与备注）→ 删除后空隙自动并入未分类。
- [x] 点「未分类」空隙 → 弹窗预填该段起止时间（如 12:10–13:30）→ 保存后空隙变成正式块。
- [x] 跨午夜事件在两天里各出现一半；重叠事件合并为一块并在块内逐条列出（单元测试覆盖）。
- [x] 分类管理：新建/编辑/删除；删除「工作」时其事件迁移到「未分类」；默认顺序为 工作/休息/学习/娱乐，「沟通」在「执行」前、「沉浸」在「社交」前。
- [x] 启动器图标为「墨時」：松绿圆形底 + 纸白「時」，自适应图标在桌面/抽屉/圆形遮罩下均正常。
- [x] 深色模式切换后背景、文字、块底色、虚线、按钮均正确。
- [x] 旧版本（v1，含 2 条带标题记录）覆盖安装：`user_version` 1→2，记录变成 30 分钟块，标题进入备注，未崩溃。
- [x] `am force-stop` 后重启，数据完整保留。
- [x] `./gradlew testDebugUnitTest assembleDebug --console=plain` 全绿（TimelineBuilder 7 个用例）。

## 已知限制

- 分类最多 3 层；不支持拖拽排序，同级按创建顺序（`sortOrder`）排列。
- 事件删除是硬删除，无回收站 / 撤销。
- 分类名称未做同级重名校验。
- 「未分类」填充段是展示层推导的，本身不是数据库记录；可点按补记，但不能直接长按删除。
- 升级用户不会自动获得新分类体系（保留原分类树），需要时在分类管理里手动加。
- 无搜索、筛选、统计、导出、提醒、重复事件。
- 未做平板 / 横屏专属布局，界面按手机竖屏设计。
