package com.kesco.adk.moko.slideback

import android.graphics.Canvas

interface SlideListener {
    fun onSlideStart()
    fun onSlide(percent: Float, state: SlideState)
    fun onSlideFinish()
}

enum class SlideEdge {
    LEFT, TOP, RIGHT, BOTTOM, NONE
}

enum class SlideShadow {
    EDGE, FULL
}

enum class SlideState {
    DRAGGING, IDLE, SETTLING
}

internal enum class Direction {
    Horizontal, Vertical
}

internal interface SlideViewDelegate {
    fun canViewDragged(pointerId: Int): Boolean
    fun clampViewPosition(distance: Int, direct: Direction): Int
    fun decorateDraggedView(canvas: Canvas)
    fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int>
    fun draggedPercent(left: Int, top: Int): Float
}