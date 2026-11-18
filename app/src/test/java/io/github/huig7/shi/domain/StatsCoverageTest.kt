package io.github.huig7.shi.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** 覆盖率：重叠记录只算一次墙钟时间，分母是窗口内已经过去的时间。 */
class StatsCoverageTest : StatisticsTestFixtures() {

    @Test
    fun coverageCountsOverlapOnce() {
        val summary = build(
            event(1, "2026-11-01T08:00", "2026-11-01T10:00"),
            event(2, "2026-11-01T09:00", "2026-11-01T11:00"),
        )
        assertEquals(4 * HOUR, summary.totalMillis)
        assertEquals(3 * HOUR, summary.coveredMillis)
        assertEquals(12 * HOUR, summary.elapsedMillis)
    }

    @Test
    fun pastWindowUsesFullWindowAsElapsed() {
        val summary = build(
            event(1, "2026-10-31T08:00", "2026-10-31T10:00"),
            window = StatsWindows.of(StatsMode.DAY, today.minusDays(1), zone),
        )
        assertEquals(2 * HOUR, summary.coveredMillis)
        assertEquals(24 * HOUR, summary.elapsedMillis)
    }

    @Test
    fun futureWindowHasZeroElapsed() {
        val summary = build(
            window = StatsWindows.of(StatsMode.DAY, today.plusDays(1), zone),
        )
        assertEquals(0L, summary.coveredMillis)
        assertEquals(0L, summary.elapsedMillis)
    }
}
