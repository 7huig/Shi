package io.github.huig7.shi.ui.category

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.ui.home.CategoryDot

/** 行在窗口中的位置：中心 y 与高度，用于拖拽浮层跟手。 */
internal data class RowBounds(val centerY: Float, val height: Float)

@Composable
internal fun CategoryRow(
    node: CategoryNode,
    depth: Int,
    expanded: Boolean,
    dragging: Boolean,
    modifier: Modifier = Modifier,
    onAddChild: (CategoryNode) -> Unit,
    onEdit: (CategoryNode) -> Unit,
    onDelete: (CategoryNode) -> Unit,
    onToggleExpanded: (Long) -> Unit,
    onBoundsChanged: (centerY: Float, height: Float) -> Unit,
    onDragStart: (Float) -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var handleTopWindow by remember { mutableFloatStateOf(0f) }
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val hasChildren = node.children.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                onBoundsChanged(bounds.center.y, bounds.height)
            }
            // 拖动时原行变成占位：保留一点底色，内容隐藏（浮层代替它跟手）。
            .background(
                if (dragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else Color.Transparent,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (dragging) 0f else 1f)
                .clickable { onEdit(node) }
                .padding(start = (20 + depth * 18).dp, end = 4.dp)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(colorHex = node.category.colorHex, size = 10.dp)
            Spacer(modifier = Modifier.width(10.dp))
            // 名字 + 子级数量 + 箭头放进同一段弹性区域：短名字时箭头紧跟名字，
            // 右侧的系统标记/增删/手柄仍然贴住行尾。
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = node.category.name,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (hasChildren) {
                    // 收起时提示直接子级数量，帮助判断箭头是否值得点。
                    if (!expanded) {
                        Text(
                            text = "· " + node.children.size,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                // 读屏只报「N 个子分类」，不念出修饰用的圆点与数字。
                                .clearAndSetSemantics {
                                    contentDescription = node.children.size.toString() + " 个子分类"
                                },
                        )
                    }
                    ExpandArrow(
                        expanded = expanded,
                        name = node.category.name,
                        onClick = { onToggleExpanded(node.category.id) },
                    )
                }
            }
            if (depth < 2) {
                IconButton(onClick = { onAddChild(node) }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "添加子分类",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = { onDelete(node) }) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "删除分类",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DragHandle(
                modifier = Modifier
                    .onGloballyPositioned { handleTopWindow = it.boundsInWindow().top }
                    .pointerInput(node.category.id) {
                        detectDragGestures(
                            onDragStart = { offset -> currentOnDragStart(handleTopWindow + offset.y) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentOnDrag(dragAmount.y)
                            },
                            onDragEnd = { currentOnDragEnd() },
                            onDragCancel = { currentOnDragCancel() },
                        )
                    },
            )
        }
    }
}

/** 展开/收起箭头：展开指向下，收起旋转 -90 度指向右。 */
@Composable
private fun ExpandArrow(expanded: Boolean, name: String, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "categoryArrowRotation",
    )
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = if (expanded) "收起" + name else "展开" + name,
            modifier = Modifier
                .size(22.dp)
                .rotate(rotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 右侧三横线拖拽手柄；触摸区域 44×56dp，视觉 16×14dp。 */
@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    val handleColor = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 56.dp)
            .semantics { contentDescription = "拖动排序" },
        contentAlignment = Alignment.Center,
    ) {
        DragHandleBars(color = handleColor)
    }
}

/** 拖拽浮层里的静态手柄，只负责视觉，不接收手势。 */
@Composable
private fun DragHandleBars(color: Color) {
    Canvas(modifier = Modifier.size(width = 16.dp, height = 14.dp)) {
        val stroke = 1.6.dp.toPx()
        val step = size.height / 3f
        for (index in 0..2) {
            val y = step * index + step / 2f
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}

/**
 * 拖拽时跟随手指的浮层：内容就是这一行（圆点 + 名称 + 系统标记 + 手柄）。
 * 用 Surface 做出「拿起来」的阴影层次；不接收点击，也不拦截列表手势。
 */
@Composable
internal fun DragOverlayRow(
    node: CategoryNode,
    depth: Int,
    heightPx: Float,
    modifier: Modifier = Modifier,
) {
    val heightDp = with(LocalDensity.current) { heightPx.toDp() }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = (20 + depth * 18).dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(colorHex = node.category.colorHex, size = 10.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = node.category.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier.size(width = 44.dp, height = 56.dp),
                contentAlignment = Alignment.Center,
            ) {
                DragHandleBars(color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
