package com.example.habito.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.habito.data.AuthState
import com.example.habito.data.LoginRequest
import com.example.habito.data.RegisterRequest
import com.example.habito.data.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import java.util.UUID

class AuthRepository private constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null
        private const val PREF_NAME = "auth_prefs"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_USER_CREDENTIALS = "user_credentials"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                AuthRepository(prefs).also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    
    private val _authState = MutableStateFlow(AuthState.UNAUTHENTICATED)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    // Store user credentials as email -> hashedPassword map
    private val userCredentials: MutableMap<String, String> by lazy {
        val json = sharedPreferences.getString(KEY_USER_CREDENTIALS, "{}")
        val type = object : com.google.gson.reflect.TypeToken<MutableMap<String, String>>() {}.type
        gson.fromJson(json, type) ?: mutableMapOf()
    }

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val userJson = sharedPreferences.getString(KEY_CURRENT_USER, null)
        if (userJson != null) {
            val user = gson.fromJson(userJson, User::class.java)
            _currentUser.value = user
            _authState.value = AuthState.AUTHENTICATED
        } else {
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_FIRST_LAUNCH, false)
            .apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    suspend fun login(request: LoginRequest): Result<User> {
        return try {
            val hashedPassword = hashPassword(request.password)
            val storedPassword = userCredentials[request.email]
            
            if (storedPassword != null && storedPassword == hashedPassword) {
                // Create user object (in real app, you'd fetch from server)
                val user = User(
                    id = UUID.randomUUID().toString(),
                    email = request.email,
                    name = extractNameFromEmail(request.email),
                    isFirstTimeUser = false
                )
                
                saveCurrentUser(user)
                _currentUser.value = user
                _authState.value = AuthState.AUTHENTICATED
                
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<User> {
        return try {
            if (!request.isValid()) {
                return Result.failure(Exception("Invalid registration data"))
            }
            
            if (userCredentials.containsKey(request.email)) {
                return Result.failure(Exception("User already exists"))
            }
            
            val hashedPassword = hashPassword(request.password)
            userCredentials[request.email] = hashedPassword
            saveCredentials()
            
            val user = User(
                id = UUID.randomUUID().toString(),
                email = request.email,
                name = request.name,
                isFirstTimeUser = true
            )
            
            saveCurrentUser(user)
            _currentUser.value = user
            _authState.value = AuthState.FIRST_TIME_USER
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        sharedPreferences.edit()
            .remove(KEY_CURRENT_USER)
            .apply()
        
        _currentUser.value = null
        _authState.value = AuthState.UNAUTHENTICATED
    }

    fun updateUserFirstTimeStatus() {
        val currentUser = _currentUser.value
        if (currentUser != null && currentUser.isFirstTimeUser) {
            val updatedUser = currentUser.copy(isFirstTimeUser = false)
            saveCurrentUser(updatedUser)
            _currentUser.value = updatedUser
            _authState.value = AuthState.AUTHENTICATED
        }
    }

    private fun saveCurrentUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit()
            .putString(KEY_CURRENT_USER, userJson)
            .apply()
    }

    private fun saveCredentials() {
        val credentialsJson = gson.toJson(userCredentials)
        sharedPreferences.edit()
            .putString(KEY_USER_CREDENTIALS, credentialsJson)
            .apply()
    }

    fun changePassword(email: String, oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val stored = userCredentials[email]
            val oldHash = hashPassword(oldPassword)
            if (stored == null || stored != oldHash) {
                return Result.failure(Exception("Current password is incorrect"))
            }
            val newHash = hashPassword(newPassword)
            userCredentials[email] = newHash
            saveCredentials()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun extractNameFromEmail(email: String): String {
        return email.substringBefore("@").replace(".", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
    }
}