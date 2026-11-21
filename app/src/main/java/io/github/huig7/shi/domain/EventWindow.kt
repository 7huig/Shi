package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.TimelineEvent

/**
 * 事件在统计窗口内的裁剪结果。
 *
 * [start] / [end] 是裁剪后的时间戳，[event] 保留原始记录，供分类路径与颜色使用。
 */
internal data class ClippedEvent(
    val event: TimelineEvent,
    val start: Long,
    val end: Long,
)

/**
 * 把事件裁剪到半开区间 [from, until)，丢弃与窗口没有交集的部分。
 *
 * 统计页与年度报告共用这一份逻辑，避免两套口径在跨天、跨年与「裁剪到现在」时出现偏差。
 */
internal fun clipTo(
    events: List<TimelineEvent>,
    from: Long,
    until: Long,
): List<ClippedEvent> {
    if (until <= from) return emptyList()
    return events.mapNotNull { event ->
        val start = maxOf(event.event.startAt, from)
        val end = minOf(event.event.startAt + event.event.durationMillis, until)
        if (end <= start) null else ClippedEvent(event, start, end)
    }
}
