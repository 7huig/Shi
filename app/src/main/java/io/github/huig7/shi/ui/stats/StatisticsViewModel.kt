package io.github.huig7.shi.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.domain.StatsBuilder
import io.github.huig7.shi.domain.StatsMode
import io.github.huig7.shi.domain.StatsSummary
import io.github.huig7.shi.domain.StatsWindow
import io.github.huig7.shi.domain.StatsWindows
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.util.DateTimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/** 面包屑的一节：从根分类到当前聚焦分类。 */
data class StatisticsCrumb(val categoryId: Long, val name: String)

/**
 * 统计页的展示状态。
 *
 * [window] 与 [summary] 在首帧聚合完成前为 null；[label] 跟随窗口（跨年等规则见 [statsLabel]）。
 */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val mode: StatsMode = StatsMode.DAY,
    val window: StatsWindow? = null,
    val label: String = "",
    val canGoNext: Boolean = false,
    val customRange: Pair<LocalDate, LocalDate>? = null,
    val summary: StatsSummary? = null,
    /** 下钻路径（自根分类到当前聚焦分类）；空表示根层。 */
    val focusPath: List<StatisticsCrumb> = emptyList(),
)

/**
 * 统计页 ViewModel：五种查看方式、前后翻页、自定义时间段。
 *
 * 事件与分类树都来自 [TimeLogRepository]，每分钟推进一次「现在」，
 * 让当前周期边界、「今天」前缀与记录条数口径保持新鲜。
 */
class StatisticsViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val selectedMode = MutableStateFlow(StatsMode.DAY)

    /** null 表示跟随「当前周期」（跨天后自动前移）；翻页后锁定到目标窗口的起始日。 */
    private val anchorDate = MutableStateFlow<LocalDate?>(null)

    private val selectedCustomRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)

    /** 下钻路径（分类 ID，自根到当前）；空表示根层。 */
    private val focusPath = MutableStateFlow<List<Long>>(emptyList())

    /** 每 60 秒推进一次「现在」；本轮订阅结束后由 stateIn 自动停止。 */
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(REFRESH_INTERVAL_MILLIS)
        }
    }

    private data class Controls(
        val mode: StatsMode,
        val anchor: LocalDate?,
        val customRange: Pair<LocalDate, LocalDate>?,
        /** 下钻路径（分类 ID，根 → 当前）；空表示根层。 */
        val focusPath: List<Long>,
    )

    private val controls = combine(
        selectedMode,
        anchorDate,
        selectedCustomRange,
        focusPath,
    ) { mode, anchor, customRange, path ->
        Controls(mode, anchor, customRange, path)
    }

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.timeline,
        repository.categoryTree,
        clock,
        controls,
    ) { events, tree, now, control ->
        val zone = DateTimeFormat.zone()
        val today = StatsWindows.dateOf(now, zone)
        val window = windowOf(control, today, zone)
        val summary = StatsBuilder.build(
            events = events,
            tree = tree,
            window = window,
            now = now,
            zone = zone,
            rootId = control.focusPath.lastOrNull(),
        )
        StatisticsUiState(
            isLoading = false,
            mode = window.mode,
            window = window,
            label = statsLabel(window, now, zone),
            canGoNext = StatsWindows.shift(window, 1, zone).startAt <= now,
            customRange = control.customRange,
            summary = summary,
            // 分类被删除等情况下 build 会退回根层，面包屑跟随实际生效的聚焦分类。
            focusPath = crumbPath(tree, summary.focusCategoryId),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
        initialValue = StatisticsUiState(),
    )

    /**
     * 切换查看方式。
     *
     * 日/周/月/年回到「当前周期」（锚点清空，跨天后自动前移）；自定义默认最近 7 天（含今天）。
     */
    fun selectMode(mode: StatsMode) {
        val zone = DateTimeFormat.zone()
        val today = StatsWindows.dateOf(System.currentTimeMillis(), zone)
        // 切换查看方式时回到根层，避免停留在旧窗口层级下的空白下钻。
        focusPath.value = emptyList()
        when (mode) {
            StatsMode.CUSTOM -> {
                selectedMode.value = StatsMode.CUSTOM
                selectedCustomRange.value = today.minusDays(DEFAULT_CUSTOM_DAYS - 1) to today
                anchorDate.value = null
            }
            else -> {
                selectedMode.value = mode
                // 切换查看方式时回到当前周期，不沿用上一个模式的锚点。
                anchorDate.value = null
            }
        }
    }

    /** 上一个周期；自定义窗口按本地日历天数平移。 */
    fun goPrevious() {
        val zone = DateTimeFormat.zone()
        val today = StatsWindows.dateOf(System.currentTimeMillis(), zone)
        applyWindow(StatsWindows.shift(currentWindow(zone, today), -1, zone), zone)
    }

    /** 下一个周期；目标窗口的开始时间还在未来时忽略，避免翻到空白的将来。 */
    fun goNext() {
        val zone = DateTimeFormat.zone()
        val now = System.currentTimeMillis()
        val today = StatsWindows.dateOf(now, zone)
        val target = StatsWindows.shift(currentWindow(zone, today), 1, zone)
        if (target.startAt > now) return
        applyWindow(target, zone)
    }

    /** 进入某个分类的直接下一级；点击行时只会传入当前层可下钻的分类。 */
    fun drillInto(categoryId: Long) {
        focusPath.value = focusPath.value + categoryId
    }

    /** 返回上一级分类；已在根层时不动。 */
    fun drillUp() {
        if (focusPath.value.isEmpty()) return
        focusPath.value = focusPath.value.dropLast(1)
    }

    /** 跳到面包屑的第 [level] 层（0 = 根层，n = 保留前 n 级）。 */
    fun focusAt(level: Int) {
        val target = level.coerceIn(0, focusPath.value.size)
        if (target == focusPath.value.size) return
        focusPath.value = focusPath.value.take(target)
    }

    /** 设置自定义时间段（闭区间，含首尾两天）；结束早于起始时自动交换。 */
    fun setCustomRange(start: LocalDate, end: LocalDate) {
        val first = if (end.isBefore(start)) end else start
        val last = if (end.isBefore(start)) start else end
        selectedMode.value = StatsMode.CUSTOM
        selectedCustomRange.value = first to last
        anchorDate.value = first
    }

    private fun currentWindow(zone: ZoneId, today: LocalDate): StatsWindow = windowOf(
        Controls(
            mode = selectedMode.value,
            anchor = anchorDate.value,
            customRange = selectedCustomRange.value,
            focusPath = focusPath.value,
        ),
        today,
        zone,
    )

    private fun windowOf(control: Controls, today: LocalDate, zone: ZoneId): StatsWindow =
        when (control.mode) {
            StatsMode.CUSTOM -> {
                val range = control.customRange ?: (today.minusDays(DEFAULT_CUSTOM_DAYS - 1) to today)
                StatsWindows.custom(range.first, range.second, zone)
            }
            else -> StatsWindows.of(control.mode, control.anchor ?: today, zone)
        }

    private fun applyWindow(window: StatsWindow, zone: ZoneId) {
        if (window.mode == StatsMode.CUSTOM) {
            val startDate = StatsWindows.dateOf(window.startAt, zone)
            val endDate = StatsWindows.dateOf(window.endAt - 1L, zone)
            selectedMode.value = StatsMode.CUSTOM
            selectedCustomRange.value = startDate to endDate
            anchorDate.value = startDate
        } else {
            selectedMode.value = window.mode
            anchorDate.value = StatsWindows.dateOf(window.startAt, zone)
        }
    }

    companion object {
        private const val REFRESH_INTERVAL_MILLIS = 60_000L
        private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
        private const val DEFAULT_CUSTOM_DAYS = 7L

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StatisticsViewModel(repository) as T
            }
    }
}

