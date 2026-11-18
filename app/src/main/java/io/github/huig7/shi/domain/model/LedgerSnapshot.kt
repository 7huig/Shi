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
