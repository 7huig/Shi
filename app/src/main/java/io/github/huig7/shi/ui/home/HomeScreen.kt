package io.github.huig7.shi.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.ui.common.DatePickerSheet
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * 时间轴页：外壳（MainActivity）统一提供页签、搜索、分类菜单与右下角「+」按钮，
 * 这样时间轴与统计页可以放在同一个 HorizontalPager 里左右滑动切换。
 */
@Composable
fun TimelinePage(
    state: HomeUiState,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
    onNewRecord: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Unit
            state.days.isEmpty() -> EmptyTimeline(onNewRecord = onNewRecord)
            else -> TimelineList(
                days = state.days,
                onEventClick = onEventClick,
                onEventLongClick = onEventLongClick,
                onFillGap = onFillGap,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

/**
 * 时间轴页右下角的悬浮加号：主题主色圆圈 + 对应前景色「+」，点击新建记录。
 * 长按由外壳接管为「快速记录」；列表底部已预留 96dp，避免遮住最后一条记录。
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TimelineFabOverlay(
    onNewRecord: () -> Unit,
    onQuickRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 24.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .size(62.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "新建记录",
                    onLongClickLabel = "快速记录",
                    onClick = onNewRecord,
                    onLongClick = onQuickRecord,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "新建记录，长按快速记录",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun EmptyTimeline(onNewRecord: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "还没有记录。",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "先记下刚刚做过的一件事；不必一次记准，之后随时可以改。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onNewRecord,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier.height(48.dp),
        ) {
            Text("记下刚刚的 30 分钟", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "也可以点右下角的 +；长按它可以直接快速记录。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimelineList(
    days: List<HomeDay>,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var pickerDate by remember { mutableStateOf<LocalDate?>(null) }
    val itemCount = days.size + days.sumOf { it.segments.size } + days.count { it.isToday }
    // 倒序改成正序后，最新的记录在列表底部，靠近右下角的「+」；第一次打开与新增记录后都停在底部。
    val lastRecordedAt = remember(days) {
        days.flatMap { it.segments }.filterNot { it.isUnrecorded }
            .maxOfOrNull { it.endAt } ?: 0L
    }
    var hasPositioned by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(lastRecordedAt, itemCount) {
        if (itemCount == 0) return@LaunchedEffect
        if (!hasPositioned) {
            listState.scrollToItem(itemCount - 1)
            hasPositioned = true
        } else if (lastRecordedAt > 0L) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        // 底部留 96dp 给右下角的悬浮「+」，避免遮住最后一条记录。
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
    ) {
        days.forEach { day ->
            item(key = "day-${day.date}") {
                DayHeader(day = day, onDateClick = { pickerDate = day.date })
            }
            itemsIndexed(
                items = day.segments,
                key = { _, segment ->
                    "seg-${day.date}-${segment.startAt}-" +
                        segment.events.joinToString(",") { it.event.id.toString() }
                },
            ) { index, segment ->
                TimelineSegmentRow(
                    segment = segment,
                    onEventClick = onEventClick,
                    onEventLongClick = onEventLongClick,
                    onFillGap = { onFillGap(segment.startAt, segment.endAt) },
                    centerTimeLabel = index > 0,
                )
            }
            if (day.isToday) {
                item(key = "now-${day.date}") {
                    NowMarker()
                }
            }
        }
    }

    pickerDate?.let { initialDate ->
        DatePickerSheet(
            initialDate = initialDate,
            onDismiss = { pickerDate = null },
            onConfirm = { date ->
                pickerDate = null
                val target = indexOfDay(days, date)
                if (target == null) {
                    scope.launch { snackbarHostState.showSnackbar("这天没有记录") }
                } else {
                    scope.launch { listState.animateScrollToItem(target) }
                }
            },
        )
    }
}

/** 某一天在 LazyColumn 里的 header 下标；不存在时返回 null。 */
private fun indexOfDay(days: List<HomeDay>, date: LocalDate): Int? {
    var index = 0
    for (day in days) {
        if (day.date == date) return index
        index += 1 + day.segments.size + if (day.isToday) 1 else 0
    }
    return null
}

@Composable
private fun DayHeader(day: HomeDay, onDateClick: () -> Unit) {
    val recorded = day.segments.filterNot { it.isUnrecorded }.sumOf { it.endAt - it.startAt }
    val gap = day.segments.filter { it.isUnrecorded }.sumOf { it.endAt - it.startAt }
    val summary = buildString {
        append("已记录 ").append(DateTimeFormat.durationText(recorded))
        if (gap > 0L) {
            append(" · 空档 ").append(DateTimeFormat.durationText(gap))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "切换日期", onClick = onDateClick)
            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = day.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = day.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            maxLines = 1,
        )
        Spacer(Modifier.height(5.dp))
        DayOverviewBar(day = day, modifier = Modifier.fillMaxWidth().height(4.dp))
    }
}

/** 一天 24 小时的记录密度：有记录的弧段用分类色，空档保持轨道色。 */
@Composable
private fun DayOverviewBar(day: HomeDay, modifier: Modifier) {
    val zone = DateTimeFormat.zone()
    val dayStart = day.date.atStartOfDay(zone).toInstant().toEpochMilli()
    val dayEnd = day.date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val span = (dayEnd - dayStart).coerceAtLeast(1L)
    val track = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    Canvas(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
    ) {
        drawRect(color = track, size = size)
        day.segments.forEach { segment ->
            if (segment.isUnrecorded) return@forEach
            val start = segment.startAt.coerceIn(dayStart, dayEnd)
            val end = segment.endAt.coerceIn(dayStart, dayEnd)
            if (end <= start) return@forEach
            val left = ((start - dayStart).toFloat() / span.toFloat()) * size.width
            val width = ((end - start).toFloat() / span.toFloat()) * size.width
            drawRect(
                color = Color(segment.events.first().categoryColor),
                topLeft = Offset(left, 0f),
                size = Size(width.coerceAtLeast(1f), size.height),
            )
        }
    }
}

@Composable
private fun NowMarker() {
    val gutter = (48f * LocalDensity.current.fontScale).coerceIn(48f, 96f).dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(gutter), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "现在",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 2.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}
