package com.kesco.adk.moko.slideback

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

private val FULL_ALPHA: Int = 255

/**
 * 底层滑动的Layout
 */
public class SlidLayout(ctx: Context, val innerView: View) : FrameLayout(ctx) {
    private val screenWidth: Int
    private val dragHelper: ViewDragHelper;

    private val shadow: Drawable
    private val childRect: Rect = Rect()

    private var inLayout: Boolean = false
    private var innerViewLeft = 0
    private var innerViewTop = 0

    private var scrollPercent: Float = 0f

    private var listener: SlideListener? = null

    init {
        screenWidth = getResources().getDisplayMetrics().widthPixels
        dragHelper = ViewDragHelper.create(this, 1.0f, DragCallback())
        innerView.setId(R.id.slide_view)
        shadow = getResources().getDrawable(R.drawable.shadow_left)
    }

    override fun requestLayout() {
        if (!inLayout) {
            super.requestLayout()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        inLayout = true
        innerView.layout(innerViewLeft, innerViewTop, innerViewLeft + innerView.getMeasuredWidth(),
                innerViewTop + innerView.getMeasuredHeight())
        inLayout = false
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val ret = super.drawChild(canvas, child, drawingTime)
        if (child == innerView) {
            child.getHitRect(childRect)
            shadow.setBounds(childRect.left - shadow.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom)
            shadow.setAlpha(((1 - scrollPercent) * FULL_ALPHA).toInt())
            shadow.draw(canvas)
        }
        return ret
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var shouldIntercept: Boolean
        try {
            shouldIntercept = dragHelper.shouldInterceptTouchEvent(ev)
        } catch(e: Exception) {
            shouldIntercept = false
        }
        return shouldIntercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            dragHelper.processTouchEvent(event)
        } catch(ex: IllegalArgumentException) {
            return super.onTouchEvent(event)
        }
        return true
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    public fun setListener(l: SlideListener) {
        listener = l
    }

    private inner class DragCallback() : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            /* 这里做个边缘触摸判断 */
            return child.getId() == R.id.slide_view && dragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT, pointerId)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return Math.max(0, Math.min(left, screenWidth))
        }

        override fun getViewHorizontalDragRange(child: View?): Int {
            return screenWidth
        }

        override fun onViewDragStateChanged(state: Int) {
            when (state) {
                ViewDragHelper.STATE_DRAGGING, ViewDragHelper.STATE_IDLE, ViewDragHelper.STATE_SETTLING -> {
                    super.onViewDragStateChanged(state)
                }
            }
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            innerViewLeft = left
            scrollPercent = Math.abs(left.toFloat() / (innerView.getWidth() + shadow.getIntrinsicWidth()))
            invalidate()

            listener?.onSlide(scrollPercent)
            if (scrollPercent > 0.95f) listener?.onSlideFinish()
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val left = releasedChild.getLeft()
            val threshold = screenWidth / 2
            val settleLeft: Int
            if (xvel > threshold) {
                settleLeft = screenWidth
            } else if (xvel < 0 && Math.abs(xvel) > threshold) {
                settleLeft = 0
            } else {
                settleLeft = if (left > threshold) screenWidth else 0
            }
            dragHelper.settleCapturedViewAt(settleLeft, releasedChild.getTop())
            invalidate()
        }
    }
}
