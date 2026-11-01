package com.example.timelog.data.seed

/**
 * 首次建库时写入的默认分类。固定主键是为了在同一个事务里安全地写入父子关系，
 * 同时给「未分类」一个稳定 ID，删除其它分类时可以把事件迁移过去。
 */
data class SeedCategory(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    val isSystem: Boolean = false,
)

object DefaultCategories {
    val all: List<SeedCategory> = listOf(
        SeedCategory(1, "工作", null, 0, 0xFF3D6BDB),
        SeedCategory(11, "项目A", 1, 0, 0xFF4C7BE8),
        SeedCategory(12, "会议", 1, 1, 0xFF2F5DAF),
        SeedCategory(2, "生活", null, 1, 0xFF2F8F5B),
        SeedCategory(21, "家务", 2, 0, 0xFF4FAF78),
        SeedCategory(22, "购物", 2, 1, 0xFF277A4C),
        SeedCategory(3, "学习", null, 2, 0xFFC07A2B),
        SeedCategory(31, "阅读", 3, 0, 0xFFD99445),
        SeedCategory(32, "课程", 3, 1, 0xFF9E6220),
        SeedCategory(4, "休息", null, 3, 0xFF8A63C9),
        SeedCategory(41, "睡眠", 4, 0, 0xFF9C79D6),
        SeedCategory(42, "娱乐", 4, 1, 0xFF7150A8),
        SeedCategory(5, "未分类", null, 4, 0xFF8C928C, isSystem = true),
    )
}
