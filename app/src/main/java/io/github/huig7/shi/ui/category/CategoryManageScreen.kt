package io.github.huig7.shi.ui.category

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.domain.model.SolarTermSeasons
import io.github.huig7.shi.ui.home.CategoryDot

@OptIn(ExperimentalMaterial3Api::class)
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
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    val baseRows = remember(state.tree) {
        state.tree.flatten().map { (node, depth) -> CategoryRowData(node = node, depth = depth) }
    }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOrder by remember { mutableStateOf<List<Long>?>(null) }
    var dragSlot by remember { mutableIntStateOf(0) }
    var pointerWindowY by remember { mutableFloatStateOf(Float.NaN) }
    val rowCenters = remember { mutableMapOf<Long, Float>() }
    val listWindowTop = remember { floatArrayOf(0f) }
    val rows = remember(baseRows, dragOrder) {
        dragOrder?.let { reorderSiblingBlocks(baseRows, it) } ?: baseRows
    }
    val currentRows by rememberUpdatedState(rows)
    val currentDraggingId by rememberUpdatedState(draggingId)

    // 拖动过程中每帧重算落点并做边缘自动滚动；松开后只提交一次数据库更新。
    LaunchedEffect(draggingId) {
        if (draggingId == null) return@LaunchedEffect
        val edge = with(density) { 72.dp.toPx() }
        val maxStep = with(density) { 14.dp.toPx() }
        while (true) {
            withFrameNanos { }
            val layout = listState.layoutInfo
            val visible = layout.visibleItemsInfo
            if (visible.isEmpty()) continue
            val id = currentDraggingId ?: continue
            val firstVisible = visible.first().index
            val lastVisible = visible.last().index
            val dragged = currentRows.firstOrNull { it.node.category.id == id } ?: continue
            val parentId = dragged.node.category.parentId
            val others = currentRows.filter {
                it.node.category.parentId == parentId && it.node.category.id != id
            }
            var above = 0
            others.forEach { row ->
                val siblingId = row.node.category.id
                val lazyIndex = currentRows.indexOfFirst { it.node.category.id == siblingId } + 1
                val isAbove = when {
                    lazyIndex < firstVisible -> true
                    lazyIndex > lastVisible -> false
                    else -> {
                        val center = rowCenters[siblingId]
                        center != null && pointerWindowY > center
                    }
                }
                if (isAbove) above++
            }
            dragSlot = above.coerceIn(0, others.size)
            val order = others.map { it.node.category.id }.toMutableList()
            order.add(dragSlot.coerceIn(0, order.size), id)
            if (dragOrder != order) dragOrder = order

            val pointerInList = pointerWindowY - listWindowTop[0]
            val viewportHeight = layout.viewportSize.height.toFloat()
            val delta = when {
                pointerInList < edge -> -((edge - pointerInList) / edge).coerceIn(0f, 1f) * maxStep
                viewportHeight - pointerInList < edge ->
                    ((edge - (viewportHeight - pointerInList)) / edge).coerceIn(0f, 1f) * maxStep
                else -> 0f
            }
            if (delta != 0f) listState.scrollBy(delta)
        }
    }

    fun commitDrag() {
        val id = draggingId ?: return
        val slot = dragSlot
        draggingId = null
        dragOrder = null
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
                    text = "按住右侧手柄上下拖动，可调整同级分类的顺序。最多 3 层，" +
                        "删除分类时其中的事件会一并删除。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
                )
            }
            items(
                items = rows,
                key = { it.node.category.id },
            ) { row ->
                CategoryRow(
                    node = row.node,
                    depth = row.depth,
                    dragging = draggingId == row.node.category.id,
                    onAddChild = onAddChild,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onDragStart = { windowY ->
                        pointerWindowY = windowY
                        dragOrder = null
                        dragSlot = baseRows
                            .filter { it.node.category.parentId == row.node.category.parentId }
                            .indexOfFirst { it.node.category.id == row.node.category.id }
                            .coerceAtLeast(0)
                        draggingId = row.node.category.id
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { deltaY -> pointerWindowY += deltaY },
                    onDragEnd = { commitDrag() },
                    onDragCancel = { cancelDrag() },
                    onCenterChanged = { centerY ->
                        rowCenters[row.node.category.id] = centerY
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

@Composable
private fun CategoryRow(
    node: CategoryNode,
    depth: Int,
    dragging: Boolean,
    onAddChild: (CategoryNode) -> Unit,
    onEdit: (CategoryNode) -> Unit,
    onDelete: (CategoryNode) -> Unit,
    onDragStart: (Float) -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onCenterChanged: (Float) -> Unit,
) {
    var handleTopWindow by remember { mutableFloatStateOf(0f) }
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { onCenterChanged(it.boundsInWindow().center.y) }
            .background(
                if (dragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f) else Color.Transparent,
            )
            .clickable { onEdit(node) }
            .padding(start = (20 + depth * 18).dp, end = 4.dp)
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryDot(colorHex = node.category.colorHex, size = 10.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = node.category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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

/** 右侧三横线拖拽手柄；触摸区域 40dp，视觉 16×14dp。 */
@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    val handleColor = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .size(width = 40.dp, height = 52.dp)
            .semantics { contentDescription = "拖动排序" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(width = 16.dp, height = 14.dp)) {
            val stroke = 1.6.dp.toPx()
            val step = size.height / 3f
            for (index in 0..2) {
                val y = step * index + step / 2f
                drawLine(
                    color = handleColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = stroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
    }
}

private data class CategoryRowData(val node: CategoryNode, val depth: Int)

/**
 * 拖动中的实时预览：同一父分类下的每个兄弟行连同它的子树算作一块，
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
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(colorHex), CircleShape)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}
