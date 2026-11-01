package com.example.timelog.domain

import com.example.timelog.domain.model.TimelineEvent
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** 统计页可选的时间范围。 */
enum class StatsPeriod { TODAY, WEEK, MONTH, ALL }

/** 某个一级分类（分类路径的根）在一段时间内的累计用时。 */
data class CategoryDuration(
    val rootName: String,
    val colorHex: Long,
    val millis: Long,
)

/** 某一天（本地时区的自然日）的累计用时。 */
data class DayDuration(
    val date: LocalDate,
    val millis: Long,
)

/**
 * 统计结果。
 *
 * - [totalMillis] 是本期所有事件被裁剪后的时长之和；事件之间若有重叠，会各算各的，
 *   因此总时长可能大于本期的墙钟时长（与逐条事件口径保持一致，方便核对分类明细）。
 * - [eventCount] 是与本期有交集的事件条数（完全落在范围外的事件不计）。
 * - [categories] 按一级分类（categoryPath 里第一个「 / 」之前的部分）汇总，用时降序；
 *   同一分类下取按开始时间最早的那条事件的颜色作为代表色。
 * - [last7Days] 固定为「今天往前 7 天」，与所选 [period] 无关，用于迷你柱状图的背景参照；
 *   没有记录的日子也会保留（millis = 0），并且只统计到 [now] 为止。
 */
data class StatsSummary(
    val period: StatsPeriod,
    val totalMillis: Long,
    val eventCount: Int,
    val categories: List<CategoryDuration>,
    val last7Days: List<DayDuration>,
)

object StatsBuilder {
    const val ROOT_SEPARATOR: String = " / "
    const val LAST_DAY_COUNT: Int = 7

    /**
     * 把事件区间裁剪到 [period] 对应的范围后汇总：
     * TODAY = [今天 00:00, now]，WEEK = [本周一 00:00, now]，
     * MONTH = [本月 1 日 00:00, now]，ALL = [最早事件开始, now]。
     */
    fun build(
        events: List<TimelineEvent>,
        period: StatsPeriod,
        now: Long,
        zone: ZoneId,
    ): StatsSummary {
        val today = toDate(now, zone)
        val periodStart = periodStart(period, events, today, now, zone)
        // 按开始时间 + id 排序，保证同一份数据每次得到相同的分类代表色。
        val clipped = events
            .sortedWith(compareBy({ it.event.startAt }, { it.event.id }))
            .mapNotNull { item ->
                val start = maxOf(item.event.startAt, periodStart)
                val end = minOf(item.event.startAt + item.event.durationMillis, now)
                if (end <= start) null else ClippedEvent(item, start, end)
            }

        return StatsSummary(
            period = period,
            totalMillis = clipped.sumOf { it.end - it.start },
            eventCount = clipped.size,
            categories = categoriesOf(clipped),
            last7Days = last7DaysOf(events, today, now, zone),
        )
    }

    /** 一级分类名：categoryPath 里第一个「 / 」之前的部分；没有分隔符时就是整条路径。 */
    internal fun rootNameOf(categoryPath: String): String {
        val index = categoryPath.indexOf(ROOT_SEPARATOR)
        return if (index < 0) categoryPath else categoryPath.substring(0, index)
    }

    private fun categoriesOf(clipped: List<ClippedEvent>): List<CategoryDuration> {
        val byRoot = LinkedHashMap<String, RootAccumulator>()
        clipped.forEach { item ->
            val root = rootNameOf(item.item.categoryPath)
            val accumulator = byRoot.getOrPut(root) {
                RootAccumulator(colorHex = item.item.categoryColor)
            }
            accumulator.millis += item.end - item.start
        }
        return byRoot
            .map { (rootName, accumulator) ->
                CategoryDuration(
                    rootName = rootName,
                    colorHex = accumulator.colorHex,
                    millis = accumulator.millis,
                )
            }
            .sortedWith(
                compareByDescending<CategoryDuration> { it.millis }
                    .thenBy { it.rootName },
            )
    }

    private fun last7DaysOf(
        events: List<TimelineEvent>,
        today: LocalDate,
        now: Long,
        zone: ZoneId,
    ): List<DayDuration> = (LAST_DAY_COUNT - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        val dayStart = startOfDay(date, zone)
        val dayEnd = minOf(startOfDay(date.plusDays(1), zone), now)
        val millis = if (dayEnd <= dayStart) {
            0L
        } else {
            events.sumOf { item ->
                val start = maxOf(item.event.startAt, dayStart)
                val end = minOf(item.event.startAt + item.event.durationMillis, dayEnd)
                if (end > start) end - start else 0L
            }
        }
        DayDuration(date = date, millis = millis)
    }

    private fun periodStart(
        period: StatsPeriod,
        events: List<TimelineEvent>,
        today: LocalDate,
        now: Long,
        zone: ZoneId,
    ): Long = when (period) {
        StatsPeriod.TODAY -> startOfDay(today, zone)
        StatsPeriod.WEEK -> startOfDay(
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            zone,
        )
        StatsPeriod.MONTH -> startOfDay(today.withDayOfMonth(1), zone)
        StatsPeriod.ALL -> events.minOfOrNull { it.event.startAt } ?: now
    }

    private fun startOfDay(date: LocalDate, zone: ZoneId): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    private fun toDate(epochMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    private data class ClippedEvent(
        val item: TimelineEvent,
        val start: Long,
        val end: Long,
    )

    private class RootAccumulator(
        val colorHex: Long,
        var millis: Long = 0L,
    )
}
