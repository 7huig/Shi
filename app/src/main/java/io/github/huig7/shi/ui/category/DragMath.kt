package io.github.huig7.shi.ui.category

/**
 * 拖拽排序的纯函数：不依赖 Compose，便于单元测试。
 * 坐标统一用「像素」表示，由 UI 层按密度换算后传入。
 */

/**
 * 计算稳定的落点 slot（插入下标，范围 0..siblingCenters.size）。
 *
 * [siblingCenters] 是同一父分类下、按当前显示顺序排列的兄弟行中心（不含被拖行）。
 * LazyColumn 只渲染可见行，所以屏幕上方之外的兄弟用 -Float.MAX_VALUE、
 * 下方之外的用 Float.MAX_VALUE 表示其中心，这样边界处的结果仍然正确。
 *
 * 只有指针越过「当前插入点相邻兄弟的中心」至少 [hysteresisPx]，才允许 slot 变化；
 * 否则保持 [currentSlot]。滞回量让临界点附近不会来回抖动。
 * 返回值语义：被拖行会插到第 slot 个兄弟之前（slot = 兄弟数量表示排到最后）。
 */
fun stableDropSlot(
    pointerY: Float,
    siblingCenters: List<Float>,
    currentSlot: Int,
    hysteresisPx: Float,
): Int {
    if (siblingCenters.isEmpty()) return 0
    var slot = currentSlot.coerceIn(0, siblingCenters.size)
    val hysteresis = hysteresisPx.coerceAtLeast(0f)
    // 向下：先越过插入点下方第一位兄弟的中心、再留出滞回量，才把插入点后移。
    while (slot < siblingCenters.size && pointerY > siblingCenters[slot] + hysteresis) slot++
    // 向上：先越过插入点上方第一位兄弟的中心、再留出滞回量，才把插入点前移。
    while (slot > 0 && pointerY < siblingCenters[slot - 1] - hysteresis) slot--
    return slot
}

/**
 * 拖到列表边缘时的自动滚动位移（每帧，正数向下滚）。
 *
 * [pointerInViewport] 是指针相对列表视口的 y。指针进入上下 [edgePx] 的窄边时，
 * 按进入深度线性加速：刚到边缘时为 0，贴到视口边界时达到 [maxStepPx]。
 * 指针离开视口时冻结滚动，避免从顶栏或底栏误触发。
 */
fun edgeScrollDelta(
    pointerInViewport: Float,
    viewportHeight: Float,
    edgePx: Float,
    maxStepPx: Float,
): Float {
    if (viewportHeight <= 0f || edgePx <= 0f || maxStepPx <= 0f) return 0f
    if (pointerInViewport < 0f || pointerInViewport > viewportHeight) return 0f
    val intoTop = edgePx - pointerInViewport
    if (intoTop > 0f) return -((intoTop / edgePx).coerceIn(0f, 1f)) * maxStepPx
    val intoBottom = pointerInViewport - (viewportHeight - edgePx)
    if (intoBottom > 0f) return ((intoBottom / edgePx).coerceIn(0f, 1f)) * maxStepPx
    return 0f
}
