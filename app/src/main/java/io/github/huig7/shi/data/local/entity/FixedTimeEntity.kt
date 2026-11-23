package io.github.huig7.shi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 一个分类的固定时间。
 *
 * 一个分类一天可以有多条；删除分类时由外键级联删除。
 * [leadMinutes] 是提前提示的分钟数，0 表示到点才提示。
 */
@Entity(
    tableName = "fixed_time",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId")],
)
data class FixedTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Long,
    /** 从 00:00 起算的分钟数，0..1439。 */
    val minuteOfDay: Int,
    /** 事项名称；空时界面回退到分类名。 */
    val label: String? = null,
    /** 提前多少分钟开始提示。 */
    val leadMinutes: Int = 0,
)
