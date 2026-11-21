package io.github.huig7.shi.ui.annual

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import io.github.huig7.shi.domain.annual.AnnualReport
import io.github.huig7.shi.util.DateTimeFormat
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 年度报告的文本版：只引用报告里已有的数字，不写主观评价。
 *
 * 纯函数，方便单测；分享动作本身在 [shareAnnualReportText] 里。
 */
internal fun buildAnnualReportText(
    report: AnnualReport,
    persona: AnnualPersona = annualPersona(report),
): String {
    val lines = mutableListOf<String>()
    lines += if (report.isPartial) {
        "時 · ${report.year} 年度报告（截至 ${DateTimeFormat.monthDay(report.throughDate)}）"
    } else {
        "時 · ${report.year} 年度报告"
    }
    lines += "记录 ${DateTimeFormat.durationText(report.totalMillis)} · ${report.activeDays} 天"
    lines += "年度称号：${persona.title}"
    lines += "年度关键词：${annualKeyword(report)}"
    lines += annualTimeEquivalent(report)
    if (report.roots.isNotEmpty()) {
        lines += ""
        report.roots.take(3).forEach { usage ->
            val percent = if (report.totalMillis > 0L) {
                (usage.millis.toDouble() / report.totalMillis.toDouble() * 100.0).roundToInt()
            } else {
                0
            }
            lines += "· ${usage.name} $percent%"
        }
    }
    report.busiestMonth?.let { lines += "最忙的是 $it 月" }
    if (report.longestStreakDays >= 2) {
        lines += "连续 ${report.longestStreakDays} 天有记录"
    }
    report.totalDeltaRatio?.let { ratio ->
        lines += annualDeltaText(ratio)
    }
    return lines.joinToString("\n")
}

/**
 * 同比文案。
 *
 * 增长超过 10 倍时改用「是去年的 N 倍」，避免去年同期数据很小时出现四位数百分比。
 */
internal fun annualDeltaText(ratio: Float): String = when {
    ratio >= 10f -> "是去年的 ${(1f + ratio).roundToInt()} 倍"
    ratio >= 0.05f -> "比去年同期多 ${(ratio * 100f).roundToInt()}%"
    ratio <= -0.05f -> "比去年同期少 ${(-ratio * 100f).roundToInt()}%"
    else -> "和去年差不多"
}

/** 用系统分享面板发一段文本。零权限、无新依赖。 */
internal fun shareAnnualReportText(context: Context, report: AnnualReport) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "時 · ${report.year} 年度报告")
        putExtra(Intent.EXTRA_TEXT, buildAnnualReportText(report))
    }
    context.startActivity(Intent.createChooser(intent, "分享年度报告"))
}

/**
 * 把 1080×1920 的摘要图写入缓存并交给系统分享。
 *
 * 通过 FileProvider 暴露单个文件，不申请存储权限。
 */
internal suspend fun shareAnnualReportImage(
    context: Context,
    bitmap: Bitmap,
    year: Int,
) {
    val uri = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "shi-annual-$year.png")
        FileOutputStream(file).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "图片写入失败" }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "時 · $year 年度报告")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享年度报告"))
}
