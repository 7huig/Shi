package io.github.huig7.shi.domain

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

/** 统计窗口边界、自建范围与前后翻页（含 DST 下的本地日历语义）。 */
class StatsWindowsTest : StatisticsTestFixtures() {

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
}
