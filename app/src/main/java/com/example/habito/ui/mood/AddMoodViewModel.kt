package com.example.habito.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.data.model.MoodEntry
import com.example.habito.data.model.MoodEmojis
import com.example.habito.database.repositories.MoodRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddMoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MoodRepository.getInstance(application)
    
    private val _selectedEmoji = MutableStateFlow<MoodEmojis.MoodOption?>(null)
    val selectedEmoji: StateFlow<MoodEmojis.MoodOption?> = _selectedEmoji.asStateFlow()
    
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()
    
    private val _saveEvent = MutableSharedFlow<Boolean>()
    val saveEvent: SharedFlow<Boolean> = _saveEvent.asSharedFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun selectEmoji(emoji: MoodEmojis.MoodOption) {
        _selectedEmoji.value = emoji
    }
    
    fun updateNote(note: String) {
        _note.value = note
    }
    
    fun saveMoodEntry() {
        val emoji = _selectedEmoji.value
        if (emoji != null) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val moodEntry = MoodEntry(
                        id = UUID.randomUUID().toString(),
                        isoDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        emoji = emoji.emoji,
                        moodLevel = emoji.level,
                        note = _note.value,
                        timestamp = System.currentTimeMillis()
                    )
                    // Append a new entry (allow multiple moods per day)
                    repository.saveMoodEntry(moodEntry)
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
        return _selectedEmoji.value != null
    }
}