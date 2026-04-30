package com.example.habito.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.habito.MainActivity
import com.example.habito.data.AuthState
import com.example.habito.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels { 
        AuthViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // Add text change listeners for validation
        binding.nameEditText.addTextChangedListener { clearNameError() }
        binding.emailEditText.addTextChangedListener { clearEmailError() }
        binding.passwordEditText.addTextChangedListener { clearPasswordError() }
        binding.confirmPasswordEditText.addTextChangedListener { clearConfirmPasswordError() }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.authState.collect { authState ->
                when (authState) {
                    AuthState.AUTHENTICATED, AuthState.FIRST_TIME_USER -> {
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                    else -> {
                        // Stay on register screen
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.registerButton.isEnabled = !isLoading
                binding.registerButton.text = if (isLoading) "Creating Account..." else "Create Account"
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInput(name, email, password, confirmPassword)) {
                viewModel.register(name, email, password, confirmPassword)
            }
        }

        binding.loginTextView.setOnClickListener {
            finish() // Return to login screen
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email format"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun clearNameError() { binding.nameInputLayout.error = null }
    private fun clearEmailError() { binding.emailInputLayout.error = null }
    private fun clearPasswordError() { binding.passwordInputLayout.error = null }
    private fun clearConfirmPasswordError() { binding.confirmPasswordInputLayout.error = null }
}