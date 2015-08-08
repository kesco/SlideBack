package com.kesco.adk.moko.slideback

import android.content.Context
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

    var listener: SlideListener? = null
        public set(value) = slideDelegate.addListener(value)

    var slideEdge: SlideEdge = SlideEdge.NONE
        public set(value) {
            when (value) {
                SlideEdge.NONE -> slideDelegate = NoneSlideViewImpl()
                SlideEdge.LEFT -> slideDelegate = LeftSlideViewImpl(slideView, slideHelper)
                SlideEdge.RIGHT -> slideDelegate = RightSlideViewImpl(slideView, slideHelper)
            }
        }

    init {
        screenWidth = getResources().getDisplayMetrics().widthPixels
        slideHelper = ViewDragHelper.create(this, 1.0f, SlideCallback())
        slideView.setId(R.id.slide_view)
    }

    override fun requestLayout(): Unit = if (!inLayout) super.requestLayout()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        inLayout = true
        slideView.layout(slideViewLeft, slideViewTop, slideViewLeft + slideView.getMeasuredWidth(),
                slideViewTop + slideView.getMeasuredHeight())
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
        override fun tryCaptureView(child: View, pointerId: Int): Boolean = child.getId() == R.id.slide_view && slideDelegate.canViewDragged(pointerId)

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = slideDelegate.clampViewPosition(left)

        override fun getViewHorizontalDragRange(child: View?): Int = screenWidth

        override fun onViewDragStateChanged(state: Int) {
            when (state) {
                ViewDragHelper.STATE_DRAGGING, ViewDragHelper.STATE_IDLE, ViewDragHelper.STATE_SETTLING -> {
                    super.onViewDragStateChanged(state)
                }
            }
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            slideViewLeft = left
            slideViewTop = top
            invalidate()
            slideDelegate.onDraggedViewPositionChange(left, top)
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

    override fun clampViewPosition(distance: Int): Int = 0

    override fun decorateDraggedView(canvas: Canvas) {
    }

    override fun onDraggedViewPositionChange(left: Int, top: Int) {
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> = arrayOf(0, 0)

    override fun addListener(l: SlideListener?) {
    }
}

private class LeftSlideViewImpl(val draggedView: View, val dragHelper: ViewDragHelper) : SlideViewDelegate {
    var scrollPercent: Float = 0f
    val shadow: Drawable
    val shadowWidth: Int
    val rect: Rect = Rect()
    var l: SlideListener? = null
    var isFinish: Boolean = false

    init {
        shadow = drawShadow(draggedView.getContext(), SlideEdge.LEFT)
        shadowWidth = draggedView.getContext().getResources().getDimensionPixelSize(R.dimen.shadow_width)
    }

    override fun canViewDragged(pointerId: Int): Boolean = dragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT, pointerId)

    override fun clampViewPosition(distance: Int): Int = Math.max(0, Math.min(distance, draggedView.getMeasuredWidth()))

    override fun decorateDraggedView(canvas: Canvas) {
        draggedView.getHitRect(rect)
        shadow.setBounds(rect.left - shadowWidth, rect.top,
                rect.left, rect.bottom)
        shadow.setAlpha(((1 - scrollPercent) * FULL_ALPHA).toInt())
        shadow.draw(canvas)
    }

    override fun onDraggedViewPositionChange(left: Int, top: Int) {
        scrollPercent = Math.abs(left.toFloat() / (draggedView.getWidth() + shadowWidth))
        l?.onSlide(scrollPercent)
        if (scrollPercent > 0.95f && !isFinish) {
            isFinish = true
            l?.onSlideFinish()
        }
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> {
        val left = draggedView.getLeft()
        val width = draggedView.getMeasuredWidth()
        val threshold = width / 2
        val settleLeft: Int
        if (xvel > threshold) {
            settleLeft = width
        } else if (xvel < 0 && Math.abs(xvel) > threshold) {
            settleLeft = 0
        } else {
            settleLeft = if (left > threshold) width else 0
        }
        return arrayOf(settleLeft, draggedView.getTop())
    }

    override fun addListener(l: SlideListener?) {
        this.l = l
    }
}

private class RightSlideViewImpl(val draggedView: View, val dragHelper: ViewDragHelper) : SlideViewDelegate {
    var scrollPercent: Float = 0f
    val shadow: Drawable
    val shadowWidth: Int
    val rect: Rect = Rect()
    var l: SlideListener? = null
    var isFinish: Boolean = false

    init {
        shadow = drawShadow(draggedView.getContext(), SlideEdge.RIGHT)
        shadowWidth = draggedView.getContext().getResources().getDimensionPixelSize(R.dimen.shadow_width)
    }

    override fun canViewDragged(pointerId: Int): Boolean = dragHelper.isEdgeTouched(ViewDragHelper.EDGE_RIGHT, pointerId)

    override fun clampViewPosition(distance: Int): Int = Math.min(0, Math.max(distance, -draggedView.getMeasuredWidth()))

    override fun decorateDraggedView(canvas: Canvas) {
        draggedView.getHitRect(rect)
        shadow.setBounds(rect.right, rect.top,
                rect.right + shadowWidth, rect.bottom)
        shadow.setAlpha(((1 - scrollPercent) * FULL_ALPHA).toInt())
        shadow.draw(canvas)
    }

    override fun onDraggedViewPositionChange(left: Int, top: Int) {
        scrollPercent = Math.abs(left.toFloat() / (draggedView.getWidth() + shadowWidth))
        l?.onSlide(scrollPercent)
        if (scrollPercent > 0.95f && !isFinish) {
            isFinish = true
            l?.onSlideFinish()
        }
    }

    override fun draggedViewDestination(xvel: Float, yvel: Float): Array<Int> {
        val left = draggedView.getLeft()
        val width = draggedView.getMeasuredWidth()
        val threshold = width / 2
        val settleLeft: Int
        if (Math.abs(xvel) > threshold) {
            settleLeft = -width
        } else if (xvel > 0) {
            settleLeft = 0
        } else {
            settleLeft = if (Math.abs(left) > threshold) width else 0
        }
        return arrayOf(settleLeft, draggedView.getTop())
    }

    override fun addListener(l: SlideListener?) {
        this.l = l
    }
}

private fun drawShadow(ctx: Context, edge: SlideEdge): Drawable {
    val res = ctx.getResources()
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
        SlideEdge.BOTTOM -> GradientDrawable.Orientation.BOTTOM_TOP
        SlideEdge.NONE -> throw IllegalArgumentException("None has no shadow")
    }
    shadow.setOrientation(orientation)
    return shadow
}

