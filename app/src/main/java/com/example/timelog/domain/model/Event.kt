package com.example.timelog.domain.model

data class Event(
    val id: Long,
    val title: String,
    val note: String?,
    val timestamp: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

data class TimelineEvent(
    val event: Event,
    val categoryPath: String,
    val categoryColor: Long,
)
