package com.example.habito.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habito.data.AuthState
import com.example.habito.database.repositories.UserRepository
import com.example.habito.database.repositories.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    suspend fun isFirstLaunch(): Boolean {
        return settingsRepository.isFirstLaunch()
    }

    fun checkAuthenticationStatus() {
        viewModelScope.launch {
            // This will trigger auth state collection
        }
    }
}

class SplashViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(
                UserRepository.getInstance(context),
                SettingsRepository.getInstance(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}