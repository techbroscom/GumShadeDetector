package com.techbros.myproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.techbros.myproject.adapter.TestAdapter
import com.techbros.myproject.databinding.ActivityHomeBinding
import com.techbros.myproject.viewModel.HomeViewModel

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this
        binding.viewModel = homeViewModel

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        setupBackPressHandler()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "G Shade Wizard"
            setDisplayShowTitleEnabled(true)
        }

        binding.btnThemeToggle.setOnClickListener {
            ThemeManager.toggleTheme(this)
            updateThemeButtonIcon()
        }
        updateThemeButtonIcon()
    }

    private fun updateThemeButtonIcon() {
        val currentMode = ThemeManager.getThemeMode(this)
        val iconRes = when (currentMode) {
            ThemeManager.THEME_LIGHT -> android.R.drawable.ic_menu_day
            ThemeManager.THEME_DARK -> android.R.drawable.ic_menu_night
            else -> android.R.drawable.ic_menu_today
        }
        // Icon will be handled by the theme toggle button in layout
    }

    private fun setupRecyclerView() {
        val adapter = TestAdapter(emptyList()) { test ->
            TestBottomSheet(test).show(supportFragmentManager, "TestBottomSheet")
        }

        binding.rvTests.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            this.adapter = adapter
        }

        homeViewModel.tests.observe(this) { tests ->
            adapter.updateTests(tests)
        }
    }

    private fun setupClickListeners() {
        binding.btnStartTest.setOnClickListener {
            val intent = Intent(this, PatientDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> finishAffinity() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
