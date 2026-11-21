package io.github.huig7.shi.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.domain.plan.CategoryPlan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 「目标与提醒」列表里的一行：分类 + 它当前的计划（未配置时为空计划）。 */
data class CategoryPlanRow(
    val categoryId: Long,
    val name: String,
    val path: String,
    val depth: Int,
    val colorHex: Long,
    val plan: CategoryPlan?,
)

/**
 * 正在编辑的配置草稿。
 *
 * 数字先按字符串保存，允许用户删空重填；保存时再统一校验并给出具体错误。
 */
data class CategoryPlanEditor(
    val categoryId: Long,
    val categoryName: String,
    val categoryPath: String,
    val heatmapEnabled: Boolean,
    val goalEnabled: Boolean,
    val goalMinutes: String,
    val fixedEnabled: Boolean,
    val fixedMinuteOfDay: Int,
    val fixedLabel: String,
    val intervalEnabled: Boolean,
    val intervalMinutes: String,
    val intervalLabel: String,
    val error: String? = null,
)

data class CategoryPlanUiState(
    val isLoading: Boolean = true,
    val rows: List<CategoryPlanRow> = emptyList(),
    val editor: CategoryPlanEditor? = null,
)

class CategoryPlanViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val editor = MutableStateFlow<CategoryPlanEditor?>(null)

    val uiState: StateFlow<CategoryPlanUiState> = combine(
        repository.categoryTree,
        repository.categoryPlans,
        editor,
    ) { tree, plans, draft ->
        val byCategory = plans.associateBy { it.categoryId }
        CategoryPlanUiState(
            isLoading = false,
            rows = tree.flatten().map { (node, depth) ->
                CategoryPlanRow(
                    categoryId = node.category.id,
                    name = node.category.name,
                    path = tree.pathOf(node.category.id),
                    depth = depth,
                    colorHex = node.category.colorHex,
                    plan = byCategory[node.category.id],
                )
            },
            editor = draft,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryPlanUiState(),
    )

    fun startEdit(categoryId: Long) {
        val row = uiState.value.rows.firstOrNull { it.categoryId == categoryId } ?: return
        val plan = row.plan
        editor.value = CategoryPlanEditor(
            categoryId = row.categoryId,
            categoryName = row.name,
            categoryPath = row.path,
            heatmapEnabled = plan?.heatmapEnabled == true,
            goalEnabled = plan?.hasDailyGoal == true,
            goalMinutes = (plan?.dailyGoalMinutes?.takeIf { it > 0 } ?: DEFAULT_GOAL_MINUTES)
                .toString(),
            fixedEnabled = plan?.hasFixedTime == true,
            fixedMinuteOfDay = plan?.fixedTimeMinuteOfDay ?: DEFAULT_FIXED_MINUTE,
            fixedLabel = plan?.fixedTimeLabel.orEmpty(),
            intervalEnabled = plan?.hasInterval == true,
            intervalMinutes = (plan?.intervalMinutes?.takeIf { it > 0 } ?: DEFAULT_INTERVAL_MINUTES)
                .toString(),
            intervalLabel = plan?.intervalLabel.orEmpty(),
        )
    }

    fun dismissEditor() {
        editor.value = null
    }

    fun setHeatmapEnabled(enabled: Boolean) = update { it.copy(heatmapEnabled = enabled) }

    fun setGoalEnabled(enabled: Boolean) = update { it.copy(goalEnabled = enabled) }

    fun setGoalMinutes(value: String) = update {
        it.copy(goalMinutes = value.filter(Char::isDigit), error = null)
    }

    fun setFixedEnabled(enabled: Boolean) = update { it.copy(fixedEnabled = enabled) }

    fun setFixedMinuteOfDay(minute: Int) = update {
        it.copy(fixedMinuteOfDay = minute.coerceIn(0, MINUTES_PER_DAY - 1))
    }

    fun setFixedLabel(value: String) = update { it.copy(fixedLabel = value) }

    fun setIntervalEnabled(enabled: Boolean) = update { it.copy(intervalEnabled = enabled) }

    fun setIntervalMinutes(value: String) = update {
        it.copy(intervalMinutes = value.filter(Char::isDigit), error = null)
    }

    fun setIntervalLabel(value: String) = update { it.copy(intervalLabel = value) }

    fun save() {
        val draft = editor.value ?: return
        val goalMinutes = draft.goalMinutes.toIntOrNull()
        if (draft.goalEnabled && (goalMinutes == null || goalMinutes !in 1..MAX_GOAL_MINUTES)) {
            update { it.copy(error = "每日目标请填 1–1440 分钟") }
            return
        }
        val intervalMinutes = draft.intervalMinutes.toIntOrNull()
        if (
            draft.intervalEnabled &&
            (intervalMinutes == null || intervalMinutes !in MIN_INTERVAL_MINUTES..MAX_INTERVAL_MINUTES)
        ) {
            update { it.copy(error = "间隔请填 5–10080 分钟") }
            return
        }

        val plan = CategoryPlan(
            categoryId = draft.categoryId,
            heatmapEnabled = draft.heatmapEnabled,
            dailyGoalMinutes = if (draft.goalEnabled) goalMinutes ?: 0 else 0,
            fixedTimeMinuteOfDay = draft.fixedMinuteOfDay.takeIf { draft.fixedEnabled },
            fixedTimeLabel = draft.fixedLabel.trim().ifEmpty {
                if (draft.fixedEnabled) draft.categoryName else null
            },
            intervalMinutes = if (draft.intervalEnabled) intervalMinutes else null,
            intervalLabel = draft.intervalLabel.trim().ifEmpty {
                if (draft.intervalEnabled) draft.categoryName else null
            },
        )

        viewModelScope.launch {
            try {
                repository.saveCategoryPlan(plan)
                editor.value = null
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                update { it.copy(error = "保存失败，请重试") }
            }
        }
    }

    private fun update(transform: (CategoryPlanEditor) -> CategoryPlanEditor) {
        editor.update { current -> current?.let(transform) }
    }

    companion object {
        private const val MINUTES_PER_DAY = 24 * 60
        private const val DEFAULT_GOAL_MINUTES = 60
        private const val DEFAULT_FIXED_MINUTE = 9 * 60
        private const val DEFAULT_INTERVAL_MINUTES = 120
        private const val MAX_GOAL_MINUTES = MINUTES_PER_DAY
        private const val MIN_INTERVAL_MINUTES = 5
        private const val MAX_INTERVAL_MINUTES = 7 * MINUTES_PER_DAY

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoryPlanViewModel(repository) }
        }
    }
}
