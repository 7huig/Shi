# 時 · 完整源码

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
- `app/src/main/java/com/example/timelog/data/transfer/TransferJson.kt`
- `app/src/main/java/com/example/timelog/domain/OverlapResolver.kt`
- `app/src/main/java/com/example/timelog/domain/Statistics.kt`
- `app/src/main/java/com/example/timelog/domain/model/Category.kt`
- `app/src/main/java/com/example/timelog/domain/model/CategoryPalette.kt`
- `app/src/main/java/com/example/timelog/domain/model/CategoryTree.kt`
- `app/src/main/java/com/example/timelog/domain/model/Event.kt`
- `app/src/main/java/com/example/timelog/domain/model/LedgerSnapshot.kt`
- `app/src/main/java/com/example/timelog/domain/model/Timeline.kt`
- `app/src/main/java/com/example/timelog/ui/category/CategoryManageScreen.kt`
- `app/src/main/java/com/example/timelog/ui/category/CategoryManageViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/data/DataTransferSection.kt`
- `app/src/main/java/com/example/timelog/ui/data/DataTransferViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/home/HomeScreen.kt`
- `app/src/main/java/com/example/timelog/ui/home/HomeViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/home/TimelineItem.kt`
- `app/src/main/java/com/example/timelog/ui/record/CategoryGrid.kt`
- `app/src/main/java/com/example/timelog/ui/record/RecordSheet.kt`
- `app/src/main/java/com/example/timelog/ui/record/RecordViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/search/SearchScreen.kt`
- `app/src/main/java/com/example/timelog/ui/search/SearchViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/stats/StatisticsScreen.kt`
- `app/src/main/java/com/example/timelog/ui/stats/StatisticsViewModel.kt`
- `app/src/main/java/com/example/timelog/ui/theme/Color.kt`
- `app/src/main/java/com/example/timelog/ui/theme/Theme.kt`
- `app/src/main/java/com/example/timelog/ui/theme/Type.kt`
- `app/src/main/java/com/example/timelog/util/DateTimeFormat.kt`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/values-night/themes.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/test/java/com/example/timelog/domain/OverlapResolverTest.kt`
- `app/src/test/java/com/example/timelog/domain/StatisticsTest.kt`
- `app/src/test/java/com/example/timelog/domain/TimelineBuilderTest.kt`
- `build.gradle.kts`
- `design/icons/README.md`
- `design/icons/build_launcher_assets.py`
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
        versionCode = 6
        versionName = "0.1.0"
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.ui.category.CategoryManageScreen
import com.example.timelog.ui.category.CategoryManageViewModel
import com.example.timelog.ui.data.DataTransferSection
import com.example.timelog.ui.data.DataTransferViewModel
import com.example.timelog.ui.home.HomeViewModel
import com.example.timelog.ui.home.TimelineFabOverlay
import com.example.timelog.ui.home.TimelinePage
import com.example.timelog.ui.record.RecordSheet
import com.example.timelog.ui.record.RecordViewModel
import com.example.timelog.ui.search.SearchScreen
import com.example.timelog.ui.search.SearchViewModel
import com.example.timelog.ui.stats.StatisticsScreen
import com.example.timelog.ui.stats.StatisticsViewModel
import com.example.timelog.ui.theme.TimeLogTheme
import kotlinx.coroutines.launch

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

private const val PAGE_TIMELINE = 0
private const val PAGE_STATS = 1

private const val OVERLAY_SEARCH = "search"
private const val OVERLAY_CATEGORIES = "categories"

/**
 * 应用外壳：紧凑顶栏 + 左右滑动的主界面/统计页。
 * 时间轴页的「記一笔」入口是右下角蓝底白色加号的浮动按钮；搜索与分类是整屏浮层。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeLogApp(repository: TimeLogRepository) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    val recordViewModel: RecordViewModel = viewModel(factory = RecordViewModel.factory(repository))
    val categoryViewModel: CategoryManageViewModel = viewModel(
        factory = CategoryManageViewModel.factory(repository),
    )
    val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModel.factory(repository))
    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModel.factory(repository),
    )
    val transferViewModel: DataTransferViewModel = viewModel(
        factory = DataTransferViewModel.factory(repository),
    )

    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val recordState by recordViewModel.uiState.collectAsStateWithLifecycle()
    val categoryState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    val statisticsState by statisticsViewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var overlay by rememberSaveable { mutableStateOf<String?>(null) }
    val editorTarget = homeState.editor
    val currentPage = pagerState.currentPage

    LaunchedEffect(editorTarget) {
        editorTarget?.let(recordViewModel::start)
    }

    LaunchedEffect(recordState.saved) {
        if (recordState.saved) {
            homeViewModel.closeRecord()
            recordViewModel.reset()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                page = currentPage,
                onSelectPage = { target ->
                    scope.launch { pagerState.animateScrollToPage(target) }
                },
                onSearch = { overlay = OVERLAY_SEARCH },
                onOpenCategories = { overlay = OVERLAY_CATEGORIES },
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding(),
            ) { page ->
                when (page) {
                    PAGE_TIMELINE -> TimelinePage(
                        state = homeState,
                        onEventClick = homeViewModel::openEditRecord,
                        onEventLongClick = homeViewModel::requestDelete,
                        onFillGap = homeViewModel::openFillRecord,
                        onDismissDelete = homeViewModel::dismissDelete,
                        onConfirmDelete = homeViewModel::confirmDelete,
                    )

                    else -> StatisticsScreen(
                        state = statisticsState,
                        onModeSelected = statisticsViewModel::selectMode,
                        onPrevious = statisticsViewModel::goPrevious,
                        onNext = statisticsViewModel::goNext,
                        onCustomRangeSelected = statisticsViewModel::setCustomRange,
                        footer = { DataTransferSection(viewModel = transferViewModel) },
                    )
                }
            }
        }

        // 浮动「+」入口：叠在内容之上，不占用列表高度；只在時記页显示。
        if (currentPage == PAGE_TIMELINE) {
            TimelineFabOverlay(onNewRecord = homeViewModel::openNewRecord)
        }

        if (overlay == OVERLAY_SEARCH) {
            Surface(modifier = Modifier.fillMaxSize()) {
                SearchScreen(
                    onBack = { overlay = null },
                    onOpenEvent = { eventId ->
                        overlay = null
                        homeViewModel.openEditRecord(eventId)
                    },
                    viewModel = searchViewModel,
                )
            }
        }

        if (overlay == OVERLAY_CATEGORIES) {
            BackHandler { overlay = null }
            CategoryManageScreen(
                state = categoryState,
                onBack = { overlay = null },
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
            onConfirmOverwrite = recordViewModel::confirmOverwrite,
            onCancelOverwrite = recordViewModel::dismissOverwrite,
        )
    }
}

/** 紧凑顶栏：两个页签同为 23sp serif 样式，选中的用墨色、未选的用次要色。 */
@Composable
private fun AppHeader(
    page: Int,
    onSelectPage: (Int) -> Unit,
    onSearch: () -> Unit,
    onOpenCategories: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val active = MaterialTheme.colorScheme.onBackground
    val tabStyle = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 23.sp,
        lineHeight = 28.sp,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 22.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "時記",
            style = tabStyle,
            color = if (page == PAGE_TIMELINE) active else muted,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelectPage(PAGE_TIMELINE) }
                .padding(horizontal = 2.dp, vertical = 2.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = "時計",
            style = tabStyle,
            color = if (page == PAGE_STATS) active else muted,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelectPage(PAGE_STATS) }
                .padding(horizontal = 2.dp, vertical = 2.dp),
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSearch) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "搜索备注",
                tint = muted,
            )
        }
        TextButton(onClick = onOpenCategories) {
            Text(
                text = "分类",
                style = MaterialTheme.typography.labelLarge,
                color = muted,
            )
        }
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
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "time-log.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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

    @Query("DELETE FROM category")
    suspend fun deleteAll()
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

    @Query("SELECT * FROM event ORDER BY startAt ASC, id ASC")
    suspend fun getAll(): List<EventEntity>

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

    @Query("DELETE FROM event")
    suspend fun deleteAll()

    @Query("DELETE FROM event WHERE categoryId IN (:categoryIds)")
    suspend fun deleteByCategoryIds(categoryIds: List<Long>): Int

    @Query("SELECT COUNT(*) FROM event WHERE categoryId IN (:categoryIds)")
    suspend fun countByCategoryIds(categoryIds: List<Long>): Int

    /** 与 [startAt, endAt) 有交集的事件；excludeId 用来排除正在编辑的那条。 */
    @Query(
        "SELECT * FROM event " +
            "WHERE startAt < :endAt AND endAt > :startAt AND id != :excludeId " +
            "ORDER BY startAt ASC, id ASC",
    )
    suspend fun getOverlapping(startAt: Long, endAt: Long, excludeId: Long): List<EventEntity>

    @Query(
        "SELECT * FROM event " +
            "WHERE note IS NOT NULL AND note LIKE '%' || :query || '%' " +
            "ORDER BY startAt DESC, id DESC",
    )
    fun searchByNote(query: String): Flow<List<EventEntity>>
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
import com.example.timelog.domain.OverlapPlan
import com.example.timelog.domain.model.Category
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.Event
import com.example.timelog.domain.model.ExportedCategory
import com.example.timelog.domain.model.ExportedEvent
import com.example.timelog.domain.model.ImportMode
import com.example.timelog.domain.model.LedgerSnapshot
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.buildCategoryTree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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

    /** 按备注搜索；空白关键字直接返回空列表，不查库。 */
    fun searchEvents(query: String): Flow<List<TimelineEvent>> {
        val keyword = query.trim()
        if (keyword.isEmpty()) return flowOf(emptyList())
        return combine(
            eventDao.searchByNote(keyword),
            categoryDao.observeAll(),
        ) { entities, categories ->
            val tree = buildCategoryTree(categories.map { it.toDomain() })
            entities.map { entity ->
                val event = entity.toDomain()
                TimelineEvent(
                    event = event,
                    categoryPath = tree.pathOf(event.categoryId),
                    categoryColor = tree.find(event.categoryId)?.category?.colorHex ?: 0xFF9AA29A,
                )
            }
        }
    }

    suspend fun getOverlappingEvents(startAt: Long, endAt: Long, excludeId: Long): List<Event> =
        eventDao.getOverlapping(startAt, endAt, excludeId).map { it.toDomain() }

    suspend fun saveEvent(
        id: Long?,
        note: String?,
        startAt: Long,
        endAt: Long,
        categoryId: Long,
    ) {
        val now = System.currentTimeMillis()
        val cleanNote = note.cleanNote()
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

    /**
     * 保存正在编辑的区间，并按 [plan] 让其它重叠记录让路：
     * victims 由用户确认后才会出现在 plan 里，调用方保证确认语义。
     */
    suspend fun saveEventResolved(
        id: Long?,
        note: String?,
        startAt: Long,
        endAt: Long,
        categoryId: Long,
        plan: OverlapPlan,
    ): Long = database.withTransaction {
        val now = System.currentTimeMillis()
        val cleanNote = note.cleanNote()
        val safeEndAt = endAt.coerceAtLeast(startAt + 60_000L)

        plan.victims.forEach { victim -> eventDao.deleteById(victim.id) }
        plan.updates.forEach { update ->
            eventDao.getById(update.event.id)?.let { existing ->
                eventDao.update(
                    existing.copy(
                        startAt = update.startAt,
                        endAt = update.endAt,
                        updatedAt = now,
                    ),
                )
            }
        }
        plan.clones.forEach { clone ->
            val cloneEnd = clone.endAt.coerceAtLeast(clone.startAt + 60_000L)
            eventDao.insert(
                EventEntity(
                    note = clone.note.cleanNote(),
                    startAt = clone.startAt,
                    endAt = cloneEnd,
                    categoryId = clone.categoryId,
                    createdAt = clone.createdAt,
                    updatedAt = now,
                ),
            )
        }

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
            val existing = eventDao.getById(id)
            if (existing == null) {
                -1L
            } else {
                eventDao.update(
                    existing.copy(
                        note = cleanNote,
                        startAt = startAt,
                        endAt = safeEndAt,
                        categoryId = categoryId,
                        updatedAt = now,
                    ),
                )
                id
            }
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
     * 删除分类时连同该分类及其子树下的事件一起删除（用户明确要求，不做迁移）。
     * 事件外键是 RESTRICT，必须先删事件，才能删除分类；子分类由自引用外键 CASCADE 清理。
     */
    suspend fun deleteCategory(categoryId: Long): CategoryDeletionResult = database.withTransaction {
        val all = categoryDao.getAll().map { it.toDomain() }
        val tree = buildCategoryTree(all)
        val node = tree.find(categoryId) ?: return@withTransaction CategoryDeletionResult.NotFound
        if (node.category.isSystem) return@withTransaction CategoryDeletionResult.SystemCategory

        val ids = tree.subtreeIds(categoryId)
        val deletedEventCount = if (ids.isEmpty()) 0 else eventDao.deleteByCategoryIds(ids)
        categoryDao.deleteById(categoryId)
        CategoryDeletionResult.Deleted(
            categoryCount = ids.size,
            deletedEventCount = deletedEventCount,
        )
    }

    suspend fun snapshot(): LedgerSnapshot = database.withTransaction {
        LedgerSnapshot(
            exportedAt = System.currentTimeMillis(),
            categories = categoryDao.getAll().map { it.toExport() },
            events = eventDao.getAll().map { it.toExport() },
        )
    }

    /**
     * 导入数据。[ImportMode.REPLACE] 先清空再写入；[ImportMode.MERGE] 保留现有数据，
     * 同名同父的分类复用，其余分类新建，事件按旧分类 ID 映射到新分类。
     */
    suspend fun restore(snapshot: LedgerSnapshot, mode: ImportMode) = database.withTransaction {
        when (mode) {
            ImportMode.REPLACE -> restoreReplace(snapshot)
            ImportMode.MERGE -> restoreMerge(snapshot)
        }
    }

    private suspend fun restoreReplace(snapshot: LedgerSnapshot) {
        eventDao.deleteAll()
        categoryDao.deleteAll()

        val idMap = mutableMapOf<Long, Long>()
        orderByDepth(snapshot.categories).forEach { category ->
            val parentNewId = category.parentId?.let(idMap::get)
            val entity = CategoryEntity(
                id = category.id.coerceAtLeast(0L),
                name = category.name.trim(),
                parentId = parentNewId,
                sortOrder = category.sortOrder,
                colorHex = category.colorHex,
                isSystem = category.isSystem,
            )
            val newId = categoryDao.insert(entity)
            idMap[category.id] = newId
        }
        insertImportedEvents(snapshot.events, idMap)
    }

    private suspend fun restoreMerge(snapshot: LedgerSnapshot) {
        val existing = categoryDao.getAll().toMutableList()
        val system = existing.firstOrNull { it.isSystem }
        val idMap = mutableMapOf<Long, Long>()

        orderByDepth(snapshot.categories).forEach { category ->
            val parentNewId = category.parentId?.let(idMap::get)
            if (category.isSystem && system != null) {
                idMap[category.id] = system.id
                return@forEach
            }
            val reused = existing.firstOrNull {
                it.name == category.name.trim() &&
                    it.parentId == parentNewId &&
                    it.isSystem == category.isSystem
            }
            if (reused != null) {
                idMap[category.id] = reused.id
            } else {
                val newId = categoryDao.insert(
                    CategoryEntity(
                        name = category.name.trim(),
                        parentId = parentNewId,
                        sortOrder = category.sortOrder,
                        colorHex = category.colorHex,
                        isSystem = category.isSystem,
                    ),
                )
                idMap[category.id] = newId
                existing.add(
                    CategoryEntity(
                        id = newId,
                        name = category.name.trim(),
                        parentId = parentNewId,
                        sortOrder = category.sortOrder,
                        colorHex = category.colorHex,
                        isSystem = category.isSystem,
                    ),
                )
            }
        }
        insertImportedEvents(snapshot.events, idMap)
    }

    private suspend fun insertImportedEvents(
        events: List<ExportedEvent>,
        idMap: Map<Long, Long>,
    ) {
        val fallback = categoryDao.getSystemCategory()?.id
            ?: categoryDao.insert(
                CategoryEntity(
                    name = "未分类",
                    parentId = null,
                    sortOrder = categoryDao.maxSortOrder(null) + 1,
                    colorHex = 0xFF8C928C,
                    isSystem = true,
                ),
            )
        events.forEach { event ->
            val startAt = event.startAt
            val endAt = event.endAt.coerceAtLeast(startAt + 60_000L)
            eventDao.insert(
                EventEntity(
                    note = event.note.cleanNote(),
                    startAt = startAt,
                    endAt = endAt,
                    categoryId = idMap[event.categoryId] ?: fallback,
                    createdAt = event.createdAt,
                    updatedAt = event.updatedAt,
                ),
            )
        }
    }

    /** 父分类先于子分类插入，避免外键失败；异常数据里的环按根处理。 */
    private fun orderByDepth(categories: List<ExportedCategory>): List<ExportedCategory> {
        val byId = categories.associateBy { it.id }
        val depths = mutableMapOf<Long, Int>()

        fun depthOf(id: Long, visiting: MutableSet<Long>): Int {
            depths[id]?.let { return it }
            if (!visiting.add(id)) return 0
            val parentId = byId[id]?.parentId
            val depth = if (parentId == null || parentId !in byId) {
                0
            } else {
                depthOf(parentId, visiting) + 1
            }
            depths[id] = depth
            return depth
        }

        return categories.sortedBy { depthOf(it.id, mutableSetOf()) }
    }

    private fun String?.cleanNote(): String? = this?.trim()?.ifEmpty { null }

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

    private fun CategoryEntity.toExport(): ExportedCategory = ExportedCategory(
        id = id,
        name = name,
        parentId = parentId,
        sortOrder = sortOrder,
        colorHex = colorHex,
        isSystem = isSystem,
    )

    private fun EventEntity.toExport(): ExportedEvent = ExportedEvent(
        note = note,
        startAt = startAt,
        endAt = endAt,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

sealed interface CategoryDeletionResult {
    data class Deleted(val categoryCount: Int, val deletedEventCount: Int) : CategoryDeletionResult
    data object NotFound : CategoryDeletionResult
    data object SystemCategory : CategoryDeletionResult
}
```

