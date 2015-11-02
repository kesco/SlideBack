package com.kesco.adk.moko.slideback

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

private val FULL_ALPHA: Int = 255

/**
 * 底层滑动的Layout
 */
public class SlideLayout(ctx: Context, val slideView: View) : FrameLayout(ctx) {
    private val screenWidth: Int
    private val slideHelper: ViewDragHelper;

    private var inLayout: Boolean = false
    private var slideViewLeft = 0
    private var slideViewTop = 0

    private var slideDelegate: SlideViewDelegate = NoneSlideViewImpl()

    private var slidePercent: Float = 0f
    private var slideState: SlideState = SlideState.IDLE

    var listener: SlideListener? = null

    var slideEdge: SlideEdge = SlideEdge.NONE
        public set(value) {
            when (value) {
                SlideEdge.NONE -> slideDelegate = NoneSlideViewImpl()
                SlideEdge.LEFT -> slideDelegate = LeftSlideViewImpl(slideView, slideHelper)
                SlideEdge.RIGHT -> slideDelegate = RightSlideViewImpl(slideView, slideHelper)
                SlideEdge.TOP -> slideDelegate = TopSlideViewImpl(slideView, slideHelper)
                SlideEdge.BOTTOM -> slideDelegate = BottomSlideViewImpl(slideView, slideHelper)
            }
        }

    init {
        screenWidth = resources.displayMetrics.widthPixels
        slideHelper = ViewDragHelper.create(this, 1.0f, SlideCallback())
        slideView.id = R.id.slide_view
    }

    override fun requestLayout(): Unit {
        if (!inLayout) super.requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        inLayout = true
        slideView.layout(slideViewLeft, slideViewTop, slideViewLeft + slideView.measuredWidth,
                slideViewTop + slideView.measuredHeight)
        inLayout = false
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val ret = super.drawChild(canvas, child, drawingTime)
        if (child == slideView) {
            slideDelegate.decorateDraggedView(canvas)
        }
        return ret
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var shouldIntercept: Boolean
        try {
            shouldIntercept = slideHelper.shouldInterceptTouchEvent(ev)
        } catch(e: Exception) {
            shouldIntercept = false
        }
        return shouldIntercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            slideHelper.processTouchEvent(event)
        } catch(ex: IllegalArgumentException) {
            return super.onTouchEvent(event)
        }
        return true
    }

    override fun computeScroll() {
        if (slideHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    inner class SlideCallback() : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            val ok = child.id == R.id.slide_view && slideDelegate.canViewDragged(pointerId)
            if (ok) listener?.onSlideStart()
            return ok
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = slideDelegate.clampViewPosition(left, Direction.Horizontal)

        override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int = slideDelegate.clampViewPosition(top, Direction.Vertical)

        override fun getViewHorizontalDragRange(child: View?): Int = screenWidth

        override fun getViewVerticalDragRange(child: View?): Int = slideView.measuredHeight

        override fun onViewDragStateChanged(state: Int) {
            when (state) {
                ViewDragHelper.STATE_IDLE -> slideState = SlideState.IDLE
                ViewDragHelper.STATE_DRAGGING -> slideState = SlideState.DRAGGING
                ViewDragHelper.STATE_SETTLING -> slideState = SlideState.SETTLING
            }
            if (slidePercent > 0.9f && slideState != SlideState.DRAGGING) {
                listener?.onSlideFinish()
            }
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
            slideViewLeft = left
            slideViewTop = top
            invalidate()
            slidePercent = slideDelegate.draggedPercent(left, top)
            listener?.onSlide(slidePercent, slideState)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val dest = slideDelegate.draggedViewDestination(xvel, yvel)
            slideHelper.settleCapturedViewAt(dest[0], dest[1])
            invalidate()
        }
    }
}

private class NoneSlideViewImpl() : SlideViewDelegate {
    override fun canViewDragged(pointerId: Int): Boolean = false

    override fun clampViewPosition(distance: Int, direct: Direction): Int = 0

    override fun decorateDraggedView(canvas: Canvas) {
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> = arrayOf(0, 0)

    override fun draggedPercent(left: Int, top: Int): Float = 0f
}

private abstract class SlideViewImpl(val draggedView: View, val dragHelper: ViewDragHelper, edge: SlideEdge) : SlideViewDelegate {
    var scrollPercent: Float = 0f
    val shadow: Drawable
    val shadowWidth: Int
    val rect: Rect = Rect()

    init {
        shadow = drawShadow(draggedView.context, edge)
//        shadowWidth = draggedView.context.resources.getDimensionPixelSize(R.dimen.shadow_width)
        shadowWidth = draggedView.resources.displayMetrics.widthPixels
    }
}

private class LeftSlideViewImpl(draggedView: View, dragHelper: ViewDragHelper) : SlideViewImpl(draggedView, dragHelper, SlideEdge.LEFT) {
    override fun canViewDragged(pointerId: Int): Boolean = dragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT, pointerId)

    override fun clampViewPosition(distance: Int, direct: Direction): Int {
        return if (direct == Direction.Horizontal) Math.max(0, Math.min(distance, draggedView.measuredWidth)) else 0
    }

