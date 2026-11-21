package io.github.huig7.shi.ui.annual

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.huig7.shi.domain.CategoryUsage
import io.github.huig7.shi.domain.annual.AnnualReport
import io.github.huig7.shi.ui.theme.Hairline
import io.github.huig7.shi.ui.theme.Ink
import io.github.huig7.shi.ui.theme.InkMuted
import io.github.huig7.shi.ui.theme.Paper
import io.github.huig7.shi.ui.theme.Pine
import io.github.huig7.shi.util.DateTimeFormat
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val CARD_WIDTH_PX = 1080f
private const val CARD_HEIGHT_PX = 1920f
private const val CARD_WIDTH_DP = 360f
private const val CARD_HEIGHT_DP = 640f
private const val CARD_DENSITY = 3f

/**
 * 分享图预览：把 360×640dp 的卡片按 3 倍密度渲染成 1080×1920px，
 * 用 GraphicsLayer 截取后写进缓存并通过 FileProvider 分享。
 *
 * 卡片在预览里按可用空间等比缩放，截图仍取原始尺寸，不截屏幕长图。
 */
@Composable
internal fun AnnualSharePreviewDialog(
    report: AnnualReport,
    persona: AnnualPersona,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val layer = rememberGraphicsLayer()
    var sharing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!sharing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.86f)),
        ) {
            val density = LocalDensity.current
            val previewScale = remember(maxWidth, maxHeight) {
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }
                minOf(
                    widthPx * 0.88f / CARD_WIDTH_PX,
                    heightPx * 0.74f / CARD_HEIGHT_PX,
                ).coerceIn(0.1f, 1f)
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = previewScale, scaleY = previewScale)
                        .size(
                            width = with(density) { (CARD_WIDTH_PX * previewScale).toDp() },
                            height = with(density) { (CARD_HEIGHT_PX * previewScale).toDp() },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalDensity provides Density(CARD_DENSITY)) {
                        Box(
                            modifier = Modifier
                                .requiredSize(CARD_WIDTH_DP.dp, CARD_HEIGHT_DP.dp)
                                .drawWithContent {
                                    layer.record { this@drawWithContent.drawContent() }
                                    drawLayer(layer)
                                },
                        ) {
                            AnnualShareCard(report = report, persona = persona)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !sharing) {
                        Text(text = "取消", color = Color.White)
                    }
                    Button(
                        enabled = !sharing,
                        onClick = {
                            sharing = true
                            scope.launch {
                                try {
                                    val captured = layer.toImageBitmap().asAndroidBitmap()
                                    val bitmap = if (captured.config == Bitmap.Config.HARDWARE) {
                                        captured.copy(Bitmap.Config.ARGB_8888, false)
                                            ?: error("无法转换分享图")
                                    } else {
                                        captured
                                    }
                                    shareAnnualReportImage(context, bitmap, report.year)
                                    onDismiss()
                                } catch (error: Throwable) {
                                    Toast.makeText(context, "生成分享图失败", Toast.LENGTH_SHORT).show()
                                } finally {
                                    sharing = false
                                }
                            }
                        },
                    ) {
                        Text(text = if (sharing) "生成中…" else "分享图片")
                    }
                }
            }
        }
    }
}

/** 固定纸色、松绿的分享卡片；不依赖深色主题，保证分享图风格稳定。 */
@Composable
internal fun AnnualShareCard(report: AnnualReport, persona: AnnualPersona) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "時",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = Pine,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${report.year} 年度报告",
                fontSize = 13.sp,
                color = InkMuted,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (report.isPartial) {
                    "截至 ${DateTimeFormat.monthDay(report.throughDate)}"
                } else {
                    "全年"
                },
                fontSize = 10.sp,
                color = InkMuted,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = DateTimeFormat.durationText(report.totalMillis),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 38.sp,
            lineHeight = 46.sp,
            color = Ink,
            maxLines = 2,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${report.activeDays} 天有记录 · ${report.eventCount} 条",
            fontSize = 12.sp,
            color = InkMuted,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = persona.title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            color = Pine,
            maxLines = 1,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = persona.tagline,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            color = InkMuted,
            maxLines = 2,
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Hairline),
        )
        Spacer(Modifier.height(10.dp))
        report.roots.take(3).forEach { usage ->
            ShareCategoryRow(usage = usage, totalMillis = report.totalMillis)
        }
        Spacer(Modifier.weight(1f))
        MonthBars(
            months = report.months,
            highlightMonth = report.busiestMonth,
            barHeight = 96.dp,
            barColor = Pine,
            labelColor = InkMuted,
            highlightLabelColor = Ink,
        )
    }
}

@Composable
private fun ShareCategoryRow(usage: CategoryUsage, totalMillis: Long) {
    val accent = Color(usage.colorHex)
    val percent = if (totalMillis > 0L) {
        (usage.millis.toDouble() / totalMillis.toDouble() * 100.0).roundToInt()
    } else {
        0
    }
    val fraction = if (totalMillis > 0L) {
        (usage.millis.toDouble() / totalMillis.toDouble()).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
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
                fontSize = 14.sp,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$percent%",
                fontSize = 11.sp,
                color = InkMuted,
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Hairline),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(accent),
            )
        }
    }
}
