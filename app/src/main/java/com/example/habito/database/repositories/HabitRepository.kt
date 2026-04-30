package com.example.habito.database.repositories

import android.content.Context
import com.example.habito.data.model.*
import com.example.habito.database.DatabaseManager
import com.example.habito.database.entities.HabitCompletionEntity
import com.example.habito.database.entities.HabitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository private constructor(context: Context) {
    
    private val habitDao = DatabaseManager.getInstance(context).habitDao
    private val habitCompletionDao = DatabaseManager.getInstance(context).habitCompletionDao
    
    companion object {
        @Volatile
        private var INSTANCE: HabitRepository? = null
        
        fun getInstance(context: Context): HabitRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HabitRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    

    
    suspend fun getAllHabits(): List<Habit> = withContext(Dispatchers.IO) {
        habitDao.getAllHabits().map { it.toHabit() }
    }
    
    fun getAllHabitsFlow(): Flow<List<Habit>> {
        return habitDao.getAllHabitsFlow().map { entities ->
            entities.map { it.toHabit() }
        }
    }
    
    suspend fun getHabitById(habitId: String): Habit? = withContext(Dispatchers.IO) {
        habitDao.getHabitById(habitId)?.toHabit()
    }
    
    suspend fun saveHabit(habit: Habit) = withContext(Dispatchers.IO) {
        val habitEntity = habit.toEntity()
        habitDao.insertHabit(habitEntity)
    }
    
    suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        val habitEntity = habit.toEntity()
        habitDao.updateHabit(habitEntity)
    }
    
    suspend fun deleteHabit(habitId: String) = withContext(Dispatchers.IO) {
        habitDao.deleteHabitById(habitId)
    }
    
    suspend fun getHabitsByCategory(category: HabitCategory): List<Habit> = withContext(Dispatchers.IO) {
        habitDao.getHabitsByCategory(category).map { it.toHabit() }
    }
    
    suspend fun getHabitsByTimeOfDay(timeOfDay: TimeOfDay): List<Habit> = withContext(Dispatchers.IO) {
        habitDao.getHabitsByTimeOfDay(timeOfDay).map { it.toHabit() }
    }
    

    suspend fun getCompletionsForDate(date: String): List<HabitCompletion> = withContext(Dispatchers.IO) {
        habitCompletionDao.getCompletionsForDate(date).map { it.toHabitCompletion() }
    }
    
    suspend fun getCompletionForHabit(habitId: String, date: String): HabitCompletion? = withContext(Dispatchers.IO) {
        habitCompletionDao.getCompletionForDate(habitId, date)?.toHabitCompletion()
    }
    
    suspend fun markHabitComplete(habitId: String, date: String, count: Int = 1) = withContext(Dispatchers.IO) {
        val habit = habitDao.getHabitById(habitId)
        val existingCompletion = habitCompletionDao.getCompletionForDate(habitId, date)
        
        val newCount = if (habit?.target == 1) 1 else (existingCompletion?.count ?: 0) + count
        val isCompleted = if (habit?.target == 1) true else newCount >= (habit?.target ?: 1)
        
        if (existingCompletion != null) {
            habitCompletionDao.updateCompletionStatus(habitId, date, newCount, isCompleted)
        } else {
            val completionEntity = HabitCompletionEntity(
                habitId = habitId,
                date = date,
                count = newCount,
                isCompleted = isCompleted,
                timestamp = System.currentTimeMillis()
            )
            habitCompletionDao.insertCompletion(completionEntity)
        }
    }
    
    suspend fun unmarkHabitComplete(habitId: String, date: String) = withContext(Dispatchers.IO) {
        habitCompletionDao.updateCompletionStatus(habitId, date, 0, false)
    }
    
    suspend fun getTotalCompletionCount(habitId: String): Int = withContext(Dispatchers.IO) {
        habitCompletionDao.getTotalCompletionCount(habitId)
    }
    
    suspend fun getStreakDays(habitId: String): Int = withContext(Dispatchers.IO) {
        habitCompletionDao.getStreakDays(habitId)
    }
    

    suspend fun getDailyProgress(date: String): DailyProgress = withContext(Dispatchers.IO) {
        val habits = getAllHabits()
        val completions = getCompletionsForDate(date)
        DailyProgress.calculateProgress(habits, completions, date)
    }
    
    suspend fun getProgressForDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyProgress> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DailyProgress>()
        var currentDate = startDate
        
        while (!currentDate.isAfter(endDate)) {
            val dateString = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            result.add(getDailyProgress(dateString))
            currentDate = currentDate.plusDays(1)
        }
        
        result
    }
    

    suspend fun initializeWithSampleData() = withContext(Dispatchers.IO) {
        if (habitDao.getHabitCount() == 0) {
            val sampleHabits = listOf(
                HabitEntity(
                    id = "habit_water",
                    title = "Drink Water",
                    iconRes = android.R.drawable.ic_menu_add,
                    target = 8,
                    category = HabitCategory.HEALTH,
                    reminderEnabled = true,
                    reminderInterval = 60 * 60 * 1000L
                ),
                HabitEntity(
                    id = "habit_meditate",
                    title = "Meditate",
                    iconRes = android.R.drawable.ic_menu_add,
                    target = 1,
                    category = HabitCategory.MINDFULNESS,
                    timeOfDay = TimeOfDay.MORNING
                ),
                HabitEntity(
                    id = "habit_walk",
                    title = "Take a Walk",
                    iconRes = android.R.drawable.ic_menu_add,
                    target = 1,
                    category = HabitCategory.EXERCISE,
                    timeOfDay = TimeOfDay.ANYTIME
                ),
                HabitEntity(
                    id = "habit_sleep_review",
                    title = "Sleep Quality Review",
                    iconRes = android.R.drawable.ic_menu_add,
                    target = 1,
                    category = HabitCategory.WELLNESS,
                    timeOfDay = TimeOfDay.EVENING
                )
            )
            
            habitDao.insertHabits(sampleHabits)
        }
    }
    

    private fun HabitEntity.toHabit(): Habit {
        return Habit(
            id = this.id,
            title = this.title,
            iconRes = this.iconRes,
            target = this.target,
            reminderEnabled = this.reminderEnabled,
            reminderInterval = this.reminderInterval,
            category = this.category,
            timeOfDay = this.timeOfDay,
            createdAt = this.createdAt
        )
    }
    
    private fun Habit.toEntity(): HabitEntity {
        return HabitEntity(
            id = this.id,
            title = this.title,
            iconRes = this.iconRes,
            target = this.target,
            reminderEnabled = this.reminderEnabled,
            reminderInterval = this.reminderInterval,
            category = this.category,
            timeOfDay = this.timeOfDay,
            createdAt = this.createdAt
        )
    }
    
    private fun HabitCompletionEntity.toHabitCompletion(): HabitCompletion {
        return HabitCompletion(
            habitId = this.habitId,
            date = this.date,
            count = this.count,
            isCompleted = this.isCompleted,
            timestamp = this.timestamp
        )
    }
}
