package io.github.huig7.shi.data.transfer

import io.github.huig7.shi.domain.model.ExportedCategory
import io.github.huig7.shi.domain.model.ExportedCategoryPlan
import io.github.huig7.shi.domain.model.ExportedEvent
import io.github.huig7.shi.domain.model.ExportedFixedTime
import io.github.huig7.shi.domain.model.LedgerSnapshot
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * 备份文件的 JSON 编解码。
 *
 * 当前写出 { schema: 5, exportedAt, categories: [...], events: [...], plans: [...] }，
 * 并兼容读取 schema 1 / 2 / 3 / 4（旧文件的计划没有 fixedTimes 数组时读取单个固定时间字段，
 * 旧版 lastCompletedAt 会忽略）。
 * 只依赖 Android 自带的 org.json，不引入额外依赖。
 *
 * 读取失败统一抛 [IllegalArgumentException]（文案「不是有效的数据文件」），
 * 方便调用方把 message 直接展示给用户；note 缺失 / 为空按 null 处理。
 */
object TransferJson {
    private const val SCHEMA_VERSION = 5
    private const val MIN_SUPPORTED_SCHEMA = 1
    private const val FALLBACK_COLOR = 0xFF9AA29A
    private const val INVALID_MESSAGE = "不是有效的数据文件"

    fun encode(snapshot: LedgerSnapshot): String {
        val categories = JSONArray()
        snapshot.categories.forEach { category ->
            categories.put(
                JSONObject().apply {
                    put("id", category.id)
                    put("name", category.name)
                    if (category.parentId == null) {
                        put("parentId", JSONObject.NULL)
                    } else {
                        put("parentId", category.parentId)
                    }
                    put("sortOrder", category.sortOrder)
                    put("colorHex", category.colorHex)
                    put("isSystem", category.isSystem)
                },
            )
        }

        val events = JSONArray()
        snapshot.events.forEach { event ->
            events.put(
                JSONObject().apply {
                    put("note", event.note ?: JSONObject.NULL)
                    put("startAt", event.startAt)
                    put("endAt", event.endAt)
                    put("categoryId", event.categoryId)
                    put("createdAt", event.createdAt)
                    put("updatedAt", event.updatedAt)
                    put("uuid", event.uuid ?: JSONObject.NULL)
                },
            )
        }

        val plans = JSONArray()
        snapshot.plans.forEach { plan ->
            val fixedTimes = JSONArray()
            plan.fixedTimes.forEach { fixed ->
                fixedTimes.put(
                    JSONObject().apply {
                        put("minuteOfDay", fixed.minuteOfDay)
                        put("label", fixed.label ?: JSONObject.NULL)
                        put("leadMinutes", fixed.leadMinutes)
                    },
                )
            }
            plans.put(
                JSONObject().apply {
                    put("categoryId", plan.categoryId)
                    put("heatmapEnabled", plan.heatmapEnabled)
                    put("dailyGoalMinutes", plan.dailyGoalMinutes)
                    put("fixedTimes", fixedTimes)
                    put("intervalMinutes", plan.intervalMinutes)
                    put("intervalLabel", plan.intervalLabel ?: JSONObject.NULL)
                    put("nextReminderAt", plan.nextReminderAt)
                },
            )
        }

        return JSONObject().apply {
            put("schema", SCHEMA_VERSION)
            put("exportedAt", snapshot.exportedAt)
            put("categories", categories)
            put("events", events)
            put("plans", plans)
        }.toString(2)
    }

    fun decode(text: String): LedgerSnapshot {
        if (text.isBlank()) throw IllegalArgumentException(INVALID_MESSAGE)
        return try {
            readRoot(JSONObject(text))
        } catch (error: JSONException) {
            throw IllegalArgumentException(INVALID_MESSAGE, error)
        }
    }

    private fun readRoot(root: JSONObject): LedgerSnapshot {
        if (!root.has("schema") || root.isNull("schema")) invalid()
        if (root.getInt("schema") !in MIN_SUPPORTED_SCHEMA..SCHEMA_VERSION) invalid()

        val exportedAt = if (root.has("exportedAt") && !root.isNull("exportedAt")) {
            root.getLong("exportedAt")
        } else {
            0L
        }

        val categoryArray = root.getJSONArray("categories")
        val categories = buildList(categoryArray.length()) {
            for (index in 0 until categoryArray.length()) {
                add(categoryArray.getJSONObject(index).toCategory())
            }
        }

        val eventArray = root.getJSONArray("events")
        val events = buildList(eventArray.length()) {
            for (index in 0 until eventArray.length()) {
                add(eventArray.getJSONObject(index).toEvent())
            }
        }

        val plans = if (root.has("plans") && !root.isNull("plans")) {
            val planArray = root.getJSONArray("plans")
            buildList(planArray.length()) {
                for (index in 0 until planArray.length()) {
                    add(planArray.getJSONObject(index).toPlan())
                }
            }
        } else {
            emptyList()
        }

        return LedgerSnapshot(
            exportedAt = exportedAt,
            categories = categories,
            events = events,
            plans = plans,
        )
    }

