package com.example.timelog.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.OverlapPlan
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.Event
import com.example.timelog.domain.resolveOverlap
import com.example.timelog.ui.home.EditorTarget
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordUiState(
    val isEditing: Boolean = false,
    val eventId: Long? = null,
    val note: String = "",
    val startDate: LocalDate = LocalDate.now(DateTimeFormat.zone()),
    val startTime: LocalTime = LocalTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0),
    val endDate: LocalDate = LocalDate.now(DateTimeFormat.zone()),
    val endTime: LocalTime = LocalTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0),
    val tree: CategoryTree = CategoryTree(),
    val selectedCategoryId: Long? = null,
    val timeError: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    /** 会被目标区间整段盖住、需要用户确认的已有记录；非空时由 RecordSheet 弹确认框。 */
    val pendingOverwrite: List<Event> = emptyList(),
)

class RecordViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** 等用户确认「仍要保存」的落库参数；确认后按同一份 [OverlapPlan] 真正写入。 */
    private var pendingSave: PendingSave? = null

    init {
        viewModelScope.launch {
            repository.categoryTree.collect { tree ->
                _uiState.update { state ->
                    val stillValid = state.selectedCategoryId?.takeIf { tree.find(it) != null }
                    val selected = stillValid ?: defaultCategoryId(tree)
                    state.copy(tree = tree, selectedCategoryId = selected)
                }
            }
        }
    }

    fun start(target: EditorTarget) {
        loadJob?.cancel()
        pendingSave = null
        _uiState.update {
            it.copy(
                isSaving = false,
                saved = false,
                timeError = false,
                pendingOverwrite = emptyList(),
            )
        }
        when (target) {
            is EditorTarget.New -> startNew(target)
            is EditorTarget.Edit -> startEdit(target.eventId)
        }
    }

    private fun startNew(prefill: EditorTarget.New) {
        if (prefill.startAt != null && prefill.endAt != null && prefill.endAt > prefill.startAt) {
            val startLocal = DateTimeFormat.toLocalDateTime(prefill.startAt)
            val endLocal = DateTimeFormat.toLocalDateTime(prefill.endAt)
            _uiState.update {
                it.copy(
                    isEditing = false,
                    eventId = null,
                    note = "",
                    startDate = startLocal.toLocalDate(),
                    startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime().withSecond(0).withNano(0),
                    timeError = false,
                )
            }
            return
        }
        startNewNow()
    }

    private fun startNewNow() {
        val now = LocalDateTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0)
        val fallbackStart = now.minusMinutes(30)
        _uiState.update {
            it.copy(
                isEditing = false,
                eventId = null,
                note = "",
                startDate = fallbackStart.toLocalDate(),
                startTime = fallbackStart.toLocalTime(),
                endDate = now.toLocalDate(),
                endTime = now.toLocalTime(),
                timeError = false,
            )
        }
        loadJob = viewModelScope.launch {
            val nowMillis = DateTimeFormat.toEpochMillis(now.toLocalDate(), now.toLocalTime())
            val lastEnd = repository.latestEventEnd()
            // 接着上次记录继续：上一次结束在 12 小时内，就用它作为开始；否则回退到「半小时前」。
            val startMillis = if (lastEnd in (nowMillis - 12 * 3_600_000L)..(nowMillis - 60_000L)) {
                lastEnd
            } else {
                nowMillis - 30 * 60_000L
            }
            val startLocal = DateTimeFormat.toLocalDateTime(startMillis)
            _uiState.update {
                it.copy(
                    startDate = startLocal.toLocalDate(),
                    startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                    endDate = now.toLocalDate(),
                    endTime = now.toLocalTime(),
                )
            }
        }
    }

    private fun startEdit(eventId: Long) {
        loadJob = viewModelScope.launch {
            val event = repository.getEvent(eventId) ?: return@launch
            val startLocal = DateTimeFormat.toLocalDateTime(event.startAt)
            val endLocal = DateTimeFormat.toLocalDateTime(event.endAt)
            _uiState.update {
                it.copy(
                    isEditing = true,
                    eventId = event.id,
                    note = event.note.orEmpty(),
                    startDate = startLocal.toLocalDate(),
                    startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime().withSecond(0).withNano(0),
                    selectedCategoryId = event.categoryId,
                    timeError = false,
                )
            }
        }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onStartDateSelected(date: LocalDate) = updateStart(date, _uiState.value.startTime)

    fun onStartTimeSelected(time: LocalTime) =
        updateStart(_uiState.value.startDate, time.withSecond(0).withNano(0))

    /**
     * 改开始时间时顺手把结束时间推到「开始 + 30 分钟」，避免出现负区间；
     * 改结束时间不做自动纠正，交给保存时的校验提示。
     */
    private fun updateStart(date: LocalDate, time: LocalTime) {
        _uiState.update { state ->
            val startMillis = DateTimeFormat.toEpochMillis(date, time)
            val endMillis = DateTimeFormat.toEpochMillis(state.endDate, state.endTime)
            if (endMillis <= startMillis) {
                val endLocal = DateTimeFormat.toLocalDateTime(startMillis + 30 * 60_000L)
                state.copy(
                    startDate = date,
                    startTime = time,
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime(),
                    timeError = false,
                )
            } else {
                state.copy(startDate = date, startTime = time, timeError = false)
            }
        }
    }

    fun onEndDateSelected(date: LocalDate) {
        _uiState.update { it.copy(endDate = date, timeError = false) }
    }

    fun onEndTimeSelected(time: LocalTime) {
        _uiState.update {
            it.copy(endTime = time.withSecond(0).withNano(0), timeError = false)
        }
    }

    /**
     * 保存：先查重叠并算出让路方案。
     * - 只有单侧裁剪 / 拆分（updates / clones）时静默生效；
     * - 有整段被盖住的记录（victims）时先挂起，等用户在确认框里点「仍要保存」再落库。
     */
    fun save() {
        val state = _uiState.value
        if (state.isSaving || pendingSave != null) return
        val categoryId = state.selectedCategoryId ?: return
        val startAt = DateTimeFormat.toEpochMillis(state.startDate, state.startTime)
        val endAt = DateTimeFormat.toEpochMillis(state.endDate, state.endTime)
        if (endAt <= startAt) {
            _uiState.update { it.copy(timeError = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true, timeError = false) }
        viewModelScope.launch {
            runCatching {
                // 契约：getOverlappingEvents(startAt, endAt, excludeId)
                val others = repository.getOverlappingEvents(
                    startAt,
                    endAt,
                    state.eventId ?: -1L,
                )
                val plan = resolveOverlap(startAt, endAt, others)
                if (plan.victims.isNotEmpty()) {
                    pendingSave = PendingSave(
                        id = state.eventId,
                        note = state.note,
                        startAt = startAt,
                        endAt = endAt,
                        categoryId = categoryId,
                        plan = plan,
                    )
                    _uiState.update {
                        it.copy(isSaving = false, pendingOverwrite = plan.victims)
                    }
                } else {
                    // 契约：saveEventResolved(id, note, startAt, endAt, categoryId, plan)
                    repository.saveEventResolved(
                        state.eventId,
                        state.note,
                        startAt,
                        endAt,
                        categoryId,
                        plan,
                    )
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                }
            }.onFailure {
                pendingSave = null
                _uiState.update { it.copy(isSaving = false, pendingOverwrite = emptyList()) }
            }
        }
    }

    /** 重叠确认框里点「仍要保存」：按挂起时的方案真正写入，victims 会被删除。 */
    fun confirmOverwrite() {
        val pending = pendingSave ?: return
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                repository.saveEventResolved(
                    pending.id,
                    pending.note,
                    pending.startAt,
                    pending.endAt,
                    pending.categoryId,
                    pending.plan,
                )
            }.onSuccess {
                pendingSave = null
                _uiState.update {
                    it.copy(isSaving = false, saved = true, pendingOverwrite = emptyList())
                }
            }.onFailure {
                pendingSave = null
                _uiState.update { it.copy(isSaving = false, pendingOverwrite = emptyList()) }
            }
        }
    }

    /** 重叠确认框里点「取消」：放弃这次保存，回到编辑状态。 */
    fun dismissOverwrite() {
        pendingSave = null
        _uiState.update { it.copy(isSaving = false, pendingOverwrite = emptyList()) }
    }

    fun reset() {
        loadJob?.cancel()
        pendingSave = null
        _uiState.update { current ->
            RecordUiState(
                tree = current.tree,
                selectedCategoryId = current.selectedCategoryId,
            )
        }
    }

    private fun defaultCategoryId(tree: CategoryTree): Long? {
        // 默认落在第一个叶子分类（工作 / 执行）上，方便快速连记。
        return tree.flatten().firstOrNull { it.first.children.isEmpty() }
            ?.first?.category?.id
            ?: tree.roots.firstOrNull()?.category?.id
    }

    private data class PendingSave(
        val id: Long?,
        val note: String?,
        val startAt: Long,
        val endAt: Long,
        val categoryId: Long,
        val plan: OverlapPlan,
    )

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecordViewModel(repository) }
        }
    }
}
