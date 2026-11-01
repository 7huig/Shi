package com.example.timelog.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.CategoryTree
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
    val title: String = "",
    val note: String = "",
    val date: LocalDate = LocalDate.now(DateTimeFormat.zone()),
    val time: LocalTime = LocalTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0),
    val tree: CategoryTree = CategoryTree(),
    val selectedCategoryId: Long? = null,
    val expandedIds: Set<Long> = emptySet(),
    val titleError: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

class RecordViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            repository.categoryTree.collect { tree ->
                _uiState.update { state ->
                    val stillValid = state.selectedCategoryId?.takeIf { tree.find(it) != null }
                    val selected = stillValid ?: tree.flatten().firstOrNull()?.first?.category?.id
                    state.copy(
                        tree = tree,
                        selectedCategoryId = selected,
                        expandedIds = state.expandedIds + tree.ancestorIds(selected ?: -1),
                    )
                }
            }
        }
    }

    fun start(target: EditorTarget) {
        loadJob?.cancel()
        _uiState.update { it.copy(isSaving = false, saved = false, titleError = false) }
        when (target) {
            EditorTarget.New -> {
                val now = LocalDateTime.now(DateTimeFormat.zone())
                _uiState.update {
                    it.copy(
                        isEditing = false,
                        eventId = null,
                        title = "",
                        note = "",
                        date = now.toLocalDate(),
                        time = now.toLocalTime().withSecond(0).withNano(0),
                    )
                }
            }

            is EditorTarget.Edit -> {
                loadJob = viewModelScope.launch {
                    val event = repository.getEvent(target.eventId) ?: return@launch
                    val local = DateTimeFormat.toLocalDateTime(event.timestamp)
                    _uiState.update {
                        it.copy(
                            isEditing = true,
                            eventId = event.id,
                            title = event.title,
                            note = event.note.orEmpty(),
                            date = local.toLocalDate(),
                            time = local.toLocalTime().withSecond(0).withNano(0),
                            selectedCategoryId = event.categoryId,
                            expandedIds = it.expandedIds + it.tree.ancestorIds(event.categoryId),
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = false) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                expandedIds = it.expandedIds + it.tree.ancestorIds(categoryId),
            )
        }
    }

    fun onCategoryExpanded(categoryId: Long) {
        _uiState.update {
            val expanded = if (categoryId in it.expandedIds) {
                it.expandedIds - categoryId
            } else {
                it.expandedIds + categoryId
            }
            it.copy(expandedIds = expanded)
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { it.copy(time = time.withSecond(0).withNano(0)) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        val categoryId = state.selectedCategoryId ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                repository.saveEvent(
                    id = state.eventId,
                    title = state.title,
                    note = state.note,
                    timestamp = DateTimeFormat.toEpochMillis(state.date, state.time),
                    categoryId = categoryId,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun reset() {
        loadJob?.cancel()
        _uiState.update { current ->
            RecordUiState(
                tree = current.tree,
                selectedCategoryId = current.selectedCategoryId,
                expandedIds = current.expandedIds,
            )
        }
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecordViewModel(repository) }
        }
    }
}
