package io.github.huig7.shi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.huig7.shi.data.local.entity.CategoryPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryPlanDao {
    @Query("SELECT * FROM category_plan")
    fun observeAll(): Flow<List<CategoryPlanEntity>>

    @Query("SELECT * FROM category_plan")
    suspend fun getAll(): List<CategoryPlanEntity>

    @Query("SELECT * FROM category_plan WHERE categoryId = :categoryId")
    suspend fun getById(categoryId: Long): CategoryPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: CategoryPlanEntity)

    @Query("DELETE FROM category_plan WHERE categoryId = :categoryId")
    suspend fun deleteById(categoryId: Long)

    @Query("DELETE FROM category_plan")
    suspend fun deleteAll()

    /** 完成一次间隔待办：记录完成时间，并把下一次提醒推到 [nextReminderAt]。 */
    @Query(
        "UPDATE category_plan SET lastCompletedAt = :now, nextReminderAt = :nextReminderAt, " +
            "updatedAt = :now WHERE categoryId = :categoryId",
    )
    suspend fun markCompleted(categoryId: Long, now: Long, nextReminderAt: Long): Int
}
