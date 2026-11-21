package io.github.huig7.shi.domain.annual

import io.github.huig7.shi.domain.CategoryUsage
import io.github.huig7.shi.domain.ClippedEvent
import io.github.huig7.shi.domain.StatsBuilder
import io.github.huig7.shi.domain.StatsMode
import io.github.huig7.shi.domain.StatsWindows
import io.github.huig7.shi.domain.clipTo
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.TimelineEvent
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs

/**
 * 把一年的事件聚合成 [AnnualReport]。
 *
 * 纯函数：所有时间相关输入（[year]、[now]、[zone]）都由参数传入。
 * 总量、覆盖率、分类 Top 与去年同期复用 [StatsBuilder] 的口径；
 * 月份、日期、小时分桶与极值在一次裁剪遍历里完成。
 */
object AnnualReportBuilder {

    private const val MONTHS_IN_YEAR = 12
    private const val HOURS_IN_DAY = 24
    private val SEASON_NAMES = listOf("春", "夏", "秋", "冬")

    fun build(
        events: List<TimelineEvent>,
        tree: CategoryTree,
        year: Int,
        now: Long,
        zone: ZoneId,
    ): AnnualReport {
        val window = StatsWindows.of(StatsMode.YEAR, LocalDate.of(year, 1, 1), zone)
        val summary = StatsBuilder.build(events, tree, window, now, zone)
        // 去年同窗口的分类榜：只靠 summary.roots 的 previousMillis 会漏掉「今年完全没有记录」的分类。
        val previousWindow = StatsBuilder.previousWindowOf(window, now, zone)
        val previousSummary = StatsBuilder.build(events, tree, previousWindow, now, zone)
        val until = minOf(window.endAt, now)
        val clipped = clipTo(events, window.startAt, until)

        val monthMillis = LongArray(MONTHS_IN_YEAR)
        val dayMillis = HashMap<LocalDate, Long>()
        val dayEventCounts = HashMap<LocalDate, Int>()
        val hourStarts = IntArray(HOURS_IN_DAY)
        val categoryIds = HashSet<Long>()
        var longest: ClippedEvent? = null
        var firstEvent: ClippedEvent? = null
        var lastEvent: ClippedEvent? = null
        var earliestStart: LocalTime? = null
        var latestStart: LocalTime? = null
        var latestEnd: LocalTime? = null
        var nightOwlCount = 0
        var earlyBirdCount = 0

        clipped.forEach { item ->
            // 按本地日切分：同一次遍历同时得到月桶和日桶，跨天 / 跨月记录不会丢时长。
            var cursor = item.start
            while (cursor < item.end) {
                val date = StatsWindows.dateOf(cursor, zone)
                val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val segmentEnd = minOf(item.end, dayEnd)
                val duration = segmentEnd - cursor
                monthMillis[date.monthValue - 1] += duration
                dayMillis[date] = (dayMillis[date] ?: 0L) + duration
                dayEventCounts[date] = (dayEventCounts[date] ?: 0) + 1
                cursor = segmentEnd
            }

            val realStartAt = item.event.event.startAt
            val realStart = Instant.ofEpochMilli(realStartAt).atZone(zone)
            if (realStart.year == year) {
                val startTime = realStart.toLocalTime()
                val currentEarliest = earliestStart
                if (currentEarliest == null || startTime.isBefore(currentEarliest)) {
                    earliestStart = startTime
                }
                val currentLatest = latestStart
                if (currentLatest == null || startTime.isAfter(currentLatest)) {
                    latestStart = startTime
                }
                val currentFirst = firstEvent
                if (currentFirst == null || realStartAt < currentFirst.event.event.startAt) {
                    firstEvent = item
                }
                val currentLast = lastEvent
                if (currentLast == null || realStartAt > currentLast.event.event.startAt) {
                    lastEvent = item
                }
            }

            val realEndAt = realStartAt + item.event.event.durationMillis
            val realEnd = Instant.ofEpochMilli(realEndAt).atZone(zone)
            if (realEnd.year == year && realEnd.toLocalDate() == realStart.toLocalDate()) {
                val endTime = realEnd.toLocalTime()
                val current = latestEnd
                if (current == null || endTime.isAfter(current)) latestEnd = endTime
            }

            // 「最常开始的时段」按年内裁剪后的开始时刻计数：跨年记录算作 1 月 1 日 0 点。
            val startHour = Instant.ofEpochMilli(item.start).atZone(zone).hour
            hourStarts[startHour]++
            // 夜猫子 / 早起统计按真实开始时刻：跨年延续记录不应算作今年的深夜记录。
            if (realStart.year == year) {
                when {
                    realStart.hour >= 22 || realStart.hour < 5 -> nightOwlCount++
                    realStart.hour in 5..8 -> earlyBirdCount++
                }
            }
            categoryIds += item.event.event.categoryId

            val duration = item.end - item.start
            val current = longest
            if (current == null ||
                duration > current.end - current.start ||
                (duration == current.end - current.start &&
                    item.event.event.startAt < current.event.event.startAt)
            ) {
                longest = item
            }
        }

        val activeDays = dayMillis.size
        val busiestDay = dayMillis.entries.maxWithOrNull(
            compareBy<Map.Entry<LocalDate, Long>> { it.value }.thenByDescending { it.key },
        )
        val weekendMillis = dayMillis.entries
            .filter { it.key.dayOfWeek == DayOfWeek.SATURDAY || it.key.dayOfWeek == DayOfWeek.SUNDAY }
            .sumOf { it.value }

        return AnnualReport(
            year = year,
            isPartial = now >= window.startAt && now < window.endAt,
            throughDate = if (until > window.startAt) {
                StatsWindows.dateOf(until - 1L, zone)
            } else {
                LocalDate.of(year, 1, 1)
            },
            totalMillis = summary.totalMillis,
            coveredMillis = summary.coveredMillis,
            elapsedMillis = summary.elapsedMillis,
            eventCount = clipped.size,
            activeDays = activeDays,
            avgPerActiveDayMillis = if (activeDays > 0) summary.totalMillis / activeDays else 0L,
            roots = summary.roots,
            months = monthMillis.toList(),
            hourStarts = hourStarts.toList(),
            longestEvent = longest?.let { notableOf(it, it.start, it.end) },
            earliestStart = earliestStart,
            latestEnd = latestEnd,
            longestStreakDays = longestStreak(dayMillis.keys),
            busiestDay = busiestDay?.let {
                DayUsage(it.key, it.value, dayEventCounts[it.key] ?: 0)
            },
            seasons = seasonsOf(monthMillis),
            previousTotalMillis = summary.previousTotalMillis,
            biggestChange = biggestChange(summary.roots, previousSummary.roots),
            firstEvent = firstEvent?.let {
                notableOf(it, it.event.event.startAt, it.event.event.endAt)
            },
            lastEvent = lastEvent?.let {
                notableOf(it, it.event.event.startAt, it.event.event.endAt)
            },
            latestStart = latestStart,
            weekendMillis = weekendMillis,
            nightOwlCount = nightOwlCount,
            earlyBirdCount = earlyBirdCount,
            categoryCount = categoryIds.size,
        )
    }

