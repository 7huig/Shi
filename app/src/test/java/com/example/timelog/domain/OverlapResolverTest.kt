package com.example.timelog.domain

import com.example.timelog.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlapResolverTest {

    private val hour = 3_600_000L
    private val base = 1_700_000_000_000L

    private fun at(hours: Long): Long = base + hours * hour

    private fun event(
        id: Long,
        startHour: Long,
        endHour: Long,
        note: String? = null,
        categoryId: Long = 7L,
    ): Event = Event(
        id = id,
        note = note,
        startAt = at(startHour),
        endAt = at(endHour),
        categoryId = categoryId,
        createdAt = 1_000L + id,
        updatedAt = 2_000L + id,
    )

    @Test
    fun noOverlapLeavesEverythingUntouched() {
        val plan = resolveOverlap(at(10), at(11), listOf(event(1, 8, 9), event(2, 12, 13)))

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun emptyOthersProduceEmptyPlan() {
        val plan = resolveOverlap(at(10), at(11), emptyList())

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun touchingIntervalsAreNotOverlap() {
        // 老记录 ends exactly at 目标 start.
        val before = resolveOverlap(at(10), at(11), listOf(event(1, 9, 10)))
        // 老记录 starts exactly at 目标 end.
        val after = resolveOverlap(at(10), at(11), listOf(event(2, 11, 12)))

        assertTrue(before.updates.isEmpty() && before.clones.isEmpty() && before.victims.isEmpty())
        assertTrue(after.updates.isEmpty() && after.clones.isEmpty() && after.victims.isEmpty())
    }

    @Test
    fun eventFullyInsideTargetIsVictim() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 10, 11, note = "写周报")))

        assertEquals(listOf(1L), plan.victims.map { it.id })
        assertEquals("写周报", plan.victims.single().note)
        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
    }

    @Test
    fun identicalIntervalIsVictim() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 10, 12)))

        assertEquals(listOf(1L), plan.victims.map { it.id })
        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
    }

    @Test
    fun eventStickingOutLeftIsTrimmedToTargetStart() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 9, 11)))

        val update = plan.updates.single()
        assertEquals(1L, update.event.id)
        assertEquals(at(9), update.startAt)
        assertEquals(at(10), update.endAt)
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun eventStickingOutRightIsTrimmedToTargetEnd() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 11, 13)))

        val update = plan.updates.single()
        assertEquals(1L, update.event.id)
        assertEquals(at(12), update.startAt)
        assertEquals(at(13), update.endAt)
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun eventCoveringBothSidesKeepsLeftAndClonesRight() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 9, 13, note = "值班", categoryId = 3L)))

        val update = plan.updates.single()
        assertEquals(at(9), update.startAt)
        assertEquals(at(10), update.endAt)

        val clone = plan.clones.single()
        assertEquals("值班", clone.note)
        assertEquals(at(12), clone.startAt)
        assertEquals(at(13), clone.endAt)
        assertEquals(3L, clone.categoryId)
        assertEquals(1_001L, clone.createdAt)
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun coveringTargetWithMatchingEdgesProducesSingleTrim() {
        // 老记录与目标同起点，只向右超出：右侧残余非空，左侧残余为零宽被丢弃。
        val right = resolveOverlap(at(10), at(12), listOf(event(1, 10, 13)))
        assertEquals(1, right.updates.size)
        assertEquals(at(12), right.updates.single().startAt)
        assertEquals(at(13), right.updates.single().endAt)
        assertTrue(right.clones.isEmpty())

        // 老记录与目标同终点，只向左超出：左侧残余非空，右侧残余为零宽被丢弃。
        val left = resolveOverlap(at(10), at(12), listOf(event(2, 9, 12)))
        assertEquals(1, left.updates.size)
        assertEquals(at(9), left.updates.single().startAt)
        assertEquals(at(10), left.updates.single().endAt)
        assertTrue(left.clones.isEmpty())
    }

    @Test
    fun mixedOthersProduceMixedPlanInInputOrder() {
        val plan = resolveOverlap(
            at(10),
            at(12),
            listOf(
                event(1, 9, 11),  // 左侧超出：裁掉尾部
                event(2, 10, 11), // 整段被盖住：victim
                event(3, 11, 13), // 右侧超出：裁掉头部
                event(4, 9, 13),  // 两侧都超出：更新 + 复制
                event(5, 13, 14), // 没有交集：不动
            ),
        )

        assertEquals(listOf(1L, 3L, 4L), plan.updates.map { it.event.id })
        assertEquals(1, plan.clones.size)
        assertEquals(listOf(2L), plan.victims.map { it.id })
    }

    @Test
    fun zeroWidthAndInvalidEventsAreIgnored() {
        val plan = resolveOverlap(
            at(10),
            at(12),
            listOf(
                event(1, 11, 11), // 零宽
                event(2, 13, 12), // 负宽
            ),
        )

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun invalidTargetProducesEmptyPlan() {
        val plan = resolveOverlap(at(12), at(10), listOf(event(1, 10, 11)))

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }
}
