package io.github.huig7.shi.data.repository

import androidx.room.withTransaction
import io.github.huig7.shi.data.local.AppDatabase
import io.github.huig7.shi.data.local.entity.CategoryPlanEntity
import io.github.huig7.shi.data.local.entity.FixedTimeEntity
import io.github.huig7.shi.domain.model.ExportedCategoryPlan
import io.github.huig7.shi.domain.model.ExportedFixedTime
import io.github.huig7.shi.domain.plan.CategoryPlan
import io.github.huig7.shi.domain.plan.FixedTimePlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * 分类计划的持久化与「下一次提醒」规则。
 *
 * 从 [TimeLogRepository] 拆出来，避免记录仓库继续膨胀；计划主表只有一份配置，
 * 固定时间有多条，放在 `fixed_time` 表里，由这里组合成 domain 对象。
 */
internal class CategoryPlanStore(private val database: AppDatabase) {

    private val dao = database.categoryPlanDao()
    private val fixedTimeDao = database.fixedTimeDao()

    val plans: Flow<List<CategoryPlan>> = combine(
        dao.observeAll(),
        fixedTimeDao.observeAll(),
    ) { planEntities, fixedTimes ->
        val timesByCategory = fixedTimes.groupBy { it.categoryId }
        planEntities.map { entity ->
            entity.toDomain(timesByCategory[entity.categoryId].orEmpty())
        }
    }

    suspend fun get(categoryId: Long): CategoryPlan? {
        val entity = dao.getById(categoryId) ?: return null
        return entity.toDomain(fixedTimeDao.getByCategory(categoryId))
    }

    /**
     * 保存一个分类的目标与提醒配置。
     *
     * 间隔待办只在「首次开启 / 改了间隔」时从当前时间重新计时；改事项名、固定时间
     * 或其它配置不会重置下一次提醒。所有功能都关掉时连同固定时间一起删除。
     */
    suspend fun save(plan: CategoryPlan, now: Long): CategoryPlan = database.withTransaction {
        val heatmapEnabled = plan.heatmapEnabled
        val goalMinutes = plan.dailyGoalMinutes.coerceIn(0, MAX_DAILY_GOAL_MINUTES)
        val fixedTimes = sanitizeFixedTimes(plan.fixedTimes)
        val intervalMinutes = plan.intervalMinutes
            ?.takeIf { it > 0 }
            ?.coerceIn(MIN_INTERVAL_MINUTES, MAX_INTERVAL_MINUTES)
        val intervalLabel = plan.intervalLabel?.trim()?.ifEmpty { null }

        if (
            !heatmapEnabled &&
            goalMinutes == 0 &&
            fixedTimes.isEmpty() &&
            intervalMinutes == null
        ) {
            fixedTimeDao.deleteByCategory(plan.categoryId)
            dao.deleteById(plan.categoryId)
            return@withTransaction CategoryPlan(categoryId = plan.categoryId)
        }

        val existing = dao.getById(plan.categoryId)
        val nextReminderAt = when {
            intervalMinutes == null -> 0L
            existing == null ||
                existing.intervalMinutes != intervalMinutes ||
                existing.nextReminderAt <= 0L -> now + intervalMinutes * MINUTE_MILLIS
            else -> existing.nextReminderAt
        }

        val entity = CategoryPlanEntity(
            categoryId = plan.categoryId,
            heatmapEnabled = heatmapEnabled,
            dailyGoalMinutes = goalMinutes,
            intervalMinutes = intervalMinutes ?: 0,
            intervalLabel = intervalLabel,
            nextReminderAt = nextReminderAt,
            updatedAt = now,
        )
        dao.upsert(entity)

        fixedTimeDao.deleteByCategory(plan.categoryId)
        val inserted = fixedTimes.map { fixed ->
            fixedTimeDao.insert(
                FixedTimeEntity(
                    categoryId = plan.categoryId,
                    minuteOfDay = fixed.minuteOfDay,
                    label = fixed.label,
                    leadMinutes = fixed.leadMinutes,
                ),
            )
        }
        entity.toDomain(
            fixedTimes.mapIndexed { index, fixed ->
                FixedTimeEntity(
                    id = inserted[index],
                    categoryId = plan.categoryId,
                    minuteOfDay = fixed.minuteOfDay,
                    label = fixed.label,
                    leadMinutes = fixed.leadMinutes,
                )
            },
        )
    }

