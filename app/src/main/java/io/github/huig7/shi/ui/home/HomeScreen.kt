package io.github.huig7.shi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.ui.theme.Clay
import io.github.huig7.shi.util.DateTimeFormat

/**
 * 时间轴页：不再自带 Scaffold / 顶栏 / 底栏，外壳（MainActivity）统一提供页签与右下角「+」按钮，
 * 这样时间轴与统计页可以放在同一个 HorizontalPager 里左右滑动切换。
 */
@Composable
fun TimelinePage(
    state: HomeUiState,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Unit
            state.days.isEmpty() -> EmptyTimeline()
            else -> TimelineList(
                days = state.days,
                uncategorizedName = state.uncategorized?.name ?: "未分类",
                uncategorizedColor = state.uncategorized?.colorHex,
                onEventClick = onEventClick,
                onEventLongClick = onEventLongClick,
                onFillGap = onFillGap,
            )
        }
    }

    state.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("删除这段记录？") },
            text = {
                Column {
                    Text(
                        "「${pending.categoryPath}」" +
                            DateTimeFormat.rangeText(pending.event.startAt, pending.event.endAt),
                    )
                    pending.event.note?.let { note ->
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text("取消") }
            },
        )
    }
}

/**
 * 时间轴页右下角的悬浮加号：主题主色圆圈 + 对应前景色「+」，点击新建记录。
 * 由外壳（MainActivity）叠加在时间轴页之上；列表底部已预留 96dp，避免遮住最后一条记录。
 */
@Composable
fun TimelineFabOverlay(onNewRecord: () -> Unit, modifier: Modifier = Modifier) {
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
                .clickable(role = Role.Button, onClick = onNewRecord),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "新建记录",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun EmptyTimeline() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "还没有记录。",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "点右下角的 +，记下刚刚做完的事。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimelineList(
    days: List<HomeDay>,
    uncategorizedName: String,
    uncategorizedColor: Long?,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
) {
    val listState = rememberLazyListState()
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
                DayHeader(day = day)
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
                    uncategorizedName = uncategorizedName,
                    uncategorizedColor = uncategorizedColor,
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
}

@Composable
private fun DayHeader(day: HomeDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = day.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = day.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

@Composable
private fun NowMarker() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "现在",
                style = MaterialTheme.typography.labelSmall,
                color = Clay,
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
