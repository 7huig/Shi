package io.github.huig7.shi.ui.record

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.ui.common.DatePickerSheet
import io.github.huig7.shi.util.DateTimeFormat
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
    onFillWholeGap: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onConfirmOverwrite: () -> Unit,
    onCancelOverwrite: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var picker by remember { mutableStateOf<PickerTarget?>(null) }
    var showDiscardConfirmation by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    val requestDismiss = {
        if (state.isDirty) showDiscardConfirmation = true else onDismiss()
    }
    val startAt = DateTimeFormat.toEpochMillis(state.startDate, state.startTime)
    val endAt = DateTimeFormat.toEpochMillis(state.endDate, state.endTime)
    val durationLabel = if (endAt > startAt) {
        "共 ${DateTimeFormat.durationText(endAt - startAt)}"
    } else {
        null
    }
    val title = when {
        state.isEditing -> "编辑记录"
        state.needsReview -> "确认这段时间"
        state.isGapFill -> "记录这段时间"
        else -> "记录一段"
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
    ) {
        // 必须在 BottomSheet 的 Dialog composition 内接管返回，Activity 外的 Handler 拦不到它。
        BackHandler(
            enabled = !showDiscardConfirmation &&
                !showDeleteConfirmation &&
                state.pendingOverwrite.isEmpty() &&
                picker == null,
        ) {
            requestDismiss()
        }
        if (state.isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "正在加载…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // 内容区滚动，底部操作条固定：小屏 / 键盘弹出时「保存」始终在手边。
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.93f)
                    .imePadding(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, top = 2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (state.isEditing && state.isReady) {
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "删除记录",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                )
                            }
                        }
                        IconButton(onClick = requestDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    if (state.needsReview) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "这段时间和已有记录重叠；保存时相邻记录会自动让路，请先确认开始 / 结束时间。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    CategoryCascadePicker(
                        tree = state.tree,
                        selectedId = state.selectedCategoryId,
                        onSelect = onCategorySelected,
                    )

                    Spacer(Modifier.height(16.dp))
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
                        supportingText = durationLabel,
                        onDateClick = { picker = PickerTarget.EndDate },
                        onTimeClick = { picker = PickerTarget.EndTime },
                    )
                    if (state.wholeGapStartAt != null && state.wholeGapEndAt != null) {
                        TextButton(
                            onClick = onFillWholeGap,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 0.dp,
                                vertical = 4.dp,
                            ),
                        ) {
                            Text(
                                text = "补齐整段（" +
                                    DateTimeFormat.durationText(
                                        state.wholeGapStartAt,
                                        state.wholeGapEndAt,
                                    ) + "）",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    if (state.timeError) {
                        Text(
                            text = "结束时间需要晚于开始时间",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("备注：做了什么？", style = MaterialTheme.typography.bodyMedium)
                        },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = requestDismiss) { Text("取消") }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = onSave,
                            enabled = state.isReady && !state.isSaving,
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
        }
    }

    if (showDeleteConfirmation) {
        DeleteRecordDialog(
            onDelete = {
                showDeleteConfirmation = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }

    if (showDiscardConfirmation) {
        DiscardChangesDialog(
            onDiscard = {
                showDiscardConfirmation = false
                onDismiss()
            },
            onDismiss = { showDiscardConfirmation = false },
        )
    }

    // 保存时如果会整段盖掉已有记录，先让用户确认；单侧裁剪 / 拆分不需要打扰。
    if (state.pendingOverwrite.isNotEmpty()) {
        OverlapConfirmDialog(
            victims = state.pendingOverwrite,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
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
