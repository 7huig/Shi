package com.example.timelog.data.repository

import androidx.room.withTransaction
import com.example.timelog.data.local.AppDatabase
import com.example.timelog.data.local.entity.CategoryEntity
import com.example.timelog.data.local.entity.EventEntity
import com.example.timelog.domain.OverlapPlan
import com.example.timelog.domain.model.Category
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.Event
import com.example.timelog.domain.model.ExportedCategory
import com.example.timelog.domain.model.ExportedEvent
import com.example.timelog.domain.model.ImportMode
import com.example.timelog.domain.model.LedgerSnapshot
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.buildCategoryTree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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

    suspend fun latestEventEnd(): Long = eventDao.latestEnd()

    /** 按备注搜索；空白关键字直接返回空列表，不查库。 */
    fun searchEvents(query: String): Flow<List<TimelineEvent>> {
        val keyword = query.trim()
        if (keyword.isEmpty()) return flowOf(emptyList())
        return combine(
            eventDao.searchByNote(keyword),
            categoryDao.observeAll(),
        ) { entities, categories ->
            val tree = buildCategoryTree(categories.map { it.toDomain() })
            entities.map { entity ->
                val event = entity.toDomain()
                TimelineEvent(
                    event = event,
                    categoryPath = tree.pathOf(event.categoryId),
                    categoryColor = tree.find(event.categoryId)?.category?.colorHex ?: 0xFF9AA29A,
                )
            }
        }
    }

    suspend fun getOverlappingEvents(startAt: Long, endAt: Long, excludeId: Long): List<Event> =
        eventDao.getOverlapping(startAt, endAt, excludeId).map { it.toDomain() }

    suspend fun saveEvent(
        id: Long?,
        note: String?,
        startAt: Long,
        endAt: Long,
        categoryId: Long,
    ) {
        val now = System.currentTimeMillis()
        val cleanNote = note.cleanNote()
        val safeEndAt = endAt.coerceAtLeast(startAt + 60_000L)
        if (id == null) {
            eventDao.insert(
                EventEntity(
                    note = cleanNote,
                    startAt = startAt,
                    endAt = safeEndAt,
                    categoryId = categoryId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = eventDao.getById(id) ?: return
            eventDao.update(
                existing.copy(
                    note = cleanNote,
                    startAt = startAt,
                    endAt = safeEndAt,
                    categoryId = categoryId,
                    updatedAt = now,
                ),
            )
        }
    }

    /**
     * 保存正在编辑的区间，并按 [plan] 让其它重叠记录让路：
     * victims 由用户确认后才会出现在 plan 里，调用方保证确认语义。
     */
    suspend fun saveEventResolved(
        id: Long?,
        note: String?,
        startAt: Long,
        endAt: Long,
        categoryId: Long,
        plan: OverlapPlan,
    ): Long = database.withTransaction {
        val now = System.currentTimeMillis()
        val cleanNote = note.cleanNote()
        val safeEndAt = endAt.coerceAtLeast(startAt + 60_000L)

        plan.victims.forEach { victim -> eventDao.deleteById(victim.id) }
        plan.updates.forEach { update ->
            eventDao.getById(update.event.id)?.let { existing ->
                eventDao.update(
                    existing.copy(
                        startAt = update.startAt,
                        endAt = update.endAt,
                        updatedAt = now,
                    ),
                )
            }
        }
        plan.clones.forEach { clone ->
            val cloneEnd = clone.endAt.coerceAtLeast(clone.startAt + 60_000L)
            eventDao.insert(
                EventEntity(
                    note = clone.note.cleanNote(),
                    startAt = clone.startAt,
                    endAt = cloneEnd,
                    categoryId = clone.categoryId,
                    createdAt = clone.createdAt,
                    updatedAt = now,
                ),
            )
        }

        if (id == null) {
            eventDao.insert(
                EventEntity(
                    note = cleanNote,
                    startAt = startAt,
                    endAt = safeEndAt,
                    categoryId = categoryId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = eventDao.getById(id)
            if (existing == null) {
                -1L
            } else {
                eventDao.update(
                    existing.copy(
                        note = cleanNote,
                        startAt = startAt,
                        endAt = safeEndAt,
                        categoryId = categoryId,
                        updatedAt = now,
                    ),
                )
                id
            }
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
     * 删除分类时连同该分类及其子树下的事件一起删除（用户明确要求，不做迁移）。
     * 事件外键是 RESTRICT，必须先删事件，才能删除分类；子分类由自引用外键 CASCADE 清理。
     */
    suspend fun deleteCategory(categoryId: Long): CategoryDeletionResult = database.withTransaction {
        val all = categoryDao.getAll().map { it.toDomain() }
        val tree = buildCategoryTree(all)
        val node = tree.find(categoryId) ?: return@withTransaction CategoryDeletionResult.NotFound
        if (node.category.isSystem) return@withTransaction CategoryDeletionResult.SystemCategory

        val ids = tree.subtreeIds(categoryId)
        val deletedEventCount = if (ids.isEmpty()) 0 else eventDao.deleteByCategoryIds(ids)
        categoryDao.deleteById(categoryId)
        CategoryDeletionResult.Deleted(
            categoryCount = ids.size,
            deletedEventCount = deletedEventCount,
        )
    }

    suspend fun snapshot(): LedgerSnapshot = database.withTransaction {
        LedgerSnapshot(
            exportedAt = System.currentTimeMillis(),
            categories = categoryDao.getAll().map { it.toExport() },
            events = eventDao.getAll().map { it.toExport() },
        )
    }

    /**
     * 导入数据。[ImportMode.REPLACE] 先清空再写入；[ImportMode.MERGE] 保留现有数据，
     * 同名同父的分类复用，其余分类新建，事件按旧分类 ID 映射到新分类。
     */
    suspend fun restore(snapshot: LedgerSnapshot, mode: ImportMode) = database.withTransaction {
        when (mode) {
            ImportMode.REPLACE -> restoreReplace(snapshot)
            ImportMode.MERGE -> restoreMerge(snapshot)
        }
    }

    private suspend fun restoreReplace(snapshot: LedgerSnapshot) {
        eventDao.deleteAll()
        categoryDao.deleteAll()

        val idMap = mutableMapOf<Long, Long>()
        orderByDepth(snapshot.categories).forEach { category ->
            val parentNewId = category.parentId?.let(idMap::get)
            val entity = CategoryEntity(
                id = category.id.coerceAtLeast(0L),
                name = category.name.trim(),
                parentId = parentNewId,
                sortOrder = category.sortOrder,
                colorHex = category.colorHex,
                isSystem = category.isSystem,
            )
            val newId = categoryDao.insert(entity)
            idMap[category.id] = newId
        }
        insertImportedEvents(snapshot.events, idMap)
    }

    private suspend fun restoreMerge(snapshot: LedgerSnapshot) {
        val existing = categoryDao.getAll().toMutableList()
        val system = existing.firstOrNull { it.isSystem }
        val idMap = mutableMapOf<Long, Long>()

        orderByDepth(snapshot.categories).forEach { category ->
            val parentNewId = category.parentId?.let(idMap::get)
            if (category.isSystem && system != null) {
                idMap[category.id] = system.id
                return@forEach
            }
            val reused = existing.firstOrNull {
                it.name == category.name.trim() &&
                    it.parentId == parentNewId &&
                    it.isSystem == category.isSystem
            }
            if (reused != null) {
                idMap[category.id] = reused.id
            } else {
                val newId = categoryDao.insert(
                    CategoryEntity(
                        name = category.name.trim(),
                        parentId = parentNewId,
                        sortOrder = category.sortOrder,
                        colorHex = category.colorHex,
                        isSystem = category.isSystem,
                    ),
                )
                idMap[category.id] = newId
                existing.add(
                    CategoryEntity(
                        id = newId,
                        name = category.name.trim(),
                        parentId = parentNewId,
                        sortOrder = category.sortOrder,
                        colorHex = category.colorHex,
                        isSystem = category.isSystem,
                    ),
                )
            }
        }
        insertImportedEvents(snapshot.events, idMap)
    }

    private suspend fun insertImportedEvents(
        events: List<ExportedEvent>,
        idMap: Map<Long, Long>,
    ) {
        val fallback = categoryDao.getSystemCategory()?.id
            ?: categoryDao.insert(
                CategoryEntity(
                    name = "未分类",
                    parentId = null,
                    sortOrder = categoryDao.maxSortOrder(null) + 1,
                    colorHex = 0xFF8C928C,
                    isSystem = true,
                ),
            )
        events.forEach { event ->
            val startAt = event.startAt
            val endAt = event.endAt.coerceAtLeast(startAt + 60_000L)
            eventDao.insert(
                EventEntity(
                    note = event.note.cleanNote(),
                    startAt = startAt,
                    endAt = endAt,
                    categoryId = idMap[event.categoryId] ?: fallback,
                    createdAt = event.createdAt,
                    updatedAt = event.updatedAt,
                ),
            )
        }
    }

    /** 父分类先于子分类插入，避免外键失败；异常数据里的环按根处理。 */
    private fun orderByDepth(categories: List<ExportedCategory>): List<ExportedCategory> {
        val byId = categories.associateBy { it.id }
        val depths = mutableMapOf<Long, Int>()

        fun depthOf(id: Long, visiting: MutableSet<Long>): Int {
            depths[id]?.let { return it }
            if (!visiting.add(id)) return 0
            val parentId = byId[id]?.parentId
            val depth = if (parentId == null || parentId !in byId) {
                0
            } else {
                depthOf(parentId, visiting) + 1
            }
            depths[id] = depth
            return depth
        }

        return categories.sortedBy { depthOf(it.id, mutableSetOf()) }
    }

    private fun String?.cleanNote(): String? = this?.trim()?.ifEmpty { null }

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
        note = note,
        startAt = startAt,
        endAt = endAt,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun CategoryEntity.toExport(): ExportedCategory = ExportedCategory(
        id = id,
        name = name,
        parentId = parentId,
        sortOrder = sortOrder,
        colorHex = colorHex,
        isSystem = isSystem,
    )

    private fun EventEntity.toExport(): ExportedEvent = ExportedEvent(
        note = note,
        startAt = startAt,
        endAt = endAt,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

sealed interface CategoryDeletionResult {
    data class Deleted(val categoryCount: Int, val deletedEventCount: Int) : CategoryDeletionResult
    data object NotFound : CategoryDeletionResult
    data object SystemCategory : CategoryDeletionResult
}
