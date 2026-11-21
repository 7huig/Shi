package io.github.huig7.shi.domain.model

/**
 * 二十四节气颜色：按「春夏秋冬各 6 个节气」排列，分类颜色选择器按 4 行 6 列展示，
 * 颜色下方标注节气名。这一版整体提高饱和度，让分类色块与热力图更有活力；
 * 深色模式复用同一组色值。
 */
data class SolarTermColor(
    val name: String,
    val season: String,
    val colorHex: Long,
)

val SolarTerms: List<SolarTermColor> = listOf(
    SolarTermColor("立春", "春", 0xFF4CAF50),
    SolarTermColor("雨水", "春", 0xFF26A69A),
    SolarTermColor("惊蛰", "春", 0xFF00A884),
    SolarTermColor("春分", "春", 0xFFEC6B8A),
    SolarTermColor("清明", "春", 0xFF8E5BD9),
    SolarTermColor("谷雨", "春", 0xFFD19A3E),
    SolarTermColor("立夏", "夏", 0xFF7CB342),
    SolarTermColor("小满", "夏", 0xFFF2A900),
    SolarTermColor("芒种", "夏", 0xFF9CB539),
    SolarTermColor("夏至", "夏", 0xFFF57C00),
    SolarTermColor("小暑", "夏", 0xFFF4512C),
    SolarTermColor("大暑", "夏", 0xFFD32F2F),
    SolarTermColor("立秋", "秋", 0xFFD4A017),
    SolarTermColor("处暑", "秋", 0xFFA45FD1),
    SolarTermColor("白露", "秋", 0xFF5C8DF6),
    SolarTermColor("秋分", "秋", 0xFFE8A317),
    SolarTermColor("寒露", "秋", 0xFF2F6FE4),
    SolarTermColor("霜降", "秋", 0xFFC0472B),
    SolarTermColor("立冬", "冬", 0xFF2E7D6E),
    SolarTermColor("小雪", "冬", 0xFF7E6BD9),
    SolarTermColor("大雪", "冬", 0xFF4C6FA8),
    SolarTermColor("冬至", "冬", 0xFF2C4E8A),
    SolarTermColor("小寒", "冬", 0xFF2F8FA3),
    SolarTermColor("大寒", "冬", 0xFF1F3A5F),
)

/** 四季顺序，供 4 行 6 列色板使用。 */
val SolarTermSeasons: List<Pair<String, List<SolarTermColor>>> =
    listOf("春", "夏", "秋", "冬").map { season -> season to SolarTerms.filter { it.season == season } }
