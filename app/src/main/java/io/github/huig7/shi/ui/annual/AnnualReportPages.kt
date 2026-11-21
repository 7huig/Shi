package io.github.huig7.shi.ui.annual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.annual.AnnualReport
import io.github.huig7.shi.util.DateTimeFormat
import kotlin.math.roundToInt

/** 年度报告的章节顺序；pager 的页数直接取这个枚举。 */
internal enum class AnnualReportPage {
    COVER,
    TOTAL,
    PERSONA,
    CATEGORIES,
    MONTHS,
    HABITS,
    SUPERLATIVES,
    SEASONS,
    COMPARISON,
    SHARE,
}

/**
 * 一页的通用骨架。
 *
 * [centered] 用于开场 / 称号 / 结尾这类短页；数据页可纵向滚动，避免大字体下溢出。
 */
@Composable
internal fun AnnualPage(
    centered: Boolean = false,
    watermark: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        if (watermark != null) {
            Text(
                text = watermark,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 2.dp),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 92.sp,
                lineHeight = 92.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (centered) Modifier else Modifier.verticalScroll(scrollState))
                .padding(horizontal = 28.dp, vertical = 18.dp),
            verticalArrangement = if (centered) Arrangement.Center else Arrangement.Top,
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
            content = content,
        )
    }
}

@Composable
internal fun AnnualCoverPage(report: AnnualReport) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(centered = true) {
        Text(
            text = "時 · 年度报告",
            fontSize = 11.sp,
            letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = "${report.year}",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 58.sp,
            lineHeight = 66.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "你把 ${DateTimeFormat.durationText(report.totalMillis)}，留在了这一年。",
            fontSize = 18.sp,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "${report.activeDays} 天有记录 · ${report.eventCount} 条",
            fontSize = 12.sp,
            color = muted,
        )
        if (report.isPartial) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "进行中 · 截至 ${DateTimeFormat.monthDay(report.throughDate)}",
                fontSize = 11.sp,
                color = muted,
            )
        }
    }
}

@Composable
internal fun AnnualTotalPage(report: AnnualReport) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(watermark = "01") {
        ChapterLabel("01 · 时间总量")
        Spacer(Modifier.height(18.dp))
        Text(
            text = DateTimeFormat.durationText(report.totalMillis),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = annualTimeEquivalent(report),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(26.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            StatCell(
                label = "记录天数",
                value = "${report.activeDays} 天",
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = "记录条数",
                value = "${report.eventCount} 条",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            StatCell(
                label = "平均每个记录日",
                value = DateTimeFormat.durationText(report.avgPerActiveDayMillis),
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = "解锁分类",
                value = "${report.categoryCount} 个",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "这一年，你有 ${report.activeDays} 天留下了记录。",
            fontSize = 12.sp,
            color = muted,
        )
    }
}

@Composable
internal fun AnnualPersonaPage(report: AnnualReport, persona: AnnualPersona) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(centered = true, watermark = "02") {
        ChapterLabel("02 · 年度称号")
        Spacer(Modifier.height(24.dp))
        Text(
            text = persona.title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 42.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = persona.tagline,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            color = muted,
        )
        Spacer(Modifier.height(30.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            StatCell(
                label = "最常开始",
                value = report.busiestHour?.let { "$it 点" } ?: "—",
            )
            StatCell(
                label = "解锁分类",
                value = "${report.categoryCount} 个",
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            StatCell(
                label = "深夜记录",
                value = "${report.nightOwlCount} 次",
            )
            StatCell(
                label = "清晨记录",
                value = "${report.earlyBirdCount} 次",
            )
        }
    }
}

@Composable
internal fun AnnualCategoriesPage(report: AnnualReport) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(watermark = "03") {
        ChapterLabel("03 · 时间去向")
        Spacer(Modifier.height(20.dp))
        report.roots.take(5).forEach { usage ->
            CategoryBar(usage = usage, totalMillis = report.totalMillis)
        }
        Spacer(Modifier.height(12.dp))
        report.roots.firstOrNull()?.let { top ->
            val percent = (top.millis.toDouble() / report.totalMillis.toDouble() * 100.0).roundToInt()
            Text(
                text = "${top.name}排第一，占了 $percent%。",
                fontSize = 12.sp,
                color = muted,
            )
        }
        if (report.roots.size > 5) {
            Text(
                text = "还有 ${report.roots.size - 5} 个分类没挤进前五。",
                fontSize = 12.sp,
                color = muted,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "你一共在 ${report.categoryCount} 个分类里留下过记录。",
            fontSize = 12.sp,
            color = muted,
        )
    }
}

@Composable
internal fun AnnualMonthsPage(report: AnnualReport) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AnnualPage(watermark = "04") {
        ChapterLabel("04 · 一年节奏")
        Spacer(Modifier.height(16.dp))
        PageLead("${report.activeMonthCount} 个月留下了记录。")
        report.busiestMonth?.let { month ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "最忙的是 $month 月，记录了 ${DateTimeFormat.durationText(report.months[month - 1])}。",
                fontSize = 13.sp,
                color = muted,
            )
        }
        Spacer(Modifier.height(22.dp))
        MonthBars(
            months = report.months,
            highlightMonth = report.busiestMonth,
        )
        val quietest = report.months.withIndex()
            .filter { it.value > 0L }
            .minByOrNull { it.value }
        if (quietest != null && quietest.index + 1 != report.busiestMonth) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "最安静的是 ${quietest.index + 1} 月，只有 " +
                    "${DateTimeFormat.durationText(quietest.value)}。",
                fontSize = 12.sp,
                color = muted,
            )
        }
    }
}
