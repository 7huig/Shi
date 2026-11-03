package io.github.huig7.shi.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LegacyEventUuidTest {

    @Test
    fun legacyUuidIsStableAcrossRepeatedImports() {
        val event = event()
        assertEquals(event.legacyUuid(0), event.copy().legacyUuid(0))
    }

    @Test
    fun identicalEventsKeepDistinctUuidsByOccurrence() {
        val event = event()
        assertNotEquals(event.legacyUuid(0), event.legacyUuid(1))
    }

    @Test
    fun differentLegacyEventsDoNotShareIdentity() {
        assertNotEquals(event().legacyIdentity(), event(note = "另一条").legacyIdentity())
        assertNotEquals(event().legacyIdentity(), event(createdAt = 2L).legacyIdentity())
    }

    private fun event(note: String? = "记录", createdAt: Long = 1L) = ExportedEvent(
        note = note,
        startAt = 10L,
        endAt = 20L,
        categoryId = 3L,
        createdAt = createdAt,
        updatedAt = createdAt,
    )
}
