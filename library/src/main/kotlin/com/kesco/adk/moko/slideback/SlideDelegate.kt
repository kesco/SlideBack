package com.kesco.adk.moko.slideback

import android.view.View

public interface SlideListener {
    fun onSlide(percent: Float)
    fun onSlideFinish()
}

public enum class SlideEdge {
    LEFT, TOP, RIGHT, BOTTOM, NONE
}

interface ViewDragDelegate {
    fun canViewDragged(pointerId: Int): Boolean
    fun clampViewPosition(distance: Int): Int
    fun decorateDraggedView()
    fun onDraggedViewPositionChange(left: Int, top: Int)
    fun draggedViewDestination(xvel: Float, yvel: Float):Array<Int>
}