package io.github.huig7.shi.ui.data

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.huig7.shi.domain.model.ImportMode
import io.github.huig7.shi.util.DateTimeFormat

/** 打开 SAF 文件选择器时接受的 MIME：不同文件管理器对 .json 的声明不一致，多给几个。 */
private val ImportMimeTypes = arrayOf(
    "application/json",
    "text/plain",
    "application/octet-stream",
)

/**
 * 默认收起的「数据备份」入口：展开后可导出（SAF 新建 .json）或导入（先选文件，再选导入方式）。
 * 具体的读写逻辑全部在 [DataTransferViewModel] 里，这里只负责 SAF 交互和状态展示。
 */
@Composable
fun DataTransferSection(
    viewModel: DataTransferViewModel,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportChooser by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) viewModel.export(context, uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportChooser = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(
                    onClickLabel = if (expanded) "收起数据备份" else "展开数据备份",
                ) { expanded = !expanded }
                .semantics { stateDescription = if (expanded) "已展开" else "已收起" }
                .padding(vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "数据备份",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                if (state.busy) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .semantics { contentDescription = "正在处理数据" },
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.size(12.dp))
                }
                Text(
                    text = if (expanded) "收起" else "展开",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "导出为 JSON 备份，或从备份文件恢复记录。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (expanded) {
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TransferButton(
                    text = "导出数据",
                    enabled = !state.busy,
                    filled = true,
                    onClick = {
                        val fileName = "時-" +
                            DateTimeFormat.isoDate(System.currentTimeMillis()) + ".json"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f),
                )
                TransferButton(
                    text = "导入数据",
                    enabled = !state.busy,
                    filled = false,
                    onClick = { importLauncher.launch(ImportMimeTypes) },
                    modifier = Modifier.weight(1f),
                )
            }
            state.message?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
    }

    if (showImportChooser) {
        ImportModeDialog(
            onDismiss = {
                showImportChooser = false
                pendingImportUri = null
            },
            onSelect = { mode ->
                val uri = pendingImportUri
                showImportChooser = false
                pendingImportUri = null
                if (uri != null) viewModel.import(context, uri, mode)
            },
        )
    }
}

@Composable
private fun TransferButton(
    text: String,
    enabled: Boolean,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ImportModeDialog(
    onDismiss: () -> Unit,
    onSelect: (ImportMode) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入数据") },
        text = {
            Column {
                ImportModeOption(
                    title = "合并导入",
                    description = "保留现有数据，复用同名同父分类，跳过已导入事件。",
                    warning = false,
                    onClick = { onSelect(ImportMode.MERGE) },
                )
                Spacer(Modifier.height(10.dp))
                ImportModeOption(
                    title = "覆盖导入",
                    description = "清空现有全部记录后导入，此操作无法撤销。",
                    warning = true,
                    onClick = { onSelect(ImportMode.REPLACE) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun ImportModeOption(
    title: String,
    description: String,
    warning: Boolean,
    onClick: () -> Unit,
) {
    val accent = if (warning) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
