package io.github.huig7.shi.util

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
    private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", zh)
    private val shortMonthDayFormatter = DateTimeFormatter.ofPattern("M月d日", zh)

    fun zone(): ZoneId = ZoneId.systemDefault()

    fun toLocalDateTime(epochMillis: Long): LocalDateTime =
        Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalDateTime()

    fun toEpochMillis(date: LocalDate, time: LocalTime): Long =
        LocalDateTime.of(date, time).atZone(zone()).toInstant().toEpochMilli()

    fun time(epochMillis: Long): String = toLocalDateTime(epochMillis).format(timeFormatter)

    /**
     * 间隔待办的「下一次」：今天只给时间，明天加「明天」，更远的日子带月日。
     * 跨天也能看出是哪一天，而不是只显示一个可能被误读成今天的时间。
     */
    fun relativeDayTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        val target = toLocalDateTime(epochMillis)
        val today = toLocalDateTime(now).toLocalDate()
        val clock = target.format(timeFormatter)
        return when (target.toLocalDate()) {
            today -> clock
            today.plusDays(1) -> "明天 $clock"
            else -> monthDay(target.toLocalDate()) + " " + clock
        }
    }

    fun clock(time: LocalTime): String = time.format(timeFormatter)

    fun dateWithWeekday(date: LocalDate): String = date.format(monthDayFormatter)

    fun monthDay(date: LocalDate): String = date.format(shortMonthDayFormatter)

    /** 主界面日期标题：今天/昨天保留相对称呼，但仍带日期，和统计页的日期语法保持一致。 */
    fun sectionTitle(date: LocalDate, today: LocalDate = LocalDate.now(zone())): String =
        when (date) {
            today -> "今天 · " + monthDay(date)
            today.minusDays(1) -> "昨天 · " + monthDay(date)
            else -> monthDay(date)
        }

    /** 主界面日期副标题：只显示「周X」小字，今天/昨天也不再重复日期。 */
    fun sectionSubtitle(date: LocalDate): String = weekdayLabel(date)

    /** 固定输出「周日」「周一」…，不依赖不同 JDK 的 CLDR 短星期文本。 */
    private fun weekdayLabel(date: LocalDate): String =
        "周" + date.dayOfWeek.getDisplayName(TextStyle.NARROW, zh)

    fun datePart(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(shortMonthDayFormatter)

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
        // 四舍五入到分钟：统计裁剪到「现在」时可能只差几秒，30 分钟的块不应该显示成 29 分钟。
        val totalMinutes = ((millis + 30_000L) / 60_000L).coerceAtLeast(1L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            hours == 0L -> "${minutes} 分钟"
            minutes == 0L -> "${hours} 小时"
            else -> "${hours} 小时 ${minutes} 分"
        }
    }
}
