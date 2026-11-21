# 「時」年度报告功能方案

> 状态：范围已确认 · P0–P2 已实现 · 0.4.0 趣味化改版已验收（2026-11-22）
>
> 0.4.0 调整：入口改为顶栏「⋮ → 年度报告」，随时可打开；统计页年视图入口与年末自动弹出均已移除。下方关于「入口 / 自动弹窗」的章节是历史设计记录。
>
> 实施说明：最终形态比本文最初的设计更接近「年终回顾」——年度报告改为十章节横向滑动，新增年度称号、电影 / 工作日换算、第一次与最后一次、最忙一天等；数据口径与分享能力保持不变。
>
> 约束：纯本地、不新增权限、不新增第三方依赖、不修改 Room schema。

## 0. 一句话方案

在「時計」年视图之上增加一个整屏的「年度报告」浮层：用一年的事件算出总量、分类去向、十二个月节奏、记录习惯与同比；年末首次打开应用时自动呈现，可回看历史年份，先提供文本分享，再补摘要长图。数据全部来自现有 `event` 表，聚合在后台线程完成，不引入新表。

## 1. 定位：为什么不是「加强版年视图」

年视图已经能回答「哪些分类花了多久」，年度报告要回答的是「这一年是怎么过的」。

| | 年视图（现状） | 年度报告（新增） |
| --- | --- | --- |
| 目的 | 查账：分类、占比、环比、下钻 | 复盘：节奏、习惯、极值、年度总结 |
| 形式 | 常驻页签，可交互环图 | 整屏浮层，纵向数据卡片 |
| 时间口径 | 任意年份，逐级下钻 | 以「年」为单位，一次性聚合 |
| 输出 | 留在应用内 | 可分享文本，后续可保存摘要图 |
| 复用 | `StatsBuilder`、环图、分类行 | 复用统计口径与主题，不重复画环图 |

设计原则：**年视图回答「花在哪」，年报回答「这一年长什么样」**。年报不画第二个环图，改用数字、月度柱和短文案，避免两页信息重复。既然定位是数据向，文案只陈述事实，不做抒情式总结。

## 2. 功能设计

### 2.1 入口

- **常驻入口**：统计页切到「年」且年内有记录时，在环图上方显示入口卡片：「2026 年度报告 · 查看 →」。
- **自动入口**：每年 **12 月 20 日 – 次年 1 月 10 日**，应用启动后首次进入主界面时自动打开年报浮层：
  - 12/20–12/31 展示当年（进行中，标注截至日期）；
  - 1/1–1/10 展示上一年（完整报告）。
  - 同一目标年份只自动弹一次，`SharedPreferences` 记 `annual_auto_shown_year`；用户关闭或点「先不看」后写入该年份，不再重复。
  - 受平台限制，**只在应用被打开时弹**：不做系统通知、后台唤起或 WorkManager（应用无通知权限，也不值得为一次提醒引入新依赖）。
- **报告内年份切换**：顶部左右箭头（‹ 2026 ›），只在「有记录的年份」之间移动，默认落在当前年；当前年没有记录时落到最近一个有记录的年份。
- **不加进「⋮」菜单**，避免菜单膨胀。
- 打开方式沿用现有整屏浮层：`MainActivity` 增加 `OVERLAY_ANNUAL`，左上角关闭、`BackHandler` 返回。

### 2.2 页面结构（纵向滚动，8 个区块）

