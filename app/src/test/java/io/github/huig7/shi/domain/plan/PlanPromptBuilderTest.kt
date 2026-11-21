package io.github.huig7.shi.domain.plan

import io.github.huig7.shi.domain.MINUTE
import io.github.huig7.shi.domain.StatisticsTestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 首页「今日提示」的口径：目标含子树、固定时间到点、间隔待办到期与完成后的下一次时间。 */
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

    @Test
    fun `每日目标包含子分类的记录`() {
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
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                fixedTimeMinuteOfDay = 23 * 60,
                fixedTimeLabel = "冥想",
            ),
        ).single()

        assertEquals("冥想", prompt.label)
        assertFalse(prompt.due)
        assertFalse(prompt.fixedDone)
    }

    @Test
    fun `固定时间已到且没有记录时提示`() {
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                fixedTimeMinuteOfDay = 9 * 60,
                fixedTimeLabel = "晨间计划",
            ),
        ).single()

        assertTrue(prompt.due)
        assertFalse(prompt.fixedDone)
    }

    @Test
    fun `固定时间之后已有记录视为完成`() {
        val events = listOf(
            event(1, "2026-11-01T09:10", "2026-11-01T09:40", categoryId = 1),
        )
        val prompt = prompts(
            CategoryPlan(
                categoryId = 1,
                fixedTimeMinuteOfDay = 9 * 60,
                fixedTimeLabel = "晨间计划",
            ),
            events = events,
        ).single()

        assertTrue(prompt.fixedDone)
        assertFalse(prompt.due)
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
            CategoryPlan(categoryId = 1, fixedTimeMinuteOfDay = 9 * 60, fixedTimeLabel = "晨间计划"),
            events = events,
        )
        assertEquals(2, result.size)
        assertTrue("目标还差很多，应到期", result[0].due)
        assertEquals(PlanPromptKind.GOAL, result[0].kind)
        assertFalse("固定时间已有记录，不该催", result[1].due)
    }
}
