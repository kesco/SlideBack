package com.kesco.adk.moko.slideback

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup

public object Slider {

    public fun attachToScreen(act: Activity) {
        attachToScreen(act, SlideEdge.LEFT, SlideShadow.EDGE, object : SlideListener {
            override fun onSlideStart() {
                Log.e("on Slide", "${act.toString()} Start")
                convertActivityToTranslucent(act)
            }

            override fun onSlide(percent: Float, state: SlideState) {
                Log.d("on Slide", "$percent : ${state.toString()}")
            }

            override fun onSlideFinish() {
                Log.d("on Slide", "${act.toString()} Finish")
                act.finish()
                act.overridePendingTransition(0, 0)
            }
        })
    }

    public fun attachToScreen(act: Activity, edge: SlideEdge, shadow: SlideShadow) {
        attachToScreen(act, edge, shadow, object : SlideListener {
            override fun onSlideStart() {
                Log.e("on Slide", "${act.toString()} Start")
                convertActivityToTranslucent(act)
            }

            override fun onSlide(percent: Float, state: SlideState) {
                Log.d("on Slide", "$percent : ${state.toString()}")

            }

            override fun onSlideFinish() {
                Log.d("on Slide", "${act.toString()} Finish")
                act.finish()
                act.overridePendingTransition(0, 0)
            }
        })
    }

    public fun attachToScreen(act: Activity, edge: SlideEdge, slideShadow: SlideShadow, l: SlideListener) {
        act.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val decorView: ViewGroup = act.window.decorView as ViewGroup
        decorView.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val screenView: View = decorView.getChildAt(0)
        decorView.removeViewAt(0)
        val slideLayout: SlideLayout = SlideLayout(act, screenView, slideShadow)
        slideLayout.addView(screenView)
        decorView.addView(slideLayout, 0)
        convertActivityFromTranslucent(act)
        slideLayout.slideEdge = edge
        slideLayout.listener = l
    }
}

/**
 * 参考SwipbackLayout的实现
 */
public fun convertActivityFromTranslucent(act: Activity) {
    try {
        val method = Activity::class.java.getDeclaredMethod("convertFromTranslucent")
        method.isAccessible = true
        method.invoke(act)
    } catch (t: Throwable) {
        Log.e("Slider", "Can not call the convertFromTranslucent method of Activity")
    }
}

public fun convertActivityToTranslucent(act: Activity) {
    if (Build.VERSION.SDK_INT >= 21 /* Build.VERSION_CODES.LOLLIPOP */) {
        convertActivityToTranslucentAfterL(act)
    } else {
        convertActivityToTranslucentBeforeL(act)
    }
}

/**
 * 参考SwipbackLayout的实现
 */
private fun convertActivityToTranslucentBeforeL(act: Activity) {
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
        Log.e("Slider", "Can not call the convertToTranslucent method of Activity")
    }

}

/**
 * Android L之后Activity内部实现改变了，所以反射方法要改变下
 */
private fun convertActivityToTranslucentAfterL(act: Activity) {
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
        Log.e("Slider", "Can not call the convertToTranslucent method of Activity")
    }
}