| # | 区块 | 内容 | 数据来源 |
| --- | --- | --- | --- |
| 1 | 封面 | 「2026」大字、全年总时长、「時 · 年度报告」；当前年份加「进行中 · 截至 11 月 22 日」 | 年度总量、`now` |
| 2 | 总览 | 记录天数、记录条数、平均每个记录日、覆盖率 | `activeDays`、`coveredMillis / elapsedMillis` |
| 3 | 时间去向 | Top 5 根分类：色点 + 名称 + 时长 + 占比条 + 同比 | `StatsSummary.roots`、`CategoryUsage.previousMillis` |
| 4 | 十二个月 | 12 根柱，标出最忙的月份 | `monthMillis[12]` |
| 5 | 记录习惯 | 最早开始、最晚结束、最长一次、最长连续天数、最常开始的时段 | 极值 / `streak` / 小时桶 |
| 6 | 四季 | 春 / 夏 / 秋 / 冬 用时占比，沿用节气色板的四组色系 | 月桶按自然月归并 |
| 7 | 与去年相比 | 总时长变化 ±%、变化最大的分类 | `previousTotalMillis`、`deltaRatio` |
| 8 | 结尾 | 一句数据化小结 +「分享文本」（P1）/「保存摘要图」（P2） | 规则文案 |

文案示例（保持现有应用的克制语气）：

- 封面：「2026 · 你记录了 1,284 小时」
- 总览：「312 天有记录 · 平均每个记录日 4 小时 7 分」
- 时间去向：「最多的时间给了 工作 / 深度，占 42%」
- 十二个月：「最忙的是 10 月」
- 记录习惯：「最长一次 6 小时 12 分 · 连续 23 天有记录」
- 四季：「秋天记录得最多」
- 同比：「比去年同期多 18%」

### 2.3 空态与降级

| 情况 | 表现 |
| --- | --- |
| 全年无记录 | 不渲染空报告，显示「2026 年还没有记录」+「去记录」返回时间轴 |
| 自动弹窗命中但目标年无记录 | 不自动弹出，避免打开空报告；用户仍可从年视图入口进入 |
| 只有 1–2 天记录 | 隐藏「连续天数」「最常时段」等弱信号卡片，其余照常 |
| 去年无数据 | 隐藏同比区块，不显示「新增 100%」这类噪音 |
| 分类已被删除 | 历史事件可能已被迁移或删除；残留的未知分类按 `StatsBuilder` 现有规则并入「未知分类」 |
| 当前年份 | 所有数值裁剪到「现在」，封面标注进行中 |

### 2.4 视觉与可访问性

- 复用现有 `Paper / Ink / Pine / Hairline` 与深色主题，不引入新色板。
- 占比条与柱状图同时带文字数值，不依赖颜色单独传达信息。
- 所有卡片在系统字体放大到 1.3× 时允许换行；数字与单位分开排版，避免「1,284小时」挤成一行。
- 封面与结尾留白多于统计页，突出年度感；数据向定位下不加大图或插画。

## 3. 数据与口径

### 3.1 现有能力与缺口

可以直接复用：

- `StatsWindows.of(StatsMode.YEAR, date, zone)`：本地时区年窗口。
- `StatsBuilder.build(events, tree, window, now, zone)`：年度总量、覆盖率、根分类 Top、以及**已对齐的去年同期**（`previousWindowOf` 会把去年窗口裁到与今年相同的已过时长）。
- `CategoryUsage.previousMillis / deltaRatio`：分类级同比。
- `DateTimeFormat.durationText`：时长文案。

需要新增：

- 十二个月分桶、按日分桶（记录天数、最忙一天、连续天数）。
- 开始小时分布（最常几点开始记录）。
- 极值：最早开始、最晚结束、最长一次记录。
- 四季归并（按自然月）。
- 有记录年份列表（从事件首尾推导，供年份切换，不新增 DAO 查询）。

### 3.2 领域模型草案

