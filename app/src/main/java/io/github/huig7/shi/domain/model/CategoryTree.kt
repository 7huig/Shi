package io.github.huig7.shi.domain.model

data class CategoryNode(
    val category: Category,
    val children: List<CategoryNode> = emptyList(),
)

data class CategoryTree(
    val roots: List<CategoryNode> = emptyList(),
) {
    fun find(id: Long): CategoryNode? = findIn(roots, id)

    fun pathOf(id: Long): String = pathSegments(id).joinToString(" / ")

    fun pathSegments(id: Long): List<String> {
        val path = mutableListOf<String>()
        var current = find(id)
        while (current != null) {
            path.add(0, current.category.name)
            current = current.category.parentId?.let(::find)
        }
        return path
    }

    fun ancestorIds(id: Long): Set<Long> {
        val ancestors = linkedSetOf<Long>()
        var current = find(id)?.category?.parentId?.let(::find)
        while (current != null) {
            ancestors += current.category.id
            current = current.category.parentId?.let(::find)
        }
        return ancestors
    }

    fun depthOf(id: Long): Int {
        var depth = 0
        var current = find(id)
        while (current != null) {
            depth += 1
            current = current.category.parentId?.let(::find)
        }
        return depth
    }

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

    private fun findIn(nodes: List<CategoryNode>, id: Long): CategoryNode? {
        nodes.forEach { node ->
            if (node.category.id == id) return node
            findIn(node.children, id)?.let { return it }
        }
        return null
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
