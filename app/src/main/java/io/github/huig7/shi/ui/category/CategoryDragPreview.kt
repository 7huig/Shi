package io.github.huig7.shi.ui.category

import io.github.huig7.shi.domain.model.CategoryNode

/** 分类列表的一行：节点与缩进层级。 */
internal data class CategoryRowData(val node: CategoryNode, val depth: Int)

/** 拖动预览：兄弟行连同其可见子树算一块，按 order 重排这些块，其余行保持原位。 */
internal fun reorderSiblingBlocks(
    rows: List<CategoryRowData>,
    order: List<Long>,
): List<CategoryRowData> {
    if (rows.isEmpty() || order.isEmpty()) return rows
    val orderIndex = order.withIndex().associate { (index, id) -> id to index }
    val siblingStarts = rows.indices.filter { orderIndex.containsKey(rows[it].node.category.id) }
    if (siblingStarts.isEmpty()) return rows
    val blocks = siblingStarts.map { start ->
        val depth = rows[start].depth
        var end = start + 1
        while (end < rows.size && rows[end].depth > depth) end++
        rows.subList(start, end).toList()
    }
    val prefix = rows.subList(0, siblingStarts.first()).toList()
    val suffixStart = siblingStarts.last() + blocks.last().size
    val suffix = if (suffixStart < rows.size) rows.subList(suffixStart, rows.size).toList() else emptyList()
    val ordered = blocks.sortedBy { block ->
        orderIndex[block.first().node.category.id] ?: Int.MAX_VALUE
    }
    return prefix + ordered.flatten() + suffix
}

/** 把被拖行移到兄弟列表的第 [slot] 位（slot 是去掉自己后的插入下标）。 */
internal fun orderForSlot(siblingIds: List<Long>, draggedId: Long, slot: Int): List<Long> {
    val ids = siblingIds.toMutableList()
    ids.remove(draggedId)
    ids.add(slot.coerceIn(0, ids.size), draggedId)
    return ids
}