```kotlin
// domain/annual/AnnualReport.kt
data class AnnualReport(
    val year: Int,
    val isPartial: Boolean,          // 当前年且尚未结束
    val throughDate: LocalDate,      // 裁剪到的日期
    val totalMillis: Long,           // 事件累计，重叠重复计
    val coveredMillis: Long,         // 并集，重叠只算一次
    val elapsedMillis: Long,         // 已过时长，覆盖率分母
    val eventCount: Int,
    val activeDays: Int,             // 至少一条记录的本地日
    val avgPerActiveDayMillis: Long,
    val roots: List<CategoryUsage>,  // Top N，来自 StatsBuilder
    val months: List<Long>,          // 12 个月，单位毫秒
    val hourStarts: List<Int>,       // 24 个桶，按开始时刻计数
    val longestEvent: NotableEvent?, // 年内裁剪后的最长一次
    val earliestStart: LocalTime?,
    val latestEnd: LocalTime?,
    val longestStreakDays: Int,
    val busiestDay: DayUsage?,
    val seasons: List<SeasonUsage>,  // 春 / 夏 / 秋 / 冬
    val previousTotalMillis: Long,   // 已对齐去年同期
    val previousTop: List<CategoryUsage> = emptyList(), // P1：去年 Top，用于「掉出榜单」
)
```

### 3.3 计算流程

1. 年窗口：`StatsWindows.of(StatsMode.YEAR, LocalDate.of(year, 1, 1), zone)`。
2. 复用 `StatsBuilder.build` 得到总量、覆盖率、分类 Top、对齐后的去年同期。
3. 对年窗口内事件做**一次**裁剪遍历，同时填充：月份桶、日期桶、开始小时桶、极值、最长记录。
4. 由日期桶派生：记录天数、平均、连续天数、最忙一天。
5. 由月份桶派生：四季分布、最忙月。
6. 全部在 `Dispatchers.Default` 上执行。

关键点：步骤 2 与步骤 3 的裁剪口径必须一致。现在 `StatsBuilder.clip` 是 private，建议抽出 `domain/EventWindow.kt`：

```kotlin
internal data class ClippedEvent(val event: TimelineEvent, val start: Long, val end: Long)

internal fun clipTo(events: List<TimelineEvent>, from: Long, until: Long): List<ClippedEvent>
```

`StatsBuilder` 改用它，`AnnualReportBuilder` 也用它，避免两份裁剪逻辑日后漂移。现有统计单测可以保护这次重构。

### 3.4 口径表（必须在实现和文档里保持一致）

| 指标 | 定义 | 边界处理 |
| --- | --- | --- |
| 总时长 | 每条事件在年内的裁剪时长之和 | 重叠记录重复计入，可能大于墙钟 |
| 覆盖时长 | 事件区间并集 | 重叠只算一次，与统计页一致 |
| 记录天数 | 有任意一段记录覆盖的本地日历日 | 跨天记录两天都算 |
| 平均每个记录日 | 总时长 ÷ 记录天数 | 记录天数为 0 时显示「—」 |
| 覆盖率 | 覆盖时长 ÷ 已过时长 | 未来年份不展示 |
| 连续天数 | 相邻且有记录的日历日最长的一段 | 年内计算，不跨年延伸 |
| 最忙一天 | 单日裁剪时长最大的一天 | 并列取更早的一天 |
| 最长一次 | 年内裁剪时长最长的一条事件 | 并列取开始更早的一条 |
| 最早开始 / 最晚结束 | 记录时刻在一天中的极值 | 最晚结束只统计当日结束的记录，避免跨天记录把「最晚」变成 00:30 |
| 最常开始的时段 | 按开始时刻所在小时计数 | 跨年事件用今年内的开始时刻 |
| 四季 | 3–5 月春、6–8 月夏、9–11 月秋、12/1/2 月冬 | **按自然月划分**，不是节气天文日期 |
| 同比 | 与去年同长度窗口比较 | 当前年对齐到已过时长；去年无数据不展示 |

### 3.5 明确不做（数据不支持或代价过高）

