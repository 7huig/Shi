package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.Category
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.domain.model.TimelineEvent
import io.github.huig7.shi.domain.model.buildCategoryTree
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

internal const val MINUTE = 60_000L
internal const val HOUR = 60 * MINUTE
internal const val WORK_COLOR = 0xFF4C7BE8L

/**
 * 统计测试的共享夹具：固定时区与「现在」，并构造小规模分类树和事件。
 *
 * 所有时间都通过参数注入（zone = Asia/Shanghai，now = 2026-11-01T12:00，今天是周日）。
 * 测试按行为拆在多个文件里，夹具只在这里维护一份，避免各文件漂移。
 */
abstract class StatisticsTestFixtures {

    protected val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    protected val nowMillis: Long = at("2026-11-01T12:00")
    protected val today: LocalDate = LocalDate.of(2026, 11, 1)

    protected fun at(value: String): Long = at(value, zone)

    protected fun at(value: String, targetZone: ZoneId): Long =
        LocalDateTime.parse(value).atZone(targetZone).toInstant().toEpochMilli()

    protected fun event(
        id: Long,
        start: String,
        end: String,
        categoryId: Long = 1L,
        path: String = "工作",
        color: Long = WORK_COLOR,
    ): TimelineEvent {
        val startAt = at(start)
        return TimelineEvent(
            event = Event(
                id = id,
                note = null,
                startAt = startAt,
                endAt = at(end),
                categoryId = categoryId,
                createdAt = startAt,
                updatedAt = startAt,
            ),
            categoryPath = path,
            categoryColor = color,
        )
    }

    protected fun build(
        vararg events: TimelineEvent,
        window: StatsWindow = StatsWindows.of(StatsMode.DAY, today, zone),
        now: Long = nowMillis,
        tree: CategoryTree = tree(),
        rootId: Long? = null,
    ): StatsSummary = StatsBuilder.build(events.toList(), tree, window, now, zone, rootId)

    /** 工作(1) / 执行(2) / 编码(3) / 深潜(6)，生活(4) / 运动(5)。 */
    protected fun tree(): CategoryTree = buildCategoryTree(
        listOf(
            category(1L, "工作", null, WORK_COLOR),
            category(2L, "执行", 1L, 0xFF4C7BE8),
            category(3L, "编码", 2L, 0xFF2F6FE4),
            category(6L, "深潜", 3L, 0xFF3F51B5),
            category(4L, "生活", null, 0xFF8B5CF6),
            category(5L, "运动", 4L, 0xFF8B5CF6),
        ),
    )

    private fun category(id: Long, name: String, parentId: Long?, colorHex: Long): Category =
        Category(
            id = id,
            name = name,
            parentId = parentId,
            sortOrder = id.toInt(),
            colorHex = colorHex,
        )
}
