package io.github.huig7.shi.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.huig7.shi.data.local.AppDatabase
import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.data.local.entity.EventEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

/** 删除分类的两种口径：连同事件删除，或保留事件并移到父级 / 未分类。 */
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
            val outcome = repository.deleteCategory(childId, moveEventsToParent = true)
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
    fun rootDeletionMovesRecordsToSystemCategory() = runBlocking {
        val db = database()
        try {
            val systemId = db.categoryDao().insert(
                CategoryEntity(name = "未分类", parentId = null, sortOrder = 0, colorHex = 0xFF8C928C, isSystem = true),
            )
            val rootId = db.categoryDao().insert(
                CategoryEntity(name = "阅读", parentId = null, sortOrder = 1, colorHex = 0xFF000003),
            )
            db.eventDao().insert(
                EventEntity(
                    note = null,
                    startAt = 1_000L,
                    endAt = 60_000L,
                    categoryId = rootId,
                    createdAt = 1L,
                    updatedAt = 1L,
                    uuid = "root-test",
                ),
            )
            val repository = TimeLogRepository(db)
            val outcome = repository.deleteCategory(rootId, moveEventsToParent = true)
                as CategoryDeletionResult.Deleted

            assertEquals(1, outcome.movedEventCount)
            assertEquals("未分类", outcome.movedToName)
            assertEquals(systemId, db.eventDao().getAll().single().categoryId)
        } finally {
            db.close()
        }
    }
}
