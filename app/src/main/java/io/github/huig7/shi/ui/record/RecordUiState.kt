package io.github.huig7.shi.ui.record

import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime

/** 快速记录的结果；成功写入、需要解决重叠、或失败。 */
sealed interface QuickRecordResult {
    data class Saved(val eventId: Long, val minutes: Int) : QuickRecordResult
    data class NeedsReview(val startAt: Long, val endAt: Long, val categoryId: Long) : QuickRecordResult
    data class Failed(val message: String) : QuickRecordResult
}

data class RecordUiState(
    val isEditing: Boolean = false,
    val eventId: Long? = null,
    /** 由时间轴空白处进入时为 true，标题用「记录这段时间」而不是「记录一段」。 */
    val isGapFill: Boolean = false,
    /** 从快速记录的重叠退回时为 true，表单顶部给一句解释。 */
    val needsReview: Boolean = false,
    /** 从长空档进入时，整段空档的范围；非空时表单显示「补齐整段」。 */
    val wholeGapStartAt: Long? = null,
    val wholeGapEndAt: Long? = null,
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
