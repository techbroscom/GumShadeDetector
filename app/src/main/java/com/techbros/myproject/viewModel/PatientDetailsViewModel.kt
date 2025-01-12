package com.techbros.myproject.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.techbros.myproject.UIState
import com.techbros.myproject.model.Test

class PatientDetailsViewModel : ViewModel() {
    val name = MutableLiveData<String>()
    val age = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    val otherDetails = MutableLiveData<String>()

    private val _photoPath = MutableLiveData<String>()
    val photoPath: LiveData<String> get() = _photoPath

}
