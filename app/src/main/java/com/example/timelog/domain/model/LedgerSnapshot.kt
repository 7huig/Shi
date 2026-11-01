package com.example.timelog.domain.model

/** 导入 / 导出用的完整数据快照，与数据库实体解耦，方便用 JSON 表达。 */
data class LedgerSnapshot(
    val exportedAt: Long,
    val categories: List<ExportedCategory>,
    val events: List<ExportedEvent>,
)

data class ExportedCategory(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    val isSystem: Boolean,
)

data class ExportedEvent(
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class ImportMode { MERGE, REPLACE }
