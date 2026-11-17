package io.github.huig7.shi.domain

import io.github.huig7.shi.ui.stats.statsLabel
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/** 范围导航条文案：今天前缀、跨年补齐年份、周 / 自定义区间使用短横线。 */
class StatsLabelTest : StatisticsTestFixtures() {

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
}
