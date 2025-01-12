package com.techbros.myproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseApp
import com.techbros.myproject.adapter.TestAdapter
import com.techbros.myproject.databinding.ActivityHomeBinding
import com.techbros.myproject.viewModel.HomeViewModel

class HomeActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val binding: ActivityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this
        binding.viewModel = homeViewModel

        val adapter = TestAdapter(emptyList())
        binding.rvTests.layoutManager = LinearLayoutManager(this)
        binding.rvTests.adapter = adapter

        homeViewModel.tests.observe(this) { tests ->
            adapter.updateTests(tests)
        }

        binding.btnStartTest.setOnClickListener {
            val intent = Intent(this, PatientDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}
