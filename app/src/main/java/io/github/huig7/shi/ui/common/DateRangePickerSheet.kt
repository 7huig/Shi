package io.github.huig7.shi.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
            // 自定义 headline 不再套用默认的左右 padding；铺满整行并居中，
            // 同时固定单行，长日期或大字号下只省略、不换行。
            headline = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = rangeHeadline(startMillis, endMillis),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            showModeToggle = false,
            modifier = Modifier.height(480.dp),
        )
    }
}

/** 选择器标题：同日历年只显示「M月d日 – M月d日」，跨年两端都带年份。 */
private fun rangeHeadline(startMillis: Long?, endMillis: Long?): String {
    if (startMillis == null && endMillis == null) return "选择开始和结束日期"
    val start = startMillis?.let(DateTimeFormat::dateFromUtcMillis)
    val end = endMillis?.let(DateTimeFormat::dateFromUtcMillis)
    return when {
        start != null && end == null -> DateTimeFormat.monthDay(start) + " – 选择结束日期"
        start == null && end != null -> "选择开始日期 – " + DateTimeFormat.monthDay(end)
        start != null && end != null -> {
            if (start.year == end.year) {
                DateTimeFormat.monthDay(start) + " – " + DateTimeFormat.monthDay(end)
            } else {
                start.year.toString() + "年" + DateTimeFormat.monthDay(start) + " – " +
                    end.year.toString() + "年" + DateTimeFormat.monthDay(end)
            }
        }
        else -> "选择开始和结束日期"
    }
}
