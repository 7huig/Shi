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
