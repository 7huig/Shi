package io.github.huig7.shi.domain.plan

import io.github.huig7.shi.domain.MINUTE
import io.github.huig7.shi.domain.StatisticsTestFixtures
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 热力图口径：只画开启的分类、父分类含子树、跨天事件按天拆开。 */
class CategoryHeatmapTest : StatisticsTestFixtures() {

    @Test
    fun `只返回显式开启热力图的分类`() {
        val result = CategoryHeatmapBuilder.build(
            events = emptyList(),
            tree = tree(),
            plans = listOf(
                CategoryPlan(categoryId = 1, heatmapEnabled = true),
                CategoryPlan(categoryId = 4, heatmapEnabled = false),
            ),
            endDate = today,
            zone = zone,
        )
        assertEquals(listOf(1L), result.map { it.categoryId })
    }

    @Test
    fun `默认生成 168 天且结束于今天`() {
        val result = CategoryHeatmapBuilder.build(
            events = emptyList(),
            tree = tree(),
            plans = listOf(CategoryPlan(categoryId = 1, heatmapEnabled = true)),
            endDate = today,
            zone = zone,
        ).single()

        assertEquals(168, result.days.size)
        assertEquals(today.minusDays(167), result.days.first().date)
        assertEquals(today, result.days.last().date)
        assertEquals(0L, result.totalMillis)
        assertEquals(0, result.activeDays)
    }

    @Test
    fun `父分类的每日时长包含子分类`() {
        val events = listOf(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00", categoryId = 3, path = "工作 / 执行 / 编码"),
            event(2, "2026-10-31T10:00", "2026-10-31T10:30", categoryId = 1),
        )
        val result = CategoryHeatmapBuilder.build(
            events = events,
            tree = tree(),
            plans = listOf(CategoryPlan(categoryId = 1, heatmapEnabled = true)),
            endDate = today,
            zone = zone,
        ).single()

        val day = result.days.first { it.date == LocalDate.of(2026, 10, 31) }
        assertEquals(90 * MINUTE, day.millis)
        assertEquals(90 * MINUTE, result.totalMillis)
        assertEquals(1, result.activeDays)
    }

    @Test
    fun `跨天事件拆到前后两天`() {
        val events = listOf(
            event(1, "2026-10-31T23:30", "2026-11-01T00:30", categoryId = 3, path = "工作 / 执行 / 编码"),
        )
        val result = CategoryHeatmapBuilder.build(
            events = events,
            tree = tree(),
            plans = listOf(CategoryPlan(categoryId = 1, heatmapEnabled = true)),
            endDate = today,
            zone = zone,
            dayCount = 3,
        ).single()

        assertEquals(30 * MINUTE, result.days.first { it.date == LocalDate.of(2026, 10, 31) }.millis)
        assertEquals(30 * MINUTE, result.days.first { it.date == today }.millis)
        assertEquals(60 * MINUTE, result.totalMillis)
    }

    @Test
    fun `范围外的事件不进入热力图`() {
        val events = listOf(
            event(1, "2026-09-01T09:00", "2026-09-01T10:00", categoryId = 1),
        )
        val result = CategoryHeatmapBuilder.build(
            events = events,
            tree = tree(),
            plans = listOf(CategoryPlan(categoryId = 1, heatmapEnabled = true)),
            endDate = today,
            zone = zone,
            dayCount = 7,
        ).single()

        assertTrue(result.days.all { it.millis == 0L })
    }
}
