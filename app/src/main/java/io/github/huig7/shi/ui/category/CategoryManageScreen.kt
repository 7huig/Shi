package io.github.huig7.shi.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item(key = "hint") {
                Text(
                    text = "分类最多 3 层。删除分类时，其中的事件会一并删除。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
                )
            }
            items(
                items = state.tree.flatten(),
                key = { it.first.category.id },
            ) { (node, depth) ->
                CategoryRow(
                    node = node,
                    depth = depth,
                    onAddChild = onAddChild,
                    onEdit = onEdit,
                    onDelete = onDelete,
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
    onAddChild: (CategoryNode) -> Unit,
    onEdit: (CategoryNode) -> Unit,
    onDelete: (CategoryNode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(node) }
            .padding(start = (20 + depth * 18).dp, end = 8.dp)
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
    }
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
