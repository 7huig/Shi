package io.github.huig7.shi.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.plan.PlanPrompt
import io.github.huig7.shi.domain.plan.PlanPromptKind
import io.github.huig7.shi.util.DateTimeFormat

/**
 * 首页顶部的今日提示。
 *
 * 三种卡片共用同一条「分类名 / 次要信息」两行模板，文字在卡片内水平、垂直居中；
 * 每日时长的进度条作为底部叠层，不参与文字排版，因此不会推开文字。
 */
@Composable
fun PlanPromptStrip(
    prompts: List<PlanPrompt>,
    onRecord: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (prompts.isEmpty()) return
    val goals = prompts.filter { it.kind == PlanPromptKind.GOAL }
    val tasks = prompts.filter { it.kind != PlanPromptKind.GOAL }

    Column(modifier = modifier.padding(top = 2.dp, bottom = 1.dp)) {
        if (goals.isNotEmpty()) {
            PromptRow(
                prompts = goals,
                cardWidth = 144.dp,
                onRecord = onRecord,
            )
        }
        if (goals.isNotEmpty() && tasks.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
        }
        if (tasks.isNotEmpty()) {
            PromptRow(
                prompts = tasks,
                cardWidth = 112.dp,
                onRecord = onRecord,
            )
        }
    }
}

@Composable
private fun PromptRow(
    prompts: List<PlanPrompt>,
    cardWidth: Dp,
    onRecord: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // 卡片少时居中，超出屏幕宽度时从左开始横向滚动。
        val gap = 6.dp
        val total = cardWidth * prompts.size + gap * (prompts.size - 1)
        val startPadding = ((maxWidth - total) / 2).coerceAtLeast(12.dp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(gap),
            contentPadding = PaddingValues(start = startPadding, end = 12.dp),
        ) {
            items(items = prompts, key = { it.key }) { prompt ->
                PlanPromptCard(
                    prompt = prompt,
                    width = cardWidth,
                    onRecord = { onRecord(prompt.categoryId) },
                )
            }
        }
    }
}

@Composable
private fun PlanPromptCard(
    prompt: PlanPrompt,
    width: Dp,
    onRecord: () -> Unit,
) {
    val accent = Color(prompt.colorHex)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onRecord),
        shape = RoundedCornerShape(11.dp),
        color = accent.copy(alpha = if (dark) 0.16f else 0.11f),
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = if (dark) 0.30f else 0.18f),
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = prompt.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = detailText(prompt),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (prompt.due) {
                        accent
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
            if (prompt.kind == PlanPromptKind.GOAL) {
                GoalProgress(
                    prompt = prompt,
                    accent = accent,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun GoalProgress(prompt: PlanPrompt, accent: Color, modifier: Modifier = Modifier) {
    val ratio = if (prompt.goalTargetMillis <= 0L) {
        0f
    } else {
        (prompt.goalDoneMillis.toFloat() / prompt.goalTargetMillis.toFloat()).coerceIn(0f, 1f)
    }
    Box(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent.copy(alpha = 0.75f)),
        )
    }
}

private fun detailText(prompt: PlanPrompt): String = when (prompt.kind) {
    PlanPromptKind.GOAL ->
        shortDuration(prompt.goalDoneMillis) + " / " + shortDuration(prompt.goalTargetMillis)

    PlanPromptKind.FIXED_TIME -> {
        val minute = prompt.fixedTimeMinuteOfDay ?: 0
        "%02d:%02d".format(minute / 60, minute % 60)
    }

    PlanPromptKind.INTERVAL -> {
        val now = prompt.nowMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        if (prompt.due) {
            "该做了"
        } else {
            prompt.nextReminderAt?.let {
                "下次 " + DateTimeFormat.relativeDayTime(it, now)
            } ?: "该做了"
        }
    }
}

private fun shortDuration(millis: Long): String =
    if (millis <= 0L) "0 分钟" else DateTimeFormat.durationText(millis)
