package io.github.huig7.shi.domain.model

data class Category(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    val isSystem: Boolean = false,
)
