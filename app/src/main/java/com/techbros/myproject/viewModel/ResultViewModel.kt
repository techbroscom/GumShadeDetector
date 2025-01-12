package com.techbros.myproject.viewModel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.techbros.myproject.GumShade
import com.techbros.myproject.model.Test
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class ResultViewModel : ViewModel() {

    private val _closestShade = MutableLiveData<GumShade>()
    val closestShade: LiveData<GumShade> get() = _closestShade

    private val _saveResultStatus = MutableLiveData<Boolean>()
    val saveResultStatus: LiveData<Boolean> get() = _saveResultStatus

    fun analyzeColor(selectedColor: Int) {
        val r = Color.red(selectedColor)
        val g = Color.green(selectedColor)
        val b = Color.blue(selectedColor)
        val shade = findClosestShade(r, g, b)
        if (shade != null) {
            _closestShade.value = shade
        } else {
            // Handle the case where no shade is found, e.g., show a message in the UI
            _closestShade.value = null
        }
    }

    private fun findClosestShade(r: Int, g: Int, b: Int): GumShade? {
        val tolerance = 35 // Adjustable tolerance value

        return GumShade.entries.find { shade ->
            val (sr, sg, sb) = shade.getRGB()
            isWithinTolerance(r, g, b, sr, sg, sb, tolerance)
        }
    }

    private fun isWithinTolerance(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int, tolerance: Int): Boolean {
        return abs(r1 - r2) <= tolerance &&
                abs(g1 - g2) <= tolerance &&
                abs(b1 - b2) <= tolerance
    }

    fun saveResultToFirebase(shade: Test) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("results")
        ref.push().setValue(shade)
            .addOnSuccessListener {
                _saveResultStatus.postValue(true) // Save successful
            }
            .addOnFailureListener {
                _saveResultStatus.postValue(false) // Save failed
            }
    }
}

