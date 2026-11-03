package io.github.huig7.shi.domain.model

import java.util.UUID

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
    /** schema 2 起写入；读取 schema 1 时为 null，导入后生成新 UUID。 */
    val uuid: String? = null,
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
