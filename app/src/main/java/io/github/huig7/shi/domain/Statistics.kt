package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.TimelineEvent
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** 统计页的 5 种查看方式：日 / 周 / 月 / 年 / 自定义。 */
enum class StatsMode { DAY, WEEK, MONTH, YEAR, CUSTOM }

/** 统计窗口，半开区间 [startAt, endAt)，时间戳按本地时区解释。 */
data class StatsWindow(
    val mode: StatsMode,
    val startAt: Long,
    val endAt: Long,
)

/**
 * 由 [StatsMode] 推出具体窗口，并支持前后翻页。
 *
 * 全部计算都依赖显式传入的 [ZoneId]，不读取系统时区。
 */
object StatsWindows {

    /**
     * [anchor] 所在周期的窗口。
     *
     * - DAY：当天
     * - WEEK：周一 00:00 到下周一 00:00（以周一为一周首日）
     * - MONTH：当月 1 日到下月 1 日
     * - YEAR：当年 1 月 1 日到次年 1 月 1 日
     * - CUSTOM：没有自然周期，按 [anchor] 单日返回；自定义范围请用 [custom]
     */
    fun of(mode: StatsMode, anchor: LocalDate, zone: ZoneId): StatsWindow = when (mode) {
        StatsMode.DAY -> window(StatsMode.DAY, anchor, anchor.plusDays(1), zone)
        StatsMode.WEEK -> {
            val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            window(StatsMode.WEEK, monday, monday.plusWeeks(1), zone)
        }
        StatsMode.MONTH -> {
            val firstDay = anchor.withDayOfMonth(1)
            window(StatsMode.MONTH, firstDay, firstDay.plusMonths(1), zone)
        }
        StatsMode.YEAR -> {
            val firstDay = anchor.withDayOfYear(1)
            window(StatsMode.YEAR, firstDay, firstDay.plusYears(1), zone)
        }
        StatsMode.CUSTOM -> window(StatsMode.CUSTOM, anchor, anchor.plusDays(1), zone)
    }

    /** 闭区间日期 [startDate]..[endDate] 对应的自定义窗口；结束早于起始时自动交换。 */
    fun custom(startDate: LocalDate, endDate: LocalDate, zone: ZoneId): StatsWindow {
        val start = if (endDate.isBefore(startDate)) endDate else startDate
        val end = if (endDate.isBefore(startDate)) startDate else endDate
        return window(StatsMode.CUSTOM, start, end.plusDays(1), zone)
    }

    /**
     * 前后平移 [steps] 个周期（负数向前）。
     *
     * DAY / WEEK / MONTH / YEAR 按窗口起始日做日历加减，CUSTOM 按本地日历天数平移。
     */
    fun shift(window: StatsWindow, steps: Int, zone: ZoneId): StatsWindow {
        if (steps == 0) return window
        return when (window.mode) {
            StatsMode.DAY -> of(StatsMode.DAY, dateOf(window.startAt, zone).plusDays(steps.toLong()), zone)
            StatsMode.WEEK -> of(StatsMode.WEEK, dateOf(window.startAt, zone).plusWeeks(steps.toLong()), zone)
            StatsMode.MONTH -> of(StatsMode.MONTH, dateOf(window.startAt, zone).plusMonths(steps.toLong()), zone)
            StatsMode.YEAR -> of(StatsMode.YEAR, dateOf(window.startAt, zone).plusYears(steps.toLong()), zone)
            StatsMode.CUSTOM -> {
                val startDate = dateOf(window.startAt, zone)
                val endDate = dateOf(window.endAt - 1L, zone).plusDays(1)
                val days = ChronoUnit.DAYS.between(startDate, endDate)
                val shiftedStart = startDate.plusDays(days * steps)
                val shiftedEnd = endDate.plusDays(days * steps)
                StatsWindow(
                    mode = StatsMode.CUSTOM,
                    startAt = startOfDay(shiftedStart, zone),
                    endAt = startOfDay(shiftedEnd, zone),
                )
            }
        }
    }

