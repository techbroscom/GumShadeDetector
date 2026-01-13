package com.techbros.myproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before super.onCreate()
        ThemeManager.initializeTheme(this)
        super.onCreate(savedInstanceState)
    }
}
