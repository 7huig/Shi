package com.example.timelog.domain.model

data class Event(
    val id: Long,
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    /** 时长毫秒，永远落在 [60_000, ...] 里，避免脏数据产生零宽 / 负宽事件。 */
    val durationMillis: Long get() = (endAt - startAt).coerceAtLeast(60_000L)
}

data class TimelineEvent(
    val event: Event,
    val categoryPath: String,
    val categoryColor: Long,
)
