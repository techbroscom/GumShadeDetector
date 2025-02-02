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

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val resultViewModel: ResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the selected color and RGB values from intent
        val selectedColor = intent.getIntExtra("SELECTED_COLOR", Color.WHITE)
        val rgbValues = intent.getStringExtra("RGB_VALUES") ?: "N/A"
        val labValues = intent.getStringExtra("LAB_VALUES") ?: "N/A"

        // Display color and details
        binding.colorPreview.setBackgroundColor(selectedColor)
        binding.colorDetailsText.text = rgbValues+"\n"+labValues

        // Handle button clicks
        binding.previousButton.setOnClickListener { finish() }
        binding.analyzeButton.setOnClickListener {
            // Perform further analysis or move to another activity
            resultViewModel.analyzeColor(selectedColor)
        }

        resultViewModel.closestShade.observe(this) { shade ->
            if (shade != null) {
                binding.bigValueText.text = shade.name // Display the shade name
            } else {
                binding.bigValueText.text = "No matching shade found" // Fallback message
            }
        }

        binding.saveButton.setOnClickListener{
            resultViewModel.closestShade.value?.let { it1 ->
                resultViewModel.saveResultToFirebase(
                    Test(intent.getStringExtra("name"),
                        intent.getStringExtra("age"),
                        intent.getStringExtra("gender"),
                        intent.getStringExtra("remarks"),
                        it1.name)
                )
            }
        }

        // Observe the saveResultStatus LiveData
        resultViewModel.saveResultStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show()
                navigateToHome() // Navigate to HomeActivity
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
