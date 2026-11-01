package com.example.timelog.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 时间流里连续的一块。events 为空时表示「未记录」的时间，由「未分类」占位块自动填充。
 *
 * 设计约定：
 * - 每天从 00:00 开始、到今天「现在」或次日 00:00 结束，中间不允许出现空洞；
 * - 相邻（首尾相接）事件是两个块，交界处显示时间；
 * - 重叠事件合并成同一个块，块内逐条列出。
 */
data class TimelineSegment(
    val startAt: Long,
    val endAt: Long,
    val events: List<TimelineEvent>,
) {
    val isUnrecorded: Boolean get() = events.isEmpty()
}

data class TimelineDay(
    val date: LocalDate,
    val isToday: Boolean,
    val segments: List<TimelineSegment>,
)

object TimelineBuilder {

    fun build(
        events: List<TimelineEvent>,
        today: LocalDate,
        now: Long,
        zone: ZoneId,
    ): List<TimelineDay> {
        if (events.isEmpty()) return emptyList()

        val byDate = sortedMapOf<LocalDate, MutableList<TimelineEvent>>()
        events.forEach { item ->
            val startAt = item.event.startAt
            val endAt = item.event.startAt + item.event.durationMillis
            val startDate = dateOf(startAt, zone)
            val endDate = dateOf(endAt - 1, zone)
            var date = startDate
            while (!date.isAfter(endDate)) {
                byDate.getOrPut(date) { mutableListOf() } += item
                date = date.plusDays(1)
            }
        }

        return byDate.map { (date, dayEvents) ->
            buildDay(
                date = date,
                events = dayEvents,
                today = today,
                now = now,
                zone = zone,
            )
        }
    }

    private fun buildDay(
        date: LocalDate,
        events: List<TimelineEvent>,
        today: LocalDate,
        now: Long,
        zone: ZoneId,
    ): TimelineDay {
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val nextStart = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val clipped = events.mapNotNull { item ->
            val start = maxOf(item.event.startAt, dayStart)
            val end = minOf(item.event.startAt + item.event.durationMillis, nextStart)
            if (end <= start) null else ClippedEvent(start, end, item)
        }.sortedWith(compareBy({ it.start }, { it.end }, { it.item.event.id }))

        val clusters = buildClusters(clipped)
        val lastEnd = clusters.lastOrNull()?.end ?: dayStart
        val endLimit = when {
            date.isAfter(today) -> lastEnd
            date.isBefore(today) -> nextStart
            else -> maxOf(now, lastEnd).coerceAtMost(nextStart)
        }

        val segments = mutableListOf<TimelineSegment>()
        var cursor = dayStart
        clusters.forEach { cluster ->
            val start = maxOf(cluster.start, cursor)
            if (start > cursor) {
                segments += TimelineSegment(cursor, start, emptyList())
            }
            if (cluster.end > start) {
                segments += TimelineSegment(start, cluster.end, cluster.events)
            }
            cursor = cluster.end
        }
        if (cursor < endLimit) {
            segments += TimelineSegment(cursor, endLimit, emptyList())
        }

        return TimelineDay(date = date, isToday = date == today, segments = segments)
    }

    private fun buildClusters(events: List<ClippedEvent>): List<Cluster> {
        val clusters = mutableListOf<Cluster>()
        events.forEach { clipped ->
            val last = clusters.lastOrNull()
            if (last != null && clipped.start < last.end) {
                if (clipped.end > last.end) last.end = clipped.end
                last.events += clipped.item
            } else {
                clusters += Cluster(
                    start = clipped.start,
                    end = clipped.end,
                    events = mutableListOf(clipped.item),
                )
            }
        }
        return clusters
    }

    private fun dateOf(epochMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    private data class ClippedEvent(val start: Long, val end: Long, val item: TimelineEvent)

    private data class Cluster(
        var start: Long,
        var end: Long,
        val events: MutableList<TimelineEvent>,
    )
}
