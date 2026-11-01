# 時計 · 完整源码

> 由仓库 tracked 文件自动汇总，便于按文件路径审阅。构建入口与说明见 [README.md](README.md)。

## 文件清单

- `.gitattributes`
- `.gitignore`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/timelog/MainActivity.kt`
- `app/src/main/java/com/example/timelog/TimeLogApplication.kt`
- `app/src/main/java/com/example/timelog/data/local/AppDatabase.kt`
- `app/src/main/java/com/example/timelog/data/local/dao/CategoryDao.kt`
- `app/src/main/java/com/example/timelog/data/local/dao/EventDao.kt`
- `app/src/main/java/com/example/timelog/data/local/entity/CategoryEntity.kt`
- `app/src/main/java/com/example/timelog/data/local/entity/EventEntity.kt`
- `app/src/main/java/com/example/timelog/data/repository/TimeLogRepository.kt`
- `app/src/main/java/com/example/timelog/data/seed/DefaultCategories.kt`
- `app/src/main/java/com/example/timelog/domain/model/Category.kt`
- `app/src/main/java/com/example/timelog/domain/model/CategoryTree.kt`
- `app/src/main/java/com/example/timelog/domain/model/Event.kt`
- `app/src/main/java/com/example/timelog/domain/model/Timeline.kt`
- `app/src/main/java/com/example/timelog/ui/category/CategoryManageScreen.kt`
- `app/src/main/java/com/example/timelog/ui/category/CategoryManageViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/home/HomeScreen.kt`
- `app/src/main/java/com/example/timelog/ui/home/HomeViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/home/TimelineItem.kt`
- `app/src/main/java/com/example/timelog/ui/record/CategoryGrid.kt`
- `app/src/main/java/com/example/timelog/ui/record/RecordSheet.kt`
- `app/src/main/java/com/example/timelog/ui/record/RecordViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/theme/Color.kt`
- `app/src/main/java/com/example/timelog/ui/theme/Theme.kt`
- `app/src/main/java/com/example/timelog/ui/theme/Type.kt`
- `app/src/main/java/com/example/timelog/util/DateTimeFormat.kt`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/values-night/themes.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/test/java/com/example/timelog/domain/TimelineBuilderTest.kt`
- `build.gradle.kts`
- `design/icons/README.md`
- `design/icons/render.py`
- `gradle.properties`
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`

---

## `.gitattributes`

```text
# 统一换行符，避免 Windows/Linux 协作时的 CRLF 噪音
* text=auto eol=lf

# Windows 脚本保持 CRLF
*.bat text eol=crlf
*.cmd text eol=crlf

# 二进制文件不做文本转换
*.jar binary
*.apk binary
*.aab binary
*.png binary
*.jpg binary
*.webp binary
*.jks binary
*.keystore binary
```

## `.gitignore`

```text
# --- Gradle ---
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
local.properties

# --- 本地工具/缓存 ---
.tools/
artifacts/
*.log

# --- IDE (Android Studio / IntelliJ) ---
.idea/
*.iml
*.ipr
*.iws
out/
captures/
.externalNativeBuild/
.cxx/

# --- Android ---
*.apk
*.aab
*.ap_
*.dex
*.class

# --- 签名/密钥（勿提交） ---
*.jks
*.keystore
keystore.properties
signing.properties

# --- 系统 ---
.DS_Store
Thumbs.db
desktop.ini

# --- 其他 ---
*.hprof
.kotlin/
```

## `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.timelog"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.example.timelog"
        minSdk = 29
        targetSdk = 31
        versionCode = 2
        versionName = "2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // Compose 的预编译 .so 没有调试符号，默认 strip 任务会输出提示；显式保留以保持构建日志干净。
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
}
```

## `app/proguard-rules.pro`

```text
# 项目自定义的 ProGuard / R8 规则写在这里。
# 当前未启用代码混淆（release buildType 中 isMinifyEnabled = false）。
```

## `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".TimeLogApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TimeLog">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

## `app/src/main/java/com/example/timelog/MainActivity.kt`

```kotlin
package com.example.timelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.ui.category.CategoryManageScreen
import com.example.timelog.ui.category.CategoryManageViewModel
import com.example.timelog.ui.home.HomeScreen
import com.example.timelog.ui.home.HomeViewModel
import com.example.timelog.ui.record.RecordSheet
import com.example.timelog.ui.record.RecordViewModel
import com.example.timelog.ui.theme.TimeLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as TimeLogApplication).repository
        setContent {
            TimeLogTheme {
                TimeLogApp(repository)
            }
        }
    }
}

private enum class AppScreen { Home, Categories }

@Composable
private fun TimeLogApp(repository: TimeLogRepository) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    val recordViewModel: RecordViewModel = viewModel(factory = RecordViewModel.factory(repository))
    val categoryViewModel: CategoryManageViewModel = viewModel(
        factory = CategoryManageViewModel.factory(repository),
    )

    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val recordState by recordViewModel.uiState.collectAsStateWithLifecycle()
    val categoryState by categoryViewModel.uiState.collectAsStateWithLifecycle()

    var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
    val editorTarget = homeState.editor

    BackHandler(enabled = screen == AppScreen.Categories) {
        screen = AppScreen.Home
    }

    LaunchedEffect(editorTarget) {
        editorTarget?.let(recordViewModel::start)
    }

    LaunchedEffect(recordState.saved) {
        if (recordState.saved) {
            homeViewModel.closeRecord()
            recordViewModel.reset()
        }
    }

    when (screen) {
        AppScreen.Home -> HomeScreen(
            state = homeState,
            onNewRecord = homeViewModel::openNewRecord,
            onOpenCategories = { screen = AppScreen.Categories },
            onEventClick = homeViewModel::openEditRecord,
            onEventLongClick = homeViewModel::requestDelete,
            onFillGap = homeViewModel::openFillRecord,
            onDismissDelete = homeViewModel::dismissDelete,
            onConfirmDelete = homeViewModel::confirmDelete,
        )

        AppScreen.Categories -> CategoryManageScreen(
            state = categoryState,
            onBack = { screen = AppScreen.Home },
            onAddRoot = { categoryViewModel.startCreate(null) },
            onAddChild = categoryViewModel::startCreate,
            onEdit = categoryViewModel::startEdit,
            onDelete = categoryViewModel::requestDelete,
            onEditorNameChange = categoryViewModel::onEditorNameChange,
            onEditorColorChange = categoryViewModel::onEditorColorChange,
            onEditorDismiss = categoryViewModel::dismissEditor,
            onEditorSave = categoryViewModel::saveEditor,
            onDeleteDismiss = categoryViewModel::dismissDelete,
            onDeleteConfirm = categoryViewModel::confirmDelete,
            onMessageShown = categoryViewModel::consumeMessage,
        )
    }

    if (editorTarget != null) {
        RecordSheet(
            state = recordState,
            onDismiss = homeViewModel::closeRecord,
            onNoteChange = recordViewModel::onNoteChange,
            onCategorySelected = recordViewModel::onCategorySelected,
            onStartDateSelected = recordViewModel::onStartDateSelected,
            onStartTimeSelected = recordViewModel::onStartTimeSelected,
            onEndDateSelected = recordViewModel::onEndDateSelected,
            onEndTimeSelected = recordViewModel::onEndTimeSelected,
            onSave = recordViewModel::save,
        )
    }
}
```

## `app/src/main/java/com/example/timelog/TimeLogApplication.kt`

```kotlin
package com.example.timelog

import android.app.Application
import com.example.timelog.data.local.AppDatabase
import com.example.timelog.data.repository.TimeLogRepository

class TimeLogApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: TimeLogRepository by lazy { TimeLogRepository(database) }
}
```

## `app/src/main/java/com/example/timelog/data/local/AppDatabase.kt`

```kotlin
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
```

## `app/src/main/java/com/example/timelog/data/local/dao/CategoryDao.kt`

```kotlin
package com.example.timelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.timelog.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category ORDER BY sortOrder ASC, id ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM category WHERE isSystem = 1 LIMIT 1")
    suspend fun getSystemCategory(): CategoryEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM category WHERE parentId IS :parentId")
    suspend fun maxSortOrder(parentId: Long?): Int

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

## `app/src/main/java/com/example/timelog/data/local/dao/EventDao.kt`

```kotlin
package com.example.timelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.timelog.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event ORDER BY startAt ASC, id ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT COALESCE(MAX(endAt), 0) FROM event")
    suspend fun latestEnd(): Long

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM event WHERE categoryId IN (:categoryIds)")
    suspend fun countByCategoryIds(categoryIds: List<Long>): Int

    @Query(
        "UPDATE event SET categoryId = :targetCategoryId, updatedAt = :now " +
            "WHERE categoryId IN (:categoryIds)",
    )
    suspend fun reassignCategory(categoryIds: List<Long>, targetCategoryId: Long, now: Long)
}
```

## `app/src/main/java/com/example/timelog/data/local/entity/CategoryEntity.kt`

```kotlin
package com.example.timelog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parentId")],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    val isSystem: Boolean = false,
)
```

## `app/src/main/java/com/example/timelog/data/local/entity/EventEntity.kt`

```kotlin
package com.example.timelog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 事件是一段时间块：startAt / endAt 是同一时区下的 epoch millis。
 * 标题已被移除，只保留可留空的备注。
 */
@Entity(
    tableName = "event",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("startAt")],
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
)
```

## `app/src/main/java/com/example/timelog/data/repository/TimeLogRepository.kt`

```kotlin
package com.example.timelog.data.repository

