package com.kesco.adk.moko.slideback

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup

public object Slider {
    public fun attachToScreen(act: Activity) {
        attachToScreen(act, SlideEdge.LEFT, object : SlideListener {
            override fun onSlide(percent: Float,state:SlideState) {
                Log.d("on Slide", "$percent : ${state.toString()}")
            }

            override fun onSlideFinish() {
                Log.d("on Slide", "${act.toString()} Finish")
                act.finish()
            }
        })
    }

    public fun attachToScreen(act: Activity, edge: SlideEdge, l: SlideListener) {
        val decorView: ViewGroup = act.getWindow().getDecorView() as ViewGroup
        val screenView: View = decorView.getChildAt(0)
        decorView.removeViewAt(0)

        val slideLayout: SlideLayout = SlideLayout(act, screenView)
        slideLayout.addView(screenView)
        decorView.addView(slideLayout, 0)
        slideLayout.slideEdge = edge
        slideLayout.listener = l
    }
}
