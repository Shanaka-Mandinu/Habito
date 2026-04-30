package com.example.habito.database.daos

import androidx.room.*
import com.example.habito.database.entities.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    
    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntries(entries: List<MoodEntryEntity>)
    
    // Read
    @Query("SELECT * FROM mood_entries WHERE id = :entryId")
    suspend fun getMoodEntryById(entryId: String): MoodEntryEntity?
    
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    suspend fun getAllMoodEntries(): List<MoodEntryEntity>
    
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoodEntriesFlow(): Flow<List<MoodEntryEntity>>
    
    @Query("SELECT * FROM mood_entries WHERE isoDateTime LIKE :date || '%' LIMIT 1")
    suspend fun getMoodEntryByDate(date: String): MoodEntryEntity?
    
    @Query("SELECT * FROM mood_entries WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getMoodEntriesBetweenTimestamps(startTime: Long, endTime: Long): List<MoodEntryEntity>
    
    @Query("SELECT * FROM mood_entries WHERE moodLevel = :level ORDER BY timestamp DESC")
    suspend fun getMoodEntriesByLevel(level: Int): List<MoodEntryEntity>
    
    @Query("SELECT * FROM mood_entries ORDER BY isoDateTime DESC")
    suspend fun getMoodEntriesSortedByDate(): List<MoodEntryEntity>
    
    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getMoodEntryCount(): Int
    
    @Query("SELECT AVG(moodLevel) FROM mood_entries")
    suspend fun getAverageMoodLevel(): Float?
    
    // Update
    @Update
    suspend fun updateMoodEntry(entry: MoodEntryEntity)
    
    @Query("UPDATE mood_entries SET note = :note WHERE id = :entryId")
    suspend fun updateMoodNote(entryId: String, note: String)
    
    // Delete
    @Delete
    suspend fun deleteMoodEntry(entry: MoodEntryEntity)
    
    @Query("DELETE FROM mood_entries WHERE id = :entryId")
    suspend fun deleteMoodEntryById(entryId: String)
    
    @Query("DELETE FROM mood_entries WHERE isoDateTime LIKE :date || '%'")
    suspend fun deleteMoodEntriesByDate(date: String)
    
    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoodEntries()
}
