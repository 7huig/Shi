package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.Category
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.domain.model.TimelineEvent
import io.github.huig7.shi.domain.model.buildCategoryTree
import io.github.huig7.shi.ui.stats.statsLabel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 统计领域层纯逻辑测试：窗口边界 / 翻页 / 裁剪 / 层级汇总 / 环比 / 孤立分类 / 下钻 / 范围文案。
 *
 * 所有时间都通过参数注入（zone = Asia/Shanghai，now = 2026-11-01T12:00，今天是周日）。
 */
class StatisticsTest {

    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val nowMillis: Long = at("2026-11-01T12:00")
    private val today: LocalDate = LocalDate.of(2026, 11, 1)

    // ---------- 窗口边界 ----------

    @Test
    fun dayWindowCoversWholeDay() {
        val window = StatsWindows.of(StatsMode.DAY, today, zone)
        assertEquals(StatsMode.DAY, window.mode)
        assertEquals(at("2026-11-01T00:00"), window.startAt)
        assertEquals(at("2026-11-02T00:00"), window.endAt)
    }

    @Test
    fun weekWindowStartsOnMonday() {
        val window = StatsWindows.of(StatsMode.WEEK, today, zone)
        assertEquals(StatsMode.WEEK, window.mode)
        assertEquals(at("2026-10-26T00:00"), window.startAt)
        assertEquals(at("2026-11-02T00:00"), window.endAt)
    }

    @Test
    fun weekWindowUsesMondayForMidweekAnchor() {
        val window = StatsWindows.of(StatsMode.WEEK, LocalDate.of(2026, 10, 28), zone)
        assertEquals(at("2026-10-26T00:00"), window.startAt)
        assertEquals(at("2026-11-02T00:00"), window.endAt)
    }

    @Test
    fun monthWindowCoversWholeMonth() {
        val window = StatsWindows.of(StatsMode.MONTH, LocalDate.of(2026, 11, 15), zone)
        assertEquals(at("2026-11-01T00:00"), window.startAt)
        assertEquals(at("2026-12-01T00:00"), window.endAt)
    }

    @Test
    fun yearWindowCoversWholeYear() {
        val window = StatsWindows.of(StatsMode.YEAR, LocalDate.of(2026, 4, 5), zone)
        assertEquals(at("2026-01-01T00:00"), window.startAt)
        assertEquals(at("2027-01-01T00:00"), window.endAt)
    }

