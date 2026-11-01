package com.example.timelog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.timelog.data.local.dao.CategoryDao
import com.example.timelog.data.local.dao.EventDao
import com.example.timelog.data.local.entity.CategoryEntity
import com.example.timelog.data.local.entity.EventEntity
import com.example.timelog.data.seed.DefaultCategories

@Database(
    entities = [CategoryEntity::class, EventEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "time-log.db")
                .addMigrations(MIGRATION_1_2)
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
