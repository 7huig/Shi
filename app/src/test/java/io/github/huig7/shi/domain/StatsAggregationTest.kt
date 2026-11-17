package io.github.huig7.shi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 事件裁剪、层级汇总、排序与孤立分类合并。 */
class StatsAggregationTest : StatisticsTestFixtures() {

    @Test
    fun emptyWindowHasNoUsages() {
        val summary = build()
        assertEquals(0L, summary.totalMillis)
        assertEquals(0, summary.eventCount)
        assertTrue(summary.roots.isEmpty())
        assertEquals(0L, summary.previousTotalMillis)
    }

    @Test
    fun clipsEventsToWindowAndNow() {
        val summary = build(
            event(1, "2026-10-31T23:00", "2026-11-01T01:00"),
            event(2, "2026-11-01T11:00", "2026-11-01T13:00"),
        )
        // 第一条只算进入窗口的 1 小时，第二条被 now(12:00) 截断，同样只剩 1 小时。
        assertEquals(2 * HOUR, summary.totalMillis)
        assertEquals(2, summary.eventCount)
        val root = summary.roots.single()
        assertEquals(2 * HOUR, root.millis)
        assertEquals(2, root.eventCount)
    }

    @Test
    fun overlappingEventsCountIndividually() {
        val summary = build(
            event(1, "2026-11-01T08:00", "2026-11-01T10:00"),
            event(2, "2026-11-01T09:00", "2026-11-01T11:00"),
        )
        assertEquals(4 * HOUR, summary.totalMillis)
        assertEquals(2, summary.eventCount)
        assertEquals(4 * HOUR, summary.roots.single().millis)
    }

    @Test
    fun ignoresEventsOutsideWindow() {
        val summary = build(event(1, "2026-10-01T08:00", "2026-10-01T09:00"))
        assertEquals(0L, summary.totalMillis)
        assertTrue(summary.roots.isEmpty())
    }

    @Test
    fun rollsSubtreeDurationAndCountIntoRoot() {
        val summary = build(
            event(1, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 3L, path = "工作 / 执行 / 编码"),
            event(2, "2026-11-01T10:00", "2026-11-01T10:30", categoryId = 6L, path = "工作 / 执行 / 编码 / 深潜"),
            event(3, "2026-11-01T10:30", "2026-11-01T10:45", categoryId = 1L, path = "工作"),
        )
        val root = summary.roots.single()
        assertEquals(1L, root.categoryId)
        assertEquals(105 * MINUTE, root.millis)
        assertEquals(3, root.eventCount)
        assertEquals(0, root.depth)

        val second = root.children.single()
        assertEquals(2L, second.categoryId)
        assertEquals(90 * MINUTE, second.millis)
        assertEquals(2, second.eventCount)
        assertEquals(1, second.depth)

        // 第三层保留，但更深的分类不再展开（时长仍计入 [third] 与祖先）。
        val third = second.children.single()
        assertEquals(3L, third.categoryId)
        assertEquals(90 * MINUTE, third.millis)
        assertEquals(2, third.eventCount)
        assertEquals(2, third.depth)
        assertTrue(third.children.isEmpty())
    }

    @Test
    fun sortsRootsByDurationThenName() {
        val busy = build(
            event(1, "2026-11-01T08:00", "2026-11-01T10:00", categoryId = 4L, path = "生活"),
            event(2, "2026-11-01T10:00", "2026-11-01T11:00", categoryId = 1L, path = "工作"),
        )
        assertEquals(listOf(4L, 1L), busy.roots.map { it.categoryId })

        val tied = build(
            event(1, "2026-11-01T08:00", "2026-11-01T09:00", categoryId = 1L, path = "工作"),
            event(2, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 4L, path = "生活"),
        )
        assertEquals(listOf("工作", "生活"), tied.roots.map { it.name })
    }

    @Test
    fun synthesizesUnknownCategoryAsLastRoot() {
        val summary = build(
            event(1, "2026-11-01T07:00", "2026-11-01T10:00", categoryId = 99L, path = "阅读 / 文学", color = 0xFFFF0000L),
            event(2, "2026-11-01T10:00", "2026-11-01T12:00", categoryId = 1L, path = "工作"),
        )
        assertEquals(2, summary.roots.size)
        assertEquals(1L, summary.roots[0].categoryId)
        val orphan = summary.roots[1]
        assertEquals(StatsBuilder.ORPHAN_CATEGORY_ID, orphan.categoryId)
        assertEquals("阅读", orphan.name)
        assertEquals(3 * HOUR, orphan.millis)
        assertEquals(1, orphan.eventCount)
        assertEquals(0xFFFF0000L, orphan.colorHex)
        assertEquals(0, orphan.depth)
    }

    @Test
    fun mergesUnknownCategoriesWithSameRootName() {
        val summary = build(
            event(1, "2026-11-01T08:00", "2026-11-01T08:30", categoryId = 98L, path = "阅读 / A", color = 0xFF112233L),
            event(2, "2026-11-01T08:30", "2026-11-01T09:15", categoryId = 99L, path = "阅读 / B", color = 0xFF445566L),
        )
        val orphan = summary.roots.single()
        assertEquals("阅读", orphan.name)
        assertEquals(75 * MINUTE, orphan.millis)
        assertEquals(2, orphan.eventCount)
        assertEquals(0xFF112233L, orphan.colorHex)
    }

    @Test
    fun blankCategoryPathFallsBackToUnknownName() {
        val summary = build(event(1, "2026-11-01T08:00", "2026-11-01T09:00", categoryId = 99L, path = ""))
        assertEquals("未分类", summary.roots.single().name)
    }
}
