package io.github.huig7.shi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.ui.category.CategoryManageScreen
import io.github.huig7.shi.ui.category.CategoryManageViewModel
import io.github.huig7.shi.ui.data.DataBackupScreen
import io.github.huig7.shi.ui.data.DataTransferSection
import io.github.huig7.shi.ui.data.DataTransferViewModel
import io.github.huig7.shi.ui.home.HomeUiEvent
import io.github.huig7.shi.ui.home.HomeViewModel
import io.github.huig7.shi.ui.home.TimelineFabOverlay
import io.github.huig7.shi.ui.home.TimelinePage
import io.github.huig7.shi.ui.record.QuickCategory
import io.github.huig7.shi.ui.record.QuickRecordResult
import io.github.huig7.shi.ui.record.QuickRecordSheet
import io.github.huig7.shi.ui.record.RecordSheet
import io.github.huig7.shi.ui.record.RecordViewModel
import io.github.huig7.shi.ui.search.SearchScreen
import io.github.huig7.shi.ui.search.SearchViewModel
import io.github.huig7.shi.ui.stats.StatisticsScreen
import io.github.huig7.shi.ui.stats.StatisticsViewModel
import io.github.huig7.shi.ui.theme.TimeLogTheme
import io.github.huig7.shi.util.DateTimeFormat
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

private const val OVERLAY_SEARCH = "search"
private const val OVERLAY_CATEGORIES = "categories"
private const val OVERLAY_DATA = "data"

private const val PREFS_UI = "shi_ui"
private const val KEY_ONBOARDED = "onboarded_v1"

