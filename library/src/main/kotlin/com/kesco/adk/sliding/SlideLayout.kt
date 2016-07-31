package com.kesco.adk.sliding

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.kesco.adk.moko.slideback.R

interface SlideListener {
  fun onSlideStart()
  fun onSlide(percent: Float, state: SlideState)
  fun onSlideFinish()
}

enum class SlideState {
  DRAGGING, IDLE, SETTLING
}

enum class SlideEdge(val edge: Int) {
  NONE(-1), LEFT(0), RIGHT(1)
}

enum class SlideShadow(val type: Int) {
  NONE(-1), EDGE(0), FULL(1)
}

internal fun val2Edge(edge: Int): SlideEdge {
  return when (edge) {
    0 -> SlideEdge.LEFT
    1 -> SlideEdge.RIGHT
    else -> SlideEdge.NONE
  }
}

internal fun val2Shadow(type: Int): SlideShadow {
  return when (type) {
    0 -> SlideShadow.EDGE
    1 -> SlideShadow.FULL
    else -> SlideShadow.NONE
  }
}

private fun _getColorCompat(res: Resources, id: Int) = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) res.getColor(id, null) else res.getColor(id)

private fun _drawShadow(ctx: Context, edge: SlideEdge): Drawable {
  val shadow = GradientDrawable()
  shadow.colors = intArrayOf(
      _getColorCompat(ctx.resources, R.color.start_shadow_color),
      _getColorCompat(ctx.resources, R.color.center_shadow_color),
      _getColorCompat(ctx.resources, R.color.end_shadow_color))
  shadow.shape = GradientDrawable.RECTANGLE
  shadow.orientation = when (edge) {
    SlideEdge.LEFT -> GradientDrawable.Orientation.LEFT_RIGHT
    SlideEdge.RIGHT -> GradientDrawable.Orientation.RIGHT_LEFT
    SlideEdge.NONE -> throw IllegalArgumentException("None has no shadow")
  }
  return shadow
}

private val FULL_ALPHA: Int = 255
private val TAG = "Slider"

class SlideLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {
  private var _slideHelper: ViewDragHelper
  private var _vTarget: View? = null
  private var _edge: SlideEdge
  private var _inLayout = false
  private var _screenWidth: Int
  private var _slideViewLeft = 0
  private var _slideViewTop = 0
  private var _slideState = SlideState.IDLE
  private var _shadow: SlideShadow
  private var _slidePercent = 0f
  private var _listener: SlideListener? = null
  private var _shadowDrawable: Drawable? = null
  private var _shadowWidth = 0
  private var _shadowRect = Rect()

  var edge: SlideEdge
    set(value) {
      _edge = value
      _shadowDrawable = if (value == SlideEdge.NONE) null else _drawShadow(context, value)
      _shadowWidth = if (_shadow == SlideShadow.EDGE) context.resources.getDimensionPixelSize(R.dimen.shadow_width) else {
        when (edge) {
          SlideEdge.LEFT, SlideEdge.RIGHT -> resources.displayMetrics.widthPixels
          else -> 0
        }
      }
    }
    get() = _edge

  var shadow: SlideShadow
    set(value) {
      _shadow = value
    }
    get() = _shadow

  var listener: SlideListener
    set(l) {
      _listener = l
    }
    get() {
      if (_listener != null) {
        return _listener as SlideListener
      } else {
        return object : SlideListener {
          override fun onSlideStart() {
          }

          override fun onSlide(percent: Float, state: SlideState) {
          }

          override fun onSlideFinish() {
          }
        }
      }
    }

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

  init {
    val typeArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SlideLayout, defStyleAttr, 0)
    _edge = val2Edge(typeArray.getInt(R.styleable.SlideLayout_slide_edge, -1))
    _shadow = val2Shadow(typeArray.getInt(R.styleable.SlideLayout_slide_shadow, -1))
    typeArray.recycle()

