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
import com.example.timelog.ui.home.RecordBar
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
 * 应用外壳：紧凑顶栏 + 左右滑动的主界面/统计页 + 时间轴页底部的「完成」。
 * 搜索与分类是整屏浮层（覆盖外壳），记录编辑仍是底部弹窗。
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
                    .then(if (currentPage == PAGE_STATS) Modifier.navigationBarsPadding() else Modifier),
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
                        onPeriodSelected = statisticsViewModel::selectPeriod,
                        footer = { DataTransferSection(viewModel = transferViewModel) },
                    )
                }
            }

            if (currentPage == PAGE_TIMELINE) {
                RecordBar(onNewRecord = homeViewModel::openNewRecord)
            }
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

/** 紧凑顶栏：不再用大号 30sp 标题，两个页签本身就是切换入口，左侧留 24dp 与时间轴对齐。 */
@Composable
private fun AppHeader(
    page: Int,
    onSelectPage: (Int) -> Unit,
    onSearch: () -> Unit,
    onOpenCategories: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 22.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "時計",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 23.sp,
                lineHeight = 28.sp,
            ),
            color = if (page == PAGE_TIMELINE) {
                MaterialTheme.colorScheme.onBackground
            } else {
                muted
            },
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelectPage(PAGE_TIMELINE) }
                .padding(horizontal = 2.dp, vertical = 2.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = "统计",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp, lineHeight = 24.sp),
            color = if (page == PAGE_STATS) accent else muted,
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
