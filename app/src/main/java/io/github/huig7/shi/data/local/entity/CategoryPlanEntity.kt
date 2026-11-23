package io.github.huig7.shi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * 一个分类的「目标与提醒」配置。
 *
 * 一个分类最多有一条配置；删除分类时由外键级联删除，不需要额外清理。
 * 关掉的功能用 0 / null 表示，不用额外的开关列，避免出现「开关开着但参数为空」的矛盾状态。
 * 固定时间有多条，单独放在 `fixed_time` 表里。
 */
@Entity(
    tableName = "category_plan",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CategoryPlanEntity(
    @PrimaryKey val categoryId: Long,
    /** 是否在统计页展示该分类（含子分类）的最近 24 周热力图。 */
    val heatmapEnabled: Boolean = false,
    /** 每天希望投入的分钟数；0 表示不设目标。 */
    val dailyGoalMinutes: Int = 0,
    /** 间隔待办的间隔分钟数；0 表示不设。 */
    val intervalMinutes: Int = 0,
    val intervalLabel: String? = null,
    /** 下一次间隔提醒的时间戳；0 表示未启用。 */
    val nextReminderAt: Long = 0L,
    val updatedAt: Long = 0L,
)
