package io.github.huig7.shi.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.CategoryUsage
import io.github.huig7.shi.domain.StatsSummary
import io.github.huig7.shi.util.DateTimeFormat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val RING_LAYER_COUNT = 3
private const val RING_GAP_ANGLE = 3.6f
private const val RING_MIN_ANGLE = 1f

/** 各层环带的内 / 外半径比例（相对于外接圆半径 R）。 */
private val RING_INNER_RATIOS = floatArrayOf(0.46f, 0.68f, 0.84f)
private val RING_OUTER_RATIOS = floatArrayOf(0.68f, 0.84f, 1.0f)

/** 弧段在分类树中的索引路径；不能只用分类 ID，因为多个未分类根可能共用占位 ID。 */
internal data class RingSegment(
    val path: List<Int>,
    val usage: CategoryUsage,
    val layer: Int,
    val startAngle: Float,
    val sweepAngle: Float,
)

@Composable
internal fun RingSection(
    summary: StatsSummary,
    segments: List<RingSegment>,
    selectedSegment: RingSegment?,
    onSegmentSelected: (List<Int>?) -> Unit,
) {
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val isEmpty = summary.totalMillis <= 0L
    val minimumTouchRadius = with(LocalDensity.current) { 24.dp.toPx() }
    val currentOnSegmentSelected by rememberUpdatedState(onSegmentSelected)
    val selectedPercent = selectedSegment?.let { segment ->
        if (summary.totalMillis <= 0L) {
            0
        } else {
            (segment.usage.millis.toDouble() / summary.totalMillis * 100.0).roundToInt()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(segments, minimumTouchRadius, isEmpty) {
                        detectTapGestures { position ->
                            if (!isEmpty) {
                                val hit = findRingSegment(
                                    position = position,
                                    canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                                    segments = segments,
                                    minimumTouchRadius = minimumTouchRadius,
                                )
                                currentOnSegmentSelected(hit?.path)
                            }
                        }
                    }
                    .semantics {
                        contentDescription = "三层分类用时环图，可选择弧段查看分类详情"
                        stateDescription = selectedSegment?.let { "已选择${it.usage.name}" } ?: "未选择分类"
                        customActions = segments.map { segment ->
                            CustomAccessibilityAction(
                                label = "选择${segment.usage.name}，第${segment.layer + 1}层",
                            ) {
                                currentOnSegmentSelected(segment.path)
                                true
                            }
                        }
                    },
            ) {
                val radius = minOf(size.width, size.height) / 2f
                if (radius <= 0f) return@Canvas
                val center = Offset(size.width / 2f, size.height / 2f)
                if (isEmpty) {
                    for (layer in 0 until RING_LAYER_COUNT) {
                        drawRingTrack(layer, center, radius, trackColor)
                    }
                } else {
                    val populatedLayers = segments.mapTo(mutableSetOf()) { it.layer }
                    for (layer in 0 until RING_LAYER_COUNT) {
                        if (layer in populatedLayers) drawRingTrack(layer, center, radius, trackColor)
                    }
                    segments.forEach { segment ->
                        val geometry = ringGeometry(segment.layer, center, radius)
                        val selected = segment.path == selectedSegment?.path
                        drawArc(
                            color = Color(segment.usage.colorHex).copy(
                                alpha = if (selectedSegment == null || selected) 1f else 0.55f,
                            ),
                            startAngle = segment.startAngle,
                            sweepAngle = segment.sweepAngle,
                            useCenter = false,
                            topLeft = geometry.topLeft,
                            size = geometry.size,
                            style = Stroke(
                                width = geometry.strokeWidth + if (selected) 2.dp.toPx() else 0f,
                            ),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.widthIn(max = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (selectedSegment != null) {
                    Text(
                        text = selectedSegment.usage.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = when {
                        isEmpty -> "—"
                        selectedSegment != null -> DateTimeFormat.durationText(selectedSegment.usage.millis)
                        else -> DateTimeFormat.durationText(summary.totalMillis)
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (selectedSegment != null) {
                        "第${selectedSegment.layer + 1}层 · ${selectedSegment.usage.eventCount} 条记录"
                    } else {
                        "共 ${summary.eventCount} 条记录"
                    },
                    fontSize = 11.sp,
                    color = mutedColor,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
                if (selectedSegment != null && selectedPercent != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "占比 $selectedPercent%",
                        fontSize = 10.sp,
                        color = mutedColor,
                        maxLines = 1,
                    )
                }
            }
        }
        if (!isEmpty) {
            Text(
                text = "内 → 外：一级 · 二级 · 三级",
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 10.sp,
                color = mutedColor,
                maxLines = 1,
            )
        }
    }
}

/** 单层环带的几何参数：绘制矩形、直径与环宽。 */
private class RingGeometry(val topLeft: Offset, val size: Size, val strokeWidth: Float)

private fun ringGeometry(layer: Int, center: Offset, radius: Float): RingGeometry {
    val innerRadius = radius * RING_INNER_RATIOS[layer]
    val outerRadius = radius * RING_OUTER_RATIOS[layer]
    val ringRadius = (innerRadius + outerRadius) / 2f
    return RingGeometry(
        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
        size = Size(ringRadius * 2f, ringRadius * 2f),
        strokeWidth = outerRadius - innerRadius,
    )
}

/** 画一整圈淡色轨道。 */
private fun DrawScope.drawRingTrack(layer: Int, center: Offset, radius: Float, color: Color) {
    val geometry = ringGeometry(layer, center, radius)
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = geometry.topLeft,
        size = geometry.size,
        style = Stroke(width = geometry.strokeWidth),
    )
}

/** 预先展开弧段，让绘制、触摸命中与无障碍操作共享同一组几何数据。 */
internal fun buildRingSegments(roots: List<CategoryUsage>): List<RingSegment> {
    val segments = mutableListOf<RingSegment>()
    appendRingSegments(
        usages = roots,
        layer = 0,
        startAngle = -90f,
        totalSweep = 360f,
        parentPath = emptyList(),
        segments = segments,
    )
    return segments
}

private fun appendRingSegments(
    usages: List<CategoryUsage>,
    layer: Int,
    startAngle: Float,
    totalSweep: Float,
    parentPath: List<Int>,
    segments: MutableList<RingSegment>,
) {
    if (layer >= RING_LAYER_COUNT || usages.isEmpty()) return
    val totalMillis = usages.sumOf { it.millis }
    if (totalMillis <= 0L) return

    var cursor = startAngle
    usages.forEachIndexed { index, usage ->
        val path = parentPath + index
        val sweep = (totalSweep.toDouble() * usage.millis.toDouble() / totalMillis.toDouble()).toFloat()
        val visibleSweep = (sweep - RING_GAP_ANGLE).coerceAtLeast(RING_MIN_ANGLE).coerceAtMost(sweep)
        if (visibleSweep > 0f) {
            segments += RingSegment(
                path = path,
                usage = usage,
                layer = layer,
                startAngle = cursor + (sweep - visibleSweep) / 2f,
                sweepAngle = visibleSweep,
            )
        }
        if (usage.children.isNotEmpty()) {
            appendRingSegments(
                usages = usage.children,
                layer = layer + 1,
                startAngle = cursor,
                totalSweep = sweep,
                parentPath = path,
                segments = segments,
            )
        }
        cursor += sweep
    }
}

/** 命中可见弧段；环带过窄时将径向触摸范围扩到至少 48dp，并取离环心线最近者。 */
private fun findRingSegment(
    position: Offset,
    canvasSize: Size,
    segments: List<RingSegment>,
    minimumTouchRadius: Float,
): RingSegment? {
    val radius = minOf(canvasSize.width, canvasSize.height) / 2f
    if (radius <= 0f) return null
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val dx = position.x - center.x
    val dy = position.y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    val angle = (atan2(dy, dx) * 180.0 / PI).toFloat()

    return segments.mapNotNull { segment ->
        val geometry = ringGeometry(segment.layer, center, radius)
        val ringRadius = geometry.size.width / 2f
        val radialDistance = abs(distance - ringRadius)
        val touchRadius = maxOf(geometry.strokeWidth / 2f, minimumTouchRadius)
        if (radialDistance > touchRadius) return@mapNotNull null
        val angleInSegment = normalizedAngle(angle - segment.startAngle)
        if (angleInSegment > segment.sweepAngle) {
            null
        } else {
            segment to radialDistance
        }
    }.minByOrNull { it.second }?.first
}

private fun normalizedAngle(angle: Float): Float {
    val remainder = angle % 360f
    return if (remainder < 0f) remainder + 360f else remainder
}
