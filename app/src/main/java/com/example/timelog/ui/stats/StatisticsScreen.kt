package com.example.timelog.ui.stats

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timelog.domain.CategoryDuration
import com.example.timelog.domain.DayDuration
import com.example.timelog.domain.StatsPeriod
import com.example.timelog.domain.StatsSummary
import com.example.timelog.util.DateTimeFormat
import java.time.format.TextStyle
import java.util.Locale

/**
 * 统计页（分页容器里的一页，自己不带 Scaffold / 顶栏）。
 * 内容顺序：时间范围切换 -> 总时长与条数 -> 一级分类用时 -> 最近 7 天迷你柱状图 -> [footer]。
 * 颜色取自主题（浅色是 Paper / Ink / Pine / Clay，深色由 colorScheme 自动跟随）。
 */
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onPeriodSelected: (StatsPeriod) -> Unit,
    footer: @Composable () -> Unit = {},
) {
    val summary = state.summary
    val maxCategoryMillis = summary.categories.maxOfOrNull { it.millis } ?: 0L

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
    ) {
        item(key = "period-selector") {
            PeriodSelector(period = state.period, onPeriodSelected = onPeriodSelected)
        }
        if (!state.isLoading) {
            if (summary.totalMillis == 0L) {
                item(key = "empty") { EmptyStats() }
            } else {
                item(key = "total") { TotalHeadline(summary = summary) }
                item(key = "category-title") { SectionTitle("分类用时") }
                items(
                    items = summary.categories,
                    key = { category -> "category-" + category.rootName },
                ) { category ->
                    CategoryBarRow(
                        category = category,
                        maxCategoryMillis = maxCategoryMillis,
                    )
                }
            }
            if (summary.last7Days.any { it.millis > 0L }) {
                item(key = "last-7-days") { Last7DaysChart(days = summary.last7Days) }
            }
        }
        item(key = "footer") { footer() }
    }
}

@Composable
private fun PeriodSelector(
    period: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatsPeriod.entries.forEach { candidate ->
            FilterChip(
                selected = candidate == period,
                onClick = { onPeriodSelected(candidate) },
                label = { Text(periodLabel(candidate)) },
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

private fun periodLabel(period: StatsPeriod): String = when (period) {
    StatsPeriod.TODAY -> "今天"
    StatsPeriod.WEEK -> "本周"
    StatsPeriod.MONTH -> "本月"
    StatsPeriod.ALL -> "全部"
}

@Composable
private fun TotalHeadline(summary: StatsSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
    ) {
        Text(
            text = "总时长",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = DateTimeFormat.durationText(summary.totalMillis),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "共 " + summary.eventCount + " 条记录",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun CategoryBarRow(
    category: CategoryDuration,
    maxCategoryMillis: Long,
) {
    val fraction = if (maxCategoryMillis <= 0L) {
        0f
    } else {
        (category.millis.toFloat() / maxCategoryMillis.toFloat()).coerceIn(0.02f, 1f)
    }
    val accent = Color(category.colorHex)
    val track = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = category.rootName.ifBlank { "未分类" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = DateTimeFormat.durationText(category.millis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(track),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent),
            )
        }
    }
}

@Composable
private fun Last7DaysChart(days: List<DayDuration>) {
    val maxMillis = days.maxOfOrNull { it.millis } ?: 0L
    val today = days.lastOrNull()?.date
    val barArea = 56.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
    ) {
        Text(
            text = "最近 7 天",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { day ->
                val isToday = day.date == today
                val fraction = if (maxMillis <= 0L) {
                    0f
                } else {
                    (day.millis.toFloat() / maxMillis.toFloat()).coerceIn(0f, 1f)
                }
                val barHeight = (fraction * barArea.value).coerceAtLeast(
                    if (day.millis > 0L) 3f else 2f,
                )
                val barColor = when {
                    day.millis == 0L -> MaterialTheme.colorScheme.surfaceVariant
                    isToday -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primary
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barArea),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isToday) {
                            "今天"
                        } else {
                            day.date.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.SIMPLIFIED_CHINESE,
                            )
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isToday) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStats() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp),
    ) {
        Text(
            text = "这段时间还没有记录。",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "换一个时间范围看看，或回到时间流记下刚做完的事。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
