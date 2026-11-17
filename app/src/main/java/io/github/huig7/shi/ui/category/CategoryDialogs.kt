package io.github.huig7.shi.ui.category

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 新建 / 编辑分类：名称 + 节气色板。 */
@Composable
internal fun CategoryEditorDialog(
    editor: CategoryEditor,
    onNameChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
}

/** 删除分类前的确认：说明会一并删除的子分类与事件数。 */
@Composable
internal fun CategoryDeleteDialog(
    pending: PendingCategoryDelete,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
