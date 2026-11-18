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
import io.github.huig7.shi.domain.model.MAX_CATEGORY_DEPTH
import io.github.huig7.shi.domain.model.ReparentCheck
import io.github.huig7.shi.domain.model.SolarTerms
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryEditor(
    val id: Long?,
    /** 编辑中的目标父级；null 表示顶层。可在编辑弹窗里调整，实现跨层级移动。 */
    val parentId: Long?,
    val name: String,
    val colorHex: Long,
)

data class PendingCategoryDelete(
    val node: CategoryNode,
    val eventCount: Int,
    /** 保留记录时默认改挂的目标；根分类没有父级，为 null，交给用户选择。 */
    val moveTargetId: Long?,
    val moveTargetName: String?,
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
        // depthOf 是 1 基深度；MAX_CATEGORY_DEPTH 是层数。父级达到最深一层时不能再加子级。
        if (parent != null && tree.depthOf(parent.category.id) >= MAX_CATEGORY_DEPTH) {
            transient.update { it.copy(message = "分类最多支持 $MAX_CATEGORY_DEPTH 层") }
            return
        }
        transient.update { local ->
            local.copy(
                // 在收起的父分类上「+」新建子分类时，先展开它，否则新分类保存后看不到。
                expanded = if (parent == null) local.expanded else local.expanded + parent.category.id,
                editor = CategoryEditor(
                    id = null,
                    parentId = parent?.category?.id,
                    name = "",
                    colorHex = SolarTerms.random().colorHex,
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

    /** 在编辑弹窗里选择新的父分类；null 表示升级为顶层分类。 */
    fun onEditorParentChange(parentId: Long?) {
        transient.update { current ->
            // 展开新父级的整条父链，保存后能立刻在列表里看到移动结果，而不是「消失」了。
            val newlyExpanded = parentId
                ?.let { id -> uiState.value.tree.chainOf(id).map { it.category.id }.toSet() }
                .orEmpty()
            current.copy(
                editor = current.editor?.copy(parentId = parentId),
                expanded = current.expanded + newlyExpanded,
            )
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
            try {
                if (editor.id == null) {
                    repository.createCategory(name, editor.parentId, editor.colorHex)
                    transient.update { it.copy(editor = null) }
                } else {
                    when (
                        repository.updateCategory(
                            id = editor.id,
                            name = name,
                            colorHex = editor.colorHex,
                            parentId = editor.parentId,
                        )
                    ) {
                        ReparentCheck.Ok -> transient.update { it.copy(editor = null) }
                        ReparentCheck.NotFound ->
                            transient.update { it.copy(message = "分类已不存在，请重试") }
                        ReparentCheck.SelfOrDescendant ->
                            transient.update { it.copy(message = "不能移到自己的子分类下面") }
                        ReparentCheck.DepthExceeded ->
                            transient.update { it.copy(message = "最多支持 $MAX_CATEGORY_DEPTH 层，无法移到该分类下") }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // 保留编辑器内容，允许直接重试。
                transient.update { it.copy(message = "保存失败，请重试") }
            }
        }
    }

    /** 拖动排序：在同父分类的兄弟节点之间移动。 */
    fun moveCategory(categoryId: Long, targetIndex: Int) {
        viewModelScope.launch {
            try {
                repository.moveCategory(categoryId, targetIndex)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                transient.update { it.copy(message = "排序失败，请重试") }
            }
        }
    }

    fun requestDelete(node: CategoryNode) {
        val currentTree = uiState.value.tree
        val subtreeSize = currentTree.subtreeIds(node.category.id).size
        if (subtreeSize > 0 && subtreeSize == currentTree.flatten().size) {
            // 只剩这一个分类：提前拦住，避免用户确认后才发现删不掉。
            transient.update { it.copy(message = "至少保留一个分类，才能继续记录") }
            return
        }
        viewModelScope.launch {
            try {
                val count = repository.countEventsInSubtree(node.category.id)
                val tree = uiState.value.tree
                val parentId = node.category.parentId?.takeIf { tree.find(it) != null }
                transient.update {
                    it.copy(
                        pendingDelete = PendingCategoryDelete(
                            node = node,
                            eventCount = count,
                            moveTargetId = parentId,
                            moveTargetName = parentId?.let { id -> tree.find(id)?.category?.name },
                        ),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                transient.update { it.copy(message = "读取分类数据失败，请重试") }
            }
        }
    }

    fun dismissDelete() {
        transient.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        deleteCategory(moveEventsToId = null)
    }

    /** 删除分类但保留记录：改挂到父分类，根分类则由用户在弹窗里选择目标。 */
    fun confirmDeleteMovingEvents(targetId: Long) {
        deleteCategory(moveEventsToId = targetId)
    }

    private fun deleteCategory(moveEventsToId: Long?) {
        val pending = transient.value.pendingDelete ?: return
        viewModelScope.launch {
            try {
                val message = when (
                    val outcome = repository.deleteCategory(
                        pending.node.category.id,
                        moveEventsToId = moveEventsToId,
                    )
                ) {
                    is CategoryDeletionResult.Deleted -> buildString {
                        append("已删除 ").append(outcome.categoryCount).append(" 个分类")
                        if (outcome.deletedEventCount > 0) {
                            append("，").append(outcome.deletedEventCount).append(" 条事件")
                        }
                        if (outcome.movedEventCount > 0) {
                            append("，").append(outcome.movedEventCount).append(" 条记录移到「")
                            append(outcome.movedToName ?: "所选分类").append("」")
                        }
                    }

                    CategoryDeletionResult.NotFound -> "分类已不存在"
                    CategoryDeletionResult.LastCategory -> "至少保留一个分类，才能继续记录"
                    CategoryDeletionResult.InvalidMoveTarget -> "目标分类无效，请重新选择"
                }
                transient.update { it.copy(pendingDelete = null, message = message) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                transient.update { it.copy(pendingDelete = null, message = "删除失败，请重试") }
            }
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
