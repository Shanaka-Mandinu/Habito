package com.example.habito.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.habito.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "habito_prefs"
        private const val KEY_HABITS = "habits_v1"
        private const val KEY_COMPLETION_PREFIX = "habit_completion_"
        
        @Volatile
        private var INSTANCE: HabitRepository? = null
        
        fun getInstance(context: Context): HabitRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HabitRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun getAllHabits(): List<Habit> = withContext(Dispatchers.IO) {
        val habitsJson = prefs.getString(KEY_HABITS, "[]")
        val type = object : TypeToken<List<Habit>>() {}.type
        gson.fromJson<List<Habit>>(habitsJson, type) ?: emptyList()
    }
    
    suspend fun saveHabit(habit: Habit) = withContext(Dispatchers.IO) {
        val habits = getAllHabits().toMutableList()
        val existingIndex = habits.indexOfFirst { it.id == habit.id }
        if (existingIndex >= 0) {
            habits[existingIndex] = habit
        } else {
            habits.add(habit)
        }
        saveHabits(habits)
    }
    
    suspend fun deleteHabit(habitId: String) = withContext(Dispatchers.IO) {
        val habits = getAllHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
        
        // Also clean up completion data for this habit
        val allKeys = prefs.all.keys.filter { it.startsWith(KEY_COMPLETION_PREFIX) }
        allKeys.forEach { key ->
            val completions = getCompletionsForDate(key.removePrefix(KEY_COMPLETION_PREFIX))
                .filter { it.habitId != habitId }
            saveCompletionsForDate(key.removePrefix(KEY_COMPLETION_PREFIX), completions)
        }
    }
    
    private suspend fun saveHabits(habits: List<Habit>) = withContext(Dispatchers.IO) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, habitsJson).apply()
    }
    
    suspend fun getCompletionsForDate(date: String): List<HabitCompletion> = withContext(Dispatchers.IO) {
        val key = KEY_COMPLETION_PREFIX + date
        val completionsJson = prefs.getString(key, "[]")
        val type = object : TypeToken<List<HabitCompletion>>() {}.type
        gson.fromJson<List<HabitCompletion>>(completionsJson, type) ?: emptyList()
    }
    
    suspend fun saveCompletionsForDate(date: String, completions: List<HabitCompletion>) = withContext(Dispatchers.IO) {
        val key = KEY_COMPLETION_PREFIX + date
        val completionsJson = gson.toJson(completions)
        prefs.edit().putString(key, completionsJson).apply()
    }
    
    suspend fun markHabitComplete(habitId: String, date: String, count: Int = 1) = withContext(Dispatchers.IO) {
        val completions = getCompletionsForDate(date).toMutableList()
        val existingIndex = completions.indexOfFirst { it.habitId == habitId }
        
        val habit = getAllHabits().find { it.id == habitId }
        val newCompletion = if (existingIndex >= 0) {
            val existing = completions[existingIndex]
            existing.copy(
                count = if (habit?.target == 1) 1 else existing.count + count,
                isCompleted = if (habit?.target == 1) true else existing.count + count >= (habit?.target ?: 1),
                timestamp = System.currentTimeMillis()
            )
        } else {
            HabitCompletion(
                habitId = habitId,
                date = date,
                count = if (habit?.target == 1) 1 else count,
                isCompleted = if (habit?.target == 1) true else count >= (habit?.target ?: 1),
                timestamp = System.currentTimeMillis()
            )
        }
        
        if (existingIndex >= 0) {
            completions[existingIndex] = newCompletion
        } else {
            completions.add(newCompletion)
        }
        
        saveCompletionsForDate(date, completions)
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
        if (getAllHabits().isEmpty()) {
            val sampleHabits = listOf(
                Habit(
                    id = "habit_water",
                    title = "Drink Water",
                    iconRes = android.R.drawable.ic_menu_add, // We'll update this with proper icons later
                    target = 8,
                    category = HabitCategory.HEALTH,
                    reminderEnabled = true,
                    reminderInterval = 60 * 60 * 1000L // 1 hour
                ),
                Habit(
                    id = "habit_meditate",
                    title = "Meditate",
                    iconRes = android.R.drawable.ic_menu_add,
                    target = 1,
                    category = HabitCategory.MINDFULNESS,
                    timeOfDay = TimeOfDay.MORNING
                ),
                Habit(
                    id = "habit_walk",
                    title = "Take a Walk",
                    iconRes = android.R.drawable.ic_menu_add,
                    target = 1,
                    category = HabitCategory.EXERCISE,
                    timeOfDay = TimeOfDay.ANYTIME
                ),
                Habit(
                    id = "habit_sleep_review",
                    title = "Sleep Quality Review",
                    iconRes = android.R.drawable.ic_menu_add,
                    target = 1,
                    category = HabitCategory.WELLNESS,
                    timeOfDay = TimeOfDay.EVENING
                )
            )
            
            saveHabits(sampleHabits)
        }
    }
}