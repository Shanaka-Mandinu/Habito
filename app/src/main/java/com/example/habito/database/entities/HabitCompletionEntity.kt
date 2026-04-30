package com.example.habito.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "date"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val habitId: String,
    val date: String,
    val count: Int = 0,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
