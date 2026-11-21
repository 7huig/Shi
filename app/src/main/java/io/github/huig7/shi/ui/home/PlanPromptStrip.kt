package io.github.huig7.shi.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.plan.PlanPrompt
import io.github.huig7.shi.domain.plan.PlanPromptKind
import io.github.huig7.shi.util.DateTimeFormat

/**
 * 首页顶部的「今日提示」横向卡片。
 *
 * 只展示用户在「目标与提醒」里主动配置过的分类；不弹通知、不打断记录。
 * 点卡片记一笔对应分类，间隔待办可以点「完成」把下一次时间往后推。
 */
@Composable
fun PlanPromptStrip(
    prompts: List<PlanPrompt>,
    onRecord: (Long) -> Unit,
    onComplete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (prompts.isEmpty()) return
    Column(modifier = modifier.padding(top = 4.dp, bottom = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "今日提示",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${prompts.size} 件",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        ) {
            items(items = prompts, key = { "${it.kind}-${it.categoryId}" }) { prompt ->
                PlanPromptCard(
                    prompt = prompt,
                    onRecord = { onRecord(prompt.categoryId) },
                    onComplete = { onComplete(prompt.categoryId) },
                )
            }
        }
    }
}

@Composable
private fun PlanPromptCard(
    prompt: PlanPrompt,
    onRecord: () -> Unit,
    onComplete: () -> Unit,
) {
    val accent = Color(prompt.colorHex)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = Modifier
            .width(238.dp)
            .height(116.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onRecord),
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = if (dark) 0.16f else 0.11f),
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = if (dark) 0.30f else 0.18f),
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryDot(colorHex = prompt.colorHex, size = 7.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = kindLabel(prompt.kind),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = accent,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.weight(1f))
                if (prompt.due) {
                    Text(
                        text = "现在",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = accent,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = prompt.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            when (prompt.kind) {
                PlanPromptKind.GOAL -> GoalBody(prompt, accent)
                PlanPromptKind.FIXED_TIME -> FixedBody(prompt, accent)
                PlanPromptKind.INTERVAL -> IntervalBody(prompt, accent, onComplete)
            }
        }
    }
}

@Composable
private fun GoalBody(prompt: PlanPrompt, accent: Color) {
    val ratio = if (prompt.goalTargetMillis <= 0L) {
        0f
    } else {
        (prompt.goalDoneMillis.toFloat() / prompt.goalTargetMillis.toFloat()).coerceIn(0f, 1f)
    }
    Text(
        text = shortDuration(prompt.goalDoneMillis) + " / " + shortDuration(prompt.goalTargetMillis),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
    Spacer(Modifier.height(6.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent.copy(alpha = 0.75f)),
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = if (prompt.due) {
            "还差 " + shortDuration(prompt.goalTargetMillis - prompt.goalDoneMillis)
        } else {
            "今日目标已达成"
        },
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = if (prompt.due) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            accent
        },
        maxLines = 1,
    )
}

@Composable
private fun FixedBody(prompt: PlanPrompt, accent: Color) {
    val minute = prompt.fixedTimeMinuteOfDay ?: 0
    val clock = "%02d:%02d".format(minute / 60, minute % 60)
    Text(
        text = when {
            prompt.fixedDone -> "$clock · 已记录"
            prompt.due -> "$clock · 到点了"
            else -> "$clock · 今天"
        },
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = if (prompt.due) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
private fun IntervalBody(prompt: PlanPrompt, accent: Color, onComplete: () -> Unit) {
    Text(
        text = if (prompt.due) {
            "该做了"
        } else {
            prompt.nextReminderAt?.let { "下次 " + DateTimeFormat.time(it) } ?: "该做了"
        },
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = if (prompt.due) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "每 " + intervalText(prompt.intervalMinutes ?: 0),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Surface(
            onClick = onComplete,
            shape = RoundedCornerShape(10.dp),
            color = accent.copy(alpha = if (prompt.due) 0.18f else 0.10f),
        ) {
            Text(
                text = "完成",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = accent,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }
    }
}

private fun kindLabel(kind: PlanPromptKind): String = when (kind) {
    PlanPromptKind.GOAL -> "今日目标"
    PlanPromptKind.FIXED_TIME -> "固定时间"
    PlanPromptKind.INTERVAL -> "间隔待办"
}

private fun shortDuration(millis: Long): String =
    if (millis <= 0L) "0 分钟" else DateTimeFormat.durationText(millis)

private fun intervalText(minutes: Int): String {
    val safe = minutes.coerceAtLeast(0)
    val hours = safe / 60
    val rest = safe % 60
    return when {
        hours == 0 -> "$rest 分钟"
        rest == 0 -> "$hours 小时"
        else -> "$hours 小时 $rest 分"
    }
}