    _screenWidth = resources.displayMetrics.widthPixels
    _slideHelper = ViewDragHelper.create(this, 1.0f, SlideCallBack())
  }

  override fun requestLayout() {
    if (!_inLayout) super.requestLayout()
  }

  private fun _ensureTarget() {
    if (_vTarget == null) {
      if (childCount > 1 && !isInEditMode) {
        throw IllegalStateException("SlideLayout can host only one direct child")
      }
      _vTarget = getChildAt(0)
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    _inLayout = true
    _ensureTarget()
    val width = _vTarget?.measuredWidth ?: 0
    val height = _vTarget?.measuredHeight ?: 0
    _vTarget?.layout(_slideViewLeft, _slideViewTop, _slideViewLeft + width,
        _slideViewTop + height)
    _inLayout = false
  }

  override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
    val ret = super.drawChild(canvas, child, drawingTime)
    _ensureTarget()
    if (_vTarget != null) {
      when (_edge) {
        SlideEdge.LEFT -> {
          _vTarget?.getHitRect(_shadowRect)
          _shadowDrawable?.setBounds(_shadowRect.left - _shadowWidth, _shadowRect.top,
              _shadowRect.left, _shadowRect.bottom)
          _shadowDrawable?.alpha = ((1 - _slidePercent) * FULL_ALPHA).toInt()
          _shadowDrawable?.draw(canvas)
        }
        SlideEdge.RIGHT -> {
          _vTarget?.getHitRect(_shadowRect)
          _shadowDrawable?.setBounds(_shadowRect.right, _shadowRect.top,
              _shadowRect.right + _shadowWidth, _shadowRect.bottom)
          _shadowDrawable?.alpha = ((1 - _slidePercent) * FULL_ALPHA).toInt()
          _shadowDrawable?.draw(canvas)
        }
      }
    }
    return ret
  }

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    try {
      return _slideHelper.shouldInterceptTouchEvent(ev)
    } catch (ex: Exception) {
      return false
    }
  }

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    try {
      _slideHelper.processTouchEvent(event)
      return true
    } catch (ex: IllegalArgumentException) {
      return super.onTouchEvent(event)
    }
  }

  override fun computeScroll() {
    if (_slideHelper.continueSettling(true)) {
      ViewCompat.postInvalidateOnAnimation(this)
    }
  }

  private inner class SlideCallBack() : ViewDragHelper.Callback() {
    override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
      _ensureTarget()
      val edge = when (_edge) {
        SlideEdge.LEFT -> ViewDragHelper.EDGE_LEFT
        SlideEdge.RIGHT -> ViewDragHelper.EDGE_RIGHT
        else -> ViewDragHelper.EDGE_ALL
      }
      val ret = _vTarget != null && edge != ViewDragHelper.EDGE_ALL && _slideHelper.isEdgeTouched(edge, pointerId)
      if (ret) _listener?.onSlideStart()
      return ret
    }

    override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
      _ensureTarget()
      val width = _vTarget?.measuredWidth ?: 0
      return when (_edge) {
        SlideEdge.LEFT -> Math.max(0, Math.min(left, width))
        SlideEdge.RIGHT -> Math.min(0, Math.max(left, width))
        SlideEdge.NONE -> 0
      }
    }

    override fun getViewHorizontalDragRange(child: View?): Int = _screenWidth

    override fun onViewDragStateChanged(state: Int) {
      _slideState = when (state) {
        ViewDragHelper.STATE_DRAGGING -> SlideState.DRAGGING
        ViewDragHelper.STATE_SETTLING -> SlideState.SETTLING
        else -> SlideState.IDLE
      }
      if (_slidePercent > 0.9f && _slideState != SlideState.DRAGGING) {
        _listener?.onSlideFinish()
      }
    }

    override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
      _ensureTarget()
      _slideViewLeft = left
      _slideViewTop = top
      invalidate()
      if (_vTarget != null) {
        val width = _vTarget?.width ?: 0
        when (_edge) {
          SlideEdge.LEFT -> {
            _slidePercent = Math.abs(left.toFloat() / (width))
          }
          SlideEdge.RIGHT -> {
            _slidePercent = Math.abs(left.toFloat() / (width))
          }
        }
      }
      _listener?.onSlide(_slidePercent, _slideState)
    }

    override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
      _ensureTarget()
      val top = _vTarget?.top ?: 0
      val left = _vTarget?.left ?: 0
      val width = _vTarget?.width ?: 0
      if (_vTarget != null) {
        val settleLeft: Int
        if (_edge == SlideEdge.LEFT) {
          val threshold = width / 2
          if (xvel > threshold) {
            settleLeft = width
          } else if (xvel < 0 && Math.abs(xvel) > threshold) {
            settleLeft = 0
          } else {
            settleLeft = if (left > threshold) width else 0
          }
        } else if (_edge == SlideEdge.RIGHT) {
          val threshold = width / 2
          settleLeft = if ((xvel < 0 && Math.abs(xvel) > threshold) || Math.abs(left) > threshold) -width else 0
        } else {
          settleLeft = 0
        }
        _slideHelper.settleCapturedViewAt(settleLeft, top)
        invalidate()
      }
    }
  }
}

