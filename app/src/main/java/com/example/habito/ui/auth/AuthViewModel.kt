package com.example.habito.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habito.data.AuthState
import com.example.habito.data.LoginRequest
import com.example.habito.data.RegisterRequest
import com.example.habito.database.repositories.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: UserRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    // One-off error events so the same message can be emitted repeatedly
    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvents: SharedFlow<String> = _errorEvents

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""

            val result = authRepository.login(LoginRequest(email, password))
            result.fold(
                onSuccess = {
                    // Auth state will be updated automatically via repository
                },
                onFailure = { exception ->
                    val message = exception.message ?: "Login failed"
                    _errorMessage.value = message
                    _errorEvents.tryEmit(message)
                }
            )

            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""

            val request = RegisterRequest(name, email, password, confirmPassword)
            if (!request.isValid()) {
                _errorMessage.value = "Please check your input"
                _errorEvents.tryEmit("Please check your input")
                _isLoading.value = false
                return@launch
            }

            val result = authRepository.register(request)
            result.fold(
                onSuccess = {
                    // Auth state will be updated automatically via repository
                },
                onFailure = { exception ->
                    val message = exception.message ?: "Registration failed"
                    _errorMessage.value = message
                    _errorEvents.tryEmit(message)
                }
            )

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = ""
    }
}

class AuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                UserRepository.getInstance(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}