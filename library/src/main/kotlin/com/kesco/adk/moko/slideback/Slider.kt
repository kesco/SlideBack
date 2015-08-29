package com.kesco.adk.moko.slideback

import android.app.Activity
import android.app.ActivityOptions
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup

public object Slider {
    public fun attachToScreen(act: Activity) {
        attachToScreen(act, SlideEdge.LEFT, object : SlideListener {
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

    public fun attachToScreen(act: Activity, edge: SlideEdge) {
        attachToScreen(act, edge, object : SlideListener {
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

    public fun attachToScreen(act: Activity, edge: SlideEdge, l: SlideListener) {
        act.getWindow().setBackgroundDrawable(ColorDrawable(0))
        val decorView: ViewGroup = act.getWindow().getDecorView() as ViewGroup
        decorView.setBackgroundDrawable(null)
        val screenView: View = decorView.getChildAt(0)
        decorView.removeViewAt(0)
        val slideLayout: SlideLayout = SlideLayout(act, screenView)
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
        val method = javaClass<Activity>().getDeclaredMethod("convertFromTranslucent")
        method.setAccessible(true)
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
        val classes = javaClass<Activity>().getDeclaredClasses()
        var translucentConversionListenerClazz: Class<*>? = null
        for (clazz in classes) {
            if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                translucentConversionListenerClazz = clazz
            }
        }
        val method = javaClass<Activity>().getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz)
        method.setAccessible(true)
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
        val getActivityOptions = javaClass<Activity>().getDeclaredMethod("getActivityOptions")
        getActivityOptions.setAccessible(true)
        val options = getActivityOptions.invoke(act)

        val classes = javaClass<Activity>().getDeclaredClasses()
        var translucentConversionListenerClazz: Class<*>? = null
        for (clazz in classes) {
            if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                translucentConversionListenerClazz = clazz
            }
        }
        val convertToTranslucent = javaClass<Activity>().getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz, javaClass<ActivityOptions>())
        convertToTranslucent.setAccessible(true)
        convertToTranslucent.invoke(act, null, options)
    } catch (t: Throwable) {
        Log.e("Slider", "Can not call the convertToTranslucent method of Activity")
    }
}