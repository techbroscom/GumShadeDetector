package com.techbros.myproject.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.techbros.myproject.UIState

class LoginViewModel : ViewModel() {
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    private val _loginState = MutableLiveData<UIState<String>>()
    val loginState: LiveData<UIState<String>> get() = _loginState

    fun validateInputs(): Boolean {
        return !email.value.isNullOrEmpty() && !password.value.isNullOrEmpty()
    }

    fun login() {
        if (validateInputs()) {
            _loginState.value =
                UIState.Loading // Simulate network request
            val success = mockNetworkRequest(email.value!!, password.value!!)
            if (success) {
                _loginState.value = UIState.Success("Login Successful")
            } else {
                _loginState.value = UIState.Failure("Login Failed")
            }
        } else {
            _loginState.value = UIState.Failure("Please enter all fields")
        }
    }

    private fun mockNetworkRequest(
        email: String,
        password: String
    ): Boolean { // Simulated network request logic
        return email == "test" && password == "1234"
    }
}
