package com.example.habito.data.model

data class AppSettings(
    val hydrationReminderEnabled: Boolean = true,
    val hydrationInterval: Long = 60 * 60 * 1000L, // 1 hour in milliseconds
    val hydrationStartTime: String = "08:00", // 24-hour format
    val hydrationEndTime: String = "22:00", // 24-hour format
    val dailyWaterGoal: Int = 8, // glasses
    val notificationsEnabled: Boolean = false,
    val darkThemeEnabled: Boolean = false,
    val firstLaunch: Boolean = true
)