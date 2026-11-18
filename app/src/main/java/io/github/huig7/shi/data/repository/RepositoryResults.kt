package io.github.huig7.shi.data.repository

/** 保存记录的结果：写入成功、编辑对象已不存在，或重叠情况在确认期间发生变化。 */
sealed interface SaveEventResult {
    data class Saved(val id: Long) : SaveEventResult
    data object EventNotFound : SaveEventResult
    data object OverlapChanged : SaveEventResult
}

/** 删除分类的结果。 */
sealed interface CategoryDeletionResult {
    data class Deleted(
        val categoryCount: Int,
        val deletedEventCount: Int,
        val movedEventCount: Int = 0,
        val movedToName: String? = null,
    ) : CategoryDeletionResult

    data object NotFound : CategoryDeletionResult

    /** 这是最后一个分类：删掉后没有分类可记录，也会让分类树与记录表单无选项。 */
    data object LastCategory : CategoryDeletionResult

    /** 保留记录时指定的目标分类不存在，或落在被删除的子树里。 */
    data object InvalidMoveTarget : CategoryDeletionResult
}
