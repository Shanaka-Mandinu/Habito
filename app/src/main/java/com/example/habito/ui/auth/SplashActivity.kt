package com.example.habito.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.habito.MainActivity
import com.example.habito.data.AuthState
import com.example.habito.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels { 
        SplashViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate splash elements
        animateSplashElements()

        // Observe auth state and navigate accordingly
        lifecycleScope.launch {
            delay(2000) // Show splash for 2 seconds
            
            viewModel.authState.collect { authState ->
                navigateBasedOnAuthState(authState)
            }
        }
    }

    private fun animateSplashElements() {
        binding.apply {
            // Fade in logo
            logoImageView.alpha = 0f
            logoImageView.animate()
                .alpha(1f)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(1000)
                .withEndAction {
                    logoImageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(500)
                        .start()
                }
                .start()

            // Slide up text
            appNameTextView.alpha = 0f
            appNameTextView.translationY = 50f
            appNameTextView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(500)
                .start()

            taglineTextView.alpha = 0f
            taglineTextView.translationY = 30f
            taglineTextView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(700)
                .start()
        }
    }

    private suspend fun navigateBasedOnAuthState(authState: AuthState) {
        val intent = when {
            viewModel.isFirstLaunch() -> {
                Intent(this, OnboardingActivity::class.java)
            }
            authState == AuthState.UNAUTHENTICATED -> {
                Intent(this, LoginActivity::class.java)
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }
        
        startActivity(intent)
        finish()
    }
}