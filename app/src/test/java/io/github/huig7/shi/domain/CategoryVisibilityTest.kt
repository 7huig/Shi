package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.Category
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.buildCategoryTree
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 分类树展开/收起后的可见行测试。
 *
 * 用一棵自建的小树，不涉及用户数据：
 * 1 阅读
 *   2 书
 *     4 小说
 *   3 影
 * 5 运动
 *   6 跑步
 */
class CategoryVisibilityTest {

    private val tree: CategoryTree = buildCategoryTree(
        listOf(
            category(1, "阅读", parentId = null, sortOrder = 0),
            category(2, "书", parentId = 1, sortOrder = 0),
            category(3, "影", parentId = 1, sortOrder = 1),
            category(4, "小说", parentId = 2, sortOrder = 0),
            category(5, "运动", parentId = null, sortOrder = 1),
            category(6, "跑步", parentId = 5, sortOrder = 0),
        ),
    )

    @Test
    fun collapsedTreeShowsOnlyRoots() {
        assertEquals(
            listOf(1L to 0, 5L to 0),
            visibleIdDepths(emptySet()),
        )
    }

    @Test
    fun expandingRootShowsItsDirectChildren() {
        assertEquals(
            listOf(1L to 0, 2L to 1, 3L to 1, 5L to 0),
            visibleIdDepths(setOf(1L)),
        )
    }

    @Test
    fun grandchildNeedsEveryAncestorExpanded() {
        // 只有 1、2 都展开时，孙级 4 才可见；可见顺序与兄弟排序一致。
        assertEquals(
            listOf(1L to 0, 2L to 1, 4L to 2, 3L to 1, 5L to 0),
            visibleIdDepths(setOf(1L, 2L)),
        )
        // 只展开 2（祖先 1 仍收起）时，孙级不可见。
        assertEquals(
            listOf(1L to 0, 5L to 0),
            visibleIdDepths(setOf(2L)),
        )
    }

    @Test
    fun expandingLeafDoesNotChangeAnything() {
        // 叶子节点没有子级，展开它不产生新行。
        assertEquals(
            visibleIdDepths(setOf(1L)),
            visibleIdDepths(setOf(1L, 3L)),
        )
    }

    @Test
    fun expandingOneRootDoesNotRevealTheOtherRootsChildren() {
        assertEquals(
            listOf(1L to 0, 5L to 0, 6L to 1),
            visibleIdDepths(setOf(5L)),
        )
    }

    @Test
    fun unknownExpandedIdsAreIgnored() {
        assertEquals(
            listOf(1L to 0, 5L to 0),
            visibleIdDepths(setOf(999L)),
        )
    }

    @Test
    fun emptyTreeHasNoVisibleRows() {
        assertEquals(
            emptyList<Pair<Long, Int>>(),
            CategoryTree().visibleRows(setOf(1L)).map { it.first.category.id to it.second },
        )
    }

    private fun visibleIdDepths(expandedIds: Set<Long>): List<Pair<Long, Int>> =
        tree.visibleRows(expandedIds).map { (node, depth) -> node.category.id to depth }

    private fun category(id: Long, name: String, parentId: Long?, sortOrder: Int): Category =
        Category(
            id = id,
            name = name,
            parentId = parentId,
            sortOrder = sortOrder,
            colorHex = 0L,
        )
}
