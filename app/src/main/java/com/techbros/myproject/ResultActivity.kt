package com.techbros.myproject

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.techbros.myproject.databinding.ActivityResultBinding
import com.techbros.myproject.model.Test
import com.techbros.myproject.viewModel.ResultViewModel

class ResultActivity : BaseActivity() {
    private lateinit var binding: ActivityResultBinding
    private val resultViewModel: ResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        displayResults()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Analysis Results"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun displayResults() {
        val selectedColor = intent.getIntExtra("SELECTED_COLOR", Color.WHITE)
        val rgbValues = intent.getStringExtra("RGB_VALUES") ?: "N/A"
        val labValues = intent.getStringExtra("LAB_VALUES") ?: "N/A"

        binding.colorPreview.setBackgroundColor(selectedColor)
        binding.colorDetailsText.text = "$rgbValues\n$labValues"
    }

    private fun setupClickListeners() {
        binding.previousButton.setOnClickListener { finish() }

        binding.analyzeButton.setOnClickListener {
            val selectedColor = intent.getIntExtra("SELECTED_COLOR", Color.WHITE)
            resultViewModel.analyzeColor(selectedColor)
        }

        binding.saveButton.setOnClickListener {
            resultViewModel.closestShade.value?.let { shade ->
                val test = Test(
                    intent.getStringExtra("name"),
                    intent.getStringExtra("age"),
                    intent.getStringExtra("gender"),
                    intent.getStringExtra("remarks"),
                    shade.name
                )
                resultViewModel.saveResultToFirebase(test)
            }
        }
    }

    private fun observeViewModel() {
        resultViewModel.closestShade.observe(this) { shade ->
            binding.bigValueText.text = shade?.name ?: "No matching shade found"
        }

        resultViewModel.saveResultStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Toast.makeText(this, "Failed to save result.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Close current activity
    }
}