/**
 * 参考SwipbackLayout的实现
 */
fun convertActivityFromTranslucent(act: Activity) {
  try {
    val method = Activity::class.java.getDeclaredMethod("convertFromTranslucent")
    method.isAccessible = true
    method.invoke(act)
  } catch (t: Throwable) {
    Log.e(TAG, "Can not call the convertFromTranslucent method of Activity")
  }
}

fun convertActivityToTranslucent(act: Activity) {
  if (Build.VERSION.SDK_INT >= 21 /* Build.VERSION_CODES.LOLLIPOP */) {
    _convertActivityToTranslucentAfterL(act)
  } else {
    _convertActivityToTranslucentBeforeL(act)
  }
}

/**
 * 参考SwipbackLayout的实现
 */
private fun _convertActivityToTranslucentBeforeL(act: Activity) {
  try {
    val classes = Activity::class.java.declaredClasses
    var translucentConversionListenerClazz: Class<*>? = null
    for (clazz in classes) {
      if (clazz.simpleName.contains("TranslucentConversionListener")) {
        translucentConversionListenerClazz = clazz
      }
    }
    val method = Activity::class.java.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz)
    method.isAccessible = true
    method.invoke(act, null)
  } catch (t: Throwable) {
    Log.e(TAG, "Can not call the convertToTranslucent method of Activity")
  }

}

/**
 * Android L之后Activity内部实现改变了，所以反射方法要改变下
 */
private fun _convertActivityToTranslucentAfterL(act: Activity) {
  try {
    val getActivityOptions = Activity::class.java.getDeclaredMethod("getActivityOptions")
    getActivityOptions.isAccessible = true
    val options = getActivityOptions.invoke(act)

    val classes = Activity::class.java.declaredClasses
    var translucentConversionListenerClazz: Class<*>? = null
    for (clazz in classes) {
      if (clazz.simpleName.contains("TranslucentConversionListener")) {
        translucentConversionListenerClazz = clazz
      }
    }
    val convertToTranslucent = Activity::class.java.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz, ActivityOptions::class.java)
    convertToTranslucent.isAccessible = true
    convertToTranslucent.invoke(act, null, options)
  } catch (t: Throwable) {
    Log.e(TAG, "Can not call the convertToTranslucent method of Activity")
  }
}

object Slider {
  fun attachToScreen(act: Activity, edge: SlideEdge, shadow: SlideShadow, l: SlideListener) {
    val decorView: ViewGroup = act.window.decorView as ViewGroup
    val bg: Drawable = decorView.background
    act.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    decorView.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val screenView: View = decorView.getChildAt(0)
    decorView.removeViewAt(0)
    val slideLayout: SlideLayout = SlideLayout(act)
    screenView.background = bg
    slideLayout.addView(screenView)
    decorView.addView(slideLayout, 0)
    slideLayout.edge = edge
    slideLayout.listener = l
    slideLayout.shadow = shadow
    convertActivityFromTranslucent(act)
  }

  fun attachToScreen(act: Activity, edge: SlideEdge) {
    attachToScreen(act, edge, SlideShadow.EDGE, object : SlideListener {
      override fun onSlideStart() {
        Log.d(TAG, "Sliding: ${act.toString()} Start")
        convertActivityToTranslucent(act)
      }

      override fun onSlide(percent: Float, state: SlideState) {
        Log.d(TAG, "Sliding $percent : ${state.toString()}")

      }

      override fun onSlideFinish() {
        Log.d(TAG, "Sliding ${act.toString()} Finish")
        act.finish()
        act.overridePendingTransition(0, 0)
      }
    })
  }
}