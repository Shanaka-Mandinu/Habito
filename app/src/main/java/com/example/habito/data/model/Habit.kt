package com.example.habito.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Habit(
    val id: String,
    val title: String,
    val iconRes: Int,
    val target: Int = 1, // Default target count (e.g., 1 for yes/no habits, 8 for water glasses)
    val reminderEnabled: Boolean = false,
    val reminderInterval: Long = 60 * 60 * 1000L, // 1 hour in milliseconds
    val category: HabitCategory = HabitCategory.HEALTH,
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class HabitCategory {
    HEALTH, WELLNESS, PRODUCTIVITY, EXERCISE, MINDFULNESS, NUTRITION
}

enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING, ANYTIME
}