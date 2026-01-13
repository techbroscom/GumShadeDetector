package com.techbros.myproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.techbros.myproject.databinding.ActivityLoginBinding
import com.techbros.myproject.viewModel.LoginViewModel

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Using Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this
        binding.viewModel = loginViewModel

        setupThemeToggle()
        setupClickListeners()
        observeLoginState()
    }

    private fun setupThemeToggle() {
        binding.btnThemeToggle.setOnClickListener {
            ThemeManager.toggleTheme(this)
            updateThemeButtonText()
        }
        updateThemeButtonText()
    }

    private fun updateThemeButtonText() {
        val currentMode = ThemeManager.getThemeMode(this)
        binding.btnThemeToggle.text = ThemeManager.getThemeLabel(currentMode)
    }

    private fun setupClickListeners() {
        binding.tvRegister.setOnClickListener {
            Toast.makeText(
                this,
                "Redirecting to Register...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeLoginState() {
        loginViewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is UIState.Loading ->
                    Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

                is UIState.Success -> {
                    Toast.makeText(this, state.data, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                is UIState.Failure ->
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()

                is UIState.Error ->
                    Toast.makeText(this, "An error occurred: ${state.exception.message}", Toast.LENGTH_SHORT).show()

                else -> {}
            }
        })
    }
}