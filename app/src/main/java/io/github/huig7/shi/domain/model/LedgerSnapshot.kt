package io.github.huig7.shi.domain.model

import java.util.UUID

/** 导入 / 导出用的完整数据快照，与数据库实体解耦，方便用 JSON 表达。 */
data class LedgerSnapshot(
    val exportedAt: Long,
    val categories: List<ExportedCategory>,
    val events: List<ExportedEvent>,
    /** schema 3 起写入；旧备份没有计划，导入后保持未配置。 */
    val plans: List<ExportedCategoryPlan> = emptyList(),
)

data class ExportedCategory(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    /**
     * schema 2 及更早版本用来标记系统分类的字段，现已不再写 true。
     *
     * 保留它是为了继续读写旧备份：导入旧文件时会跳过没有被事件引用的系统「未分类」，
     * 避免升级后把已经删掉的分类又恢复出来。
     */
    val isSystem: Boolean = false,
)

data class ExportedEvent(
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
    /** schema 2 起写入；读取 schema 1 时为 null，导入后生成新 UUID。 */
    val uuid: String? = null,
)

/**
 * 分类计划的导出形态。
 *
 * 保存用户填写的参数与下一次提醒时间，而不只导出开关；这样恢复到另一台设备后，
 * 间隔待办不会从头开始，完成历史也能保留。
 */
data class ExportedCategoryPlan(
    val categoryId: Long,
    val heatmapEnabled: Boolean,
    val dailyGoalMinutes: Int,
    val fixedTimeMinuteOfDay: Int,
    val fixedTimeLabel: String?,
    val intervalMinutes: Int,
    val intervalLabel: String?,
    val nextReminderAt: Long,
    val lastCompletedAt: Long,
)

enum class ImportMode { MERGE, REPLACE }

/** schema 1 没有事件 UUID；用稳定字段和同内容出现序号兼容重复合并。 */
internal fun ExportedEvent.legacyIdentity(): String = buildString {
    append(startAt).append('|')
    append(endAt).append('|')
    append(categoryId).append('|')
    append(createdAt).append('|')
    append(updatedAt).append('|')
    append(note?.length ?: -1).append('|')
    append(note.orEmpty())
}

internal fun ExportedEvent.legacyUuid(occurrence: Int): String =
    UUID.nameUUIDFromBytes(
        "shi-legacy-event-v1\n${legacyIdentity()}\n$occurrence".toByteArray(Charsets.UTF_8),
    ).toString()
