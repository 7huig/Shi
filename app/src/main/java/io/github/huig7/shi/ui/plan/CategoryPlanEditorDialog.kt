package io.github.huig7.shi.ui.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    // 0 表示没有打开时间选择器；数据库里的固定时间 id 都是正数，新建草稿是负数。
    var pickerKey by rememberSaveable(editor.categoryId) { mutableStateOf(0L) }

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
                    title = "每日时长",
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
                    title = "固定时间",
                    checked = editor.fixedEnabled,
                    onCheckedChange = viewModel::setFixedEnabled,
                )
                if (editor.fixedEnabled) {
                    editor.fixedTimes.forEach { fixed ->
                        FixedTimeRow(
                            fixed = fixed,
                            categoryName = editor.categoryName,
                            onTimeClick = { pickerKey = fixed.key },
                            onLabelChange = { viewModel.setFixedTimeLabel(fixed.key, it) },
                            onLeadChange = { viewModel.setFixedTimeLead(fixed.key, it) },
                            onRemove = { viewModel.removeFixedTime(fixed.key) },
                        )
                    }
                    TextButton(
                        onClick = viewModel::addFixedTime,
                        enabled = editor.fixedTimes.size < MAX_FIXED_TIMES,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.width(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("添加时间")
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

    val picking = editor.fixedTimes.firstOrNull { it.key == pickerKey }
    if (picking != null) {
        TimePickerDialog(
            initialTime = LocalTime.of(
                picking.minuteOfDay / 60,
                picking.minuteOfDay % 60,
            ),
            onDismiss = { pickerKey = 0L },
            onConfirm = { time ->
                viewModel.setFixedTimeMinute(picking.key, time.hour * 60 + time.minute)
                pickerKey = 0L
            },
        )
    }
}

@Composable
private fun FixedTimeRow(
    fixed: FixedTimeDraft,
    categoryName: String,
    onTimeClick: () -> Unit,
    onLabelChange: (String) -> Unit,
    onLeadChange: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onTimeClick,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            ) {
                Text(
                    text = clockText(fixed.minuteOfDay),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = fixed.label,
                onValueChange = onLabelChange,
                label = { Text("事项名称") },
                placeholder = { Text(categoryName, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "删除这个时间",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "提前提示",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(4.dp))
            LeadTimeMenu(leadMinutes = fixed.leadMinutes, onSelect = onLeadChange)
        }
    }
}

@Composable
private fun LeadTimeMenu(leadMinutes: Int, onSelect: (Int) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Text(
                text = leadText(leadMinutes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            LEAD_OPTIONS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(leadText(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

private fun leadText(minutes: Int): String =
    if (minutes <= 0) "准时提醒" else "提前 $minutes 分钟"

/** 给编辑弹窗复用：把分钟数格式化成 HH:mm。 */
internal fun clockText(minuteOfDay: Int): String =
    "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

private val LEAD_OPTIONS = listOf(0, 5, 10, 15, 30, 60, 120)

private const val MAX_FIXED_TIMES = 8

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
