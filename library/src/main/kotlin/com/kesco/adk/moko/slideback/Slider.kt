package com.kesco.adk.moko.slideback

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup

public object Slider {
    public fun attach(act: Activity) {
        val decorView: ViewGroup = act.getWindow().getDecorView() as ViewGroup
        val screenView: View = decorView.getChildAt(0)
        decorView.removeViewAt(0)

        val slideLayout: SlidLayout = SlidLayout(act, screenView)
        slideLayout.addView(screenView)
        decorView.addView(slideLayout, 0)

        slideLayout.setListener(object :SlideListener {
            override fun onSlide(percent: Float) {
                Log.d("on Slide",percent.toString())
            }

            override fun onSlideFinish() {
                act.finish()
            }
        })
    }
}