    private fun JSONObject.toCategory(): ExportedCategory {
        if (!has("id") || isNull("id")) invalid()
        val name = optString("name").trim()
        if (name.isEmpty()) invalid()
        return ExportedCategory(
            id = getLong("id"),
            name = name,
            parentId = if (has("parentId") && !isNull("parentId")) getLong("parentId") else null,
            sortOrder = if (has("sortOrder") && !isNull("sortOrder")) getInt("sortOrder") else 0,
            colorHex = if (has("colorHex") && !isNull("colorHex")) {
                getLong("colorHex")
            } else {
                FALLBACK_COLOR
            },
            isSystem = has("isSystem") && !isNull("isSystem") && getBoolean("isSystem"),
        )
    }

    private fun JSONObject.toEvent(): ExportedEvent {
        if (!has("startAt") || isNull("startAt")) invalid()
        if (!has("endAt") || isNull("endAt")) invalid()
        if (!has("categoryId") || isNull("categoryId")) invalid()

        val startAt = getLong("startAt")
        val endAt = getLong("endAt")
        if (endAt < startAt) invalid()
        val createdAt = if (has("createdAt") && !isNull("createdAt")) getLong("createdAt") else startAt
        val updatedAt = if (has("updatedAt") && !isNull("updatedAt")) getLong("updatedAt") else createdAt

        return ExportedEvent(
            note = noteOrNull(),
            startAt = startAt,
            endAt = endAt,
            categoryId = getLong("categoryId"),
            createdAt = createdAt,
            updatedAt = updatedAt,
            uuid = if (has("uuid") && !isNull("uuid")) getString("uuid").trim().ifEmpty { null } else null,
        )
    }

    private fun JSONObject.toPlan(): ExportedCategoryPlan {
        if (!has("categoryId") || isNull("categoryId")) invalid()
        return ExportedCategoryPlan(
            categoryId = getLong("categoryId"),
            heatmapEnabled = optBoolean("heatmapEnabled", false),
            dailyGoalMinutes = optInt("dailyGoalMinutes", 0),
            fixedTimes = if (has("fixedTimes") && !isNull("fixedTimes")) {
                getJSONArray("fixedTimes").let { array ->
                    buildList(array.length()) {
                        for (index in 0 until array.length()) {
                            add(array.getJSONObject(index).toFixedTime())
                        }
                    }
                }
            } else {
                legacyFixedTimes()
            },
            intervalMinutes = optInt("intervalMinutes", 0),
            intervalLabel = if (has("intervalLabel") && !isNull("intervalLabel")) {
                getString("intervalLabel").trim().ifEmpty { null }
            } else {
                null
            },
            nextReminderAt = optLong("nextReminderAt", 0L),
        )
    }

    private fun JSONObject.toFixedTime(): ExportedFixedTime {
        val minute = optInt("minuteOfDay", -1)
        if (minute !in 0 until MINUTES_PER_DAY) invalid()
        return ExportedFixedTime(
            minuteOfDay = minute,
            label = if (has("label") && !isNull("label")) {
                getString("label").trim().ifEmpty { null }
            } else {
                null
            },
            leadMinutes = optInt("leadMinutes", 0).coerceIn(0, MAX_FIXED_LEAD_MINUTES),
        )
    }

    /** schema 3 及更早的备份只有一个固定时间字段；读成一条提前 0 分钟的记录。 */
    private fun JSONObject.legacyFixedTimes(): List<ExportedFixedTime> {
        val minute = optInt("fixedTimeMinuteOfDay", -1)
        if (minute !in 0 until MINUTES_PER_DAY) return emptyList()
        val label = if (has("fixedTimeLabel") && !isNull("fixedTimeLabel")) {
            getString("fixedTimeLabel").trim().ifEmpty { null }
        } else {
            null
        }
        return listOf(ExportedFixedTime(minuteOfDay = minute, label = label, leadMinutes = 0))
    }

    /** 备注缺失、为 JSON null 或空白时都按 null 处理，与仓库保存时的清洗规则一致。 */
    private fun JSONObject.noteOrNull(): String? {
        if (!has("note") || isNull("note")) return null
        return optString("note").trim().ifEmpty { null }
    }

    private fun invalid(): Nothing = throw IllegalArgumentException(INVALID_MESSAGE)

    private const val MINUTES_PER_DAY = 24 * 60
    private const val MAX_FIXED_LEAD_MINUTES = 180
}
