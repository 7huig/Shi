package io.github.huig7.shi.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.huig7.shi.data.repository.CategoryDeletionResult
import io.github.huig7.shi.data.repository.TimeLogRepository
import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.SolarTermColors
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
    /** 已展开的分类 id；默认空 = 全部收起，只看到根分类。 */
    val expandedIds: Set<Long> = emptySet(),
    /** 当前是否所有「有子分类」的节点都已展开，用于顶栏「全部展开 / 全部收起」。 */
    val allExpanded: Boolean = false,
)

class CategoryManageViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private data class Transient(
        val editor: CategoryEditor? = null,
        val pendingDelete: PendingCategoryDelete? = null,
        val message: String? = null,
        /** 展开状态不落库，只保存在进程内；离开分类页再回来仍然保留。 */
        val expanded: Set<Long> = emptySet(),
    )

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<CategoryManageUiState> = combine(
        repository.categoryTree,
        transient,
    ) { tree, local ->
        val branches = branchIds(tree)
        CategoryManageUiState(
            tree = tree,
            editor = local.editor,
            pendingDelete = local.pendingDelete,
            message = local.message,
            expandedIds = local.expanded,
            allExpanded = branches.isNotEmpty() && branches.all { it in local.expanded },
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
        transient.update { local ->
            local.copy(
                // 在收起的父分类上「+」新建子分类时，先展开它，否则新分类保存后看不到。
                expanded = if (parent == null) local.expanded else local.expanded + parent.category.id,
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

    /** 拖动排序：在同父分类的兄弟节点之间移动。 */
    fun moveCategory(categoryId: Long, targetIndex: Int) {
        viewModelScope.launch { repository.moveCategory(categoryId, targetIndex) }
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

    /** 展开或收起一个分类（只影响它的子级是否可见）。 */
    fun toggleExpanded(categoryId: Long) {
        transient.update { local ->
            local.copy(
                expanded = if (categoryId in local.expanded) {
                    local.expanded - categoryId
                } else {
                    local.expanded + categoryId
                },
            )
        }
    }

    /** 展开所有「有子分类」的节点；只影响可见性，不落库。 */
    fun expandAll() {
        transient.update { it.copy(expanded = branchIds(uiState.value.tree)) }
    }

    /** 全部收起，只保留根分类。 */
    fun collapseAll() {
        transient.update { it.copy(expanded = emptySet()) }
    }

    companion object {
        private const val MAX_DEPTH = 2

        /** 所有有子分类的节点 id；「全部展开」与「是否已全部展开」都用它。 */
        private fun branchIds(tree: CategoryTree): Set<Long> =
            tree.flatten()
                .filter { (node, _) -> node.children.isNotEmpty() }
                .map { (node, _) -> node.category.id }
                .toSet()

        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoryManageViewModel(repository) }
        }
    }
}
