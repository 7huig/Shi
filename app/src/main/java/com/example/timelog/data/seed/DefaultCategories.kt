package com.example.timelog.data.seed

/**
 * 首次建库时写入的默认分类。固定主键是为了在同一个事务里安全地写入父子关系，
 * 同时给「未分类」一个稳定 ID，删除其它分类时可以把事件迁移过去。
 *
 * 默认顺序：工作（沟通、执行）→ 休息（睡眠、放空）→ 学习（输入、输出）→ 娱乐（沉浸、社交）。
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
        SeedCategory(11, "执行", 1, 1, 0xFF4C7BE8),
        SeedCategory(12, "沟通", 1, 0, 0xFF2F5DAF),
        SeedCategory(2, "休息", null, 1, 0xFF8A63C9),
        SeedCategory(21, "睡眠", 2, 0, 0xFF9C79D6),
        SeedCategory(22, "放空", 2, 1, 0xFF7150A8),
        SeedCategory(3, "学习", null, 2, 0xFFC07A2B),
        SeedCategory(31, "输入", 3, 0, 0xFFD99445),
        SeedCategory(32, "输出", 3, 1, 0xFF9E6220),
        SeedCategory(4, "娱乐", null, 3, 0xFFB4654A),
        SeedCategory(41, "社交", 4, 1, 0xFFCC7C5E),
        SeedCategory(42, "沉浸", 4, 0, 0xFF8E4A35),
        SeedCategory(5, "未分类", null, 4, 0xFF8C928C, isSystem = true),
    )
}
