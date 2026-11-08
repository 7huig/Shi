package io.github.huig7.shi.domain.model

/**
 * 级联选择器的一栏：这一栏能选什么、当前选中了哪一个。
 *
 * 选项固定来自上一栏选中节点的 children；[selectedId] 为 null 表示这一栏还没有人选中
 * （用户可能只选到上一级，这是允许的——记录可以挂到任意一级）。
 */
data class CascadeLevel(
    val options: List<CategoryNode>,
    val selectedId: Long?,
)

/**
 * 三级级联选择器的展开结果。levels 固定 3 个（一级 / 二级 / 三级），
 * path 是从根到当前选中分类的完整路径，便于顶部显示「工作 / 学习 / 看书」。
 *
 * 纯数据 + 纯函数，不依赖 Compose / Android，方便单测。
 */
data class CategoryCascade(
    val levels: List<CascadeLevel>,
    val path: List<Category>,
    val selected: Category?,
) {
    /** 与 RecordUiState.selectedCategoryId 对齐的简化入口。 */
    val selectedCategoryId: Long? get() = selected?.id

    companion object {
        const val LEVEL_COUNT = 3
    }
}

/**
 * 以 [selectedId] 为终点展开三栏：
 * 一级 = roots；二级 = 一级选中节点的 children；三级 = 二级选中节点的 children。
 *
 * 只选到中间层也成立——选中根分类时二级仍列出它的 children（无人选中），三级为空。
 * selectedId 为 null 或找不到时只保留一级栏目，避免 UI 出现悬空的选项。
 */
fun CategoryTree.cascadeFor(selectedId: Long?): CategoryCascade {
    // 从选中节点往上找到根，得到 [根, ..., 选中]；找不到就是空列表。
    val chain = selectedId?.let(::ancestorChain).orEmpty()
    val level1 = levelOf(roots, chain.getOrNull(0))
    val level2 = levelOf(chain.getOrNull(0)?.children.orEmpty(), chain.getOrNull(1))
    val level3 = levelOf(chain.getOrNull(1)?.children.orEmpty(), chain.getOrNull(2))
    val path = chain.map { it.category }
    return CategoryCascade(
        levels = listOf(level1, level2, level3),
        path = path,
        selected = path.lastOrNull(),
    )
}

/** 保证 selectedId 一定出现在本栏 options 里，数据异常时宁可不选中也不要高亮错行。 */
private fun levelOf(options: List<CategoryNode>, selected: CategoryNode?): CascadeLevel =
    CascadeLevel(
        options = options,
        selectedId = selected?.category?.id?.takeIf { id -> options.any { it.category.id == id } },
    )

/** 与 CategoryTree.pathSegments 同源，但保留 Category 本体（UI 需要 name 与 colorHex）。 */
private fun CategoryTree.ancestorChain(id: Long): List<CategoryNode> {
    val chain = ArrayDeque<CategoryNode>()
    var current = find(id)
    while (current != null) {
        chain.addFirst(current)
        current = current.category.parentId?.let(::find)
    }
    return chain.toList()
}