    @Test
    fun customWindowIncludesEndDate() {
        val window = StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), zone)
        assertEquals(StatsMode.CUSTOM, window.mode)
        assertEquals(at("2026-10-01T00:00"), window.startAt)
        assertEquals(at("2026-10-16T00:00"), window.endAt)
    }

    @Test
    fun customWindowSwapsReversedDatesAndSupportsSingleDay() {
        assertEquals(
            StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), zone),
            StatsWindows.custom(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1), zone),
        )
        val singleDay = StatsWindows.custom(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1), zone)
        assertEquals(at("2026-11-01T00:00"), singleDay.startAt)
        assertEquals(at("2026-11-02T00:00"), singleDay.endAt)
    }

    @Test
    fun shiftMovesEachPeriodByOne() {
        assertEquals(
            at("2026-10-31T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.DAY, today, zone), -1, zone).startAt,
        )
        assertEquals(
            at("2026-11-02T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.WEEK, today, zone), 1, zone).startAt,
        )
        assertEquals(
            at("2025-12-01T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.MONTH, LocalDate.of(2026, 1, 15), zone), -1, zone).startAt,
        )
        assertEquals(
            at("2027-01-01T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.YEAR, LocalDate.of(2026, 4, 5), zone), 1, zone).startAt,
        )
        val day = StatsWindows.of(StatsMode.DAY, today, zone)
        assertEquals(day, StatsWindows.shift(day, 0, zone))
    }

    @Test
    fun shiftKeepsCustomWindowLength() {
        val window = StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7), zone)
        val next = StatsWindows.shift(window, 1, zone)
        assertEquals(at("2026-10-08T00:00"), next.startAt)
        assertEquals(at("2026-10-15T00:00"), next.endAt)
        val previous = StatsWindows.shift(window, -1, zone)
        assertEquals(at("2026-09-24T00:00"), previous.startAt)
        assertEquals(at("2026-10-01T00:00"), previous.endAt)
    }

    @Test
    fun customShiftAcrossSpringDstKeepsLocalMidnightAndCalendarLength() {
        val newYork = ZoneId.of("America/New_York")
        val window = StatsWindows.custom(
            LocalDate.of(2024, 3, 10),
            LocalDate.of(2024, 3, 10),
            newYork,
        )
        val next = StatsWindows.shift(window, 1, newYork)
        assertEquals(at("2024-03-11T00:00", newYork), next.startAt)
        assertEquals(at("2024-03-12T00:00", newYork), next.endAt)
    }

    @Test
    fun customShiftAcrossAutumnDstKeepsLocalMidnightAndCalendarLength() {
        val newYork = ZoneId.of("America/New_York")
        val window = StatsWindows.custom(
            LocalDate.of(2024, 11, 3),
            LocalDate.of(2024, 11, 3),
            newYork,
        )
        val next = StatsWindows.shift(window, 1, newYork)
        assertEquals(at("2024-11-04T00:00", newYork), next.startAt)
        assertEquals(at("2024-11-05T00:00", newYork), next.endAt)
    }

    // ---------- 聚合 ----------

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

    // ---------- 环比 ----------

    @Test
    fun previousWindowAlignsElapsedTime() {
        val summary = build(
            event(1, "2026-11-01T10:00", "2026-11-01T11:00"),
            event(2, "2026-10-31T03:00", "2026-10-31T04:00"),
            // now = 12:00，上一窗口只对齐到 10-31 12:00，下面这条不进入环比。
            event(3, "2026-10-31T18:00", "2026-10-31T19:00"),
        )
        assertEquals(HOUR, summary.totalMillis)
        assertEquals(HOUR, summary.previousTotalMillis)
        assertEquals(HOUR, summary.roots.single().previousMillis)
    }

    @Test
    fun previousWindowUsesFullPeriodWhenWindowIsInPast() {
        val window = StatsWindows.of(StatsMode.DAY, LocalDate.of(2026, 10, 25), zone)
        val summary = build(
            event(1, "2026-10-25T09:00", "2026-10-25T10:00"),
            event(2, "2026-10-24T20:00", "2026-10-24T21:00"),
            window = window,
        )
        assertEquals(HOUR, summary.roots.single().previousMillis)
        assertEquals(HOUR, summary.previousTotalMillis)
    }

    @Test
    fun previousMillisIsZeroForNewCategory() {
        val summary = build(event(1, "2026-11-01T10:00", "2026-11-01T11:00"))
        assertEquals(0L, summary.roots.single().previousMillis)
        assertEquals(0L, summary.previousTotalMillis)
        assertNull(summary.roots.single().deltaRatio)
    }

    @Test
    fun deltaRatioReflectsChange() {
        val increase = build(
            event(1, "2026-11-01T08:00", "2026-11-01T10:00"),
            event(2, "2026-10-31T03:00", "2026-10-31T04:00"),
        )
        assertEquals(1f, increase.roots.single().deltaRatio!!, 0.0001f)

        val decrease = build(
            event(1, "2026-11-01T08:00", "2026-11-01T09:00"),
            event(2, "2026-10-31T03:00", "2026-10-31T05:00"),
        )
        assertEquals(-0.5f, decrease.roots.single().deltaRatio!!, 0.0001f)
    }

    // ---------- 范围文案 ----------

    @Test
    fun dayLabelPrefixesTodayAndAddsYearWhenNeeded() {
        assertEquals(
            "今天 · 11月1日 周日",
            statsLabel(StatsWindows.of(StatsMode.DAY, today, zone), nowMillis, zone),
        )
        assertEquals(
            "10月31日 周六",
            statsLabel(StatsWindows.of(StatsMode.DAY, LocalDate.of(2026, 10, 31), zone), nowMillis, zone),
        )
        assertEquals(
            "2025年11月1日 周六",
            statsLabel(StatsWindows.of(StatsMode.DAY, LocalDate.of(2025, 11, 1), zone), nowMillis, zone),
        )
    }

    @Test
    fun weekLabelUsesEnDashAndAddsYearsAcrossNewYear() {
        assertEquals(
            "10月26日 – 11月1日",
            statsLabel(StatsWindows.of(StatsMode.WEEK, today, zone), nowMillis, zone),
        )
        assertEquals(
            "2025年12月29日 – 2026年1月4日",
            statsLabel(StatsWindows.of(StatsMode.WEEK, LocalDate.of(2026, 1, 1), zone), nowMillis, zone),
        )
    }

    @Test
    fun monthAndYearLabels() {
        assertEquals(
            "2026年11月",
            statsLabel(StatsWindows.of(StatsMode.MONTH, today, zone), nowMillis, zone),
        )
        assertEquals(
            "2026年",
            statsLabel(StatsWindows.of(StatsMode.YEAR, today, zone), nowMillis, zone),
        )
    }

    @Test
    fun customRangeLabelAddsYearsAcrossNewYear() {
        assertEquals(
            "10月1日 – 10月15日",
            statsLabel(StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), zone), nowMillis, zone),
        )
        assertEquals(
            "2025年12月30日 – 2026年1月2日",
            statsLabel(StatsWindows.custom(LocalDate.of(2025, 12, 30), LocalDate.of(2026, 1, 2), zone), nowMillis, zone),
        )
    }

    // ---------- 下钻 ----------

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

    // ---------- 夹具 ----------

    private fun at(value: String): Long = at(value, zone)

    private fun at(value: String, targetZone: ZoneId): Long =
        LocalDateTime.parse(value).atZone(targetZone).toInstant().toEpochMilli()

    private fun event(
        id: Long,
        start: String,
        end: String,
        categoryId: Long = 1L,
        path: String = "工作",
        color: Long = WORK_COLOR,
    ): TimelineEvent {
        val startAt = at(start)
        return TimelineEvent(
            event = Event(
                id = id,
                note = null,
                startAt = startAt,
                endAt = at(end),
                categoryId = categoryId,
                createdAt = startAt,
                updatedAt = startAt,
            ),
            categoryPath = path,
            categoryColor = color,
        )
    }

    private fun build(
        vararg events: TimelineEvent,
        window: StatsWindow = StatsWindows.of(StatsMode.DAY, today, zone),
        now: Long = nowMillis,
        tree: CategoryTree = tree(),
        rootId: Long? = null,
    ): StatsSummary = StatsBuilder.build(events.toList(), tree, window, now, zone, rootId)

    /** 工作(1) / 执行(2) / 编码(3) / 深潜(6)，生活(4) / 运动(5)。 */
    private fun tree(): CategoryTree = buildCategoryTree(
        listOf(
            category(1L, "工作", null, WORK_COLOR),
            category(2L, "执行", 1L, 0xFF4C7BE8),
            category(3L, "编码", 2L, 0xFF2F6FE4),
            category(6L, "深潜", 3L, 0xFF3F51B5),
            category(4L, "生活", null, 0xFF8B5CF6),
            category(5L, "运动", 4L, 0xFF8B5CF6),
        ),
    )

    private fun category(id: Long, name: String, parentId: Long?, colorHex: Long): Category =
        Category(
            id = id,
            name = name,
            parentId = parentId,
            sortOrder = id.toInt(),
            colorHex = colorHex,
        )

    private companion object {
        private const val MINUTE = 60_000L
        private const val HOUR = 60 * MINUTE
        private const val WORK_COLOR = 0xFF4C7BE8L
    }
}
