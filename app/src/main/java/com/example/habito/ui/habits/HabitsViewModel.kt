package com.example.habito.ui.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.data.model.DailyProgress
import com.example.habito.data.model.Habit
import com.example.habito.data.model.HabitCompletion
import com.example.habito.database.repositories.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitWithProgress(
    val habit: Habit,
    val completion: HabitCompletion?,
    val isCompleted: Boolean,
    val progress: Int, // Current count
    val target: Int // Target count
)

class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository.getInstance(application)
    
    private val _habitsWithProgress = MutableStateFlow<List<HabitWithProgress>>(emptyList())
    val habitsWithProgress: StateFlow<List<HabitWithProgress>> = _habitsWithProgress.asStateFlow()
    
    private val _dailyProgress = MutableStateFlow(DailyProgress("", 0, 0, 0f))
    val dailyProgress: StateFlow<DailyProgress> = _dailyProgress.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadHabits() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val habits = repository.getAllHabits()
                val completions = repository.getCompletionsForDate(today)
                
                val habitsWithProgress = habits.map { habit ->
                    val completion = completions.find { it.habitId == habit.id }
                    val isCompleted = completion?.let {
                        if (habit.target == 1) it.isCompleted
                        else it.count >= habit.target
                    } ?: false
                    
                    HabitWithProgress(
                        habit = habit,
                        completion = completion,
                        isCompleted = isCompleted,
                        progress = completion?.count ?: 0,
                        target = habit.target
                    )
                }
                
                _habitsWithProgress.value = habitsWithProgress
                
                // Update daily progress
                val progress = repository.getDailyProgress(today)
                _dailyProgress.value = progress
                
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun markHabitComplete(habitId: String) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                repository.markHabitComplete(habitId, today)
                loadHabits() // Refresh the list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habitId)
                loadHabits() // Refresh the list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}