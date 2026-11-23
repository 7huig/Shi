package io.github.huig7.shi.domain.plan

import io.github.huig7.shi.domain.clipTo
import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.TimelineEvent
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * 把分类计划翻译成首页可以直接渲染的「今日提示」。
 *
 * 纯函数：所有时间输入都显式传入，方便测试跨天、目标进度、固定时间是否已过等边界。
 * 这里不区分「提示该不该弹」，只给出当前状态；是否展示由界面决定。
 */
object PlanPromptBuilder {

    private const val MINUTE_MILLIS = 60_000L

    fun build(
        plans: List<CategoryPlan>,
        tree: CategoryTree,
        events: List<TimelineEvent>,
        now: Long,
        zone: ZoneId,
    ): List<PlanPrompt> {
        if (plans.isEmpty()) return emptyList()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val clipped = clipTo(events, dayStart, minOf(dayEnd, now))

        val direct = HashMap<Long, Long>()
        clipped.forEach { item ->
            val categoryId = item.event.event.categoryId
            direct[categoryId] = (direct[categoryId] ?: 0L) + (item.end - item.start)
        }
        val subtree = subtreeTotals(tree, direct)

        // 间隔待办会读取所有历史记录（包括昨天），用来判断「刚记过一笔」。
        val latestDirect = HashMap<Long, Long>()
        events.forEach { item ->
            val event = item.event
            if (event.startAt > now) return@forEach
            val endAt = (event.startAt + event.durationMillis).coerceAtMost(now)
            if (endAt <= 0L) return@forEach
            latestDirect[event.categoryId] =
                maxOf(latestDirect[event.categoryId] ?: 0L, endAt)
        }
        val latestSubtree = subtreeLatest(tree, latestDirect)

        val prompts = ArrayList<PlanPrompt>()
        plans.forEach { plan ->
            val node = tree.find(plan.categoryId) ?: return@forEach
            val subtreeIds = tree.subtreeIds(plan.categoryId).toSet()
            val name = node.category.name
            val color = node.category.colorHex

            if (plan.hasDailyGoal) {
                val done = subtree[plan.categoryId] ?: 0L
                prompts += PlanPrompt(
                    categoryId = plan.categoryId,
                    categoryName = name,
                    colorHex = color,
                    kind = PlanPromptKind.GOAL,
                    label = name,
                    due = done < plan.dailyGoalMillis,
                    goalDoneMillis = done,
                    goalTargetMillis = plan.dailyGoalMillis,
                    nowMillis = now,
                )
            }

            val fixedTimes = plan.fixedTimes
                .filter { it.minuteOfDay in 0 until MINUTES_PER_DAY }
                .sortedBy { it.minuteOfDay }
            fixedTimes.forEachIndexed { index, fixed ->
                val minute = fixed.minuteOfDay
                val fixedAt = today.atTime(LocalTime.of(minute / 60, minute % 60))
                    .atZone(zone).toInstant().toEpochMilli()
                val nextAt = fixedTimes.getOrNull(index + 1)?.let { next ->
                    today.atTime(LocalTime.of(next.minuteOfDay / 60, next.minuteOfDay % 60))
                        .atZone(zone).toInstant().toEpochMilli()
                } ?: dayEnd
                // 每个固定时间只认「本时段内」的记录，避免晚上的一次记录把早上所有事项都标成已完成。
                val fixedDone = clipped.any { item ->
                    item.event.event.categoryId in subtreeIds &&
                        item.end > fixedAt &&
                        item.start < nextAt
                }
                val leadMillis = fixed.leadMinutes.coerceAtLeast(0) * MINUTE_MILLIS
                prompts += PlanPrompt(
                    categoryId = plan.categoryId,
                    categoryName = name,
                    colorHex = color,
                    kind = PlanPromptKind.FIXED_TIME,
                    label = fixed.label?.takeIf { it.isNotBlank() } ?: name,
                    due = now >= fixedAt - leadMillis && !fixedDone,
                    fixedTimeMinuteOfDay = minute,
                    fixedTimeId = fixed.id,
                    fixedTimeLeadMinutes = fixed.leadMinutes.coerceAtLeast(0),
                    fixedDone = fixedDone,
                    nowMillis = now,
                )
            }

            if (plan.hasInterval) {
                // 记录本身就是一次「做过」：只认本次间隔开始之后的记录，避免刚开启计划时
                // 被很久以前的记录立刻催办；跨天的记录同样参与计算。
                val interval = plan.intervalMinutes ?: 0
                val intervalMillis = interval * MINUTE_MILLIS
                val persistedNext = plan.nextReminderAt
                val cycleStart = persistedNext?.minus(intervalMillis)
                val lastRecordEnd = latestSubtree[plan.categoryId] ?: 0L
                val recentRecordEnd = if (cycleStart != null && lastRecordEnd > cycleStart) {
                    lastRecordEnd
                } else {
                    0L
                }
                val computedNext = when {
                    recentRecordEnd > 0L -> recentRecordEnd + intervalMillis
                    persistedNext != null -> persistedNext
                    else -> now
                }
                prompts += PlanPrompt(
                    categoryId = plan.categoryId,
                    categoryName = name,
                    colorHex = color,
                    kind = PlanPromptKind.INTERVAL,
                    label = plan.intervalLabel?.takeIf { it.isNotBlank() } ?: name,
                    due = now >= computedNext,
                    nextReminderAt = computedNext,
                    intervalMinutes = plan.intervalMinutes,
                    nowMillis = now,
                )
            }
        }

        return prompts.sortedWith(
            compareByDescending<PlanPrompt> { it.due }
                .thenBy { it.kind.ordinal }
                .thenBy { it.categoryName }
                .thenBy { it.fixedTimeMinuteOfDay ?: 0 },
        )
    }

    /** 沿分类树自下而上累计今天的直接用时，父分类的目标自动包含子分类。 */
    internal fun subtreeTotals(
        tree: CategoryTree,
        direct: Map<Long, Long>,
    ): Map<Long, Long> {
        val totals = HashMap<Long, Long>()
        fun walk(node: CategoryNode): Long {
            var total = direct[node.category.id] ?: 0L
            node.children.forEach { child -> total += walk(child) }
            totals[node.category.id] = total
            return total
        }
        tree.roots.forEach(::walk)
        return totals
    }

    /** 与 [subtreeTotals] 类似，但取子树里最晚的一条记录结束时间。 */
    private fun subtreeLatest(
        tree: CategoryTree,
        direct: Map<Long, Long>,
    ): Map<Long, Long> {
        val latest = HashMap<Long, Long>()
        fun walk(node: CategoryNode): Long {
            var value = direct[node.category.id] ?: 0L
            node.children.forEach { child -> value = maxOf(value, walk(child)) }
            latest[node.category.id] = value
            return value
        }
        tree.roots.forEach(::walk)
        return latest
    }

    private const val MINUTES_PER_DAY = 24 * 60
}
