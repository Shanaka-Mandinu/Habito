package com.example.habito.ui.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.R
import com.example.habito.data.model.Habit
import com.example.habito.data.model.HabitCategory
import com.example.habito.data.model.TimeOfDay
import com.example.habito.database.repositories.HabitRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AddEditHabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository.getInstance(application)
    
    private val _habit = MutableStateFlow<Habit?>(null)
    val habit: StateFlow<Habit?> = _habit.asStateFlow()
    
    private val _saveEvent = MutableSharedFlow<Boolean>()
    val saveEvent: SharedFlow<Boolean> = _saveEvent.asSharedFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var isEditMode = false
    
    fun loadHabit(habitId: String?) {
        if (habitId != null) {
            isEditMode = true
            viewModelScope.launch {
                try {
                    val habits = repository.getAllHabits()
                    val habit = habits.find { it.id == habitId }
                    _habit.value = habit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            isEditMode = false
            _habit.value = createNewHabit()
        }
    }
    
    private fun createNewHabit(): Habit {
        return Habit(
            id = UUID.randomUUID().toString(),
            title = "",
            iconRes = R.drawable.ic_habit_default,
            target = 1,
            reminderEnabled = false,
            reminderInterval = 60 * 60 * 1000L, // 1 hour
            category = HabitCategory.HEALTH,
            timeOfDay = TimeOfDay.ANYTIME
        )
    }
    
    fun updateTitle(title: String) {
        _habit.value = _habit.value?.copy(title = title)
    }
    
    fun updateTarget(target: Int) {
        _habit.value = _habit.value?.copy(target = target)
    }
    
    fun updateCategory(category: HabitCategory) {
        _habit.value = _habit.value?.copy(category = category)
    }
    
    fun updateTimeOfDay(timeOfDay: TimeOfDay) {
        _habit.value = _habit.value?.copy(timeOfDay = timeOfDay)
    }
    
    fun updateReminderEnabled(enabled: Boolean) {
        _habit.value = _habit.value?.copy(reminderEnabled = enabled)
    }
    
    fun updateReminderInterval(interval: Long) {
        _habit.value = _habit.value?.copy(reminderInterval = interval)
    }
    
    fun saveHabit() {
        val currentHabit = _habit.value
        if (currentHabit != null && currentHabit.title.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    repository.saveHabit(currentHabit)
                    _saveEvent.emit(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _saveEvent.emit(false)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun deleteHabit() {
        val currentHabit = _habit.value
        if (currentHabit != null && isEditMode) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    repository.deleteHabit(currentHabit.id)
                    _saveEvent.emit(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _saveEvent.emit(false)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun isFormValid(): Boolean {
        val currentHabit = _habit.value
        return currentHabit?.title?.isNotBlank() == true
    }
}