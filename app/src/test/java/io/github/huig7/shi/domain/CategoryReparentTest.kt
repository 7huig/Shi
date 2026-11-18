package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.Category
import io.github.huig7.shi.domain.model.ReparentCheck
import io.github.huig7.shi.domain.model.buildCategoryTree
import io.github.huig7.shi.domain.model.checkReparent
import io.github.huig7.shi.domain.model.reparentTargets
import io.github.huig7.shi.domain.model.subtreeDepth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 分类跨层级移动的规则测试。树（最多 3 层）：
 *
 *     工作(1) ─┬─ 学习(2) ── 看书(3)
 *              └─ 会议(4)
 *     生活(5) ── 运动(6)
 */
class CategoryReparentTest {

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
            category(1, "工作", sortOrder = 0),
            category(2, "学习", parentId = 1, sortOrder = 0),
            category(3, "看书", parentId = 2, sortOrder = 0),
            category(4, "会议", parentId = 1, sortOrder = 1),
            category(5, "生活", sortOrder = 1),
            category(6, "运动", parentId = 5, sortOrder = 0),
        ),
    )

    @Test
    fun subtreeDepthCountsDeepestDescendantLevels() {
        assertEquals(2, tree.subtreeDepth(1))
        assertEquals(1, tree.subtreeDepth(2))
        assertEquals(0, tree.subtreeDepth(3))
        assertEquals(1, tree.subtreeDepth(5))
        assertEquals(0, tree.subtreeDepth(6))
    }

    @Test
    fun childCanBePromotedToRoot() {
        // 学习(2) 带一个子分类，升为顶层后最深处到第 2 层，合法。
        assertEquals(ReparentCheck.Ok, tree.checkReparent(2, null))
    }

    @Test
    fun rootCanBecomeChildOfAnotherRootWhenDepthAllows() {
        // 生活(5) 只有一个子级：挂到 工作(1) 下后为 2、3 层，合法。
        assertEquals(ReparentCheck.Ok, tree.checkReparent(5, 1))
    }

    @Test
    fun movingSubtreeThatWouldExceedThreeLevelsIsRejected() {
        // 工作(1) 已到第 3 层；挂到 生活(5) 下会变成第 4 层。
        assertEquals(ReparentCheck.DepthExceeded, tree.checkReparent(1, 5))
    }

    @Test
    fun movingUnderSelfOrDescendantIsRejected() {
        assertEquals(ReparentCheck.SelfOrDescendant, tree.checkReparent(2, 2))
        assertEquals(ReparentCheck.SelfOrDescendant, tree.checkReparent(2, 3))
    }

    @Test
    fun movingToUnknownParentIsRejected() {
        assertEquals(ReparentCheck.NotFound, tree.checkReparent(2, 999))
        assertEquals(ReparentCheck.NotFound, tree.checkReparent(999, 1))
    }

    @Test
    fun leafCannotMoveUnderDeepestLevel() {
        // 看书(3) 已在第 3 层，叶子挂到它下面会变成第 4 层。
        assertEquals(ReparentCheck.DepthExceeded, tree.checkReparent(6, 3))
        assertEquals(ReparentCheck.Ok, tree.checkReparent(6, 2))
    }

    @Test
    fun reparentTargetsExcludeSelfSubtreeAndTooDeepTargets() {
        val targets = tree.reparentTargets(2)
        val ids = targets.mapNotNull { it.node?.category?.id }.toSet()

        // 顶层始终是候选；自己与后代（2/3）必须排除。
        assertTrue(targets.any { it.node == null })
        assertTrue(2L !in ids && 3L !in ids)
        // 工作(1) 与生活(5) 都是根，挂过去后学习最多到第 2 层，合法。
        assertTrue(1L in ids && 5L in ids)
        // 会议(4) / 运动(6) 在第 2 层，挂过去会到第 4 层，必须排除。
        assertTrue(4L !in ids && 6L !in ids)
    }

    @Test
    fun reparentTargetsIncludeCurrentParent() {
        val targets = tree.reparentTargets(3)
        assertTrue(targets.any { it.node?.category?.id == 2L })
    }
}
