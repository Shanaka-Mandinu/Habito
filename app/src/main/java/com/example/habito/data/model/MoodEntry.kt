package com.example.habito.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoodEntry(
    val id: String,
    val isoDateTime: String, // ISO 8601 format: "2025-10-04T14:30:00"
    val emoji: String, // Unicode emoji
    val moodLevel: Int, // 1-5 scale for chart purposes
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

// Predefined mood emojis with their corresponding levels
object MoodEmojis {
    data class MoodOption(val emoji: String, val level: Int, val description: String)
    
    // Limit to 5 core moods
    val moodOptions = listOf(
        MoodOption("😄", 5, "Very Happy"),
        MoodOption("😊", 4, "Happy"),
        MoodOption("😐", 3, "Neutral"),
        MoodOption("😔", 2, "Sad"),
        MoodOption("😭", 1, "Very Sad")
    )
    
    fun getMoodLevel(emoji: String): Int {
        return moodOptions.find { it.emoji == emoji }?.level ?: 3
    }
}