/** 范围文案里日期的本地化格式；声明在文件级，避免每次重组重建。 */
private val dayMonthWeekdayFormatter = DateTimeFormatter.ofPattern("M月d日 E", Locale.SIMPLIFIED_CHINESE)
private val fullDayMonthWeekdayFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 E", Locale.SIMPLIFIED_CHINESE)
private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy年M月", Locale.SIMPLIFIED_CHINESE)
private val yearFormatter = DateTimeFormatter.ofPattern("yyyy年", Locale.SIMPLIFIED_CHINESE)

/**
 * 范围导航条上的文案。
 *
 * - DAY：同年「11月1日 周日」，跨年「2025年11月1日 周日」，今天加「今天 · 」前缀
 * - WEEK / CUSTOM：同日历年「10月1日 – 10月15日」，跨年两端都带年份
 * - MONTH：「2026年11月」
 * - YEAR：「2026年」
 */
internal fun statsLabel(window: StatsWindow, now: Long, zone: ZoneId): String {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val startDate = Instant.ofEpochMilli(window.startAt).atZone(zone).toLocalDate()
    return when (window.mode) {
        StatsMode.DAY -> dayLabel(startDate, today)
        StatsMode.WEEK, StatsMode.CUSTOM -> {
            val endDate = Instant.ofEpochMilli(window.endAt - 1L).atZone(zone).toLocalDate()
            rangeLabel(startDate, endDate)
        }
        StatsMode.MONTH -> yearMonthFormatter.format(startDate)
        StatsMode.YEAR -> yearFormatter.format(startDate)
    }
}

private fun dayLabel(date: LocalDate, today: LocalDate): String {
    val text = if (date.year == today.year) {
        dayMonthWeekdayFormatter.format(date)
    } else {
        fullDayMonthWeekdayFormatter.format(date)
    }
    return if (date == today) "今天 · $text" else text
}

/** 聚焦分类到根的分类链；[categoryId] 为 null（根层）或分类已不存在时返回空。 */
private fun crumbPath(tree: CategoryTree, categoryId: Long?): List<StatisticsCrumb> {
    if (categoryId == null) return emptyList()
    val trail = mutableListOf<StatisticsCrumb>()
    var current = tree.find(categoryId)
    while (current != null) {
        trail.add(0, StatisticsCrumb(current.category.id, current.category.name))
        current = current.category.parentId?.let(tree::find)
    }
    return trail
}

private fun rangeLabel(start: LocalDate, end: LocalDate): String =
    if (start.year == end.year) {
        "${start.monthValue}月${start.dayOfMonth}日 – ${end.monthValue}月${end.dayOfMonth}日"
    } else {
        "${start.year}年${start.monthValue}月${start.dayOfMonth}日 – " +
            "${end.year}年${end.monthValue}月${end.dayOfMonth}日"
    }