    suspend fun deleteAll() = database.withTransaction {
        fixedTimeDao.deleteAll()
        dao.deleteAll()
    }

    suspend fun exportAll(): List<ExportedCategoryPlan> {
        val timesByCategory = fixedTimeDao.getAll().groupBy { it.categoryId }
        return dao.getAll().map { entity ->
            entity.toExport(timesByCategory[entity.categoryId].orEmpty())
        }
    }

    suspend fun restore(
        plans: List<ExportedCategoryPlan>,
        idMap: Map<Long, Long>,
        overwrite: Boolean,
    ) = database.withTransaction {
        plans.forEach { plan ->
            val targetId = idMap[plan.categoryId] ?: return@forEach
            if (!overwrite && dao.getById(targetId) != null) return@forEach
            dao.upsert(
                CategoryPlanEntity(
                    categoryId = targetId,
                    heatmapEnabled = plan.heatmapEnabled,
                    dailyGoalMinutes = plan.dailyGoalMinutes,
                    intervalMinutes = plan.intervalMinutes,
                    intervalLabel = plan.intervalLabel,
                    nextReminderAt = plan.nextReminderAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            fixedTimeDao.deleteByCategory(targetId)
            plan.fixedTimes.forEach { fixed ->
                if (fixed.minuteOfDay !in 0 until MINUTES_PER_DAY) return@forEach
                fixedTimeDao.insert(
                    FixedTimeEntity(
                        categoryId = targetId,
                        minuteOfDay = fixed.minuteOfDay,
                        label = fixed.label?.trim()?.ifEmpty { null },
                        leadMinutes = fixed.leadMinutes.coerceIn(0, MAX_FIXED_LEAD_MINUTES),
                    ),
                )
            }
        }
    }

    private fun sanitizeFixedTimes(times: List<FixedTimePlan>): List<FixedTimePlan> =
        times
            .filter { it.minuteOfDay in 0 until MINUTES_PER_DAY }
            .map { fixed ->
                FixedTimePlan(
                    minuteOfDay = fixed.minuteOfDay,
                    label = fixed.label?.trim()?.ifEmpty { null },
                    leadMinutes = fixed.leadMinutes.coerceIn(0, MAX_FIXED_LEAD_MINUTES),
                )
            }
            .distinctBy { it.minuteOfDay }
            .sortedBy { it.minuteOfDay }

    private fun CategoryPlanEntity.toDomain(times: List<FixedTimeEntity>): CategoryPlan =
        CategoryPlan(
            categoryId = categoryId,
            heatmapEnabled = heatmapEnabled,
            dailyGoalMinutes = dailyGoalMinutes,
            fixedTimes = times
                .sortedBy { it.minuteOfDay }
                .map { fixed ->
                    FixedTimePlan(
                        id = fixed.id,
                        minuteOfDay = fixed.minuteOfDay,
                        label = fixed.label,
                        leadMinutes = fixed.leadMinutes,
                    )
                },
            intervalMinutes = intervalMinutes.takeIf { it > 0 },
            intervalLabel = intervalLabel,
            nextReminderAt = nextReminderAt.takeIf { it > 0L },
        )

    private fun CategoryPlanEntity.toExport(times: List<FixedTimeEntity>): ExportedCategoryPlan =
        ExportedCategoryPlan(
            categoryId = categoryId,
            heatmapEnabled = heatmapEnabled,
            dailyGoalMinutes = dailyGoalMinutes,
            fixedTimes = times
                .sortedBy { it.minuteOfDay }
                .map { fixed ->
                    ExportedFixedTime(
                        minuteOfDay = fixed.minuteOfDay,
                        label = fixed.label,
                        leadMinutes = fixed.leadMinutes,
                    )
                },
            intervalMinutes = intervalMinutes,
            intervalLabel = intervalLabel,
            nextReminderAt = nextReminderAt,
        )

    private companion object {
        const val MINUTE_MILLIS = 60_000L
        const val MINUTES_PER_DAY = 24 * 60
        const val MAX_DAILY_GOAL_MINUTES = MINUTES_PER_DAY
        const val MIN_INTERVAL_MINUTES = 5
        const val MAX_INTERVAL_MINUTES = 7 * MINUTES_PER_DAY
        const val MAX_FIXED_LEAD_MINUTES = 180
    }
}
