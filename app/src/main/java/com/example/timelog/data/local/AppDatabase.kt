package com.example.timelog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.timelog.data.local.dao.CategoryDao
import com.example.timelog.data.local.dao.EventDao
import com.example.timelog.data.local.entity.CategoryEntity
import com.example.timelog.data.local.entity.EventEntity
import com.example.timelog.data.seed.DefaultCategories

@Database(
    entities = [CategoryEntity::class, EventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "time-log.db")
                .addCallback(SeedCallback())
                .build()
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
