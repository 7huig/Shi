package com.example.timelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.timelog.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event ORDER BY startAt ASC, id ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT COALESCE(MAX(endAt), 0) FROM event")
    suspend fun latestEnd(): Long

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM event WHERE categoryId IN (:categoryIds)")
    suspend fun countByCategoryIds(categoryIds: List<Long>): Int

    @Query(
        "UPDATE event SET categoryId = :targetCategoryId, updatedAt = :now " +
            "WHERE categoryId IN (:categoryIds)",
    )
    suspend fun reassignCategory(categoryIds: List<Long>, targetCategoryId: Long, now: Long)
}
