package io.github.huig7.shi.ui.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.domain.model.CategoryNode
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
    onDeleteMoveEvents: () -> Unit,
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
                            "最多 3 层；删除分类时可以选择一并删除事件，或保留事件并移到父级。",
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
        CategoryEditorDialog(
            editor = editor,
            onNameChange = onEditorNameChange,
            onColorChange = onEditorColorChange,
            onDismiss = onEditorDismiss,
            onSave = onEditorSave,
        )
    }

    state.pendingDelete?.let { pending ->
        CategoryDeleteDialog(
            pending = pending,
            onDismiss = onDeleteDismiss,
            onConfirm = onDeleteConfirm,
            onMoveEvents = onDeleteMoveEvents,
        )
    }
}
