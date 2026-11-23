package io.github.huig7.shi.domain.plan

import io.github.huig7.shi.domain.MINUTE
import io.github.huig7.shi.domain.StatisticsTestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 首页「今日提示」的口径：目标含子树、固定时间提前量与多时间点、间隔待办跨天自动刷新。 */
class PlanPromptBuilderTest : StatisticsTestFixtures() {

    private fun prompts(
        vararg plans: CategoryPlan,
        events: List<io.github.huig7.shi.domain.model.TimelineEvent> = emptyList(),
        now: Long = nowMillis,
    ): List<PlanPrompt> = PlanPromptBuilder.build(
        plans = plans.toList(),
        tree = tree(),
        events = events,
        now = now,
        zone = zone,
    )

    private fun fixedPrompt(
        minuteOfDay: Int,
        leadMinutes: Int = 0,
        label: String = "晨间计划",
    ): CategoryPlan = CategoryPlan(
        categoryId = 1,
        fixedTimes = listOf(
            FixedTimePlan(
                minuteOfDay = minuteOfDay,
                label = label,
                leadMinutes = leadMinutes,
            ),
        ),
    )

    @Test
    fun `每日时长包含子分类的记录`() {
        val events = listOf(
            event(1, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 3, path = "工作 / 执行 / 编码"),
            event(2, "2026-11-01T10:00", "2026-11-01T10:30", categoryId = 1),
        )
        val goal = prompts(
            CategoryPlan(categoryId = 1, dailyGoalMinutes = 120),
            events = events,
        ).single()

        assertEquals(PlanPromptKind.GOAL, goal.kind)
        assertEquals(90 * MINUTE, goal.goalDoneMillis)
        assertEquals(120 * MINUTE, goal.goalTargetMillis)
        assertTrue("还差 30 分钟，应该仍在进行中", goal.due)
    }

    @Test
    fun `达到目标后不再催促`() {
        val events = listOf(
            event(1, "2026-11-01T09:00", "2026-11-01T11:00", categoryId = 1),
        )
        val goal = prompts(
            CategoryPlan(categoryId = 1, dailyGoalMinutes = 120),
            events = events,
        ).single()

        assertFalse(goal.due)
        assertEquals(120 * MINUTE, goal.goalDoneMillis)
    }

    @Test
    fun `固定时间未到只预告不到点`() {
        val prompt = prompts(fixedPrompt(23 * 60, label = "冥想")).single()

        assertEquals("冥想", prompt.label)
        assertFalse(prompt.due)
        assertFalse(prompt.fixedDone)
    }

    @Test
    fun `固定时间已到且没有记录时提示`() {
        val prompt = prompts(fixedPrompt(9 * 60)).single()

        assertTrue(prompt.due)
        assertFalse(prompt.fixedDone)
    }

    @Test
    fun `固定时间之后已有记录视为完成`() {
        val events = listOf(
            event(1, "2026-11-01T09:10", "2026-11-01T09:40", categoryId = 1),
        )
        val prompt = prompts(fixedPrompt(9 * 60), events = events).single()

        assertTrue(prompt.fixedDone)
        assertFalse(prompt.due)
    }

    @Test
    fun `提前量决定什么时候开始提示`() {
        fun promptAt(now: String): PlanPrompt = prompts(
            fixedPrompt(9 * 60, leadMinutes = 15),
            now = at(now),
        ).single()

        assertFalse("08:44 还没进入提前 15 分钟窗口", promptAt("2026-11-01T08:44").due)
        assertTrue("08:45 正好进入窗口", promptAt("2026-11-01T08:45").due)
    }

    @Test
    fun `同一分类可以设置多个固定时间`() {
        val plan = CategoryPlan(
            categoryId = 1,
            fixedTimes = listOf(
                FixedTimePlan(id = 1, minuteOfDay = 9 * 60, label = "晨间计划"),
                FixedTimePlan(id = 2, minuteOfDay = 15 * 60, label = "复盘"),
            ),
        )
        val result = prompts(plan)

        assertEquals(2, result.size)
        assertTrue(result.first { it.fixedTimeMinuteOfDay == 9 * 60 }.due)
        assertFalse(result.first { it.fixedTimeMinuteOfDay == 15 * 60 }.due)
        assertEquals(2, result.map { it.key }.toSet().size)
    }

