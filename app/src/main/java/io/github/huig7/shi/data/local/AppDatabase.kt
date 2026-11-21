package io.github.huig7.shi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.huig7.shi.data.local.dao.CategoryDao
import io.github.huig7.shi.data.local.dao.CategoryPlanDao
import io.github.huig7.shi.data.local.dao.EventDao
import io.github.huig7.shi.data.local.entity.CategoryEntity
import io.github.huig7.shi.data.local.entity.CategoryPlanEntity
import io.github.huig7.shi.data.local.entity.EventEntity
import io.github.huig7.shi.data.seed.DefaultCategories
import java.util.UUID

@Database(
    entities = [CategoryEntity::class, EventEntity::class, CategoryPlanEntity::class],
    version = 8,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao
    abstract fun categoryPlanDao(): CategoryPlanDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "time-log.db")
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                )
                .addCallback(SeedCallback())
                .build()

        /**
         * v1 的事件只有单点时间戳和必填标题；v2 改成「块」：startAt / endAt + 可留空备注。
         * 旧事件按 30 分钟时长补全，标题并入备注（标题本来承载了「做了什么」的信息）。
         */
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
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
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
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
        internal val MIGRATION_3_4 = object : Migration(3, 4) {
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
        internal val MIGRATION_4_5 = object : Migration(4, 5) {
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

        /**
         * v6 移除系统「未分类」分类。
         *
         * - 空的系统分类（没有事件、也没有子分类）直接删除；
         * - 仍有事件或子分类的系统分类保留为普通分类，避免丢数据（用户可自行改名或删除）；
         * - 最后把遗留的 isSystem 标记清零，业务代码不再依赖它。
         *
         * 该列在 v6 中保留但不再使用，因此无需重建 category 表。
         */
        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    DELETE FROM category
                    WHERE isSystem = 1
                      AND id NOT IN (SELECT categoryId FROM event)
                      AND id NOT IN (
                          SELECT parentId FROM category WHERE parentId IS NOT NULL
                      )
                    """.trimIndent(),
                )
                db.execSQL("UPDATE category SET isSystem = 0 WHERE isSystem = 1")
            }
        }

        /**
         * v7 新增分类计划表：热力图、每日目标、固定时间事项与间隔待办。
         *
         * 外键用 CASCADE，删除分类时计划一起消失；旧数据没有计划，迁移后保持为空。
         */
        internal val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `category_plan` (
                        `categoryId` INTEGER NOT NULL,
                        `heatmapEnabled` INTEGER NOT NULL,
                        `dailyGoalMinutes` INTEGER NOT NULL,
                        `fixedTimeMinuteOfDay` INTEGER NOT NULL,
                        `fixedTimeLabel` TEXT,
                        `intervalMinutes` INTEGER NOT NULL,
                        `intervalLabel` TEXT,
                        `nextReminderAt` INTEGER NOT NULL,
                        `lastCompletedAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`categoryId`),
                        FOREIGN KEY(`categoryId`) REFERENCES `category`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
            }
        }

        /**
         * v8 把默认分类从低饱和节气色换成高饱和版本。
         *
         * 只改仍是旧默认色的 12 个固定 ID，用户自己挑过的颜色保持不动；
         * 自定义分类若恰好用了旧色板中的颜色，也会一起更新，保持整棵树的新配色一致。
         */
        internal val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recolor(db, 1, 0xFF5A7FA8, 0xFF2F6FE4)
                recolor(db, 11, 0xFF3F5B78, 0xFF2C4E8A)
                recolor(db, 12, 0xFF8A9AB8, 0xFF5C8DF6)
                recolor(db, 2, 0xFF9E86B8, 0xFFA45FD1)
                recolor(db, 21, 0xFF8B72A6, 0xFF8E5BD9)
                recolor(db, 22, 0xFF8A86A8, 0xFF7E6BD9)
                recolor(db, 3, 0xFFC9A24E, 0xFFE8A317)
                recolor(db, 31, 0xFFB8933F, 0xFFF2A900)
                recolor(db, 32, 0xFFA98A5A, 0xFFD19A3E)
                recolor(db, 4, 0xFF94594A, 0xFFC0472B)
                recolor(db, 41, 0xFFC46E4A, 0xFFF4512C)
                recolor(db, 42, 0xFFA8463A, 0xFFD32F2F)
            }

            private fun recolor(db: SupportSQLiteDatabase, id: Long, oldColor: Long, newColor: Long) {
                db.execSQL(
                    "UPDATE category SET colorHex = ? WHERE id = ? AND colorHex = ?",
                    arrayOf<Any?>(newColor, id, oldColor),
                )
            }
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DefaultCategories.all.forEach { category ->
                    db.execSQL(
                        "INSERT INTO category (id, name, parentId, sortOrder, colorHex, isSystem) " +
                            "VALUES (?, ?, ?, ?, ?, 0)",
                        arrayOf<Any?>(
                            category.id,
                            category.name,
                            category.parentId,
                            category.sortOrder,
                            category.colorHex,
                        ),
                    )
                }
            }
        }
    }
}
