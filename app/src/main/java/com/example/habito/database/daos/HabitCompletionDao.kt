package com.example.habito.database.daos

import androidx.room.*
import com.example.habito.database.entities.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    
    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<HabitCompletionEntity>)
    
    // Read
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun getCompletionForDate(habitId: String, date: String): HabitCompletionEntity?
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getCompletionsForHabit(habitId: String): List<HabitCompletionEntity>
    
    @Query("SELECT * FROM habit_completions WHERE date = :date")
    suspend fun getCompletionsForDate(date: String): List<HabitCompletionEntity>
    
    @Query("SELECT * FROM habit_completions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getCompletionsBetweenDates(startDate: String, endDate: String): List<HabitCompletionEntity>
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getHabitCompletionsBetweenDates(habitId: String, startDate: String, endDate: String): List<HabitCompletionEntity>
    
    @Query("SELECT * FROM habit_completions ORDER BY date DESC")
    fun getAllCompletionsFlow(): Flow<List<HabitCompletionEntity>>
    
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND isCompleted = 1")
    suspend fun getTotalCompletionCount(habitId: String): Int
    
    @Query("SELECT COUNT(DISTINCT date) FROM habit_completions WHERE habitId = :habitId AND isCompleted = 1")
    suspend fun getStreakDays(habitId: String): Int
    
    // Update
    @Update
    suspend fun updateCompletion(completion: HabitCompletionEntity)
    
    @Query("UPDATE habit_completions SET count = :count, isCompleted = :isCompleted WHERE habitId = :habitId AND date = :date")
    suspend fun updateCompletionStatus(habitId: String, date: String, count: Int, isCompleted: Boolean)
    
    // Delete
    @Delete
    suspend fun deleteCompletion(completion: HabitCompletionEntity)
    
    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: String)
    
    @Query("DELETE FROM habit_completions WHERE date = :date")
    suspend fun deleteCompletionsForDate(date: String)
    
    @Query("DELETE FROM habit_completions")
    suspend fun deleteAllCompletions()
}
