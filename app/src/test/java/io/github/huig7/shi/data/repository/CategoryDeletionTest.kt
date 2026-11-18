package io.github.huig7.shi.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.huig7.shi.data.local.AppDatabase
import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.data.local.entity.EventEntity
import io.github.huig7.shi.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

/** 删除分类的两种口径：连同事件删除，或保留事件并移到调用方指定的分类。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CategoryDeletionTest {

    private fun database(): AppDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun moveEventsToParentKeepsRecords() = runBlocking {
        val db = database()
        try {
            val parentId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 0, colorHex = 0xFF000001),
            )
            val childId = db.categoryDao().insert(
                CategoryEntity(name = "执行", parentId = parentId, sortOrder = 0, colorHex = 0xFF000002),
            )
            db.eventDao().insert(
                EventEntity(
                    note = "写代码",
                    startAt = 1_000L,
                    endAt = 3_600_000L,
                    categoryId = childId,
                    createdAt = 1L,
                    updatedAt = 1L,
                    uuid = "move-test",
                ),
            )
            val repository = TimeLogRepository(db)
            val outcome = repository.deleteCategory(childId, moveEventsToId = parentId)
                as CategoryDeletionResult.Deleted

            assertEquals(0, outcome.deletedEventCount)
            assertEquals(1, outcome.movedEventCount)
            assertEquals("工作", outcome.movedToName)
            assertEquals(parentId, db.eventDao().getAll().single().categoryId)
            assertNull(db.categoryDao().getById(childId))
        } finally {
            db.close()
        }
    }

    @Test
    fun rootDeletionWithChosenTargetMovesRecords() = runBlocking {
        val db = database()
        try {
            val readingId = db.categoryDao().insert(
                CategoryEntity(name = "阅读", parentId = null, sortOrder = 0, colorHex = 0xFF000003),
            )
            val workId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 1, colorHex = 0xFF000004),
            )
            db.eventDao().insert(
                EventEntity(
                    note = null,
                    startAt = 1_000L,
                    endAt = 60_000L,
                    categoryId = readingId,
                    createdAt = 1L,
                    updatedAt = 1L,
                    uuid = "root-move-test",
                ),
            )
            val repository = TimeLogRepository(db)
            val outcome = repository.deleteCategory(readingId, moveEventsToId = workId)
                as CategoryDeletionResult.Deleted

            assertEquals(1, outcome.movedEventCount)
            assertEquals("工作", outcome.movedToName)
            assertEquals(workId, db.eventDao().getAll().single().categoryId)
            assertNull(db.categoryDao().getById(readingId))
        } finally {
            db.close()
        }
    }

    @Test
    fun rootDeletionWithoutTargetDeletesEvents() = runBlocking {
        val db = database()
        try {
            val readingId = db.categoryDao().insert(
                CategoryEntity(name = "阅读", parentId = null, sortOrder = 0, colorHex = 0xFF000003),
            )
            db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 1, colorHex = 0xFF000004),
            )
            db.eventDao().insert(
                EventEntity(
                    note = "小说",
                    startAt = 1_000L,
                    endAt = 60_000L,
                    categoryId = readingId,
                    createdAt = 1L,
                    updatedAt = 1L,
                    uuid = "root-delete-test",
                ),
            )
            val repository = TimeLogRepository(db)
            val outcome = repository.deleteCategory(readingId, moveEventsToId = null)
                as CategoryDeletionResult.Deleted

            assertEquals(1, outcome.deletedEventCount)
            assertEquals(0, outcome.movedEventCount)
            assertEquals(0, db.eventDao().getAll().size)
            assertNull(db.categoryDao().getById(readingId))
        } finally {
            db.close()
        }
    }

    @Test
    fun lastCategoryCannotBeDeleted() = runBlocking {
        val db = database()
        try {
            val onlyId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 0, colorHex = 0xFF000001),
            )
            db.eventDao().insert(
                EventEntity(
                    note = null,
                    startAt = 1_000L,
                    endAt = 60_000L,
                    categoryId = onlyId,
                    createdAt = 1L,
                    updatedAt = 1L,
                    uuid = "last-test",
                ),
            )
            val repository = TimeLogRepository(db)
            val outcome = repository.deleteCategory(onlyId, moveEventsToId = null)

            assertEquals(CategoryDeletionResult.LastCategory, outcome)
            assertEquals(1, db.eventDao().getAll().size)
            assertEquals(onlyId, db.categoryDao().getById(onlyId)?.id)
        } finally {
            db.close()
        }
    }

    @Test
    fun restoringEventFailsWhenItsCategoryWasDeleted() = runBlocking {
        val db = database()
        try {
            val categoryId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 0, colorHex = 0xFF000001),
            )
            val repository = TimeLogRepository(db)
            // 事件还没入库，分类就被删掉了；撤销删除不能凭空造一个「未分类」。
            db.categoryDao().deleteById(categoryId)
            val restored = repository.restoreEvent(
                Event(
                    id = 1L,
                    note = "写代码",
                    startAt = 1_000L,
                    endAt = 2_000L,
                    categoryId = categoryId,
                    createdAt = 1L,
                    updatedAt = 1L,
                    uuid = "restore-missing-category",
                ),
            )

            assertEquals(false, restored)
            assertEquals(0, db.eventDao().getAll().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun invalidMoveTargetIsRejectedWithoutDeletingAnything() = runBlocking {
        val db = database()
        try {
            val rootId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 0, colorHex = 0xFF000001),
            )
            val childId = db.categoryDao().insert(
                CategoryEntity(name = "执行", parentId = rootId, sortOrder = 0, colorHex = 0xFF000002),
            )
            // 另留一个根分类，避免先触发「至少保留一个分类」的兜底规则。
            db.categoryDao().insert(
                CategoryEntity(name = "生活", parentId = null, sortOrder = 1, colorHex = 0xFF000005),
            )
            db.eventDao().insert(
                EventEntity(
                    note = null,
                    startAt = 1_000L,
                    endAt = 60_000L,
                    categoryId = rootId,
                    createdAt = 1L,
                    updatedAt = 1L,
                    uuid = "invalid-target-test",
                ),
            )
            val repository = TimeLogRepository(db)
            val outcome = repository.deleteCategory(rootId, moveEventsToId = childId)

            assertEquals(CategoryDeletionResult.InvalidMoveTarget, outcome)
            assertEquals(1, db.eventDao().getAll().size)
            assertEquals(rootId, db.categoryDao().getById(rootId)?.id)
        } finally {
            db.close()
        }
    }
}