- **分类变迁史**：事件只存当前 `categoryId`，分类被删除时事件会被迁移或删除，改名后历史也统一显示新名字。因此不做「去年有、今年消失的分类」这类叙事，除非新增分类快照表（不划算）。
- **节气日期**：现有 `SolarTermPalette` 只有颜色和名称，没有节气日期，算节气需要天文算法。年报只做四季。
- **备注词频 / 关键词**：中文分词没有可靠的无依赖方案，2-gram 质量不可控，且涉及隐私。P3 再评估。
- **系统通知 / 后台唤起**：应用没有通知权限，也不引入 WorkManager；「年末自动弹出」改为应用启动时的本地判定，不涉及后台调度。
- **新的图表库**：柱状图和占比条用 Compose Canvas / Box 即可。

## 4. 技术实现

### 4.1 domain 层

- `domain/EventWindow.kt`：抽出公共裁剪函数，供统计与年报共用。
- `domain/annual/AnnualReport.kt`：模型与派生字段（`isPartial`、`avgPerActiveDayMillis` 等用 `val` 计算属性）。
- `domain/annual/AnnualReportBuilder.kt`：纯函数，签名与 `StatsBuilder.build` 风格一致，所有时间输入显式传入（`year`、`now`、`zone`）。
- `domain/annual/Streak.kt`（可选）：连续天数单独成文件，便于单测。
- `util/DateTimeFormat.kt`：增加 `durationTextShort`（超过 100 小时显示「1,284 小时」，不再带分钟）与千分位格式化，供封面和卡片使用。

纯函数、无 Android 依赖，可直接在 JVM 单测里覆盖。

### 4.2 UI 层

- `ui/annual/AnnualReportViewModel.kt`
  - `selectedYear` 默认当前年；`repository.timeline` + `repository.categoryTree` + 时钟 + 年份合成 `StateFlow`。
  - `availableYears` 直接从 `timeline` 的首尾事件推出（最早记录年到最晚记录年，倒序，忽略未来年份），不新增 DAO 查询。
  - 时钟按**本地日边界**推进，而不是每分钟：年报只有「裁剪到现在」随日期变化，按分钟重算一年数据是浪费。
  - `flowOn(Dispatchers.Default)`，`stateIn` 与统计页一致。
  - 自动弹窗的判定放在 `MainActivity`：读取 SharedPreferences 与当前日期，只负责「切换年份并打开 overlay」，不参与年报计算。
- `ui/annual/AnnualReportScreen.kt`：浮层骨架、顶栏（关闭 / 年份切换）、各区块顺序。
- `ui/annual/AnnualReportSections.kt`：总览、分类、四季、同比卡片。
- `ui/annual/MonthBars.kt`：12 个月柱状图（Canvas），附带最忙月标注。
- `ui/annual/AnnualShareCard.kt`（P2）：固定 1080×1920 的摘要卡，供截图分享。
- 修改 `ui/stats/StatisticsScreen.kt`：年模式入口卡片（新增 `onOpenAnnualReport` 参数）。
- 修改 `MainActivity.kt`：新增 ViewModel、overlay 常量、浮层分支与年末自动弹出判定。

### 4.3 性能

- 数据源是现成的 `repository.timeline`（全量事件流），不新增 DAO 查询；年窗口内事件通常几千条，`O(N)` 遍历在 Default 线程是毫秒级。
- 分类 Top 复用 `StatsBuilder` 的一次年度聚合，不按 12 个月各调一次。
- 只在事件变化、年份变化、跨日时重算；滚动不触发计算。
- 若未来出现 5 万条以上的极端数据，再加「只查年窗口列」的 DAO 投影或按年缓存，P2 之前不做。

### 4.4 分享

- **P1 文本分享**：把报告拼成一段纯文本（总量、Top 分类、最忙月、连续天数、同比），走 `Intent.ACTION_SEND` + `text/plain`；零权限、无新依赖。
- **P2 长图**：用 Compose 1.9（BOM 2025.08.01）的 `rememberGraphicsLayer()` + `toImageBitmap()` 把**离屏摘要卡**画成位图，不截屏幕长图。
  - 固定 1080×1920，内容只含数字与分类名，**默认不包含备注原文**。
  - 写入 `cacheDir/share/`，通过 `FileProvider` + `Intent.ACTION_SEND` 分享；不申请存储权限。
  - 若长图成本超出预期，文本分享已经可用，长图可以独立后置。

