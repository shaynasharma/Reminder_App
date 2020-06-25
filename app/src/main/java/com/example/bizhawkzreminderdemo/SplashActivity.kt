package com.example.bizhawkzreminderdemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)
    val rootlayout = findViewById<View>(R.id.rootlayout)
    rootlayout.animate().translationY(-2100f).setDuration(800).startDelay = 800
    Handler().postDelayed({ navigateToHome() }, 2000)
  }

  private fun navigateToHome() {
    finish()
    overridePendingTransition(0, 0)
    val homeIntent = Intent(this, MainActivity::class.java)
    homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(homeIntent)
  }
}