## `app/src/main/java/com/example/timelog/data/seed/DefaultCategories.kt`

```kotlin
package com.example.timelog.data.seed

/**
 * 首次建库时写入的默认分类。固定主键是为了在同一个事务里安全地写入父子关系，
 * 同时给「未分类」一个稳定 ID（删除分类时会连同事件一起删除，系统分类仅作兜底展示）。
 *
 * 默认顺序：工作（沟通、执行）→ 休息（睡眠、放空）→ 学习（输入、输出）→ 娱乐（沉浸、社交）。
 * 颜色取自 24 节气色板（[com.example.timelog.domain.model.SolarTermColors]），不显示节气名称。
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
        SeedCategory(1, "工作", null, 0, 0xFF5A7FA8),
        SeedCategory(11, "执行", 1, 1, 0xFF3F5B78),
        SeedCategory(12, "沟通", 1, 0, 0xFF8A9AB8),
        SeedCategory(2, "休息", null, 1, 0xFF9E86B8),
        SeedCategory(21, "睡眠", 2, 0, 0xFF8B72A6),
        SeedCategory(22, "放空", 2, 1, 0xFF8A86A8),
        SeedCategory(3, "学习", null, 2, 0xFFC9A24E),
        SeedCategory(31, "输入", 3, 0, 0xFFB8933F),
        SeedCategory(32, "输出", 3, 1, 0xFFA98A5A),
        SeedCategory(4, "娱乐", null, 3, 0xFF94594A),
        SeedCategory(41, "社交", 4, 1, 0xFFC46E4A),
        SeedCategory(42, "沉浸", 4, 0, 0xFFA8463A),
        SeedCategory(5, "未分类", null, 4, 0xFF8C928C, isSystem = true),
    )
}
```

## `app/src/main/java/com/example/timelog/data/transfer/TransferJson.kt`

```kotlin
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
```

## `app/src/main/java/com/example/timelog/domain/OverlapResolver.kt`

```kotlin
package com.example.timelog.domain

import com.example.timelog.domain.model.Event

/**
 * 目标区间（用户正在保存的这一段）与已有记录冲突时的处理方案。
 *
 * 语义：目标区间优先，老记录让路。
 * - 老记录被目标区间整个盖住 → [OverlapPlan.victims]，要用户确认后才删除；
 * - 老记录只在一侧超出 → [OverlapPlan.updates] 把超出的那一侧裁掉；
 * - 老记录两侧都超出 → update 保留左侧残余，clones 复制出右侧残余；
 * - 首尾相接（老记录 end == 目标 start 等）或没有交集 → 原样不动。
 */
data class OverlapUpdate(val event: Event, val startAt: Long, val endAt: Long)

/** 老记录横跨目标区间两侧时，右侧残余被复制成的新记录；左侧残余由 [OverlapUpdate] 原地保留。 */
data class OverlapClone(
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
)

/** [resolveOverlap] 的结果：updates / clones 静默生效，victims 需要用户确认。 */
data class OverlapPlan(
    val updates: List<OverlapUpdate> = emptyList(),
    val clones: List<OverlapClone> = emptyList(),
    val victims: List<Event> = emptyList(),
)

/**
 * 计算把 [startAt, endAt) 写进时间轴时，其余记录 [others] 该怎么让路。
 *
 * 区间按半开区间 [start, end) 处理：老记录的 end == 目标 start（或反之）不算重叠。
 * 目标区间非法（endAt <= startAt）时返回空方案；零宽 / 负宽的老记录直接跳过。
 */
fun resolveOverlap(startAt: Long, endAt: Long, others: List<Event>): OverlapPlan {
    if (endAt <= startAt) return OverlapPlan()

    val updates = mutableListOf<OverlapUpdate>()
    val clones = mutableListOf<OverlapClone>()
    val victims = mutableListOf<Event>()

    others.forEach { other ->
        val otherStart = other.startAt
        val otherEnd = other.endAt
        // 脏数据（零宽 / 负宽）和首尾相接都不算重叠。
        if (otherEnd <= otherStart) return@forEach
        if (otherStart >= endAt || otherEnd <= startAt) return@forEach

        val keepsLeft = otherStart < startAt
        val keepsRight = otherEnd > endAt
        when {
            keepsLeft && keepsRight -> {
                updates += OverlapUpdate(other, otherStart, startAt)
                clones += OverlapClone(
                    note = other.note,
                    startAt = endAt,
                    endAt = otherEnd,
                    categoryId = other.categoryId,
                    createdAt = other.createdAt,
                )
            }

            keepsLeft -> updates += OverlapUpdate(other, otherStart, startAt)
            keepsRight -> updates += OverlapUpdate(other, endAt, otherEnd)
            else -> victims += other
        }
    }

    return OverlapPlan(updates = updates, clones = clones, victims = victims)
}
```

## `app/src/main/java/com/example/timelog/domain/Statistics.kt`

