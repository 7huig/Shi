package com.example.timelog.ui.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.ui.home.CategoryDot

/**
 * 固定展开的分类选择区：不滚动、全部可见，一眼扫到就能点。
 * 每个根分类一行，根 + 子分类并排；行内放不下时 FlowRow 自动换行。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryGrid(
    tree: CategoryTree,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tree.roots.forEach { root ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryChip(
                    id = root.category.id,
                    name = root.category.name,
                    colorHex = root.category.colorHex,
                    selected = root.category.id == selectedId,
                    emphasized = true,
                    onSelect = onSelect,
                )
                root.children.forEach { child ->
                    CategoryChip(
                        id = child.category.id,
                        name = child.category.name,
                        colorHex = child.category.colorHex,
                        selected = child.category.id == selectedId,
                        emphasized = false,
                        onSelect = onSelect,
                    )
                    child.children.forEach { grandChild ->
                        CategoryChip(
                            id = grandChild.category.id,
                            name = grandChild.category.name,
                            colorHex = grandChild.category.colorHex,
                            selected = grandChild.category.id == selectedId,
                            emphasized = false,
                            onSelect = onSelect,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    id: Long,
    name: String,
    colorHex: Long,
    selected: Boolean,
    emphasized: Boolean,
    onSelect: (Long) -> Unit,
) {
    val color = Color(colorHex)
    Surface(
        onClick = { onSelect(id) },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        border = if (selected) {
            BorderStroke(1.5.dp, color)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(colorHex = colorHex, size = 7.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected || emphasized) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }
}
