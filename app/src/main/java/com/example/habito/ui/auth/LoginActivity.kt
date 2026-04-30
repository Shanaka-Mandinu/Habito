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
import com.example.habito.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels { 
        AuthViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // Add text change listeners for validation
        binding.emailEditText.addTextChangedListener {
            clearEmailError()
        }
        
        binding.passwordEditText.addTextChangedListener {
            clearPasswordError()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.authState.collect { authState ->
                when (authState) {
                    AuthState.AUTHENTICATED -> {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    AuthState.FIRST_TIME_USER -> {
                        // Handle first time user if needed
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    else -> {
                        // Stay on login screen
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.loginButton.isEnabled = !isLoading
                binding.loginButton.text = if (isLoading) "Logging in..." else "Login"
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Listen to one-off error events so repeated identical errors still show
        lifecycleScope.launch {
            viewModel.errorEvents.collect { error ->
                Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

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

        return isValid
    }

    private fun clearEmailError() {
        binding.emailInputLayout.error = null
    }

    private fun clearPasswordError() {
        binding.passwordInputLayout.error = null
    }
}