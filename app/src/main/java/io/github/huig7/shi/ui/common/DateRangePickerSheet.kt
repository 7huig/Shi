package io.github.huig7.shi.ui.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate

/**
 * 自定义统计区间的单次范围选择：一次同时选起止日期，
 * 替代原来连续弹两次「起始日 → 结束日」的流程。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateRangePickerSheet(
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = DateTimeFormat.startOfDayUtcMillis(initialStart),
        initialSelectedEndDateMillis = DateTimeFormat.startOfDayUtcMillis(initialEnd),
    )
    val startMillis = state.selectedStartDateMillis
    val endMillis = state.selectedEndDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = startMillis != null && endMillis != null,
                onClick = {
                    val start = startMillis
                    val end = endMillis
                    if (start != null && end != null) {
                        onConfirm(
                            DateTimeFormat.dateFromUtcMillis(start),
                            DateTimeFormat.dateFromUtcMillis(end),
                        )
                    }
                },
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    text = "选择时间段",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                )
            },
            showModeToggle = false,
            modifier = Modifier.height(480.dp),
        )
    }
}