### 4.5 年末自动弹出

判定逻辑放在 `MainActivity`，纯本地、无权限：

```kotlin
val today = LocalDate.now(zone)
val inWindow = (today.monthValue == 12 && today.dayOfMonth >= 20) ||
    (today.monthValue == 1 && today.dayOfMonth <= 10)
val targetYear = if (today.monthValue == 1) today.year - 1 else today.year
val alreadyShown = prefs.getInt(KEY_ANNUAL_SHOWN_YEAR, -1) == targetYear
// events 用 MainActivity 已收集的 homeState.events，不额外查库。
val hasData = events.any { event ->
    val startYear = Instant.ofEpochMilli(event.event.startAt).atZone(zone).year
    val endYear = Instant.ofEpochMilli(event.event.endAt - 1).atZone(zone).year
    targetYear in startYear..endYear
}
if (inWindow && !alreadyShown && hasData) {
    annualViewModel.selectYear(targetYear)
    overlay = OVERLAY_ANNUAL
    prefs.edit { putInt(KEY_ANNUAL_SHOWN_YEAR, targetYear) }
}
```

- 只在启动后的首次组合时判定一次，避免重组反复触发。
- 「同一目标年一次」在弹窗打开时即写入，关闭、返回、点「先不看」都不会让它在同一年再次弹出。
- 目标年没有记录时直接跳过，不自动打开空报告。
- 不做「每年 1 月 1 日 00:00 精确闹钟」：没有后台调度权限，也不值得为一次提醒引入 WorkManager。
- 如果后续觉得打扰，可以在报告结尾加一个「以后不再自动弹出」开关（一处偏好 + 一行文案），成本很小，P1 视反馈决定。

### 4.6 文件清单

| 文件 | 变更 | 内容 | 预估 |
| --- | --- | --- | --- |
| `domain/EventWindow.kt` | 新增 | 公共裁剪函数 | 1.5 KiB |
| `domain/Statistics.kt` | 修改 | 改用公共裁剪，去掉私有 `clip` | ±20 行 |
| `domain/annual/AnnualReport.kt` | 新增 | 模型与派生字段 | 5 KiB |
| `domain/annual/AnnualReportBuilder.kt` | 新增 | 分桶、极值、streak、四季 | 10 KiB |
| `ui/annual/AnnualReportViewModel.kt` | 新增 | 状态、年份、日边界时钟 | 5 KiB |
| `ui/annual/AnnualReportScreen.kt` | 新增 | 浮层骨架与区块编排 | 8 KiB |
| `ui/annual/AnnualReportSections.kt` | 新增 | 总览 / 分类 / 四季 / 同比 | 10 KiB |
| `ui/annual/MonthBars.kt` | 新增 | 12 月柱状图 | 5 KiB |
| `ui/annual/AnnualShareCard.kt` | 新增（P2） | 1080×1920 摘要卡 | 7 KiB |
| `ui/annual/AnnualReportShare.kt` | 新增（P1/P2） | 文本拼接、位图落盘与 ACTION_SEND | 4 KiB |
| `ui/stats/StatisticsScreen.kt` | 修改 | 年模式入口卡片 | +30 行 |
| `MainActivity.kt` | 修改 | overlay、ViewModel、浮层、自动弹出 | +60 行 |
| `AndroidManifest.xml` / `res/xml/file_paths.xml` | 修改（P2） | FileProvider | 15 行 |
| `test/.../AnnualReportBuilderTest.kt` | 新增 | 口径与边界 | 10 KiB |
| `docs/QA.md`、`README.md` | 修改 | 验收项与功能说明 | — |

注意仓库的 20 KiB 代码文件预提交检查：上面的 UI 文件已按职责拆开，单个文件不要超过上限。

