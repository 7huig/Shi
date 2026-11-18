package io.github.huig7.shi.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.huig7.shi.data.local.AppDatabase
import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.data.local.entity.EventEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** 搜索页：关键词与分类筛选（含子树）的组合口径。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchEventsTest {

    private fun database(): AppDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun blankKeywordWithCategoryListsWholeSubtree() = runBlocking {
        val db = database()
        try {
            val workId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 0, colorHex = 0xFF000001),
            )
            val childId = db.categoryDao().insert(
                CategoryEntity(name = "执行", parentId = workId, sortOrder = 0, colorHex = 0xFF000002),
            )
            val otherId = db.categoryDao().insert(
                CategoryEntity(name = "生活", parentId = null, sortOrder = 1, colorHex = 0xFF000003),
            )
            insertEvent(db, "写代码", childId, "uuid-1")
            insertEvent(db, "开会", workId, "uuid-2")
            insertEvent(db, "散步", otherId, "uuid-3")

            val repository = TimeLogRepository(db)
            val results = repository.searchEvents("", workId).first()

            assertEquals(2, results.size)
            assertEquals(setOf("写代码", "开会"), results.map { it.event.note }.toSet())
            assertEquals("工作 / 执行", results.first { it.event.note == "写代码" }.categoryPath)
        } finally {
            db.close()
        }
    }

    @Test
    fun keywordAndCategoryFiltersCombine() = runBlocking {
        val db = database()
        try {
            val workId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 0, colorHex = 0xFF000001),
            )
            val childId = db.categoryDao().insert(
                CategoryEntity(name = "执行", parentId = workId, sortOrder = 0, colorHex = 0xFF000002),
            )
            val otherId = db.categoryDao().insert(
                CategoryEntity(name = "生活", parentId = null, sortOrder = 1, colorHex = 0xFF000003),
            )
            insertEvent(db, "写代码", childId, "uuid-1")
            insertEvent(db, "开会", workId, "uuid-2")
            insertEvent(db, "写日记", otherId, "uuid-3")

            val repository = TimeLogRepository(db)
            val results = repository.searchEvents("写", workId).first()

            assertEquals(listOf("写代码"), results.map { it.event.note })
        } finally {
            db.close()
        }
    }

    @Test
    fun blankKeywordWithoutCategoryStaysEmpty() = runBlocking {
        val db = database()
        try {
            val categoryId = db.categoryDao().insert(
                CategoryEntity(name = "工作", parentId = null, sortOrder = 0, colorHex = 0xFF000001),
            )
            insertEvent(db, "写代码", categoryId, "uuid-1")
            val repository = TimeLogRepository(db)

            assertEquals(0, repository.searchEvents("", null).first().size)
        } finally {
            db.close()
        }
    }

    private suspend fun insertEvent(
        db: AppDatabase,
        note: String,
        categoryId: Long,
        uuid: String,
    ) {
        db.eventDao().insert(
            EventEntity(
                note = note,
                startAt = 1_000L,
                endAt = 2_000L,
                categoryId = categoryId,
                createdAt = 1L,
                updatedAt = 1L,
                uuid = uuid,
            ),
        )
    }
}
