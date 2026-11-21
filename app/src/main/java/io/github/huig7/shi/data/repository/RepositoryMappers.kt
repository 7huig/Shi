package io.github.huig7.shi.data.repository

import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.data.local.entity.EventEntity
import io.github.huig7.shi.domain.model.Category
import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.domain.model.ExportedCategory
import io.github.huig7.shi.domain.model.ExportedEvent

/** Room 实体与 domain / 备份模型之间的映射；与仓库行为分开，便于单文件阅读。 */
internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    parentId = parentId,
    sortOrder = sortOrder,
    colorHex = colorHex,
)

internal fun EventEntity.toDomain(): Event = Event(
    id = id,
    note = note,
    startAt = startAt,
    endAt = endAt,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    uuid = uuid,
)

internal fun CategoryEntity.toExport(): ExportedCategory = ExportedCategory(
    id = id,
    name = name,
    parentId = parentId,
    sortOrder = sortOrder,
    colorHex = colorHex,
    // isSystem 只为兼容旧备份格式保留；新导出的分类都是普通分类。
    isSystem = false,
)

internal fun EventEntity.toExport(): ExportedEvent = ExportedEvent(
    note = note,
    startAt = startAt,
    endAt = endAt,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    uuid = uuid,
)
