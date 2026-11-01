package com.example.timelog.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateTimeFormat {
    private val zh: Locale = Locale.SIMPLIFIED_CHINESE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", zh)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", zh)
    private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", zh)
    private val shortMonthDayFormatter = DateTimeFormatter.ofPattern("M月d日", zh)
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy年", zh)

    fun zone(): ZoneId = ZoneId.systemDefault()

    fun toLocalDateTime(epochMillis: Long): LocalDateTime =
        Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalDateTime()

    fun toEpochMillis(date: LocalDate, time: LocalTime): Long =
        LocalDateTime.of(date, time).atZone(zone()).toInstant().toEpochMilli()

    fun time(epochMillis: Long): String = toLocalDateTime(epochMillis).format(timeFormatter)

    fun clock(time: LocalTime): String = time.format(timeFormatter)

    fun dateWithWeekday(date: LocalDate): String = date.format(monthDayFormatter)

    fun monthDay(date: LocalDate): String = date.format(shortMonthDayFormatter)

    fun sectionTitle(date: LocalDate, today: LocalDate = LocalDate.now(zone())): String =
        when (date) {
            today -> "今天"
            today.minusDays(1) -> "昨天"
            else -> if (date.year == today.year) {
                date.format(monthDayFormatter)
            } else {
                date.format(fullDateFormatter)
            }
        }

    fun sectionSubtitle(date: LocalDate, today: LocalDate = LocalDate.now(zone())): String =
        when (date) {
            today, today.minusDays(1) -> date.format(monthDayFormatter)
            else -> date.format(yearFormatter) + " · " + date.dayOfWeek.getDisplayName(
                TextStyle.FULL,
                zh,
            )
        }

    fun fullDateTime(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(fullDateFormatter) + " " + time(epochMillis)

    fun datePart(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(shortMonthDayFormatter)

    fun dateWithWeekday(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(monthDayFormatter)

    fun isoDate(epochMillis: Long): String =
        toLocalDateTime(epochMillis).toLocalDate().toString()

    fun startOfDayUtcMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    fun dateFromUtcMillis(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()

    /** 事件块的时间范围，用于编辑 / 删除确认。 */
    fun rangeText(startAt: Long, endAt: Long): String {
        val start = toLocalDateTime(startAt)
        val end = toLocalDateTime(endAt)
        return if (start.toLocalDate() == end.toLocalDate()) {
            clock(start.toLocalTime()) + "–" + clock(end.toLocalTime())
        } else {
            monthDay(start.toLocalDate()) + " " + clock(start.toLocalTime()) +
                "–" + monthDay(end.toLocalDate()) + " " + clock(end.toLocalTime())
        }
    }

    fun durationText(startAt: Long, endAt: Long): String =
        durationText((endAt - startAt).coerceAtLeast(60_000L))

    fun durationText(millis: Long): String {
        val totalMinutes = (millis / 60_000L).coerceAtLeast(1L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            hours == 0L -> "${minutes} 分钟"
            minutes == 0L -> "${hours} 小时"
            else -> "${hours} 小时 ${minutes} 分"
        }
    }
}