## 5. 分期与工作量

| 阶段 | 范围 | 产出 | 预估 |
| --- | --- | --- | --- |
| P0 内核 | 年窗口、公共裁剪、年报模型与 builder、总览 + 分类 Top + 12 月柱、统计页入口、历史年份切换、单测 | 可用的年度报告（只读、可翻年） | 2 人日 |
| P1 完整报告 | 记录习惯卡片、四季、同比、空态与降级、文本分享、年末自动弹出 | 完整可分享文字版的年度报告 | 1.5 人日 |
| P2 分享图 | 1080×1920 摘要卡、FileProvider、保存/分享 | 可保存/分享的摘要图 | 1 人日 |
| 收尾 | README / QA / 手动验收 / 版本 0.4.0 | 发布 | 0.5 人日 |

合计约 **5 人日**（单人）。只做 P0 约 2 人日，即可上线一个「能看、能翻年」的年度报告。

## 6. 测试与验收

### 6.1 JVM 单测（新增 `AnnualReportBuilderTest`）

- 年窗口边界：1 月 1 日 00:00 与 12 月 31 日 23:59 的记录各归其年。
- 闰年：2024-02-29 的记录计入 2 月，长度正确。
- 跨年事件：12 月 31 日跨到 1 月 1 日，两年各计裁剪部分，且月桶之和等于总时长。
- 裁剪到「现在」：当前年未来日期的事件不计入。
- 空年：所有数值为 0，不抛异常、不出现除零。
- 记录天数：同日多条只算一天；跨天记录两天都算。
- 连续天数：跨月连续、断档、只有一天三种情况。
- 极值：最长一次、最忙一天并列时的选择稳定。
- 小时桶：按开始时刻归桶，跨年事件用年内开始时刻。
- 四季：12 月归冬、3 月归春，四季之和等于总时长。
- 同比：去年无数据时为 0；当前年对齐到已过时长。
- 时区：`America/New_York` 的 DST 日不出现多算 / 少算。
- 年份列表：跨年记录同时出现在两个年份；未来年份被忽略；默认年份选择稳定。

复用 `StatisticsTestFixtures` 的 `at()` 与固定 `zone`，保持与现有统计测试同样的写法。

### 6.2 手动验收（补进 `docs/QA.md`）

- [ ] 统计页年模式显示入口卡片；日 / 周 / 月 / 自定义模式不显示。
- [ ] 入口打开年度报告，返回键与左上角都能关闭，回到统计页原状态。
- [ ] 当前年份封面显示「进行中 · 截至 M 月 d 日」，数值与年视图总时长一致。
- [ ] 12 个月柱状图之和等于年视图总时长（四舍五入误差内）。
- [ ] 分类 Top 的顺序、颜色、占比与年视图分类列表一致。
- [ ] 去年无数据时同比区块隐藏；有数据时符号与颜色正确（红增绿减沿用现有约定）。
- [ ] 年份切换只在有记录的年份之间移动；当前年 / 历史年 / 空年表现正确。
- [ ] 12 月 20 日–次年 1 月 10 日首次启动自动弹出对应年份；关闭后再次启动不重复。
- [ ] 非窗口期不自动弹；目标年无记录不自动弹。
- [ ] 深色模式、系统字体 1.3×、窄屏下卡片不溢出。
- [ ] 空年份显示空态，不出现 NaN / Infinity / 空白卡片。
- [ ] 无网络、无新权限；文本分享内容正确，长图不含备注原文（P2）。

### 6.3 CI

沿用现有：`./gradlew testDebugUnitTest lintDebug assembleDebug`。新增测试计入 QA 里的用例总数。

## 7. 风险与对策

