package io.github.huig7.shi.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 与上一个窗口的等时长环比。 */
class StatsDeltaTest : StatisticsTestFixtures() {

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
}
