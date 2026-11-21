package io.github.huig7.shi.ui.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.reparentTargets
import io.github.huig7.shi.ui.home.CategoryDot

/** 新建 / 编辑分类：名称、父分类（可跨层级调整）与节气色板。 */
@Composable
internal fun CategoryEditorDialog(
    editor: CategoryEditor,
    tree: CategoryTree,
    onNameChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    onParentChange: (Long?) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    var showParentPicker by rememberSaveable(editor.id) { mutableStateOf(false) }
    val parentName = editor.parentId?.let { tree.find(it)?.category?.name }
    val editing = editor.id != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (editing) "编辑分类" else "新建分类")
        },
        text = {
            // 大字号下名称 + 父级 + 色板可能超出弹窗高度，允许滚动避免底部色板被裁掉。
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (editing) {
                    Text(
                        text = "父分类",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        onClick = { showParentPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = parentName ?: "顶层（根分类）",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "选择父分类",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (parentName != null) {
                    Text(
                        text = "父分类：$parentName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = onNameChange,
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
                    onSelect = onColorChange,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = editor.name.isNotBlank(),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )

    if (showParentPicker && editor.id != null) {
        CategoryParentPickerDialog(
            tree = tree,
            categoryId = editor.id,
            selectedParentId = editor.parentId,
            onSelect = { parentId ->
                showParentPicker = false
                onParentChange(parentId)
            },
            onDismiss = { showParentPicker = false },
        )
    }
}

/** 编辑分类时选择父级：只列出不会形成环、且移动后仍不超过 3 层的目标。 */
@Composable
private fun CategoryParentPickerDialog(
    tree: CategoryTree,
    categoryId: Long,
    selectedParentId: Long?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val targets = remember(tree, categoryId) { tree.reparentTargets(categoryId) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调整父分类") },
        text = {
            if (targets.isEmpty()) {
                Text("没有可用的父分类。")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(items = targets, key = { it.node?.category?.id ?: -1L }) { target ->
                        val node = target.node
                        CategoryTargetRow(
                            title = node?.category?.name ?: "顶层（成为根分类）",
                            depth = target.depth,
                            colorHex = node?.category?.colorHex,
                            selected = node?.category?.id == selectedParentId,
                            onClick = { onSelect(node?.category?.id) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

/** 删除分类前的确认：说明会一并删除的子分类与事件数，并给「保留记录」的出口。 */
@Composable
internal fun CategoryDeleteDialog(
    pending: PendingCategoryDelete,
    tree: CategoryTree,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onMoveEvents: (Long) -> Unit,
) {
    var showTargetPicker by rememberSaveable(pending.node.category.id) { mutableStateOf(false) }
    val moveCandidates = remember(tree, pending.node.category.id) {
        val excluded = tree.subtreeIds(pending.node.category.id).toSet()
        tree.flatten()
            .filter { (node, _) -> node.category.id !in excluded }
            .map { (node, depth) -> node to depth }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除分类？") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("「${pending.node.category.name}」及其子分类会被删除。")
                if (pending.eventCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    val moveTargetId = pending.moveTargetId
                    if (moveTargetId != null) {
                        Text(
                            text = "其中 ${pending.eventCount} 条事件默认会一并删除；" +
                                "也可以保留它们，移到「${pending.moveTargetName}」。",
                        )
                        Spacer(Modifier.height(6.dp))
                        TextButton(
                            onClick = { onMoveEvents(moveTargetId) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "保留 ${pending.eventCount} 条记录，移到「${pending.moveTargetName}」",
                            )
                        }
                    } else {
                        Text(
                            text = "这是顶层分类，没有父级可以接收记录。" +
                                "默认会连同 ${pending.eventCount} 条事件一起删除；" +
                                "也可以保留它们，另外选择一个分类。",
                        )
                        Spacer(Modifier.height(6.dp))
                        TextButton(
                            onClick = { showTargetPicker = true },
                            enabled = moveCandidates.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("保留 ${pending.eventCount} 条记录，选择目标分类")
                        }
                        if (moveCandidates.isEmpty()) {
                            Text(
                                text = "没有其他分类可以接收记录，请先新建一个分类。",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "删除分类本身无法撤销。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(if (pending.eventCount > 0) "全部删除" else "删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )

    if (showTargetPicker) {
        CategoryMoveTargetPickerDialog(
            candidates = moveCandidates,
            onSelect = { targetId ->
                showTargetPicker = false
                onMoveEvents(targetId)
            },
            onDismiss = { showTargetPicker = false },
        )
    }
}

/** 根分类删除时选择记录的新归属。 */
@Composable
private fun CategoryMoveTargetPickerDialog(
    candidates: List<Pair<CategoryNode, Int>>,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择目标分类") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(items = candidates, key = { it.first.category.id }) { (node, depth) ->
                    CategoryTargetRow(
                        title = node.category.name,
                        depth = depth,
                        colorHex = node.category.colorHex,
                        selected = false,
                        onClick = { onSelect(node.category.id) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

/** 父分类 / 目标分类共用的单行：缩进表示层级，右侧勾选当前父级。 */
@Composable
private fun CategoryTargetRow(
    title: String,
    depth: Int,
    colorHex: Long?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (depth * 16).dp, top = 11.dp, bottom = 11.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (colorHex != null) {
            CategoryDot(colorHex = colorHex, size = 8.dp)
        } else {
            Box(modifier = Modifier.size(8.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "当前父分类",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
