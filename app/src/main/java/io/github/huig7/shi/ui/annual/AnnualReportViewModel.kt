package io.github.huig7.shi.ui.annual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.domain.annual.AnnualReport
import io.github.huig7.shi.domain.annual.AnnualReportBuilder
import io.github.huig7.shi.domain.model.TimelineEvent
import io.github.huig7.shi.util.DateTimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

/**
 * 年度报告浮层的状态。
 *
 * [year] 是实际展示的年份：用户没有手动选择时跟随当前年；
 * 当前年没有记录时自动落到最近一个有记录的年份。
 */
data class AnnualReportUiState(
    val isLoading: Boolean = true,
    val year: Int = LocalDate.now(DateTimeFormat.zone()).year,
    /** 有记录的年份，按从新到旧排列；不含未来年份。 */
    val availableYears: List<Int> = emptyList(),
    val report: AnnualReport? = null,
) {
    private val yearIndex: Int get() = availableYears.indexOf(year)

    val canGoOlder: Boolean get() = yearIndex >= 0 && yearIndex < availableYears.lastIndex

    val canGoNewer: Boolean get() = yearIndex > 0
}

/**
 * 年度报告 ViewModel：年份切换、数据聚合并跟随日期推进。
 *
 * 时钟只在本地日边界推进一次——年报里唯一随「现在」变化的是当前年的裁剪位置，
 * 没必要每分钟重算一整年的数据。
 */
class AnnualReportViewModel(private val repository: TimeLogRepository) : ViewModel() {

    /** null 表示自动选择：当前年优先，没有记录则用最近一个有记录的年份。 */
    private val requestedYear = MutableStateFlow<Int?>(null)

    private val clock = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(now)
            val zone = DateTimeFormat.zone()
            val nextMidnight = Instant.ofEpochMilli(now)
                .atZone(zone)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
            delay((nextMidnight - now).coerceAtLeast(1_000L))
        }
    }

    val uiState: StateFlow<AnnualReportUiState> = combine(
        repository.timeline,
        repository.categoryTree,
        clock,
        requestedYear,
    ) { events, tree, now, requested ->
        val zone = DateTimeFormat.zone()
        val currentYear = Instant.ofEpochMilli(now).atZone(zone).year
        val years = availableYears(events, zone, currentYear)
        val year = resolveYear(requested, years, currentYear)
        AnnualReportUiState(
            isLoading = false,
            year = year,
            availableYears = years,
            report = AnnualReportBuilder.build(
                events = events,
                tree = tree,
                year = year,
                now = now,
                zone = zone,
            ),
        )
    }
        // 年报聚合是全量计算，与统计页一样不放在主线程。
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = AnnualReportUiState(),
        )

    /** 打开指定年份；不在有记录年份内时由 [resolveYear] 兜底。 */
    fun selectYear(year: Int) {
        requestedYear.value = year
    }

    /** 更早的一年。 */
    fun goOlderYear() {
        val state = uiState.value
        val index = state.availableYears.indexOf(state.year)
        if (index >= 0 && index < state.availableYears.lastIndex) {
            requestedYear.value = state.availableYears[index + 1]
        }
    }

    /** 更新的一年。 */
    fun goNewerYear() {
        val state = uiState.value
        val index = state.availableYears.indexOf(state.year)
        if (index > 0) {
            requestedYear.value = state.availableYears[index - 1]
        }
    }

    private fun resolveYear(requested: Int?, years: List<Int>, currentYear: Int): Int = when {
        years.isEmpty() -> currentYear
        requested != null && requested in years -> requested
        currentYear in years -> currentYear
        else -> years.first()
    }

    /**
     * 从事件首尾推导有记录的年份，从新到旧。
     *
     * 只用现成的 timeline 流，不额外查库；跨年事件会同时出现在两个年份里。
     */
    private fun availableYears(
        events: List<TimelineEvent>,
        zone: ZoneId,
        currentYear: Int,
    ): List<Int> {
        val years = sortedSetOf<Int>()
        events.forEach { item ->
            val startYear = Instant.ofEpochMilli(item.event.startAt).atZone(zone).year
            val endAt = item.event.startAt + item.event.durationMillis
            val endYear = Instant.ofEpochMilli(endAt - 1L).atZone(zone).year
            val from = minOf(startYear, endYear)
            val to = maxOf(startYear, endYear)
            for (year in from..to) {
                if (year <= currentYear) years += year
            }
        }
        return years.toList().asReversed()
    }

    companion object {
        private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AnnualReportViewModel(repository) }
        }
    }
}
