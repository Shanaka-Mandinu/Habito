package com.example.habito.database.daos

import androidx.room.*
import com.example.habito.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    // Read
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash")
    suspend fun authenticateUser(email: String, passwordHash: String): UserEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun userExists(email: String): Boolean
    
    // Update
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET isFirstTimeUser = :isFirstTime WHERE id = :userId")
    suspend fun updateFirstTimeStatus(userId: String, isFirstTime: Boolean)
    
    @Query("UPDATE users SET isOnboardingCompleted = :completed WHERE id = :userId")
    suspend fun updateOnboardingStatus(userId: String, completed: Boolean)
    
    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePassword(userId: String, newPasswordHash: String)
    
    // Delete
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
