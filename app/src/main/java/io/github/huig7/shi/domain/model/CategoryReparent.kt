package io.github.huig7.shi.domain.model

/** 分类最多 3 层：根分类为第 1 层，最深只能到第 3 层。 */
const val MAX_CATEGORY_DEPTH = 3

/**
 * 把分类挂到新父级时的校验结果。
 *
 * 分类树没有「拖到任意层级」的手势，跨层级调整统一走「编辑分类 → 父分类」，
 * 这里集中做防环与层数校验，UI 与仓库共用同一套规则。
 */
sealed interface ReparentCheck {
    data object Ok : ReparentCheck

    /** 分类或目标父级已经不存在（并发删除等）。 */
    data object NotFound : ReparentCheck

    /** 不能挂到自己或自己的后代下面，否则整棵子树会从树上脱落。 */
    data object SelfOrDescendant : ReparentCheck

    /** 移动后子树会超过 3 层。 */
    data object DepthExceeded : ReparentCheck
}

/**
 * [categoryId] 自身到最深层后代的相对层数差：叶子为 0，
 * 有一个子分类为 1，依此类推。
 */
fun CategoryTree.subtreeDepth(categoryId: Long): Int {
    val node = find(categoryId) ?: return 0
    fun depthOfNode(current: CategoryNode): Int =
        current.children.maxOfOrNull { child -> 1 + depthOfNode(child) } ?: 0
    return depthOfNode(node)
}

/**
 * 判断把 [categoryId] 挂到 [newParentId]（null = 顶层）是否合法。
 *
 * 约束只有两条：不能形成环；移动后整棵子树仍然不超过 [MAX_CATEGORY_DEPTH] 层。
 */
fun CategoryTree.checkReparent(categoryId: Long, newParentId: Long?): ReparentCheck {
    if (find(categoryId) == null) return ReparentCheck.NotFound
    if (newParentId != null) {
        if (find(newParentId) == null) return ReparentCheck.NotFound
        // 目标在自己子树里：包括「挂到自己下面」和「挂到孙分类下面」。
        if (newParentId in subtreeIds(categoryId)) return ReparentCheck.SelfOrDescendant
    }
    val newDepth = if (newParentId == null) 1 else depthOf(newParentId) + 1
    if (newDepth + subtreeDepth(categoryId) > MAX_CATEGORY_DEPTH) {
        return ReparentCheck.DepthExceeded
    }
    return ReparentCheck.Ok
}

/** 编辑分类时「父分类」候选：[node] 为 null 表示顶层；[depth] 用于列表缩进。 */
data class ReparentTarget(
    val node: CategoryNode?,
    val depth: Int,
)

/**
 * 当前分类可选的父级：顶层在前，其余按分类树展示顺序排列。
 * 只返回通过 [checkReparent] 的选项，避免用户选中后才报错。
 */
fun CategoryTree.reparentTargets(categoryId: Long): List<ReparentTarget> {
    if (find(categoryId) == null) return emptyList()
    val result = mutableListOf<ReparentTarget>()
    if (checkReparent(categoryId, null) == ReparentCheck.Ok) {
        result += ReparentTarget(node = null, depth = 0)
    }
    flatten().forEach { (node, depth) ->
        if (checkReparent(categoryId, node.category.id) == ReparentCheck.Ok) {
            result += ReparentTarget(node = node, depth = depth)
        }
    }
    return result
}
