package com.example.timelog.ui.home

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timelog.ui.theme.Clay
import com.example.timelog.util.DateTimeFormat

@Composable
fun HomeScreen(
    state: HomeUiState,
    onNewRecord: () -> Unit,
    onOpenCategories: () -> Unit,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HomeHeader(onOpenCategories = onOpenCategories) },
        bottomBar = { RecordBar(onNewRecord = onNewRecord) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Unit
                state.days.isEmpty() -> EmptyTimeline()
                else -> TimelineList(
                    days = state.days,
                    onEventClick = onEventClick,
                    onEventLongClick = onEventLongClick,
                    onFillGap = onFillGap,
                )
            }
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

@Composable
private fun HomeHeader(onOpenCategories: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "時計",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onOpenCategories) {
            Text("分类", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun RecordBar(onNewRecord: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
    ) {
        Button(
            onClick = onNewRecord,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("完成", style = MaterialTheme.typography.titleMedium)
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
            text = "点下方的「完成」，记下刚刚做完的事。",
            style = MaterialTheme.typography.bodyMedium,
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
) {
    val listState = rememberLazyListState()
    val itemCount = days.size + days.sumOf { it.segments.size } + days.count { it.isToday }
    // 倒序改成正序后，最新的记录在列表底部，靠近「完成」键；第一次打开与新增记录后都停在底部。
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
        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
    ) {
        days.forEach { day ->
            item(key = "day-${day.date}") {
                DayHeader(day = day)
            }
            items(
                items = day.segments,
                key = { segment ->
                    "seg-${day.date}-${segment.startAt}-" +
                        segment.events.joinToString(",") { it.event.id.toString() }
                },
            ) { segment ->
                val single = segment.events.singleOrNull()
                TimelineSegmentRow(
                    segment = segment,
                    onClick = single?.let { event -> { onEventClick(event.event.id) } },
                    onLongClick = single?.let { event -> { onEventLongClick(event.event.id) } },
                    onFillGap = { onFillGap(segment.startAt, segment.endAt) },
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
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterEnd) {
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
