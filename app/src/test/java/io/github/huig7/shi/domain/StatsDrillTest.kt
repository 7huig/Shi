package io.github.huig7.shi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 下钻：子树口径、从直接子分类开始、分类不存在时回退根层。 */
class StatsDrillTest : StatisticsTestFixtures() {

    @Test
    fun drillAggregatesSubtreeAndStartsFromDirectChildren() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T09:15", categoryId = 1L),
            event(2L, "2026-11-01T09:15", "2026-11-01T09:45", categoryId = 2L),
            event(3L, "2026-11-01T09:45", "2026-11-01T10:30", categoryId = 3L),
            event(4L, "2026-11-01T10:30", "2026-11-01T10:45", categoryId = 6L),
            rootId = 1L,
        )

        // 中心总时长与记录数只算「工作」子树，列表从直接子分类「执行」开始且层级归零。
        assertEquals(105 * MINUTE, summary.totalMillis)
        assertEquals(4, summary.eventCount)
        assertEquals(1L, summary.focusCategoryId)
        assertEquals(1, summary.roots.size)
        val execution = summary.roots.first()
        assertEquals(2L, execution.categoryId)
        assertEquals(90 * MINUTE, execution.millis)
        assertEquals(0, execution.depth)
        val coding = execution.children.single()
        assertEquals(3L, coding.categoryId)
        assertEquals(60 * MINUTE, coding.millis)
        assertEquals(1, coding.depth)
        val deep = coding.children.single()
        assertEquals(6L, deep.categoryId)
        assertEquals(15 * MINUTE, deep.millis)
        assertEquals(2, deep.depth)
    }

    @Test
    fun drillIntoMiddleLevelStartsFromItsChildren() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T09:45", categoryId = 3L),
            event(2L, "2026-11-01T09:45", "2026-11-01T10:15", categoryId = 6L),
            rootId = 2L,
        )

        assertEquals(75 * MINUTE, summary.totalMillis)
        assertEquals(2, summary.eventCount)
        assertEquals(2L, summary.focusCategoryId)
        val entry = summary.roots.single()
        assertEquals(3L, entry.categoryId)
        assertEquals(75 * MINUTE, entry.millis)
        assertEquals(0, entry.depth)
        assertEquals(6L, entry.children.single().categoryId)
        assertEquals(30 * MINUTE, entry.children.single().millis)
        assertEquals(1, entry.children.single().depth)
    }

    @Test
    fun drillIntoUnknownCategoryFallsBackToRootLevel() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 1L),
            event(2L, "2026-11-01T10:00", "2026-11-01T11:00", categoryId = 2L),
            rootId = 999L,
        )

        // 分类已删除等情况下退回根层，界面不会停在空白下钻。
        assertNull(summary.focusCategoryId)
        assertEquals(2 * HOUR, summary.totalMillis)
        assertEquals(1, summary.roots.size)
        assertEquals(1L, summary.roots.single().categoryId)
        assertEquals(2 * HOUR, summary.roots.single().millis)
    }

    @Test
    fun drillExcludesCategoriesOutsideTree() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T09:30", categoryId = 2L),
            event(2L, "2026-11-01T10:00", "2026-11-01T11:00", categoryId = 99L, path = "其他"),
            rootId = 1L,
        )

        // 孤立分类（tree 中不存在）只参与根层统计，下钻时被排除。
        assertEquals(30 * MINUTE, summary.totalMillis)
        assertEquals(1, summary.eventCount)
        assertEquals(1L, summary.focusCategoryId)
        assertEquals(2L, summary.roots.single().categoryId)
        assertEquals(30 * MINUTE, summary.roots.single().millis)
    }

    @Test
    fun drillIntoLeafKeepsDirectTimeButHasNoChildren() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T09:30", categoryId = 6L),
            rootId = 6L,
        )

        // 叶子分类：直接记录仍计入中心总时长，但没有下一级可列。
        assertEquals(30 * MINUTE, summary.totalMillis)
        assertEquals(1, summary.eventCount)
        assertEquals(6L, summary.focusCategoryId)
        assertTrue(summary.roots.isEmpty())
        assertEquals(0L, summary.previousTotalMillis)
    }

    @Test
    fun drillIntoCategoryWithoutEventsIsEmpty() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 1L),
            rootId = 5L,
        )

        assertEquals(0L, summary.totalMillis)
        assertEquals(0, summary.eventCount)
        assertEquals(5L, summary.focusCategoryId)
        assertTrue(summary.roots.isEmpty())
        assertEquals(0L, summary.previousTotalMillis)
    }

    @Test
    fun drillComparesSubtreeWithAlignedPreviousWindow() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 2L),
            event(2L, "2026-10-31T09:00", "2026-10-31T09:20", categoryId = 2L),
            event(3L, "2026-10-31T09:30", "2026-10-31T09:40", categoryId = 4L),
            event(4L, "2026-10-31T18:00", "2026-10-31T19:00", categoryId = 2L),
            rootId = 1L,
        )

        // 环比换成子树口径，并沿用「对齐已过时长」规则（18:00 超出对齐窗口被裁掉）。
        assertEquals(1 * HOUR, summary.totalMillis)
        assertEquals(20 * MINUTE, summary.previousTotalMillis)
        val execution = summary.roots.single()
        assertEquals(2L, execution.categoryId)
        assertEquals(1 * HOUR, execution.millis)
        assertEquals(20 * MINUTE, execution.previousMillis)
    }

    @Test
    fun rootLevelHasNoFocusCategory() {
        val summary = build(
            event(1L, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 1L),
            event(2L, "2026-11-01T10:00", "2026-11-01T11:00", categoryId = 2L),
        )

        assertNull(summary.focusCategoryId)
        assertEquals(2 * HOUR, summary.totalMillis)
        assertEquals(1, summary.roots.size)
        assertEquals(1L, summary.roots.single().categoryId)
        assertEquals(2 * HOUR, summary.roots.single().millis)
    }
}
