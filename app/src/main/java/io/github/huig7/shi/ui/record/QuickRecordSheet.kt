package io.github.huig7.shi.ui.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.ui.home.CategoryDot

/** 快速记录里的一枚分类：最近使用优先，其次用默认分类补齐。 */
data class QuickCategory(
    val id: Long,
    val name: String,
    val colorHex: Long,
)

private val QuickDurations = listOf(15, 30, 45, 60)

/**
 * 快速记录：选分类 + 时长，一键写入「现在往前 N 分钟」。
 *
 * 重叠区间不在这里静默覆盖，交给完整表单确认；需要备注或改时间时也走完整表单。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickRecordSheet(
    categories: List<QuickCategory>,
    defaultCategoryId: Long?,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, minutes: Int) -> Unit,
    onOpenFullForm: (categoryId: Long?, minutes: Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedId by rememberSaveable(defaultCategoryId, categories) {
        mutableStateOf(defaultCategoryId ?: categories.firstOrNull()?.id)
    }
    var minutes by rememberSaveable { mutableIntStateOf(30) }
    val selected = categories.firstOrNull { it.id == selectedId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "快速记录",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "以现在为结束时间，记下刚刚的一段时间。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "关闭快速记录",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "分类",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            if (categories.isEmpty()) {
                Text(
                    text = "还没有可用分类，先用完整表单选一个。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp),
                ) {
                    items(items = categories, key = { it.id }) { category ->
                        QuickCategoryChip(
                            category = category,
                            selected = category.id == selectedId,
                            onClick = { selectedId = category.id },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "时长",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = QuickDurations) { candidate ->
                    FilterChip(
                        selected = candidate == minutes,
                        onClick = { minutes = candidate },
                        label = { Text("$candidate 分钟") },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { selected?.let { onSave(it.id, minutes) } },
                enabled = selected != null,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = if (selected != null) "记录 $minutes 分钟" else "请选择分类",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            TextButton(
                onClick = { onOpenFullForm(selectedId, minutes) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "补备注或改时间",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun QuickCategoryChip(
    category: QuickCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = Color(category.colorHex)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) color.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(colorHex = category.colorHex, size = 8.dp)
            Spacer(Modifier.width(7.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
