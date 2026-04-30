package com.example.habito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey
    val id: String,
    val isoDateTime: String, // ISO 8601 format: "2025-10-04T14:30:00"
    val emoji: String,
    val moodLevel: Int, // 1-5 scale
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
