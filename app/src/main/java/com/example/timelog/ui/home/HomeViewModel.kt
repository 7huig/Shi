package com.example.timelog.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EditorTarget {
    data object New : EditorTarget
    data class Edit(val eventId: Long) : EditorTarget
}

data class TimelineDay(
    val date: LocalDate,
    val title: String,
    val subtitle: String,
    val events: List<TimelineEvent>,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val days: List<TimelineDay> = emptyList(),
    val totalCount: Int = 0,
    val tree: CategoryTree = CategoryTree(),
    val editor: EditorTarget? = null,
    val pendingDelete: TimelineEvent? = null,
)

class HomeViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private data class Transient(
        val editor: EditorTarget? = null,
        val pendingDeleteId: Long? = null,
    )

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.timeline,
        repository.categoryTree,
        transient,
    ) { timeline, tree, local ->
        val today = LocalDate.now(DateTimeFormat.zone())
        val days = timeline
            .groupBy { DateTimeFormat.toLocalDateTime(it.event.timestamp).toLocalDate() }
            .map { (date, events) ->
                TimelineDay(
                    date = date,
                    title = DateTimeFormat.sectionTitle(date, today),
                    subtitle = DateTimeFormat.sectionSubtitle(date, today),
                    events = events,
                )
            }
        HomeUiState(
            isLoading = false,
            days = days,
            totalCount = timeline.size,
            tree = tree,
            editor = local.editor,
            pendingDelete = timeline.firstOrNull { it.event.id == local.pendingDeleteId },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun openNewRecord() {
        transient.update { it.copy(editor = EditorTarget.New) }
    }

    fun openEditRecord(eventId: Long) {
        transient.update { it.copy(editor = EditorTarget.Edit(eventId)) }
    }

    fun closeRecord() {
        transient.update { it.copy(editor = null) }
    }

    fun requestDelete(event: TimelineEvent) {
        transient.update { it.copy(pendingDeleteId = event.event.id) }
    }

    fun dismissDelete() {
        transient.update { it.copy(pendingDeleteId = null) }
    }

    fun confirmDelete() {
        val id = transient.value.pendingDeleteId ?: return
        viewModelScope.launch {
            repository.deleteEvent(id)
            transient.update { it.copy(pendingDeleteId = null) }
        }
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
