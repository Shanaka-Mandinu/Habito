package com.example.habito.database.daos

import androidx.room.*
import com.example.habito.database.entities.HabitEntity
import com.example.habito.data.model.HabitCategory
import com.example.habito.data.model.TimeOfDay
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    
    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)
    
    // Read
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): HabitEntity?
    
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    suspend fun getAllHabits(): List<HabitEntity>
    
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabitsFlow(): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getHabitsByCategory(category: HabitCategory): List<HabitEntity>
    
    @Query("SELECT * FROM habits WHERE timeOfDay = :timeOfDay ORDER BY createdAt DESC")
    suspend fun getHabitsByTimeOfDay(timeOfDay: TimeOfDay): List<HabitEntity>
    
    @Query("SELECT * FROM habits WHERE reminderEnabled = 1")
    suspend fun getHabitsWithReminders(): List<HabitEntity>
    
    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int
    
    // Update
    @Update
    suspend fun updateHabit(habit: HabitEntity)
    
    @Query("UPDATE habits SET reminderEnabled = :enabled WHERE id = :habitId")
    suspend fun updateReminderStatus(habitId: String, enabled: Boolean)
    
    @Query("UPDATE habits SET title = :title WHERE id = :habitId")
    suspend fun updateHabitTitle(habitId: String, title: String)
    
    // Delete
    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
    
    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: String)
    
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}
