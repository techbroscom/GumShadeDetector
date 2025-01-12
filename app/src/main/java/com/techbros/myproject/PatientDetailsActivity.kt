package com.techbros.myproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.techbros.myproject.databinding.ActivityPatientDetailsBinding
import com.techbros.myproject.viewModel.PatientDetailsViewModel

class PatientDetailsActivity : AppCompatActivity() {
    private val patientDetailsViewModel: PatientDetailsViewModel by viewModels()
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        val binding: ActivityPatientDetailsBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_patient_details
        )
        binding.lifecycleOwner = this
        binding.viewModel = patientDetailsViewModel

        binding.btnSubmit.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("name", binding.etName.text.toString())
            bundle.putString("age", binding.etAge.text.toString())
            bundle.putString("gender", binding.filledExposedDropdown.text.toString())
            bundle.putString("remarks", binding.etOtherDetails.text.toString())
            // Launch camera to take picture
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, resources.getStringArray(R.array.gender_array))
        binding.filledExposedDropdown.setAdapter(adapter)
    }
}