    private fun notableOf(item: ClippedEvent, startAt: Long, endAt: Long): NotableEvent =
        NotableEvent(
            eventId = item.event.event.id,
            categoryPath = item.event.categoryPath,
            colorHex = item.event.categoryColor,
            startAt = startAt,
            endAt = endAt,
            durationMillis = (endAt - startAt).coerceAtLeast(0L),
        )

    /** 同比变化绝对值最大的根分类；并列时按分类 ID 保证结果稳定。 */
    private fun biggestChange(
        current: List<CategoryUsage>,
        previous: List<CategoryUsage>,
    ): CategoryChange? {
        // 孤立分类共用 ORPHAN_CATEGORY_ID，不能按 ID 合并，直接排除。
        val currentById = current.filter { it.categoryId > 0L }.associateBy { it.categoryId }
        val previousById = previous.filter { it.categoryId > 0L }.associateBy { it.categoryId }
        val ids = currentById.keys + previousById.keys
        return ids.mapNotNull { id ->
            val currentUsage = currentById[id]
            val previousUsage = previousById[id]
            val name = currentUsage?.name ?: previousUsage?.name ?: return@mapNotNull null
            val currentMillis = currentUsage?.millis ?: 0L
            val previousMillis = previousUsage?.millis ?: 0L
            if (currentMillis == previousMillis) return@mapNotNull null
            CategoryChange(
                categoryId = id,
                name = name,
                colorHex = currentUsage?.colorHex ?: previousUsage?.colorHex ?: 0L,
                currentMillis = currentMillis,
                previousMillis = previousMillis,
            )
        }.maxWithOrNull(
            compareBy<CategoryChange> { abs(it.deltaMillis) }.thenBy { it.categoryId },
        )
    }

    /** 相邻日期的最长连续段；日期集合为空时为 0。 */
    private fun longestStreak(dates: Collection<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.sorted()
        var best = 1
        var run = 1
        for (index in 1 until sorted.size) {
            run = if (sorted[index] == sorted[index - 1].plusDays(1)) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    /** 按自然月归并四季：3–5 春、6–8 夏、9–11 秋、12/1/2 冬。 */
    private fun seasonsOf(monthMillis: LongArray): List<SeasonUsage> {
        val seasonMillis = longArrayOf(
            monthMillis[2] + monthMillis[3] + monthMillis[4],
            monthMillis[5] + monthMillis[6] + monthMillis[7],
            monthMillis[8] + monthMillis[9] + monthMillis[10],
            monthMillis[11] + monthMillis[0] + monthMillis[1],
        )
        return SEASON_NAMES.mapIndexed { index, name -> SeasonUsage(name, seasonMillis[index]) }
    }
}
