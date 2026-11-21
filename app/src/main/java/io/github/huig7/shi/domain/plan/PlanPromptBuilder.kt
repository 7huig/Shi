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
                )
            }

            plan.fixedTimeMinuteOfDay?.let { minute ->
                val fixedAt = today.atTime(LocalTime.of(minute / 60, minute % 60))
                    .atZone(zone).toInstant().toEpochMilli()
                val fixedDone = clipped.any { item ->
                    item.event.event.categoryId in subtreeIds && item.end > fixedAt
                }
                prompts += PlanPrompt(
                    categoryId = plan.categoryId,
                    categoryName = name,
                    colorHex = color,
                    kind = PlanPromptKind.FIXED_TIME,
                    label = plan.fixedTimeLabel?.takeIf { it.isNotBlank() } ?: name,
                    due = now >= fixedAt && !fixedDone,
                    fixedTimeMinuteOfDay = minute,
                    fixedDone = fixedDone,
                )
            }

            if (plan.hasInterval) {
                // 旧数据没有 nextReminderAt 时按「现在就该做」处理，保存新计划时一定会写入下一次时间。
                val nextAt = plan.nextReminderAt ?: now
                prompts += PlanPrompt(
                    categoryId = plan.categoryId,
                    categoryName = name,
                    colorHex = color,
                    kind = PlanPromptKind.INTERVAL,
                    label = plan.intervalLabel?.takeIf { it.isNotBlank() } ?: name,
                    due = now >= nextAt,
                    nextReminderAt = nextAt,
                    intervalMinutes = plan.intervalMinutes,
                    lastCompletedAt = plan.lastCompletedAt,
                )
            }
        }

        return prompts.sortedWith(
            compareByDescending<PlanPrompt> { it.due }
                .thenBy { it.kind.ordinal }
                .thenBy { it.categoryName },
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
}