    override fun decorateDraggedView(canvas: Canvas) {
        draggedView.getHitRect(rect)
        shadow.setBounds(rect.left - shadowWidth, rect.top,
                rect.left, rect.bottom)
        shadow.alpha = ((1 - scrollPercent) * FULL_ALPHA).toInt()
        shadow.draw(canvas)
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> {
        val left = draggedView.left
        val width = draggedView.measuredWidth
        val threshold = width / 2
        val settleLeft: Int
        if (xvel > threshold) {
            settleLeft = width
        } else if (xvel < 0 && Math.abs(xvel) > threshold) {
            settleLeft = 0
        } else {
            settleLeft = if (left > threshold) width else 0
        }
        return arrayOf(settleLeft, draggedView.top)
    }

    override fun draggedPercent(left: Int, top: Int): Float = Math.abs(left.toFloat() / (draggedView.width))
}

private class RightSlideViewImpl(draggedView: View, dragHelper: ViewDragHelper) : SlideViewImpl(draggedView, dragHelper, SlideEdge.RIGHT) {
    override fun canViewDragged(pointerId: Int): Boolean = dragHelper.isEdgeTouched(ViewDragHelper.EDGE_RIGHT, pointerId)

    override fun clampViewPosition(distance: Int, direct: Direction): Int {
        return if (direct == Direction.Horizontal) Math.min(0, Math.max(distance, -draggedView.measuredWidth)) else 0
    }

    override fun decorateDraggedView(canvas: Canvas) {
        draggedView.getHitRect(rect)
        shadow.setBounds(rect.right, rect.top,
                rect.right + shadowWidth, rect.bottom)
        shadow.alpha = ((1 - scrollPercent) * FULL_ALPHA).toInt()
        shadow.draw(canvas)
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> {
        val left = draggedView.left
        val width = draggedView.measuredWidth
        val threshold = width / 2
        val settleLeft = if ((xvel < 0 && Math.abs(xvel) > threshold) || Math.abs(left) > threshold) -width else 0
        return arrayOf(settleLeft, draggedView.top)
    }

    override fun draggedPercent(left: Int, top: Int): Float = Math.abs(left.toFloat() / (draggedView.width + shadowWidth))
}

private class TopSlideViewImpl(draggedView: View, dragHelper: ViewDragHelper) : SlideViewImpl(draggedView, dragHelper, SlideEdge.TOP) {
    val statusBarHeight: Int

    init {
        statusBarHeight = getStatusBarHeight(draggedView.context)
    }

    override fun canViewDragged(pointerId: Int): Boolean = true

    override fun clampViewPosition(distance: Int, direct: Direction): Int {
        return if (direct == Direction.Vertical) Math.max(0, Math.min(distance, draggedView.measuredHeight)) else 0
    }

    override fun decorateDraggedView(canvas: Canvas) {
        draggedView.getHitRect(rect)
        shadow.setBounds(rect.left, rect.top + statusBarHeight - shadowWidth,
                rect.right, rect.top + statusBarHeight)
        shadow.alpha = ((1 - scrollPercent) * FULL_ALPHA).toInt()
        shadow.draw(canvas)
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> {
        val top = draggedView.top
        val height = draggedView.measuredHeight
        val threshold = height / 2
        val settleTop: Int
        if (yvel > threshold) {
            settleTop = height
        } else if (yvel < 0 && Math.abs(yvel) > threshold) {
            settleTop = 0
        } else {
            settleTop = if (top > threshold) height else 0
        }
        return arrayOf(draggedView.left, settleTop)
    }

    override fun draggedPercent(left: Int, top: Int): Float = Math.abs(top.toFloat() / (draggedView.height + shadowWidth))
}

private class BottomSlideViewImpl(draggedView: View, dragHelper: ViewDragHelper) : SlideViewImpl(draggedView, dragHelper, SlideEdge.BOTTOM) {
    override fun canViewDragged(pointerId: Int): Boolean = true

    override fun clampViewPosition(distance: Int, direct: Direction): Int {
        return if (direct == Direction.Vertical) Math.min(0, Math.max(distance, -draggedView.measuredHeight)) else 0
    }

    override fun decorateDraggedView(canvas: Canvas) {
        draggedView.getHitRect(rect)
        shadow.setBounds(rect.left, rect.bottom,
                rect.right, rect.bottom + shadowWidth)
        shadow.alpha = ((1 - scrollPercent) * FULL_ALPHA).toInt()
        shadow.draw(canvas)
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> {
        val top = draggedView.top
        val height = draggedView.measuredHeight
        val threshold = height / 2
        val settleTop: Int
        if (Math.abs(yvel) > threshold) {
            settleTop = -height
        } else if (yvel > 0) {
            settleTop = 0
        } else {
            settleTop = if (Math.abs(top) > threshold) height else 0
        }
        return arrayOf(draggedView.left, settleTop)
    }

    override fun draggedPercent(left: Int, top: Int): Float = Math.abs(top.toFloat() / (draggedView.height + shadowWidth))
}

private fun drawShadow(ctx: Context, edge: SlideEdge): Drawable {
    val res = ctx.resources
    val shadow = GradientDrawable()
    shadow.setColors(intArrayOf(
            res.getColor(R.color.start_shadow_color),
            res.getColor(R.color.center_shadow_color),
            res.getColor(R.color.end_shadow_color)))
    shadow.setShape(GradientDrawable.RECTANGLE)
    val orientation = when (edge) {
        SlideEdge.LEFT -> GradientDrawable.Orientation.LEFT_RIGHT
        SlideEdge.RIGHT -> GradientDrawable.Orientation.RIGHT_LEFT
        SlideEdge.TOP -> GradientDrawable.Orientation.TOP_BOTTOM
        SlideEdge.BOTTOM -> GradientDrawable.Orientation.TOP_BOTTOM
        SlideEdge.NONE -> throw IllegalArgumentException("None has no shadow")
    }
    shadow.orientation = orientation
    return shadow
}

private fun getStatusBarHeight(ctx: Context): Int {
    val resource = ctx.resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resource > 0) ctx.resources.getDimensionPixelSize(resource) else throw RuntimeException("Can not get the status bar on the platform.")
}

private fun Resources.getColor(id: Int) = getColor(id, null)

