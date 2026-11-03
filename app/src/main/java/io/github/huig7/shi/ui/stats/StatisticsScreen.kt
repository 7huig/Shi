package io.github.huig7.shi.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.CategoryUsage
import io.github.huig7.shi.domain.StatsMode
import io.github.huig7.shi.domain.StatsSummary
import io.github.huig7.shi.ui.theme.FallGreen
import io.github.huig7.shi.ui.theme.FallGreenDark
import io.github.huig7.shi.ui.theme.RiseRed
import io.github.huig7.shi.ui.theme.RiseRedDark
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** 自定义时间段需要连续弹两次日期选择：先起始日，再结束日。 */
private enum class DatePickerStep { START, END }

/** 下钻层数上限：环形图最多三层（见 [io.github.huig7.shi.domain.StatsBuilder.MAX_DEPTH]），最多再进入两级。 */
private const val MAX_DRILL_LEVELS = 2

private const val RING_LAYER_COUNT = 3
private const val RING_GAP_ANGLE = 2.5f
private const val RING_MIN_ANGLE = 1f

/** 各层环带的内 / 外半径比例（相对于外接圆半径 R）。 */
private val RING_INNER_RATIOS = floatArrayOf(0.46f, 0.68f, 0.84f)
private val RING_OUTER_RATIOS = floatArrayOf(0.68f, 0.84f, 1.0f)

/** 弧段在分类树中的索引路径；不能只用分类 ID，因为多个未分类根可能共用占位 ID。 */
private data class RingSegment(
    val path: List<Int>,
    val usage: CategoryUsage,
    val layer: Int,
    val startAngle: Float,
    val sweepAngle: Float,
)

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

