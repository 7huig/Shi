package com.example.timelog.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timelog.domain.model.TimelineSegment
import com.example.timelog.util.DateTimeFormat

private val BlockShape = RoundedCornerShape(14.dp)
private val EventShape = RoundedCornerShape(8.dp)

@Composable
fun CategoryDot(
    colorHex: Long,
    size: Dp = 7.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color(colorHex)),
    )
}

/**
 * 时间流里的一行：左侧只在块与块的交接处显示时间，右侧是块本体。
 * 未记录的时间由虚线「未分类」块占位。
 *
 * 合并块里每条记录各自可点：重叠记录以前整块不可点，导致无法编辑，这里按事件拆开点击区域。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineSegmentRow(
    segment: TimelineSegment,
    uncategorizedName: String = "未分类",
    uncategorizedColor: Long? = null,
    onEventClick: (Long) -> Unit = {},
    onEventLongClick: (Long) -> Unit = {},
    onFillGap: (() -> Unit)? = null,
) {
    val height = rememberBlockHeight(segment)
    val hairline = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(modifier = Modifier.width(44.dp).height(height)) {
            Text(
                text = DateTimeFormat.time(segment.startAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 3.dp),
            )
        }
        androidx.compose.foundation.Canvas(modifier = Modifier.width(6.dp).height(height)) {
            val y = 8.dp.toPx()
            drawLine(
                color = hairline,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }
        Spacer(Modifier.width(4.dp))

        val blockModifier = Modifier
            .weight(1f)
            .height(height)
        if (segment.isUnrecorded) {
            // 空隙本身就是一段「未分类」时间：点它可以就地补记。
            val fillable = if (onFillGap != null && segment.endAt - segment.startAt >= 60_000L) {
                blockModifier.combinedClickable(onClick = { onFillGap() }, onLongClick = {})
            } else {
                blockModifier
            }
            UnrecordedBlock(
                segment = segment,
                name = uncategorizedName,
                colorHex = uncategorizedColor,
                modifier = fillable,
            )
        } else {
            RecordedBlock(
                segment = segment,
                modifier = blockModifier,
                onEventClick = onEventClick,
                onEventLongClick = onEventLongClick,
            )
        }
    }
}

@Composable
private fun RecordedBlock(
    segment: TimelineSegment,
    modifier: Modifier,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
) {
    val accent = Color(segment.events.first().categoryColor)
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val textColor = if (darkTheme) lerp(accent, Color.White, 0.35f) else accent
    val background = accent.copy(alpha = if (darkTheme) 0.20f else 0.09f)
    val border = accent.copy(alpha = if (darkTheme) 0.38f else 0.22f)

    Box(
        modifier = modifier
            .clip(BlockShape)
            .background(background)
            .border(1.dp, border, BlockShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(accent.copy(alpha = if (darkTheme) 0.85f else 0.7f)),
        )
        Column(
            modifier = Modifier.padding(start = 13.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
        ) {
            segment.events.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(Modifier.height(2.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EventShape)
                        .combinedClickable(
                            onClick = { onEventClick(item.event.id) },
                            onLongClick = { onEventLongClick(item.event.id) },
                        )
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryDot(colorHex = item.categoryColor, size = 6.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.categoryPath,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    item.event.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnrecordedBlock(
    segment: TimelineSegment,
    name: String,
    colorHex: Long?,
    modifier: Modifier,
) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    val height = rememberBlockHeight(segment)
    val labelColor = colorHex?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                shape = BlockShape,
            )
            .drawBehind {
                val stroke = 1.dp.toPx()
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(
                                left = stroke / 2f,
                                top = stroke / 2f,
                                right = size.width - stroke / 2f,
                                bottom = size.height - stroke / 2f,
                            ),
                            cornerRadius = CornerRadius(14.dp.toPx()),
                        ),
                    )
                }
                drawPath(
                    path = path,
                    color = outline,
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(5.dp.toPx(), 5.dp.toPx()),
                        ),
                    ),
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (colorHex != null) {
                    CategoryDot(colorHex = colorHex, size = 6.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
            if (height >= 62.dp) {
                Text(
                    text = DateTimeFormat.durationText(segment.startAt, segment.endAt),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/** 块高按分钟数近似线性，并做上下限，避免 8 小时块占满整屏。 */
@Composable
private fun rememberBlockHeight(segment: TimelineSegment): Dp {
    val minutes = ((segment.endAt - segment.startAt).coerceAtLeast(60_000L)) / 60_000f
    return if (segment.isUnrecorded) {
        (42f + minutes * 0.45f).coerceIn(46f, 112f).dp
    } else {
        val extraRows = (segment.events.size - 1).coerceAtLeast(0) * 26f
        (54f + minutes * 0.55f + extraRows).coerceIn(58f, 208f).dp
    }
}
