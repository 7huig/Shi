package com.example.timelog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 事件是一段时间块：startAt / endAt 是同一时区下的 epoch millis。
 * 标题已被移除，只保留可留空的备注。
 */
@Entity(
    tableName = "event",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("startAt")],
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
)
