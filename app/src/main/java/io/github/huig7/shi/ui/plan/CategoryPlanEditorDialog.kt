package io.github.huig7.shi.ui.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.ui.record.TimePickerDialog
import java.time.LocalTime

/** 单个分类的计划编辑弹窗：四块配置都单独开关，互不影响。 */
@Composable
internal fun CategoryPlanEditorDialog(
    editor: CategoryPlanEditor,
    viewModel: CategoryPlanViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    var showTimePicker by rememberSaveable(editor.categoryId) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(editor.categoryName)
                Text(
                    text = editor.categoryPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                PlanToggleRow(
                    title = "显示热力图",
                    checked = editor.heatmapEnabled,
                    onCheckedChange = viewModel::setHeatmapEnabled,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                PlanToggleRow(
                    title = "每日时长目标",
                    checked = editor.goalEnabled,
                    onCheckedChange = viewModel::setGoalEnabled,
                )
                if (editor.goalEnabled) {
                    OutlinedTextField(
                        value = editor.goalMinutes,
                        onValueChange = viewModel::setGoalMinutes,
                        label = { Text("分钟") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(140.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 60).forEach { preset ->
                            FilterChip(
                                selected = editor.goalMinutes.toIntOrNull() == preset,
                                onClick = { viewModel.setGoalMinutes(preset.toString()) },
                                label = { Text("$preset", fontSize = 11.sp) },
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                PlanToggleRow(
                    title = "固定时间事项",
                    checked = editor.fixedEnabled,
                    onCheckedChange = viewModel::setFixedEnabled,
                )
                if (editor.fixedEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { showTimePicker = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        ) {
                            Text(
                                text = clockText(editor.fixedMinuteOfDay),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = editor.fixedLabel,
                            onValueChange = viewModel::setFixedLabel,
                            label = { Text("事项名称（默认分类名）") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                PlanToggleRow(
                    title = "间隔待办",
                    checked = editor.intervalEnabled,
                    onCheckedChange = viewModel::setIntervalEnabled,
                )
                if (editor.intervalEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = editor.intervalMinutes,
                            onValueChange = viewModel::setIntervalMinutes,
                            label = { Text("间隔分钟") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = editor.intervalLabel,
                            onValueChange = viewModel::setIntervalLabel,
                            label = { Text("待办名称（默认分类名）") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                editor.error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = LocalTime.of(
                editor.fixedMinuteOfDay / 60,
                editor.fixedMinuteOfDay % 60,
            ),
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                showTimePicker = false
                viewModel.setFixedMinuteOfDay(time.hour * 60 + time.minute)
            },
        )
    }
}

/** 开关行：整行可点，避免只有小开关可点导致大字号下难点。 */
@Composable
private fun PlanToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