```kotlin
package com.example.timelog.domain

import com.example.timelog.domain.model.CategoryNode
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.TimelineEvent
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** 统计页的 5 种查看方式：日 / 周 / 月 / 年 / 自定义。 */
enum class StatsMode { DAY, WEEK, MONTH, YEAR, CUSTOM }

/** 统计窗口，半开区间 [startAt, endAt)，时间戳按本地时区解释。 */
data class StatsWindow(
    val mode: StatsMode,
    val startAt: Long,
    val endAt: Long,
)

/**
 * 由 [StatsMode] 推出具体窗口，并支持前后翻页。
 *
 * 全部计算都依赖显式传入的 [ZoneId]，不读取系统时区。
 */
object StatsWindows {

    /**
     * [anchor] 所在周期的窗口。
     *
     * - DAY：当天
     * - WEEK：周一 00:00 到下周一 00:00（以周一为一周首日）
     * - MONTH：当月 1 日到下月 1 日
     * - YEAR：当年 1 月 1 日到次年 1 月 1 日
     * - CUSTOM：没有自然周期，按 [anchor] 单日返回；自定义范围请用 [custom]
     */
    fun of(mode: StatsMode, anchor: LocalDate, zone: ZoneId): StatsWindow = when (mode) {
        StatsMode.DAY -> window(StatsMode.DAY, anchor, anchor.plusDays(1), zone)
        StatsMode.WEEK -> {
            val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            window(StatsMode.WEEK, monday, monday.plusWeeks(1), zone)
        }
        StatsMode.MONTH -> {
            val firstDay = anchor.withDayOfMonth(1)
            window(StatsMode.MONTH, firstDay, firstDay.plusMonths(1), zone)
        }
        StatsMode.YEAR -> {
            val firstDay = anchor.withDayOfYear(1)
            window(StatsMode.YEAR, firstDay, firstDay.plusYears(1), zone)
        }
        StatsMode.CUSTOM -> window(StatsMode.CUSTOM, anchor, anchor.plusDays(1), zone)
    }

    /** 闭区间日期 [startDate]..[endDate] 对应的自定义窗口；结束早于起始时自动交换。 */
    fun custom(startDate: LocalDate, endDate: LocalDate, zone: ZoneId): StatsWindow {
        val start = if (endDate.isBefore(startDate)) endDate else startDate
        val end = if (endDate.isBefore(startDate)) startDate else endDate
        return window(StatsMode.CUSTOM, start, end.plusDays(1), zone)
    }

    /**
     * 前后平移 [steps] 个周期（负数向前）。
     *
     * DAY / WEEK / MONTH / YEAR 按窗口起始日做日历加减，CUSTOM 按窗口长度（毫秒）平移。
     */
    fun shift(window: StatsWindow, steps: Int, zone: ZoneId): StatsWindow {
        if (steps == 0) return window
        return when (window.mode) {
            StatsMode.DAY -> of(StatsMode.DAY, dateOf(window.startAt, zone).plusDays(steps.toLong()), zone)
            StatsMode.WEEK -> of(StatsMode.WEEK, dateOf(window.startAt, zone).plusWeeks(steps.toLong()), zone)
            StatsMode.MONTH -> of(StatsMode.MONTH, dateOf(window.startAt, zone).plusMonths(steps.toLong()), zone)
            StatsMode.YEAR -> of(StatsMode.YEAR, dateOf(window.startAt, zone).plusYears(steps.toLong()), zone)
            StatsMode.CUSTOM -> {
                val length = window.endAt - window.startAt
                val start = window.startAt + length * steps
                StatsWindow(StatsMode.CUSTOM, start, start + length)
            }
        }
    }

    /** 本地日期对应的当天零点时间戳。 */
    internal fun startOfDay(date: LocalDate, zone: ZoneId): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    /** 时间戳在 [zone] 下的本地日期。 */
    internal fun dateOf(epochMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    private fun window(
        mode: StatsMode,
        startDate: LocalDate,
        endDate: LocalDate,
        zone: ZoneId,
    ): StatsWindow = StatsWindow(mode, startOfDay(startDate, zone), startOfDay(endDate, zone))
}

/**
 * 一个分类（含全部子类）在统计窗口内的用时。
 *
 * 环形图只画前 [StatsBuilder.MAX_DEPTH] 层，[children] 也按此截断；
 * 更深的分类时长已计入所属祖先的 [millis]。
 */
data class CategoryUsage(
    val categoryId: Long,
    val name: String,
    val colorHex: Long,
    val millis: Long,
    val eventCount: Int,
    val depth: Int,
    /** 与当前窗口等长、同模式的上一个窗口里同一分类（含子类）的总时长。 */
    val previousMillis: Long = 0L,
    val children: List<CategoryUsage> = emptyList(),
) {
    /** 环比变化；[previousMillis] 为 0（新增分类）时为 null。 */
    val deltaRatio: Float?
        get() = if (previousMillis > 0L) {
            (millis - previousMillis).toFloat() / previousMillis
        } else {
            null
        }
}

/** 统计结果：窗口、总量与按层级组织的分类用时。 */
data class StatsSummary(
    val window: StatsWindow,
    val totalMillis: Long,
    val eventCount: Int,
    /** 根分类（depth = 0），按用时降序；tree 中不存在的分类合成在最后。 */
    val roots: List<CategoryUsage>,
    /** 等长同模式上一个窗口的总时长（对齐已过时长，见 [StatsBuilder.build]）。 */
    val previousTotalMillis: Long,
)

/**
 * 把事件列表聚合成 [StatsSummary]。
 *
 * 纯函数：所有时间相关输入（[window]、[now]、[zone]）都由参数传入。
 */
object StatsBuilder {

    /** 环形图最多呈现三层：根分类、二级、三级。 */
    const val MAX_DEPTH: Int = 3

    /** tree 中找不到分类时，合成根使用的分类 ID。 */
    const val ORPHAN_CATEGORY_ID: Long = -1L

    private const val FALLBACK_COLOR = 0xFF9AA29A
    private const val PATH_SEPARATOR = " / "
    private const val UNKNOWN_NAME = "未分类"

    fun build(
        events: List<TimelineEvent>,
        tree: CategoryTree,
        window: StatsWindow,
        now: Long,
        zone: ZoneId,
    ): StatsSummary {
        val current = aggregate(clip(events, window.startAt, minOf(window.endAt, now)), tree)
        val previousWindow = previousWindowOf(window, now, zone)
        val previous = aggregate(
            clip(events, previousWindow.startAt, minOf(previousWindow.endAt, now)),
            tree,
        )

        val currentTotals = subtreeTotals(tree, current)
        val previousTotals = subtreeTotals(tree, previous)

        val roots = tree.roots
            .mapNotNull { node -> usageOf(node, 0, currentTotals, previousTotals) }
            .sortedWith(USAGE_ORDER)

        return StatsSummary(
            window = window,
            totalMillis = current.totalMillis,
            eventCount = current.eventCount,
            roots = roots + orphanUsages(current, previous),
            previousTotalMillis = previous.totalMillis,
        )
    }

    /** 与 [window] 等长、同模式的上一个窗口；当前窗口包含 [now] 时对齐到已过时长。 */
    private fun previousWindowOf(window: StatsWindow, now: Long, zone: ZoneId): StatsWindow {
        val previous = StatsWindows.shift(window, -1, zone)
        if (now < window.startAt || now >= window.endAt) return previous
        val elapsed = now - window.startAt
        val alignedEnd = previous.startAt + elapsed
        return if (alignedEnd < previous.endAt) previous.copy(endAt = alignedEnd) else previous
    }

    private fun clip(events: List<TimelineEvent>, from: Long, until: Long): List<ClippedEvent> {
        if (until <= from) return emptyList()
        return events.mapNotNull { event ->
            val start = maxOf(event.event.startAt, from)
            val end = minOf(event.event.startAt + event.event.durationMillis, until)
            if (end <= start) null else ClippedEvent(event, start, end)
        }
    }

    private fun aggregate(clipped: List<ClippedEvent>, tree: CategoryTree): Aggregate {
        val directMillis = HashMap<Long, Long>()
        val directCounts = HashMap<Long, Int>()
        val orphanMillis = HashMap<String, Long>()
        val orphanCounts = HashMap<String, Int>()
        val orphanColors = HashMap<String, Long>()
        var totalMillis = 0L

        clipped
            .sortedWith(compareBy({ it.event.event.startAt }, { it.event.event.id }))
            .forEach { item ->
                val duration = item.end - item.start
                totalMillis += duration
                val categoryId = item.event.event.categoryId
                if (tree.find(categoryId) != null) {
                    directMillis[categoryId] = (directMillis[categoryId] ?: 0L) + duration
                    directCounts[categoryId] = (directCounts[categoryId] ?: 0) + 1
                } else {
                    val name = orphanName(item.event.categoryPath)
                    orphanMillis[name] = (orphanMillis[name] ?: 0L) + duration
                    orphanCounts[name] = (orphanCounts[name] ?: 0) + 1
                    orphanColors.putIfAbsent(name, item.event.categoryColor)
                }
            }

        return Aggregate(
            directMillis = directMillis,
            directCounts = directCounts,
            orphanMillis = orphanMillis,
            orphanCounts = orphanCounts,
            orphanColors = orphanColors,
            totalMillis = totalMillis,
            eventCount = clipped.size,
        )
    }

    /** 沿分类树自下而上累计时长与次数（含全部层级，供环形图的最内层使用）。 */
    private fun subtreeTotals(tree: CategoryTree, aggregate: Aggregate): SubtreeTotals {
        val millis = HashMap<Long, Long>()
        val counts = HashMap<Long, Int>()

        fun walk(node: CategoryNode): Pair<Long, Int> {
            var totalMillis = aggregate.directMillis[node.category.id] ?: 0L
            var totalCount = aggregate.directCounts[node.category.id] ?: 0
            node.children.forEach { child ->
                val (childMillis, childCount) = walk(child)
                totalMillis += childMillis
                totalCount += childCount
            }
            millis[node.category.id] = totalMillis
            counts[node.category.id] = totalCount
            return totalMillis to totalCount
        }

        tree.roots.forEach { walk(it) }
        return SubtreeTotals(millis, counts)
    }

    private fun usageOf(
        node: CategoryNode,
        depth: Int,
        totals: SubtreeTotals,
        previousTotals: SubtreeTotals,
    ): CategoryUsage? {
        val millis = totals.millis[node.category.id] ?: 0L
        if (millis <= 0L) return null
        val children = if (depth < MAX_DEPTH - 1) {
            node.children
                .mapNotNull { child -> usageOf(child, depth + 1, totals, previousTotals) }
                .sortedWith(USAGE_ORDER)
        } else {
            emptyList()
        }
        return CategoryUsage(
            categoryId = node.category.id,
            name = node.category.name,
            colorHex = node.category.colorHex,
            millis = millis,
            eventCount = totals.counts[node.category.id] ?: 0,
            depth = depth,
            previousMillis = previousTotals.millis[node.category.id] ?: 0L,
            children = children,
        )
    }

    private fun orphanUsages(current: Aggregate, previous: Aggregate): List<CategoryUsage> =
        current.orphanMillis.map { (name, millis) ->
            CategoryUsage(
                categoryId = ORPHAN_CATEGORY_ID,
                name = name,
                colorHex = current.orphanColors[name] ?: FALLBACK_COLOR,
                millis = millis,
                eventCount = current.orphanCounts[name] ?: 0,
                depth = 0,
                previousMillis = previous.orphanMillis[name] ?: 0L,
                children = emptyList(),
            )
        }.sortedWith(USAGE_ORDER)

    private fun orphanName(categoryPath: String): String {
        val separator = categoryPath.indexOf(PATH_SEPARATOR)
        val head = if (separator < 0) categoryPath else categoryPath.substring(0, separator)
        return head.ifBlank { UNKNOWN_NAME }
    }
}

private data class ClippedEvent(val event: TimelineEvent, val start: Long, val end: Long)

private class Aggregate(
    val directMillis: Map<Long, Long>,
    val directCounts: Map<Long, Int>,
    val orphanMillis: Map<String, Long>,
    val orphanCounts: Map<String, Int>,
    val orphanColors: Map<String, Long>,
    val totalMillis: Long,
    val eventCount: Int,
)

private class SubtreeTotals(
    val millis: Map<Long, Long>,
    val counts: Map<Long, Int>,
)

/** 分类用时排序：时长降序，再看名称，保证渲染顺序稳定。 */
private val USAGE_ORDER: Comparator<CategoryUsage> =
    compareByDescending<CategoryUsage> { it.millis }.thenBy { it.name }
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

## `app/src/main/java/com/example/timelog/domain/model/CategoryPalette.kt`

```kotlin
package com.example.timelog.domain.model

/**
 * 二十四节气颜色：按「春夏秋冬各 6 个节气」排列，分类颜色选择器按 4 行 6 列展示，
 * 颜色下方标注节气名。颜色整体压低饱和度，保证在纸色背景上与墨色文字同时成立；
 * 深色模式复用同一组色值。
 */
data class SolarTermColor(
    val name: String,
    val season: String,
    val colorHex: Long,
)

val SolarTerms: List<SolarTermColor> = listOf(
    SolarTermColor("立春", "春", 0xFF7D9E6E),
    SolarTermColor("雨水", "春", 0xFF6E97A3),
    SolarTermColor("惊蛰", "春", 0xFF5E9C90),
    SolarTermColor("春分", "春", 0xFFC48E8E),
    SolarTermColor("清明", "春", 0xFF8B72A6),
    SolarTermColor("谷雨", "春", 0xFFA98A5A),
    SolarTermColor("立夏", "夏", 0xFF6E9A4C),
    SolarTermColor("小满", "夏", 0xFFB8933F),
    SolarTermColor("芒种", "夏", 0xFF8C9A46),
    SolarTermColor("夏至", "夏", 0xFFC9853F),
    SolarTermColor("小暑", "夏", 0xFFC46E4A),
    SolarTermColor("大暑", "夏", 0xFFA8463A),
    SolarTermColor("立秋", "秋", 0xFFB99A50),
    SolarTermColor("处暑", "秋", 0xFF9E86B8),
    SolarTermColor("白露", "秋", 0xFF8A9AB8),
    SolarTermColor("秋分", "秋", 0xFFC9A24E),
    SolarTermColor("寒露", "秋", 0xFF5A7FA8),
    SolarTermColor("霜降", "秋", 0xFF94594A),
    SolarTermColor("立冬", "冬", 0xFF4E7368),
    SolarTermColor("小雪", "冬", 0xFF8A86A8),
    SolarTermColor("大雪", "冬", 0xFF62788E),
    SolarTermColor("冬至", "冬", 0xFF3F5B78),
    SolarTermColor("小寒", "冬", 0xFF557E92),
    SolarTermColor("大寒", "冬", 0xFF35505F),
)

/** 兼容旧接口：仅取色值。 */
val SolarTermColors: List<Long> = SolarTerms.map { it.colorHex }

/** 四季顺序，供 4 行 6 列色板使用。 */
val SolarTermSeasons: List<Pair<String, List<SolarTermColor>>> =
    listOf("春", "夏", "秋", "冬").map { season -> season to SolarTerms.filter { it.season == season } }
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

## `app/src/main/java/com/example/timelog/domain/model/LedgerSnapshot.kt`

