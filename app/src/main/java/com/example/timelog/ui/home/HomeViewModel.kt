package com.example.timelog.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.Category
import com.example.timelog.domain.model.TimelineBuilder
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.TimelineSegment
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 首页一天的展示数据（标题由 UI 语言层负责，时间轴本身在 domain 里）。 */
data class HomeDay(
    val date: LocalDate,
    val title: String,
    val subtitle: String,
    val isToday: Boolean,
    val segments: List<TimelineSegment>,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val days: List<HomeDay> = emptyList(),
    val events: List<TimelineEvent> = emptyList(),
    /** 系统分类「未分类」：未记录的空隙和归入系统分类的事件共用同一个名字与颜色。 */
    val uncategorized: Category? = null,
    val editor: EditorTarget? = null,
    val pendingDelete: TimelineEvent? = null,
)

sealed interface EditorTarget {
    /** 新建；startAt / endAt 非空时表示由「未分类」空隙点进来，直接预填这段时间。 */
    data class New(val startAt: Long? = null, val endAt: Long? = null) : EditorTarget
    data class Edit(val eventId: Long) : EditorTarget
}

class HomeViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private data class Transient(
        val editor: EditorTarget? = null,
        val pendingDeleteId: Long? = null,
    )

    private val transient = MutableStateFlow(Transient())

    /** 每分钟推进一次「现在」，让今天的未记录尾巴和日期分界自动生长。 */
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000L)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.timeline,
        repository.categoryTree,
        clock,
        transient,
    ) { events, tree, now, local ->
        val today = DateTimeFormat.toLocalDateTime(now).toLocalDate()
        val days = TimelineBuilder.build(
            events = events,
            today = today,
            now = now,
            zone = DateTimeFormat.zone(),
        ).map { day ->
            HomeDay(
                date = day.date,
                title = DateTimeFormat.sectionTitle(day.date, today),
                subtitle = DateTimeFormat.sectionSubtitle(day.date, today),
                isToday = day.isToday,
                segments = day.segments,
            )
        }
        HomeUiState(
            isLoading = false,
            days = days,
            events = events,
            uncategorized = tree.flatten().firstOrNull { it.first.category.isSystem }?.first?.category,
            editor = local.editor,
            pendingDelete = events.firstOrNull { it.event.id == local.pendingDeleteId },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun openNewRecord() {
        transient.update { it.copy(editor = EditorTarget.New()) }
    }

    /** 点击未记录的「未分类」空隙：新建记录并预填这段时间。 */
    fun openFillRecord(startAt: Long, endAt: Long) {
        transient.update { it.copy(editor = EditorTarget.New(startAt = startAt, endAt = endAt)) }
    }

    fun openEditRecord(eventId: Long) {
        transient.update { it.copy(editor = EditorTarget.Edit(eventId)) }
    }

    fun closeRecord() {
        transient.update { it.copy(editor = null) }
    }

    fun requestDelete(eventId: Long) {
        transient.update { it.copy(pendingDeleteId = eventId) }
    }

    fun dismissDelete() {
        transient.update { it.copy(pendingDeleteId = null) }
    }

    fun confirmDelete() {
        val id = transient.value.pendingDeleteId ?: return
        transient.update { it.copy(pendingDeleteId = null) }
        viewModelScope.launch { repository.deleteEvent(id) }
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(repository) as T
            }
    }
}
