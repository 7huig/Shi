package io.github.huig7.shi.ui.annual

import io.github.huig7.shi.domain.HOUR
import io.github.huig7.shi.domain.StatisticsTestFixtures
import io.github.huig7.shi.domain.annual.AnnualReportBuilder
import io.github.huig7.shi.domain.model.TimelineEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 年度称号、关键词与时间换算的规则。 */
class AnnualReportNarrativeTest : StatisticsTestFixtures() {

    private fun report(
        year: Int,
        vararg events: TimelineEvent,
        now: String = "2026-11-01T12:00",
    ) = AnnualReportBuilder.build(events.toList(), tree(), year, at(now), zone)

    @Test
    fun nightRecordsBecomeNightOwl() {
        val persona = annualPersona(
            report(
                2026,
                event(1, "2026-03-02T23:00", "2026-03-02T23:30"),
                event(2, "2026-03-03T23:30", "2026-03-04T00:30"),
                event(3, "2026-03-04T12:00", "2026-03-04T13:00"),
            ),
        )
        assertEquals("夜行记录者", persona.title)
    }

    @Test
    fun earlyRecordsBecomeMorningPersona() {
        val persona = annualPersona(
            report(
                2026,
                event(1, "2026-03-02T07:00", "2026-03-02T08:00"),
                event(2, "2026-03-03T07:00", "2026-03-03T08:00"),
                event(3, "2026-03-04T07:00", "2026-03-04T08:00"),
                event(4, "2026-03-05T07:00", "2026-03-05T08:00"),
                event(5, "2026-03-06T12:00", "2026-03-06T13:00"),
            ),
        )
        assertEquals("清晨掌舵人", persona.title)
    }

    @Test
    fun weekendRecordsBecomeWeekendPersona() {
        val persona = annualPersona(
            report(
                2026,
                event(1, "2026-03-07T12:00", "2026-03-07T14:00"),
                event(2, "2026-03-08T12:00", "2026-03-08T14:00"),
                event(3, "2026-03-09T12:00", "2026-03-09T13:00", categoryId = 4L, path = "生活"),
            ),
        )
        assertEquals("周末生活家", persona.title)
    }

    @Test
    fun dominantCategoryBecomesLongtermist() {
        val persona = annualPersona(
            report(
                2026,
                event(1, "2026-03-02T12:00", "2026-03-02T13:00"),
                event(2, "2026-03-03T12:00", "2026-03-03T13:00"),
                event(3, "2026-03-04T12:00", "2026-03-04T13:00"),
                event(4, "2026-03-05T12:00", "2026-03-05T13:00"),
                event(5, "2026-03-06T12:00", "2026-03-06T13:00"),
                event(6, "2026-03-09T12:00", "2026-03-09T13:00", categoryId = 4L, path = "生活"),
            ),
        )
        assertEquals("工作的长期主义者", persona.title)
    }

    @Test
    fun timeEquivalentUsesMoviesAndWorkdays() {
        val text = annualTimeEquivalent(
            report(
                2026,
                event(1, "2026-03-02T08:00", "2026-03-02T18:00"),
                event(2, "2026-03-03T08:00", "2026-03-03T18:00"),
            ),
        )
        assertTrue(text.contains("10 部电影"))
        assertTrue(text.contains("2 个八小时班"))
    }

    @Test
    fun keywordJoinsTopCategories() {
        val report = report(
            2026,
            event(1, "2026-03-02T08:00", "2026-03-02T11:00"),
            event(2, "2026-03-03T08:00", "2026-03-03T09:00", categoryId = 4L, path = "生活"),
        )
        assertEquals("工作 · 生活", annualKeyword(report))
    }
}
