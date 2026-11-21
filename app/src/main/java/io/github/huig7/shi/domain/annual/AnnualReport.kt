package io.github.huig7.shi.domain.annual

import io.github.huig7.shi.domain.CategoryUsage
import java.time.LocalDate
import java.time.LocalTime

/**
 * 某一年的年度报告。
 *
 * 所有时间字段都由调用方显式传入的 year / now / zone 推导，不读取系统时钟；
 * 当前年份的数据会裁剪到 now，并在 [isPartial] 上标记。
 */
data class AnnualReport(
    val year: Int,
    /** 当前年且尚未结束。 */
    val isPartial: Boolean,
    /** 数据裁剪到的本地日期；完整年份为 12 月 31 日。 */
    val throughDate: LocalDate,
    /** 事件累计时长，重叠记录重复计入。 */
    val totalMillis: Long,
    /** 事件区间的并集，重叠只算一次。 */
    val coveredMillis: Long,
    /** 年度窗口内已经过去的时间，用作覆盖率分母。 */
    val elapsedMillis: Long,
    val eventCount: Int,
    /** 至少有记录覆盖的本地日历日。 */
    val activeDays: Int,
    val avgPerActiveDayMillis: Long,
    /** 根分类按用时降序，来自 [io.github.huig7.shi.domain.StatsBuilder]。 */
    val roots: List<CategoryUsage>,
    /** 12 个月的总时长，索引 0 为一月。 */
    val months: List<Long>,
    /** 24 个小时桶，按记录的开始时刻计数。 */
    val hourStarts: List<Int>,
    /** 年内裁剪后最长的一条记录。 */
    val longestEvent: NotableEvent?,
    /** 一天中最早的开始时刻；只统计开始于本年度内的记录。 */
    val earliestStart: LocalTime?,
    /** 一天中最晚的结束时刻；只统计开始与结束在同一本地日的记录。 */
    val latestEnd: LocalTime?,
    /** 相邻且有记录的最长连续天数，只在年内计算。 */
    val longestStreakDays: Int,
    val busiestDay: DayUsage?,
    /** 春 / 夏 / 秋 / 冬，按自然月划分。 */
    val seasons: List<SeasonUsage>,
    /** 与当前窗口等长、已对齐的去年同期总时长。 */
    val previousTotalMillis: Long,
    /** 同比变化绝对值最大的根分类；没有变化或只有孤立分类时为 null。 */
    val biggestChange: CategoryChange? = null,
    /** 本年度第一次 / 最后一次记录；只有跨年延续段时为 null。 */
    val firstEvent: NotableEvent? = null,
    val lastEvent: NotableEvent? = null,
    /** 一天中最晚的开始时刻。 */
    val latestStart: LocalTime? = null,
    /** 周末（周六 / 周日）的记录时长。 */
    val weekendMillis: Long = 0L,
    /** 开始于 22 点后或 5 点前的记录数。 */
    val nightOwlCount: Int = 0,
    /** 开始于 5–8 点的记录数。 */
    val earlyBirdCount: Int = 0,
    /** 本年出现过的分类数（含未知分类）。 */
    val categoryCount: Int = 0,
) {
    val hasData: Boolean get() = totalMillis > 0L

    /** 用时最多的根分类占总时长的比例。 */
    val topCategoryShare: Float?
        get() = roots.firstOrNull()?.let {
            if (totalMillis > 0L) it.millis.toFloat() / totalMillis else null
        }

    /** 周末记录占比；没有记录时为 null。 */
    val weekendRatio: Float?
        get() = if (totalMillis > 0L) weekendMillis.toFloat() / totalMillis else null

    /** 有记录的月份数。 */
    val activeMonthCount: Int get() = months.count { it > 0L }

    /** 覆盖时长占已过时长的比例；未来年份或没有已过时间时为 null。 */
    val coverageRatio: Float?
        get() = if (elapsedMillis > 0L) {
            (coveredMillis.toDouble() / elapsedMillis.toDouble()).toFloat().coerceIn(0f, 1f)
        } else {
            null
        }

    /** 与去年同期的总时长变化；去年没有数据时为 null。 */
    val totalDeltaRatio: Float?
        get() = if (previousTotalMillis > 0L) {
            (totalMillis - previousTotalMillis).toFloat() / previousTotalMillis
        } else {
            null
        }

    /** 用时最多的月份（1..12）；没有记录时为 null。并列取更早的月份。 */
    val busiestMonth: Int?
        get() = months.withIndex()
            .filter { it.value > 0L }
            .maxByOrNull { it.value }
            ?.index
            ?.plus(1)

    /** 最常开始记录的整点（0..23）；并列取更早的小时。 */
    val busiestHour: Int?
        get() = hourStarts.withIndex()
            .filter { it.value > 0 }
            .maxByOrNull { it.value }
            ?.index
}

/** 年度报告里单独展示的一条记录。 */
data class NotableEvent(
    val eventId: Long,
    val categoryPath: String,
    val colorHex: Long,
    val startAt: Long,
    val endAt: Long,
    val durationMillis: Long,
)

/** 单日用时。 */
data class DayUsage(
    val date: LocalDate,
    val millis: Long,
    val eventCount: Int = 0,
)

/** 四季用时。 */
data class SeasonUsage(
    val name: String,
    val millis: Long,
)

/** 同比变化最大的分类；[currentMillis] 或 [previousMillis] 可以为 0（新增 / 今年没有记录）。 */
data class CategoryChange(
    val categoryId: Long,
    val name: String,
    val colorHex: Long,
    val currentMillis: Long,
    val previousMillis: Long,
) {
    val deltaMillis: Long get() = currentMillis - previousMillis

    val deltaRatio: Float?
        get() = if (previousMillis > 0L) deltaMillis.toFloat() / previousMillis else null
}
