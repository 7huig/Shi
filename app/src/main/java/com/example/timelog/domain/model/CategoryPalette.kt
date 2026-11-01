package com.example.timelog.domain.model

/**
 * 按二十四节气顺序排列的 24 种颜色。界面只把它们当作色块 / 圆点使用，不显示节气名称。
 * 颜色整体压低饱和度，保证在纸色背景上与墨色文字同时成立；深色模式复用同一组色值。
 */
val SolarTermColors: List<Long> = listOf(
    0xFF7D9E6E, // 立春
    0xFF6E97A3, // 雨水
    0xFF5E9C90, // 惊蛰
    0xFFC48E8E, // 春分
    0xFF8B72A6, // 清明
    0xFFA98A5A, // 谷雨
    0xFF6E9A4C, // 立夏
    0xFFB8933F, // 小满
    0xFF8C9A46, // 芒种
    0xFFC9853F, // 夏至
    0xFFC46E4A, // 小暑
    0xFFA8463A, // 大暑
    0xFFB99A50, // 立秋
    0xFF9E86B8, // 处暑
    0xFF8A9AB8, // 白露
    0xFFC9A24E, // 秋分
    0xFF5A7FA8, // 寒露
    0xFF94594A, // 霜降
    0xFF4E7368, // 立冬
    0xFF8A86A8, // 小雪
    0xFF62788E, // 大雪
    0xFF3F5B78, // 冬至
    0xFF557E92, // 小寒
    0xFF35505F, // 大寒
)
