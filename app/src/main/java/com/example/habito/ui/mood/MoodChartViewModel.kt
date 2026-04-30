package com.example.habito.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.database.repositories.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoodChartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MoodRepository.getInstance(application)
    
    private val _chartData = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val chartData: StateFlow<List<Pair<String, Float>>> = _chartData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadMoodData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getWeeklyMoodData()
                _chartData.value = data
            } catch (e: Exception) {
                e.printStackTrace()
                _chartData.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}