package com.example.habito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val passwordHash: String,
    val profileImageUrl: String? = null,
    val joinedDate: Long = System.currentTimeMillis(),
    val isFirstTimeUser: Boolean = true,
    val isOnboardingCompleted: Boolean = false
)
