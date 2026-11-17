package io.github.huig7.shi.domain.model

data class CategoryNode(
    val category: Category,
    val children: List<CategoryNode> = emptyList(),
)

data class CategoryTree(
    val roots: List<CategoryNode> = emptyList(),
) {
    /** id → 节点索引；树构建后不变，首次访问时建一次，find/父链遍历不再递归全树。 */
    private val nodesById: Map<Long, CategoryNode> by lazy {
        val map = mutableMapOf<Long, CategoryNode>()
        fun collect(nodes: List<CategoryNode>) {
            nodes.forEach { node ->
                map[node.category.id] = node
                collect(node.children)
            }
        }
        collect(roots)
        map
    }

    fun find(id: Long): CategoryNode? = nodesById[id]

    /** 自根到 [id]（含）的分类链；找不到时返回空列表。 */
    fun chainOf(id: Long): List<CategoryNode> {
        val chain = ArrayDeque<CategoryNode>()
        var current = find(id)
        while (current != null) {
            chain.addFirst(current)
            current = current.category.parentId?.let(::find)
        }
        return chain.toList()
    }

    fun pathOf(id: Long): String = pathSegments(id).joinToString(" / ")

    fun pathSegments(id: Long): List<String> = chainOf(id).map { it.category.name }

    fun depthOf(id: Long): Int = chainOf(id).size

    fun subtreeIds(id: Long): List<Long> {
        val node = find(id) ?: return emptyList()
        val result = mutableListOf<Long>()
        fun collect(current: CategoryNode) {
            result += current.category.id
            current.children.forEach(::collect)
        }
        collect(node)
        return result
    }

    /**
     * 展开状态下的可见行：根分类始终可见；某个节点只有在「父链上所有节点都已展开」时才出现，
     * 它自身是否展开只影响它的子节点。返回的 Int 是层级深度（根为 0）。
     */
    fun visibleRows(expandedIds: Set<Long>): List<Pair<CategoryNode, Int>> {
        val result = mutableListOf<Pair<CategoryNode, Int>>()
        fun collect(nodes: List<CategoryNode>, depth: Int) {
            nodes.forEach { node ->
                result += node to depth
                if (node.category.id in expandedIds) collect(node.children, depth + 1)
            }
        }
        collect(roots, 0)
        return result
    }

    fun flatten(): List<Pair<CategoryNode, Int>> {
        val result = mutableListOf<Pair<CategoryNode, Int>>()
        fun collect(nodes: List<CategoryNode>, depth: Int) {
            nodes.forEach { node ->
                result += node to depth
                collect(node.children, depth + 1)
            }
        }
        collect(roots, 0)
        return result
    }
}

fun buildCategoryTree(categories: List<Category>): CategoryTree {
    val byParent = categories.groupBy { it.parentId }
    fun build(parentId: Long?): List<CategoryNode> =
        byParent[parentId].orEmpty()
            .sortedWith(compareBy<Category>({ it.sortOrder }, { it.id }))
            .map { category -> CategoryNode(category, build(category.id)) }
    return CategoryTree(build(null))
}
