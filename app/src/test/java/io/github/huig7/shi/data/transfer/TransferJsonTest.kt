package io.github.huig7.shi.data.transfer

import io.github.huig7.shi.domain.model.ExportedCategory
import io.github.huig7.shi.domain.model.ExportedCategoryPlan
import io.github.huig7.shi.domain.model.ExportedEvent
import io.github.huig7.shi.domain.model.LedgerSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferJsonTest {

    private val category = ExportedCategory(
        id = 11,
        name = "执行",
        parentId = 1,
        sortOrder = 2,
        colorHex = 0xFF3F5B78,
        isSystem = true,
    )

    private val event = ExportedEvent(
        note = "写周报",
        startAt = 1_700_000_000_000,
        endAt = 1_700_003_600_000,
        categoryId = 11,
        createdAt = 1_700_000_100_000,
        updatedAt = 1_700_000_200_000,
        uuid = "550e8400-e29b-41d4-a716-446655440000",
    )

    private val plan = ExportedCategoryPlan(
        categoryId = 11,
        heatmapEnabled = true,
        dailyGoalMinutes = 90,
        fixedTimeMinuteOfDay = 23 * 60,
        fixedTimeLabel = "冥想",
        intervalMinutes = 60,
        intervalLabel = "起来活动",
        nextReminderAt = 1_700_010_000_000,
        lastCompletedAt = 1_700_009_000_000,
    )

    private fun snapshot(
        categories: List<ExportedCategory> = listOf(category),
        events: List<ExportedEvent> = listOf(event),
        plans: List<ExportedCategoryPlan> = emptyList(),
    ) = LedgerSnapshot(
        exportedAt = 1_700_009_999_999,
        categories = categories,
        events = events,
        plans = plans,
    )

    private fun assertInvalid(text: String) {
        val error = runCatching { TransferJson.decode(text) }.exceptionOrNull()
        assertTrue("应抛 IllegalArgumentException，实际：$error", error is IllegalArgumentException)
        assertEquals("不是有效的数据文件", error!!.message)
    }

    // ---- 往返 ----

    @Test
    fun `encode 后 decode 完整保留所有字段`() {
        val original = snapshot()
        assertEquals(original, TransferJson.decode(TransferJson.encode(original)))
    }

    @Test
    fun `null 备注与 null uuid 往返后仍为 null`() {
        val original = snapshot(events = listOf(event.copy(note = null, uuid = null)))
        val decoded = TransferJson.decode(TransferJson.encode(original))
        assertNull(decoded.events.single().note)
        assertNull(decoded.events.single().uuid)
    }

    @Test
    fun `根分类 parentId 为 null 往返不变`() {
        val original = snapshot(categories = listOf(category.copy(parentId = null)))
        assertNull(TransferJson.decode(TransferJson.encode(original)).categories.single().parentId)
    }

    @Test
    fun `空账本往返不变`() {
        val original = LedgerSnapshot(exportedAt = 0, categories = emptyList(), events = emptyList())
        assertEquals(original, TransferJson.decode(TransferJson.encode(original)))
    }

    @Test
    fun `分类计划往返完整保留`() {
        val original = snapshot(plans = listOf(plan))
        assertEquals(original, TransferJson.decode(TransferJson.encode(original)))
    }

    @Test
    fun `旧 schema 没有 plans 时按空计划读取`() {
        val decoded = TransferJson.decode("""{"schema":2,"categories":[],"events":[]}""")
        assertTrue(decoded.plans.isEmpty())
    }

    // ---- 整体结构拒绝 ----

    @Test
    fun `空白文件拒绝`() {
        assertInvalid("")
        assertInvalid("   \n\t")
    }

    @Test
    fun `非法 JSON 拒绝`() {
        assertInvalid("{schema:")
        assertInvalid("[]")
        assertInvalid("null")
    }

    @Test
    fun `缺失或越界 schema 拒绝`() {
        assertInvalid("""{"categories":[],"events":[]}""")
        assertInvalid("""{"schema":0,"categories":[],"events":[]}""")
        assertInvalid("""{"schema":4,"categories":[],"events":[]}""")
        assertInvalid("""{"schema":null,"categories":[],"events":[]}""")
    }

    @Test
    fun `缺失 categories 或 events 数组拒绝`() {
        assertInvalid("""{"schema":2,"events":[]}""")
        assertInvalid("""{"schema":2,"categories":[]}""")
    }

    @Test
    fun `exportedAt 缺失按 0 处理`() {
        val decoded = TransferJson.decode("""{"schema":2,"categories":[],"events":[]}""")
        assertEquals(0L, decoded.exportedAt)
    }

    // ---- 分类校验 ----

    @Test
    fun `分类缺失 id 拒绝`() {
        assertInvalid(
            """{"schema":2,"categories":[{"name":"工作"}],"events":[]}""",
        )
    }

    @Test
    fun `分类名为空白拒绝`() {
        assertInvalid(
            """{"schema":2,"categories":[{"id":1,"name":"  "}],"events":[]}""",
        )
    }

    @Test
    fun `分类名首尾空白被裁剪`() {
        val decoded = TransferJson.decode(
            """{"schema":2,"categories":[{"id":1,"name":" 工作 "}],"events":[]}""",
        )
        assertEquals("工作", decoded.categories.single().name)
    }

    @Test
    fun `分类可选字段缺失走默认值`() {
        val decoded = TransferJson.decode(
            """{"schema":2,"categories":[{"id":1,"name":"工作"}],"events":[]}""",
        )
        val decodedCategory = decoded.categories.single()
        assertNull(decodedCategory.parentId)
        assertEquals(0, decodedCategory.sortOrder)
        assertEquals(0xFF9AA29A, decodedCategory.colorHex)
        assertEquals(false, decodedCategory.isSystem)
    }

    // ---- 事件校验 ----

    @Test
    fun `事件缺失必填时间或分类拒绝`() {
        val base = """{"startAt":100,"endAt":200,"categoryId":1}"""
        listOf("startAt", "endAt", "categoryId").forEach { field ->
            val broken = org.json.JSONObject(base).remove(field).toString()
            assertInvalid("""{"schema":2,"categories":[],"events":[$broken]}""")
        }
    }

    @Test
    fun `结束早于开始拒绝`() {
        assertInvalid(
            """{"schema":2,"categories":[],"events":[{"startAt":200,"endAt":100,"categoryId":1}]}""",
        )
    }

    @Test
    fun `零时长事件允许`() {
        val decoded = TransferJson.decode(
            """{"schema":2,"categories":[],"events":[{"startAt":100,"endAt":100,"categoryId":1}]}""",
        )
        assertEquals(100L, decoded.events.single().endAt)
    }

    @Test
    fun `createdAt 与 updatedAt 缺失依次回退`() {
        val decoded = TransferJson.decode(
            """{"schema":2,"categories":[],"events":[{"startAt":100,"endAt":200,"categoryId":1}]}""",
        )
        val decodedEvent = decoded.events.single()
        assertEquals(100L, decodedEvent.createdAt)
        assertEquals(100L, decodedEvent.updatedAt)
    }

    @Test
    fun `备注缺失 null 或空白都按 null 处理`() {
        fun noteOf(noteJson: String): String? {
            val decoded = TransferJson.decode(
                """{"schema":2,"categories":[],"events":[{"startAt":100,"endAt":200,"categoryId":1,"note":$noteJson}]}""",
            )
            return decoded.events.single().note
        }
        assertNull(noteOf("null"))
        assertNull(noteOf("\"\""))
        assertNull(noteOf("\"   \""))
        assertEquals("看书", noteOf("\" 看书 \""))
    }

    @Test
    fun `uuid 空白按 null 处理`() {
        val decoded = TransferJson.decode(
            """{"schema":2,"categories":[],"events":[{"startAt":100,"endAt":200,"categoryId":1,"uuid":"  "}]}""",
        )
        assertNull(decoded.events.single().uuid)
    }

    // ---- schema 1 兼容 ----

    @Test
    fun `schema 1 无 uuid 字段可读且 uuid 为 null`() {
        val decoded = TransferJson.decode(
            """
            {"schema":1,"exportedAt":5,
             "categories":[{"id":1,"name":"工作","parentId":null,"sortOrder":0,"colorHex":4282211291,"isSystem":true}],
             "events":[{"note":"旧记录","startAt":100,"endAt":200,"categoryId":1,"createdAt":100,"updatedAt":150}]}
            """.trimIndent(),
        )
        assertEquals(5L, decoded.exportedAt)
        assertEquals("旧记录", decoded.events.single().note)
        assertNull(decoded.events.single().uuid)
    }

    @Test
    fun `schema 1 编码输出升级为 schema 3`() {
        val decoded = TransferJson.decode(
            """{"schema":1,"categories":[],"events":[{"startAt":100,"endAt":200,"categoryId":1}]}""",
        )
        val reEncoded = org.json.JSONObject(TransferJson.encode(decoded))
        assertEquals(3, reEncoded.getInt("schema"))
    }
}