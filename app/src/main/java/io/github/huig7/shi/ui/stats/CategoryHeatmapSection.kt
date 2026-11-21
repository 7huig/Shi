package io.github.huig7.shi.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.plan.CategoryHeatmap
import io.github.huig7.shi.ui.home.CategoryDot
import io.github.huig7.shi.util.DateTimeFormat

/** 時計页顶部的分类热力图：只为在目标与提醒里显式开启的分类出现。 */
@Composable
fun CategoryHeatmapSection(heatmaps: List<CategoryHeatmap>) {
    if (heatmaps.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "分类热力图",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        heatmaps.forEach { heatmap ->
            HeatmapRow(heatmap)
        }
    }
}

@Composable
private fun HeatmapRow(heatmap: CategoryHeatmap) {
    val accent = Color(heatmap.colorHex)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(colorHex = heatmap.colorHex, size = 8.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = heatmap.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = totalText(heatmap.totalMillis) + " · " + heatmap.activeDays + " 天",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        HeatmapGrid(heatmap = heatmap, accent = accent)
        Spacer(Modifier.height(4.dp))
        Text(
            text = DateTimeFormat.monthDay(heatmap.days.last().date),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 7 行 × 24 列的日格：按日期顺序每 7 天换一列。
 *
 * 用 Canvas 一次画完，避免一百多个 Box 在滚动列表里反复测量。
 */
@Composable
private fun HeatmapGrid(heatmap: CategoryHeatmap, accent: Color) {
    val days = heatmap.days
    if (days.isEmpty()) return
    val rows = 7
    val columns = (days.size + rows - 1) / rows
    val cell = 9.dp
    val gap = 2.dp
    val gridWidth = cell * columns + gap * (columns - 1)
    val gridHeight = cell * rows + gap * (rows - 1)
    val empty = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
    val max = heatmap.maxMillis

    Canvas(
        modifier = Modifier
            .width(gridWidth)
            .height(gridHeight),
    ) {
        val cellPx = cell.toPx()
        val gapPx = gap.toPx()
        val radius = CornerRadius(2.dp.toPx())
        days.forEachIndexed { index, day ->
            val column = index / rows
            val row = index % rows
            val x = column * (cellPx + gapPx)
            val y = row * (cellPx + gapPx)
            val color = if (day.millis <= 0L || max <= 0L) {
                empty
            } else {
                val ratio = (day.millis.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                accent.copy(alpha = 0.20f + 0.72f * ratio)
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(cellPx, cellPx),
                cornerRadius = radius,
            )
        }
    }
}

private fun totalText(millis: Long): String =
    if (millis <= 0L) "0 分钟" else DateTimeFormat.durationText(millis)
