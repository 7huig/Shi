package io.github.huig7.shi.domain

import io.github.huig7.shi.domain.model.Event

/**
 * 目标区间（用户正在保存的这一段）与已有记录冲突时的处理方案。
 *
 * 语义：目标区间优先，老记录让路。
 * - 老记录被目标区间整个盖住 → [OverlapPlan.victims]，要用户确认后才删除；
 * - 老记录只在一侧超出 → [OverlapPlan.updates] 把超出的那一侧裁掉；
 * - 老记录两侧都超出 → update 保留左侧残余，clones 复制出右侧残余；
 * - 首尾相接（老记录 end == 目标 start 等）或没有交集 → 原样不动。
 */
data class OverlapUpdate(val event: Event, val startAt: Long, val endAt: Long)

/** 老记录横跨目标区间两侧时，右侧残余被复制成的新记录；左侧残余由 [OverlapUpdate] 原地保留。 */
data class OverlapClone(
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
)

/** [resolveOverlap] 的结果：updates / clones 静默生效，victims 需要用户确认。 */
data class OverlapPlan(
    val updates: List<OverlapUpdate> = emptyList(),
    val clones: List<OverlapClone> = emptyList(),
    val victims: List<Event> = emptyList(),
)

/**
 * 计算把 [startAt, endAt) 写进时间轴时，其余记录 [others] 该怎么让路。
 *
 * 区间按半开区间 [start, end) 处理：老记录的 end == 目标 start（或反之）不算重叠。
 * 目标区间非法（endAt <= startAt）时返回空方案；零宽 / 负宽的老记录直接跳过。
 */
fun resolveOverlap(startAt: Long, endAt: Long, others: List<Event>): OverlapPlan {
    if (endAt <= startAt) return OverlapPlan()

    val updates = mutableListOf<OverlapUpdate>()
    val clones = mutableListOf<OverlapClone>()
    val victims = mutableListOf<Event>()

    others.forEach { other ->
        val otherStart = other.startAt
        val otherEnd = other.endAt
        // 脏数据（零宽 / 负宽）和首尾相接都不算重叠。
        if (otherEnd <= otherStart) return@forEach
        if (otherStart >= endAt || otherEnd <= startAt) return@forEach

        val keepsLeft = otherStart < startAt
        val keepsRight = otherEnd > endAt
        when {
            keepsLeft && keepsRight -> {
                updates += OverlapUpdate(other, otherStart, startAt)
                clones += OverlapClone(
                    note = other.note,
                    startAt = endAt,
                    endAt = otherEnd,
                    categoryId = other.categoryId,
                    createdAt = other.createdAt,
                )
            }

            keepsLeft -> updates += OverlapUpdate(other, otherStart, startAt)
            keepsRight -> updates += OverlapUpdate(other, endAt, otherEnd)
            else -> victims += other
        }
    }

    return OverlapPlan(updates = updates, clones = clones, victims = victims)
}