```kotlin
package com.example.timelog.domain.model

/** 导入 / 导出用的完整数据快照，与数据库实体解耦，方便用 JSON 表达。 */
data class LedgerSnapshot(
    val exportedAt: Long,
    val categories: List<ExportedCategory>,
    val events: List<ExportedEvent>,
)

data class ExportedCategory(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val sortOrder: Int,
    val colorHex: Long,
    val isSystem: Boolean,
)

data class ExportedEvent(
    val note: String?,
    val startAt: Long,
    val endAt: Long,
    val categoryId: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class ImportMode { MERGE, REPLACE }
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
import androidx.compose.ui.unit.sp
import com.example.timelog.domain.model.CategoryNode
import com.example.timelog.domain.model.SolarTermSeasons
import com.example.timelog.ui.home.CategoryDot

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
                    text = "分类最多 3 层。删除分类时，其中的事件会一并删除。",
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
                    SolarTermPalette(
                        selectedHex = editor.colorHex,
                        onSelect = onEditorColorChange,
                    )
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
                            append("其中 ${pending.eventCount} 条事件会一并删除。")
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

/** 24 节气色板：4 行（春夏秋冬）× 6 列，颜色下方标注节气名。 */
@Composable
private fun SolarTermPalette(selectedHex: Long, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SolarTermSeasons.forEach { (season, terms) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = season,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(20.dp)
                        .padding(top = 7.dp),
                )
                terms.forEach { term ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ColorSwatch(
                            colorHex = term.colorHex,
                            selected = term.colorHex == selectedHex,
                            onClick = { onSelect(term.colorHex) },
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = term.name,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            maxLines = 1,
                            color = if (term.colorHex == selectedHex) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(colorHex: Long, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
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
import com.example.timelog.domain.model.SolarTermColors
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
                    colorHex = SolarTermColors.random(),
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
                    val deleted = if (outcome.deletedEventCount > 0) {
                        "，${outcome.deletedEventCount} 条事件"
                    } else {
                        ""
                    }
                    "已删除 ${outcome.categoryCount} 个分类$deleted"
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

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoryManageViewModel(repository) }
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/data/DataTransferSection.kt`

```kotlin
package com.example.timelog.ui.data

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timelog.domain.model.ImportMode
import com.example.timelog.util.DateTimeFormat

/** 打开 SAF 文件选择器时接受的 MIME：不同文件管理器对 .json 的声明不一致，多给几个。 */
private val ImportMimeTypes = arrayOf(
    "application/json",
    "text/plain",
    "application/octet-stream",
)

/**
 * 「数据」卡片：导出数据（SAF 新建 .json）与导入数据（先选文件，再选导入方式）。
 * 具体的读写逻辑全部在 [DataTransferViewModel] 里，这里只负责 SAF 交互和状态展示。
 */
@Composable
fun DataTransferSection(
    viewModel: DataTransferViewModel,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportChooser by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) viewModel.export(context, uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportChooser = true
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "数据",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                if (state.busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "导出为 JSON 备份，或从备份文件恢复记录。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TransferButton(
                    text = "导出数据",
                    enabled = !state.busy,
                    filled = true,
                    onClick = {
                        val fileName = "時-" +
                            DateTimeFormat.isoDate(System.currentTimeMillis()) + ".json"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f),
                )
                TransferButton(
                    text = "导入数据",
                    enabled = !state.busy,
                    filled = false,
                    onClick = { importLauncher.launch(ImportMimeTypes) },
                    modifier = Modifier.weight(1f),
                )
            }
            state.message?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
    }

    if (showImportChooser) {
        ImportModeDialog(
            onDismiss = {
                showImportChooser = false
                pendingImportUri = null
            },
            onSelect = { mode ->
                val uri = pendingImportUri
                showImportChooser = false
                pendingImportUri = null
                if (uri != null) viewModel.import(context, uri, mode)
            },
        )
    }
}

@Composable
private fun TransferButton(
    text: String,
    enabled: Boolean,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ImportModeDialog(
    onDismiss: () -> Unit,
    onSelect: (ImportMode) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入数据") },
        text = {
            Column {
                ImportModeOption(
                    title = "合并导入",
                    description = "保留现有数据，重名分类会新建。",
                    warning = false,
                    onClick = { onSelect(ImportMode.MERGE) },
                )
                Spacer(Modifier.height(10.dp))
                ImportModeOption(
                    title = "覆盖导入",
                    description = "清空现有全部记录后导入，此操作无法撤销。",
                    warning = true,
                    onClick = { onSelect(ImportMode.REPLACE) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun ImportModeOption(
    title: String,
    description: String,
    warning: Boolean,
    onClick: () -> Unit,
) {
    val accent = if (warning) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/data/DataTransferViewModel.kt`

```kotlin
package com.example.timelog.ui.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.data.transfer.TransferJson
import com.example.timelog.domain.model.ImportMode
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 导入 / 导出的状态：busy 期间按钮禁用，message 直接显示在卡片里。 */
data class DataTransferState(
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

/**
 * SAF（Storage Access Framework）文件读写 + 仓库快照 / 恢复。
 *
 * 导出：repository.snapshot() -> TransferJson.encode -> contentResolver.openOutputStream
 * 导入：contentResolver.openInputStream -> TransferJson.decode -> repository.restore
 * 文件读写都放在 Dispatchers.IO，UI 线程只更新状态。
 */
class DataTransferViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _state = MutableStateFlow(DataTransferState())
    val state: StateFlow<DataTransferState> = _state.asStateFlow()

    fun export(context: Context, uri: Uri) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null, isError = false) }
        val appContext = context.applicationContext ?: context
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val json = TransferJson.encode(repository.snapshot())
                    val output = appContext.contentResolver.openOutputStream(uri)
                        ?: throw IOException("无法打开目标文件")
                    output.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                        stream.flush()
                    }
                }.exceptionOrNull()
            }
            if (error == null) {
                _state.update { it.copy(busy = false, message = "已导出", isError = false) }
            } else {
                _state.update { it.copy(busy = false, message = exportMessage(error), isError = true) }
            }
        }
    }

    fun import(context: Context, uri: Uri, mode: ImportMode) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null, isError = false) }
        val appContext = context.applicationContext ?: context
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val input = appContext.contentResolver.openInputStream(uri)
                        ?: throw IOException("无法打开所选文件")
                    val text = input.use { stream -> stream.readBytes().toString(Charsets.UTF_8) }
                    val snapshot = TransferJson.decode(text)
                    repository.restore(snapshot, mode)
                }.exceptionOrNull()
            }
            if (error == null) {
                _state.update { it.copy(busy = false, message = "已导入", isError = false) }
            } else {
                _state.update { it.copy(busy = false, message = importMessage(error), isError = true) }
            }
        }
    }

    private fun exportMessage(error: Throwable): String =
        if (error is SecurityException) "没有访问该文件的权限" else "导出失败，请重试"

    private fun importMessage(error: Throwable): String {
        if (error is SecurityException) return "没有访问该文件的权限"
        val message = (error as? IllegalArgumentException)?.message
        if (!message.isNullOrBlank()) return message
        return "导入失败，请重试"
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DataTransferViewModel(repository) as T
            }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/home/HomeScreen.kt`

```kotlin
package com.example.timelog.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timelog.ui.theme.ActionBlue
import com.example.timelog.ui.theme.Clay
import com.example.timelog.util.DateTimeFormat

/**
 * 时间轴页：不再自带 Scaffold / 顶栏 / 底栏，外壳（MainActivity）统一提供页签与右下角「+」按钮，
 * 这样时间轴与统计页可以放在同一个 HorizontalPager 里左右滑动切换。
 */
@Composable
fun TimelinePage(
    state: HomeUiState,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Unit
            state.days.isEmpty() -> EmptyTimeline()
            else -> TimelineList(
                days = state.days,
                uncategorizedName = state.uncategorized?.name ?: "未分类",
                uncategorizedColor = state.uncategorized?.colorHex,
                onEventClick = onEventClick,
                onEventLongClick = onEventLongClick,
                onFillGap = onFillGap,
            )
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

/**
 * 时间轴页右下角的悬浮加号：蓝底圆圈 + 白色「+」，点击新建记录。
 * 由外壳（MainActivity）叠加在时间轴页之上；列表底部已预留 96dp，避免遮住最后一条记录。
 */
@Composable
fun TimelineFabOverlay(onNewRecord: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 24.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .size(62.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(ActionBlue)
                .clickable(role = Role.Button, onClick = onNewRecord),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "新建记录",
                tint = Color.White,
                modifier = Modifier.size(30.dp),
            )
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
            text = "点右下角的 +，记下刚刚做完的事。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimelineList(
    days: List<HomeDay>,
    uncategorizedName: String,
    uncategorizedColor: Long?,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
    onFillGap: (Long, Long) -> Unit,
) {
    val listState = rememberLazyListState()
    val itemCount = days.size + days.sumOf { it.segments.size } + days.count { it.isToday }
    // 倒序改成正序后，最新的记录在列表底部，靠近右下角的「+」；第一次打开与新增记录后都停在底部。
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
        // 底部留 96dp 给右下角的悬浮「+」，避免遮住最后一条记录。
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
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
                TimelineSegmentRow(
                    segment = segment,
                    uncategorizedName = uncategorizedName,
                    uncategorizedColor = uncategorizedColor,
                    onEventClick = onEventClick,
                    onEventLongClick = onEventLongClick,
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
        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
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
import com.example.timelog.domain.model.Category
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
    /** 系统分类「未分类」：未记录的空隙和归入系统分类的事件共用同一个名字与颜色。 */
    val uncategorized: Category? = null,
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
        repository.categoryTree,
        clock,
        transient,
    ) { events, tree, now, local ->
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
            uncategorized = tree.flatten().firstOrNull { it.first.category.isSystem }?.first?.category,
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
private val EventShape = RoundedCornerShape(8.dp)

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
 *
 * 合并块里每条记录各自可点：重叠记录以前整块不可点，导致无法编辑，这里按事件拆开点击区域。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineSegmentRow(
    segment: TimelineSegment,
    uncategorizedName: String = "未分类",
    uncategorizedColor: Long? = null,
    onEventClick: (Long) -> Unit = {},
    onEventLongClick: (Long) -> Unit = {},
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
        Box(modifier = Modifier.width(48.dp).height(height)) {
            Text(
                text = DateTimeFormat.time(segment.startAt),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                ),
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
            UnrecordedBlock(
                segment = segment,
                name = uncategorizedName,
                colorHex = uncategorizedColor,
                modifier = fillable,
            )
        } else {
            // 整块可点：打开块内第一条记录；块内单条记录保持各自的可点/长按区，且内层优先消费点击。
            val firstEventId = segment.events.firstOrNull()?.event?.id
            val blockClickable = if (firstEventId != null) {
                blockModifier
                    .clip(BlockShape)
                    .combinedClickable(
                        onClick = { onEventClick(firstEventId) },
                        onLongClick = { onEventLongClick(firstEventId) },
                    )
            } else {
                blockModifier
            }
            RecordedBlock(
                segment = segment,
                modifier = blockClickable,
                onEventClick = onEventClick,
                onEventLongClick = onEventLongClick,
            )
        }
    }
}

@Composable
private fun RecordedBlock(
    segment: TimelineSegment,
    modifier: Modifier,
    onEventClick: (Long) -> Unit,
    onEventLongClick: (Long) -> Unit,
) {
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
            modifier = Modifier.padding(start = 13.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
        ) {
            segment.events.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(Modifier.height(2.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EventShape)
                        .combinedClickable(
                            onClick = { onEventClick(item.event.id) },
                            onLongClick = { onEventLongClick(item.event.id) },
                        )
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryDot(colorHex = item.categoryColor, size = 6.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.categoryPath,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
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
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnrecordedBlock(
    segment: TimelineSegment,
    name: String,
    colorHex: Long?,
    modifier: Modifier,
) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    val height = rememberBlockHeight(segment)
    val labelColor = colorHex?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (colorHex != null) {
                    CategoryDot(colorHex = colorHex, size = 6.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                    ),
                    color = labelColor,
                )
            }
            if (height >= 62.dp) {
                Text(
                    text = DateTimeFormat.durationText(segment.startAt, segment.endAt),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
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
        (46f + minutes * 0.45f).coerceIn(50f, 120f).dp
    } else {
        val extraRows = (segment.events.size - 1).coerceAtLeast(0) * 28f
        (58f + minutes * 0.60f + extraRows).coerceIn(62f, 220f).dp
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
import androidx.compose.material3.AlertDialog
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
import com.example.timelog.domain.model.Event
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
    onConfirmOverwrite: () -> Unit,
    onCancelOverwrite: () -> Unit,
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

    // 保存时如果会整段盖掉已有记录，先让用户确认；单侧裁剪 / 拆分不需要打扰。
    if (state.pendingOverwrite.isNotEmpty()) {
        OverlapConfirmDialog(
            victims = state.pendingOverwrite,
            isSaving = state.isSaving,
            onConfirm = onConfirmOverwrite,
            onDismiss = onCancelOverwrite,
        )
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

/** 保存会整段覆盖已有记录时的确认框：逐条列出会被删掉的记录。 */
@Composable
private fun OverlapConfirmDialog(
    victims: List<Event>,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("时间重叠") },
        text = {
            Column {
                Text("保存之后，下面这些记录会被整段覆盖并删除：")
                victims.forEach { victim ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = DateTimeFormat.rangeText(victim.startAt, victim.endAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    victim.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text("仍要保存", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("取消") }
        },
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
import com.example.timelog.domain.OverlapPlan
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.Event
import com.example.timelog.domain.resolveOverlap
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
    /** 会被目标区间整段盖住、需要用户确认的已有记录；非空时由 RecordSheet 弹确认框。 */
    val pendingOverwrite: List<Event> = emptyList(),
)

class RecordViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** 等用户确认「仍要保存」的落库参数；确认后按同一份 [OverlapPlan] 真正写入。 */
    private var pendingSave: PendingSave? = null

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
        pendingSave = null
        _uiState.update {
            it.copy(
                isSaving = false,
                saved = false,
                timeError = false,
                pendingOverwrite = emptyList(),
            )
        }
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

    /**
     * 保存：先查重叠并算出让路方案。
     * - 只有单侧裁剪 / 拆分（updates / clones）时静默生效；
     * - 有整段被盖住的记录（victims）时先挂起，等用户在确认框里点「仍要保存」再落库。
     */
    fun save() {
        val state = _uiState.value
        if (state.isSaving || pendingSave != null) return
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
                // 契约：getOverlappingEvents(startAt, endAt, excludeId)
                val others = repository.getOverlappingEvents(
                    startAt,
                    endAt,
                    state.eventId ?: -1L,
                )
                val plan = resolveOverlap(startAt, endAt, others)
                if (plan.victims.isNotEmpty()) {
                    pendingSave = PendingSave(
                        id = state.eventId,
                        note = state.note,
                        startAt = startAt,
                        endAt = endAt,
                        categoryId = categoryId,
                        plan = plan,
                    )
                    _uiState.update {
                        it.copy(isSaving = false, pendingOverwrite = plan.victims)
                    }
                } else {
                    // 契约：saveEventResolved(id, note, startAt, endAt, categoryId, plan)
                    repository.saveEventResolved(
                        state.eventId,
                        state.note,
                        startAt,
                        endAt,
                        categoryId,
                        plan,
                    )
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                }
            }.onFailure {
                pendingSave = null
                _uiState.update { it.copy(isSaving = false, pendingOverwrite = emptyList()) }
            }
        }
    }

    /** 重叠确认框里点「仍要保存」：按挂起时的方案真正写入，victims 会被删除。 */
    fun confirmOverwrite() {
        val pending = pendingSave ?: return
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                repository.saveEventResolved(
                    pending.id,
                    pending.note,
                    pending.startAt,
                    pending.endAt,
                    pending.categoryId,
                    pending.plan,
                )
            }.onSuccess {
                pendingSave = null
                _uiState.update {
                    it.copy(isSaving = false, saved = true, pendingOverwrite = emptyList())
                }
            }.onFailure {
                pendingSave = null
                _uiState.update { it.copy(isSaving = false, pendingOverwrite = emptyList()) }
            }
        }
    }

    /** 重叠确认框里点「取消」：放弃这次保存，回到编辑状态。 */
    fun dismissOverwrite() {
        pendingSave = null
        _uiState.update { it.copy(isSaving = false, pendingOverwrite = emptyList()) }
    }

    fun reset() {
        loadJob?.cancel()
        pendingSave = null
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

    private data class PendingSave(
        val id: Long?,
        val note: String?,
        val startAt: Long,
        val endAt: Long,
        val categoryId: Long,
        val plan: OverlapPlan,
    )

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecordViewModel(repository) }
        }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/search/SearchScreen.kt`

