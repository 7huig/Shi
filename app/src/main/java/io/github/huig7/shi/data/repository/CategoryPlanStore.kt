package io.github.huig7.shi.data.repository

import io.github.huig7.shi.data.local.dao.CategoryPlanDao
import io.github.huig7.shi.data.local.entity.CategoryPlanEntity
import io.github.huig7.shi.domain.model.ExportedCategoryPlan
import io.github.huig7.shi.domain.plan.CategoryPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 分类计划的持久化与「下一次提醒」规则。
 *
 * 从 [TimeLogRepository] 拆出来，避免记录仓库继续膨胀；这里只依赖 CategoryPlanDao，
 * 不碰事件与分类树，所有方法都可以单独测试。
 */
internal class CategoryPlanStore(private val dao: CategoryPlanDao) {

    val plans: Flow<List<CategoryPlan>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun get(categoryId: Long): CategoryPlan? = dao.getById(categoryId)?.toDomain()

    /**
     * 保存一个分类的目标与提醒配置。
     *
     * 间隔待办只在「首次开启 / 改了间隔」时从当前时间重新计时；改事项名或其它配置不会
     * 重置下一次提醒。所有功能都关掉时直接删除配置行，避免留下空记录。
     */
    suspend fun save(plan: CategoryPlan, now: Long): CategoryPlan {
        val heatmapEnabled = plan.heatmapEnabled
        val goalMinutes = plan.dailyGoalMinutes.coerceIn(0, MAX_DAILY_GOAL_MINUTES)
        val fixedMinute = plan.fixedTimeMinuteOfDay?.coerceIn(0, MINUTES_PER_DAY - 1)
        val fixedLabel = plan.fixedTimeLabel?.trim()?.ifEmpty { null }
        val intervalMinutes = plan.intervalMinutes
            ?.takeIf { it > 0 }
            ?.coerceIn(MIN_INTERVAL_MINUTES, MAX_INTERVAL_MINUTES)
        val intervalLabel = plan.intervalLabel?.trim()?.ifEmpty { null }

        if (!heatmapEnabled && goalMinutes == 0 && fixedMinute == null && intervalMinutes == null) {
            dao.deleteById(plan.categoryId)
            return CategoryPlan(categoryId = plan.categoryId)
        }

        val existing = dao.getById(plan.categoryId)
        val nextReminderAt = when {
            intervalMinutes == null -> 0L
            existing == null ||
                existing.intervalMinutes != intervalMinutes ||
                existing.nextReminderAt <= 0L -> now + intervalMinutes * 60_000L
            else -> existing.nextReminderAt
        }
        val lastCompletedAt = if (
            intervalMinutes != null && existing?.intervalMinutes == intervalMinutes
        ) {
            existing.lastCompletedAt
        } else {
            0L
        }

        val entity = CategoryPlanEntity(
            categoryId = plan.categoryId,
            heatmapEnabled = heatmapEnabled,
            dailyGoalMinutes = goalMinutes,
            fixedTimeMinuteOfDay = fixedMinute ?: -1,
            fixedTimeLabel = fixedLabel,
            intervalMinutes = intervalMinutes ?: 0,
            intervalLabel = intervalLabel,
            nextReminderAt = nextReminderAt,
            lastCompletedAt = lastCompletedAt,
            updatedAt = now,
        )
        dao.upsert(entity)
        return entity.toDomain()
    }

    /** 完成一次间隔待办，返回刷新后的下一次提醒时间；没有启用间隔待办时返回 null。 */
    suspend fun complete(categoryId: Long, now: Long): Long? {
        val plan = dao.getById(categoryId) ?: return null
        val interval = plan.intervalMinutes
        if (interval <= 0) return null
        val next = now + interval * 60_000L
        dao.markCompleted(categoryId, now, next)
        return next
    }

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun exportAll(): List<ExportedCategoryPlan> = dao.getAll().map { it.toExport() }

    suspend fun restore(
        plans: List<ExportedCategoryPlan>,
        idMap: Map<Long, Long>,
        overwrite: Boolean,
    ) {
        plans.forEach { plan ->
            val targetId = idMap[plan.categoryId] ?: return@forEach
            if (!overwrite && dao.getById(targetId) != null) return@forEach
            dao.upsert(
                CategoryPlanEntity(
                    categoryId = targetId,
                    heatmapEnabled = plan.heatmapEnabled,
                    dailyGoalMinutes = plan.dailyGoalMinutes,
                    fixedTimeMinuteOfDay = plan.fixedTimeMinuteOfDay,
                    fixedTimeLabel = plan.fixedTimeLabel,
                    intervalMinutes = plan.intervalMinutes,
                    intervalLabel = plan.intervalLabel,
                    nextReminderAt = plan.nextReminderAt,
                    lastCompletedAt = plan.lastCompletedAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun CategoryPlanEntity.toDomain(): CategoryPlan = CategoryPlan(
        categoryId = categoryId,
        heatmapEnabled = heatmapEnabled,
        dailyGoalMinutes = dailyGoalMinutes,
        fixedTimeMinuteOfDay = fixedTimeMinuteOfDay.takeIf { it in 0 until MINUTES_PER_DAY },
        fixedTimeLabel = fixedTimeLabel,
        intervalMinutes = intervalMinutes.takeIf { it > 0 },
        intervalLabel = intervalLabel,
        nextReminderAt = nextReminderAt.takeIf { it > 0L },
        lastCompletedAt = lastCompletedAt.takeIf { it > 0L },
    )

    private fun CategoryPlanEntity.toExport(): ExportedCategoryPlan = ExportedCategoryPlan(
        categoryId = categoryId,
        heatmapEnabled = heatmapEnabled,
        dailyGoalMinutes = dailyGoalMinutes,
        fixedTimeMinuteOfDay = fixedTimeMinuteOfDay,
        fixedTimeLabel = fixedTimeLabel,
        intervalMinutes = intervalMinutes,
        intervalLabel = intervalLabel,
        nextReminderAt = nextReminderAt,
        lastCompletedAt = lastCompletedAt,
    )

    private companion object {
        const val MINUTES_PER_DAY = 24 * 60
        const val MAX_DAILY_GOAL_MINUTES = MINUTES_PER_DAY
        const val MIN_INTERVAL_MINUTES = 5
        const val MAX_INTERVAL_MINUTES = 7 * MINUTES_PER_DAY
    }
}
