package com.example.timelog.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.StatsBuilder
import com.example.timelog.domain.StatsPeriod
import com.example.timelog.domain.StatsSummary
import com.example.timelog.util.DateTimeFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/** 统计页的展示状态。isLoading 用于避免首帧闪一下「空统计」。 */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val period: StatsPeriod = StatsPeriod.TODAY,
    val summary: StatsSummary = StatsSummary(
        period = StatsPeriod.TODAY,
        totalMillis = 0L,
        eventCount = 0,
        categories = emptyList(),
        last7Days = emptyList(),
    ),
)

class StatisticsViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(StatsPeriod.TODAY)

    /** 每分钟推进一次「现在」，让今天的分界与「最近 7 天」自动生长；与首页保持同一节奏。 */
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000L)
        }
    }

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.timeline,
        clock,
        selectedPeriod,
    ) { events, now, period ->
        StatisticsUiState(
            isLoading = false,
            period = period,
            summary = StatsBuilder.build(
                events = events,
                period = period,
                now = now,
                zone = DateTimeFormat.zone(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(),
    )

    fun selectPeriod(period: StatsPeriod) {
        selectedPeriod.value = period
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StatisticsViewModel(repository) as T
            }
    }
}
