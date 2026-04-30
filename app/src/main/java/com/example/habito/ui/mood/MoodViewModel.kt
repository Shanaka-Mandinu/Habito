package com.example.habito.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.data.model.MoodEntry
import com.example.habito.database.repositories.MoodRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MoodRepository.getInstance(application)
    
    private val _moodEntries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val moodEntries: StateFlow<List<MoodEntry>> = _moodEntries.asStateFlow()
    
    private val _exportEvent = MutableSharedFlow<String?>()
    val exportEvent: SharedFlow<String?> = _exportEvent.asSharedFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadMoodEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val entries = repository.getMoodEntriesSortedByDate()
                _moodEntries.value = entries
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun exportMoodSummary() {
        viewModelScope.launch {
            try {
                val summary = repository.getMoodSummaryText()
                _exportEvent.emit(summary)
            } catch (e: Exception) {
                e.printStackTrace()
                _exportEvent.emit(null)
            }
        }
    }

    suspend fun deleteMood(entryId: String) {
        try {
            repository.deleteMoodEntry(entryId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}