```kotlin
package com.example.timelog.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.ui.home.CategoryDot
import com.example.timelog.util.DateTimeFormat

/**
 * 备注搜索页：整屏列表（不用 Scaffold），顶部是返回箭头 + 自动聚焦的搜索框，
 * 下面按「备注 / 分类路径 / 日期 + 时间段」列出命中记录，点击回调事件 id。
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    viewModel: SearchViewModel,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onBack = onBack,
            focusRequester = focusRequester,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        when {
            query.isBlank() -> SearchHint()
            results.isEmpty() -> NoSearchResult(query = query)
            else -> SearchResultList(results = results, onOpenEvent = onOpenEvent)
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "搜索备注",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "清空",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun SearchResultList(
    results: List<TimelineEvent>,
    onOpenEvent: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp),
    ) {
        item(key = "count") {
            Text(
                text = "找到 " + results.size + " 条记录",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 2.dp),
            )
        }
        items(items = results, key = { it.event.id }) { item ->
            SearchResultRow(item = item, onClick = { onOpenEvent(item.event.id) })
        }
    }
}

@Composable
private fun SearchResultRow(
    item: TimelineEvent,
    onClick: () -> Unit,
) {
    val event = item.event
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.padding(top = 7.dp)) {
                CategoryDot(colorHex = item.categoryColor, size = 8.dp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.note ?: "无备注",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (event.note != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.categoryPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateTimeFormat.datePart(event.startAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = DateTimeFormat.rangeText(event.startAt, event.endAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 42.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun SearchHint() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "搜索备注",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "输入关键词，就能找到备注里包含它的记录。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NoSearchResult(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "没有找到",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "没有备注包含「" + query.trim() + "」。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

## `app/src/main/java/com/example/timelog/ui/search/SearchViewModel.kt`

```kotlin
package com.example.timelog.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.model.TimelineEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** 输入停顿 250ms 后再查询，避免每敲一个字都打一次数据库。 */
    val results: StateFlow<List<TimelineEvent>> = _query
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .flatMapLatest { repository.searchEvents(it.trim()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MILLIS = 250L

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SearchViewModel(repository) as T
            }
    }
}
```

## `app/src/main/java/com/example/timelog/ui/stats/StatisticsScreen.kt`

```kotlin
package com.example.timelog.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timelog.domain.CategoryUsage
import com.example.timelog.domain.StatsMode
import com.example.timelog.domain.StatsSummary
import com.example.timelog.ui.theme.FallGreen
import com.example.timelog.ui.theme.FallGreenDark
import com.example.timelog.ui.theme.RiseRed
import com.example.timelog.ui.theme.RiseRedDark
import com.example.timelog.util.DateTimeFormat
import java.time.LocalDate
import kotlin.math.roundToInt

/** 自定义时间段需要连续弹两次日期选择：先起始日，再结束日。 */
private enum class DatePickerStep { START, END }

private const val RING_LAYER_COUNT = 3
private const val RING_GAP_ANGLE = 2.5f
private const val RING_MIN_ANGLE = 1f

/** 各层环带的内 / 外半径比例（相对于外接圆半径 R）。 */
private val RING_INNER_RATIOS = floatArrayOf(0.46f, 0.68f, 0.84f)
private val RING_OUTER_RATIOS = floatArrayOf(0.68f, 0.84f, 1.0f)

