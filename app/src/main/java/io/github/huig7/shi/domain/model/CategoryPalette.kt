package io.github.huig7.shi.domain.model

/**
 * 二十四节气颜色：按「春夏秋冬各 6 个节气」排列，分类颜色选择器按 4 行 6 列展示，
 * 颜色下方标注节气名。颜色整体压低饱和度，保证在纸色背景上与墨色文字同时成立；
 * 深色模式复用同一组色值。
 */
data class SolarTermColor(
    val name: String,
    val season: String,
    val colorHex: Long,
)

val SolarTerms: List<SolarTermColor> = listOf(
    SolarTermColor("立春", "春", 0xFF7D9E6E),
    SolarTermColor("雨水", "春", 0xFF6E97A3),
    SolarTermColor("惊蛰", "春", 0xFF5E9C90),
    SolarTermColor("春分", "春", 0xFFC48E8E),
    SolarTermColor("清明", "春", 0xFF8B72A6),
    SolarTermColor("谷雨", "春", 0xFFA98A5A),
    SolarTermColor("立夏", "夏", 0xFF6E9A4C),
    SolarTermColor("小满", "夏", 0xFFB8933F),
    SolarTermColor("芒种", "夏", 0xFF8C9A46),
    SolarTermColor("夏至", "夏", 0xFFC9853F),
    SolarTermColor("小暑", "夏", 0xFFC46E4A),
    SolarTermColor("大暑", "夏", 0xFFA8463A),
    SolarTermColor("立秋", "秋", 0xFFB99A50),
    SolarTermColor("处暑", "秋", 0xFF9E86B8),
    SolarTermColor("白露", "秋", 0xFF8A9AB8),
    SolarTermColor("秋分", "秋", 0xFFC9A24E),
    SolarTermColor("寒露", "秋", 0xFF5A7FA8),
    SolarTermColor("霜降", "秋", 0xFF94594A),
    SolarTermColor("立冬", "冬", 0xFF4E7368),
    SolarTermColor("小雪", "冬", 0xFF8A86A8),
    SolarTermColor("大雪", "冬", 0xFF62788E),
    SolarTermColor("冬至", "冬", 0xFF3F5B78),
    SolarTermColor("小寒", "冬", 0xFF557E92),
    SolarTermColor("大寒", "冬", 0xFF35505F),
)

/** 兼容旧接口：仅取色值。 */
val SolarTermColors: List<Long> = SolarTerms.map { it.colorHex }

/** 四季顺序，供 4 行 6 列色板使用。 */
val SolarTermSeasons: List<Pair<String, List<SolarTermColor>>> =
    listOf("春", "夏", "秋", "冬").map { season -> season to SolarTerms.filter { it.season == season } }
