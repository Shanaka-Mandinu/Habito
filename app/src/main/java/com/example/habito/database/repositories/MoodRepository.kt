package com.example.habito.database.repositories

import android.content.Context
import com.example.habito.data.model.MoodEntry
import com.example.habito.database.DatabaseManager
import com.example.habito.database.entities.MoodEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MoodRepository private constructor(context: Context) {
    
    private val moodEntryDao = DatabaseManager.getInstance(context).moodEntryDao
    
    companion object {
        @Volatile
        private var INSTANCE: MoodRepository? = null
        
        fun getInstance(context: Context): MoodRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MoodRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    

    suspend fun getAllMoodEntries(): List<MoodEntry> = withContext(Dispatchers.IO) {
        moodEntryDao.getAllMoodEntries().map { it.toMoodEntry() }
    }
    
    fun getAllMoodEntriesFlow(): Flow<List<MoodEntry>> {
        return moodEntryDao.getAllMoodEntriesFlow().map { entities ->
            entities.map { it.toMoodEntry() }
        }
    }
    
    suspend fun getMoodEntry(entryId: String): MoodEntry? = withContext(Dispatchers.IO) {
        moodEntryDao.getMoodEntryById(entryId)?.toMoodEntry()
    }
    
    suspend fun saveMoodEntry(moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        val entity = moodEntry.toEntity()
        moodEntryDao.insertMoodEntry(entity)
    }
    
    suspend fun updateMoodEntry(moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        val entity = moodEntry.toEntity()
        moodEntryDao.updateMoodEntry(entity)
    }
    
    suspend fun deleteMoodEntry(entryId: String) = withContext(Dispatchers.IO) {
        moodEntryDao.deleteMoodEntryById(entryId)
    }
    

    suspend fun getMoodForDate(date: LocalDate): MoodEntry? = withContext(Dispatchers.IO) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        moodEntryDao.getMoodEntryByDate(dateString)?.toMoodEntry()
    }
    
    suspend fun getMoodEntriesSortedByDate(): List<MoodEntry> = withContext(Dispatchers.IO) {
        moodEntryDao.getMoodEntriesSortedByDate().map { it.toMoodEntry() }
    }
    
    suspend fun getMoodEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): List<MoodEntry> = withContext(Dispatchers.IO) {
        val startTimestamp = startDate.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        
        moodEntryDao.getMoodEntriesBetweenTimestamps(startTimestamp, endTimestamp)
            .map { it.toMoodEntry() }
    }
    
    suspend fun getMoodEntriesForDate(date: LocalDate): List<MoodEntry> = withContext(Dispatchers.IO) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        getAllMoodEntries().filter { entry ->
            entry.isoDateTime.startsWith(dateString)
        }.sortedByDescending { 
            LocalDateTime.parse(it.isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
    
    suspend fun saveOrReplaceByDate(moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        val dateString = moodEntry.isoDateTime.substring(0, 10)
        moodEntryDao.deleteMoodEntriesByDate(dateString)
        val entity = moodEntry.toEntity()
        moodEntryDao.insertMoodEntry(entity)
    }
    

    suspend fun getAverageMoodLevel(): Float = withContext(Dispatchers.IO) {
        moodEntryDao.getAverageMoodLevel() ?: 0f
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
        
        val totalEntries = moodEntryDao.getMoodEntryCount()
        val avgMoodLevel = getAverageMoodLevel()
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
    

    private fun MoodEntryEntity.toMoodEntry(): MoodEntry {
        return MoodEntry(
            id = this.id,
            isoDateTime = this.isoDateTime,
            emoji = this.emoji,
            moodLevel = this.moodLevel,
            note = this.note,
            timestamp = this.timestamp
        )
    }
    
    private fun MoodEntry.toEntity(): MoodEntryEntity {
        return MoodEntryEntity(
            id = this.id,
            isoDateTime = this.isoDateTime,
            emoji = this.emoji,
            moodLevel = this.moodLevel,
            note = this.note,
            timestamp = this.timestamp
        )
    }
}
