package io.github.huig7.shi.ui.annual

import io.github.huig7.shi.domain.annual.AnnualReport

/**
 * 年度称号：按记录时段、周末占比、分类集中度等规则生成一句人设。
 *
 * 规则顺序有意从「最有辨识度的行为」排到「最普通的生活」，
 * 保证同一份数据只会落到一个称号里。
 */
internal data class AnnualPersona(
    val title: String,
    val tagline: String,
)

private const val MOVIE_MILLIS = 2 * 60 * 60 * 1000L
private const val WORKDAY_MILLIS = 8 * 60 * 60 * 1000L

internal fun annualPersona(report: AnnualReport): AnnualPersona {
    val total = report.eventCount.coerceAtLeast(1)
    val nightRatio = report.nightOwlCount.toFloat() / total
    val earlyRatio = report.earlyBirdCount.toFloat() / total
    val weekendRatio = report.weekendRatio ?: 0f
    val topShare = report.topCategoryShare ?: 0f
    val topName = report.roots.firstOrNull()?.name

    return when {
        nightRatio >= 0.25f -> AnnualPersona(
            title = "夜行记录者",
            tagline = "城市睡了，你还在给时间盖章。",
        )
        earlyRatio >= 0.35f -> AnnualPersona(
            title = "清晨掌舵人",
            tagline = "别人还在梦里，你已经把一天握在手里。",
        )
        weekendRatio >= 0.45f -> AnnualPersona(
            title = "周末生活家",
            tagline = "工作日属于别人，周末属于你。",
        )
        topShare >= 0.5f && topName != null -> AnnualPersona(
            title = "${topName}的长期主义者",
            tagline = "一年里，你把最多的时间交给了同一件事。",
        )
        report.activeDays >= 200 -> AnnualPersona(
            title = "记录成瘾者",
            tagline = "你几乎把每一天都留了底。",
        )
        report.eventCount <= 20 -> AnnualPersona(
            title = "偶尔记一笔的人",
            tagline = "不追求完整，只记录值得记住的。",
        )
        else -> AnnualPersona(
            title = "生活的观察者",
            tagline = "你把日子过成了可以回看的形状。",
        )
    }
}

/** 年度关键词：用时最多的三个根分类。 */
internal fun annualKeyword(report: AnnualReport): String =
    report.roots.take(3).joinToString(" · ") { it.name }.ifBlank { "还没有分类" }

/** 用电影和八小时工作换算总时长，让数字更有体感。 */
internal fun annualTimeEquivalent(report: AnnualReport): String {
    val movies = (report.totalMillis / MOVIE_MILLIS).toInt()
    val workdays = (report.totalMillis / WORKDAY_MILLIS).toInt()
    return when {
        movies >= 1 && workdays >= 1 -> "够看约 $movies 部电影，也够上 $workdays 个八小时班。"
        movies >= 1 -> "够看约 $movies 部电影。"
        else -> "还没到一部电影的长度。"
    }
}
