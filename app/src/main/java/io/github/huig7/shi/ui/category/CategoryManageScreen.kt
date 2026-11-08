package io.github.huig7.shi.ui.category

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.domain.model.SolarTermSeasons
import io.github.huig7.shi.ui.home.CategoryDot
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryManageScreen(
    state: CategoryManageUiState,
    onBack: () -> Unit,
    onAddRoot: () -> Unit,
    onAddChild: (CategoryNode) -> Unit,
    onEdit: (CategoryNode) -> Unit,
    onDelete: (CategoryNode) -> Unit,
    onEditorNameChange: (String) -> Unit,
    onEditorColorChange: (Long) -> Unit,
    onEditorDismiss: () -> Unit,
    onEditorSave: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onMove: (Long, Int) -> Unit,
    onToggleExpanded: (Long) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    // 展开状态决定可见行：默认全部收起时只显示根分类。
    val baseRows = remember(state.tree, state.expandedIds) {
        state.tree.visibleRows(state.expandedIds).map { (node, depth) -> CategoryRowData(node, depth) }
    }

    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOrder by remember { mutableStateOf<List<Long>?>(null) }
    var dragSlot by remember { mutableIntStateOf(0) }
    // 拖拽浮层的原始指针位置（窗口坐标）。只在布局阶段读取，避免每帧重组。
    var pointerWindowY by remember { mutableFloatStateOf(Float.NaN) }
    // 按下点与行中心的偏差、行高，用来让浮层「跟着手指拿起来」。
    var dragGrabOffset by remember { mutableFloatStateOf(0f) }
    var dragRowHeight by remember { mutableFloatStateOf(0f) }
    var contentTopWindow by remember { mutableFloatStateOf(0f) }
    val rowBounds = remember { mutableMapOf<Long, RowBounds>() }
    // LazyColumn 的窗口顶端：LazyListItemInfo 的 offset 是相对列表视口的。
    val listWindowTop = remember { floatArrayOf(0f) }

    val rows = remember(baseRows, dragOrder) {
        dragOrder?.let { reorderSiblingBlocks(baseRows, it) } ?: baseRows
    }
    val rowIndexById = remember(rows) {
        rows.withIndex().associate { (index, row) -> row.node.category.id to index }
    }
    val currentRows by rememberUpdatedState(rows)
    val currentRowIndexById by rememberUpdatedState(rowIndexById)
    val currentDraggingId by rememberUpdatedState(draggingId)

    // 数据源更新后丢弃本地预览顺序：此时数据库顺序已经和拖动结果一致，不再需要本地覆盖。
    LaunchedEffect(baseRows) {
        if (draggingId == null) dragOrder = null
    }

    // 拖动主循环：每帧只读布局信息；只有落点 slot 变化时才更新预览顺序与触发触觉。
    LaunchedEffect(draggingId) {
        if (draggingId == null) return@LaunchedEffect
        val hysteresisPx = with(density) { 6.dp.toPx() }
        val edgePx = with(density) { 72.dp.toPx() }
        val maxStepPx = with(density) { 16.dp.toPx() }
        while (true) {
            withFrameNanos { }
            val dragId = currentDraggingId ?: break
            val windowPointerY = pointerWindowY
            if (!windowPointerY.isFinite()) continue
            val layout = listState.layoutInfo
            val visible = layout.visibleItemsInfo
            if (visible.isEmpty()) continue
            val allRows = currentRows
            val dragged = allRows.firstOrNull { it.node.category.id == dragId } ?: break
            val parentId = dragged.node.category.parentId
            val siblings = allRows.filter {
                it.node.category.parentId == parentId && it.node.category.id != dragId
            }
            val siblingIds = siblings.map { it.node.category.id }
            val firstVisibleIndex = visible.first().index
            val lastVisibleIndex = visible.last().index
            val visibleByIndex = visible.associateBy { it.index }
            // 兄弟中心用布局目标位置（第 0 项是提示文案，所以行下标 + 1）；
            // 视口之外的兄弟用正负 Float.MAX_VALUE 占位，保证边界处不跳。
            val centers = siblings.map { row ->
                val lazyIndex = (currentRowIndexById[row.node.category.id] ?: -1) + 1
                val item = visibleByIndex[lazyIndex]
                when {
                    item != null -> item.offset + item.size / 2f
                    lazyIndex < firstVisibleIndex -> -Float.MAX_VALUE
                    else -> Float.MAX_VALUE
                }
            }
            val pointerInList = windowPointerY - listWindowTop[0]
            val newSlot = stableDropSlot(pointerInList, centers, dragSlot, hysteresisPx)
            if (newSlot != dragSlot) {
                dragSlot = newSlot
                dragOrder = orderForSlot(siblingIds, dragId, newSlot)
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            // 边缘自动滚动：越靠边越快，指针离开列表视口就冻结。
            val delta = edgeScrollDelta(
                pointerInViewport = pointerInList,
                viewportHeight = layout.viewportSize.height.toFloat(),
                edgePx = edgePx,
                maxStepPx = maxStepPx,
            )
            if (delta != 0f) listState.scrollBy(delta)
        }
    }

    fun commitDrag() {
        val id = draggingId ?: return
        val slot = dragSlot
        draggingId = null
        // 先保留 dragOrder 作为预览，等数据库返回新顺序后再丢弃，避免松手瞬间回跳。
        onMove(id, slot)
    }

    fun cancelDrag() {
        draggingId = null
        dragOrder = null
    }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("分类") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    // 全部展开 / 全部收起：全展开时提供收起入口，否则提供展开入口。
                    TextButton(onClick = if (state.allExpanded) onCollapseAll else onExpandAll) {
                        Text(
                            text = if (state.allExpanded) "全部收起" else "全部展开",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    IconButton(onClick = onAddRoot) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "新建分类",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { contentTopWindow = it.boundsInWindow().top }
                .clipToBounds(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .onGloballyPositioned { listWindowTop[0] = it.boundsInWindow().top },
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item(key = "hint") {
                    Text(
                        text = "点箭头展开或收起；按住右侧手柄上下拖动，可调整同级顺序。" +
                            "最多 3 层，删除分类时会一并删除其中的事件。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
                    )
                }
                items(
                    items = rows,
                    key = { it.node.category.id },
                ) { row ->
                    val rowId = row.node.category.id
                    val dragging = draggingId == rowId
                    CategoryRow(
                        node = row.node,
                        depth = row.depth,
                        expanded = rowId in state.expandedIds,
                        dragging = dragging,
                        modifier = if (dragging) {
                            // 占位行直接落到目标位置，避免它自己再播放位移动画导致视觉抖动。
                            Modifier.animateItem(placementSpec = null)
                        } else {
                            Modifier.animateItem()
                        },
                        onAddChild = onAddChild,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onToggleExpanded = onToggleExpanded,
                        onBoundsChanged = { centerY, height ->
                            rowBounds[rowId] = RowBounds(centerY, height)
                        },
                        onDragStart = { startPointerY ->
                            val bounds = rowBounds[rowId]
                            dragGrabOffset = startPointerY - (bounds?.centerY ?: startPointerY)
                            dragRowHeight = bounds?.height
                                ?: with(density) { 56.dp.toPx() }
                            pointerWindowY = startPointerY
                            dragOrder = null
                            dragSlot = baseRows
                                .filter { it.node.category.parentId == row.node.category.parentId }
                                .indexOfFirst { it.node.category.id == rowId }
                                .coerceAtLeast(0)
                            draggingId = rowId
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDrag = { deltaY -> pointerWindowY += deltaY },
                        onDragEnd = { commitDrag() },
                        onDragCancel = { cancelDrag() },
                    )
                }
            }

            // 浮层放在列表之后：画在行上面，但自身不消费点击；位置在布局阶段跟手更新。
            val dragRow = rows.firstOrNull { it.node.category.id == draggingId }
            if (dragRow != null) {
                val overlayHeightPx = dragRowHeight
                DragOverlayRow(
                    node = dragRow.node,
                    depth = dragRow.depth,
                    heightPx = overlayHeightPx,
                    modifier = Modifier.offset {
                        val top = pointerWindowY - dragGrabOffset - overlayHeightPx / 2f - contentTopWindow
                        if (top.isFinite()) IntOffset(0, top.roundToInt()) else IntOffset.Zero
                    },
                )
            }
        }
    }

    state.editor?.let { editor ->
        AlertDialog(
            onDismissRequest = onEditorDismiss,
            title = {
                Text(if (editor.id == null) "新建分类" else "编辑分类")
            },
            text = {
                Column {
                    if (editor.parentName != null) {
                        Text(
                            text = "父分类：${editor.parentName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = editor.name,
                        onValueChange = onEditorNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("分类名称") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "颜色",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SolarTermPalette(
                        selectedHex = editor.colorHex,
                        onSelect = onEditorColorChange,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onEditorSave,
                    enabled = editor.name.isNotBlank(),
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = onEditorDismiss) {
                    Text("取消")
                }
            },
        )
    }

    state.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("删除分类？") },
            text = {
                Text(
                    buildString {
                        append("「${pending.node.category.name}」及其子分类会被删除。")
                        if (pending.eventCount > 0) {
                            append("其中 ${pending.eventCount} 条事件会一并删除。")
                        }
                        append("此操作无法撤销。")
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDeleteConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text("取消")
                }
            },
        )
    }
}

/** 行在窗口中的位置：中心 y 与高度，用于拖拽浮层跟手。 */
private data class RowBounds(val centerY: Float, val height: Float)

/** 拖动预览：兄弟行连同其可见子树算一块，按 order 重排这些块，其余行保持原位。 */
private fun orderForSlot(siblingIds: List<Long>, draggedId: Long, slot: Int): List<Long> {
    val ids = siblingIds.toMutableList()
    ids.remove(draggedId)
    ids.add(slot.coerceIn(0, ids.size), draggedId)
    return ids
}

@Composable
private fun CategoryRow(
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
            if (node.category.isSystem) {
                Text(
                    text = "系统",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
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
            if (!node.category.isSystem) {
                IconButton(onClick = { onDelete(node) }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "删除分类",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
private fun DragOverlayRow(
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
            if (node.category.isSystem) {
                Text(
                    text = "系统",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            Box(
                modifier = Modifier.size(width = 44.dp, height = 56.dp),
                contentAlignment = Alignment.Center,
            ) {
                DragHandleBars(color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private data class CategoryRowData(val node: CategoryNode, val depth: Int)

/**
 * 拖动中的实时预览：同一父分类下的每个兄弟行连同它的可见子树算作一块，
 * 按 order（兄弟 id 的新顺序）重排这些块，其余行保持原位。
 */
private fun reorderSiblingBlocks(
    rows: List<CategoryRowData>,
    order: List<Long>,
): List<CategoryRowData> {
    if (rows.isEmpty() || order.isEmpty()) return rows
    val orderIndex = order.withIndex().associate { (index, id) -> id to index }
    val siblingStarts = rows.indices.filter { orderIndex.containsKey(rows[it].node.category.id) }
    if (siblingStarts.isEmpty()) return rows
    val blocks = siblingStarts.map { start ->
        val depth = rows[start].depth
        var end = start + 1
        while (end < rows.size && rows[end].depth > depth) end++
        rows.subList(start, end).toList()
    }
    val prefix = rows.subList(0, siblingStarts.first()).toList()
    val suffixStart = siblingStarts.last() + blocks.last().size
    val suffix = if (suffixStart < rows.size) rows.subList(suffixStart, rows.size).toList() else emptyList()
    val ordered = blocks.sortedBy { block ->
        orderIndex[block.first().node.category.id] ?: Int.MAX_VALUE
    }
    return prefix + ordered.flatten() + suffix
}

/** 24 节气色板：4 行（春夏秋冬）× 6 列，颜色下方标注节气名。 */
@Composable
private fun SolarTermPalette(selectedHex: Long, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SolarTermSeasons.forEach { (season, terms) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = season,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(20.dp)
                        .padding(top = 7.dp),
                )
                terms.forEach { term ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ColorSwatch(
                            colorHex = term.colorHex,
                            selected = term.colorHex == selectedHex,
                            onClick = { onSelect(term.colorHex) },
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = term.name,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            maxLines = 1,
                            color = if (term.colorHex == selectedHex) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(colorHex: Long, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(colorHex), CircleShape)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                    shape = CircleShape,
                ),
        )
    }
}
