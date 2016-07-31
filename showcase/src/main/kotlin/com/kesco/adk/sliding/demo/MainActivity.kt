package com.kesco.adk.sliding.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
  private lateinit var _btnNew: Button;

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    _btnNew = findViewById(R.id.btn_create) as Button
    _btnNew.setOnClickListener {
      val intent = Intent(this, DirectAttachActivity::class.java)
      startActivity(intent)
    }
  }
}
