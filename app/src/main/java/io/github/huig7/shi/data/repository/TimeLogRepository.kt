package io.github.huig7.shi.data.repository

import androidx.room.withTransaction
import io.github.huig7.shi.data.local.AppDatabase
import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.data.local.entity.EventEntity
import io.github.huig7.shi.domain.OverlapPlan
import io.github.huig7.shi.domain.model.Category
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.Event
import io.github.huig7.shi.domain.model.ExportedCategory
import io.github.huig7.shi.domain.model.ExportedEvent
import io.github.huig7.shi.domain.model.ImportMode
import io.github.huig7.shi.domain.model.LedgerSnapshot
import io.github.huig7.shi.domain.model.ReparentCheck
import io.github.huig7.shi.domain.model.TimelineEvent
import io.github.huig7.shi.domain.model.buildCategoryTree
import io.github.huig7.shi.domain.model.checkReparent
import io.github.huig7.shi.domain.model.legacyIdentity
import io.github.huig7.shi.domain.model.legacyUuid
import io.github.huig7.shi.domain.resolveOverlap
import java.util.UUID
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
        events.toTimeline(buildCategoryTree(categories.map { it.toDomain() }))
    }

    suspend fun getEvent(id: Long): Event? = eventDao.getById(id)?.toDomain()

    suspend fun latestEventEnd(): Long = eventDao.latestEnd()

    /** 最近一条记录的分类；没有记录时为 null。 */
    suspend fun latestCategoryId(): Long? = eventDao.latestCategoryId()

    /**
     * 按备注搜索，并可选按分类筛选（含该分类的整棵子树）。
     *
     * 关键词与分类都为空时直接返回空列表，不查库；只选分类、不输关键词时列出该分类下的全部记录。
     */
    fun searchEvents(query: String, categoryId: Long? = null): Flow<List<TimelineEvent>> {
        val keyword = query.trim()
        if (keyword.isEmpty() && categoryId == null) return flowOf(emptyList())
        return combine(
            eventDao.search(keyword),
            categoryDao.observeAll(),
        ) { entities, categories ->
            val tree = buildCategoryTree(categories.map { it.toDomain() })
            val categoryIds = categoryId?.let { tree.subtreeIds(it).toSet() }
            val filtered = if (categoryIds == null) {
                entities
            } else {
                entities.filter { it.categoryId in categoryIds }
            }
            filtered.toTimeline(tree)
        }
    }

    suspend fun getOverlappingEvents(startAt: Long, endAt: Long, excludeId: Long): List<Event> =
        eventDao.getOverlapping(startAt, endAt, excludeId).map { it.toDomain() }

    /**
     * 保存正在编辑的区间，并按 [plan] 让其它重叠记录让路。
     * 事务内重新查询并比较方案；确认期间发生冲突变化时不写入任何内容。
     */
    suspend fun saveEventResolved(
        id: Long?,
        note: String?,
        startAt: Long,
        endAt: Long,
        categoryId: Long,
        plan: OverlapPlan,
    ): SaveEventResult = database.withTransaction {
        if (id != null && eventDao.getById(id) == null) {
            return@withTransaction SaveEventResult.EventNotFound
        }
        val currentOthers = eventDao.getOverlapping(startAt, endAt, id ?: -1L).map { it.toDomain() }
        if (resolveOverlap(startAt, endAt, currentOthers) != plan) {
            return@withTransaction SaveEventResult.OverlapChanged
        }

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
                    uuid = UUID.randomUUID().toString(),
                ),
            )
        }

        if (id == null) {
            val newId = eventDao.insert(
                EventEntity(
                    note = cleanNote,
                    startAt = startAt,
                    endAt = safeEndAt,
                    categoryId = categoryId,
                    createdAt = now,
                    updatedAt = now,
                    uuid = UUID.randomUUID().toString(),
                ),
            )
            SaveEventResult.Saved(newId)
        } else {
            val existing = eventDao.getById(id)!!
            eventDao.update(
                existing.copy(
                    note = cleanNote,
                    startAt = startAt,
                    endAt = safeEndAt,
                    categoryId = categoryId,
                    updatedAt = now,
                ),
            )
            SaveEventResult.Saved(id)
        }
    }

    suspend fun deleteEvent(id: Long) {
        eventDao.deleteById(id)
    }

    /**
     * 撤销删除：按新 id 重新插入同一条记录。
     *
     * 不重新走重叠裁剪，撤销只应该把用户刚删的那一条原样放回去。
     * 分类若在删除记录之后也被删掉，已经没有「未分类」可以兜底，返回 false 由界面提示撤销失败。
     */
    suspend fun restoreEvent(event: Event): Boolean = database.withTransaction {
        val categoryId = event.categoryId.takeIf { categoryDao.getById(it) != null }
            ?: return@withTransaction false
        val now = System.currentTimeMillis()
        eventDao.insert(
            EventEntity(
                note = event.note.cleanNote(),
                startAt = event.startAt,
                endAt = event.endAt.coerceAtLeast(event.startAt + 60_000L),
                categoryId = categoryId,
                createdAt = event.createdAt,
                updatedAt = now,
                uuid = UUID.randomUUID().toString(),
            ),
        )
        true
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

    /**
     * 保存分类编辑：名称、颜色，以及可选的父级调整。
     *
     * 父级变化时复用 [CategoryTree.checkReparent] 的规则（防环、最多 3 层），
     * 通过后挂到新父级末尾；返回 [ReparentCheck.Ok] 表示保存成功。
     */
    suspend fun updateCategory(
        id: Long,
        name: String,
        colorHex: Long,
        parentId: Long?,
    ): ReparentCheck = database.withTransaction {
        val tree = buildCategoryTree(categoryDao.getAll().map { it.toDomain() })
        val node = tree.find(id) ?: return@withTransaction ReparentCheck.NotFound
        if (parentId != node.category.parentId) {
            when (val check = tree.checkReparent(id, parentId)) {
                ReparentCheck.Ok -> Unit
                else -> return@withTransaction check
            }
        }
        val existing = categoryDao.getById(id) ?: return@withTransaction ReparentCheck.NotFound
        val updated = if (parentId == existing.parentId) {
            existing.copy(name = name.trim(), colorHex = colorHex)
        } else {
            existing.copy(
                name = name.trim(),
                colorHex = colorHex,
                parentId = parentId,
                sortOrder = categoryDao.maxSortOrder(parentId) + 1,
            )
        }
        categoryDao.update(updated)
        ReparentCheck.Ok
    }

    /**
     * 同级拖动排序：把 [categoryId] 移动到同一个父分类下的 [targetIndex] 位置。
     * 重写这一组兄弟的 sortOrder 为 0..n-1，保证与列表展示顺序一致（列表按 sortOrder, id 排序）。
     */
    suspend fun moveCategory(categoryId: Long, targetIndex: Int) = database.withTransaction {
        val moving = categoryDao.getById(categoryId) ?: return@withTransaction
        val siblings = categoryDao.getSiblings(moving.parentId)
        val from = siblings.indexOfFirst { it.id == categoryId }
        if (from < 0) return@withTransaction
        val to = targetIndex.coerceIn(0, siblings.lastIndex)
        if (to == from) return@withTransaction
        val reordered = siblings.toMutableList().apply {
            removeAt(from)
            add(to, moving)
        }
        reordered.forEachIndexed { index, entity ->
            if (entity.sortOrder != index) {
                categoryDao.update(entity.copy(sortOrder = index))
            }
        }
    }

    suspend fun countEventsInSubtree(categoryId: Long): Int {
        val tree = buildCategoryTree(categoryDao.getAll().map { it.toDomain() })
        val ids = tree.subtreeIds(categoryId)
        return if (ids.isEmpty()) 0 else eventDao.countByCategoryIds(ids)
    }

    /**
     * 删除分类及其子分类。
     *
     * [moveEventsToId] 为 null 时一并删除子树内的事件；非 null 时保留事件并改挂到该分类
     * （根分类删除时由界面让用户选择目标分类）。目标不能落在被删除的子树里。
     *
     * 至少保留一个分类：否则删除后没有分类可记录，且已经没有系统兜底分类。
     * 事件外键是 RESTRICT，必须先处理事件，才能删除分类；子分类由自引用外键 CASCADE 清理。
     */
    suspend fun deleteCategory(
        categoryId: Long,
        moveEventsToId: Long? = null,
    ): CategoryDeletionResult = database.withTransaction {
        val all = categoryDao.getAll().map { it.toDomain() }
        val tree = buildCategoryTree(all)
        tree.find(categoryId) ?: return@withTransaction CategoryDeletionResult.NotFound

        val ids = tree.subtreeIds(categoryId)
        if (ids.size == all.size) return@withTransaction CategoryDeletionResult.LastCategory

        var movedEventCount = 0
        var movedToName: String? = null
        var deletedEventCount = 0
        if (ids.isNotEmpty()) {
            if (moveEventsToId == null) {
                deletedEventCount = eventDao.deleteByCategoryIds(ids)
            } else {
                val targetExists = moveEventsToId !in ids &&
                    categoryDao.getById(moveEventsToId) != null
                if (!targetExists) {
                    return@withTransaction CategoryDeletionResult.InvalidMoveTarget
                }
                movedEventCount = eventDao.reassignByCategoryIds(
                    ids,
                    moveEventsToId,
                    System.currentTimeMillis(),
                )
                movedToName = tree.find(moveEventsToId)?.category?.name
                    ?: categoryDao.getById(moveEventsToId)?.name
            }
        }
        categoryDao.deleteById(categoryId)
        CategoryDeletionResult.Deleted(
            categoryCount = ids.size,
            deletedEventCount = deletedEventCount,
            movedEventCount = movedEventCount,
            movedToName = movedToName,
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

        // 旧备份可能带着空的系统「未分类」，不再恢复它；被事件引用的系统分类则保留为普通分类。
        val usedCategoryIds = snapshot.events.mapTo(HashSet()) { it.categoryId }
        val idMap = mutableMapOf<Long, Long>()
        orderByDepth(snapshot.categories).forEach { category ->
            if (category.isSystem && category.id !in usedCategoryIds) return@forEach
            val parentNewId = category.parentId?.let(idMap::get)
            val entity = CategoryEntity(
                id = category.id.coerceAtLeast(0L),
                name = category.name.trim(),
                parentId = parentNewId,
                sortOrder = category.sortOrder,
                colorHex = category.colorHex,
                isSystem = false,
            )
            val newId = categoryDao.insert(entity)
            idMap[category.id] = newId
        }
        insertImportedEvents(snapshot.events, idMap, skipExisting = false)
    }

    private suspend fun restoreMerge(snapshot: LedgerSnapshot) {
        val existing = categoryDao.getAll().toMutableList()
        val usedCategoryIds = snapshot.events.mapTo(HashSet()) { it.categoryId }
        val idMap = mutableMapOf<Long, Long>()

        orderByDepth(snapshot.categories).forEach { category ->
            if (category.isSystem && category.id !in usedCategoryIds) return@forEach
            val parentNewId = category.parentId?.let(idMap::get)
            val reused = existing.firstOrNull {
                it.name == category.name.trim() && it.parentId == parentNewId
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
                        isSystem = false,
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
                        isSystem = false,
                    ),
                )
            }
        }
        insertImportedEvents(snapshot.events, idMap, skipExisting = true)
    }

    private suspend fun insertImportedEvents(
        events: List<ExportedEvent>,
        idMap: Map<Long, Long>,
        skipExisting: Boolean,
    ) {
        val legacyOccurrences = mutableMapOf<String, Int>()
        events.forEach { event ->
            // 正常导出的备份一定包含事件引用的分类；缺失说明文件不完整，宁可整次导入回滚也不静默改分类。
            val categoryId = idMap[event.categoryId]
                ?: throw IllegalArgumentException("备份文件缺少事件对应的分类")
            val uuid = event.uuid?.takeIf { it.isNotBlank() } ?: run {
                val identity = event.legacyIdentity()
                val occurrence = legacyOccurrences[identity] ?: 0
                legacyOccurrences[identity] = occurrence + 1
                event.legacyUuid(occurrence)
            }
            if (skipExisting && eventDao.getByUuid(uuid) != null) return@forEach
            val startAt = event.startAt
            val endAt = event.endAt.coerceAtLeast(startAt + 60_000L)
            eventDao.insert(
                EventEntity(
                    note = event.note.cleanNote(),
                    startAt = startAt,
                    endAt = endAt,
                    categoryId = categoryId,
                    createdAt = event.createdAt,
                    updatedAt = event.updatedAt,
                    uuid = uuid,
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

    /** 时间轴 / 搜索共用的展示映射：补分类路径与颜色，分类缺失时用兜底灰。 */
    private fun List<EventEntity>.toTimeline(tree: CategoryTree): List<TimelineEvent> =
        map { entity ->
            val event = entity.toDomain()
            TimelineEvent(
                event = event,
                categoryPath = tree.pathOf(event.categoryId),
                categoryColor = tree.find(event.categoryId)?.category?.colorHex ?: 0xFF9AA29A,
            )
        }

    private fun CategoryEntity.toDomain(): Category = Category(
        id = id,
        name = name,
        parentId = parentId,
        sortOrder = sortOrder,
        colorHex = colorHex,
    )

    private fun EventEntity.toDomain(): Event = Event(
        id = id,
        note = note,
        startAt = startAt,
        endAt = endAt,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        uuid = uuid,
    )

    private fun CategoryEntity.toExport(): ExportedCategory = ExportedCategory(
        id = id,
        name = name,
        parentId = parentId,
        sortOrder = sortOrder,
        colorHex = colorHex,
        // isSystem 只为兼容旧备份格式保留；新导出的分类都是普通分类。
        isSystem = false,
    )

    private fun EventEntity.toExport(): ExportedEvent = ExportedEvent(
        note = note,
        startAt = startAt,
        endAt = endAt,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        uuid = uuid,
    )
}


