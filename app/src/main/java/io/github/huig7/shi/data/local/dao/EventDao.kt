package io.github.huig7.shi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.huig7.shi.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event ORDER BY startAt ASC, id ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event ORDER BY startAt ASC, id ASC")
    suspend fun getAll(): List<EventEntity>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM event WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): EventEntity?

    @Query("SELECT COALESCE(MAX(endAt), 0) FROM event")
    suspend fun latestEnd(): Long

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM event")
    suspend fun deleteAll()

    @Query("DELETE FROM event WHERE categoryId IN (:categoryIds)")
    suspend fun deleteByCategoryIds(categoryIds: List<Long>): Int

    @Query("SELECT COUNT(*) FROM event WHERE categoryId IN (:categoryIds)")
    suspend fun countByCategoryIds(categoryIds: List<Long>): Int

    /** 与 [startAt, endAt) 有交集的事件；excludeId 用来排除正在编辑的那条。 */
    @Query(
        "SELECT * FROM event " +
            "WHERE startAt < :endAt AND endAt > :startAt AND id != :excludeId " +
            "ORDER BY startAt ASC, id ASC",
    )
    suspend fun getOverlapping(startAt: Long, endAt: Long, excludeId: Long): List<EventEntity>

    @Query(
        "SELECT * FROM event " +
            "WHERE note IS NOT NULL AND instr(lower(note), lower(:query)) > 0 " +
            "ORDER BY startAt DESC, id DESC",
    )
    fun searchByNote(query: String): Flow<List<EventEntity>>
}
