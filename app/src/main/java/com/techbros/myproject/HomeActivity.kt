package com.techbros.myproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.techbros.myproject.adapter.TestAdapter
import com.techbros.myproject.databinding.ActivityHomeBinding
import com.techbros.myproject.viewModel.HomeViewModel

class HomeActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val binding: ActivityHomeBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this
        binding.viewModel = homeViewModel

        val adapter = TestAdapter(emptyList()) { test ->
            TestBottomSheet(test).show(supportFragmentManager, "TestBottomSheet")
        }

        binding.rvTests.layoutManager = LinearLayoutManager(this)
        binding.rvTests.adapter = adapter

        homeViewModel.tests.observe(this) { tests ->
            adapter.updateTests(tests)
        }

        binding.btnStartTest.setOnClickListener {
            val intent = Intent(this, PatientDetailsActivity::class.java)
            startActivity(intent)
        }
        // Handle back press using onBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun showExitConfirmationDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setMessage("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id -> finishAffinity() }
            .setNegativeButton("No") { dialog, id -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }
}
