package io.github.huig7.shi.domain.annual

import io.github.huig7.shi.domain.HOUR
import io.github.huig7.shi.domain.StatisticsTestFixtures
import io.github.huig7.shi.domain.model.TimelineEvent
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 年度报告的口径：裁剪、分桶、极值与同比。 */
class AnnualReportBuilderTest : StatisticsTestFixtures() {

    private fun report(
        year: Int,
        vararg events: TimelineEvent,
        now: Long = at("2027-01-01T00:00"),
    ): AnnualReport = AnnualReportBuilder.build(events.toList(), tree(), year, now, zone)

    @Test
    fun emptyYearReturnsZeroedReport() {
        val report = report(2025, now = at("2026-11-01T12:00"))
        assertFalse(report.hasData)
        assertEquals(0L, report.totalMillis)
        assertEquals(0, report.activeDays)
        assertEquals(0, report.longestStreakDays)
        assertEquals(12, report.months.size)
        assertTrue(report.months.all { it == 0L })
        assertTrue(report.hourStarts.all { it == 0 })
        assertTrue(report.seasons.all { it.millis == 0L })
        assertNull(report.longestEvent)
        assertNull(report.busiestDay)
        assertNull(report.busiestMonth)
        assertEquals(0f, report.coverageRatio!!, 0.0001f)
    }

