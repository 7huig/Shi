package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.domain.model.TimelineBuilder
import io.github.huig7.shi.domain.model.TimelineDay
import io.github.huig7.shi.domain.model.TimelineEvent
import io.github.huig7.shi.domain.model.TimelineSegment
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineBuilderTest {

    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val today: LocalDate = LocalDate.of(2026, 11, 1)

    @Test
    fun emptyEventsProduceNoDays() {
        val days = TimelineBuilder.build(emptyList(), today, at(LocalDateTime.of(today, java.time.LocalTime.NOON)), zone)
        assertTrue(days.isEmpty())
    }

    @Test
    fun fillsWholePastDayAndSortsAscending() {
        val days = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00"),
            event(2, "2026-10-31T11:00", "2026-10-31T12:00"),
        )
        assertEquals(1, days.size)
        val segments = days.single().segments
        assertEquals(5, segments.size)
        assertTrue(segments[0].isUnrecorded)
        assertEquals(dayStart("2026-10-31"), segments[0].startAt)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(9, 0))), segments[0].endAt)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(9, 0))), segments[1].startAt)
        assertEquals(1L, segments[1].events.single().event.id)
        assertEquals(dayStart("2026-11-01"), segments.last().endAt)
        assertContiguous(segments)
    }

    @Test
    fun mergesOverlappingEventsIntoOneBlock() {
        val days = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:30"),
            event(2, "2026-10-31T10:00", "2026-10-31T11:00"),
        )
        val segments = days.single().segments
        assertEquals(3, segments.size)
        assertEquals(2, segments[1].events.size)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(9, 0))), segments[1].startAt)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(11, 0))), segments[1].endAt)
        assertContiguous(segments)
    }

    @Test
    fun keepsTouchingEventsSeparate() {
        val days = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00"),
            event(2, "2026-10-31T10:00", "2026-10-31T11:00"),
        )
        val segments = days.single().segments
        assertEquals(4, segments.size)
        assertEquals(1, segments[1].events.size)
        assertEquals(1, segments[2].events.size)
        assertEquals(segments[1].endAt, segments[2].startAt)
        assertContiguous(segments)
    }

    @Test
    fun todayEndsAtNow() {
        val days = build(
            event(1, "2026-11-01T08:00", "2026-11-01T09:00"),
            now = LocalDateTime.of(today, java.time.LocalTime.of(12, 30)),
        )
        val segments = days.single().segments
        assertEquals(at(LocalDateTime.of(today, java.time.LocalTime.of(12, 30))), segments.last().endAt)
        assertTrue(segments.last().isUnrecorded)
        assertContiguous(segments)
    }

    @Test
    fun splitsEventAcrossMidnight() {
        val days = build(
            event(1, "2026-10-31T23:00", "2026-11-01T01:00"),
            now = LocalDateTime.of(today, java.time.LocalTime.of(12, 0)),
        )
        assertEquals(2, days.size)
        val first = days[0]
        val second = days[1]
        assertEquals(LocalDate.of(2026, 10, 31), first.date)
        assertEquals(dayStart("2026-11-01"), first.segments.last().endAt)
        assertEquals(at(LocalDateTime.of(today, java.time.LocalTime.of(1, 0))), second.segments.first().endAt)
        assertEquals(1L, second.segments.first().events.single().event.id)
        assertTrue(second.isToday)
        days.forEach { assertContiguous(it.segments) }
    }

    @Test
    fun daySegmentsAreAscending() {
        val days = build(
            event(2, "2026-10-30T18:00", "2026-10-30T19:00"),
            event(1, "2026-10-30T08:00", "2026-10-30T09:00"),
        )
        val segments = days.single().segments
        segments.zipWithNext().forEach { (a, b) -> assertTrue(a.startAt <= b.startAt) }
    }

    private fun build(vararg events: TimelineEvent, now: LocalDateTime = LocalDateTime.of(today, java.time.LocalTime.of(20, 0))): List<TimelineDay> =
        TimelineBuilder.build(events.toList(), today, at(now), zone)

    private fun event(id: Long, start: String, end: String): TimelineEvent = TimelineEvent(
        event = Event(
            id = id,
            note = null,
            startAt = at(LocalDateTime.parse(start)),
            endAt = at(LocalDateTime.parse(end)),
            categoryId = 1L,
            createdAt = 0L,
            updatedAt = 0L,
        ),
        categoryPath = "工作 / 执行",
        categoryColor = 0xFF4C7BE8,
    )

    private fun at(dateTime: LocalDateTime): Long =
        dateTime.atZone(zone).toInstant().toEpochMilli()

    private fun dayStart(date: String): Long =
        LocalDate.parse(date).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun assertContiguous(segments: List<TimelineSegment>) {
        segments.zipWithNext().forEach { (a, b) -> assertEquals(a.endAt, b.startAt) }
        assertTrue(segments.all { it.endAt > it.startAt })
    }
}
