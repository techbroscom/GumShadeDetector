package com.techbros.myproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.techbros.myproject.databinding.ActivityPatientDetailsBinding
import com.techbros.myproject.viewModel.PatientDetailsViewModel
import java.io.BufferedReader
import java.io.InputStreamReader


class PatientDetailsActivity : BaseActivity() {
    private val patientDetailsViewModel: PatientDetailsViewModel by viewModels()
    private lateinit var binding: ActivityPatientDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_patient_details)
        binding.lifecycleOwner = this
        binding.viewModel = patientDetailsViewModel

        setupToolbar()
        setupGenderDropdown()
        setupWebView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Patient Details"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupGenderDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.gender_array)
        )
        binding.filledExposedDropdown.setAdapter(adapter)
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            addJavascriptInterface(WebAppInterface(this@PatientDetailsActivity, binding), "AndroidInterface")
        }

        val htmlContent = readHtmlFileFromRaw(R.raw.tooth)
        binding.webView.loadDataWithBaseURL("", htmlContent, "text/html", "UTF-8", null)
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name", binding.etName.text.toString())
                putString("age", binding.etAge.text.toString())
                putString("gender", binding.filledExposedDropdown.text.toString())
                putString("remarks", binding.etOtherDetails.text.toString())
            }

            val intent = Intent(this, MainActivity::class.java).apply {
                putExtras(bundle)
            }
            startActivity(intent)
        }

        binding.tvInstructions.setOnClickListener {
            showInstructionsDialog()
        }
    }

    // Function to read the HTML file as a string
    private fun readHtmlFileFromRaw(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = reader.readLine()
        }
        reader.close()
        return stringBuilder.toString()
    }

    private fun showInstructionsDialog() {
        val instructions = """
              - Capture the image in direct sunlight to achieve optimal lighting.
              - Avoid bright-colored surroundings that may distract from the subject.
              - Ensure that any lipstick worn by the patient is removed prior to capturing the image.
              - The gingiva should be clean and free of plaque and debris and moist with saliva.
              - Position the camera at a distance of 2 to 6 feet from the subject to ensure proper framing.
              - Ensure that the device capturing the image is aligned with the gingiva, avoiding any angled shots.
        """.trimIndent()
        MaterialAlertDialogBuilder(this)
            .setTitle("Instructions")
            .setMessage(instructions)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss()
            }.show()
    }

    class WebAppInterface(
        private val context: Context, private val binding: ActivityPatientDetailsBinding
    ) {

        @JavascriptInterface
        fun onElementsSelected(dataKeys: String) {
            // Display a toast with the comma-separated string of selected elements
            binding.etOtherDetails.setText(dataKeys)
        }
    }
}