    /** 本地日期对应的当天零点时间戳。 */
    internal fun startOfDay(date: LocalDate, zone: ZoneId): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    /** 时间戳在 [zone] 下的本地日期。 */
    internal fun dateOf(epochMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    private fun window(
        mode: StatsMode,
        startDate: LocalDate,
        endDate: LocalDate,
        zone: ZoneId,
    ): StatsWindow = StatsWindow(mode, startOfDay(startDate, zone), startOfDay(endDate, zone))
}

/**
 * 一个分类（含全部子类）在统计窗口内的用时。
 *
 * 环形图只画前 [StatsBuilder.MAX_DEPTH] 层，[children] 也按此截断；
 * 更深的分类时长已计入所属祖先的 [millis]。
 * 下钻时 [depth] 相对当前聚焦分类从 0 重新计数。
 */
data class CategoryUsage(
    val categoryId: Long,
    val name: String,
    val colorHex: Long,
    val millis: Long,
    val eventCount: Int,
    val depth: Int,
    /** 与当前窗口等长、同模式的上一个窗口里同一分类（含子类）的总时长。 */
    val previousMillis: Long = 0L,
    val children: List<CategoryUsage> = emptyList(),
) {
    /** 环比变化；[previousMillis] 为 0（新增分类）时为 null。 */
    val deltaRatio: Float?
        get() = if (previousMillis > 0L) {
            (millis - previousMillis).toFloat() / previousMillis
        } else {
            null
        }
}

/** 统计结果：窗口、总量与按层级组织的分类用时。 */
data class StatsSummary(
    val window: StatsWindow,
    val totalMillis: Long,
    val eventCount: Int,
    /** 根分类（depth = 0），按用时降序；tree 中不存在的分类合成在最后。 */
    val roots: List<CategoryUsage>,
    /** 等长同模式上一个窗口的总时长（对齐已过时长，见 [StatsBuilder.build]）。 */
    val previousTotalMillis: Long,
    /**
     * 下钻聚焦的分类 ID；null 表示根层（全部分类）。
     *
     * 聚焦时 [totalMillis]、[eventCount]、[previousTotalMillis] 都换成该分类子树的口径，
     * [roots] 是它的直接子分类（depth 从 0 重新计数），tree 中不存在的分类不参与下钻统计。
     */
    val focusCategoryId: Long? = null,
)

/**
 * 把事件列表聚合成 [StatsSummary]。
 *
 * 纯函数：所有时间相关输入（[window]、[now]、[zone]）都由参数传入。
 */
object StatsBuilder {

    /** 环形图最多呈现三层：根分类、二级、三级。 */
    const val MAX_DEPTH: Int = 3

    /** tree 中找不到分类时，合成根使用的分类 ID。 */
    const val ORPHAN_CATEGORY_ID: Long = -1L

    private const val FALLBACK_COLOR = 0xFF9AA29A
    private const val PATH_SEPARATOR = " / "
    private const val UNKNOWN_NAME = "未分类"

    /**
     * 把事件列表聚合成 [StatsSummary]。
     *
     * [rootId] 为 null 时统计全部分类（旧行为）；传入分类 ID 时下钻到该分类的「下一级」：
     * 总量、记录数与环比都只算该分类子树，[StatsSummary.roots] 换成它的直接子分类。
     * [rootId] 在 [tree] 中找不到时按根层处理，避免界面停留在空白的下钻状态。
     */
    fun build(
        events: List<TimelineEvent>,
        tree: CategoryTree,
        window: StatsWindow,
        now: Long,
        zone: ZoneId,
        rootId: Long? = null,
    ): StatsSummary {
        val current = aggregate(clip(events, window.startAt, minOf(window.endAt, now)), tree)
        val previousWindow = previousWindowOf(window, now, zone)
        val previous = aggregate(
            clip(events, previousWindow.startAt, minOf(previousWindow.endAt, now)),
            tree,
        )

        val currentTotals = subtreeTotals(tree, current)
        val previousTotals = subtreeTotals(tree, previous)

        val focus = rootId?.let(tree::find)
        if (focus == null) {
            val roots = tree.roots
                .mapNotNull { node -> usageOf(node, 0, currentTotals, previousTotals) }
                .sortedWith(USAGE_ORDER)

            return StatsSummary(
                window = window,
                totalMillis = current.totalMillis,
                eventCount = current.eventCount,
                roots = roots + orphanUsages(current, previous),
                previousTotalMillis = previous.totalMillis,
            )
        }

        // 下钻：环图与列表都从聚焦分类的直接子分类开始，层级深度归零。
        val childRoots = focus.children
            .mapNotNull { node -> usageOf(node, 0, currentTotals, previousTotals) }
            .sortedWith(USAGE_ORDER)

        return StatsSummary(
            window = window,
            totalMillis = currentTotals.millis[focus.category.id] ?: 0L,
            eventCount = currentTotals.counts[focus.category.id] ?: 0,
            roots = childRoots,
            previousTotalMillis = previousTotals.millis[focus.category.id] ?: 0L,
            focusCategoryId = focus.category.id,
        )
    }

    /** 与 [window] 等长、同模式的上一个窗口；当前窗口包含 [now] 时对齐到已过时长。 */
    private fun previousWindowOf(window: StatsWindow, now: Long, zone: ZoneId): StatsWindow {
        val previous = StatsWindows.shift(window, -1, zone)
        if (now < window.startAt || now >= window.endAt) return previous
        val elapsed = now - window.startAt
        val alignedEnd = previous.startAt + elapsed
        return if (alignedEnd < previous.endAt) previous.copy(endAt = alignedEnd) else previous
    }

    private fun clip(events: List<TimelineEvent>, from: Long, until: Long): List<ClippedEvent> {
        if (until <= from) return emptyList()
        return events.mapNotNull { event ->
            val start = maxOf(event.event.startAt, from)
            val end = minOf(event.event.startAt + event.event.durationMillis, until)
            if (end <= start) null else ClippedEvent(event, start, end)
        }
    }

