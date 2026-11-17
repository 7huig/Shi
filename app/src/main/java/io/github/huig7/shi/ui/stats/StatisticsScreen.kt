package io.github.huig7.shi.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.StatsMode
import io.github.huig7.shi.ui.theme.FallGreen
import io.github.huig7.shi.ui.theme.FallGreenDark
import io.github.huig7.shi.ui.theme.RiseRed
import io.github.huig7.shi.ui.theme.RiseRedDark
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate

/** 自定义时间段需要连续弹两次日期选择：先起始日，再结束日。 */
private enum class DatePickerStep { START, END }

/** 下钻层数上限：环形图最多三层（见 [io.github.huig7.shi.domain.StatsBuilder.MAX_DEPTH]），最多再进入两级。 */
private const val MAX_DRILL_LEVELS = 2

/**
 * 统计页（v3.0）：模式切换、范围导航、多层环形堆叠图与分类用时列表。
 *
 * 弧段与分类行可联动选中；有子分类的行可通过行尾箭头下钻，顶部面包屑与返回按钮逐级回到根层；
 * 下钻只影响「分类用时」区块与环图层级，模式、翻页和自定义区间照常可用。
 *
 * 页面整体可滚动；[footer] 渲染在内容最后，用于承载底部数据迁移入口。
 */
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onModeSelected: (StatsMode) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCustomRangeSelected: (LocalDate, LocalDate) -> Unit,
    onCategoryDrilled: (Long) -> Unit,
    onDrillUp: () -> Unit,
    onCrumbSelected: (Int) -> Unit,
    footer: @Composable () -> Unit = {},
) {
    val summary = state.summary
    var pickerStep by remember { mutableStateOf<DatePickerStep?>(null) }
    var pendingStart by remember { mutableStateOf<LocalDate?>(null) }
    // 窗口或下钻层级改变时立即回到总览；普通数据刷新保留当前选中。
    var selectedRingPath by remember(state.window, state.focusPath) {
        mutableStateOf<List<Int>?>(null)
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val riseColor = if (isDark) RiseRedDark else RiseRed
    val fallColor = if (isDark) FallGreenDark else FallGreen
    val today = LocalDate.now(DateTimeFormat.zone())

    fun openCustomRangePicker() {
        pendingStart = null
        pickerStep = DatePickerStep.START
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, bottom = 16.dp),
    ) {
        ModeSelector(
            mode = state.mode,
            onModeSelected = onModeSelected,
            onEditCustomRange = ::openCustomRangePicker,
        )
        RangeNav(
            label = state.label,
            mode = state.mode,
            canGoNext = state.canGoNext,
            onPrevious = onPrevious,
            onNext = onNext,
            onEditCustomRange = ::openCustomRangePicker,
        )
        if (state.focusPath.isNotEmpty()) {
            DrillHeader(
                path = state.focusPath,
                onDrillUp = onDrillUp,
                onCrumbSelected = onCrumbSelected,
            )
        }
        when {
            state.isLoading -> Unit
            summary == null -> Unit
            else -> {
                val segments = remember(summary) { buildRingSegments(summary.roots) }
                val selectedSegment = selectedRingPath?.let { path ->
                    segments.firstOrNull { it.path == path }
                }
                RingSection(
                    summary = summary,
                    segments = segments,
                    selectedSegment = selectedSegment,
                    onSegmentSelected = { path ->
                        selectedRingPath = if (selectedRingPath == path) null else path
                    },
                )
                if (summary.totalMillis <= 0L) {
                    EmptyStats()
                } else {
                    SectionHeader("分类用时")
                    // 环形图最多三层，因此最多再下钻两级；行主体选中，独立箭头下钻。
                    val canDrillFurther = state.focusPath.size < MAX_DRILL_LEVELS
                    val maxMillis = summary.roots.maxOfOrNull { it.millis } ?: 1L
                    if (summary.roots.isEmpty() && state.focusPath.isNotEmpty()) {
                        DrillEmptyHint()
                    } else {
                        summary.roots.forEachIndexed { index, root ->
                            val drillable = canDrillFurther && root.children.isNotEmpty()
                            CategoryUsageRow(
                                usage = root,
                                maxMillis = maxMillis,
                                riseColor = riseColor,
                                fallColor = fallColor,
                                selected = selectedSegment?.path?.firstOrNull() == index,
                                onSelect = {
                                    val rootPath = listOf(index)
                                    selectedRingPath = if (selectedRingPath == rootPath) null else rootPath
                                },
                                onDrill = if (drillable) onCategoryDrilled else null,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        footer()
    }

    when (pickerStep) {
        DatePickerStep.START -> DatePickerSheet(
            initialDate = state.customRange?.first ?: today,
            onDismiss = { pickerStep = null },
            onConfirm = { date ->
                pendingStart = date
                pickerStep = DatePickerStep.END
            },
        )
        DatePickerStep.END -> DatePickerSheet(
            initialDate = pendingStart ?: state.customRange?.second ?: today,
            onDismiss = { pickerStep = null },
            onConfirm = { end ->
                val start = pendingStart ?: end
                pendingStart = null
                pickerStep = null
                if (end.isBefore(start)) {
                    onCustomRangeSelected(end, start)
                } else {
                    onCustomRangeSelected(start, end)
                }
            },
        )
        null -> Unit
    }
}

@Composable
private fun ModeSelector(
    mode: StatsMode,
    onModeSelected: (StatsMode) -> Unit,
    onEditCustomRange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatsMode.entries.forEach { candidate ->
            FilterChip(
                selected = candidate == mode,
                onClick = {
                    if (candidate == StatsMode.CUSTOM && mode == StatsMode.CUSTOM) {
                        onEditCustomRange()
                    } else {
                        onModeSelected(candidate)
                    }
                },
                label = { Text(statsModeLabel(candidate), fontSize = 13.sp) },
            )
        }
    }
}

private fun statsModeLabel(mode: StatsMode): String = when (mode) {
    StatsMode.DAY -> "日"
    StatsMode.WEEK -> "周"
    StatsMode.MONTH -> "月"
    StatsMode.YEAR -> "年"
    StatsMode.CUSTOM -> "自定义"
}

@Composable
private fun RangeNav(
    label: String,
    mode: StatsMode,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onEditCustomRange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "上一个周期",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (mode == StatsMode.CUSTOM) {
                TextButton(
                    onClick = onEditCustomRange,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = "修改",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "下一个周期",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateTimeFormat.startOfDayUtcMillis(initialDate),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onConfirm(DateTimeFormat.dateFromUtcMillis(millis))
                    }
                },
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
