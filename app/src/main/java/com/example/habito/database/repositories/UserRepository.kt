package com.example.habito.database.repositories

import android.content.Context
import com.example.habito.data.AuthState
import com.example.habito.data.LoginRequest
import com.example.habito.data.RegisterRequest
import com.example.habito.data.User
import com.example.habito.database.DatabaseManager
import com.example.habito.database.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class UserRepository private constructor(context: Context) {
    
    private val userDao = DatabaseManager.getInstance(context).userDao
    
    private val _authState = MutableStateFlow(AuthState.UNAUTHENTICATED)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null
        
        fun getInstance(context: Context): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadCurrentUser()
        }
    }
    
    private suspend fun loadCurrentUser() = withContext(Dispatchers.IO) {
        val userEntity = userDao.getCurrentUser()
        if (userEntity != null) {
            val user = userEntity.toUser()
            _currentUser.value = user
            _authState.value = if (user.isFirstTimeUser) {
                AuthState.FIRST_TIME_USER
            } else {
                AuthState.AUTHENTICATED
            }
        } else {
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
    
    suspend fun login(request: LoginRequest): Result<User> = withContext(Dispatchers.IO) {
        try {
            val hashedPassword = hashPassword(request.password)
            val userEntity = userDao.authenticateUser(request.email, hashedPassword)
            
            if (userEntity != null) {
                val user = userEntity.toUser()
                _currentUser.value = user
                _authState.value = if (user.isFirstTimeUser) {
                    AuthState.FIRST_TIME_USER
                } else {
                    AuthState.AUTHENTICATED
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(request: RegisterRequest): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (!request.isValid()) {
                return@withContext Result.failure(Exception("Invalid registration data"))
            }
            
            if (userDao.userExists(request.email)) {
                return@withContext Result.failure(Exception("User already exists"))
            }
            
            val hashedPassword = hashPassword(request.password)
            val userId = UUID.randomUUID().toString()
            
            val userEntity = UserEntity(
                id = userId,
                email = request.email,
                name = request.name,
                passwordHash = hashedPassword,
                isFirstTimeUser = true,
                isOnboardingCompleted = false
            )
            
            userDao.insertUser(userEntity)
            
            val user = userEntity.toUser()
            _currentUser.value = user
            _authState.value = AuthState.FIRST_TIME_USER
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() = withContext(Dispatchers.IO) {
        _currentUser.value = null
        _authState.value = AuthState.UNAUTHENTICATED
    }
    
    suspend fun updateUserFirstTimeStatus() = withContext(Dispatchers.IO) {
        val currentUser = _currentUser.value
        if (currentUser != null && currentUser.isFirstTimeUser) {
            userDao.updateFirstTimeStatus(currentUser.id, false)
            val updatedUser = currentUser.copy(isFirstTimeUser = false)
            _currentUser.value = updatedUser
            _authState.value = AuthState.AUTHENTICATED
        }
    }
    
    suspend fun setOnboardingCompleted() = withContext(Dispatchers.IO) {
        val currentUser = _currentUser.value
        if (currentUser != null) {
            userDao.updateOnboardingStatus(currentUser.id, true)
        }
    }
    
    suspend fun isOnboardingCompleted(): Boolean = withContext(Dispatchers.IO) {
        val userEntity = userDao.getCurrentUser()
        userEntity?.isOnboardingCompleted ?: false
    }
    
    suspend fun changePassword(email: String, oldPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val oldHash = hashPassword(oldPassword)
            val userEntity = userDao.authenticateUser(email, oldHash)
            
            if (userEntity == null) {
                return@withContext Result.failure(Exception("Current password is incorrect"))
            }
            
            val newHash = hashPassword(newPassword)
            userDao.updatePassword(userEntity.id, newHash)
            
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
    
    // Extension function to convert UserEntity to User
    private fun UserEntity.toUser(): User {
        return User(
            id = this.id,
            email = this.email,
            name = this.name,
            profileImageUrl = this.profileImageUrl,
            joinedDate = this.joinedDate,
            isFirstTimeUser = this.isFirstTimeUser
        )
    }
}
