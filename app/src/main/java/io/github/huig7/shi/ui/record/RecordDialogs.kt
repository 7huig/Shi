package io.github.huig7.shi.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.util.DateTimeFormat

@Composable
internal fun DeleteRecordDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除这段记录？") },
        text = { Text("记录将被永久删除，此操作无法撤销。") },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
internal fun DiscardChangesDialog(
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("放弃未保存修改？") },
        text = { Text("这次修改还没有保存，关闭后将无法恢复。") },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text("放弃修改", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("继续编辑")
            }
        },
    )
}

/** 保存会整段覆盖已有记录时的确认框：逐条列出会被删掉的记录。 */
@Composable
internal fun OverlapConfirmDialog(
    victims: List<Event>,
    isSaving: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("时间重叠") },
        text = {
            Column {
                Text("保存之后，下面这些记录会被整段覆盖并删除：")
                victims.forEach { victim ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = DateTimeFormat.rangeText(victim.startAt, victim.endAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    victim.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                errorMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text("仍要保存", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("取消") }
        },
    )
}