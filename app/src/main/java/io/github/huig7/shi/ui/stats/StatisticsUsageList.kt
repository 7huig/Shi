package io.github.huig7.shi.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.CategoryUsage
import io.github.huig7.shi.util.DateTimeFormat
import kotlin.math.roundToInt

@Composable
internal fun SectionHeader(text: String) {
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
internal fun EmptyStats() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Text(
            text = "这段时间还没有记录",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** 行主体只负责选中；[onDrill] 不为 null 时，行尾独立的 48dp 箭头负责下钻。 */
@Composable
internal fun CategoryUsageRow(
    usage: CategoryUsage,
    totalMillis: Long,
    riseColor: Color,
    fallColor: Color,
    selected: Boolean,
    onSelect: () -> Unit,
    onDrill: ((Long) -> Unit)? = null,
) {
    val accent = Color(usage.colorHex)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val fraction = if (totalMillis <= 0L) {
        0f
    } else {
        (usage.millis.toDouble() / totalMillis.toDouble()).toFloat().coerceIn(0.02f, 1f)
    }
    val percent = if (totalMillis <= 0L) {
        0
    } else {
        (usage.millis.toDouble() / totalMillis.toDouble() * 100.0).roundToInt()
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
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "$percent%",
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
            // 所有行都预留同一个箭头槽：底层行留空，非底层行放箭头，
            // 这样时长与环比在所有行上共用同一条右对齐基线。
            if (onDrill != null) {
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
            } else {
                Spacer(Modifier.width(48.dp))
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        return
    }
    val percent = ((usage.millis - previous).toDouble() / previous.toDouble() * 100.0).roundToInt()
    val text = when {
        percent > 0 -> "▲ $percent%"
        percent < 0 -> "▼ ${-percent}%"
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
