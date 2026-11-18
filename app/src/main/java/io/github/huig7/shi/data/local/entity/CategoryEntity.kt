package io.github.huig7.shi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parentId")],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    /**
     * v5 及更早版本用来标记系统「未分类」的遗留列。
     *
     * 系统分类已移除，业务代码不再读取该字段；保留列是为了避免一次有风险的表重建迁移，
     * 所有新分类都写入 false，v6 迁移也会把剩余行清零。
     */
    val isSystem: Boolean = false,
)
