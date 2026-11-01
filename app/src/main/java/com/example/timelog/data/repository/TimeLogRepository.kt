package com.example.timelog.data.repository

import androidx.room.withTransaction
import com.example.timelog.data.local.AppDatabase
import com.example.timelog.data.local.entity.CategoryEntity
import com.example.timelog.data.local.entity.EventEntity
import com.example.timelog.domain.model.Category
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.Event
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.buildCategoryTree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TimeLogRepository(private val database: AppDatabase) {
    private val categoryDao = database.categoryDao()
    private val eventDao = database.eventDao()

    val categories: Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    val categoryTree: Flow<CategoryTree> = categories.map(::buildCategoryTree)

    val timeline: Flow<List<TimelineEvent>> = combine(
        eventDao.observeAll(),
        categoryDao.observeAll(),
    ) { events, categories ->
        val tree = buildCategoryTree(categories.map { it.toDomain() })
        events.map { entity ->
            val event = entity.toDomain()
            TimelineEvent(
                event = event,
                categoryPath = tree.pathOf(event.categoryId),
                categoryColor = tree.find(event.categoryId)?.category?.colorHex ?: 0xFF9AA29A,
            )
        }
    }

    suspend fun getEvent(id: Long): Event? = eventDao.getById(id)?.toDomain()

    suspend fun saveEvent(
        id: Long?,
        title: String,
        note: String?,
        timestamp: Long,
        categoryId: Long,
    ) {
        val now = System.currentTimeMillis()
        if (id == null) {
            eventDao.insert(
                EventEntity(
                    title = title.trim(),
                    note = note?.trim()?.ifEmpty { null },
                    timestamp = timestamp,
                    categoryId = categoryId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = eventDao.getById(id) ?: return
            eventDao.update(
                existing.copy(
                    title = title.trim(),
                    note = note?.trim()?.ifEmpty { null },
                    timestamp = timestamp,
                    categoryId = categoryId,
                    updatedAt = now,
                ),
            )
        }
    }

    suspend fun deleteEvent(id: Long) {
        eventDao.deleteById(id)
    }

    suspend fun createCategory(name: String, parentId: Long?, colorHex: Long): Long {
        val order = categoryDao.maxSortOrder(parentId) + 1
        return categoryDao.insert(
            CategoryEntity(
                name = name.trim(),
                parentId = parentId,
                sortOrder = order,
                colorHex = colorHex,
            ),
        )
    }

    suspend fun updateCategory(id: Long, name: String, colorHex: Long) {
        val existing = categoryDao.getById(id) ?: return
        categoryDao.update(existing.copy(name = name.trim(), colorHex = colorHex))
    }

    suspend fun countEventsInSubtree(categoryId: Long): Int {
        val tree = buildCategoryTree(categoryDao.getAll().map { it.toDomain() })
        val ids = tree.subtreeIds(categoryId)
        return if (ids.isEmpty()) 0 else eventDao.countByCategoryIds(ids)
    }

    /**
     * 删除分类时把该分类及其子树下的事件迁移到「未分类」，避免用户整理分类时丢数据。
     * 事件外键是 RESTRICT，必须先迁移事件，才能删除分类；子分类由自引用外键 CASCADE 清理。
     */
    suspend fun deleteCategory(categoryId: Long): CategoryDeletionResult = database.withTransaction {
        val all = categoryDao.getAll().map { it.toDomain() }
        val tree = buildCategoryTree(all)
        val node = tree.find(categoryId) ?: return@withTransaction CategoryDeletionResult.NotFound
        if (node.category.isSystem) return@withTransaction CategoryDeletionResult.SystemCategory

        val ids = tree.subtreeIds(categoryId)
        val eventCount = eventDao.countByCategoryIds(ids)
        val uncategorized = categoryDao.getSystemCategory()
            ?: error("系统分类「未分类」缺失")
        if (eventCount > 0) {
            eventDao.reassignCategory(ids, uncategorized.id, System.currentTimeMillis())
        }
        categoryDao.deleteById(categoryId)
        CategoryDeletionResult.Deleted(
            categoryCount = ids.size,
            reassignedEventCount = eventCount,
        )
    }

    private fun CategoryEntity.toDomain(): Category = Category(
        id = id,
        name = name,
        parentId = parentId,
        sortOrder = sortOrder,
        colorHex = colorHex,
        isSystem = isSystem,
    )

    private fun EventEntity.toDomain(): Event = Event(
        id = id,
        title = title,
        note = note,
        timestamp = timestamp,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

sealed interface CategoryDeletionResult {
    data class Deleted(val categoryCount: Int, val reassignedEventCount: Int) : CategoryDeletionResult
    data object NotFound : CategoryDeletionResult
    data object SystemCategory : CategoryDeletionResult
}
