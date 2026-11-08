package io.github.huig7.shi.ui.category

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 拖拽落点与边缘滚动的纯函数测试（像素坐标，不依赖 Compose）。
 */
class DragMathTest {

    private val hysteresis = 6f

    // ---------- stableDropSlot ----------

    @Test
    fun emptySiblingsKeepSingleSlot() {
        assertEquals(0, stableDropSlot(pointerY = 500f, siblingCenters = emptyList(), currentSlot = 0, hysteresisPx = hysteresis))
        assertEquals(0, stableDropSlot(pointerY = 500f, siblingCenters = emptyList(), currentSlot = 7, hysteresisPx = hysteresis))
    }

    @Test
    fun keepsSlotWhilePointerBetweenNeighbours() {
        // 插入点在 100 与 200 之间，指针在中间区域不动。
        assertEquals(1, stableDropSlot(150f, listOf(100f, 200f), currentSlot = 1, hysteresisPx = hysteresis))
    }

    @Test
    fun smallOvershootWithinHysteresisKeepsSlot() {
        // 向下越过 200 但不足 6px：保持原位。
        assertEquals(1, stableDropSlot(205f, listOf(100f, 200f), currentSlot = 1, hysteresisPx = hysteresis))
        // 向上越过 100 但不足 6px：保持原位。
        assertEquals(1, stableDropSlot(95f, listOf(100f, 200f), currentSlot = 1, hysteresisPx = hysteresis))
    }

    @Test
    fun crossingHysteresisChangesSlot() {
        assertEquals(2, stableDropSlot(207f, listOf(100f, 200f), currentSlot = 1, hysteresisPx = hysteresis))
        assertEquals(0, stableDropSlot(93f, listOf(100f, 200f), currentSlot = 1, hysteresisPx = hysteresis))
    }

    @Test
    fun clampsAtFirstAndLastSlot() {
        // 顶部越界仍在 0。
        assertEquals(0, stableDropSlot(-1000f, listOf(100f, 200f), currentSlot = 1, hysteresisPx = hysteresis))
        // 底部越界夹到兄弟数量。
        assertEquals(2, stableDropSlot(1000f, listOf(100f, 200f), currentSlot = 1, hysteresisPx = hysteresis))
    }

    @Test
    fun movingFarCrossesSeveralSlotsAtOnce() {
        assertEquals(3, stableDropSlot(1000f, listOf(100f, 200f, 300f), currentSlot = 0, hysteresisPx = hysteresis))
        assertEquals(0, stableDropSlot(-1000f, listOf(100f, 200f, 300f), currentSlot = 3, hysteresisPx = hysteresis))
    }

    @Test
    fun offscreenSiblingsAboveBlockMovingUp() {
        // 上方屏幕外的兄弟中心是 -MAX：指针无法越过它们继续上移。
        val centers = listOf(-Float.MAX_VALUE, -Float.MAX_VALUE, 100f)
        assertEquals(2, stableDropSlot(50f, centers, currentSlot = 2, hysteresisPx = hysteresis))
        // 已经越过最近的下方兄弟时，仍可以向下移动一格。
        assertEquals(3, stableDropSlot(200f, centers, currentSlot = 2, hysteresisPx = hysteresis))
    }

    @Test
    fun offscreenSiblingsBelowDoNotPullSlotForward() {
        val centers = listOf(100f, Float.MAX_VALUE, Float.MAX_VALUE)
        // 指针已经落在下方屏幕外兄弟的「区域」里，也要等它们真正滚动进视野才变化。
        assertEquals(1, stableDropSlot(150f, centers, currentSlot = 1, hysteresisPx = hysteresis))
        assertEquals(1, stableDropSlot(500f, centers, currentSlot = 1, hysteresisPx = hysteresis))
    }

    @Test
    fun negativeHysteresisIsTreatedAsZero() {
        assertEquals(1, stableDropSlot(100.5f, listOf(100f), currentSlot = 0, hysteresisPx = -8f))
    }

    // ---------- edgeScrollDelta ----------

    private val viewport = 800f
    private val edge = 72f
    private val maxStep = 16f

    @Test
    fun noScrollInTheMiddle() {
        assertEquals(0f, edgeScrollDelta(pointerInViewport = 400f, viewportHeight = viewport, edgePx = edge, maxStepPx = maxStep))
    }

    @Test
    fun scrollSpeedGrowsLinearlyIntoTopEdge() {
        assertEquals(-maxStep, edgeScrollDelta(0f, viewport, edge, maxStep))
        assertEquals(-maxStep / 2f, edgeScrollDelta(36f, viewport, edge, maxStep))
        assertEquals(-maxStep / 4f, edgeScrollDelta(54f, viewport, edge, maxStep))
    }

    @Test
    fun scrollSpeedGrowsLinearlyIntoBottomEdge() {
        assertEquals(maxStep, edgeScrollDelta(viewport, viewport, edge, maxStep))
        assertEquals(maxStep / 2f, edgeScrollDelta(viewport - 36f, viewport, edge, maxStep))
        assertEquals(maxStep / 4f, edgeScrollDelta(viewport - 54f, viewport, edge, maxStep))
    }

    @Test
    fun noScrollExactlyAtEdgeBoundary() {
        assertEquals(0f, edgeScrollDelta(edge, viewport, edge, maxStep))
        assertEquals(0f, edgeScrollDelta(viewport - edge, viewport, edge, maxStep))
    }

    @Test
    fun noScrollWhenPointerLeavesViewport() {
        assertEquals(0f, edgeScrollDelta(-1f, viewport, edge, maxStep))
        assertEquals(0f, edgeScrollDelta(viewport + 1f, viewport, edge, maxStep))
    }

    @Test
    fun degenerateSizesDoNotScroll() {
        assertEquals(0f, edgeScrollDelta(10f, 0f, edge, maxStep))
        assertEquals(0f, edgeScrollDelta(10f, viewport, 0f, maxStep))
        assertEquals(0f, edgeScrollDelta(10f, viewport, edge, 0f))
    }
}
