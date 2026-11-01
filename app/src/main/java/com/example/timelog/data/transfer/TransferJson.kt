package com.example.timelog.data.transfer

import com.example.timelog.domain.model.ExportedCategory
import com.example.timelog.domain.model.ExportedEvent
import com.example.timelog.domain.model.LedgerSnapshot
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * 备份文件的 JSON 编解码。
 *
 * 结构固定为 { schema: 1, exportedAt, categories: [...], events: [...] }，
 * 只依赖 Android 自带的 org.json，不引入额外依赖。
 *
 * 读取失败统一抛 [IllegalArgumentException]（文案「不是有效的数据文件」），
 * 方便调用方把 message 直接展示给用户；note 缺失 / 为空按 null 处理。
 */
object TransferJson {
    private const val SCHEMA_VERSION = 1
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
                },
            )
        }

        return JSONObject().apply {
            put("schema", SCHEMA_VERSION)
            put("exportedAt", snapshot.exportedAt)
            put("categories", categories)
            put("events", events)
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
        if (root.getInt("schema") != SCHEMA_VERSION) invalid()

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

        return LedgerSnapshot(
            exportedAt = exportedAt,
            categories = categories,
            events = events,
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
        )
    }

    /** 备注缺失、为 JSON null 或空白时都按 null 处理，与仓库保存时的清洗规则一致。 */
    private fun JSONObject.noteOrNull(): String? {
        if (!has("note") || isNull("note")) return null
        return optString("note").trim().ifEmpty { null }
    }

    private fun invalid(): Nothing = throw IllegalArgumentException(INVALID_MESSAGE)
}