    private fun aggregate(clipped: List<ClippedEvent>, tree: CategoryTree): Aggregate {
        val directMillis = HashMap<Long, Long>()
        val directCounts = HashMap<Long, Int>()
        val orphanMillis = HashMap<String, Long>()
        val orphanCounts = HashMap<String, Int>()
        val orphanColors = HashMap<String, Long>()
        var totalMillis = 0L

        clipped
            .sortedWith(compareBy({ it.event.event.startAt }, { it.event.event.id }))
            .forEach { item ->
                val duration = item.end - item.start
                totalMillis += duration
                val categoryId = item.event.event.categoryId
                if (tree.find(categoryId) != null) {
                    directMillis[categoryId] = (directMillis[categoryId] ?: 0L) + duration
                    directCounts[categoryId] = (directCounts[categoryId] ?: 0) + 1
                } else {
                    val name = orphanName(item.event.categoryPath)
                    orphanMillis[name] = (orphanMillis[name] ?: 0L) + duration
                    orphanCounts[name] = (orphanCounts[name] ?: 0) + 1
                    orphanColors.putIfAbsent(name, item.event.categoryColor)
                }
            }

        return Aggregate(
            directMillis = directMillis,
            directCounts = directCounts,
            orphanMillis = orphanMillis,
            orphanCounts = orphanCounts,
            orphanColors = orphanColors,
            totalMillis = totalMillis,
            eventCount = clipped.size,
        )
    }

    /** 沿分类树自下而上累计时长与次数（含全部层级，供环形图的最内层使用）。 */
    private fun subtreeTotals(tree: CategoryTree, aggregate: Aggregate): SubtreeTotals {
        val millis = HashMap<Long, Long>()
        val counts = HashMap<Long, Int>()

        fun walk(node: CategoryNode): Pair<Long, Int> {
            var totalMillis = aggregate.directMillis[node.category.id] ?: 0L
            var totalCount = aggregate.directCounts[node.category.id] ?: 0
            node.children.forEach { child ->
                val (childMillis, childCount) = walk(child)
                totalMillis += childMillis
                totalCount += childCount
            }
            millis[node.category.id] = totalMillis
            counts[node.category.id] = totalCount
            return totalMillis to totalCount
        }

        tree.roots.forEach { walk(it) }
        return SubtreeTotals(millis, counts)
    }

    private fun usageOf(
        node: CategoryNode,
        depth: Int,
        totals: SubtreeTotals,
        previousTotals: SubtreeTotals,
    ): CategoryUsage? {
        val millis = totals.millis[node.category.id] ?: 0L
        if (millis <= 0L) return null
        val children = if (depth < MAX_DEPTH - 1) {
            node.children
                .mapNotNull { child -> usageOf(child, depth + 1, totals, previousTotals) }
                .sortedWith(USAGE_ORDER)
        } else {
            emptyList()
        }
        return CategoryUsage(
            categoryId = node.category.id,
            name = node.category.name,
            colorHex = node.category.colorHex,
            millis = millis,
            eventCount = totals.counts[node.category.id] ?: 0,
            depth = depth,
            previousMillis = previousTotals.millis[node.category.id] ?: 0L,
            children = children,
        )
    }

    private fun orphanUsages(current: Aggregate, previous: Aggregate): List<CategoryUsage> =
        current.orphanMillis.map { (name, millis) ->
            CategoryUsage(
                categoryId = ORPHAN_CATEGORY_ID,
                name = name,
                colorHex = current.orphanColors[name] ?: FALLBACK_COLOR,
                millis = millis,
                eventCount = current.orphanCounts[name] ?: 0,
                depth = 0,
                previousMillis = previous.orphanMillis[name] ?: 0L,
                children = emptyList(),
            )
        }.sortedWith(USAGE_ORDER)

    private fun orphanName(categoryPath: String): String {
        val separator = categoryPath.indexOf(PATH_SEPARATOR)
        val head = if (separator < 0) categoryPath else categoryPath.substring(0, separator)
        return head.ifBlank { UNKNOWN_NAME }
    }
}

private data class ClippedEvent(val event: TimelineEvent, val start: Long, val end: Long)

private class Aggregate(
    val directMillis: Map<Long, Long>,
    val directCounts: Map<Long, Int>,
    val orphanMillis: Map<String, Long>,
    val orphanCounts: Map<String, Int>,
    val orphanColors: Map<String, Long>,
    val totalMillis: Long,
    val eventCount: Int,
)

private class SubtreeTotals(
    val millis: Map<Long, Long>,
    val counts: Map<Long, Int>,
)

/** 分类用时排序：时长降序，再看名称，保证渲染顺序稳定。 */
private val USAGE_ORDER: Comparator<CategoryUsage> =
    compareByDescending<CategoryUsage> { it.millis }.thenBy { it.name }
