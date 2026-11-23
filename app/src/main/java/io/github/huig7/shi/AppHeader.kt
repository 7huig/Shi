package io.github.huig7.shi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal const val PAGE_TIMELINE = 0
internal const val PAGE_STATS = 1

/** 紧凑顶栏：两个页签同为 23sp serif 样式，选中的用墨色并带 2dp 指示条。 */
@Composable
internal fun AppHeader(
    page: Int,
    onSelectPage: (Int) -> Unit,
    onSearch: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenBackup: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 22.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PageTab(
            label = "時記",
            active = page == PAGE_TIMELINE,
            onClick = { onSelectPage(PAGE_TIMELINE) },
        )
        Spacer(Modifier.width(14.dp))
        PageTab(
            label = "時計",
            active = page == PAGE_STATS,
            onClick = { onSelectPage(PAGE_STATS) },
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSearch) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "搜索备注",
                tint = muted,
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "更多",
                    tint = muted,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("分类管理") },
                    onClick = {
                        menuExpanded = false
                        onOpenCategories()
                    },
                )
                DropdownMenuItem(
                    text = { Text("数据备份") },
                    onClick = {
                        menuExpanded = false
                        onOpenBackup()
                    },
                )
            }
        }
    }
}

@Composable
private fun PageTab(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tabStyle = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 23.sp,
        lineHeight = 28.sp,
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = active, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = tabStyle,
            color = if (active) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .width(18.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    if (active) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                ),
        )
    }
}