import androidx.room.withTransaction
import com.example.timelog.data.local.AppDatabase
import com.example.timelog.data.local.entity.CategoryEntity
import com.example.timelog.data.local.entity.EventEntity
import com.example.timelog.domain.model.Category
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.Event
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.buildCategoryTree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TimeLogRepository(private val database: AppDatabase) {
    private val categoryDao = database.categoryDao()
    private val eventDao = database.eventDao()

    val categories: Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    val categoryTree: Flow<CategoryTree> = categories.map(::buildCategoryTree)

    val timeline: Flow<List<TimelineEvent>> = combine(
        eventDao.observeAll(),
        categoryDao.observeAll(),
    ) { events, categories ->
        val tree = buildCategoryTree(categories.map { it.toDomain() })
        events.map { entity ->
            val event = entity.toDomain()
            TimelineEvent(
                event = event,
                categoryPath = tree.pathOf(event.categoryId),
                categoryColor = tree.find(event.categoryId)?.category?.colorHex ?: 0xFF9AA29A,
            )
        }
    }

    suspend fun getEvent(id: Long): Event? = eventDao.getById(id)?.toDomain()

    suspend fun latestEventEnd(): Long = eventDao.latestEnd()

    suspend fun saveEvent(
        id: Long?,
        note: String?,
        startAt: Long,
        endAt: Long,
        categoryId: Long,
    ) {
        val now = System.currentTimeMillis()
        val cleanNote = note?.trim()?.ifEmpty { null }
        val safeEndAt = endAt.coerceAtLeast(startAt + 60_000L)
        if (id == null) {
            eventDao.insert(
                EventEntity(
                    note = cleanNote,
                    startAt = startAt,
                    endAt = safeEndAt,
                    categoryId = categoryId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = eventDao.getById(id) ?: return
            eventDao.update(
                existing.copy(
                    note = cleanNote,
                    startAt = startAt,
                    endAt = safeEndAt,
                    categoryId = categoryId,
                    updatedAt = now,
                ),
            )
        }
    }

    suspend fun deleteEvent(id: Long) {
        eventDao.deleteById(id)
    }

    suspend fun createCategory(name: String, parentId: Long?, colorHex: Long): Long {
        val order = categoryDao.maxSortOrder(parentId) + 1
        return categoryDao.insert(
            CategoryEntity(
                name = name.trim(),
                parentId = parentId,
                sortOrder = order,
                colorHex = colorHex,
            ),
        )
    }

    suspend fun updateCategory(id: Long, name: String, colorHex: Long) {
        val existing = categoryDao.getById(id) ?: return
        categoryDao.update(existing.copy(name = name.trim(), colorHex = colorHex))
    }

    suspend fun countEventsInSubtree(categoryId: Long): Int {
        val tree = buildCategoryTree(categoryDao.getAll().map { it.toDomain() })
        val ids = tree.subtreeIds(categoryId)
        return if (ids.isEmpty()) 0 else eventDao.countByCategoryIds(ids)
    }

    /**
     * 删除分类时把该分类及其子树下的事件迁移到「未分类」，避免用户整理分类时丢数据。
     * 事件外键是 RESTRICT，必须先迁移事件，才能删除分类；子分类由自引用外键 CASCADE 清理。
     */
    suspend fun deleteCategory(categoryId: Long): CategoryDeletionResult = database.withTransaction {
        val all = categoryDao.getAll().map { it.toDomain() }
        val tree = buildCategoryTree(all)
        val node = tree.find(categoryId) ?: return@withTransaction CategoryDeletionResult.NotFound
        if (node.category.isSystem) return@withTransaction CategoryDeletionResult.SystemCategory

        val ids = tree.subtreeIds(categoryId)
        val eventCount = eventDao.countByCategoryIds(ids)
        val uncategorized = categoryDao.getSystemCategory()
            ?: error("系统分类「未分类」缺失")
        if (eventCount > 0) {
            eventDao.reassignCategory(ids, uncategorized.id, System.currentTimeMillis())
        }
        categoryDao.deleteById(categoryId)
        CategoryDeletionResult.Deleted(
            categoryCount = ids.size,
            reassignedEventCount = eventCount,
        )
    }

    private fun CategoryEntity.toDomain(): Category = Category(
        id = id,
        name = name,
        parentId = parentId,
        sortOrder = sortOrder,
        colorHex = colorHex,
        isSystem = isSystem,
    )

    private fun EventEntity.toDomain(): Event = Event(
        id = id,
        note = note,
        startAt = startAt,
        endAt = endAt,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

sealed interface CategoryDeletionResult {
    data class Deleted(val categoryCount: Int, val reassignedEventCount: Int) : CategoryDeletionResult
    data object NotFound : CategoryDeletionResult
    data object SystemCategory : CategoryDeletionResult
}
```

## `app/src/main/java/com/example/timelog/data/seed/DefaultCategories.kt`

```kotlin
package com.example.timelog.data.seed

/**
 * 首次建库时写入的默认分类。固定主键是为了在同一个事务里安全地写入父子关系，
 * 同时给「未分类」一个稳定 ID，删除其它分类时可以把事件迁移过去。
 *
 * 默认顺序：工作（执行、沟通）→ 休息（睡眠、放空）→ 学习（输入、输出）→ 娱乐（社交、沉浸）。
 */
data class SeedCategory(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    val isSystem: Boolean = false,
)

object DefaultCategories {
    val all: List<SeedCategory> = listOf(
        SeedCategory(1, "工作", null, 0, 0xFF3D6BDB),
        SeedCategory(11, "执行", 1, 0, 0xFF4C7BE8),
        SeedCategory(12, "沟通", 1, 1, 0xFF2F5DAF),
        SeedCategory(2, "休息", null, 1, 0xFF8A63C9),
        SeedCategory(21, "睡眠", 2, 0, 0xFF9C79D6),
        SeedCategory(22, "放空", 2, 1, 0xFF7150A8),
        SeedCategory(3, "学习", null, 2, 0xFFC07A2B),
        SeedCategory(31, "输入", 3, 0, 0xFFD99445),
        SeedCategory(32, "输出", 3, 1, 0xFF9E6220),
        SeedCategory(4, "娱乐", null, 3, 0xFFB4654A),
        SeedCategory(41, "社交", 4, 0, 0xFFCC7C5E),
        SeedCategory(42, "沉浸", 4, 1, 0xFF8E4A35),
        SeedCategory(5, "未分类", null, 4, 0xFF8C928C, isSystem = true),
    )
}
```

## `app/src/main/java/com/example/timelog/domain/model/Category.kt`

```kotlin
package com.example.timelog.domain.model

data class Category(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    val isSystem: Boolean = false,
)
```

## `app/src/main/java/com/example/timelog/domain/model/CategoryTree.kt`

```kotlin
package com.example.timelog.domain.model

data class CategoryNode(
    val category: Category,
    val children: List<CategoryNode> = emptyList(),
)

data class CategoryTree(
    val roots: List<CategoryNode> = emptyList(),
) {
    fun find(id: Long): CategoryNode? = findIn(roots, id)

    fun pathOf(id: Long): String = pathSegments(id).joinToString(" / ")

    fun pathSegments(id: Long): List<String> {
        val path = mutableListOf<String>()
        var current = find(id)
        while (current != null) {
            path.add(0, current.category.name)
            current = current.category.parentId?.let(::find)
        }
        return path
    }

    fun ancestorIds(id: Long): Set<Long> {
        val ancestors = linkedSetOf<Long>()
        var current = find(id)?.category?.parentId?.let(::find)
        while (current != null) {
            ancestors += current.category.id
            current = current.category.parentId?.let(::find)
        }
        return ancestors
    }

    fun depthOf(id: Long): Int {
        var depth = 0
        var current = find(id)
        while (current != null) {
            depth += 1
            current = current.category.parentId?.let(::find)
        }
        return depth
    }

    fun subtreeIds(id: Long): List<Long> {
        val node = find(id) ?: return emptyList()
        val result = mutableListOf<Long>()
        fun collect(current: CategoryNode) {
            result += current.category.id
            current.children.forEach(::collect)
        }
        collect(node)
        return result
    }

    fun flatten(): List<Pair<CategoryNode, Int>> {
        val result = mutableListOf<Pair<CategoryNode, Int>>()
        fun collect(nodes: List<CategoryNode>, depth: Int) {
            nodes.forEach { node ->
                result += node to depth
                collect(node.children, depth + 1)
            }
        }
        collect(roots, 0)
        return result
    }

    private fun findIn(nodes: List<CategoryNode>, id: Long): CategoryNode? {
        nodes.forEach { node ->
            if (node.category.id == id) return node
            findIn(node.children, id)?.let { return it }
        }
        return null
    }
}

fun buildCategoryTree(categories: List<Category>): CategoryTree {
    val byParent = categories.groupBy { it.parentId }
    fun build(parentId: Long?): List<CategoryNode> =
        byParent[parentId].orEmpty()
            .sortedWith(compareBy<Category>({ it.sortOrder }, { it.id }))
            .map { category -> CategoryNode(category, build(category.id)) }
    return CategoryTree(build(null))
}
```

## `app/src/main/java/com/example/timelog/domain/model/Event.kt`

```kotlin
package com.example.timelog.domain.model

data class Event(
    val id: Long,
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    /** 时长毫秒，永远落在 [60_000, ...] 里，避免脏数据产生零宽 / 负宽事件。 */
    val durationMillis: Long get() = (endAt - startAt).coerceAtLeast(60_000L)
}

data class TimelineEvent(
    val event: Event,
    val categoryPath: String,
    val categoryColor: Long,
)
```

## `app/src/main/java/com/example/timelog/domain/model/Timeline.kt`

```kotlin
package com.example.timelog.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 时间流里连续的一块。events 为空时表示「未记录」的时间，由「未分类」占位块自动填充。
 *
 * 设计约定：
 * - 每天从 00:00 开始、到今天「现在」或次日 00:00 结束，中间不允许出现空洞；
 * - 相邻（首尾相接）事件是两个块，交界处显示时间；
 * - 重叠事件合并成同一个块，块内逐条列出。
 */
data class TimelineSegment(
    val startAt: Long,
    val endAt: Long,
    val events: List<TimelineEvent>,
) {
    val isUnrecorded: Boolean get() = events.isEmpty()
}

data class TimelineDay(
    val date: LocalDate,
    val isToday: Boolean,
    val segments: List<TimelineSegment>,
)

object TimelineBuilder {

    fun build(
        events: List<TimelineEvent>,
        today: LocalDate,
        now: Long,
        zone: ZoneId,
    ): List<TimelineDay> {
        if (events.isEmpty()) return emptyList()

        val byDate = sortedMapOf<LocalDate, MutableList<TimelineEvent>>()
        events.forEach { item ->
            val startAt = item.event.startAt
            val endAt = item.event.startAt + item.event.durationMillis
            val startDate = dateOf(startAt, zone)
            val endDate = dateOf(endAt - 1, zone)
            var date = startDate
            while (!date.isAfter(endDate)) {
                byDate.getOrPut(date) { mutableListOf() } += item
                date = date.plusDays(1)
            }
        }

        return byDate.map { (date, dayEvents) ->
            buildDay(
                date = date,
                events = dayEvents,
                today = today,
                now = now,
                zone = zone,
            )
        }
    }

    private fun buildDay(
        date: LocalDate,
        events: List<TimelineEvent>,
        today: LocalDate,
        now: Long,
        zone: ZoneId,
    ): TimelineDay {
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val nextStart = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val clipped = events.mapNotNull { item ->
            val start = maxOf(item.event.startAt, dayStart)
            val end = minOf(item.event.startAt + item.event.durationMillis, nextStart)
            if (end <= start) null else ClippedEvent(start, end, item)
        }.sortedWith(compareBy({ it.start }, { it.end }, { it.item.event.id }))

        val clusters = buildClusters(clipped)
        val lastEnd = clusters.lastOrNull()?.end ?: dayStart
        val endLimit = when {
            date.isAfter(today) -> lastEnd
            date.isBefore(today) -> nextStart
            else -> maxOf(now, lastEnd).coerceAtMost(nextStart)
        }

        val segments = mutableListOf<TimelineSegment>()
        var cursor = dayStart
        clusters.forEach { cluster ->
            val start = maxOf(cluster.start, cursor)
            if (start > cursor) {
                segments += TimelineSegment(cursor, start, emptyList())
            }
            if (cluster.end > start) {
                segments += TimelineSegment(start, cluster.end, cluster.events)
            }
            cursor = cluster.end
        }
        if (cursor < endLimit) {
            segments += TimelineSegment(cursor, endLimit, emptyList())
        }

        return TimelineDay(date = date, isToday = date == today, segments = segments)
    }

    private fun buildClusters(events: List<ClippedEvent>): List<Cluster> {
        val clusters = mutableListOf<Cluster>()
        events.forEach { clipped ->
            val last = clusters.lastOrNull()
            if (last != null && clipped.start < last.end) {
                if (clipped.end > last.end) last.end = clipped.end
                last.events += clipped.item
            } else {
                clusters += Cluster(
                    start = clipped.start,
                    end = clipped.end,
                    events = mutableListOf(clipped.item),
                )
            }
        }
        return clusters
    }

    private fun dateOf(epochMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    private data class ClippedEvent(val start: Long, val end: Long, val item: TimelineEvent)

    private data class Cluster(
        var start: Long,
        var end: Long,
        val events: MutableList<TimelineEvent>,
    )
}
```

## `app/src/main/java/com/example/timelog/ui/category/CategoryManageScreen.kt`

```kotlin
package com.example.timelog.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.timelog.domain.model.CategoryNode
import com.example.timelog.ui.home.CategoryDot

private val EditorColors = listOf(
    0xFF3D6BDB,
    0xFF2F8F5B,
    0xFFC07A2B,
    0xFF8A63C9,
    0xFFB4654A,
    0xFF2F7E8F,
    0xFF8C928C,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    state: CategoryManageUiState,
    onBack: () -> Unit,
    onAddRoot: () -> Unit,
    onAddChild: (CategoryNode) -> Unit,
    onEdit: (CategoryNode) -> Unit,
    onDelete: (CategoryNode) -> Unit,
    onEditorNameChange: (String) -> Unit,
    onEditorColorChange: (Long) -> Unit,
    onEditorDismiss: () -> Unit,
    onEditorSave: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("分类") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddRoot) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "新建分类",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item(key = "hint") {
                Text(
                    text = "分类最多 3 层。删除分类时，其中的事件会移到「未分类」。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
                )
            }
            items(
                items = state.tree.flatten(),
                key = { it.first.category.id },
            ) { (node, depth) ->
                CategoryRow(
                    node = node,
                    depth = depth,
                    onAddChild = onAddChild,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
    }

    state.editor?.let { editor ->
        AlertDialog(
            onDismissRequest = onEditorDismiss,
            title = {
                Text(if (editor.id == null) "新建分类" else "编辑分类")
            },
            text = {
                Column {
                    if (editor.parentName != null) {
                        Text(
                            text = "父分类：${editor.parentName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = editor.name,
                        onValueChange = onEditorNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("分类名称") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "颜色",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EditorColors.forEach { colorHex ->
                            ColorSwatch(
                                colorHex = colorHex,
                                selected = colorHex == editor.colorHex,
                                onClick = { onEditorColorChange(colorHex) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onEditorSave,
                    enabled = editor.name.isNotBlank(),
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = onEditorDismiss) {
                    Text("取消")
                }
            },
        )
    }

    state.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("删除分类？") },
            text = {
                Text(
                    buildString {
                        append("「${pending.node.category.name}」及其子分类会被删除。")
                        if (pending.eventCount > 0) {
                            append("${pending.eventCount} 条事件会移动到「未分类」，不会丢失。")
                        }
                        append("此操作无法撤销。")
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDeleteConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun CategoryRow(
    node: CategoryNode,
    depth: Int,
    onAddChild: (CategoryNode) -> Unit,
    onEdit: (CategoryNode) -> Unit,
    onDelete: (CategoryNode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(node) }
            .padding(start = (20 + depth * 18).dp, end = 8.dp)
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryDot(colorHex = node.category.colorHex, size = 10.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = node.category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (node.children.isNotEmpty()) {
                Text(
                    text = "${node.children.size} 个子分类",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (node.category.isSystem) {
            Text(
                text = "系统",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        if (depth < 2) {
            IconButton(onClick = { onAddChild(node) }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "添加子分类",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!node.category.isSystem) {
            IconButton(onClick = { onDelete(node) }) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "删除分类",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(colorHex: Long, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(colorHex), CircleShape)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}
```

## `app/src/main/java/com/example/timelog/ui/category/CategoryManageViewModel.kt`

```kotlin
package com.example.timelog.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timelog.data.repository.CategoryDeletionResult
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.CategoryNode
import com.example.timelog.domain.model.CategoryTree
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryEditor(
    val id: Long?,
    val parentId: Long?,
    val parentName: String?,
    val name: String,
    val colorHex: Long,
)

data class PendingCategoryDelete(
    val node: CategoryNode,
    val eventCount: Int,
)

data class CategoryManageUiState(
    val tree: CategoryTree = CategoryTree(),
    val editor: CategoryEditor? = null,
    val pendingDelete: PendingCategoryDelete? = null,
    val message: String? = null,
)

class CategoryManageViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private data class Transient(
        val editor: CategoryEditor? = null,
        val pendingDelete: PendingCategoryDelete? = null,
        val message: String? = null,
    )

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<CategoryManageUiState> = combine(
        repository.categoryTree,
        transient,
    ) { tree, local ->
        CategoryManageUiState(
            tree = tree,
            editor = local.editor,
            pendingDelete = local.pendingDelete,
            message = local.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryManageUiState(),
    )

    fun startCreate(parent: CategoryNode?) {
        val tree = uiState.value.tree
        // depthOf 是 1 基深度；MAX_DEPTH 是 0 基的最大层级索引，因此这里必须是 >。
        if (parent != null && tree.depthOf(parent.category.id) > MAX_DEPTH) {
            transient.update { it.copy(message = "分类最多支持 3 层") }
            return
        }
        transient.update {
            it.copy(
                editor = CategoryEditor(
                    id = null,
                    parentId = parent?.category?.id,
                    parentName = parent?.category?.name,
                    name = "",
                    colorHex = DEFAULT_COLORS.random(),
                ),
            )
        }
    }

    fun startEdit(node: CategoryNode) {
        transient.update {
            it.copy(
                editor = CategoryEditor(
                    id = node.category.id,
                    parentId = node.category.parentId,
                    parentName = node.category.parentId?.let { id ->
                        uiState.value.tree.find(id)?.category?.name
                    },
                    name = node.category.name,
                    colorHex = node.category.colorHex,
                ),
            )
        }
    }

    fun onEditorNameChange(value: String) {
        transient.update { current ->
            current.copy(editor = current.editor?.copy(name = value))
        }
    }

    fun onEditorColorChange(colorHex: Long) {
        transient.update { current ->
            current.copy(editor = current.editor?.copy(colorHex = colorHex))
        }
    }

    fun dismissEditor() {
        transient.update { it.copy(editor = null) }
    }

    fun saveEditor() {
        val editor = transient.value.editor ?: return
        val name = editor.name.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            if (editor.id == null) {
                repository.createCategory(name, editor.parentId, editor.colorHex)
            } else {
                repository.updateCategory(editor.id, name, editor.colorHex)
            }
            transient.update { it.copy(editor = null) }
        }
    }

    fun requestDelete(node: CategoryNode) {
        if (node.category.isSystem) {
            transient.update { it.copy(message = "「未分类」用于兜底，不能删除") }
            return
        }
        viewModelScope.launch {
            val count = repository.countEventsInSubtree(node.category.id)
            transient.update { it.copy(pendingDelete = PendingCategoryDelete(node, count)) }
        }
    }

    fun dismissDelete() {
        transient.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val pending = transient.value.pendingDelete ?: return
        viewModelScope.launch {
            val message = when (val outcome = repository.deleteCategory(pending.node.category.id)) {
                is CategoryDeletionResult.Deleted -> {
                    val moved = if (outcome.reassignedEventCount > 0) {
                        "，${outcome.reassignedEventCount} 条事件移到「未分类」"
                    } else {
                        ""
                    }
                    "已删除 ${outcome.categoryCount} 个分类$moved"
                }

                CategoryDeletionResult.NotFound -> "分类已不存在"
                CategoryDeletionResult.SystemCategory -> "系统分类不能删除"
            }
            transient.update { it.copy(pendingDelete = null, message = message) }
        }
    }

    fun consumeMessage() {
        transient.update { it.copy(message = null) }
    }

    companion object {
        private const val MAX_DEPTH = 2
        private val DEFAULT_COLORS = listOf(
            0xFF3D6BDB,
            0xFF2F8F5B,
            0xFFC07A2B,
            0xFF8A63C9,
            0xFFB4654A,
            0xFF2F7E8F,
        )

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoryManageViewModel(repository) }
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/home/HomeScreen.kt`

```kotlin
package com.example.timelog.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timelog.ui.theme.Clay
import com.example.timelog.util.DateTimeFormat

@Composable
fun HomeScreen(
    state: HomeUiState,
    onNewRecord: () -> Unit,
    onOpenCategories: () -> Unit,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HomeHeader(onOpenCategories = onOpenCategories) },
        bottomBar = { RecordBar(onNewRecord = onNewRecord) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Unit
                state.days.isEmpty() -> EmptyTimeline()
                else -> TimelineList(
                    days = state.days,
                    onEventClick = onEventClick,
                    onEventLongClick = onEventLongClick,
                    onFillGap = onFillGap,
                )
            }
        }
    }

    state.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("删除这段记录？") },
            text = {
                Column {
                    Text(
                        "「${pending.categoryPath}」" +
                            DateTimeFormat.rangeText(pending.event.startAt, pending.event.endAt),
                    )
                    pending.event.note?.let { note ->
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text("取消") }
            },
        )
    }
}

@Composable
private fun HomeHeader(onOpenCategories: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "時計",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onOpenCategories) {
            Text("分类", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun RecordBar(onNewRecord: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
    ) {
        Button(
            onClick = onNewRecord,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("完成", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun EmptyTimeline() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "还没有记录。",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "点下方的「完成」，记下刚刚做完的事。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimelineList(
    days: List<HomeDay>,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
) {
    val listState = rememberLazyListState()
    val itemCount = days.size + days.sumOf { it.segments.size } + days.count { it.isToday }
    // 倒序改成正序后，最新的记录在列表底部，靠近「完成」键；第一次打开与新增记录后都停在底部。
    val lastRecordedAt = remember(days) {
        days.flatMap { it.segments }.filterNot { it.isUnrecorded }
            .maxOfOrNull { it.endAt } ?: 0L
    }
    var hasPositioned by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(lastRecordedAt, itemCount) {
        if (itemCount == 0) return@LaunchedEffect
        if (!hasPositioned) {
            listState.scrollToItem(itemCount - 1)
            hasPositioned = true
        } else if (lastRecordedAt > 0L) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
    ) {
        days.forEach { day ->
            item(key = "day-${day.date}") {
                DayHeader(day = day)
            }
            items(
                items = day.segments,
                key = { segment ->
                    "seg-${day.date}-${segment.startAt}-" +
                        segment.events.joinToString(",") { it.event.id.toString() }
                },
            ) { segment ->
                val single = segment.events.singleOrNull()
                TimelineSegmentRow(
                    segment = segment,
                    onClick = single?.let { event -> { onEventClick(event.event.id) } },
                    onLongClick = single?.let { event -> { onEventLongClick(event.event.id) } },
                    onFillGap = { onFillGap(segment.startAt, segment.endAt) },
                )
            }
            if (day.isToday) {
                item(key = "now-${day.date}") {
                    NowMarker()
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: HomeDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = day.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = day.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

@Composable
private fun NowMarker() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "现在",
                style = MaterialTheme.typography.labelSmall,
                color = Clay,
                modifier = Modifier.padding(end = 2.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}
```

## `app/src/main/java/com/example/timelog/ui/home/HomeViewModel.kt`

```kotlin
package com.example.timelog.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.TimelineBuilder
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.TimelineSegment
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 首页一天的展示数据（标题由 UI 语言层负责，时间轴本身在 domain 里）。 */
data class HomeDay(
    val date: LocalDate,
    val title: String,
    val subtitle: String,
    val isToday: Boolean,
    val segments: List<TimelineSegment>,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val days: List<HomeDay> = emptyList(),
    val events: List<TimelineEvent> = emptyList(),
    val editor: EditorTarget? = null,
    val pendingDelete: TimelineEvent? = null,
)

sealed interface EditorTarget {
    /** 新建；startAt / endAt 非空时表示由「未分类」空隙点进来，直接预填这段时间。 */
    data class New(val startAt: Long? = null, val endAt: Long? = null) : EditorTarget
    data class Edit(val eventId: Long) : EditorTarget
}

class HomeViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private data class Transient(
        val editor: EditorTarget? = null,
        val pendingDeleteId: Long? = null,
    )

    private val transient = MutableStateFlow(Transient())

    /** 每分钟推进一次「现在」，让今天的未记录尾巴和日期分界自动生长。 */
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000L)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.timeline,
        clock,
        transient,
    ) { events, now, local ->
        val today = DateTimeFormat.toLocalDateTime(now).toLocalDate()
        val days = TimelineBuilder.build(
            events = events,
            today = today,
            now = now,
            zone = DateTimeFormat.zone(),
        ).map { day ->
            HomeDay(
                date = day.date,
                title = DateTimeFormat.sectionTitle(day.date, today),
                subtitle = DateTimeFormat.sectionSubtitle(day.date, today),
                isToday = day.isToday,
                segments = day.segments,
            )
        }
        HomeUiState(
            isLoading = false,
            days = days,
            events = events,
            editor = local.editor,
            pendingDelete = events.firstOrNull { it.event.id == local.pendingDeleteId },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun openNewRecord() {
        transient.update { it.copy(editor = EditorTarget.New()) }
    }

    /** 点击未记录的「未分类」空隙：新建记录并预填这段时间。 */
    fun openFillRecord(startAt: Long, endAt: Long) {
        transient.update { it.copy(editor = EditorTarget.New(startAt = startAt, endAt = endAt)) }
    }

    fun openEditRecord(eventId: Long) {
        transient.update { it.copy(editor = EditorTarget.Edit(eventId)) }
    }

    fun closeRecord() {
        transient.update { it.copy(editor = null) }
    }

    fun requestDelete(eventId: Long) {
        transient.update { it.copy(pendingDeleteId = eventId) }
    }

    fun dismissDelete() {
        transient.update { it.copy(pendingDeleteId = null) }
    }

    fun confirmDelete() {
        val id = transient.value.pendingDeleteId ?: return
        transient.update { it.copy(pendingDeleteId = null) }
        viewModelScope.launch { repository.deleteEvent(id) }
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(repository) as T
            }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/home/TimelineItem.kt`

```kotlin
package com.example.timelog.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timelog.domain.model.TimelineSegment
import com.example.timelog.util.DateTimeFormat

private val BlockShape = RoundedCornerShape(14.dp)

@Composable
fun CategoryDot(
    colorHex: Long,
    size: Dp = 7.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color(colorHex)),
    )
}

/**
 * 时间流里的一行：左侧只在块与块的交接处显示时间，右侧是块本体。
 * 未记录的时间由虚线「未分类」块占位。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineSegmentRow(
    segment: TimelineSegment,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onFillGap: (() -> Unit)? = null,
) {
    val height = rememberBlockHeight(segment)
    val hairline = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(modifier = Modifier.width(44.dp).height(height)) {
            Text(
                text = DateTimeFormat.time(segment.startAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 3.dp),
            )
        }
        androidx.compose.foundation.Canvas(modifier = Modifier.width(6.dp).height(height)) {
            val y = 8.dp.toPx()
            drawLine(
                color = hairline,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }
        Spacer(Modifier.width(4.dp))

        val blockModifier = Modifier
            .weight(1f)
            .height(height)
        if (segment.isUnrecorded) {
            // 空隙本身就是一段「未分类」时间：点它可以就地补记。
            val fillable = if (onFillGap != null && segment.endAt - segment.startAt >= 60_000L) {
                blockModifier.combinedClickable(onClick = { onFillGap() }, onLongClick = {})
            } else {
                blockModifier
            }
            UnrecordedBlock(segment = segment, modifier = fillable)
        } else {
            val clickable = if (onClick != null || onLongClick != null) {
                blockModifier.combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = { onLongClick?.invoke() },
                )
            } else {
                blockModifier
            }
            RecordedBlock(segment = segment, modifier = clickable)
        }
    }
}

@Composable
private fun RecordedBlock(segment: TimelineSegment, modifier: Modifier) {
    val accent = Color(segment.events.first().categoryColor)
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val textColor = if (darkTheme) lerp(accent, Color.White, 0.35f) else accent
    val background = accent.copy(alpha = if (darkTheme) 0.20f else 0.09f)
    val border = accent.copy(alpha = if (darkTheme) 0.38f else 0.22f)

    Box(
        modifier = modifier
            .clip(BlockShape)
            .background(background)
            .border(1.dp, border, BlockShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(accent.copy(alpha = if (darkTheme) 0.85f else 0.7f)),
        )
        Column(
            modifier = Modifier.padding(start = 13.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        ) {
            segment.events.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryDot(colorHex = item.categoryColor, size = 6.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.categoryPath,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                item.event.note?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 12.dp, top = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun UnrecordedBlock(segment: TimelineSegment, modifier: Modifier) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    val height = rememberBlockHeight(segment)
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                shape = BlockShape,
            )
            .drawBehind {
                val stroke = 1.dp.toPx()
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(
                                left = stroke / 2f,
                                top = stroke / 2f,
                                right = size.width - stroke / 2f,
                                bottom = size.height - stroke / 2f,
                            ),
                            cornerRadius = CornerRadius(14.dp.toPx()),
                        ),
                    )
                }
                drawPath(
                    path = path,
                    color = outline,
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(5.dp.toPx(), 5.dp.toPx()),
                        ),
                    ),
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Column {
            Text(
                text = "未分类",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (height >= 62.dp) {
                Text(
                    text = DateTimeFormat.durationText(segment.startAt, segment.endAt),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/** 块高按分钟数近似线性，并做上下限，避免 8 小时块占满整屏。 */
@Composable
private fun rememberBlockHeight(segment: TimelineSegment): Dp {
    val minutes = ((segment.endAt - segment.startAt).coerceAtLeast(60_000L)) / 60_000f
    return if (segment.isUnrecorded) {
        (42f + minutes * 0.45f).coerceIn(46f, 112f).dp
    } else {
        val extraRows = (segment.events.size - 1).coerceAtLeast(0) * 30f
        (54f + minutes * 0.55f + extraRows).coerceIn(58f, 208f).dp
    }
}
```

## `app/src/main/java/com/example/timelog/ui/record/CategoryGrid.kt`

```kotlin
package com.example.timelog.ui.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.ui.home.CategoryDot

/**
 * 固定展开的分类选择区：不滚动、全部可见，一眼扫到就能点。
 * 每个根分类一行，根 + 子分类并排；行内放不下时 FlowRow 自动换行。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryGrid(
    tree: CategoryTree,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tree.roots.forEach { root ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryChip(
                    id = root.category.id,
                    name = root.category.name,
                    colorHex = root.category.colorHex,
                    selected = root.category.id == selectedId,
                    emphasized = true,
                    onSelect = onSelect,
                )
                root.children.forEach { child ->
                    CategoryChip(
                        id = child.category.id,
                        name = child.category.name,
                        colorHex = child.category.colorHex,
                        selected = child.category.id == selectedId,
                        emphasized = false,
                        onSelect = onSelect,
                    )
                    child.children.forEach { grandChild ->
                        CategoryChip(
                            id = grandChild.category.id,
                            name = grandChild.category.name,
                            colorHex = grandChild.category.colorHex,
                            selected = grandChild.category.id == selectedId,
                            emphasized = false,
                            onSelect = onSelect,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    id: Long,
    name: String,
    colorHex: Long,
    selected: Boolean,
    emphasized: Boolean,
    onSelect: (Long) -> Unit,
) {
    val color = Color(colorHex)
    Surface(
        onClick = { onSelect(id) },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        border = if (selected) {
            BorderStroke(1.5.dp, color)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(colorHex = colorHex, size = 7.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected || emphasized) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/record/RecordSheet.kt`

```kotlin
package com.example.timelog.ui.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime

private enum class PickerTarget { StartDate, StartTime, EndDate, EndTime }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSheet(
    state: RecordUiState,
    onDismiss: () -> Unit,
    onNoteChange: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onStartTimeSelected: (LocalTime) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var picker by remember { mutableStateOf<PickerTarget?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            Text(
                text = if (state.isEditing) "编辑记录" else "记录一段",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(18.dp))
            FieldLabel("分类")
            Spacer(Modifier.height(8.dp))
            CategoryGrid(
                tree = state.tree,
                selectedId = state.selectedCategoryId,
                onSelect = onCategorySelected,
            )

            Spacer(Modifier.height(20.dp))
            FieldLabel("时间")
            Spacer(Modifier.height(8.dp))
            TimeField(
                label = "开始",
                date = state.startDate,
                time = state.startTime,
                onDateClick = { picker = PickerTarget.StartDate },
                onTimeClick = { picker = PickerTarget.StartTime },
            )
            Spacer(Modifier.height(10.dp))
            TimeField(
                label = "结束",
                date = state.endDate,
                time = state.endTime,
                onDateClick = { picker = PickerTarget.EndDate },
                onTimeClick = { picker = PickerTarget.EndTime },
            )
            if (state.timeError) {
                Text(
                    text = "结束时间需要晚于开始时间",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 38.dp, top = 6.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel("备注")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("可留空", style = MaterialTheme.typography.bodyMedium) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp).widthIn(min = 112.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("保存", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    when (picker) {
        PickerTarget.StartDate -> DatePickerSheet(
            initialDate = state.startDate,
            onDismiss = { picker = null },
            onConfirm = { date ->
                picker = null
                onStartDateSelected(date)
            },
        )

        PickerTarget.EndDate -> DatePickerSheet(
            initialDate = state.endDate,
            onDismiss = { picker = null },
            onConfirm = { date ->
                picker = null
                onEndDateSelected(date)
            },
        )

        PickerTarget.StartTime -> TimePickerDialog(
            initialTime = state.startTime,
            onDismiss = { picker = null },
            onConfirm = { time ->
                picker = null
                onStartTimeSelected(time)
            },
        )

        PickerTarget.EndTime -> TimePickerDialog(
            initialTime = state.endTime,
            onDismiss = { picker = null },
            onConfirm = { time ->
                picker = null
                onEndTimeSelected(time)
            },
        )

        null -> Unit
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun TimeField(
    label: String,
    date: LocalDate,
    time: LocalTime,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(38.dp),
        )
        Surface(
            onClick = onDateClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            Text(
                text = DateTimeFormat.dateWithWeekday(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            onClick = onTimeClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        ) {
            Text(
                text = DateTimeFormat.clock(time),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateTimeFormat.startOfDayUtcMillis(initialDate),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onConfirm(DateTimeFormat.dateFromUtcMillis(it))
                    }
                },
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = pickerState)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(Modifier.width(4.dp))
                    TextButton(
                        onClick = {
                            onConfirm(LocalTime.of(pickerState.hour, pickerState.minute))
                        },
                    ) { Text("确定") }
                }
            }
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/record/RecordViewModel.kt`

```kotlin
package com.example.timelog.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.ui.home.EditorTarget
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordUiState(
    val isEditing: Boolean = false,
    val eventId: Long? = null,
    val note: String = "",
    val startDate: LocalDate = LocalDate.now(DateTimeFormat.zone()),
    val startTime: LocalTime = LocalTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0),
    val endDate: LocalDate = LocalDate.now(DateTimeFormat.zone()),
    val endTime: LocalTime = LocalTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0),
    val tree: CategoryTree = CategoryTree(),
    val selectedCategoryId: Long? = null,
    val timeError: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

class RecordViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            repository.categoryTree.collect { tree ->
                _uiState.update { state ->
                    val stillValid = state.selectedCategoryId?.takeIf { tree.find(it) != null }
                    val selected = stillValid ?: defaultCategoryId(tree)
                    state.copy(tree = tree, selectedCategoryId = selected)
                }
            }
        }
    }

    fun start(target: EditorTarget) {
        loadJob?.cancel()
        _uiState.update { it.copy(isSaving = false, saved = false, timeError = false) }
        when (target) {
            is EditorTarget.New -> startNew(target)
            is EditorTarget.Edit -> startEdit(target.eventId)
        }
    }

    private fun startNew(prefill: EditorTarget.New) {
        if (prefill.startAt != null && prefill.endAt != null && prefill.endAt > prefill.startAt) {
            val startLocal = DateTimeFormat.toLocalDateTime(prefill.startAt)
            val endLocal = DateTimeFormat.toLocalDateTime(prefill.endAt)
            _uiState.update {
                it.copy(
                    isEditing = false,
                    eventId = null,
                    note = "",
                    startDate = startLocal.toLocalDate(),
                    startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime().withSecond(0).withNano(0),
                    timeError = false,
                )
            }
            return
        }
        startNewNow()
    }

    private fun startNewNow() {
        val now = LocalDateTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0)
        val fallbackStart = now.minusMinutes(30)
        _uiState.update {
            it.copy(
                isEditing = false,
                eventId = null,
                note = "",
                startDate = fallbackStart.toLocalDate(),
                startTime = fallbackStart.toLocalTime(),
                endDate = now.toLocalDate(),
                endTime = now.toLocalTime(),
                timeError = false,
            )
        }
        loadJob = viewModelScope.launch {
            val nowMillis = DateTimeFormat.toEpochMillis(now.toLocalDate(), now.toLocalTime())
            val lastEnd = repository.latestEventEnd()
            // 接着上次记录继续：上一次结束在 12 小时内，就用它作为开始；否则回退到「半小时前」。
            val startMillis = if (lastEnd in (nowMillis - 12 * 3_600_000L)..(nowMillis - 60_000L)) {
                lastEnd
            } else {
                nowMillis - 30 * 60_000L
            }
            val startLocal = DateTimeFormat.toLocalDateTime(startMillis)
            _uiState.update {
                it.copy(
                    startDate = startLocal.toLocalDate(),
                    startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                    endDate = now.toLocalDate(),
                    endTime = now.toLocalTime(),
                )
            }
        }
    }

    private fun startEdit(eventId: Long) {
        loadJob = viewModelScope.launch {
            val event = repository.getEvent(eventId) ?: return@launch
            val startLocal = DateTimeFormat.toLocalDateTime(event.startAt)
            val endLocal = DateTimeFormat.toLocalDateTime(event.endAt)
            _uiState.update {
                it.copy(
                    isEditing = true,
                    eventId = event.id,
                    note = event.note.orEmpty(),
                    startDate = startLocal.toLocalDate(),
                    startTime = startLocal.toLocalTime().withSecond(0).withNano(0),
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime().withSecond(0).withNano(0),
                    selectedCategoryId = event.categoryId,
                    timeError = false,
                )
            }
        }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onStartDateSelected(date: LocalDate) = updateStart(date, _uiState.value.startTime)

    fun onStartTimeSelected(time: LocalTime) =
        updateStart(_uiState.value.startDate, time.withSecond(0).withNano(0))

    /**
     * 改开始时间时顺手把结束时间推到「开始 + 30 分钟」，避免出现负区间；
     * 改结束时间不做自动纠正，交给保存时的校验提示。
     */
    private fun updateStart(date: LocalDate, time: LocalTime) {
        _uiState.update { state ->
            val startMillis = DateTimeFormat.toEpochMillis(date, time)
            val endMillis = DateTimeFormat.toEpochMillis(state.endDate, state.endTime)
            if (endMillis <= startMillis) {
                val endLocal = DateTimeFormat.toLocalDateTime(startMillis + 30 * 60_000L)
                state.copy(
                    startDate = date,
                    startTime = time,
                    endDate = endLocal.toLocalDate(),
                    endTime = endLocal.toLocalTime(),
                    timeError = false,
                )
            } else {
                state.copy(startDate = date, startTime = time, timeError = false)
            }
        }
    }

    fun onEndDateSelected(date: LocalDate) {
        _uiState.update { it.copy(endDate = date, timeError = false) }
    }

    fun onEndTimeSelected(time: LocalTime) {
        _uiState.update {
            it.copy(endTime = time.withSecond(0).withNano(0), timeError = false)
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val categoryId = state.selectedCategoryId ?: return
        val startAt = DateTimeFormat.toEpochMillis(state.startDate, state.startTime)
        val endAt = DateTimeFormat.toEpochMillis(state.endDate, state.endTime)
        if (endAt <= startAt) {
            _uiState.update { it.copy(timeError = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true, timeError = false) }
        viewModelScope.launch {
            runCatching {
                repository.saveEvent(
                    id = state.eventId,
                    note = state.note,
                    startAt = startAt,
                    endAt = endAt,
                    categoryId = categoryId,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun reset() {
        loadJob?.cancel()
        _uiState.update { current ->
            RecordUiState(
                tree = current.tree,
                selectedCategoryId = current.selectedCategoryId,
            )
        }
    }

    private fun defaultCategoryId(tree: CategoryTree): Long? {
        // 默认落在第一个叶子分类（工作 / 执行）上，方便快速连记。
        return tree.flatten().firstOrNull { it.first.children.isEmpty() }
            ?.first?.category?.id
            ?: tree.roots.firstOrNull()?.category?.id
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecordViewModel(repository) }
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/theme/Color.kt`

```kotlin
package com.example.timelog.ui.theme

import androidx.compose.ui.graphics.Color

val Paper = Color(0xFFFBFAF7)
val PaperDark = Color(0xFF121412)
val Ink = Color(0xFF1E211F)
val InkMuted = Color(0xFF6E746F)
val Pine = Color(0xFF2F5D50)
val PineSoft = Color(0xFFE6EFEA)
val Clay = Color(0xFFB4654A)
val Hairline = Color(0xFFE4E1DA)

val NightSurface = Color(0xFF1A1D1A)
val NightInk = Color(0xFFE7EAE5)
val NightMuted = Color(0xFFA1A8A1)
val NightPine = Color(0xFFA8D5C2)
val NightHairline = Color(0xFF313631)
```

## `app/src/main/java/com/example/timelog/ui/theme/Theme.kt`

```kotlin
package com.example.timelog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = PineSoft,
    onPrimaryContainer = Pine,
    secondary = Clay,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF2F0EB),
    onSurfaceVariant = InkMuted,
    outline = Color(0xFFB6B2A8),
    outlineVariant = Hairline,
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = NightPine,
    onPrimary = Color(0xFF17332B),
    primaryContainer = Color(0xFF2A4A3F),
    onPrimaryContainer = NightInk,
    secondary = Color(0xFFE0A48C),
    onSecondary = Color(0xFF3F2118),
    background = PaperDark,
    onBackground = NightInk,
    surface = PaperDark,
    onSurface = NightInk,
    surfaceVariant = NightSurface,
    onSurfaceVariant = NightMuted,
    outline = Color(0xFF757B75),
    outlineVariant = NightHairline,
    error = Color(0xFFFFB4AB),
)

@Composable
fun TimeLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TimeLogTypography,
        content = content,
    )
}
```

## `app/src/main/java/com/example/timelog/ui/theme/Type.kt`

```kotlin
package com.example.timelog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TimeLogTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)
```

## `app/src/main/java/com/example/timelog/util/DateTimeFormat.kt`

```kotlin
package com.example.timelog.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateTimeFormat {
    private val zh: Locale = Locale.SIMPLIFIED_CHINESE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", zh)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", zh)
    private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", zh)
    private val shortMonthDayFormatter = DateTimeFormatter.ofPattern("M月d日", zh)
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy年", zh)

    fun zone(): ZoneId = ZoneId.systemDefault()

    fun toLocalDateTime(epochMillis: Long): LocalDateTime =
        Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalDateTime()

    fun toEpochMillis(date: LocalDate, time: LocalTime): Long =
        LocalDateTime.of(date, time).atZone(zone()).toInstant().toEpochMilli()

    fun time(epochMillis: Long): String = toLocalDateTime(epochMillis).format(timeFormatter)

    fun clock(time: LocalTime): String = time.format(timeFormatter)

    fun dateWithWeekday(date: LocalDate): String = date.format(monthDayFormatter)

    fun monthDay(date: LocalDate): String = date.format(shortMonthDayFormatter)

    fun sectionTitle(date: LocalDate, today: LocalDate = LocalDate.now(zone())): String =
        when (date) {
            today -> "今天"
            today.minusDays(1) -> "昨天"
            else -> if (date.year == today.year) {
                date.format(monthDayFormatter)
            } else {
                date.format(fullDateFormatter)
            }
        }

    fun sectionSubtitle(date: LocalDate, today: LocalDate = LocalDate.now(zone())): String =
        when (date) {
            today, today.minusDays(1) -> date.format(monthDayFormatter)
            else -> date.format(yearFormatter) + " · " + date.dayOfWeek.getDisplayName(
                TextStyle.FULL,
                zh,
            )
        }

    fun fullDateTime(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(fullDateFormatter) + " " + time(epochMillis)

    fun datePart(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(shortMonthDayFormatter)

    fun dateWithWeekday(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(monthDayFormatter)

    fun isoDate(epochMillis: Long): String =
        toLocalDateTime(epochMillis).toLocalDate().toString()

    fun startOfDayUtcMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    fun dateFromUtcMillis(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()

    /** 事件块的时间范围，用于编辑 / 删除确认。 */
    fun rangeText(startAt: Long, endAt: Long): String {
        val start = toLocalDateTime(startAt)
        val end = toLocalDateTime(endAt)
        return if (start.toLocalDate() == end.toLocalDate()) {
            clock(start.toLocalTime()) + "–" + clock(end.toLocalTime())
        } else {
            monthDay(start.toLocalDate()) + " " + clock(start.toLocalTime()) +
                "–" + monthDay(end.toLocalDate()) + " " + clock(end.toLocalTime())
        }
    }

    fun durationText(startAt: Long, endAt: Long): String =
        durationText((endAt - startAt).coerceAtLeast(60_000L))

    fun durationText(millis: Long): String {
        val totalMinutes = (millis / 60_000L).coerceAtLeast(1L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            hours == 0L -> "${minutes} 分钟"
            minutes == 0L -> "${hours} 小时"
            else -> "${hours} 小时 ${minutes} 分"
        }
    }
}
```

## `app/src/main/res/drawable/ic_launcher_foreground.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M54,30 L78,78 L30,78 Z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M54,46 L66,70 L42,70 Z" />
</vector>
```

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

## `app/src/main/res/values-night/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.TimeLog" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#121412</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
```

## `app/src/main/res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#2F5D50</color>
    <color name="window_background">#FBFAF7</color>
</resources>
```

## `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">時計</string>
</resources>
```

## `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.TimeLog" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">@color/window_background</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">true</item>
    </style>
</resources>
```

## `app/src/test/java/com/example/timelog/domain/TimelineBuilderTest.kt`

```kotlin
package com.example.timelog.domain

import com.example.timelog.domain.model.Event
import com.example.timelog.domain.model.TimelineBuilder
import com.example.timelog.domain.model.TimelineDay
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.TimelineSegment
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineBuilderTest {

    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val today: LocalDate = LocalDate.of(2026, 11, 1)

    @Test
    fun emptyEventsProduceNoDays() {
        val days = TimelineBuilder.build(emptyList(), today, at(LocalDateTime.of(today, java.time.LocalTime.NOON)), zone)
        assertTrue(days.isEmpty())
    }

    @Test
    fun fillsWholePastDayAndSortsAscending() {
        val days = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00"),
            event(2, "2026-10-31T11:00", "2026-10-31T12:00"),
        )
        assertEquals(1, days.size)
        val segments = days.single().segments
        assertEquals(5, segments.size)
        assertTrue(segments[0].isUnrecorded)
        assertEquals(dayStart("2026-10-31"), segments[0].startAt)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(9, 0))), segments[0].endAt)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(9, 0))), segments[1].startAt)
        assertEquals(1L, segments[1].events.single().event.id)
        assertEquals(dayStart("2026-11-01"), segments.last().endAt)
        assertContiguous(segments)
    }

    @Test
    fun mergesOverlappingEventsIntoOneBlock() {
        val days = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:30"),
            event(2, "2026-10-31T10:00", "2026-10-31T11:00"),
        )
        val segments = days.single().segments
        assertEquals(3, segments.size)
        assertEquals(2, segments[1].events.size)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(9, 0))), segments[1].startAt)
        assertEquals(at(LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(11, 0))), segments[1].endAt)
        assertContiguous(segments)
    }

    @Test
    fun keepsTouchingEventsSeparate() {
        val days = build(
            event(1, "2026-10-31T09:00", "2026-10-31T10:00"),
            event(2, "2026-10-31T10:00", "2026-10-31T11:00"),
        )
        val segments = days.single().segments
        assertEquals(4, segments.size)
        assertEquals(1, segments[1].events.size)
        assertEquals(1, segments[2].events.size)
        assertEquals(segments[1].endAt, segments[2].startAt)
        assertContiguous(segments)
    }

    @Test
    fun todayEndsAtNow() {
        val days = build(
            event(1, "2026-11-01T08:00", "2026-11-01T09:00"),
            now = LocalDateTime.of(today, java.time.LocalTime.of(12, 30)),
        )
        val segments = days.single().segments
        assertEquals(at(LocalDateTime.of(today, java.time.LocalTime.of(12, 30))), segments.last().endAt)
        assertTrue(segments.last().isUnrecorded)
        assertContiguous(segments)
    }

    @Test
    fun splitsEventAcrossMidnight() {
        val days = build(
            event(1, "2026-10-31T23:00", "2026-11-01T01:00"),
            now = LocalDateTime.of(today, java.time.LocalTime.of(12, 0)),
        )
        assertEquals(2, days.size)
        val first = days[0]
        val second = days[1]
        assertEquals(LocalDate.of(2026, 10, 31), first.date)
        assertEquals(dayStart("2026-11-01"), first.segments.last().endAt)
        assertEquals(at(LocalDateTime.of(today, java.time.LocalTime.of(1, 0))), second.segments.first().endAt)
        assertEquals(1L, second.segments.first().events.single().event.id)
        assertTrue(second.isToday)
        days.forEach { assertContiguous(it.segments) }
    }

    @Test
    fun daySegmentsAreAscending() {
        val days = build(
            event(2, "2026-10-30T18:00", "2026-10-30T19:00"),
            event(1, "2026-10-30T08:00", "2026-10-30T09:00"),
        )
        val segments = days.single().segments
        segments.zipWithNext().forEach { (a, b) -> assertTrue(a.startAt <= b.startAt) }
    }

    private fun build(vararg events: TimelineEvent, now: LocalDateTime = LocalDateTime.of(today, java.time.LocalTime.of(20, 0))): List<TimelineDay> =
        TimelineBuilder.build(events.toList(), today, at(now), zone)

    private fun event(id: Long, start: String, end: String): TimelineEvent = TimelineEvent(
        event = Event(
            id = id,
            note = null,
            startAt = at(LocalDateTime.parse(start)),
            endAt = at(LocalDateTime.parse(end)),
            categoryId = 1L,
            createdAt = 0L,
            updatedAt = 0L,
        ),
        categoryPath = "工作 / 执行",
        categoryColor = 0xFF4C7BE8,
    )

    private fun at(dateTime: LocalDateTime): Long =
        dateTime.atZone(zone).toInstant().toEpochMilli()

    private fun dayStart(date: String): Long =
        LocalDate.parse(date).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun assertContiguous(segments: List<TimelineSegment>) {
        segments.zipWithNext().forEach { (a, b) -> assertEquals(a.endAt, b.startAt) }
        assertTrue(segments.all { it.endAt > it.startAt })
    }
}
```

## `build.gradle.kts`

```kotlin
// 根构建脚本只锁定插件版本，具体配置留在 :app 模块。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
```

## `design/icons/README.md`

```markdown
# 時計 · Launcher icon concepts

Five directions for the 時計 (time-log) launcher icon — washi paper, ink, pine, clay.
Flat vector, no gradients/shadows. Masters rendered at 4× and LANCZOS-downscaled to 512 px.
Key content stays inside the Android adaptive icon 66 % safe zone (338 px of 512).

**Start here → `overview.png`** (A–E on a light neutral ground, plus a 96 px home-screen check row).

## Concepts

| # | 名称 | Idea | Rationale |
|---|------|------|-----------|
| A | 墨時 | 「時」 monogram | A pine field with a paper-white 時 in Yu Mincho. The app logs time by the character "time" itself — no clock needed. |
| B | 素時計 | Minimal clock | A 12 px ink ring with two hands and a clay second hand. The quietest possible clock; the clay hand is the only warm note. |
| C | 時流 | Timeline blocks | Three rounded event blocks (pine / clay / ochre) at different heights — the app's day log reduced to a rhythm. |
| D | 朱印 | Hanko seal | A clay seal square with a paper 時 and a carved inner keyline. Time logged = a stamp pressed on the day. |
| E | 日月弧 | Sun-to-moon arc | A thin ink arc from an ink moon dot to a clay sun dot on pine-soft. A whole day as one restrained arc. |

Pick one and the master 512 px file is ready for adaptive-icon export (foreground/background split or full-bleed).

## Files

- `icon-A.png` … `icon-E.png` — 512×512 full-bleed square masters
- `icon-A-round.png` … `icon-E-round.png` — 512×512 circular-mask previews (transparent outside the circle)
- `overview.png` — 1720×860 contact sheet, labelled A–E + 96 px legibility row
- `render.py` — reproducible Pillow renderer (4× supersampling, LANCZOS)

## Palette

| token | hex |
|-------|-----|
| paper | #FBFAF7 |
| ink | #1E211F |
| pine | #2F5D50 |
| pine-soft | #E6EFEA |
| clay | #B4654A |
| hairline | #E4E1DA |
| ochre (category) | #C07A2B |
| category blue (unused) | #3D6BDB |
| category green (unused) | #2F8F5B |
| category purple (unused) | #8A63C9 |

Type: 時 is Yu Mincho Demibold in A and the overview captions, Microsoft YaHei Bold in the D seal.
E's ground is pine-soft so the set reads as a family without using the paper field five times.
C's ochre is the category ochre #C07A2B, the only category colour used here.
```

## `design/icons/render.py`

```python

# -*- coding: utf-8 -*-
"""時計 launcher icon concepts - flat vector, 4x supersampled, LANCZOS downscale."""
import os, math
from PIL import Image, ImageDraw, ImageFont

OUT = os.path.dirname(os.path.abspath(__file__))
W, S = 512, 4
C = W * S

PAPER     = (251, 250, 247)
INK       = (30, 33, 31)
PINE      = (47, 93, 80)
PINE_SOFT = (230, 239, 234)
CLAY      = (180, 101, 74)
HAIR      = (228, 225, 218)
OCHRE     = (192, 122, 43)
MUTED     = (108, 110, 106)

SERIF = r"C:/Windows/Fonts/yumindb.ttf"
SANSB = r"C:/Windows/Fonts/msyhbd.ttc"
SANS  = r"C:/Windows/Fonts/msyh.ttc"

def P(*v):
    return [float(x) * S for x in v]

def new(bg=PAPER):
    return Image.new("RGB", (C, C), bg)

def glyph_mask(ch, em_px, path):
    f = ImageFont.truetype(path, int(round(em_px)))
    tmp = Image.new("L", (int(em_px * 2) + 80, int(em_px * 2) + 80), 0)
    ImageDraw.Draw(tmp).text((em_px, em_px), ch, font=f, fill=255)
    bb = tmp.getbbox()
    return tmp.crop(bb)

def glyph_ink_target(ch, target_h_512, path):
    m = glyph_mask(ch, 1000, path)
    return 1000.0 * target_h_512 * S / m.size[1]

def draw_glyph(im, ch, cx, cy, target_h_512, fill, path=SERIF):
    em = glyph_ink_target(ch, target_h_512, path)
    m = glyph_mask(ch, em, path)
    w, h = m.size
    x = int(round(cx * S - w / 2.0))
    y = int(round(cy * S - h / 2.0))
    im.paste(Image.new("RGB", (w, h), fill), (x, y), m)
    return w / S, h / S

def rline(d, p1, p2, fill, w):
    d.line([p1, p2], fill=fill, width=int(round(w * S)))
    r = w * S / 2.0
    for x, y in (p1, p2):
        d.ellipse([x - r, y - r, x + r, y + r], fill=fill)

def vline(d, p1, p2, fill, w):
    rline(d, P(*p1), P(*p2), fill, w)

def circle(d, cx, cy, r, fill=None, outline=None, width=0):
    d.ellipse(P(cx - r, cy - r, cx + r, cy + r), fill=fill, outline=outline,
              width=int(round(width * S)))

def rrect(d, x0, y0, x1, y1, r, fill=None, outline=None, width=0):
    d.rounded_rectangle(P(x0, y0, x1, y1), radius=r * S, fill=fill,
                        outline=outline, width=int(round(width * S)))

# ---------------------------------------------------------------- A: 墨時 monogram
def make_A():
    im = new(PINE)
    draw_glyph(im, "時", 256, 256, 306, PAPER, SERIF)
    return im

# ---------------------------------------------------------------- B: minimal clock
def make_B():
    im = new(PAPER)
    d = ImageDraw.Draw(im)
    cx = cy = 256.0
    circle(d, cx, cy, 142, outline=INK, width=12)
    a2 = math.radians(118)                      # clay second hand, with tail
    vline(d, (cx - 30 * math.cos(a2), cy - 30 * math.sin(a2)),
             (cx + 131 * math.cos(a2), cy + 131 * math.sin(a2)), CLAY, 4.5)
    a = math.radians(-150)                      # hour hand -> 10 o'clock
    vline(d, (cx, cy), (cx + 75 * math.cos(a), cy + 75 * math.sin(a)), INK, 14)
    a = math.radians(-30)                       # minute hand -> 2 o'clock
    vline(d, (cx, cy), (cx + 113 * math.cos(a), cy + 113 * math.sin(a)), INK, 12)
    circle(d, cx, cy, 10.5, fill=INK)
    circle(d, cx, cy, 4.0, fill=CLAY)
    return im

# ---------------------------------------------------------------- C: timeline blocks
def make_C():
    im = new(PAPER)
    d = ImageDraw.Draw(im)
    bars = [(PINE, 268, 52), (CLAY, 196, 78), (OCHRE, 228, 50)]
    gap = 24.0
    total = sum(h for _, _, h in bars) + gap * (len(bars) - 1)
    y = 256 - total / 2.0
    left = 256 - 134.0
    for col, bw, bh in bars:
        rrect(d, left, y, left + bw, y + bh, bh / 2.0, fill=col)
        y += bh + gap
    return im

# ---------------------------------------------------------------- D: hanko seal
def make_D():
    im = new(PAPER)
    d = ImageDraw.Draw(im)
    half = 165.0
    rrect(d, 256 - half, 256 - half, 256 + half, 256 + half, 18, fill=CLAY)
    inset = 16.0
    rrect(d, 256 - half + inset, 256 - half + inset,
          256 + half - inset, 256 + half - inset, 9,
          outline=PAPER, width=3.2)
    draw_glyph(im, "時", 256, 256, 230, PAPER, SANSB)
    return im

# ---------------------------------------------------------------- E: sun-moon arc
def recenter(im, bg):
    from PIL import ImageChops
    bb = ImageChops.difference(im, Image.new("RGB", im.size, bg)).getbbox()
    dx = im.size[0] / 2.0 - (bb[0] + bb[2]) / 2.0
    dy = im.size[1] / 2.0 - (bb[1] + bb[3]) / 2.0
    return im.transform(im.size, Image.AFFINE, (1, 0, -dx, 0, 1, -dy),
                        resample=Image.BICUBIC, fillcolor=bg)

def make_E():
    im = new(PINE_SOFT)
    d = ImageDraw.Draw(im)
    cx, cy, r = 256.0, 318.0, 140.0
    d.arc(P(cx - r, cy - r, cx + r, cy + r), 180, 360, fill=INK,
          width=int(round(12 * S)))
    circle(d, cx - r, cy, 16, fill=INK)       # moon
    circle(d, cx + r, cy, 25, fill=CLAY)      # sun
    return recenter(im, PINE_SOFT)

MAKERS = {"A": make_A, "B": make_B, "C": make_C, "D": make_D, "E": make_E}
NAMES = [("A", "墨時", "「時」字徽"),
         ("B", "素時計", "极简表盘"),
         ("C", "時流", "时间块"),
         ("D", "朱印", "朱色印章"),
         ("E", "日月弧", "昼夜之弧")]

def hi_circle_mask():
    m = Image.new("L", (C, C), 0)
    ImageDraw.Draw(m).ellipse([0, 0, C - 1, C - 1], fill=255)
    return m

MASK_HI = hi_circle_mask()

def save_icons():
    for k, fn in MAKERS.items():
        im = fn()
        im.resize((W, W), Image.LANCZOS).save(os.path.join(OUT, "icon-%s.png" % k))
        r = im.convert("RGBA")
        r.putalpha(MASK_HI)
        r.resize((W, W), Image.LANCZOS).save(os.path.join(OUT, "icon-%s-round.png" % k))

def fnt(path, px):
    return ImageFont.truetype(path, int(round(px)))

def ctext(d, xy, txt, font, fill):
    bb = d.textbbox((0, 0), txt, font=font)
    d.text((xy[0] - (bb[2] - bb[0]) / 2.0 - bb[0],
            xy[1] - (bb[3] - bb[1]) / 2.0 - bb[1]), txt, font=font, fill=fill)

def rtext(d, xy, txt, font, fill):
    bb = d.textbbox((0, 0), txt, font=font)
    d.text((xy[0] - (bb[2] - bb[0]) - bb[0], xy[1] - bb[1]), txt, font=font, fill=fill)

def make_overview():
    SC = 2
    OW, OH = 1720, 860
    bg = (242, 239, 233)
    im = Image.new("RGB", (OW * SC, OH * SC), bg)
    d = ImageDraw.Draw(im)

    def T(v):
        return int(round(v * SC))

    f_title = fnt(SERIF, 46 * SC)
    f_sub   = fnt(SANS, 21 * SC)
    f_note  = fnt(SANS, 17 * SC)
    f_let   = fnt(SANSB, 40 * SC)
    f_name  = fnt(SERIF, 30 * SC)
    f_cap   = fnt(SANS, 21 * SC)

    # header
    bb = d.textbbox((0, 0), "時計", font=f_title)
    d.text((T(76), T(42) - bb[1]), "時計", font=f_title, fill=INK)
    tw = bb[2] - bb[0]
    d.text((T(76) + tw + T(20), T(66) - f_sub.getbbox("cg")[1]),
           "launcher icon concepts", font=f_sub, fill=MUTED)
    d.text((T(76), T(112)), "五个方向 · five directions", font=f_note, fill=MUTED)
    d.line([T(76), T(148), T(OW - 76), T(148)], fill=HAIR, width=T(2))

    # five icon columns
    cellw, cy, dia = 320.0, 330.0, 216.0
    x0 = (OW - cellw * 5) / 2.0
    icons = {k: Image.open(os.path.join(OUT, "icon-%s-round.png" % k)).convert("RGBA")
             for k in "ABCDE"}
    for i, (letter, nm, cap) in enumerate(NAMES):
        cx = x0 + cellw * i + cellw / 2.0
        ic = icons[letter].resize((T(dia), T(dia)), Image.LANCZOS)
        im.paste(ic, (T(cx - dia / 2.0), T(cy - dia / 2.0)), ic)
        d.ellipse([T(cx - dia / 2.0), T(cy - dia / 2.0),
                   T(cx + dia / 2.0), T(cy + dia / 2.0)],
                  outline=HAIR, width=T(2))
        ctext(d, (T(cx), T(cy + dia / 2.0 + 44)), letter, f_let, INK)
        ctext(d, (T(cx), T(cy + dia / 2.0 + 92)), nm, f_name, INK)
        ctext(d, (T(cx), T(cy + dia / 2.0 + 130)), cap, f_cap, MUTED)

    # 96 px band
    d.line([T(76), T(612), T(OW - 76), T(612)], fill=HAIR, width=T(2))
    ctext(d, (T(OW / 2.0), T(640)), "96 px  ·  手机主屏尺寸预览", f_note, MUTED)
    for i, (letter, _, _) in enumerate(NAMES):
        cx = x0 + cellw * i + cellw / 2.0
        ic = icons[letter].resize((T(96), T(96)), Image.LANCZOS)
        im.paste(ic, (T(cx - 48), T(662)), ic)

    ctext(d, (T(OW / 2.0), T(812)),
          "時計  ·  washi paper / ink / pine / clay  ·  512 px masters", f_note, MUTED)

    im.resize((OW, OH), Image.LANCZOS).save(os.path.join(OUT, "overview.png"))

def make_checks():
    for bgcol, tag in [((255, 255, 255), "light"), ((23, 26, 24), "dark")]:
        strip = Image.new("RGB", (5 * 120, 120), bgcol)
        for i, k in enumerate("ABCDE"):
            ic = Image.open(os.path.join(OUT, "icon-%s-round.png" % k)).convert("RGBA")
            ic = ic.resize((96, 96), Image.LANCZOS)
            strip.paste(ic, (i * 120 + 12, 12), ic)
        strip.save(os.path.join(OUT, "_check96-%s.png" % tag))

if __name__ == "__main__":
    save_icons()
    make_overview()
    make_checks()
    print("rendered")
```

## `gradle.properties`

```properties
# JVM 参数 / 构建行为（AGP 9.0.1 + Gradle 9.1.0）
org.gradle.jvmargs=-Xmx3072m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=false

# AndroidX 与命名空间
android.useAndroidX=true
android.nonTransitiveRClass=true
```

## `gradle/libs.versions.toml`

```toml
[versions]
agp = "9.0.1"
kotlin = "2.2.10"
ksp = "2.3.6"

composeBom = "2025.08.01"
coreKtx = "1.17.0"
activityCompose = "1.10.1"
lifecycle = "2.9.4"

room = "2.7.2"
coroutines = "1.10.2"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }

androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }

androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

## `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.1.0-bin.zip
networkTimeout=10000
validateDistributionUrl=false
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

## `gradlew`

```text
#!/bin/sh

#
# Copyright © 2015 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################
#
#   Gradle start up script for POSIX generated by Gradle.
#
#   Important for running:
#
#   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#       noncompliant, but you have some other compliant shell such as ksh or
#       bash, then to run this script, type that shell name before the whole
#       command line, like:
#
#           ksh Gradle
#
#       Busybox and similar reduced shells will NOT work, because this script
#       requires all of these POSIX shell features:
#         * functions;
#         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * compound commands having a testable exit status, especially «case»;
#         * various built-in commands including «command», «set», and «ulimit».
#
#   Important for patching:
#
#   (2) This script targets any POSIX shell, so it avoids extensions provided
#       by Bash, Ksh, etc; in particular arrays are avoided.
#
#       The "traditional" practice of packing multiple parameters into a
#       space-separated string is a well documented source of bugs and security
#       problems, so this is (mostly) avoided, by progressively accumulating
#       options in "$@", and eventually passing that to Java.
#
#       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
#       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
#       see the in-line comments for details.
#
#       There are tweaks for specific operating systems such as AIX, CygWin,
#       Darwin, MinGW, and NonStop.
#
#   (3) This script is generated from the Groovy template
#       https://github.com/gradle/gradle/blob/HEAD/platforms/jvm/plugins-application/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#       within the Gradle project.
#
#       You can find Gradle at https://github.com/gradle/gradle/.
#
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
# Discard cd standard output in case $CDPATH is set (https://github.com/gradle/gradle/issues/25036)
APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s\n' "$PWD" ) || exit

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac



# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # Roll the args list around exactly as many times as the number of
        # args, so each arg winds up back in the position where it started, but
        # possibly modified.
        #
        # NB: a `for` loop captures its iteration list before it begins, so
        # changing the positional parameters here affects neither the number of
        # iterations, nor the values presented in `arg`.
        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi


# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
#     and any embedded shellness will be escaped.
#   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
#     treated as '${Hostname}' itself on the command line.

set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
        "$@"

# Stop when "xargs" is not available.
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# In Bash we could simply go:
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$@"
#
# but POSIX shell has neither arrays nor command substitution, so instead we
# post-process each arg (as a line of input to sed) to backslash-escape any
# character that might be a shell metacharacter, then use eval to reverse
# that process (while maintaining the separation between arguments), and wrap
# the whole thing up as a single "set" statement.
#
# This will of course break if any of these variables contains a newline or
# an unmatched quote.
#

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"
```

## `gradlew.bat`

```text
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line



@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
```

## `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TimeLog"
include(":app")
```
