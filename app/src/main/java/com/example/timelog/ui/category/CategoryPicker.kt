package com.example.timelog.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.timelog.domain.model.CategoryNode
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.ui.home.CategoryDot

@Composable
fun CategoryPicker(
    tree: CategoryTree,
    selectedId: Long?,
    expandedIds: Set<Long>,
    onSelect: (Long) -> Unit,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 224.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        tree.roots.forEach { node ->
            PickerRow(
                node = node,
                depth = 0,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onSelect = onSelect,
                onToggle = onToggle,
            )
        }
    }
}

@Composable
private fun PickerRow(
    node: CategoryNode,
    depth: Int,
    selectedId: Long?,
    expandedIds: Set<Long>,
    onSelect: (Long) -> Unit,
    onToggle: (Long) -> Unit,
) {
    val selected = node.category.id == selectedId
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 20).dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            )
            .clickable { onSelect(node.category.id) }
            .padding(horizontal = 8.dp)
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (node.children.isEmpty()) {
            Spacer(modifier = Modifier.width(32.dp))
        } else {
            IconButton(
                onClick = { onToggle(node.category.id) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (node.category.id in expandedIds) {
                        Icons.Rounded.KeyboardArrowDown
                    } else {
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight
                    },
                    contentDescription = if (node.category.id in expandedIds) "收起" else "展开",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        CategoryDot(colorHex = node.category.colorHex)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = node.category.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        if (selected) {
            Box(modifier = Modifier.padding(end = 4.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "已选择",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    if (node.category.id in expandedIds) {
        node.children.forEach { child ->
            PickerRow(
                node = child,
                depth = depth + 1,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onSelect = onSelect,
                onToggle = onToggle,
            )
        }
    }
}