    @Test
    fun currentYearIsClippedToNowAndMarkedPartial() {
        val report = report(
            2026,
            event(1, "2026-11-01T10:00", "2026-11-01T13:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(2 * HOUR, report.totalMillis)
        assertTrue(report.isPartial)
        assertEquals(LocalDate.of(2026, 11, 1), report.throughDate)
        assertEquals(1, report.activeDays)
        assertEquals(2 * HOUR, report.months[10])
    }

    @Test
    fun pastYearIsCompleteThroughDecember() {
        val report = report(
            2025,
            event(1, "2025-06-01T10:00", "2025-06-01T11:00"),
            now = at("2026-11-01T12:00"),
        )
        assertFalse(report.isPartial)
        assertEquals(LocalDate.of(2025, 12, 31), report.throughDate)
        assertEquals(1 * HOUR, report.totalMillis)
    }

    @Test
    fun monthsAndSeasonsSumToTotal() {
        val report = report(
            2025,
            event(1, "2025-01-10T08:00", "2025-01-10T09:00"),
            event(2, "2025-03-10T08:00", "2025-03-10T10:00"),
            event(3, "2025-06-10T08:00", "2025-06-10T11:00"),
            event(4, "2025-10-10T08:00", "2025-10-10T12:00"),
            event(5, "2025-12-10T08:00", "2025-12-10T13:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(15 * HOUR, report.totalMillis)
        assertEquals(15 * HOUR, report.months.sum())
        // 冬 = 1 月 + 12 月；春 = 3 月；夏 = 6 月；秋 = 10 月。
        assertEquals(
            listOf(2 * HOUR, 3 * HOUR, 4 * HOUR, 6 * HOUR),
            report.seasons.map { it.millis },
        )
        assertEquals(listOf("春", "夏", "秋", "冬"), report.seasons.map { it.name })
        assertEquals(12, report.busiestMonth)
    }

    @Test
    fun crossYearEventIsClippedIntoBothYears() {
        val cross = event(1, "2025-12-31T23:00", "2026-01-01T02:00")
        val first = report(2025, cross, now = at("2026-11-01T12:00"))
        assertEquals(1 * HOUR, first.totalMillis)
        assertEquals(1 * HOUR, first.months[11])

        val second = report(2026, cross, now = at("2026-11-01T12:00"))
        assertEquals(2 * HOUR, second.totalMillis)
        assertEquals(2 * HOUR, second.months[0])
        assertEquals(2 * HOUR, second.months.sum())
    }

    @Test
    fun activeDaysDedupeAndStreakCountsConsecutiveDates() {
        val report = report(
            2026,
            event(1, "2026-03-01T08:00", "2026-03-01T09:00"),
            event(2, "2026-03-01T10:00", "2026-03-01T10:30"),
            event(3, "2026-03-02T08:00", "2026-03-02T09:00"),
            event(4, "2026-03-03T08:00", "2026-03-03T09:00"),
            event(5, "2026-03-05T08:00", "2026-03-05T09:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(4, report.activeDays)
        assertEquals(3, report.longestStreakDays)
        assertEquals(LocalDate.of(2026, 3, 1), report.busiestDay?.date)
        assertEquals(90 * 60_000L, report.busiestDay?.millis)
    }

    @Test
    fun busiestDayTiePicksEarlierDate() {
        val report = report(
            2026,
            event(1, "2026-04-02T08:00", "2026-04-02T10:00"),
            event(2, "2026-04-01T08:00", "2026-04-01T10:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(LocalDate.of(2026, 4, 1), report.busiestDay?.date)
    }

    @Test
    fun earliestAndLatestIgnoreCrossMidnightBlocks() {
        val report = report(
            2026,
            event(1, "2026-05-01T06:15", "2026-05-01T07:00"),
            event(2, "2026-05-02T21:00", "2026-05-02T22:30"),
            // 跨天记录不参与「最晚结束」，避免把 01:00 当成当天最晚。
            event(3, "2026-05-03T23:00", "2026-05-04T01:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(LocalTime.of(6, 15), report.earliestStart)
        assertEquals(LocalTime.of(22, 30), report.latestEnd)
    }

    @Test
    fun hourBucketsUseClippedStartOfYear() {
        val report = report(
            2026,
            event(1, "2025-12-31T23:30", "2026-01-01T02:00"),
            event(2, "2026-06-01T21:30", "2026-06-01T22:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(1, report.hourStarts[0])
        assertEquals(1, report.hourStarts[21])
    }

    @Test
    fun longestEventTiePicksEarlierStart() {
        val report = report(
            2026,
            event(2, "2026-02-02T08:00", "2026-02-02T10:00"),
            event(1, "2026-02-01T08:00", "2026-02-01T10:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(1L, report.longestEvent?.eventId)
        assertEquals(2 * HOUR, report.longestEvent?.durationMillis)
    }

    @Test
    fun rootsAndPreviousTotalComeFromStatsBuilder() {
        val report = report(
            2025,
            event(1, "2024-06-01T08:00", "2024-06-01T13:00"),
            event(2, "2025-06-01T08:00", "2025-06-01T10:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(2 * HOUR, report.totalMillis)
        assertEquals(5 * HOUR, report.previousTotalMillis)
        assertEquals(-0.6f, report.totalDeltaRatio!!, 0.001f)
        val work = report.roots.single()
        assertEquals("工作", work.name)
        assertEquals(5 * HOUR, work.previousMillis)
        assertEquals(-0.6f, work.deltaRatio!!, 0.001f)
        val change = report.biggestChange!!
        assertEquals("工作", change.name)
        assertEquals(2 * HOUR, change.currentMillis)
        assertEquals(5 * HOUR, change.previousMillis)
    }

    @Test
    fun biggestChangeSeesCategoryThatDisappeared() {
        val report = report(
            2025,
            event(1, "2024-06-01T08:00", "2024-06-01T11:00", categoryId = 4L, path = "生活"),
            event(2, "2025-06-01T08:00", "2025-06-01T09:00"),
            now = at("2026-11-01T12:00"),
        )
        val change = report.biggestChange!!
        assertEquals("生活", change.name)
        assertEquals(0L, change.currentMillis)
        assertEquals(3 * HOUR, change.previousMillis)
    }

    @Test
    fun busiestHourCountsStartHoursAndTiePicksEarlier() {
        val report = report(
            2026,
            event(1, "2026-03-01T21:00", "2026-03-01T22:00"),
            event(2, "2026-03-02T21:30", "2026-03-02T22:00"),
            event(3, "2026-03-03T09:00", "2026-03-03T10:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(21, report.busiestHour)

        val tied = report(
            2026,
            event(1, "2026-04-01T21:00", "2026-04-01T22:00"),
            event(2, "2026-04-02T09:00", "2026-04-02T10:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(9, tied.busiestHour)
    }

    @Test
    fun firstAndLastEventsAndLatestStart() {
        val report = report(
            2026,
            event(1, "2026-01-05T07:00", "2026-01-05T08:00"),
            event(2, "2026-11-20T22:30", "2026-11-20T23:30"),
            now = at("2026-12-01T12:00"),
        )
        assertEquals(1L, report.firstEvent?.eventId)
        assertEquals(2L, report.lastEvent?.eventId)
        assertEquals(LocalTime.of(22, 30), report.latestStart)
    }

    @Test
    fun nightOwlAndEarlyBirdCountsUseStartHour() {
        val report = report(
            2026,
            event(1, "2026-03-02T23:00", "2026-03-02T23:30"),
            event(2, "2026-03-03T07:00", "2026-03-03T08:00"),
            event(3, "2026-03-04T12:00", "2026-03-04T13:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(1, report.nightOwlCount)
        assertEquals(1, report.earlyBirdCount)
    }

    @Test
    fun weekendMillisCountsSaturdayAndSunday() {
        val report = report(
            2026,
            event(1, "2026-03-07T12:00", "2026-03-07T14:00"),
            event(2, "2026-03-08T12:00", "2026-03-08T14:00"),
            event(3, "2026-03-09T12:00", "2026-03-09T13:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(4 * HOUR, report.weekendMillis)
        assertEquals(0.8f, report.weekendRatio!!, 0.001f)
    }

    @Test
    fun categoryCountAndBusiestDayEventCount() {
        val report = report(
            2026,
            event(1, "2026-03-01T08:00", "2026-03-01T09:00"),
            event(2, "2026-03-01T10:00", "2026-03-01T11:00", categoryId = 4L, path = "生活"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(2, report.categoryCount)
        assertEquals(2, report.busiestDay?.eventCount)
    }

    @Test
    fun crossYearContinuationDoesNotCountAsNightRecord() {
        val report = report(
            2026,
            event(1, "2025-12-31T23:00", "2026-01-01T02:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(0, report.nightOwlCount)
        // 「最常开始的时段」仍按年内裁剪后的 0 点计入。
        assertEquals(1, report.hourStarts[0])
    }

    @Test
    fun crossYearEventIsLastRecordOfItsStartYear() {
        val report = report(
            2025,
            event(1, "2025-06-01T09:00", "2025-06-01T10:00"),
            event(2, "2025-12-31T23:00", "2026-01-01T02:00"),
            now = at("2026-11-01T12:00"),
        )
        assertEquals(2L, report.lastEvent?.eventId)
        assertEquals(1L, report.firstEvent?.eventId)
    }

    @Test
    fun leapDayCountsInFebruary() {
        val report = report(
            2024,
            event(1, "2024-02-29T02:00", "2024-02-29T04:00"),
            now = at("2025-01-01T00:00"),
        )
        assertEquals(2 * HOUR, report.months[1])
        assertEquals(2 * HOUR, report.totalMillis)
        assertEquals(1, report.activeDays)
    }
}
