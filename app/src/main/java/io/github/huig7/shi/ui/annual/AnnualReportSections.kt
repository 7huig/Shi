package io.github.huig7.shi.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.CategoryUsage
import io.github.huig7.shi.util.DateTimeFormat
import kotlin.math.roundToInt

private val BAR_GAP = 4.dp

@Composable
internal fun ChapterLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
internal fun PageLead(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
        fontSize = 22.sp,
        lineHeight = 32.sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
internal fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun CategoryBar(usage: CategoryUsage, totalMillis: Long) {
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
            .padding(vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = usage.name,
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$percent%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = DateTimeFormat.durationText(usage.millis),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(accent),
            )
        }
    }
}

@Composable
internal fun SeasonBar(
    name: String,
    millis: Long,
    totalMillis: Long,
    accent: Color,
) {
    val fraction = if (totalMillis <= 0L) {
        0f
    } else {
        (millis.toDouble() / totalMillis.toDouble()).toFloat().coerceIn(0f, 1f)
    }
    val percent = (fraction * 100f).roundToInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$percent%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (millis > 0L) DateTimeFormat.durationText(millis) else "—",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
            )
        }
    }
}

/**
 * 十二个月柱状图。
 *
 * 高度按最大值归一；有记录但占比极小的月份至少保留 4% 高度。
 * 颜色可覆盖，分享长图用纸色 + 松绿的固定配色。
 */
@Composable
internal fun MonthBars(
    months: List<Long>,
    highlightMonth: Int?,
    modifier: Modifier = Modifier,
    barHeight: Dp = 130.dp,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    highlightLabelColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val max = months.maxOrNull() ?: 0L
    val mutedBar = barColor.copy(alpha = 0.32f)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            horizontalArrangement = Arrangement.spacedBy(BAR_GAP),
            verticalAlignment = Alignment.Bottom,
        ) {
            months.forEachIndexed { index, millis ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (millis > 0L && max > 0L) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight((millis.toDouble() / max.toDouble()).toFloat().coerceIn(0.04f, 1f))
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(if (index + 1 == highlightMonth) barColor else mutedBar),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BAR_GAP),
        ) {
            months.forEachIndexed { index, _ ->
                Text(
                    text = "${index + 1}",
                    modifier = Modifier.weight(1f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    color = if (index + 1 == highlightMonth) highlightLabelColor else labelColor,
                )
            }
        }
    }
}
