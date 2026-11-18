package io.github.huig7.shi.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.StatsMode
import io.github.huig7.shi.ui.common.DateRangePickerSheet
import io.github.huig7.shi.ui.theme.FallGreen
import io.github.huig7.shi.ui.theme.FallGreenDark
import io.github.huig7.shi.ui.theme.RiseRed
import io.github.huig7.shi.ui.theme.RiseRedDark
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate
import kotlin.math.roundToInt

/** 下钻层数上限：环形图最多三层（见 [io.github.huig7.shi.domain.StatsBuilder.MAX_DEPTH]），最多再进入两级。 */
private const val MAX_DRILL_LEVELS = 2

/**
 * 统计页（v3.0）：模式切换、范围导航、多层环形堆叠图与分类用时列表。
 *
 * 弧段与分类行可联动选中；有子分类的行可通过行尾箭头下钻，顶部面包屑与返回按钮逐级回到根层；
 * 下钻只影响「分类用时」区块与环图层级，模式、翻页和自定义区间照常可用。
 *
 * 页面整体可滚动。数据备份入口统一收在顶栏「⋮ → 数据备份」，这里不再重复。
 */
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onModeSelected: (StatsMode) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onCustomRangeSelected: (LocalDate, LocalDate) -> Unit,
    onCategoryDrilled: (Long) -> Unit,
    onDrillUp: () -> Unit,
    onCrumbSelected: (Int) -> Unit,
) {
    val summary = state.summary
    var showRangePicker by remember { mutableStateOf(false) }
    // 窗口或下钻层级改变时立即回到总览；普通数据刷新保留当前选中。
    var selectedRingPath by remember(state.window, state.focusPath) {
        mutableStateOf<List<Int>?>(null)
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val riseColor = if (isDark) RiseRedDark else RiseRed
    val fallColor = if (isDark) FallGreenDark else FallGreen
    val today = LocalDate.now(DateTimeFormat.zone())

    fun openCustomRangePicker() {
        showRangePicker = true
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
            canGoToday = state.canGoToday,
            onPrevious = onPrevious,
            onNext = onNext,
            onToday = onToday,
            onEditCustomRange = ::openCustomRangePicker,
        )
        if (state.focusPath.isNotEmpty()) {
            DrillHeader(
                path = state.focusPath,
                onDrillUp = onDrillUp,
                onCrumbSelected = onCrumbSelected,
            )
        }
        summary?.let {
            if (it.totalMillis > 0L && it.elapsedMillis > 0L) {
                CoverageSection(summary = it)
            }
        }
        when {
            state.isLoading -> Unit
            summary == null -> Unit
            summary.totalMillis <= 0L -> EmptyStats()
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
                SectionHeader("分类用时")
                // 环形图最多三层，因此最多再下钻两级；行主体选中，独立箭头下钻。
                val canDrillFurther = state.focusPath.size < MAX_DRILL_LEVELS
                if (summary.roots.isEmpty() && state.focusPath.isNotEmpty()) {
                    DrillEmptyHint()
                } else {
                    summary.roots.forEachIndexed { index, root ->
                        val drillable = canDrillFurther && root.children.isNotEmpty()
                        CategoryUsageRow(
                            usage = root,
                            totalMillis = summary.totalMillis,
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

    if (showRangePicker) {
        DateRangePickerSheet(
            initialStart = state.customRange?.first ?: today.minusDays(6),
            initialEnd = state.customRange?.second ?: today,
            onDismiss = { showRangePicker = false },
            onConfirm = { start, end ->
                showRangePicker = false
                onCustomRangeSelected(start, end)
            },
        )
    }
}

/** 窗口内有记录的墙钟占比：把「已记录」和「一天 / 一周」分开，避免把环图的满圆误读成整天。 */
@Composable
private fun CoverageSection(summary: io.github.huig7.shi.domain.StatsSummary) {
    val elapsed = summary.elapsedMillis
    if (elapsed <= 0L) return
    val ratio = (summary.coveredMillis.toDouble() / elapsed.toDouble()).toFloat().coerceIn(0f, 1f)
    val percent = (ratio * 100f).roundToInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "已记录 $percent%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = DateTimeFormat.durationText(summary.coveredMillis) + " / " +
                    DateTimeFormat.durationText(elapsed),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
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
    canGoToday: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
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
                // 让长日期先让位给右侧按钮：否则自定义区间的长文案会把「修改 / 今天」挤成竖排。
                modifier = Modifier.weight(1f, fill = false),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
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
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
            if (canGoToday) {
                TextButton(
                    onClick = onToday,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = "今天",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "下一个周期",
                tint = if (canGoNext) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                },
            )
        }
    }
}
