package io.github.huig7.shi.data.local

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 迁移测试。历史版本（v1–v4）没有导出过 schema JSON，
 * 因此起点库按当年的建表 SQL 手工搭建，终点统一用 v5 的导出 schema 校验。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseMigrationTest {

    private val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private val categoryTable = """
        CREATE TABLE IF NOT EXISTS `category` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL,
            `parentId` INTEGER,
            `sortOrder` INTEGER NOT NULL,
            `colorHex` INTEGER NOT NULL,
            `isSystem` INTEGER NOT NULL,
            FOREIGN KEY(`parentId`) REFERENCES `category`(`id`)
                ON UPDATE NO ACTION ON DELETE CASCADE
        )
    """.trimIndent()

    private val eventTableV1 = """
        CREATE TABLE IF NOT EXISTS `event` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `title` TEXT NOT NULL,
            `note` TEXT,
            `timestamp` INTEGER NOT NULL,
            `categoryId` INTEGER NOT NULL,
            `createdAt` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            FOREIGN KEY(`categoryId`) REFERENCES `category`(`id`)
                ON UPDATE NO ACTION ON DELETE RESTRICT
        )
    """.trimIndent()

    /** v2–v4 的事件表结构相同：块时间戳，无 uuid。 */
    private val eventTableV2ToV4 = """
        CREATE TABLE IF NOT EXISTS `event` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `note` TEXT,
            `startAt` INTEGER NOT NULL,
            `endAt` INTEGER NOT NULL,
            `categoryId` INTEGER NOT NULL,
            `createdAt` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            FOREIGN KEY(`categoryId`) REFERENCES `category`(`id`)
                ON UPDATE NO ACTION ON DELETE RESTRICT
        )
    """.trimIndent()

    /** v5 的起点库：事件已有 uuid 与唯一索引。 */
    private val eventTableV5 = """
        CREATE TABLE IF NOT EXISTS `event` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `note` TEXT,
            `startAt` INTEGER NOT NULL,
            `endAt` INTEGER NOT NULL,
            `categoryId` INTEGER NOT NULL,
            `createdAt` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            `uuid` TEXT NOT NULL DEFAULT '',
            FOREIGN KEY(`categoryId`) REFERENCES `category`(`id`)
                ON UPDATE NO ACTION ON DELETE RESTRICT
        )
    """.trimIndent()

    private fun createDatabase(name: String, version: Int, seed: (SupportSQLiteDatabase) -> Unit) {
        context.deleteDatabase(name)
        openDatabase(name, version).use(seed)
    }

    private fun openDatabase(name: String, version: Int): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(categoryTable)
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_category_parentId` ON `category` (`parentId`)")
                    val eventTable = when {
                        version == 1 -> eventTableV1
                        version >= 5 -> eventTableV5
                        else -> eventTableV2ToV4
                    }
                    db.execSQL(eventTable)
                    if (version >= 2) {
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_categoryId` ON `event` (`categoryId`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_startAt` ON `event` (`startAt`)")
                    }
                    if (version >= 5) {
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS `index_event_uuid` ON `event` (`uuid`)",
                        )
                    }
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    private fun insertCategory(
        db: SupportSQLiteDatabase,
        id: Long,
        name: String,
        sortOrder: Int = 0,
        colorHex: Long = 0xFF5A7FA8,
        isSystem: Boolean = false,
    ) {
        db.execSQL(
            "INSERT INTO category (id, name, parentId, sortOrder, colorHex, isSystem) " +
                "VALUES (?, ?, NULL, ?, ?, ?)",
            arrayOf<Any?>(id, name, sortOrder, colorHex, if (isSystem) 1 else 0),
        )
    }

    private fun SupportSQLiteDatabase.stringColumn(sql: String): List<String> =
        query(sql).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }

    private fun SupportSQLiteDatabase.longColumn(sql: String): List<Long> =
        query(sql).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getLong(0))
            }
        }

    @Test
    fun fullChain1To6_matchesSchemaAndConvertsEvents() {
        val name = "migration-1-6"
        createDatabase(name, 1) { db ->
            insertCategory(db, 1, "工作")
            listOf(
                // title / note / 期望合并结果
                Triple("写代码", null, "写代码"),
                Triple("", "读书", "读书"),
                Triple("会议", "纪要", "会议 / 纪要"),
            ).forEachIndexed { index, (title, note, _) ->
                db.execSQL(
                    "INSERT INTO event (id, title, note, timestamp, categoryId, createdAt, updatedAt) " +
                        "VALUES (?, ?, ?, ?, 1, ?, ?)",
                    arrayOf<Any?>(index + 1, title, note, 1000L * (index + 1), 1000L, 1000L),
                )
            }
        }

        val db = helper.runMigrationsAndValidate(
            name, 6, true,
            AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
        )
        db.use {
            assertEquals(listOf(1801000L, 1802000L, 1803000L), it.longColumn("SELECT endAt FROM event ORDER BY id"))
            assertEquals(listOf("写代码", "读书", "会议 / 纪要"), it.stringColumn("SELECT note FROM event ORDER BY id"))
            val uuids = it.stringColumn("SELECT uuid FROM event ORDER BY id")
            assertEquals(3, uuids.size)
            assertTrue("uuid 应全部非空", uuids.all { uuid -> uuid.isNotBlank() })
            assertEquals("uuid 应互不相同", 3, uuids.toSet().size)
        }
    }

    @Test
    fun migrate5To6_removesEmptySystemCategoryAndKeepsUsedOne() {
        val name = "migration-5-6"
        createDatabase(name, 5) { db ->
            insertCategory(db, 1, "工作")
            // 空的系统「未分类」：迁移后应消失。
            insertCategory(db, 5, "未分类", sortOrder = 4, isSystem = true)
            // 仍被事件引用的系统「未分类」：保留为普通分类，避免丢记录。
            insertCategory(db, 6, "未分类", sortOrder = 5, isSystem = true)
            db.execSQL(
                "INSERT INTO event (id, note, startAt, endAt, categoryId, createdAt, updatedAt, uuid) " +
                    "VALUES (1, '写代码', 1000, 2000, 1, 1000, 1000, 'uuid-1')",
            )
            db.execSQL(
                "INSERT INTO event (id, note, startAt, endAt, categoryId, createdAt, updatedAt, uuid) " +
                    "VALUES (2, '旧记录', 3000, 4000, 6, 1000, 1000, 'uuid-2')",
            )
        }

        val db = helper.runMigrationsAndValidate(name, 6, true, AppDatabase.MIGRATION_5_6)
        db.use {
            assertEquals(listOf("工作", "未分类"), it.stringColumn("SELECT name FROM category ORDER BY id"))
            assertEquals(
                listOf(0L, 0L),
                it.longColumn("SELECT isSystem FROM category ORDER BY id"),
            )
            assertEquals(listOf(1L, 6L), it.longColumn("SELECT categoryId FROM event ORDER BY id"))
            assertEquals(2L, it.longColumn("SELECT COUNT(*) FROM event").single())
        }
    }

    @Test
    fun migrate4To5_backfillsUniqueUuidsAndKeepsData() {
        val name = "migration-4-5"
        createDatabase(name, 4) { db ->
            insertCategory(db, 1, "工作")
            (1..3).forEach { index ->
                db.execSQL(
                    "INSERT INTO event (id, note, startAt, endAt, categoryId, createdAt, updatedAt) " +
                        "VALUES (?, ?, ?, ?, 1, ?, ?)",
                    arrayOf<Any?>(index, "记录$index", 1000L * index, 2000L * index, 1000L, 1000L),
                )
            }
        }

        val db = helper.runMigrationsAndValidate(name, 5, true, AppDatabase.MIGRATION_4_5)
        db.use {
            val uuids = it.stringColumn("SELECT uuid FROM event ORDER BY id")
            assertTrue(uuids.all { uuid -> uuid.isNotBlank() })
            assertEquals(3, uuids.toSet().size)
            assertEquals(listOf("记录1", "记录2", "记录3"), it.stringColumn("SELECT note FROM event ORDER BY id"))
            assertEquals(listOf(2000L, 4000L, 6000L), it.longColumn("SELECT endAt FROM event ORDER BY id"))
        }
    }

    @Test
    fun migrate2To3_keepsRenamedCategories() {
        val name = "migration-2-3"
        createDatabase(name, 2) { db ->
            insertCategory(db, 11, "执行", sortOrder = 0)
            insertCategory(db, 12, "开会", sortOrder = 5) // 用户改过名，不应被重排
        }
        openDatabase(name, 2).use { db ->
            AppDatabase.MIGRATION_2_3.migrate(db)
            assertEquals(listOf(1L), db.longColumn("SELECT sortOrder FROM category WHERE id = 11"))
            assertEquals(listOf(5L), db.longColumn("SELECT sortOrder FROM category WHERE id = 12"))
        }
    }

    @Test
    fun migrate3To4_keepsUserChosenColors() {
        val name = "migration-3-4"
        createDatabase(name, 3) { db ->
            insertCategory(db, 1, "工作", colorHex = 0xFF3D6BDB) // 旧默认色，应换节气色
            insertCategory(db, 2, "学习", colorHex = 0xFF123456) // 用户自选色，保持不动
        }
        openDatabase(name, 3).use { db ->
            AppDatabase.MIGRATION_3_4.migrate(db)
            assertEquals(listOf(0xFF5A7FA8), db.longColumn("SELECT colorHex FROM category WHERE id = 1"))
            assertEquals(listOf(0xFF123456), db.longColumn("SELECT colorHex FROM category WHERE id = 2"))
        }
    }
}