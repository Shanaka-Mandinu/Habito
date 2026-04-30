package com.example.habito.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.habito.data.model.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MoodRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "mood_prefs"
        private const val KEY_MOOD_ENTRIES = "mood_entries_v1"
        
        @Volatile
        private var INSTANCE: MoodRepository? = null
        
        fun getInstance(context: Context): MoodRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MoodRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun getMoodForDate(date: LocalDate): MoodEntry? = withContext(Dispatchers.IO) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        getAllMoodEntries().firstOrNull { it.isoDateTime.startsWith(dateString) }
    }

    // Save one mood per day: replace existing entry for the same date
    suspend fun saveOrReplaceByDate(moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        val entries = getAllMoodEntries().toMutableList()
        val dateString = moodEntry.isoDateTime.substring(0, 10)
        entries.removeAll { it.isoDateTime.startsWith(dateString) }
        entries.add(moodEntry)
        saveMoodEntries(entries)
    }

    suspend fun getAllMoodEntries(): List<MoodEntry> = withContext(Dispatchers.IO) {
        val entriesJson = prefs.getString(KEY_MOOD_ENTRIES, "[]")
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        gson.fromJson<List<MoodEntry>>(entriesJson, type) ?: emptyList()
    }
    
    suspend fun getMoodEntriesSortedByDate(): List<MoodEntry> = withContext(Dispatchers.IO) {
        getAllMoodEntries().sortedByDescending { 
            LocalDateTime.parse(it.isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
    
    suspend fun getMoodEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): List<MoodEntry> = withContext(Dispatchers.IO) {
        getAllMoodEntries().filter { entry ->
            val entryDate = LocalDate.parse(entry.isoDateTime.split("T")[0], DateTimeFormatter.ISO_LOCAL_DATE)
            !entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)
        }.sortedByDescending { 
            LocalDateTime.parse(it.isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
    
    suspend fun getMoodEntriesForDate(date: LocalDate): List<MoodEntry> = withContext(Dispatchers.IO) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        getAllMoodEntries().filter { entry ->
            entry.isoDateTime.startsWith(dateString)
        }.sortedByDescending { 
            LocalDateTime.parse(it.isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
    
    suspend fun saveMoodEntry(moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        val entries = getAllMoodEntries().toMutableList()
        val existingIndex = entries.indexOfFirst { it.id == moodEntry.id }
        if (existingIndex >= 0) {
            entries[existingIndex] = moodEntry
        } else {
            entries.add(moodEntry)
        }
        saveMoodEntries(entries)
    }
    
    suspend fun deleteMoodEntry(entryId: String) = withContext(Dispatchers.IO) {
        val entries = getAllMoodEntries().toMutableList()
        entries.removeAll { it.id == entryId }
        saveMoodEntries(entries)
    }
    
    suspend fun getMoodEntry(entryId: String): MoodEntry? = withContext(Dispatchers.IO) {
        getAllMoodEntries().find { it.id == entryId }
    }
    
    private suspend fun saveMoodEntries(entries: List<MoodEntry>) = withContext(Dispatchers.IO) {
        val entriesJson = gson.toJson(entries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, entriesJson).apply()
    }
    
    suspend fun getMoodSummaryText(): String = withContext(Dispatchers.IO) {
        val entries = getMoodEntriesSortedByDate()
        if (entries.isEmpty()) {
            return@withContext "No mood entries recorded yet."
        }
        
        val last7Days = entries.take(7)
        val summary = StringBuilder()
        summary.append("Mood Summary (Last 7 entries):\n\n")
        
        last7Days.forEach { entry ->
            val dateTime = LocalDateTime.parse(entry.isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
            summary.append("${entry.emoji} $formattedDate")
            if (entry.note.isNotEmpty()) {
                summary.append(" - ${entry.note}")
            }
            summary.append("\n")
        }
        
        // Add some statistics
        val totalEntries = entries.size
        val avgMoodLevel = entries.map { it.moodLevel }.average()
        summary.append("\nTotal entries: $totalEntries")
        summary.append("\nAverage mood level: ${"%.1f".format(avgMoodLevel)}/5")
        
        summary.toString()
    }
    
    suspend fun getWeeklyMoodData(): List<Pair<String, Float>> = withContext(Dispatchers.IO) {
        val sevenDaysAgo = LocalDate.now().minusDays(6)
        val entries = getMoodEntriesForDateRange(sevenDaysAgo, LocalDate.now())
        
        val result = mutableListOf<Pair<String, Float>>()
        for (i in 0..6) {
            val date = sevenDaysAgo.plusDays(i.toLong())
            val dateString = date.format(DateTimeFormatter.ofPattern("MM/dd"))
            val dayEntries = entries.filter { entry ->
                entry.isoDateTime.startsWith(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }
            val avgMood = if (dayEntries.isNotEmpty()) {
                dayEntries.map { it.moodLevel }.average().toFloat()
            } else {
                0f
            }
            result.add(Pair(dateString, avgMood))
        }
        
        result
    }
}