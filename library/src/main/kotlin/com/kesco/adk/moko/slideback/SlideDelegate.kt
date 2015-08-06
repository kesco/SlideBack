package com.kesco.adk.moko.slideback

import android.graphics.Canvas
import android.view.View

public interface SlideListener {
    fun onSlide(percent: Float)
    fun onSlideFinish()
}

public enum class SlideEdge {
    LEFT, TOP, RIGHT, BOTTOM, NONE
}

interface SlideViewDelegate {
    fun canViewDragged(pointerId: Int): Boolean
    fun clampViewPosition(distance: Int): Int
    fun decorateDraggedView(canvas:Canvas)
    fun onDraggedViewPositionChange(left: Int, top: Int)
    fun draggedViewDestination(xvel: Float, yvel: Float):Array<Int>

    fun addListener(l:SlideListener?)
}