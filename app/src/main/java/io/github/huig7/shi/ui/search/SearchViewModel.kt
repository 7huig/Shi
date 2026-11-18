package io.github.huig7.shi.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.TimelineEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** 分类筛选；null 表示全部分类，否则包含所选分类的整棵子树。 */
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    /** 筛选弹窗需要的分类树；与数据库保持同步，分类改名 / 删除后立即反映。 */
    val categoryTree: StateFlow<CategoryTree> = repository.categoryTree.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryTree(),
    )

    /** 输入停顿 250ms 后再查询，避免每敲一个字都打一次数据库；只选分类时立即刷新。 */
    val results: StateFlow<List<TimelineEvent>> = combine(
        _query.debounce(SEARCH_DEBOUNCE_MILLIS),
        _selectedCategoryId,
    ) { query, categoryId ->
        query.trim() to categoryId
    }
        .flatMapLatest { (query, categoryId) ->
            repository.searchEvents(query, categoryId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onCategorySelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MILLIS = 250L

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { SearchViewModel(repository) }
        }
    }
}
