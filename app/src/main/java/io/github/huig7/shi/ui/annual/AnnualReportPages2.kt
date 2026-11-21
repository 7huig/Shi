package io.github.huig7.shi.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.annual.AnnualReport
import io.github.huig7.shi.domain.annual.CategoryChange
import io.github.huig7.shi.ui.theme.FallGreen
import io.github.huig7.shi.ui.theme.FallGreenDark
import io.github.huig7.shi.ui.theme.RiseRed
import io.github.huig7.shi.ui.theme.RiseRedDark
import io.github.huig7.shi.util.DateTimeFormat
import kotlin.math.roundToInt

private val SEASON_COLORS = longArrayOf(0xFF66BB6A, 0xFFF4512C, 0xFFE8A317, 0xFF2E7D6E)

@Composable
internal fun AnnualHabitsPage(report: AnnualReport) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(watermark = "05") {
        ChapterLabel("05 · 记录习惯")
        report.busiestHour?.let { hour ->
            Spacer(Modifier.height(16.dp))
            PageLead("你的手，最勤快的时候是 $hour 点。")
        }
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            StatCell(
                label = "最早开始",
                value = report.earliestStart?.let(DateTimeFormat::clock) ?: "—",
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = "最晚结束",
                value = report.latestEnd?.let(DateTimeFormat::clock) ?: "—",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            StatCell(
                label = "最长一次",
                value = report.longestEvent?.durationMillis?.let(DateTimeFormat::durationText) ?: "—",
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = "最长连续",
                value = if (report.longestStreakDays >= 2) "${report.longestStreakDays} 天" else "—",
                modifier = Modifier.weight(1f),
            )
        }
        report.longestEvent?.let { event ->
            Spacer(Modifier.height(14.dp))
            Text(
                text = "${event.categoryPath} · ${DateTimeFormat.datePart(event.startAt)}",
                fontSize = 12.sp,
                color = muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "你记录了 ${report.eventCount} 次，其中 ${report.earlyBirdCount} 次在清晨、" +
                "${report.nightOwlCount} 次在深夜。",
            fontSize = 12.sp,
            lineHeight = 20.sp,
            color = muted,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "平均每次记录 ${DateTimeFormat.durationText(report.totalMillis / report.eventCount)}。",
            fontSize = 12.sp,
            color = muted,
        )
    }
}

@Composable
internal fun AnnualSuperlativesPage(report: AnnualReport) {
    AnnualPage(watermark = "06") {
        ChapterLabel("06 · 一些「最」")
        Spacer(Modifier.height(20.dp))
        report.firstEvent?.let { event ->
            SuperlativeRow(
                label = "第一次记录",
                value = "${DateTimeFormat.datePart(event.startAt)} ${DateTimeFormat.time(event.startAt)}",
                detail = event.categoryPath,
            )
        }
        report.lastEvent?.let { event ->
            SuperlativeRow(
                label = "最后一次记录",
                value = "${DateTimeFormat.datePart(event.startAt)} ${DateTimeFormat.time(event.startAt)}",
                detail = event.categoryPath,
            )
        }
        report.busiestDay?.let { day ->
            SuperlativeRow(
                label = "最忙的一天",
                value = "${DateTimeFormat.monthDay(day.date)} · " +
                    DateTimeFormat.durationText(day.millis),
                detail = "${day.eventCount} 条记录",
            )
        }
        report.longestEvent?.let { event ->
            SuperlativeRow(
                label = "最长的一次",
                value = DateTimeFormat.durationText(event.durationMillis),
                detail = event.categoryPath,
            )
        }
        report.latestStart?.let { time ->
            SuperlativeRow(
                label = "最晚开始",
                value = DateTimeFormat.clock(time),
            )
        }
    }
}

@Composable
private fun SuperlativeRow(label: String, value: String, detail: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        detail?.let {
            Spacer(Modifier.height(2.dp))
            Text(
                text = it,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun AnnualSeasonsPage(report: AnnualReport) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val total = report.seasons.sumOf { it.millis }
    val top = report.seasons.maxByOrNull { it.millis }
    AnnualPage(watermark = "07") {
        ChapterLabel("07 · 四季")
        Spacer(Modifier.height(16.dp))
        PageLead(
            if (top != null && total > 0L) {
                "${top.name}天记录最多。"
            } else {
                "四季还没有留下记录。"
            },
        )
        if (top != null && total > 0L) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "占了 ${(top.millis.toDouble() / total.toDouble() * 100.0).roundToInt()}%。",
                fontSize = 13.sp,
                color = muted,
            )
        }
        Spacer(Modifier.height(20.dp))
        report.seasons.forEachIndexed { index, season ->
            SeasonBar(
                name = "${season.name}天",
                millis = season.millis,
                totalMillis = total,
                accent = Color(SEASON_COLORS.getOrElse(index) { SEASON_COLORS.last() }),
            )
        }
    }
}

@Composable
internal fun AnnualComparisonPage(report: AnnualReport) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(centered = true, watermark = "08") {
        ChapterLabel("08 · 与去年相比")
        Spacer(Modifier.height(24.dp))
        val ratio = report.totalDeltaRatio
        if (ratio == null) {
            Text(
                text = "这是第一份年度报告。",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val rise = if (isDark) RiseRedDark else RiseRed
            val fall = if (isDark) FallGreenDark else FallGreen
            val color = when {
                ratio >= 0.05f -> rise
                ratio <= -0.05f -> fall
                else -> muted
            }
            Text(
                text = annualDeltaText(ratio),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                color = color,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "去年同期 ${DateTimeFormat.durationText(report.previousTotalMillis)}",
                fontSize = 12.sp,
                color = muted,
            )
            report.biggestChange?.let { change ->
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "变化最大的是 ${change.name}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = changeSummary(change),
                    fontSize = 12.sp,
                    color = muted,
                )
            }
        }
    }
}

@Composable
internal fun AnnualSharePage(
    report: AnnualReport,
    persona: AnnualPersona,
    onShareText: () -> Unit,
    onShareImage: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(centered = true, watermark = "09") {
        ChapterLabel("09 · 最后")
        Spacer(Modifier.height(22.dp))
        Text(
            text = "${report.year} 的你，是${persona.title}。",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "年度关键词：${annualKeyword(report)}",
            fontSize = 13.sp,
            color = muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(30.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onShareText, modifier = Modifier.weight(1f)) {
                Text(text = "分享文本", fontSize = 13.sp)
            }
            Button(onClick = onShareImage, modifier = Modifier.weight(1f)) {
                Text(text = "保存图片", fontSize = 13.sp)
            }
        }
    }
}

private fun changeSummary(change: CategoryChange): String = when {
    change.previousMillis == 0L -> "今年新增 ${DateTimeFormat.durationText(change.currentMillis)}"
    change.currentMillis == 0L -> "今年没有记录"
    else -> "${DateTimeFormat.durationText(change.currentMillis)} · 去年 " +
        DateTimeFormat.durationText(change.previousMillis)
}
