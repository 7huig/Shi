package io.github.huig7.shi.ui.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.huig7.shi.domain.plan.CategoryPlan
import io.github.huig7.shi.ui.home.CategoryDot
import java.time.LocalTime

/**
 * 「目标与提醒」：按分类设置每日目标、固定时间事项、间隔待办和热力图。
 *
 * 页面只做配置，不申请通知权限；首页与统计页读取同一份计划，静态展示。
 */
@Composable
fun CategoryPlanScreen(
    viewModel: CategoryPlanViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 18.dp, top = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "目标与提醒",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (state.isLoading) {
            Spacer(Modifier.height(8.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(items = state.rows, key = { it.categoryId }) { row ->
                    PlanCategoryRow(
                        row = row,
                        onClick = { viewModel.startEdit(row.categoryId) },
                    )
                }
            }
        }
    }

    state.editor?.let { editor ->
        CategoryPlanEditorDialog(
            editor = editor,
            viewModel = viewModel,
            onDismiss = viewModel::dismissEditor,
            onSave = viewModel::save,
        )
    }
}

@Composable
private fun PlanCategoryRow(
    row: CategoryPlanRow,
    onClick: () -> Unit,
) {
    val summary = planSummary(row.plan)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = (10 + row.depth * 16).dp,
                end = 10.dp,
                top = 11.dp,
                bottom = 11.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryDot(colorHex = row.colorHex, size = 8.dp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (row.depth > 0) {
                Text(
                    text = row.path.substringBeforeLast(" / "),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            summary?.let {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
        )
    }
}

/** 配置摘要：最多四条，用「 · 」连接；未配置返回 null。 */
private fun planSummary(plan: CategoryPlan?): String? {
    if (plan == null || !plan.hasAnySetting) return null
    val parts = buildList {
        if (plan.heatmapEnabled) add("热力图")
        if (plan.hasDailyGoal) add("每日 " + minutesText(plan.dailyGoalMinutes))
        plan.fixedTimeMinuteOfDay?.let { minute ->
            add(
                LocalTime.of(minute / 60, minute % 60).format(TIME_FORMAT) + " " +
                    (plan.fixedTimeLabel?.ifBlank { "固定事项" } ?: "固定事项"),
            )
        }
        if (plan.hasInterval) {
            add("每 " + minutesText(plan.intervalMinutes ?: 0) + " " + (plan.intervalLabel ?: "待办"))
        }
    }
    return parts.joinToString(" · ")
}

private fun minutesText(minutes: Int): String {
    val safe = minutes.coerceAtLeast(0)
    val hours = safe / 60
    val rest = safe % 60
    return when {
        hours == 0 -> "$rest 分钟"
        rest == 0 -> "$hours 小时"
        else -> "$hours 小时 $rest 分"
    }
}

private val TIME_FORMAT = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

/** 给编辑弹窗复用：把分钟数格式化成 HH:mm。 */
internal fun clockText(minuteOfDay: Int): String =
    LocalTime.of(minuteOfDay / 60, minuteOfDay % 60).format(TIME_FORMAT)
