package io.github.huig7.shi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.huig7.shi.data.local.entity.FixedTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedTimeDao {
    @Query("SELECT * FROM fixed_time ORDER BY minuteOfDay, id")
    fun observeAll(): Flow<List<FixedTimeEntity>>

    @Query("SELECT * FROM fixed_time ORDER BY minuteOfDay, id")
    suspend fun getAll(): List<FixedTimeEntity>

    @Query(
        "SELECT * FROM fixed_time WHERE categoryId = :categoryId ORDER BY minuteOfDay, id",
    )
    suspend fun getByCategory(categoryId: Long): List<FixedTimeEntity>

    @Insert
    suspend fun insert(item: FixedTimeEntity): Long

    @Query("DELETE FROM fixed_time WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: Long)

    @Query("DELETE FROM fixed_time")
    suspend fun deleteAll()
}
