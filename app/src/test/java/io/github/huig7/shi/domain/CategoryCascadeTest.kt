package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.Category
import io.github.huig7.shi.domain.model.buildCategoryTree
import io.github.huig7.shi.domain.model.cascadeFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 三级级联展开的纯逻辑测试。树是自己手写的小树（不是用户真实数据）：
 *
 *     工作(1) ─┬─ 学习(2) ── 看书(3)
 *              └─ 会议(4)          （叶子）
 *     生活(5) ── 运动(6)          （叶子）
 */
class CategoryCascadeTest {

    private fun category(id: Long, name: String, parentId: Long? = null, sortOrder: Int = 0) =
        Category(
            id = id,
            name = name,
            parentId = parentId,
            sortOrder = sortOrder,
            colorHex = 0xFF3F51B5L + id,
        )

    private val tree = buildCategoryTree(
        listOf(
            category(1, "工作", parentId = null, sortOrder = 0),
            category(2, "学习", parentId = 1, sortOrder = 0),
            category(3, "看书", parentId = 2, sortOrder = 0),
            category(4, "会议", parentId = 1, sortOrder = 1),
            category(5, "生活", parentId = null, sortOrder = 1),
            category(6, "运动", parentId = 5, sortOrder = 0),
        ),
    )

    private fun ids(vararg ids: Long) = ids.toList()

    @Test
    fun noSelectionShowsOnlyFirstLevel() {
        val cascade = tree.cascadeFor(null)

        assertEquals(3, cascade.levels.size)
        assertEquals(listOf(1L, 5L), cascade.levels[0].options.map { it.category.id })
        assertNull(cascade.levels[0].selectedId)
        assertTrue(cascade.levels[1].options.isEmpty())
        assertNull(cascade.levels[1].selectedId)
        assertTrue(cascade.levels[2].options.isEmpty())
        assertNull(cascade.levels[2].selectedId)
        assertTrue(cascade.path.isEmpty())
        assertNull(cascade.selected)
        assertNull(cascade.selectedCategoryId)
    }

    @Test
    fun selectingRootListsItsChildrenWithoutSelectingThem() {
        val cascade = tree.cascadeFor(1)

        assertEquals(1L, cascade.levels[0].selectedId)
        assertEquals(listOf(2L, 4L), cascade.levels[1].options.map { it.category.id })
        assertNull(cascade.levels[1].selectedId)
        assertTrue(cascade.levels[2].options.isEmpty())
        assertNull(cascade.levels[2].selectedId)
        assertEquals(listOf("工作"), cascade.path.map { it.name })
        assertEquals(1L, cascade.selectedCategoryId)
    }

    @Test
    fun selectingSecondLevelLeafLeavesThirdLevelEmpty() {
        val cascade = tree.cascadeFor(4)

        assertEquals(1L, cascade.levels[0].selectedId)
        assertEquals(4L, cascade.levels[1].selectedId)
        assertTrue(cascade.levels[2].options.isEmpty())
        assertNull(cascade.levels[2].selectedId)
        assertEquals(listOf("工作", "会议"), cascade.path.map { it.name })
        assertEquals(4L, cascade.selectedCategoryId)
    }

    @Test
    fun selectingSecondLevelWithChildrenShowsThirdLevel() {
        val cascade = tree.cascadeFor(2)

        assertEquals(2L, cascade.levels[1].selectedId)
        assertEquals(listOf(3L), cascade.levels[2].options.map { it.category.id })
        assertNull(cascade.levels[2].selectedId)
        assertEquals(listOf("工作", "学习"), cascade.path.map { it.name })
    }

    @Test
    fun selectingThirdLevelHighlightsWholePath() {
        val cascade = tree.cascadeFor(3)

        assertEquals(1L, cascade.levels[0].selectedId)
        assertEquals(2L, cascade.levels[1].selectedId)
        assertEquals(3L, cascade.levels[2].selectedId)
        assertEquals(listOf(3L), cascade.levels[2].options.map { it.category.id })
        assertEquals(listOf(1L, 2L, 3L), cascade.path.map { it.id })
        assertEquals(3L, cascade.selectedCategoryId)
    }

    @Test
    fun deepPathKeepsRootToLeafOrder() {
        val cascade = tree.cascadeFor(3)

        assertEquals(listOf("工作", "学习", "看书"), cascade.path.map { it.name })
        assertEquals(3, cascade.path.size)
        assertEquals("看书", cascade.selected?.name)
    }

    @Test
    fun unknownSelectionFallsBackToFirstLevelOnly() {
        val cascade = tree.cascadeFor(999L)

        assertEquals(listOf(1L, 5L), cascade.levels[0].options.map { it.category.id })
        assertNull(cascade.levels[0].selectedId)
        assertTrue(cascade.levels[1].options.isEmpty())
        assertTrue(cascade.levels[2].options.isEmpty())
        assertTrue(cascade.path.isEmpty())
        assertNull(cascade.selectedCategoryId)
    }

    @Test
    fun selectingSecondLevelRootShowsItsOwnChildren() {
        val cascade = tree.cascadeFor(5)

        assertEquals(5L, cascade.levels[0].selectedId)
        assertEquals(listOf(6L), cascade.levels[1].options.map { it.category.id })
        assertNull(cascade.levels[1].selectedId)
        assertEquals(listOf("生活"), cascade.path.map { it.name })
    }
}
