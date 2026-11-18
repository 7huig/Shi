package io.github.huig7.shi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.domain.model.TimelineBuilder
import io.github.huig7.shi.domain.model.TimelineDay
import io.github.huig7.shi.domain.model.TimelineEvent
import io.github.huig7.shi.domain.model.TimelineSegment
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
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
    val editor: EditorTarget? = null,
)

sealed interface EditorTarget {
    /**
     * 新建；startAt / endAt 非空时表示由时间轴空隙点进来，直接预填这段时间。
     * [wholeGapStartAt] / [wholeGapEndAt] 非空时，表示当前只预填了长空档的尾部，
     * 表单可以一键改为补齐整段。
     */
    data class New(
        val startAt: Long? = null,
        val endAt: Long? = null,
        val categoryId: Long? = null,
        val isGapFill: Boolean = false,
        /** 快速记录因重叠退回完整表单时为 true，表单顶部给一句解释。 */
        val needsReview: Boolean = false,
        val wholeGapStartAt: Long? = null,
        val wholeGapEndAt: Long? = null,
    ) : EditorTarget

    data class Edit(val eventId: Long) : EditorTarget
}

/** 首页一次性的提示 / 撤销事件；由 MainActivity 用 Snackbar 消费。 */
sealed interface HomeUiEvent {
    data class Deleted(val event: TimelineEvent) : HomeUiEvent
    data class Restored(val event: TimelineEvent) : HomeUiEvent
    data class Message(val text: String) : HomeUiEvent
}

class HomeViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private data class Transient(
        val editor: EditorTarget? = null,
    )

    private val transient = MutableStateFlow(Transient())

    private val messages = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvents: Flow<HomeUiEvent> = messages.receiveAsFlow()

    // 每分钟只重建「今天」这一天的尾部；历史天的结果按事件列表身份缓存。
    private var cachedEvents: List<TimelineEvent>? = null
    private var cachedToday: LocalDate? = null
    private var cachedDays: List<TimelineDay> = emptyList()

    /** 每分钟推进一次「现在」，让今天的未记录尾巴和日期分界自动生长。 */
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000L)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.timeline,
        clock,
        transient,
    ) { events, now, local ->
        val zone = DateTimeFormat.zone()
        val today = DateTimeFormat.toLocalDateTime(now).toLocalDate()
        val timelineDays = if (
            events === cachedEvents &&
            today == cachedToday &&
            cachedDays.isNotEmpty()
        ) {
            cachedDays.map { day ->
                if (day.date == today) {
                    TimelineBuilder.buildDay(
                        date = today,
                        events = events,
                        today = today,
                        now = now,
                        zone = zone,
                    )
                } else {
                    day
                }
            }
        } else {
            TimelineBuilder.build(
                events = events,
                today = today,
                now = now,
                zone = zone,
            ).also {
                cachedEvents = events
                cachedToday = today
                cachedDays = it
            }
        }
        val days = timelineDays.map { day ->
            HomeDay(
                date = day.date,
                title = DateTimeFormat.sectionTitle(day.date, today),
                subtitle = DateTimeFormat.sectionSubtitle(day.date),
                isToday = day.isToday,
                segments = day.segments,
            )
        }
        HomeUiState(
            isLoading = false,
            days = days,
            events = events,
            editor = local.editor,
        )
    }
        // 时间轴构建是全量排序聚合，不放主线程。
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun openNewRecord() {
        transient.update { it.copy(editor = EditorTarget.New()) }
    }

    /** 快速记录遇到重叠时，带着刚选好的分类和区间打开完整表单。 */
    fun openNewRecordAt(
        startAt: Long,
        endAt: Long,
        categoryId: Long?,
        needsReview: Boolean = false,
    ) {
        transient.update {
            it.copy(
                editor = EditorTarget.New(
                    startAt = startAt,
                    endAt = endAt,
                    categoryId = categoryId,
                    needsReview = needsReview,
                ),
            )
        }
    }

    /**
     * 点击未记录的空隙：短空隙整段预填；超过 3 小时的长空隙只预填最后 30 分钟，
     * 同时在表单里保留「补齐整段」入口，避免一打开就是 20 小时的记录。
     */
    fun openFillGap(startAt: Long, endAt: Long) {
        val duration = endAt - startAt
        val editor = if (duration > LONG_GAP_THRESHOLD_MILLIS) {
            EditorTarget.New(
                startAt = (endAt - LONG_GAP_PREFILL_MILLIS).coerceAtLeast(startAt),
                endAt = endAt,
                isGapFill = true,
                wholeGapStartAt = startAt,
                wholeGapEndAt = endAt,
            )
        } else {
            EditorTarget.New(startAt = startAt, endAt = endAt, isGapFill = true)
        }
        transient.update { it.copy(editor = editor) }
    }

    fun openEditRecord(eventId: Long) {
        transient.update { it.copy(editor = EditorTarget.Edit(eventId)) }
    }

    fun closeRecord() {
        transient.update { it.copy(editor = null) }
    }

    /**
     * 长按删除：直接删除并给出可撤销 Snackbar，不再用确认框打断。
     * 删除失败时保持原状并提示。
     */
    fun requestDelete(eventId: Long) {
        val target = uiState.value.events.firstOrNull { it.event.id == eventId } ?: return
        viewModelScope.launch {
            try {
                repository.deleteEvent(eventId)
                messages.send(HomeUiEvent.Deleted(target))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                messages.send(HomeUiEvent.Message("删除失败，请重试"))
            }
        }
    }

    /** 撤销刚才的删除；记录按新 id 重新插入，备注、分类与时间保持不变。 */
    fun undoDelete(event: TimelineEvent) {
        viewModelScope.launch {
            try {
                if (repository.restoreEvent(event.event)) {
                    messages.send(HomeUiEvent.Restored(event))
                } else {
                    // 分类在删除记录后也被删掉了，没有「未分类」可以兜底。
                    messages.send(HomeUiEvent.Message("撤销失败：原分类已被删除"))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                messages.send(HomeUiEvent.Message("撤销失败，记录可能已被修改"))
            }
        }
    }

    /** 编辑页内确认删除后调用；与长按删除共用撤销逻辑。 */
    fun deleteEvent(eventId: Long) {
        requestDelete(eventId)
    }

    /** 撤销快速记录时静默删除，不再产生新的「已删除」提示。 */
    fun deleteSilently(eventId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteEvent(eventId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // 静默失败：列表会在下次刷新时给出真实状态。
            }
        }
    }

    companion object {
        private const val LONG_GAP_THRESHOLD_MILLIS = 3 * 60 * 60 * 1000L
        private const val LONG_GAP_PREFILL_MILLIS = 30 * 60 * 1000L

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
