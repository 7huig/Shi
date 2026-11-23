package io.github.huig7.shi.domain.plan

/**
 * 分类的长期计划：热力图、每日时长、固定时间、间隔待办。
 *
 * 四种配置可以同时存在。空值表示未启用，界面只根据这里的状态决定是否展示提示，
 * 不在别处再维护一份开关。
 */
data class CategoryPlan(
    val categoryId: Long,
    val heatmapEnabled: Boolean = false,
    val dailyGoalMinutes: Int = 0,
    /** 一天内可以设多个固定时间，按分钟数升序；空列表表示未启用。 */
    val fixedTimes: List<FixedTimePlan> = emptyList(),
    val intervalMinutes: Int? = null,
    val intervalLabel: String? = null,
    val nextReminderAt: Long? = null,
) {
    val hasDailyGoal: Boolean get() = dailyGoalMinutes > 0

    val hasFixedTime: Boolean get() = fixedTimes.isNotEmpty()

    val hasInterval: Boolean get() = intervalMinutes != null && intervalMinutes > 0

    val hasAnySetting: Boolean
        get() = heatmapEnabled || hasDailyGoal || hasFixedTime || hasInterval

    /** 每日时长对应的毫秒数，供进度条和进度文案共用。 */
    val dailyGoalMillis: Long get() = dailyGoalMinutes.coerceAtLeast(0) * 60_000L

    companion object {
        fun empty(categoryId: Long): CategoryPlan = CategoryPlan(categoryId = categoryId)
    }
}

/**
 * 一个固定时间。
 *
 * [leadMinutes] 表示提前多少分钟开始在首页提示；0 表示到点才提示。
 */
data class FixedTimePlan(
    val id: Long = 0L,
    val minuteOfDay: Int,
    val label: String? = null,
    val leadMinutes: Int = 0,
)

/** 首页静态提示的三种来源。 */
enum class PlanPromptKind { GOAL, FIXED_TIME, INTERVAL }

/**
 * 首页「今日提示」的一条卡片数据。
 *
 * [due] 只描述「现在是否值得看一眼」，不做通知、不响铃；记录发生后由数据层重新计算，
 * 卡片在下一次状态流更新时自然改成下一次时间。
 */
data class PlanPrompt(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: Long,
    val kind: PlanPromptKind,
    /** 卡片标题：每日时长用分类名，固定时间 / 间隔待办优先用用户填的事项名。 */
    val label: String,
    val due: Boolean,
    /** 每日时长：今天该分类（含子分类）已经记录的毫秒数。 */
    val goalDoneMillis: Long = 0L,
    /** 每日时长：目标毫秒数。 */
    val goalTargetMillis: Long = 0L,
    /** 固定时间：今天的分钟数（00:00 起算）。 */
    val fixedTimeMinuteOfDay: Int? = null,
    /** 固定时间：数据库里的行 id，用于同分类多个时间点的稳定 key。 */
    val fixedTimeId: Long? = null,
    /** 固定时间：提前多少分钟开始提示。 */
    val fixedTimeLeadMinutes: Int = 0,
    /** 固定时间：今天的事项是否已经有一条记录接上。 */
    val fixedDone: Boolean = false,
    /** 间隔待办：下一次提醒时间戳（已结合记录自动推算）。 */
    val nextReminderAt: Long? = null,
    /** 间隔待办：用户设置的间隔分钟数。 */
    val intervalMinutes: Int? = null,
    /** 生成这条提示时的「现在」，用来计算固定时间还剩几分钟。 */
    val nowMillis: Long = 0L,
) {
    /** LazyRow 的稳定 key：同一分类可以有多个固定时间。 */
    val key: String
        get() = when (kind) {
            PlanPromptKind.GOAL -> "goal-$categoryId"
            PlanPromptKind.INTERVAL -> "interval-$categoryId"
            PlanPromptKind.FIXED_TIME ->
                "fixed-$categoryId-${fixedTimeId ?: fixedTimeMinuteOfDay}"
        }
}
