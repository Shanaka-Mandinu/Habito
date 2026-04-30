package com.example.habito.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.data.model.DailyProgress
import com.example.habito.database.repositories.HabitRepository
import com.example.habito.database.repositories.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val habitRepository = HabitRepository.getInstance(application)
    private val moodRepository = MoodRepository.getInstance(application)

    private val _dailyProgress = MutableStateFlow(DailyProgress("", 0, 0, 0f))
    val dailyProgress: StateFlow<DailyProgress> = _dailyProgress.asStateFlow()

    private val _weeklyMood = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val weeklyMood: StateFlow<List<Pair<String, Float>>> = _weeklyMood.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                _dailyProgress.value = habitRepository.getDailyProgress(today)
                _weeklyMood.value = moodRepository.getWeeklyMoodData()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