@Composable
private fun RingSection(
    summary: StatsSummary,
    segments: List<RingSegment>,
    selectedSegment: RingSegment?,
    onSegmentSelected: (List<Int>?) -> Unit,
) {
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val isEmpty = summary.totalMillis <= 0L
    val minimumTouchRadius = with(LocalDensity.current) { 24.dp.toPx() }
    val currentOnSegmentSelected by rememberUpdatedState(onSegmentSelected)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(segments, minimumTouchRadius, isEmpty) {
                        detectTapGestures { position ->
                            if (!isEmpty) {
                                val hit = findRingSegment(
                                    position = position,
                                    canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                                    segments = segments,
                                    minimumTouchRadius = minimumTouchRadius,
                                )
                                currentOnSegmentSelected(hit?.path)
                            }
                        }
                    }
                    .semantics {
                        contentDescription = "三层分类用时环图，可选择弧段查看分类详情"
                        stateDescription = selectedSegment?.let { "已选择${it.usage.name}" } ?: "未选择分类"
                        customActions = segments.map { segment ->
                            CustomAccessibilityAction(
                                label = "选择${segment.usage.name}，第${segment.layer + 1}层",
                            ) {
                                currentOnSegmentSelected(segment.path)
                                true
                            }
                        }
                    },
            ) {
                val radius = minOf(size.width, size.height) / 2f
                if (radius <= 0f) return@Canvas
                val center = Offset(size.width / 2f, size.height / 2f)
                if (isEmpty) {
                    for (layer in 0 until RING_LAYER_COUNT) {
                        drawRingTrack(layer, center, radius, trackColor)
                    }
                } else {
                    val populatedLayers = segments.mapTo(mutableSetOf()) { it.layer }
                    for (layer in 0 until RING_LAYER_COUNT) {
                        if (layer in populatedLayers) drawRingTrack(layer, center, radius, trackColor)
                    }
                    segments.forEach { segment ->
                        val geometry = ringGeometry(segment.layer, center, radius)
                        val selected = segment.path == selectedSegment?.path
                        drawArc(
                            color = Color(segment.usage.colorHex).copy(
                                alpha = if (selectedSegment == null || selected) 1f else 0.55f,
                            ),
                            startAngle = segment.startAngle,
                            sweepAngle = segment.sweepAngle,
                            useCenter = false,
                            topLeft = geometry.topLeft,
                            size = geometry.size,
                            style = Stroke(
                                width = geometry.strokeWidth + if (selected) 2.dp.toPx() else 0f,
                            ),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.widthIn(max = 124.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (selectedSegment != null) {
                    Text(
                        text = selectedSegment.usage.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = when {
                        isEmpty -> "—"
                        selectedSegment != null -> DateTimeFormat.durationText(selectedSegment.usage.millis)
                        else -> DateTimeFormat.durationText(summary.totalMillis)
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (selectedSegment != null) {
                        "第${selectedSegment.layer + 1}层 · ${selectedSegment.usage.eventCount} 条记录"
                    } else {
                        "共 ${summary.eventCount} 条记录"
                    },
                    fontSize = 11.sp,
                    color = mutedColor,
                    maxLines = 1,
                )
            }
        }
        if (!isEmpty) {
            Text(
                text = "内 → 外：一级 · 二级 · 三级",
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 10.sp,
                color = mutedColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun EmptyStats() {
    Text(
        text = "这段时间还没有记录",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** 下钻时显示在范围导航下方的返回按钮与面包屑。 */
@Composable
private fun DrillHeader(
    path: List<StatisticsCrumb>,
    onDrillUp: () -> Unit,
    onCrumbSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDrillUp) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "返回上一级分类",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CrumbText(text = "全部分类", active = false) { onCrumbSelected(0) }
            path.forEachIndexed { index, crumb ->
                Text(
                    text = "/",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CrumbText(text = crumb.name, active = index == path.lastIndex) {
                    onCrumbSelected(index + 1)
                }
            }
        }
    }
}

/** 面包屑的一节；[active] 为当前层级，不可再点。 */
@Composable
private fun CrumbText(text: String, active: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = !active, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        fontSize = 13.sp,
        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        color = if (active) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.primary
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** 下钻到的分类本身有记录、但没有可继续细分的子分类。 */
@Composable
private fun DrillEmptyHint() {
    Text(
        text = "该分类下暂无可细分的子分类",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** 行主体只负责选中；[onDrill] 不为 null 时，行尾独立的 48dp 箭头负责下钻。 */
@Composable
private fun CategoryUsageRow(
    usage: CategoryUsage,
    maxMillis: Long,
    riseColor: Color,
    fallColor: Color,
    selected: Boolean,
    onSelect: () -> Unit,
    onDrill: ((Long) -> Unit)? = null,
) {
    val accent = Color(usage.colorHex)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val fraction = if (maxMillis <= 0L) {
        0f
    } else {
        (usage.millis.toDouble() / maxMillis.toDouble()).toFloat().coerceIn(0.03f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.09f) else Color.Transparent,
            )
            .selectable(selected = selected, onClick = onSelect)
            .heightIn(min = 56.dp)
            .padding(horizontal = 8.dp, vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = usage.name,
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${usage.eventCount}次",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            DeltaLabel(usage = usage, riseColor = riseColor, fallColor = fallColor)
            Spacer(Modifier.width(8.dp))
            Text(
                text = DateTimeFormat.durationText(usage.millis),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            if (onDrill != null) {
                // 保留 48dp 触控区域，但不让按钮撑高文字行，避免比例条被向下推。
                Box(
                    modifier = Modifier.width(48.dp).height(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = { onDrill(usage.categoryId) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "查看${usage.name}的下一级分类",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent),
            )
        }
    }
}

@Composable
private fun DeltaLabel(usage: CategoryUsage, riseColor: Color, fallColor: Color) {
    val previous = usage.previousMillis
    if (previous <= 0L) {
        Text(
            text = "新增",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = riseColor,
            maxLines = 1,
        )
        return
    }
    val percent = ((usage.millis - previous).toDouble() / previous.toDouble() * 100.0).roundToInt()
    val text = when {
        percent > 0 -> "+$percent%"
        percent < 0 -> "$percent%"
        else -> "0%"
    }
    val color = when {
        percent > 0 -> riseColor
        percent < 0 -> fallColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        maxLines = 1,
    )
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

/** 单层环带的几何参数：绘制矩形、直径与环宽。 */
private class RingGeometry(val topLeft: Offset, val size: Size, val strokeWidth: Float)

private fun ringGeometry(layer: Int, center: Offset, radius: Float): RingGeometry {
    val innerRadius = radius * RING_INNER_RATIOS[layer]
    val outerRadius = radius * RING_OUTER_RATIOS[layer]
    val ringRadius = (innerRadius + outerRadius) / 2f
    return RingGeometry(
        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
        size = Size(ringRadius * 2f, ringRadius * 2f),
        strokeWidth = outerRadius - innerRadius,
    )
}

/** 画一整圈淡色轨道。 */
private fun DrawScope.drawRingTrack(layer: Int, center: Offset, radius: Float, color: Color) {
    val geometry = ringGeometry(layer, center, radius)
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = geometry.topLeft,
        size = geometry.size,
        style = Stroke(width = geometry.strokeWidth),
    )
}

/** 预先展开弧段，让绘制、触摸命中与无障碍操作共享同一组几何数据。 */
private fun buildRingSegments(roots: List<CategoryUsage>): List<RingSegment> {
    val segments = mutableListOf<RingSegment>()
    appendRingSegments(
        usages = roots,
        layer = 0,
        startAngle = -90f,
        totalSweep = 360f,
        parentPath = emptyList(),
        segments = segments,
    )
    return segments
}

private fun appendRingSegments(
    usages: List<CategoryUsage>,
    layer: Int,
    startAngle: Float,
    totalSweep: Float,
    parentPath: List<Int>,
    segments: MutableList<RingSegment>,
) {
    if (layer >= RING_LAYER_COUNT || usages.isEmpty()) return
    val totalMillis = usages.sumOf { it.millis }
    if (totalMillis <= 0L) return

    var cursor = startAngle
    usages.forEachIndexed { index, usage ->
        val path = parentPath + index
        val sweep = (totalSweep.toDouble() * usage.millis.toDouble() / totalMillis.toDouble()).toFloat()
        val visibleSweep = (sweep - RING_GAP_ANGLE).coerceAtLeast(RING_MIN_ANGLE).coerceAtMost(sweep)
        if (visibleSweep > 0f) {
            segments += RingSegment(
                path = path,
                usage = usage,
                layer = layer,
                startAngle = cursor + (sweep - visibleSweep) / 2f,
                sweepAngle = visibleSweep,
            )
        }
        if (usage.children.isNotEmpty()) {
            appendRingSegments(
                usages = usage.children,
                layer = layer + 1,
                startAngle = cursor,
                totalSweep = sweep,
                parentPath = path,
                segments = segments,
            )
        }
        cursor += sweep
    }
}

/** 命中可见弧段；环带过窄时将径向触摸范围扩到至少 48dp，并取离环心线最近者。 */
private fun findRingSegment(
    position: Offset,
    canvasSize: Size,
    segments: List<RingSegment>,
    minimumTouchRadius: Float,
): RingSegment? {
    val radius = minOf(canvasSize.width, canvasSize.height) / 2f
    if (radius <= 0f) return null
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val dx = position.x - center.x
    val dy = position.y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    val angle = (atan2(dy, dx) * 180.0 / PI).toFloat()

    return segments.mapNotNull { segment ->
        val geometry = ringGeometry(segment.layer, center, radius)
        val ringRadius = geometry.size.width / 2f
        val radialDistance = abs(distance - ringRadius)
        val touchRadius = maxOf(geometry.strokeWidth / 2f, minimumTouchRadius)
        if (radialDistance > touchRadius) return@mapNotNull null
        val angleInSegment = normalizedAngle(angle - segment.startAngle)
        if (angleInSegment > segment.sweepAngle) {
            null
        } else {
            segment to radialDistance
        }
    }.minByOrNull { it.second }?.first
}

private fun normalizedAngle(angle: Float): Float {
    val remainder = angle % 360f
    return if (remainder < 0f) remainder + 360f else remainder
}
