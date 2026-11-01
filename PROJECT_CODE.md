# 时间流 · 完整源码

> 由仓库 tracked 文件自动汇总，便于按文件路径审阅。构建入口与说明见 [README.md](README.md)。

## 文件清单

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
- `app/src/main/java/com/example/timelog/ui/category/CategoryManageScreen.kt`
- `app/src/main/java/com/example/timelog/ui/category/CategoryManageViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/category/CategoryPicker.kt`
- `app/src/main/java/com/example/timelog/ui/home/HomeScreen.kt`
- `app/src/main/java/com/example/timelog/ui/home/HomeViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/home/TimelineItem.kt`
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
- `build.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`

---

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
        versionCode = 1
        versionName = "1.0"
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
            onRecord = homeViewModel::openNewRecord,
            onOpenCategories = { screen = AppScreen.Categories },
            onEventClick = homeViewModel::openEditRecord,
            onEventLongClick = homeViewModel::requestDelete,
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
            onTitleChange = recordViewModel::onTitleChange,
            onNoteChange = recordViewModel::onNoteChange,
            onCategorySelected = recordViewModel::onCategorySelected,
            onCategoryExpanded = recordViewModel::onCategoryExpanded,
            onDateSelected = recordViewModel::onDateSelected,
            onTimeSelected = recordViewModel::onTimeSelected,
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
    @Query("SELECT * FROM event ORDER BY timestamp DESC, id DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

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
    indices = [Index("categoryId"), Index("timestamp")],
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String?,
    val timestamp: Long,
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

    suspend fun saveEvent(
        id: Long?,
        title: String,
        note: String?,
        timestamp: Long,
        categoryId: Long,
    ) {
        val now = System.currentTimeMillis()
        if (id == null) {
            eventDao.insert(
                EventEntity(
                    title = title.trim(),
                    note = note?.trim()?.ifEmpty { null },
                    timestamp = timestamp,
                    categoryId = categoryId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = eventDao.getById(id) ?: return
            eventDao.update(
                existing.copy(
                    title = title.trim(),
                    note = note?.trim()?.ifEmpty { null },
                    timestamp = timestamp,
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
        title = title,
        note = note,
        timestamp = timestamp,
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
        SeedCategory(11, "项目A", 1, 0, 0xFF4C7BE8),
        SeedCategory(12, "会议", 1, 1, 0xFF2F5DAF),
        SeedCategory(2, "生活", null, 1, 0xFF2F8F5B),
        SeedCategory(21, "家务", 2, 0, 0xFF4FAF78),
        SeedCategory(22, "购物", 2, 1, 0xFF277A4C),
        SeedCategory(3, "学习", null, 2, 0xFFC07A2B),
        SeedCategory(31, "阅读", 3, 0, 0xFFD99445),
        SeedCategory(32, "课程", 3, 1, 0xFF9E6220),
        SeedCategory(4, "休息", null, 3, 0xFF8A63C9),
        SeedCategory(41, "睡眠", 4, 0, 0xFF9C79D6),
        SeedCategory(42, "娱乐", 4, 1, 0xFF7150A8),
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
    val title: String,
    val note: String?,
    val timestamp: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

data class TimelineEvent(
    val event: Event,
    val categoryPath: String,
    val categoryColor: Long,
)
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

## `app/src/main/java/com/example/timelog/ui/category/CategoryPicker.kt`

```kotlin
package com.example.timelog.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.timelog.domain.model.CategoryNode
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.ui.home.CategoryDot

@Composable
fun CategoryPicker(
    tree: CategoryTree,
    selectedId: Long?,
    expandedIds: Set<Long>,
    onSelect: (Long) -> Unit,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 224.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        tree.roots.forEach { node ->
            PickerRow(
                node = node,
                depth = 0,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onSelect = onSelect,
                onToggle = onToggle,
            )
        }
    }
}

@Composable
private fun PickerRow(
    node: CategoryNode,
    depth: Int,
    selectedId: Long?,
    expandedIds: Set<Long>,
    onSelect: (Long) -> Unit,
    onToggle: (Long) -> Unit,
) {
    val selected = node.category.id == selectedId
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 20).dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            )
            .clickable { onSelect(node.category.id) }
            .padding(horizontal = 8.dp)
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (node.children.isEmpty()) {
            Spacer(modifier = Modifier.width(32.dp))
        } else {
            IconButton(
                onClick = { onToggle(node.category.id) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (node.category.id in expandedIds) {
                        Icons.Rounded.KeyboardArrowDown
                    } else {
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight
                    },
                    contentDescription = if (node.category.id in expandedIds) "收起" else "展开",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        CategoryDot(colorHex = node.category.colorHex)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = node.category.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        if (selected) {
            Box(modifier = Modifier.padding(end = 4.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "已选择",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    if (node.category.id in expandedIds) {
        node.children.forEach { child ->
            PickerRow(
                node = child,
                depth = depth + 1,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onSelect = onSelect,
                onToggle = onToggle,
            )
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/home/HomeScreen.kt`

```kotlin
package com.example.timelog.ui.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.timelog.domain.model.TimelineEvent

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRecord: () -> Unit,
    onOpenCategories: () -> Unit,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (TimelineEvent) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeHeader(totalCount = state.totalCount, onOpenCategories = onOpenCategories)
        },
        bottomBar = {
            RecordBar(onRecord = onRecord)
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            state.days.isEmpty() -> EmptyTimeline(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> TimelineList(
                days = state.days,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onEventClick = onEventClick,
                onEventLongClick = onEventLongClick,
            )
        }
    }

    val pendingDelete = state.pendingDelete
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("删除这条记录？") },
            text = {
                Text("「${pendingDelete.event.title}」将被永久删除，无法恢复。")
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun HomeHeader(totalCount: Int, onOpenCategories: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 12.dp, top = 14.dp, bottom = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "时间流",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (totalCount == 0) "把发生的事，按时间排好" else "共 $totalCount 条记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onOpenCategories) {
                Text("分类")
            }
        }
    }
}

@Composable
private fun RecordBar(onRecord: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Button(
            onClick = onRecord,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .height(56.dp),
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "完成",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun EmptyTimeline(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "还没有记录。\n点下方的「完成」记下第一件事。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp),
        )
    }
}

@Composable
private fun TimelineList(
    days: List<TimelineDay>,
    modifier: Modifier = Modifier,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (TimelineEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        days.forEach { day ->
            item(key = "day-${day.date}") {
                DayHeader(day = day)
            }
            items(items = day.events, key = { it.event.id }) { event ->
                TimelineItem(
                    item = event,
                    onClick = { onEventClick(event.event.id) },
                    onLongClick = { onEventLongClick(event) },
                )
            }
        }
    }
}

@Composable
private fun DayHeader(day: TimelineDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = day.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = day.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp),
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EditorTarget {
    data object New : EditorTarget
    data class Edit(val eventId: Long) : EditorTarget
}

data class TimelineDay(
    val date: LocalDate,
    val title: String,
    val subtitle: String,
    val events: List<TimelineEvent>,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val days: List<TimelineDay> = emptyList(),
    val totalCount: Int = 0,
    val tree: CategoryTree = CategoryTree(),
    val editor: EditorTarget? = null,
    val pendingDelete: TimelineEvent? = null,
)

class HomeViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private data class Transient(
        val editor: EditorTarget? = null,
        val pendingDeleteId: Long? = null,
    )

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.timeline,
        repository.categoryTree,
        transient,
    ) { timeline, tree, local ->
        val today = LocalDate.now(DateTimeFormat.zone())
        val days = timeline
            .groupBy { DateTimeFormat.toLocalDateTime(it.event.timestamp).toLocalDate() }
            .map { (date, events) ->
                TimelineDay(
                    date = date,
                    title = DateTimeFormat.sectionTitle(date, today),
                    subtitle = DateTimeFormat.sectionSubtitle(date, today),
                    events = events,
                )
            }
        HomeUiState(
            isLoading = false,
            days = days,
            totalCount = timeline.size,
            tree = tree,
            editor = local.editor,
            pendingDelete = timeline.firstOrNull { it.event.id == local.pendingDeleteId },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun openNewRecord() {
        transient.update { it.copy(editor = EditorTarget.New) }
    }

    fun openEditRecord(eventId: Long) {
        transient.update { it.copy(editor = EditorTarget.Edit(eventId)) }
    }

    fun closeRecord() {
        transient.update { it.copy(editor = null) }
    }

    fun requestDelete(event: TimelineEvent) {
        transient.update { it.copy(pendingDeleteId = event.event.id) }
    }

    fun dismissDelete() {
        transient.update { it.copy(pendingDeleteId = null) }
    }

    fun confirmDelete() {
        val id = transient.value.pendingDeleteId ?: return
        viewModelScope.launch {
            repository.deleteEvent(id)
            transient.update { it.copy(pendingDeleteId = null) }
        }
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/home/TimelineItem.kt`

```kotlin
package com.example.timelog.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.util.DateTimeFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineItem(
    item: TimelineEvent,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val lineColor = MaterialTheme.colorScheme.outlineVariant
        Box(
            modifier = Modifier
                .width(68.dp)
                .fillMaxHeight(),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = size.width / 2f
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    text = DateTimeFormat.time(item.event.timestamp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp, bottom = 16.dp),
        ) {
            Text(
                text = item.event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryDot(colorHex = item.categoryColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.categoryPath.ifEmpty { "未分类" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val note = item.event.note
            if (!note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun CategoryDot(colorHex: Long, size: Dp = 7.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color(colorHex), CircleShape),
    )
}
```

## `app/src/main/java/com/example/timelog/ui/record/RecordSheet.kt`

```kotlin
package com.example.timelog.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.timelog.ui.category.CategoryPicker
import com.example.timelog.util.DateTimeFormat
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSheet(
    state: RecordUiState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onCategoryExpanded: (Long) -> Unit,
    onDateSelected: (java.time.LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = if (state.isEditing) "编辑记录" else "记录此刻",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel(text = "分类")
            Spacer(modifier = Modifier.height(6.dp))
            CategoryPicker(
                tree = state.tree,
                selectedId = state.selectedCategoryId,
                expandedIds = state.expandedIds,
                onSelect = onCategorySelected,
                onToggle = onCategoryExpanded,
            )
            Spacer(modifier = Modifier.height(18.dp))

            FieldLabel(text = "标题")
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("做了什么？") },
                singleLine = true,
                isError = state.titleError,
                supportingText = if (state.titleError) {
                    { Text("请输入标题") }
                } else {
                    null
                },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "备注")
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("可留空") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(18.dp))

            FieldLabel(text = "时间")
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = {
                        Text(DateTimeFormat.dateWithWeekday(
                            DateTimeFormat.toEpochMillis(state.date, LocalTime.NOON),
                        ))
                    },
                )
                AssistChip(
                    onClick = { showTimePicker = true },
                    label = { Text(DateTimeFormat.time(DateTimeFormat.toEpochMillis(state.date, state.time))) },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving &&
                        state.title.isNotBlank() &&
                        state.selectedCategoryId != null,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(if (state.isSaving) "保存中" else "保存")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeFormat.startOfDayUtcMillis(state.date),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(DateTimeFormat.dateFromUtcMillis(millis))
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.time.hour,
            initialMinute = state.time.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        showTimePicker = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
    val title: String = "",
    val note: String = "",
    val date: LocalDate = LocalDate.now(DateTimeFormat.zone()),
    val time: LocalTime = LocalTime.now(DateTimeFormat.zone()).withSecond(0).withNano(0),
    val tree: CategoryTree = CategoryTree(),
    val selectedCategoryId: Long? = null,
    val expandedIds: Set<Long> = emptySet(),
    val titleError: Boolean = false,
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
                    val selected = stillValid ?: tree.flatten().firstOrNull()?.first?.category?.id
                    state.copy(
                        tree = tree,
                        selectedCategoryId = selected,
                        expandedIds = state.expandedIds + tree.ancestorIds(selected ?: -1),
                    )
                }
            }
        }
    }

    fun start(target: EditorTarget) {
        loadJob?.cancel()
        _uiState.update { it.copy(isSaving = false, saved = false, titleError = false) }
        when (target) {
            EditorTarget.New -> {
                val now = LocalDateTime.now(DateTimeFormat.zone())
                _uiState.update {
                    it.copy(
                        isEditing = false,
                        eventId = null,
                        title = "",
                        note = "",
                        date = now.toLocalDate(),
                        time = now.toLocalTime().withSecond(0).withNano(0),
                    )
                }
            }

            is EditorTarget.Edit -> {
                loadJob = viewModelScope.launch {
                    val event = repository.getEvent(target.eventId) ?: return@launch
                    val local = DateTimeFormat.toLocalDateTime(event.timestamp)
                    _uiState.update {
                        it.copy(
                            isEditing = true,
                            eventId = event.id,
                            title = event.title,
                            note = event.note.orEmpty(),
                            date = local.toLocalDate(),
                            time = local.toLocalTime().withSecond(0).withNano(0),
                            selectedCategoryId = event.categoryId,
                            expandedIds = it.expandedIds + it.tree.ancestorIds(event.categoryId),
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = false) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                expandedIds = it.expandedIds + it.tree.ancestorIds(categoryId),
            )
        }
    }

    fun onCategoryExpanded(categoryId: Long) {
        _uiState.update {
            val expanded = if (categoryId in it.expandedIds) {
                it.expandedIds - categoryId
            } else {
                it.expandedIds + categoryId
            }
            it.copy(expandedIds = expanded)
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { it.copy(time = time.withSecond(0).withNano(0)) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        val categoryId = state.selectedCategoryId ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                repository.saveEvent(
                    id = state.eventId,
                    title = state.title,
                    note = state.note,
                    timestamp = DateTimeFormat.toEpochMillis(state.date, state.time),
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
                expandedIds = current.expandedIds,
            )
        }
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
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy年", zh)

    fun zone(): ZoneId = ZoneId.systemDefault()

    fun toLocalDateTime(epochMillis: Long): LocalDateTime =
        Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalDateTime()

    fun toEpochMillis(date: LocalDate, time: LocalTime): Long =
        LocalDateTime.of(date, time).atZone(zone()).toInstant().toEpochMilli()

    fun time(epochMillis: Long): String = toLocalDateTime(epochMillis).format(timeFormatter)

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
        toLocalDateTime(epochMillis).format(DateTimeFormatter.ofPattern("M月d日", zh))

    fun dateWithWeekday(epochMillis: Long): String =
        toLocalDateTime(epochMillis).format(monthDayFormatter)

    fun isoDate(epochMillis: Long): String =
        toLocalDateTime(epochMillis).toLocalDate().toString()

    fun startOfDayUtcMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    fun dateFromUtcMillis(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
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
    <string name="app_name">时间流</string>
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

## `build.gradle.kts`

```kotlin
// 根构建脚本只锁定插件版本，具体配置留在 :app 模块。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
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

