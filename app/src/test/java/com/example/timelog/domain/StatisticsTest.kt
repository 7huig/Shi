package com.example.timelog.domain

import com.example.timelog.domain.model.Event
import com.example.timelog.domain.model.TimelineEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsTest {

    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")

    /** 2026-11-01 是周日，所以「本周」从 2026-10-26（周一）开始。 */
    private val today: LocalDate = LocalDate.of(2026, 11, 1)
    private val now: Long = at("2026-11-01T12:00")

    private val hour: Long = 3_600_000L
    private val minute: Long = 60_000L

    @Test
    fun emptyEventsProduceZeroSummaryWithSevenEmptyDays() {
        val summary = StatsBuilder.build(emptyList(), StatsPeriod.TODAY, now, zone)

        assertEquals(StatsPeriod.TODAY, summary.period)
        assertEquals(0L, summary.totalMillis)
        assertEquals(0, summary.eventCount)
        assertTrue(summary.categories.isEmpty())
        assertEquals(7, summary.last7Days.size)
        assertEquals(
            (6 downTo 0).map { today.minusDays(it.toLong()) },
            summary.last7Days.map { it.date },
        )
        assertEquals(today.minusDays(6), summary.last7Days.first().date)
        assertEquals(today, summary.last7Days.last().date)
        assertTrue(summary.last7Days.all { it.millis == 0L })
    }

    @Test
    fun todayClipsEventAtMidnightAndAtNow() {
        val summary = build(
            event(1, "2026-10-31T23:00", "2026-11-01T01:00"),
            period = StatsPeriod.TODAY,
        )

        assertEquals(hour, summary.totalMillis)
        assertEquals(1, summary.eventCount)
        val category = summary.categories.single()
        assertEquals("工作", category.rootName)
        assertEquals(hour, category.millis)
        // 「最近 7 天」按自然日拆分，跨午夜的那一小时各归各天。
        assertEquals(hour, dayMillis(summary, "2026-10-31"))
        assertEquals(hour, dayMillis(summary, "2026-11-01"))
    }

    @Test
    fun todayIgnoresEventsOutsideTheDay() {
        val summary = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00"),
            period = StatsPeriod.TODAY,
        )

        assertEquals(0L, summary.totalMillis)
        assertEquals(0, summary.eventCount)
        assertTrue(summary.categories.isEmpty())
        assertEquals(hour, dayMillis(summary, "2026-10-31"))
        assertEquals(0L, dayMillis(summary, "2026-11-01"))
    }

    @Test
    fun weekStartsOnMonday() {
        val summary = build(
            event(1, "2026-10-25T09:00", "2026-10-25T10:00"), // 上周日，范围外
            event(2, "2026-10-26T00:00", "2026-10-26T01:00"), // 本周一 00:00 起算
            event(3, "2026-10-28T10:00", "2026-10-28T11:00"),
            event(4, "2026-11-01T08:00", "2026-11-01T09:00"),
            period = StatsPeriod.WEEK,
        )

        assertEquals(3 * hour, summary.totalMillis)
        assertEquals(3, summary.eventCount)
        assertEquals(3 * hour, summary.categories.single().millis)
    }

    @Test
    fun monthStartsOnFirstDayAndAllStartsAtEarliestEvent() {
        val events = listOf(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00"),
            event(2, "2026-11-01T08:00", "2026-11-01T09:00"),
        )

        val month = StatsBuilder.build(events, StatsPeriod.MONTH, now, zone)
        assertEquals(hour, month.totalMillis)
        assertEquals(1, month.eventCount)

        val all = StatsBuilder.build(events, StatsPeriod.ALL, now, zone)
        assertEquals(2 * hour, all.totalMillis)
        assertEquals(2, all.eventCount)
    }

    @Test
    fun allClipsEventsThatEndAfterNow() {
        val summary = build(
            event(1, "2026-11-01T11:00", "2026-11-01T15:00"),
            period = StatsPeriod.ALL,
        )

        assertEquals(hour, summary.totalMillis)
        assertEquals(hour, build(event(1, "2026-11-01T11:00", "2026-11-01T15:00"), StatsPeriod.TODAY).totalMillis)
    }

    @Test
    fun categoriesGroupByRootAndSortByDurationDescending() {
        val summary = build(
            event(1, "2026-11-01T08:00", "2026-11-01T11:00", path = "生活 / 运动", color = 0xFF22AA22),
            event(2, "2026-11-01T09:00", "2026-11-01T10:00", path = "工作 / 执行"),
            event(3, "2026-11-01T10:00", "2026-11-01T10:30", path = "工作 / 会议"),
            period = StatsPeriod.TODAY,
        )

        assertEquals(4 * hour + 30 * minute, summary.totalMillis)
        assertEquals(3, summary.eventCount)
        assertEquals(listOf("生活", "工作"), summary.categories.map { it.rootName })
        assertEquals(3 * hour, summary.categories[0].millis)
        assertEquals(hour + 30 * minute, summary.categories[1].millis)
        // 代表色来自该根下最早开始的那条事件（工作 / 执行）。
        assertEquals(0xFF4C7BE8, summary.categories[1].colorHex)
        assertEquals(0xFF22AA22, summary.categories[0].colorHex)
    }

    @Test
    fun categoryColorComesFromEarliestEventInRootIgnoringInputOrder() {
        val later = event(2, "2026-11-01T10:00", "2026-11-01T11:00", path = "工作 / 会议", color = 0xFF111111)
        val earlier = event(1, "2026-11-01T09:00", "2026-11-01T10:00", path = "工作 / 执行", color = 0xFF4C7BE8)

        val summary = StatsBuilder.build(listOf(later, earlier), StatsPeriod.TODAY, now, zone)

        assertEquals(0xFF4C7BE8, summary.categories.single().colorHex)
    }

    @Test
    fun categoryColorUsesLowerIdWhenStartTimesTie() {
        val higherId = event(5, "2026-11-01T09:00", "2026-11-01T10:00", color = 0xFF111111)
        val lowerId = event(2, "2026-11-01T09:00", "2026-11-01T10:00", color = 0xFF4C7BE8)

        val summary = StatsBuilder.build(listOf(higherId, lowerId), StatsPeriod.TODAY, now, zone)

        assertEquals(0xFF4C7BE8, summary.categories.single().colorHex)
    }

    @Test
    fun equalDurationsBreakTiesByRootName() {
        val summary = build(
            event(1, "2026-11-01T08:00", "2026-11-01T09:00", path = "Beta / b"),
            event(2, "2026-11-01T09:00", "2026-11-01T10:00", path = "Alpha / a"),
            period = StatsPeriod.TODAY,
        )

        assertEquals(listOf("Alpha", "Beta"), summary.categories.map { it.rootName })
    }

    @Test
    fun singleLevelCategoryPathIsItsOwnRoot() {
        val summary = build(
            event(1, "2026-11-01T09:00", "2026-11-01T10:00", path = "未分类"),
            period = StatsPeriod.TODAY,
        )

        assertEquals("未分类", summary.categories.single().rootName)
    }

    @Test
    fun overlappingEventsAreCountedPerEvent() {
        // 口径：逐条事件累加，重叠不合并（与分类明细可核对）。
        val summary = build(
            event(1, "2026-11-01T09:00", "2026-11-01T10:00"),
            event(2, "2026-11-01T09:00", "2026-11-01T10:00"),
            period = StatsPeriod.TODAY,
        )

        assertEquals(2 * hour, summary.totalMillis)
        assertEquals(2, summary.eventCount)
        assertEquals(2 * hour, summary.categories.single().millis)
    }

    @Test
    fun last7DaysIsSevenDayWindowEndingTodayWithZeros() {
        val summary = build(
            event(1, "2026-10-26T10:00", "2026-10-26T11:00"), // 窗口第一天
            event(2, "2026-11-01T09:00", "2026-11-01T10:00"), // 今天
            period = StatsPeriod.TODAY,
        )

        assertEquals(
            listOf(
                "2026-10-26",
                "2026-10-27",
                "2026-10-28",
                "2026-10-29",
                "2026-10-30",
                "2026-10-31",
                "2026-11-01",
            ),
            summary.last7Days.map { it.date.toString() },
        )
        assertEquals(hour, dayMillis(summary, "2026-10-26"))
        assertEquals(0L, dayMillis(summary, "2026-10-27"))
        assertEquals(0L, dayMillis(summary, "2026-10-31"))
        assertEquals(hour, dayMillis(summary, "2026-11-01"))
    }

    @Test
    fun last7DaysSplitsEventAtMidnight() {
        val summary = build(
            event(1, "2026-10-31T23:30", "2026-11-01T00:30"),
            period = StatsPeriod.TODAY,
        )

        assertEquals(30 * minute, dayMillis(summary, "2026-10-31"))
        assertEquals(30 * minute, dayMillis(summary, "2026-11-01"))
        assertEquals(30 * minute, summary.totalMillis)
    }

    @Test
    fun last7DaysIgnoresEventsBeforeTheWindow() {
        val summary = build(
            event(1, "2026-10-20T09:00", "2026-10-20T10:00"),
            period = StatsPeriod.ALL,
        )

        assertTrue(summary.last7Days.all { it.millis == 0L })
        assertEquals(hour, summary.totalMillis)
    }

    @Test
    fun last7DaysIsIndependentOfSelectedPeriod() {
        val summary = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00"),
            period = StatsPeriod.TODAY,
        )

        assertEquals(0L, summary.totalMillis)
        assertEquals(hour, dayMillis(summary, "2026-10-31"))
    }

    @Test
    fun cliplessRangeStaysEmptyWhenAllEventsAreInTheFuture() {
        val summary = build(
            event(1, "2026-11-02T09:00", "2026-11-02T10:00"),
            period = StatsPeriod.ALL,
        )

        assertEquals(0L, summary.totalMillis)
        assertEquals(0, summary.eventCount)
        assertTrue(summary.categories.isEmpty())
    }

    private fun build(vararg events: TimelineEvent, period: StatsPeriod): StatsSummary =
        StatsBuilder.build(events.toList(), period, now, zone)

    private fun build(event: TimelineEvent, period: StatsPeriod): StatsSummary =
        StatsBuilder.build(listOf(event), period, now, zone)

    private fun dayMillis(summary: StatsSummary, date: String): Long =
        summary.last7Days.single { it.date == LocalDate.parse(date) }.millis

    private fun event(
        id: Long,
        start: String,
        end: String,
        path: String = "工作 / 执行",
        color: Long = 0xFF4C7BE8,
    ): TimelineEvent = TimelineEvent(
        event = Event(
            id = id,
            note = null,
            startAt = at(start),
            endAt = at(end),
            categoryId = 1L,
            createdAt = 0L,
            updatedAt = 0L,
        ),
        categoryPath = path,
        categoryColor = color,
    )

    private fun at(local: String): Long =
        LocalDateTime.parse(local).atZone(zone).toInstant().toEpochMilli()
}