    @Test
    fun `前一个固定时间的记录不会算到后一个时段`() {
        val plan = CategoryPlan(
            categoryId = 1,
            fixedTimes = listOf(
                FixedTimePlan(id = 1, minuteOfDay = 9 * 60, label = "晨间计划"),
                FixedTimePlan(id = 2, minuteOfDay = 15 * 60, label = "复盘"),
            ),
        )
        val events = listOf(
            event(1, "2026-11-01T15:10", "2026-11-01T15:20", categoryId = 1),
        )
        val result = prompts(plan, events = events, now = at("2026-11-01T16:00"))

        assertFalse(result.first { it.fixedTimeMinuteOfDay == 9 * 60 }.fixedDone)
        assertTrue(result.first { it.fixedTimeMinuteOfDay == 15 * 60 }.fixedDone)
    }

    @Test
    fun `间隔待办未到下一次时间时不催`() {
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                intervalMinutes = 60,
                intervalLabel = "起来活动",
                nextReminderAt = nowMillis + 30 * MINUTE,
            ),
        ).single()

        assertEquals(PlanPromptKind.INTERVAL, prompt.kind)
        assertFalse(prompt.due)
        assertEquals(nowMillis + 30 * MINUTE, prompt.nextReminderAt)
    }

    @Test
    fun `间隔待办到点后提示`() {
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                intervalMinutes = 60,
                intervalLabel = "起来活动",
                nextReminderAt = nowMillis - MINUTE,
            ),
        ).single()

        assertTrue(prompt.due)
    }

    @Test
    fun `旧计划没有下一次时间时按现在到期处理`() {
        val prompt = prompts(
            CategoryPlan(categoryId = 1, intervalMinutes = 60),
        ).single()

        assertTrue(prompt.due)
        assertEquals(nowMillis, prompt.nextReminderAt)
    }

    @Test
    fun `当天记录会自动推进间隔待办`() {
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                intervalMinutes = 60,
                intervalLabel = "起来活动",
                nextReminderAt = nowMillis - MINUTE,
            ),
            events = listOf(
                event(1, "2026-11-01T11:30", "2026-11-01T11:45", categoryId = 1),
            ),
        ).single()

        assertFalse("11:30 刚记录过，不该继续催", prompt.due)
        assertEquals(at("2026-11-01T12:45"), prompt.nextReminderAt)
    }

    @Test
    fun `跨天的记录也会自动推进间隔待办`() {
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                intervalMinutes = 24 * 60,
                intervalLabel = "整理桌面",
                nextReminderAt = nowMillis - MINUTE,
            ),
            events = listOf(
                event(1, "2026-10-31T22:00", "2026-10-31T23:00", categoryId = 1),
            ),
        ).single()

        assertFalse("昨天的记录也算近期做过", prompt.due)
        assertEquals(at("2026-11-01T23:00"), prompt.nextReminderAt)
    }

    @Test
    fun `新建间隔计划不会被更早的记录提前触发`() {
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                intervalMinutes = 60,
                intervalLabel = "起来活动",
                nextReminderAt = nowMillis + 30 * MINUTE,
            ),
            events = listOf(
                event(1, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 1),
            ),
        ).single()

        assertFalse("记录发生在本次间隔开始之前，不该把下一次提前", prompt.due)
        assertEquals(nowMillis + 30 * MINUTE, prompt.nextReminderAt)
    }

    @Test
    fun `删除分类后的计划不生成提示`() {
        val prompts = prompts(
            CategoryPlan(categoryId = 999, dailyGoalMinutes = 60),
        )
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `到期的提示排在未到期前面`() {
        val events = listOf(
            event(1, "2026-11-01T09:10", "2026-11-01T09:40", categoryId = 1),
        )
        val result = prompts(
            CategoryPlan(categoryId = 1, dailyGoalMinutes = 180),
            fixedPrompt(9 * 60),
            events = events,
        )
        assertEquals(2, result.size)
        assertTrue("目标还差很多，应到期", result[0].due)
        assertEquals(PlanPromptKind.GOAL, result[0].kind)
        assertFalse("固定时间已有记录，不该催", result[1].due)
    }
}
