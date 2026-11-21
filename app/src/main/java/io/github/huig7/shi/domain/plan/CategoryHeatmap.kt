package io.github.huig7.shi.domain.plan

import io.github.huig7.shi.domain.clipTo
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.TimelineEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 热力图里的一天；没有记录时 [millis] 为 0。 */
data class HeatmapDay(val date: LocalDate, val millis: Long)

/**
 * 某个分类在最近一段时间内的每日用时。
 *
 * [days] 按日期从早到晚排列；父分类包含整棵子树的记录。
 */
data class CategoryHeatmap(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: Long,
    val days: List<HeatmapDay>,
) {
    val totalMillis: Long get() = days.sumOf { it.millis }

    val activeDays: Int get() = days.count { it.millis > 0L }

    /** 用于把每天的颜色映射到 0..1；全为空时返回 0。 */
    val maxMillis: Long get() = days.maxOfOrNull { it.millis } ?: 0L
}

/**
 * 只为显式开启热力图的分类生成数据。
 *
 * 跨天事件会按本地日历拆到每一天；父分类的每一天自动累加子分类，
 * 与统计页「分类含子分类」的口径一致。
 */
object CategoryHeatmapBuilder {

    const val DEFAULT_DAYS: Int = 168

    fun build(
        events: List<TimelineEvent>,
        tree: CategoryTree,
        plans: List<CategoryPlan>,
        endDate: LocalDate,
        zone: ZoneId,
        dayCount: Int = DEFAULT_DAYS,
    ): List<CategoryHeatmap> {
        val enabled = plans.filter { it.heatmapEnabled && tree.find(it.categoryId) != null }
        if (enabled.isEmpty()) return emptyList()

        val days = dayCount.coerceIn(1, 366)
        val startDate = endDate.minusDays(days - 1L)
        val from = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val until = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        // categoryId -> (本地日期 -> 直接记录毫秒数)。跨天事件在循环里切开。
        val direct = HashMap<Long, HashMap<LocalDate, Long>>()
        clipTo(events, from, until).forEach { item ->
            var cursor = item.start
            while (cursor < item.end) {
                val date = Instant.ofEpochMilli(cursor).atZone(zone).toLocalDate()
                val nextDay = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val sliceEnd = minOf(item.end, nextDay)
                if (sliceEnd > cursor) {
                    val byDate = direct.getOrPut(item.event.event.categoryId) { HashMap() }
                    byDate[date] = (byDate[date] ?: 0L) + (sliceEnd - cursor)
                }
                cursor = sliceEnd
            }
        }

        return enabled.mapNotNull { plan ->
            val node = tree.find(plan.categoryId) ?: return@mapNotNull null
            val subtreeIds = tree.subtreeIds(plan.categoryId)
            val heatmapDays = ArrayList<HeatmapDay>(days)
            var date = startDate
            while (!date.isAfter(endDate)) {
                var total = 0L
                subtreeIds.forEach { id ->
                    total += direct[id]?.get(date) ?: 0L
                }
                heatmapDays += HeatmapDay(date, total)
                date = date.plusDays(1)
            }
            CategoryHeatmap(
                categoryId = plan.categoryId,
                categoryName = node.category.name,
                colorHex = node.category.colorHex,
                days = heatmapDays,
            )
        }
    }
}
