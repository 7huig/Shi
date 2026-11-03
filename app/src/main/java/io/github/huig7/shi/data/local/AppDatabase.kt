package io.github.huig7.shi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.huig7.shi.data.local.dao.CategoryDao
import io.github.huig7.shi.data.local.dao.EventDao
import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.data.local.entity.EventEntity
import io.github.huig7.shi.data.seed.DefaultCategories
import java.util.UUID

@Database(
    entities = [CategoryEntity::class, EventEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "time-log.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(SeedCallback())
                .build()

        /**
         * v1 的事件只有单点时间戳和必填标题；v2 改成「块」：startAt / endAt + 可留空备注。
         * 旧事件按 30 分钟时长补全，标题并入备注（标题本来承载了「做了什么」的信息）。
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `event_new` (
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
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO event_new (id, note, startAt, endAt, categoryId, createdAt, updatedAt)
                    SELECT id,
                           CASE
                               WHEN note IS NULL OR TRIM(note) = '' THEN title
                               WHEN title IS NULL OR TRIM(title) = '' THEN note
                               ELSE title || ' / ' || note
                           END,
                           timestamp,
                           timestamp + 1800000,
                           categoryId,
                           createdAt,
                           updatedAt
                    FROM event
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE event")
                db.execSQL("ALTER TABLE event_new RENAME TO event")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_event_categoryId ON event(categoryId)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_event_startAt ON event(startAt)")
            }
        }

        /**
         * v3 只调整默认分类的展示顺序：工作下「沟通」排在「执行」前，娱乐下「沉浸」排在「社交」前。
         * 只改默认种子的 4 个固定 ID，且要求名称仍是默认值，避免覆盖用户改过的分类。
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE category SET sortOrder = 1 WHERE id = 11 AND name = '执行'",
                )
                db.execSQL(
                    "UPDATE category SET sortOrder = 0 WHERE id = 12 AND name = '沟通'",
                )
                db.execSQL(
                    "UPDATE category SET sortOrder = 1 WHERE id = 41 AND name = '社交'",
                )
                db.execSQL(
                    "UPDATE category SET sortOrder = 0 WHERE id = 42 AND name = '沉浸'",
                )
            }
        }

        /**
         * v4 换成 24 节气色板：只改仍是旧默认色的种子分类，用户自己挑过的颜色保持不动。
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recolor(db, 1, 0xFF3D6BDB, 0xFF5A7FA8)
                recolor(db, 11, 0xFF4C7BE8, 0xFF3F5B78)
                recolor(db, 12, 0xFF2F5DAF, 0xFF8A9AB8)
                recolor(db, 2, 0xFF8A63C9, 0xFF9E86B8)
                recolor(db, 21, 0xFF9C79D6, 0xFF8B72A6)
                recolor(db, 22, 0xFF7150A8, 0xFF8A86A8)
                recolor(db, 3, 0xFFC07A2B, 0xFFC9A24E)
                recolor(db, 31, 0xFFD99445, 0xFFB8933F)
                recolor(db, 32, 0xFF9E6220, 0xFFA98A5A)
                recolor(db, 4, 0xFFB4654A, 0xFF94594A)
                recolor(db, 41, 0xFFCC7C5E, 0xFFC46E4A)
                recolor(db, 42, 0xFF8E4A35, 0xFFA8463A)
            }

            private fun recolor(db: SupportSQLiteDatabase, id: Long, oldColor: Long, newColor: Long) {
                db.execSQL(
                    "UPDATE category SET colorHex = ? WHERE id = ? AND colorHex = ?",
                    arrayOf<Any?>(newColor, id, oldColor),
                )
            }
        }

        /** v5 为事件增加跨设备稳定的 UUID，用于合并导入时识别同一条记录。 */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE event ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                val ids = buildList {
                    db.query("SELECT id FROM event").use { cursor ->
                        while (cursor.moveToNext()) add(cursor.getLong(0))
                    }
                }
                ids.forEach { id ->
                    db.execSQL(
                        "UPDATE event SET uuid = ? WHERE id = ?",
                        arrayOf<Any?>(UUID.randomUUID().toString(), id),
                    )
                }
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_event_uuid ON event(uuid)")
            }
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DefaultCategories.all.forEach { category ->
                    db.execSQL(
                        "INSERT INTO category (id, name, parentId, sortOrder, colorHex, isSystem) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf<Any?>(
                            category.id,
                            category.name,
                            category.parentId,
                            category.sortOrder,
                            category.colorHex,
                            if (category.isSystem) 1 else 0,
                        ),
                    )
                }
            }
        }
    }
}