/**
 * 统计页（v3.0）：模式切换、范围导航、多层环形堆叠图与分类用时列表。
 *
 * 页面整体可滚动；[footer] 渲染在内容最后，用于承载底部数据迁移入口。
 */
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onModeSelected: (StatsMode) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCustomRangeSelected: (LocalDate, LocalDate) -> Unit,
    footer: @Composable () -> Unit = {},
) {
    val summary = state.summary
    var pickerStep by remember { mutableStateOf<DatePickerStep?>(null) }
    var pendingStart by remember { mutableStateOf<LocalDate?>(null) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val riseColor = if (isDark) RiseRedDark else RiseRed
    val fallColor = if (isDark) FallGreenDark else FallGreen
    val today = LocalDate.now(DateTimeFormat.zone())

    fun openCustomRangePicker() {
        pendingStart = null
        pickerStep = DatePickerStep.START
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, bottom = 16.dp),
    ) {
        ModeSelector(
            mode = state.mode,
            onModeSelected = onModeSelected,
            onEditCustomRange = ::openCustomRangePicker,
        )
        RangeNav(
            label = state.label,
            mode = state.mode,
            canGoNext = state.canGoNext,
            onPrevious = onPrevious,
            onNext = onNext,
            onEditCustomRange = ::openCustomRangePicker,
        )
        when {
            state.isLoading -> Unit
            summary == null -> Unit
            else -> {
                RingSection(summary = summary)
                if (summary.totalMillis <= 0L) {
                    EmptyStats()
                } else {
                    SectionHeader("分类用时")
                    val maxMillis = summary.roots.maxOfOrNull { it.millis } ?: 1L
                    summary.roots.forEach { root ->
                        CategoryUsageRow(
                            usage = root,
                            maxMillis = maxMillis,
                            riseColor = riseColor,
                            fallColor = fallColor,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        footer()
    }

    when (pickerStep) {
        DatePickerStep.START -> DatePickerSheet(
            initialDate = state.customRange?.first ?: today,
            onDismiss = { pickerStep = null },
            onConfirm = { date ->
                pendingStart = date
                pickerStep = DatePickerStep.END
            },
        )
        DatePickerStep.END -> DatePickerSheet(
            initialDate = pendingStart ?: state.customRange?.second ?: today,
            onDismiss = { pickerStep = null },
            onConfirm = { end ->
                val start = pendingStart ?: end
                pendingStart = null
                pickerStep = null
                if (end.isBefore(start)) {
                    onCustomRangeSelected(end, start)
                } else {
                    onCustomRangeSelected(start, end)
                }
            },
        )
        null -> Unit
    }
}

@Composable
private fun ModeSelector(
    mode: StatsMode,
    onModeSelected: (StatsMode) -> Unit,
    onEditCustomRange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatsMode.entries.forEach { candidate ->
            FilterChip(
                selected = candidate == mode,
                onClick = {
                    if (candidate == StatsMode.CUSTOM && mode == StatsMode.CUSTOM) {
                        onEditCustomRange()
                    } else {
                        onModeSelected(candidate)
                    }
                },
                label = { Text(statsModeLabel(candidate), fontSize = 13.sp) },
            )
        }
    }
}

private fun statsModeLabel(mode: StatsMode): String = when (mode) {
    StatsMode.DAY -> "日"
    StatsMode.WEEK -> "周"
    StatsMode.MONTH -> "月"
    StatsMode.YEAR -> "年"
    StatsMode.CUSTOM -> "自定义"
}

@Composable
private fun RangeNav(
    label: String,
    mode: StatsMode,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onEditCustomRange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "上一个周期",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (mode == StatsMode.CUSTOM) {
                TextButton(
                    onClick = onEditCustomRange,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = "修改",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "下一个周期",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RingSection(summary: StatsSummary) {
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val isEmpty = summary.totalMillis <= 0L

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            val radius = minOf(size.width, size.height) / 2f
            if (radius <= 0f) return@Canvas
            val center = Offset(size.width / 2f, size.height / 2f)
            if (isEmpty) {
                for (layer in 0 until RING_LAYER_COUNT) {
                    drawRingTrack(layer, center, radius, trackColor)
                }
            } else {
                val layerHasContent = BooleanArray(RING_LAYER_COUNT)
                summary.roots.forEach { root ->
                    if (root.millis > 0L) layerHasContent[0] = true
                    if (root.children.isNotEmpty()) layerHasContent[1] = true
                    if (root.children.any { it.children.isNotEmpty() }) layerHasContent[2] = true
                }
                for (layer in 0 until RING_LAYER_COUNT) {
                    if (layerHasContent[layer]) drawRingTrack(layer, center, radius, trackColor)
                }
                drawUsageLayer(
                    usages = summary.roots,
                    layer = 0,
                    startAngle = -90f,
                    totalSweep = 360f,
                    center = center,
                    radius = radius,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isEmpty) "—" else DateTimeFormat.durationText(summary.totalMillis),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "共 ${summary.eventCount} 条记录",
                fontSize = 11.sp,
                color = mutedColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun EmptyStats() {
    Text(
        text = "这段时间还没有记录",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CategoryUsageRow(
    usage: CategoryUsage,
    maxMillis: Long,
    riseColor: Color,
    fallColor: Color,
) {
    val accent = Color(usage.colorHex)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val fraction = if (maxMillis <= 0L) {
        0f
    } else {
        (usage.millis.toDouble() / maxMillis.toDouble()).toFloat().coerceIn(0.03f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = usage.name,
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${usage.eventCount}次",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            DeltaLabel(usage = usage, riseColor = riseColor, fallColor = fallColor)
            Spacer(Modifier.width(8.dp))
            Text(
                text = DateTimeFormat.durationText(usage.millis),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent),
            )
        }
    }
}

@Composable
private fun DeltaLabel(usage: CategoryUsage, riseColor: Color, fallColor: Color) {
    val previous = usage.previousMillis
    if (previous <= 0L) {
        Text(
            text = "新增",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = riseColor,
            maxLines = 1,
        )
        return
    }
    val percent = ((usage.millis - previous).toDouble() / previous.toDouble() * 100.0).roundToInt()
    val text = when {
        percent > 0 -> "+$percent%"
        percent < 0 -> "$percent%"
        else -> "0%"
    }
    val color = when {
        percent > 0 -> riseColor
        percent < 0 -> fallColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        maxLines = 1,
    )
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
                    pickerState.selectedDateMillis?.let { millis ->
                        onConfirm(DateTimeFormat.dateFromUtcMillis(millis))
                    }
                },
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

/** 单层环带的几何参数：绘制矩形、直径与环宽。 */
private class RingGeometry(val topLeft: Offset, val size: Size, val strokeWidth: Float)

private fun ringGeometry(layer: Int, center: Offset, radius: Float): RingGeometry {
    val innerRadius = radius * RING_INNER_RATIOS[layer]
    val outerRadius = radius * RING_OUTER_RATIOS[layer]
    val ringRadius = (innerRadius + outerRadius) / 2f
    return RingGeometry(
        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
        size = Size(ringRadius * 2f, ringRadius * 2f),
        strokeWidth = outerRadius - innerRadius,
    )
}

/** 画一整圈淡色轨道。 */
private fun DrawScope.drawRingTrack(layer: Int, center: Offset, radius: Float, color: Color) {
    val geometry = ringGeometry(layer, center, radius)
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = geometry.topLeft,
        size = geometry.size,
        style = Stroke(width = geometry.strokeWidth),
    )
}

/**
 * 递归画出某一层分类环；子级按父级的角度范围再切分，形成多层堆叠。
 */
private fun DrawScope.drawUsageLayer(
    usages: List<CategoryUsage>,
    layer: Int,
    startAngle: Float,
    totalSweep: Float,
    center: Offset,
    radius: Float,
) {
    if (layer >= RING_LAYER_COUNT || usages.isEmpty()) return
    val totalMillis = usages.sumOf { it.millis }
    if (totalMillis <= 0L) return

    val geometry = ringGeometry(layer, center, radius)
    var cursor = startAngle
    usages.forEach { usage ->
        val sweep = (totalSweep.toDouble() * usage.millis.toDouble() / totalMillis.toDouble()).toFloat()
        val visibleSweep = (sweep - RING_GAP_ANGLE).coerceAtLeast(RING_MIN_ANGLE).coerceAtMost(sweep)
        if (visibleSweep > 0f) {
            drawArc(
                color = Color(usage.colorHex),
                startAngle = cursor + (sweep - visibleSweep) / 2f,
                sweepAngle = visibleSweep,
                useCenter = false,
                topLeft = geometry.topLeft,
                size = geometry.size,
                style = Stroke(width = geometry.strokeWidth),
            )
        }
        if (usage.children.isNotEmpty()) {
            drawUsageLayer(
                usages = usage.children,
                layer = layer + 1,
                startAngle = cursor,
                totalSweep = sweep,
                center = center,
                radius = radius,
            )
        }
        cursor += sweep
    }
}
```

## `app/src/main/java/com/example/timelog/ui/stats/StatisticsViewModel.kt`

```kotlin
package com.example.timelog.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.domain.StatsBuilder
import com.example.timelog.domain.StatsMode
import com.example.timelog.domain.StatsSummary
import com.example.timelog.domain.StatsWindow
import com.example.timelog.domain.StatsWindows
import com.example.timelog.util.DateTimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * 统计页的展示状态。
 *
 * [window] 与 [summary] 在首帧聚合完成前为 null；[label] 跟随窗口（跨年等规则见 [statsLabel]）。
 */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val mode: StatsMode = StatsMode.DAY,
    val window: StatsWindow? = null,
    val label: String = "",
    val canGoNext: Boolean = false,
    val customRange: Pair<LocalDate, LocalDate>? = null,
    val summary: StatsSummary? = null,
)

/**
 * 统计页 ViewModel：五种查看方式、前后翻页、自定义时间段。
 *
 * 事件与分类树都来自 [TimeLogRepository]，每分钟推进一次「现在」，
 * 让当前周期边界、「今天」前缀与记录条数口径保持新鲜。
 */
class StatisticsViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val selectedMode = MutableStateFlow(StatsMode.DAY)

    /** null 表示跟随「当前周期」（跨天后自动前移）；翻页后锁定到目标窗口的起始日。 */
    private val anchorDate = MutableStateFlow<LocalDate?>(null)

    private val selectedCustomRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)

    /** 每 60 秒推进一次「现在」；本轮订阅结束后由 stateIn 自动停止。 */
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(REFRESH_INTERVAL_MILLIS)
        }
    }

    private data class Controls(
        val mode: StatsMode,
        val anchor: LocalDate?,
        val customRange: Pair<LocalDate, LocalDate>?,
    )

    private val controls = combine(selectedMode, anchorDate, selectedCustomRange) { mode, anchor, customRange ->
        Controls(mode, anchor, customRange)
    }

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.timeline,
        repository.categoryTree,
        clock,
        controls,
    ) { events, tree, now, control ->
        val zone = DateTimeFormat.zone()
        val today = StatsWindows.dateOf(now, zone)
        val window = windowOf(control, today, zone)
        StatisticsUiState(
            isLoading = false,
            mode = window.mode,
            window = window,
            label = statsLabel(window, now, zone),
            canGoNext = StatsWindows.shift(window, 1, zone).startAt <= now,
            customRange = control.customRange,
            summary = StatsBuilder.build(events, tree, window, now, zone),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
        initialValue = StatisticsUiState(),
    )

    /**
     * 切换查看方式。
     *
     * 日/周/月/年回到「当前周期」（锚点清空，跨天后自动前移）；自定义默认最近 7 天（含今天）。
     */
    fun selectMode(mode: StatsMode) {
        val zone = DateTimeFormat.zone()
        val today = StatsWindows.dateOf(System.currentTimeMillis(), zone)
        when (mode) {
            StatsMode.CUSTOM -> {
                selectedMode.value = StatsMode.CUSTOM
                selectedCustomRange.value = today.minusDays(DEFAULT_CUSTOM_DAYS - 1) to today
                anchorDate.value = null
            }
            else -> {
                selectedMode.value = mode
                // 切换查看方式时回到当前周期，不沿用上一个模式的锚点。
                anchorDate.value = null
            }
        }
    }

    /** 上一个周期；自定义窗口按窗口长度平移。 */
    fun goPrevious() {
        val zone = DateTimeFormat.zone()
        val today = StatsWindows.dateOf(System.currentTimeMillis(), zone)
        applyWindow(StatsWindows.shift(currentWindow(zone, today), -1, zone), zone)
    }

    /** 下一个周期；目标窗口的开始时间还在未来时忽略，避免翻到空白的将来。 */
    fun goNext() {
        val zone = DateTimeFormat.zone()
        val now = System.currentTimeMillis()
        val today = StatsWindows.dateOf(now, zone)
        val target = StatsWindows.shift(currentWindow(zone, today), 1, zone)
        if (target.startAt > now) return
        applyWindow(target, zone)
    }

    /** 设置自定义时间段（闭区间，含首尾两天）；结束早于起始时自动交换。 */
    fun setCustomRange(start: LocalDate, end: LocalDate) {
        val first = if (end.isBefore(start)) end else start
        val last = if (end.isBefore(start)) start else end
        selectedMode.value = StatsMode.CUSTOM
        selectedCustomRange.value = first to last
        anchorDate.value = first
    }

    private fun currentWindow(zone: ZoneId, today: LocalDate): StatsWindow =
        windowOf(Controls(selectedMode.value, anchorDate.value, selectedCustomRange.value), today, zone)

    private fun windowOf(control: Controls, today: LocalDate, zone: ZoneId): StatsWindow =
        when (control.mode) {
            StatsMode.CUSTOM -> {
                val range = control.customRange ?: (today.minusDays(DEFAULT_CUSTOM_DAYS - 1) to today)
                StatsWindows.custom(range.first, range.second, zone)
            }
            else -> StatsWindows.of(control.mode, control.anchor ?: today, zone)
        }

    private fun applyWindow(window: StatsWindow, zone: ZoneId) {
        if (window.mode == StatsMode.CUSTOM) {
            val startDate = StatsWindows.dateOf(window.startAt, zone)
            val endDate = StatsWindows.dateOf(window.endAt - 1L, zone)
            selectedMode.value = StatsMode.CUSTOM
            selectedCustomRange.value = startDate to endDate
            anchorDate.value = startDate
        } else {
            selectedMode.value = window.mode
            anchorDate.value = StatsWindows.dateOf(window.startAt, zone)
        }
    }

    companion object {
        private const val REFRESH_INTERVAL_MILLIS = 60_000L
        private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
        private const val DEFAULT_CUSTOM_DAYS = 7L

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StatisticsViewModel(repository) as T
            }
    }
}

/** 范围文案里日期的本地化格式；声明在文件级，避免每次重组重建。 */
private val dayMonthWeekdayFormatter = DateTimeFormatter.ofPattern("M月d日 E", Locale.SIMPLIFIED_CHINESE)
private val fullDayMonthWeekdayFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 E", Locale.SIMPLIFIED_CHINESE)
private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy年M月", Locale.SIMPLIFIED_CHINESE)
private val yearFormatter = DateTimeFormatter.ofPattern("yyyy年", Locale.SIMPLIFIED_CHINESE)

/**
 * 范围导航条上的文案。
 *
 * - DAY：同年「11月1日 周日」，跨年「2025年11月1日 周日」，今天加「今天 · 」前缀
 * - WEEK / CUSTOM：同日历年「10月1日 – 10月15日」，跨年两端都带年份
 * - MONTH：「2026年11月」
 * - YEAR：「2026年」
 */
internal fun statsLabel(window: StatsWindow, now: Long, zone: ZoneId): String {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val startDate = Instant.ofEpochMilli(window.startAt).atZone(zone).toLocalDate()
    return when (window.mode) {
        StatsMode.DAY -> dayLabel(startDate, today)
        StatsMode.WEEK, StatsMode.CUSTOM -> {
            val endDate = Instant.ofEpochMilli(window.endAt - 1L).atZone(zone).toLocalDate()
            rangeLabel(startDate, endDate)
        }
        StatsMode.MONTH -> yearMonthFormatter.format(startDate)
        StatsMode.YEAR -> yearFormatter.format(startDate)
    }
}

private fun dayLabel(date: LocalDate, today: LocalDate): String {
    val text = if (date.year == today.year) {
        dayMonthWeekdayFormatter.format(date)
    } else {
        fullDayMonthWeekdayFormatter.format(date)
    }
    return if (date == today) "今天 · $text" else text
}

private fun rangeLabel(start: LocalDate, end: LocalDate): String =
    if (start.year == end.year) {
        "${start.monthValue}月${start.dayOfMonth}日 – ${end.monthValue}月${end.dayOfMonth}日"
    } else {
        "${start.year}年${start.monthValue}月${start.dayOfMonth}日 – " +
            "${end.year}年${end.monthValue}月${end.dayOfMonth}日"
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

/** 「完成」浮动按钮：用户指定蓝底圆圈 + 白色加号。 */
val ActionBlue = Color(0xFF2F6FE4)

/** 分类用时环比：增加为红、减少为绿（用户指定）。深色模式用同族的浅色版本。 */
val RiseRed = Color(0xFFC0392B)
val FallGreen = Color(0xFF2E7D32)
val RiseRedDark = Color(0xFFF08A80)
val FallGreenDark = Color(0xFF7FD1A3)

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

    fun zone(): ZoneId = ZoneId.systemDefault()

    fun toLocalDateTime(epochMillis: Long): LocalDateTime =
        Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalDateTime()

    fun toEpochMillis(date: LocalDate, time: LocalTime): Long =
        LocalDateTime.of(date, time).atZone(zone()).toInstant().toEpochMilli()

    fun time(epochMillis: Long): String = toLocalDateTime(epochMillis).format(timeFormatter)

    fun clock(time: LocalTime): String = time.format(timeFormatter)

    fun dateWithWeekday(date: LocalDate): String = date.format(monthDayFormatter)

    fun monthDay(date: LocalDate): String = date.format(shortMonthDayFormatter)

    /** 主界面日期标题：今天/昨天，其余只显示「M月d日」，不显示年份与星期。 */
    fun sectionTitle(date: LocalDate, today: LocalDate = LocalDate.now(zone())): String =
        when (date) {
            today -> "今天"
            today.minusDays(1) -> "昨天"
            else -> monthDay(date)
        }

    /** 主界面日期副标题：今天/昨天补上日期，其余只留「周X」小字。 */
    fun sectionSubtitle(date: LocalDate, today: LocalDate = LocalDate.now(zone())): String =
        when (date) {
            today, today.minusDays(1) -> monthDay(date) + " · " + weekdayLabel(date)
            else -> weekdayLabel(date)
        }

    /** 固定输出「周日」「周一」…，不依赖不同 JDK 的 CLDR 短星期文本。 */
    private fun weekdayLabel(date: LocalDate): String =
        "周" + date.dayOfWeek.getDisplayName(TextStyle.NARROW, zh)

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
    <string name="app_name">時</string>
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

## `app/src/test/java/com/example/timelog/domain/OverlapResolverTest.kt`

```kotlin
package com.example.timelog.domain

import com.example.timelog.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlapResolverTest {

    private val hour = 3_600_000L
    private val base = 1_700_000_000_000L

    private fun at(hours: Long): Long = base + hours * hour

    private fun event(
        id: Long,
        startHour: Long,
        endHour: Long,
        note: String? = null,
        categoryId: Long = 7L,
    ): Event = Event(
        id = id,
        note = note,
        startAt = at(startHour),
        endAt = at(endHour),
        categoryId = categoryId,
        createdAt = 1_000L + id,
        updatedAt = 2_000L + id,
    )

    @Test
    fun noOverlapLeavesEverythingUntouched() {
        val plan = resolveOverlap(at(10), at(11), listOf(event(1, 8, 9), event(2, 12, 13)))

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun emptyOthersProduceEmptyPlan() {
        val plan = resolveOverlap(at(10), at(11), emptyList())

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun touchingIntervalsAreNotOverlap() {
        // 老记录 ends exactly at 目标 start.
        val before = resolveOverlap(at(10), at(11), listOf(event(1, 9, 10)))
        // 老记录 starts exactly at 目标 end.
        val after = resolveOverlap(at(10), at(11), listOf(event(2, 11, 12)))

        assertTrue(before.updates.isEmpty() && before.clones.isEmpty() && before.victims.isEmpty())
        assertTrue(after.updates.isEmpty() && after.clones.isEmpty() && after.victims.isEmpty())
    }

    @Test
    fun eventFullyInsideTargetIsVictim() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 10, 11, note = "写周报")))

        assertEquals(listOf(1L), plan.victims.map { it.id })
        assertEquals("写周报", plan.victims.single().note)
        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
    }

    @Test
    fun identicalIntervalIsVictim() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 10, 12)))

        assertEquals(listOf(1L), plan.victims.map { it.id })
        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
    }

    @Test
    fun eventStickingOutLeftIsTrimmedToTargetStart() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 9, 11)))

        val update = plan.updates.single()
        assertEquals(1L, update.event.id)
        assertEquals(at(9), update.startAt)
        assertEquals(at(10), update.endAt)
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun eventStickingOutRightIsTrimmedToTargetEnd() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 11, 13)))

        val update = plan.updates.single()
        assertEquals(1L, update.event.id)
        assertEquals(at(12), update.startAt)
        assertEquals(at(13), update.endAt)
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun eventCoveringBothSidesKeepsLeftAndClonesRight() {
        val plan = resolveOverlap(at(10), at(12), listOf(event(1, 9, 13, note = "值班", categoryId = 3L)))

        val update = plan.updates.single()
        assertEquals(at(9), update.startAt)
        assertEquals(at(10), update.endAt)

        val clone = plan.clones.single()
        assertEquals("值班", clone.note)
        assertEquals(at(12), clone.startAt)
        assertEquals(at(13), clone.endAt)
        assertEquals(3L, clone.categoryId)
        assertEquals(1_001L, clone.createdAt)
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun coveringTargetWithMatchingEdgesProducesSingleTrim() {
        // 老记录与目标同起点，只向右超出：右侧残余非空，左侧残余为零宽被丢弃。
        val right = resolveOverlap(at(10), at(12), listOf(event(1, 10, 13)))
        assertEquals(1, right.updates.size)
        assertEquals(at(12), right.updates.single().startAt)
        assertEquals(at(13), right.updates.single().endAt)
        assertTrue(right.clones.isEmpty())

        // 老记录与目标同终点，只向左超出：左侧残余非空，右侧残余为零宽被丢弃。
        val left = resolveOverlap(at(10), at(12), listOf(event(2, 9, 12)))
        assertEquals(1, left.updates.size)
        assertEquals(at(9), left.updates.single().startAt)
        assertEquals(at(10), left.updates.single().endAt)
        assertTrue(left.clones.isEmpty())
    }

    @Test
    fun mixedOthersProduceMixedPlanInInputOrder() {
        val plan = resolveOverlap(
            at(10),
            at(12),
            listOf(
                event(1, 9, 11),  // 左侧超出：裁掉尾部
                event(2, 10, 11), // 整段被盖住：victim
                event(3, 11, 13), // 右侧超出：裁掉头部
                event(4, 9, 13),  // 两侧都超出：更新 + 复制
                event(5, 13, 14), // 没有交集：不动
            ),
        )

        assertEquals(listOf(1L, 3L, 4L), plan.updates.map { it.event.id })
        assertEquals(1, plan.clones.size)
        assertEquals(listOf(2L), plan.victims.map { it.id })
    }

    @Test
    fun zeroWidthAndInvalidEventsAreIgnored() {
        val plan = resolveOverlap(
            at(10),
            at(12),
            listOf(
                event(1, 11, 11), // 零宽
                event(2, 13, 12), // 负宽
            ),
        )

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }

    @Test
    fun invalidTargetProducesEmptyPlan() {
        val plan = resolveOverlap(at(12), at(10), listOf(event(1, 10, 11)))

        assertTrue(plan.updates.isEmpty())
        assertTrue(plan.clones.isEmpty())
        assertTrue(plan.victims.isEmpty())
    }
}
```

## `app/src/test/java/com/example/timelog/domain/StatisticsTest.kt`

```kotlin
package com.example.timelog.domain

import com.example.timelog.domain.model.Category
import com.example.timelog.domain.model.CategoryTree
import com.example.timelog.domain.model.Event
import com.example.timelog.domain.model.TimelineEvent
import com.example.timelog.domain.model.buildCategoryTree
import com.example.timelog.ui.stats.statsLabel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 统计领域层纯逻辑测试：窗口边界 / 翻页 / 裁剪 / 层级汇总 / 环比 / 孤立分类 / 范围文案。
 *
 * 所有时间都通过参数注入（zone = Asia/Shanghai，now = 2026-11-01T12:00，今天是周日）。
 */
class StatisticsTest {

    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val nowMillis: Long = at("2026-11-01T12:00")
    private val today: LocalDate = LocalDate.of(2026, 11, 1)

    // ---------- 窗口边界 ----------

    @Test
    fun dayWindowCoversWholeDay() {
        val window = StatsWindows.of(StatsMode.DAY, today, zone)
        assertEquals(StatsMode.DAY, window.mode)
        assertEquals(at("2026-11-01T00:00"), window.startAt)
        assertEquals(at("2026-11-02T00:00"), window.endAt)
    }

    @Test
    fun weekWindowStartsOnMonday() {
        val window = StatsWindows.of(StatsMode.WEEK, today, zone)
        assertEquals(StatsMode.WEEK, window.mode)
        assertEquals(at("2026-10-26T00:00"), window.startAt)
        assertEquals(at("2026-11-02T00:00"), window.endAt)
    }

    @Test
    fun weekWindowUsesMondayForMidweekAnchor() {
        val window = StatsWindows.of(StatsMode.WEEK, LocalDate.of(2026, 10, 28), zone)
        assertEquals(at("2026-10-26T00:00"), window.startAt)
        assertEquals(at("2026-11-02T00:00"), window.endAt)
    }

    @Test
    fun monthWindowCoversWholeMonth() {
        val window = StatsWindows.of(StatsMode.MONTH, LocalDate.of(2026, 11, 15), zone)
        assertEquals(at("2026-11-01T00:00"), window.startAt)
        assertEquals(at("2026-12-01T00:00"), window.endAt)
    }

    @Test
    fun yearWindowCoversWholeYear() {
        val window = StatsWindows.of(StatsMode.YEAR, LocalDate.of(2026, 4, 5), zone)
        assertEquals(at("2026-01-01T00:00"), window.startAt)
        assertEquals(at("2027-01-01T00:00"), window.endAt)
    }

    @Test
    fun customWindowIncludesEndDate() {
        val window = StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), zone)
        assertEquals(StatsMode.CUSTOM, window.mode)
        assertEquals(at("2026-10-01T00:00"), window.startAt)
        assertEquals(at("2026-10-16T00:00"), window.endAt)
    }

    @Test
    fun customWindowSwapsReversedDatesAndSupportsSingleDay() {
        assertEquals(
            StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), zone),
            StatsWindows.custom(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1), zone),
        )
        val singleDay = StatsWindows.custom(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1), zone)
        assertEquals(at("2026-11-01T00:00"), singleDay.startAt)
        assertEquals(at("2026-11-02T00:00"), singleDay.endAt)
    }

    @Test
    fun shiftMovesEachPeriodByOne() {
        assertEquals(
            at("2026-10-31T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.DAY, today, zone), -1, zone).startAt,
        )
        assertEquals(
            at("2026-11-02T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.WEEK, today, zone), 1, zone).startAt,
        )
        assertEquals(
            at("2025-12-01T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.MONTH, LocalDate.of(2026, 1, 15), zone), -1, zone).startAt,
        )
        assertEquals(
            at("2027-01-01T00:00"),
            StatsWindows.shift(StatsWindows.of(StatsMode.YEAR, LocalDate.of(2026, 4, 5), zone), 1, zone).startAt,
        )
        val day = StatsWindows.of(StatsMode.DAY, today, zone)
        assertEquals(day, StatsWindows.shift(day, 0, zone))
    }

    @Test
    fun shiftKeepsCustomWindowLength() {
        val window = StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7), zone)
        val next = StatsWindows.shift(window, 1, zone)
        assertEquals(at("2026-10-08T00:00"), next.startAt)
        assertEquals(at("2026-10-15T00:00"), next.endAt)
        val previous = StatsWindows.shift(window, -1, zone)
        assertEquals(at("2026-09-24T00:00"), previous.startAt)
        assertEquals(at("2026-10-01T00:00"), previous.endAt)
    }

    // ---------- 聚合 ----------

    @Test
    fun emptyWindowHasNoUsages() {
        val summary = build()
        assertEquals(0L, summary.totalMillis)
        assertEquals(0, summary.eventCount)
        assertTrue(summary.roots.isEmpty())
        assertEquals(0L, summary.previousTotalMillis)
    }

    @Test
    fun clipsEventsToWindowAndNow() {
        val summary = build(
            event(1, "2026-10-31T23:00", "2026-11-01T01:00"),
            event(2, "2026-11-01T11:00", "2026-11-01T13:00"),
        )
        // 第一条只算进入窗口的 1 小时，第二条被 now(12:00) 截断，同样只剩 1 小时。
        assertEquals(2 * HOUR, summary.totalMillis)
        assertEquals(2, summary.eventCount)
        val root = summary.roots.single()
        assertEquals(2 * HOUR, root.millis)
        assertEquals(2, root.eventCount)
    }

    @Test
    fun overlappingEventsCountIndividually() {
        val summary = build(
            event(1, "2026-11-01T08:00", "2026-11-01T10:00"),
            event(2, "2026-11-01T09:00", "2026-11-01T11:00"),
        )
        assertEquals(4 * HOUR, summary.totalMillis)
        assertEquals(2, summary.eventCount)
        assertEquals(4 * HOUR, summary.roots.single().millis)
    }

    @Test
    fun ignoresEventsOutsideWindow() {
        val summary = build(event(1, "2026-10-01T08:00", "2026-10-01T09:00"))
        assertEquals(0L, summary.totalMillis)
        assertTrue(summary.roots.isEmpty())
    }

    @Test
    fun rollsSubtreeDurationAndCountIntoRoot() {
        val summary = build(
            event(1, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 3L, path = "工作 / 执行 / 编码"),
            event(2, "2026-11-01T10:00", "2026-11-01T10:30", categoryId = 6L, path = "工作 / 执行 / 编码 / 深潜"),
            event(3, "2026-11-01T10:30", "2026-11-01T10:45", categoryId = 1L, path = "工作"),
        )
        val root = summary.roots.single()
        assertEquals(1L, root.categoryId)
        assertEquals(105 * MINUTE, root.millis)
        assertEquals(3, root.eventCount)
        assertEquals(0, root.depth)

        val second = root.children.single()
        assertEquals(2L, second.categoryId)
        assertEquals(90 * MINUTE, second.millis)
        assertEquals(2, second.eventCount)
        assertEquals(1, second.depth)

        // 第三层保留，但更深的分类不再展开（时长仍计入 [third] 与祖先）。
        val third = second.children.single()
        assertEquals(3L, third.categoryId)
        assertEquals(90 * MINUTE, third.millis)
        assertEquals(2, third.eventCount)
        assertEquals(2, third.depth)
        assertTrue(third.children.isEmpty())
    }

    @Test
    fun sortsRootsByDurationThenName() {
        val busy = build(
            event(1, "2026-11-01T08:00", "2026-11-01T10:00", categoryId = 4L, path = "生活"),
            event(2, "2026-11-01T10:00", "2026-11-01T11:00", categoryId = 1L, path = "工作"),
        )
        assertEquals(listOf(4L, 1L), busy.roots.map { it.categoryId })

        val tied = build(
            event(1, "2026-11-01T08:00", "2026-11-01T09:00", categoryId = 1L, path = "工作"),
            event(2, "2026-11-01T09:00", "2026-11-01T10:00", categoryId = 4L, path = "生活"),
        )
        assertEquals(listOf("工作", "生活"), tied.roots.map { it.name })
    }

    @Test
    fun synthesizesUnknownCategoryAsLastRoot() {
        val summary = build(
            event(1, "2026-11-01T07:00", "2026-11-01T10:00", categoryId = 99L, path = "阅读 / 文学", color = 0xFFFF0000L),
            event(2, "2026-11-01T10:00", "2026-11-01T12:00", categoryId = 1L, path = "工作"),
        )
        assertEquals(2, summary.roots.size)
        assertEquals(1L, summary.roots[0].categoryId)
        val orphan = summary.roots[1]
        assertEquals(StatsBuilder.ORPHAN_CATEGORY_ID, orphan.categoryId)
        assertEquals("阅读", orphan.name)
        assertEquals(3 * HOUR, orphan.millis)
        assertEquals(1, orphan.eventCount)
        assertEquals(0xFFFF0000L, orphan.colorHex)
        assertEquals(0, orphan.depth)
    }

    @Test
    fun mergesUnknownCategoriesWithSameRootName() {
        val summary = build(
            event(1, "2026-11-01T08:00", "2026-11-01T08:30", categoryId = 98L, path = "阅读 / A", color = 0xFF112233L),
            event(2, "2026-11-01T08:30", "2026-11-01T09:15", categoryId = 99L, path = "阅读 / B", color = 0xFF445566L),
        )
        val orphan = summary.roots.single()
        assertEquals("阅读", orphan.name)
        assertEquals(75 * MINUTE, orphan.millis)
        assertEquals(2, orphan.eventCount)
        assertEquals(0xFF112233L, orphan.colorHex)
    }

    @Test
    fun blankCategoryPathFallsBackToUnknownName() {
        val summary = build(event(1, "2026-11-01T08:00", "2026-11-01T09:00", categoryId = 99L, path = ""))
        assertEquals("未分类", summary.roots.single().name)
    }

    // ---------- 环比 ----------

    @Test
    fun previousWindowAlignsElapsedTime() {
        val summary = build(
            event(1, "2026-11-01T10:00", "2026-11-01T11:00"),
            event(2, "2026-10-31T03:00", "2026-10-31T04:00"),
            // now = 12:00，上一窗口只对齐到 10-31 12:00，下面这条不进入环比。
            event(3, "2026-10-31T18:00", "2026-10-31T19:00"),
        )
        assertEquals(HOUR, summary.totalMillis)
        assertEquals(HOUR, summary.previousTotalMillis)
        assertEquals(HOUR, summary.roots.single().previousMillis)
    }

    @Test
    fun previousWindowUsesFullPeriodWhenWindowIsInPast() {
        val window = StatsWindows.of(StatsMode.DAY, LocalDate.of(2026, 10, 25), zone)
        val summary = build(
            event(1, "2026-10-25T09:00", "2026-10-25T10:00"),
            event(2, "2026-10-24T20:00", "2026-10-24T21:00"),
            window = window,
        )
        assertEquals(HOUR, summary.roots.single().previousMillis)
        assertEquals(HOUR, summary.previousTotalMillis)
    }

    @Test
    fun previousMillisIsZeroForNewCategory() {
        val summary = build(event(1, "2026-11-01T10:00", "2026-11-01T11:00"))
        assertEquals(0L, summary.roots.single().previousMillis)
        assertEquals(0L, summary.previousTotalMillis)
        assertNull(summary.roots.single().deltaRatio)
    }

    @Test
    fun deltaRatioReflectsChange() {
        val increase = build(
            event(1, "2026-11-01T08:00", "2026-11-01T10:00"),
            event(2, "2026-10-31T03:00", "2026-10-31T04:00"),
        )
        assertEquals(1f, increase.roots.single().deltaRatio!!, 0.0001f)

        val decrease = build(
            event(1, "2026-11-01T08:00", "2026-11-01T09:00"),
            event(2, "2026-10-31T03:00", "2026-10-31T05:00"),
        )
        assertEquals(-0.5f, decrease.roots.single().deltaRatio!!, 0.0001f)
    }

    // ---------- 范围文案 ----------

    @Test
    fun dayLabelPrefixesTodayAndAddsYearWhenNeeded() {
        assertEquals(
            "今天 · 11月1日 周日",
            statsLabel(StatsWindows.of(StatsMode.DAY, today, zone), nowMillis, zone),
        )
        assertEquals(
            "10月31日 周六",
            statsLabel(StatsWindows.of(StatsMode.DAY, LocalDate.of(2026, 10, 31), zone), nowMillis, zone),
        )
        assertEquals(
            "2025年11月1日 周六",
            statsLabel(StatsWindows.of(StatsMode.DAY, LocalDate.of(2025, 11, 1), zone), nowMillis, zone),
        )
    }

    @Test
    fun weekLabelUsesEnDashAndAddsYearsAcrossNewYear() {
        assertEquals(
            "10月26日 – 11月1日",
            statsLabel(StatsWindows.of(StatsMode.WEEK, today, zone), nowMillis, zone),
        )
        assertEquals(
            "2025年12月29日 – 2026年1月4日",
            statsLabel(StatsWindows.of(StatsMode.WEEK, LocalDate.of(2026, 1, 1), zone), nowMillis, zone),
        )
    }

    @Test
    fun monthAndYearLabels() {
        assertEquals(
            "2026年11月",
            statsLabel(StatsWindows.of(StatsMode.MONTH, today, zone), nowMillis, zone),
        )
        assertEquals(
            "2026年",
            statsLabel(StatsWindows.of(StatsMode.YEAR, today, zone), nowMillis, zone),
        )
    }

    @Test
    fun customRangeLabelAddsYearsAcrossNewYear() {
        assertEquals(
            "10月1日 – 10月15日",
            statsLabel(StatsWindows.custom(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), zone), nowMillis, zone),
        )
        assertEquals(
            "2025年12月30日 – 2026年1月2日",
            statsLabel(StatsWindows.custom(LocalDate.of(2025, 12, 30), LocalDate.of(2026, 1, 2), zone), nowMillis, zone),
        )
    }

    // ---------- 夹具 ----------

    private fun at(value: String): Long =
        LocalDateTime.parse(value).atZone(zone).toInstant().toEpochMilli()

    private fun event(
        id: Long,
        start: String,
        end: String,
        categoryId: Long = 1L,
        path: String = "工作",
        color: Long = WORK_COLOR,
    ): TimelineEvent {
        val startAt = at(start)
        return TimelineEvent(
            event = Event(
                id = id,
                note = null,
                startAt = startAt,
                endAt = at(end),
                categoryId = categoryId,
                createdAt = startAt,
                updatedAt = startAt,
            ),
            categoryPath = path,
            categoryColor = color,
        )
    }

    private fun build(
        vararg events: TimelineEvent,
        window: StatsWindow = StatsWindows.of(StatsMode.DAY, today, zone),
        now: Long = nowMillis,
        tree: CategoryTree = tree(),
    ): StatsSummary = StatsBuilder.build(events.toList(), tree, window, now, zone)

    /** 工作(1) / 执行(2) / 编码(3) / 深潜(6)，生活(4) / 运动(5)。 */
    private fun tree(): CategoryTree = buildCategoryTree(
        listOf(
            category(1L, "工作", null, WORK_COLOR),
            category(2L, "执行", 1L, 0xFF4C7BE8),
            category(3L, "编码", 2L, 0xFF2F6FE4),
            category(6L, "深潜", 3L, 0xFF3F51B5),
            category(4L, "生活", null, 0xFF8B5CF6),
            category(5L, "运动", 4L, 0xFF8B5CF6),
        ),
    )

    private fun category(id: Long, name: String, parentId: Long?, colorHex: Long): Category =
        Category(
            id = id,
            name = name,
            parentId = parentId,
            sortOrder = id.toInt(),
            colorHex = colorHex,
        )

    private companion object {
        private const val MINUTE = 60_000L
        private const val HOUR = 60 * MINUTE
        private const val WORK_COLOR = 0xFF4C7BE8L
    }
}
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

**Selected: A · 墨時.** The Android launcher foreground is generated by `build_launcher_assets.py`
(paper 時 on transparent 108 dp canvas, glyph height 43 dp) into
`app/src/main/res/drawable-*dpi/ic_launcher_foreground.png`; the background comes from
`ic_launcher_background` (#2F5D50). The other four directions stay here as design record.

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

## `design/icons/build_launcher_assets.py`

```python
"""从 design/icons/icon-A.png 生成 Android 自适应图标前景（透明背景 + 纸白「時」）。

用法（仓库根目录）：
    python design/icons/build_launcher_assets.py

前景画布为 108dp（自适应图标标准），字形高度按母版比例取 43dp（约可见圆形直径 72dp 的 59%），
背景由 res/values/colors.xml 的 ic_launcher_background (#2F5D50) 提供。
"""

import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "design", "icons", "icon-A.png")
RES = os.path.join(ROOT, "app", "src", "main", "res")

PINE = (0x2F, 0x5D, 0x50)
PAPER = (0xFB, 0xFA, 0xF7)
GLYPH_DP = 43.0       # 字形高度（dp）
CANVAS_DP = 108.0     # 自适应图标前景画布
SUPERSAMPLE = 8       # 母版渲染倍数

DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}


def glyph_layer() -> Image.Image:
    im = Image.open(SRC).convert("RGB")
    w, h = im.size
    px = im.load()
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    op = out.load()
    pine_lum = 0.2126 * PINE[0] + 0.7152 * PINE[1] + 0.0722 * PINE[2]
    paper_lum = 0.2126 * PAPER[0] + 0.7152 * PAPER[1] + 0.0722 * PAPER[2]
    for y in range(h):
        for x in range(w):
            r, g, b = px[x, y]
            lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
            alpha = (lum - pine_lum) / (paper_lum - pine_lum)
            alpha = max(0.0, min(1.0, alpha))
            if alpha > 0.0:
                op[x, y] = PAPER + (int(round(alpha * 255)),)
    return out


def main() -> None:
    glyph = glyph_layer()
    bbox = glyph.getbbox()
    glyph = glyph.crop(bbox)
    gw, gh = glyph.size

    canvas_px = int(round(CANVAS_DP * SUPERSAMPLE))
    glyph_h_px = int(round(GLYPH_DP * SUPERSAMPLE))
    glyph_w_px = int(round(glyph_h_px * gw / gh))
    master = glyph.resize((glyph_w_px, glyph_h_px), Image.LANCZOS)

    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    canvas.paste(master, ((canvas_px - glyph_w_px) // 2, (canvas_px - glyph_h_px) // 2), master)

    for name, scale in DENSITIES.items():
        size = int(round(CANVAS_DP * scale))
        out_dir = os.path.join(RES, "drawable-" + name)
        os.makedirs(out_dir, exist_ok=True)
        img = canvas.resize((size, size), Image.LANCZOS)
        img.save(os.path.join(out_dir, "ic_launcher_foreground.png"))
        print("wrote", os.path.join("drawable-" + name, "ic_launcher_foreground.png"), size)

    # 预览：108dp 画布 + 72dp 圆形遮罩，浅/深两种桌面底色
    preview = Image.new("RGB", (760, 320), (0xE9, 0xE7, 0xE2))
    draw = ImageDraw.Draw(preview)
    for i, bg in enumerate([(0xE9, 0xE7, 0xE2), (0x17, 0x1A, 0x18)]):
        block = Image.new("RGB", (380, 320), bg)
        icon_bg = Image.new("RGBA", (288, 288), PINE + (255,))
        icon = Image.alpha_composite(icon_bg, canvas.resize((288, 288), Image.LANCZOS))
        mask = Image.new("L", (288, 288), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, 287, 287), fill=255)
        block.paste(icon, (46, 16), mask)
        preview.paste(block, (i * 380, 0))
    preview.save(os.path.join(ROOT, ".tools", "icon-preview.png"))
    print("wrote .tools/icon-preview.png")


if __name__ == "__main__":
    main()
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