| 风险 | 影响 | 对策 |
| --- | --- | --- |
| 口径与年视图不一致 | 用户发现两个页面数字对不上 | 复用 `StatsBuilder` 与公共裁剪；单测锁定「月桶之和 = 总时长」 |
| 重叠记录被误解 | 「我一年怎么会有 9000 小时」 | 封面用覆盖时长做辅助说明；总览卡片写明「记录时长可重叠」 |
| 分类删除导致历史不可追溯 | 年报讲不出「消失的分类」 | 方案里明确不做分类变迁；只呈现当前分类树与「未知分类」 |
| 自动弹出打扰用户 | 用户反感 | 只在年末窗口弹一次；目标年无记录不弹；提供「先不看」；必要时加永久关闭开关 |
| 分享图过大 OOM | 分享失败 | 固定 1080×1920 摘要卡，不做整页长图 |
| 年度聚合拖慢统计页 | 进入统计页卡顿 | 年报独立 ViewModel 与订阅，不在统计页计算；按日边界刷新 |
| 文件超 20 KiB | 提交被 pre-commit 拦截 | 按区块拆分文件，单文件控制在 15 KiB 以内 |

## 8. 已确认的决策

| 决策 | 选择 | 对方案的影响 |
| --- | --- | --- |
| 定位 | 数据向年度报告 | 以数字、占比、月度节奏为主；文案只陈述事实，不做抒情式年终回顾 |
| 入口 | 年末自动弹出 + 年视图常驻入口 | 新增 4.5 的启动判定；只在应用打开时生效，同一目标年一次，不引入通知与后台调度 |
| 分享 | 先文本后长图 | 文本分享进 P1，依赖为零；长图留在 P2，用离屏摘要卡实现 |
| 年份 | 支持切换历史年份 | 年份切换进 P0；`availableYears` 从事件首尾推导，不新增数据库查询 |

这样定下来后，P0 只依赖现有数据与统计口径，不碰数据库、不碰权限；P1 的两项（文本分享、年末弹出）都是本地能力；P2 的长图是唯一需要新增 `FileProvider` 的部分。

## 附录 A：核心计算伪代码

```kotlin
fun build(events: List<TimelineEvent>, tree: CategoryTree, year: Int, now: Long, zone: ZoneId): AnnualReport {
    val window = StatsWindows.of(StatsMode.YEAR, LocalDate.of(year, 1, 1), zone)
    val summary = StatsBuilder.build(events, tree, window, now, zone)

    val until = minOf(window.endAt, now)
    val clipped = clipTo(events, window.startAt, until)

    val monthMillis = LongArray(12)
    val dayMillis = HashMap<LocalDate, Long>()
    val hourStarts = IntArray(24)
    var longest: ClippedEvent? = null
    var earliest: LocalTime? = null
    var latestEnd: LocalTime? = null

    clipped.forEach { item ->
        var cursor = item.start
        while (cursor < item.end) {
            val date = StatsWindows.dateOf(cursor, zone)
            val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val segmentEnd = minOf(item.end, dayEnd)
            val duration = segmentEnd - cursor
            monthMillis[date.monthValue - 1] += duration
            dayMillis[date] = (dayMillis[date] ?: 0L) + duration
            cursor = segmentEnd
        }
        val startHour = Instant.ofEpochMilli(item.start).atZone(zone).hour
        hourStarts[startHour]++
        // earliest / latestEnd / longest 在此更新
    }

    val dates = dayMillis.keys.sorted()
    val streak = longestStreak(dates) // 相邻日期的最长连续段
    // months -> seasons、dayMillis -> busiestDay / activeDays / average
    // 组装 AnnualReport，previousTotalMillis 直接用 summary.previousTotalMillis
}
```

## 附录 B：一句话总结文案规则

- 同比变化 ≥ 5%：「比去年同期多 / 少 X%」。
- 同比在 ±5% 以内：「和去年差不多」。
- 去年无数据：不展示同比，改为「这是第一份年度报告」。
- 全年只有零星几天：用「记录刚刚开始」，不强行升华。
- 所有文案只引用报告里已有的数字，不引入主观评价。
