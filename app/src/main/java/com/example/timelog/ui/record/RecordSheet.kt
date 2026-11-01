package com.example.timelog.ui.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.timelog.domain.model.Event
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime

private enum class PickerTarget { StartDate, StartTime, EndDate, EndTime }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSheet(
    state: RecordUiState,
    onDismiss: () -> Unit,
    onNoteChange: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onStartTimeSelected: (LocalTime) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit,
    onSave: () -> Unit,
    onConfirmOverwrite: () -> Unit,
    onCancelOverwrite: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var picker by remember { mutableStateOf<PickerTarget?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            Text(
                text = if (state.isEditing) "编辑记录" else "记录一段",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(18.dp))
            FieldLabel("分类")
            Spacer(Modifier.height(8.dp))
            CategoryGrid(
                tree = state.tree,
                selectedId = state.selectedCategoryId,
                onSelect = onCategorySelected,
            )

            Spacer(Modifier.height(20.dp))
            FieldLabel("时间")
            Spacer(Modifier.height(8.dp))
            TimeField(
                label = "开始",
                date = state.startDate,
                time = state.startTime,
                onDateClick = { picker = PickerTarget.StartDate },
                onTimeClick = { picker = PickerTarget.StartTime },
            )
            Spacer(Modifier.height(10.dp))
            TimeField(
                label = "结束",
                date = state.endDate,
                time = state.endTime,
                onDateClick = { picker = PickerTarget.EndDate },
                onTimeClick = { picker = PickerTarget.EndTime },
            )
            if (state.timeError) {
                Text(
                    text = "结束时间需要晚于开始时间",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 38.dp, top = 6.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel("备注")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("可留空", style = MaterialTheme.typography.bodyMedium) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp).widthIn(min = 112.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("保存", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // 保存时如果会整段盖掉已有记录，先让用户确认；单侧裁剪 / 拆分不需要打扰。
    if (state.pendingOverwrite.isNotEmpty()) {
        OverlapConfirmDialog(
            victims = state.pendingOverwrite,
            isSaving = state.isSaving,
            onConfirm = onConfirmOverwrite,
            onDismiss = onCancelOverwrite,
        )
    }

    when (picker) {
        PickerTarget.StartDate -> DatePickerSheet(
            initialDate = state.startDate,
            onDismiss = { picker = null },
            onConfirm = { date ->
                picker = null
                onStartDateSelected(date)
            },
        )

        PickerTarget.EndDate -> DatePickerSheet(
            initialDate = state.endDate,
            onDismiss = { picker = null },
            onConfirm = { date ->
                picker = null
                onEndDateSelected(date)
            },
        )

        PickerTarget.StartTime -> TimePickerDialog(
            initialTime = state.startTime,
            onDismiss = { picker = null },
            onConfirm = { time ->
                picker = null
                onStartTimeSelected(time)
            },
        )

        PickerTarget.EndTime -> TimePickerDialog(
            initialTime = state.endTime,
            onDismiss = { picker = null },
            onConfirm = { time ->
                picker = null
                onEndTimeSelected(time)
            },
        )

        null -> Unit
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun TimeField(
    label: String,
    date: LocalDate,
    time: LocalTime,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(38.dp),
        )
        Surface(
            onClick = onDateClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            Text(
                text = DateTimeFormat.dateWithWeekday(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            onClick = onTimeClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        ) {
            Text(
                text = DateTimeFormat.clock(time),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateTimeFormat.startOfDayUtcMillis(initialDate),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onConfirm(DateTimeFormat.dateFromUtcMillis(it))
                    }
                },
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = pickerState)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(Modifier.width(4.dp))
                    TextButton(
                        onClick = {
                            onConfirm(LocalTime.of(pickerState.hour, pickerState.minute))
                        },
                    ) { Text("确定") }
                }
            }
        }
    }
}

/** 保存会整段覆盖已有记录时的确认框：逐条列出会被删掉的记录。 */
@Composable
private fun OverlapConfirmDialog(
    victims: List<Event>,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
