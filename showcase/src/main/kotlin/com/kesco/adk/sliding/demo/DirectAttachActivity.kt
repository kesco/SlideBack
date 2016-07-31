package com.kesco.adk.sliding.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kesco.adk.sliding.SlideEdge
import com.kesco.adk.sliding.Slider

class DirectAttachActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_direct_attach)
    Slider.attachToScreen(this, SlideEdge.LEFT)
  }
}
