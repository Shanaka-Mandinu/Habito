package com.example.habito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val hydrationReminderEnabled: Boolean = true,
    val hydrationInterval: Long = 60 * 60 * 1000L,
    val hydrationStartTime: String = "08:00",
    val hydrationEndTime: String = "22:00",
    val dailyWaterGoal: Int = 8,
    val notificationsEnabled: Boolean = false,
    val darkThemeEnabled: Boolean = false,
    val firstLaunch: Boolean = true
)
