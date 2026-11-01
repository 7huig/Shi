package io.github.huig7.shi.data.seed

/**
 * 首次建库时写入的默认分类。固定主键是为了在同一个事务里安全地写入父子关系，
 * 同时给「未分类」一个稳定 ID（删除分类时会连同事件一起删除，系统分类仅作兜底展示）。
 *
 * 默认顺序：工作（沟通、执行）→ 休息（睡眠、放空）→ 学习（输入、输出）→ 娱乐（沉浸、社交）。
 * 颜色取自 24 节气色板（[io.github.huig7.shi.domain.model.SolarTermColors]），不显示节气名称。
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
        SeedCategory(1, "工作", null, 0, 0xFF5A7FA8),
        SeedCategory(11, "执行", 1, 1, 0xFF3F5B78),
        SeedCategory(12, "沟通", 1, 0, 0xFF8A9AB8),
        SeedCategory(2, "休息", null, 1, 0xFF9E86B8),
        SeedCategory(21, "睡眠", 2, 0, 0xFF8B72A6),
        SeedCategory(22, "放空", 2, 1, 0xFF8A86A8),
        SeedCategory(3, "学习", null, 2, 0xFFC9A24E),
        SeedCategory(31, "输入", 3, 0, 0xFFB8933F),
        SeedCategory(32, "输出", 3, 1, 0xFFA98A5A),
        SeedCategory(4, "娱乐", null, 3, 0xFF94594A),
        SeedCategory(41, "社交", 4, 1, 0xFFC46E4A),
        SeedCategory(42, "沉浸", 4, 0, 0xFFA8463A),
        SeedCategory(5, "未分类", null, 4, 0xFF8C928C, isSystem = true),
    )
}