/**
 * 应用外壳：紧凑顶栏 + 左右滑动的主界面/统计页。
 * 时间轴页的「+」单击新建、长按快速记录；搜索、分类、数据备份是整屏浮层。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeLogApp(repository: TimeLogRepository) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE) }
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
    val snackbarHostState = remember { SnackbarHostState() }
    var overlay by rememberSaveable { mutableStateOf<String?>(null) }
    var showQuickRecord by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showOnboarding by rememberSaveable {
        mutableStateOf(!prefs.getBoolean(KEY_ONBOARDED, false))
    }
    val editorTarget = homeState.editor
    val currentPage = pagerState.currentPage
    val closeEditor = {
        homeViewModel.closeRecord()
        recordViewModel.reset()
    }

    LaunchedEffect(editorTarget) {
        editorTarget?.let(recordViewModel::start)
    }

    LaunchedEffect(recordState.saved) {
        if (recordState.saved) {
            closeEditor()
        }
    }

    // 从统计页返回时先回时间轴，而不是直接退出应用。
    BackHandler(
        enabled = overlay == null && !showQuickRecord && currentPage != PAGE_TIMELINE,
    ) {
        scope.launch { pagerState.animateScrollToPage(PAGE_TIMELINE) }
    }

    // 删除 / 撤销 / 失败提示统一走 Snackbar。
    LaunchedEffect(Unit) {
        homeViewModel.uiEvents.collect { event ->
            when (event) {
                is HomeUiEvent.Deleted -> {
                    val label = "已删除 " + event.event.categoryPath + " · " +
                        DateTimeFormat.durationText(
                            event.event.event.startAt,
                            event.event.event.endAt,
                        )
                    val result = snackbarHostState.showSnackbar(
                        message = label,
                        actionLabel = "撤销",
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        homeViewModel.undoDelete(event.event)
                    }
                }

                is HomeUiEvent.Restored -> {
                    snackbarHostState.showSnackbar("已恢复这条记录")
                }

                is HomeUiEvent.Message -> {
                    snackbarHostState.showSnackbar(event.text)
                }
            }
        }
    }

    // 快速记录结果：成功可撤销；重叠时不静默覆盖，打开完整表单确认。
    LaunchedEffect(Unit) {
        recordViewModel.quickRecordResults.collect { result ->
            when (result) {
                is QuickRecordResult.Saved -> {
                    val outcome = snackbarHostState.showSnackbar(
                        message = "已记录 ${result.minutes} 分钟",
                        actionLabel = "撤销",
                        duration = SnackbarDuration.Long,
                    )
                    if (outcome == SnackbarResult.ActionPerformed) {
                        homeViewModel.deleteSilently(result.eventId)
                    }
                }

                is QuickRecordResult.NeedsReview -> {
                    homeViewModel.openNewRecordAt(
                        startAt = result.startAt,
                        endAt = result.endAt,
                        categoryId = result.categoryId,
                        needsReview = true,
                    )
                }

                is QuickRecordResult.Failed -> {
                    snackbarHostState.showSnackbar(result.message)
                }
            }
        }
    }

    // 最近使用的分类优先；没有历史记录时用默认叶子分类补齐，保证第一次也能快速记录。
    val quickCategories = remember(homeState.events, categoryState.tree) {
        val recent = homeState.events.asReversed()
            .distinctBy { it.event.categoryId }
            .take(5)
            .map {
                QuickCategory(
                    id = it.event.categoryId,
                    name = it.categoryPath.substringAfterLast(" / "),
                    colorHex = it.categoryColor,
                )
            }
        val defaults = categoryState.tree.flatten()
            .filter { (node, _) -> node.children.isEmpty() }
            .take(5)
            .map { (node, _) ->
                QuickCategory(
                    id = node.category.id,
                    name = node.category.name,
                    colorHex = node.category.colorHex,
                )
            }
        (recent + defaults).distinctBy { it.id }.take(5)
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
                onOpenBackup = { overlay = OVERLAY_DATA },
                onOpenAbout = { showAbout = true },
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
                        onFillGap = homeViewModel::openFillGap,
                        onNewRecord = homeViewModel::openNewRecord,
                        snackbarHostState = snackbarHostState,
                    )

                    else -> StatisticsScreen(
                        state = statisticsState,
                        onModeSelected = statisticsViewModel::selectMode,
                        onPrevious = statisticsViewModel::goPrevious,
                        onNext = statisticsViewModel::goNext,
                        onCustomRangeSelected = statisticsViewModel::setCustomRange,
                        onCategoryDrilled = statisticsViewModel::drillInto,
                        onDrillUp = statisticsViewModel::drillUp,
                        onCrumbSelected = statisticsViewModel::focusAt,
                        onToday = statisticsViewModel::goToday,
                        footer = { DataTransferSection(viewModel = transferViewModel) },
                    )
                }
            }
        }

        // 浮动「+」入口：叠在内容之上，不占用列表高度；只在時記页显示。
        if (currentPage == PAGE_TIMELINE) {
            TimelineFabOverlay(
                onNewRecord = homeViewModel::openNewRecord,
                onQuickRecord = { showQuickRecord = true },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp)
                .padding(bottom = 92.dp)
                .navigationBarsPadding(),
        )

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
                onDeleteMoveEvents = categoryViewModel::confirmDeleteMovingEvents,
                onMove = categoryViewModel::moveCategory,
                onToggleExpanded = categoryViewModel::toggleExpanded,
                onExpandAll = { categoryViewModel.expandAll() },
                onCollapseAll = { categoryViewModel.collapseAll() },
                onMessageShown = categoryViewModel::consumeMessage,
            )
        }

        if (overlay == OVERLAY_DATA) {
            DataBackupScreen(
                viewModel = transferViewModel,
                onBack = { overlay = null },
            )
        }
    }

    if (editorTarget != null) {
        RecordSheet(
            state = recordState,
            onDismiss = closeEditor,
            onNoteChange = recordViewModel::onNoteChange,
            onCategorySelected = recordViewModel::onCategorySelected,
            onStartDateSelected = recordViewModel::onStartDateSelected,
            onStartTimeSelected = recordViewModel::onStartTimeSelected,
            onEndDateSelected = recordViewModel::onEndDateSelected,
            onEndTimeSelected = recordViewModel::onEndTimeSelected,
            onFillWholeGap = recordViewModel::fillWholeGap,
            onSave = recordViewModel::save,
            onDelete = {
                recordState.eventId?.let { eventId ->
                    homeViewModel.deleteEvent(eventId)
                    closeEditor()
                }
            },
            onConfirmOverwrite = recordViewModel::confirmOverwrite,
            onCancelOverwrite = recordViewModel::dismissOverwrite,
        )
    }

    if (showQuickRecord) {
        QuickRecordSheet(
            categories = quickCategories,
            defaultCategoryId = homeState.events.lastOrNull()?.event?.categoryId,
            onDismiss = { showQuickRecord = false },
            onSave = { categoryId, minutes ->
                showQuickRecord = false
                recordViewModel.quickSave(categoryId, minutes)
            },
            onOpenFullForm = { categoryId, minutes ->
                showQuickRecord = false
                val endAt = System.currentTimeMillis()
                homeViewModel.openNewRecordAt(
                    startAt = endAt - minutes * 60_000L,
                    endAt = endAt,
                    categoryId = categoryId,
                )
            },
        )
    }

    if (showAbout) {
        AboutDialog(
            version = appVersion(context),
            onDismiss = { showAbout = false },
        )
    }

    if (showOnboarding) {
        OnboardingDialog(
            onDone = {
                prefs.edit { putBoolean(KEY_ONBOARDED, true) }
                showOnboarding = false
            },
        )
    }
}
