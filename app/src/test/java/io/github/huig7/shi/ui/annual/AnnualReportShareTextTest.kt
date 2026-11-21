package io.github.huig7.shi.ui.annual

import io.github.huig7.shi.domain.StatisticsTestFixtures
import io.github.huig7.shi.domain.annual.AnnualReportBuilder
import io.github.huig7.shi.domain.model.TimelineEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 文本分享只引用报告里的数字，并按进行中 / 同比有无做裁剪。 */
class AnnualReportShareTextTest : StatisticsTestFixtures() {

    private fun text(
        year: Int,
        now: String,
        vararg events: TimelineEvent,
    ): String = buildAnnualReportText(
        AnnualReportBuilder.build(events.toList(), tree(), year, at(now), zone),
    )

    @Test
    fun fullYearTextHasTotalsAndTopCategories() {
        val share = text(
            2025,
            "2026-11-01T12:00",
            event(1, "2025-01-10T08:00", "2025-01-10T10:00"),
            event(2, "2025-06-10T08:00", "2025-06-10T12:00", categoryId = 4L, path = "生活"),
        )
        assertTrue(share.contains("時 · 2025 年度报告"))
        assertTrue(share.contains("记录 6 小时"))
        assertTrue(share.contains("年度称号："))
        assertTrue(share.contains("年度关键词："))
        assertTrue(share.contains("够看约 3 部电影"))
        assertTrue(share.contains("生活"))
        assertTrue(share.contains("最忙的是 6 月"))
        assertFalse(share.contains("截至"))
        assertFalse(share.contains("去年同期"))
    }

    @Test
    fun partialYearMentionsThroughDate() {
        val share = text(
            2026,
            "2026-11-01T12:00",
            event(1, "2026-11-01T10:00", "2026-11-01T12:00"),
        )
        assertTrue(share.contains("截至 11月1日"))
    }

    @Test
    fun previousYearAddsComparison() {
        val share = text(
            2025,
            "2026-11-01T12:00",
            event(1, "2024-06-01T08:00", "2024-06-01T13:00"),
            event(2, "2025-06-01T08:00", "2025-06-01T10:00"),
        )
        assertTrue(share.contains("比去年同期少 60%"))
    }

    @Test
    fun deltaTextSwitchesToMultiplesForLargeGrowth() {
        assertEquals("是去年的 18 倍", annualDeltaText(17.17f))
        assertEquals("比去年同期多 18%", annualDeltaText(0.18f))
        assertEquals("比去年同期少 60%", annualDeltaText(-0.6f))
        assertEquals("和去年差不多", annualDeltaText(0.01f))
    }
}
