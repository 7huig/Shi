package io.github.huig7.shi.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.huig7.shi.data.repository.SaveEventResult
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.domain.OverlapPlan
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.domain.resolveOverlap
import io.github.huig7.shi.ui.home.EditorTarget
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.CancellationException
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
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
    val isDirty: Boolean = false,
    val timeDirty: Boolean = false,
    val pendingOverwrite: List<Event> = emptyList(),
)

class RecordViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var saveJob: Job? = null
    private var activeTarget: EditorTarget? = null
    private var sessionCounter = 0L
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
        // 同一会话在旋转等配置变更后仍然保留，不能重复初始化草稿。
        if (activeTarget == target) return
        loadJob?.cancel()
        saveJob?.cancel()
        pendingSave = null
        activeTarget = target
        val session = ++sessionCounter
        _uiState.update { current ->
            RecordUiState(
                tree = current.tree,
                selectedCategoryId = current.selectedCategoryId,
                isLoading = true,
            )
        }
        when (target) {
            is EditorTarget.New -> startNew(target, session)
            is EditorTarget.Edit -> startEdit(target.eventId, session)
        }
    }

    private fun startNew(prefill: EditorTarget.New, session: Long) {
        if (prefill.startAt != null && prefill.endAt != null && prefill.endAt > prefill.startAt) {
            val startLocal = DateTimeFormat.toLocalDateTime(prefill.startAt)
            val endLocal = DateTimeFormat.toLocalDateTime(prefill.endAt)
            updateSession(session) {
                it.copy(
                    isEditing = false,
                    eventId = null,
                    note = "",
                    startDate = startLocal.toLocalDate(),
                    startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime().withSecond(0).withNano(0),
                    isLoading = false,
                    isReady = true,
                )
            }
            return
        }
        startNewNow(session)
    }

    private fun startNewNow(session: Long) {
        val now = LocalDateTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0)
        val fallbackStart = now.minusMinutes(30)
        updateSession(session) {
            it.copy(
                isEditing = false,
                eventId = null,
                note = "",
                startDate = fallbackStart.toLocalDate(),
                startTime = fallbackStart.toLocalTime(),
                endDate = now.toLocalDate(),
                endTime = now.toLocalTime(),
            )
        }
        loadJob = viewModelScope.launch {
            try {
                val nowMillis = DateTimeFormat.toEpochMillis(now.toLocalDate(), now.toLocalTime())
                val lastEnd = repository.latestEventEnd()
                val startMillis = if (lastEnd in (nowMillis - 12 * 3_600_000L)..(nowMillis - 60_000L)) {
                    lastEnd
                } else {
                    nowMillis - 30 * 60_000L
                }
                val startLocal = DateTimeFormat.toLocalDateTime(startMillis)
                updateSession(session) { state ->
                    if (state.timeDirty) {
                        state.copy(isLoading = false, isReady = true)
                    } else {
                        state.copy(
                            startDate = startLocal.toLocalDate(),
                            startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                            endDate = now.toLocalDate(),
                            endTime = now.toLocalTime(),
                            isLoading = false,
                            isReady = true,
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // 自动续接失败时保留半小时前的可用默认值。
                updateSession(session) { it.copy(isLoading = false, isReady = true) }
            }
        }
    }

    private fun startEdit(eventId: Long, session: Long) {
        updateSession(session) { it.copy(isEditing = true, eventId = eventId) }
        loadJob = viewModelScope.launch {
            try {
                val event = repository.getEvent(eventId)
                if (event == null) {
                    updateSession(session) {
                        it.copy(
                            isLoading = false,
                            isReady = false,
                            errorMessage = "记录已不存在，请关闭后重试",
                        )
                    }
                    return@launch
                }
                val startLocal = DateTimeFormat.toLocalDateTime(event.startAt)
                val endLocal = DateTimeFormat.toLocalDateTime(event.endAt)
                updateSession(session) {
                    it.copy(
                        isEditing = true,
                        eventId = event.id,
                        note = event.note.orEmpty(),
                        startDate = startLocal.toLocalDate(),
                        startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                        endDate = endLocal.toLocalDate(),
                        endTime = endLocal.toLocalTime().withSecond(0).withNano(0),
                        selectedCategoryId = event.categoryId,
                        isLoading = false,
                        isReady = true,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                updateSession(session) {
                    it.copy(
                        isLoading = false,
                        isReady = false,
                        errorMessage = "记录加载失败，请关闭后重试",
                    )
                }
            }
        }
    }

    fun onNoteChange(value: String) {
        _uiState.update { state ->
            if (value == state.note) state else state.copy(
                note = value,
                isDirty = true,
                errorMessage = null,
            )
        }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { state ->
            if (categoryId == state.selectedCategoryId) state else state.copy(
                selectedCategoryId = categoryId,
                isDirty = true,
                errorMessage = null,
            )
        }
    }

    fun onStartDateSelected(date: LocalDate) = updateStart(date, _uiState.value.startTime)

    fun onStartTimeSelected(time: LocalTime) =
        updateStart(_uiState.value.startDate, time.withSecond(0).withNano(0))

    private fun updateStart(date: LocalDate, time: LocalTime) {
        _uiState.update { state ->
            val startMillis = DateTimeFormat.toEpochMillis(date, time)
            val endMillis = DateTimeFormat.toEpochMillis(state.endDate, state.endTime)
            val updated = if (endMillis <= startMillis) {
                val endLocal = DateTimeFormat.toLocalDateTime(startMillis + 30 * 60_000L)
                state.copy(
                    startDate = date,
                    startTime = time,
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime(),
                )
            } else {
                state.copy(startDate = date, startTime = time)
            }
            updated.copy(isDirty = true, timeDirty = true, timeError = false, errorMessage = null)
        }
    }

    fun onEndDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                endDate = date,
                isDirty = true,
                timeDirty = true,
                timeError = false,
                errorMessage = null,
            )
        }
    }

    fun onEndTimeSelected(time: LocalTime) {
        _uiState.update {
            it.copy(
                endTime = time.withSecond(0).withNano(0),
                isDirty = true,
                timeDirty = true,
                timeError = false,
                errorMessage = null,
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isReady || state.isSaving || pendingSave != null) return
        val categoryId = state.selectedCategoryId ?: return
        val startAt = DateTimeFormat.toEpochMillis(state.startDate, state.startTime)
        val endAt = DateTimeFormat.toEpochMillis(state.endDate, state.endTime)
        if (endAt <= startAt) {
            _uiState.update { it.copy(timeError = true) }
            return
        }
        val session = sessionCounter
        val pending = PendingSave(
            session = session,
            id = state.eventId,
            note = state.note,
            startAt = startAt,
            endAt = endAt,
            categoryId = categoryId,
            plan = null,
        )
        _uiState.update { it.copy(isSaving = true, timeError = false, errorMessage = null) }
        saveJob = viewModelScope.launch {
            try {
                val others = repository.getOverlappingEvents(startAt, endAt, state.eventId ?: -1L)
                if (!isCurrent(session)) return@launch
                val plan = resolveOverlap(startAt, endAt, others)
                if (plan.victims.isNotEmpty()) {
                    pendingSave = pending.copy(plan = plan)
                    updateSession(session) {
                        it.copy(isSaving = false, pendingOverwrite = plan.victims)
                    }
                } else {
                    val result = repository.saveEventResolved(
                        id = pending.id,
                        note = pending.note,
                        startAt = pending.startAt,
                        endAt = pending.endAt,
                        categoryId = pending.categoryId,
                        plan = plan,
                    )
                    handleSaveResult(result, session)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                pendingSave = null
                updateSession(session) {
                    it.copy(
                        isSaving = false,
                        pendingOverwrite = emptyList(),
                        errorMessage = "保存失败，请重试",
                    )
                }
            }
        }
    }

    fun confirmOverwrite() {
        val pending = pendingSave ?: return
        if (_uiState.value.isSaving || pending.session != sessionCounter) return
        val plan = pending.plan ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        saveJob = viewModelScope.launch {
            try {
                val result = repository.saveEventResolved(
                    id = pending.id,
                    note = pending.note,
                    startAt = pending.startAt,
                    endAt = pending.endAt,
                    categoryId = pending.categoryId,
                    plan = plan,
                )
                handleSaveResult(result, pending.session)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // 保留确认上下文，允许直接重试。
                updateSession(pending.session) {
                    it.copy(isSaving = false, errorMessage = "保存失败，请重试")
                }
            }
        }
    }

    private fun handleSaveResult(result: SaveEventResult, session: Long) {
        when (result) {
            is SaveEventResult.Saved -> {
                pendingSave = null
                updateSession(session) {
                    it.copy(
                        isSaving = false,
                        saved = true,
                        pendingOverwrite = emptyList(),
                        errorMessage = null,
                    )
                }
            }

            SaveEventResult.EventNotFound -> {
                pendingSave = null
                updateSession(session) {
                    it.copy(
                        isSaving = false,
                        isReady = false,
                        pendingOverwrite = emptyList(),
                        errorMessage = "记录已不存在，请关闭后重试",
                    )
                }
            }

            SaveEventResult.OverlapChanged -> {
                pendingSave = null
                updateSession(session) {
                    it.copy(
                        isSaving = false,
                        pendingOverwrite = emptyList(),
                        errorMessage = "重叠记录已变化，请重新保存",
                    )
                }
            }
        }
    }

    fun dismissOverwrite() {
        pendingSave = null
        _uiState.update {
            it.copy(isSaving = false, pendingOverwrite = emptyList(), errorMessage = null)
        }
    }

    fun reset() {
        activeTarget = null
        sessionCounter++
        loadJob?.cancel()
        saveJob?.cancel()
        pendingSave = null
        _uiState.update { current ->
            RecordUiState(
                tree = current.tree,
                selectedCategoryId = current.selectedCategoryId,
            )
        }
    }

    private fun isCurrent(session: Long): Boolean =
        activeTarget != null && session == sessionCounter

    private fun updateSession(session: Long, transform: (RecordUiState) -> RecordUiState) {
        if (!isCurrent(session)) return
        _uiState.update(transform)
    }

    private fun defaultCategoryId(tree: CategoryTree): Long? {
        return tree.flatten().firstOrNull { it.first.children.isEmpty() }
            ?.first?.category?.id
            ?: tree.roots.firstOrNull()?.category?.id
    }

    private data class PendingSave(
        val session: Long,
        val id: Long?,
        val note: String?,
        val startAt: Long,
        val endAt: Long,
        val categoryId: Long,
        val plan: OverlapPlan?,
    )

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecordViewModel(repository) }
        }
    }
}
