package com.example.habito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.habito.data.model.HabitCategory
import com.example.habito.data.model.TimeOfDay

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val iconRes: Int,
    val target: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderInterval: Long = 60 * 60 * 1000L,
    val category: HabitCategory = HabitCategory.HEALTH,
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val createdAt: Long = System.currentTimeMillis()
)
