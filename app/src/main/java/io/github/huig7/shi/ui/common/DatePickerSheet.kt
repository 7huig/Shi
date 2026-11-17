package io.github.huig7.shi.ui.common

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import io.github.huig7.shi.util.DateTimeFormat
import java.time.LocalDate

/** 记录表单与统计自定义时间段共用的日期选择对话框。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerSheet(
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