package com.example.habito.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val joinedDate: Long = System.currentTimeMillis(),
    val isFirstTimeUser: Boolean = true
) : Parcelable

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String
) {
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               email.isNotBlank() && 
               password.length >= 6 && 
               password == confirmPassword &&
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

enum class AuthState {
    UNAUTHENTICATED,
    AUTHENTICATED,
    FIRST_TIME_USER
}