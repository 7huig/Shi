package io.github.huig7.shi.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.huig7.shi.data.local.AppDatabase
import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.domain.model.ImportMode
import io.github.huig7.shi.domain.plan.CategoryPlan
import io.github.huig7.shi.domain.plan.FixedTimePlan
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 分类计划的保存与「下一次提醒」口径；间隔只由记录自动推进。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CategoryPlanRepositoryTest {

    private fun database(): AppDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private suspend fun addCategory(db: AppDatabase, name: String): Long =
        db.categoryDao().insert(
            CategoryEntity(name = name, parentId = null, sortOrder = 0, colorHex = 0xFF000001),
        )

    @Test
    fun intervalStartsFromNow() = runBlocking {
        val db = database()
        try {
            val categoryId = addCategory(db, "健康")
            val repository = TimeLogRepository(db)
            val saved = repository.saveCategoryPlan(
                CategoryPlan(
                    categoryId = categoryId,
                    intervalMinutes = 60,
                    intervalLabel = "起来活动",
                ),
                now = 1_000L,
            )
            assertEquals(1_000L + 3_600_000L, saved.nextReminderAt)

            val reloaded = repository.getCategoryPlan(categoryId)!!
            assertEquals(1_000L + 3_600_000L, reloaded.nextReminderAt)
        } finally {
            db.close()
        }
    }

    @Test
    fun editingLabelKeepsNextTimeButChangingIntervalResets() = runBlocking {
        val db = database()
        try {
            val categoryId = addCategory(db, "健康")
            val repository = TimeLogRepository(db)
            val first = repository.saveCategoryPlan(
                CategoryPlan(categoryId = categoryId, intervalMinutes = 60, intervalLabel = "喝水"),
                now = 1_000L,
            )
            assertEquals(1_000L + 3_600_000L, first.nextReminderAt)

            val renamed = repository.saveCategoryPlan(
                first.copy(intervalLabel = "喝水 / 站起来"),
                now = 2_000L,
            )
            assertEquals("改名不应重置下一次提醒", first.nextReminderAt, renamed.nextReminderAt)

            val changed = repository.saveCategoryPlan(
                renamed.copy(intervalMinutes = 30),
                now = 3_000L,
            )
            assertEquals(3_000L + 1_800_000L, changed.nextReminderAt)
        } finally {
            db.close()
        }
    }

    @Test
    fun turningEverythingOffDeletesThePlan() = runBlocking {
        val db = database()
        try {
            val categoryId = addCategory(db, "阅读")
            val repository = TimeLogRepository(db)
            repository.saveCategoryPlan(
                CategoryPlan(categoryId = categoryId, dailyGoalMinutes = 30),
                now = 1_000L,
            )
            repository.saveCategoryPlan(CategoryPlan(categoryId = categoryId), now = 2_000L)
            assertNull(repository.getCategoryPlan(categoryId))
            assertTrue(db.categoryPlanDao().getAll().isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun snapshotRestoreKeepsPlanAndNextReminder() = runBlocking {
        val db = database()
        try {
            val categoryId = addCategory(db, "健康")
            val repository = TimeLogRepository(db)
            val saved = repository.saveCategoryPlan(
                CategoryPlan(
                    categoryId = categoryId,
                    heatmapEnabled = true,
                    dailyGoalMinutes = 45,
                    fixedTimes = listOf(
                        FixedTimePlan(minuteOfDay = 22 * 60, label = "拉伸", leadMinutes = 10),
                    ),
                    intervalMinutes = 90,
                    intervalLabel = "喝水",
                ),
                now = 1_000L,
            )
            val snapshot = repository.snapshot()
            repository.restore(snapshot, ImportMode.REPLACE)

            val restored = repository.getCategoryPlan(categoryId)!!
            assertEquals(saved.heatmapEnabled, restored.heatmapEnabled)
            assertEquals(saved.dailyGoalMinutes, restored.dailyGoalMinutes)
            assertEquals(
                saved.fixedTimes.map { it.copy(id = 0L) },
                restored.fixedTimes.map { it.copy(id = 0L) },
            )
            assertEquals(saved.intervalMinutes, restored.intervalMinutes)
            assertEquals(saved.intervalLabel, restored.intervalLabel)
            assertEquals(saved.nextReminderAt, restored.nextReminderAt)
        } finally {
            db.close()
        }
    }

    @Test
    fun fixedTimesSupportMultipleEntriesAndLeadMinutes() = runBlocking {
        val db = database()
        try {
            val categoryId = addCategory(db, "健康")
            val repository = TimeLogRepository(db)
            repository.saveCategoryPlan(
                CategoryPlan(
                    categoryId = categoryId,
                    fixedTimes = listOf(
                        FixedTimePlan(minuteOfDay = 20 * 60, label = "复盘", leadMinutes = 5),
                        FixedTimePlan(minuteOfDay = 9 * 60, label = "晨间", leadMinutes = 15),
                    ),
                ),
                now = 1_000L,
            )

            val reloaded = repository.getCategoryPlan(categoryId)!!
            assertEquals(listOf("晨间", "复盘"), reloaded.fixedTimes.map { it.label })
            assertEquals(listOf(9 * 60, 20 * 60), reloaded.fixedTimes.map { it.minuteOfDay })
            assertEquals(listOf(15, 5), reloaded.fixedTimes.map { it.leadMinutes })
        } finally {
            db.close()
        }
    }

    @Test
    fun fixedOnlyPlanIsKeptUntilTurnedOff() = runBlocking {
        val db = database()
        try {
            val categoryId = addCategory(db, "阅读")
            val repository = TimeLogRepository(db)
            repository.saveCategoryPlan(
                CategoryPlan(
                    categoryId = categoryId,
                    fixedTimes = listOf(FixedTimePlan(minuteOfDay = 21 * 60, label = "读书")),
                ),
                now = 1_000L,
            )
            assertTrue(repository.getCategoryPlan(categoryId)!!.hasFixedTime)

            repository.saveCategoryPlan(CategoryPlan(categoryId = categoryId), now = 2_000L)
            assertNull(repository.getCategoryPlan(categoryId))
            assertTrue(db.fixedTimeDao().getAll().isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun deletingCategoryCascadesItsPlan() = runBlocking {
        val db = database()
        try {
            val first = addCategory(db, "工作")
            addCategory(db, "生活")
            val repository = TimeLogRepository(db)
            repository.saveCategoryPlan(
                CategoryPlan(
                    categoryId = first,
                    heatmapEnabled = true,
                    fixedTimes = listOf(FixedTimePlan(minuteOfDay = 8 * 60, label = "晨间")),
                ),
                now = 1_000L,
            )

            repository.deleteCategory(first)
            assertNull(repository.getCategoryPlan(first))
            assertTrue(db.categoryPlanDao().getAll().isEmpty())
            assertTrue(db.fixedTimeDao().getAll().isEmpty())
        } finally {
            db.close()
        }
    }
}
