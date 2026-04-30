package com.example.habito.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.data.model.AppSettings
import com.example.habito.database.repositories.SettingsRepository
import com.example.habito.database.repositories.UserRepository
import com.example.habito.utils.HydrationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository.getInstance(application)
    private val hydrationManager = HydrationManager.getInstance(application)
    private val authRepository = UserRepository.getInstance(application)
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _events = MutableStateFlow<String?>(null)
    val events: StateFlow<String?> = _events.asStateFlow()
    
    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val settings = repository.getSettings()
                _settings.value = settings
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateHydrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val current = _settings.value
                // If enabling hydration but notifications are off, block and revert
                if (enabled && !current.notificationsEnabled) {
                    // Re-save with hydrationReminderEnabled = false
                    val reverted = current.copy(hydrationReminderEnabled = false)
                    repository.saveSettings(reverted)
                    _settings.value = reverted
                    _events.value = "notify_enable_notifications_first"
                    return@launch
                }

                val updated = current.copy(hydrationReminderEnabled = enabled)
                repository.saveSettings(updated)
                _settings.value = updated

                // Update hydration reminders
                if (enabled) {
                    hydrationManager.scheduleHydrationReminders()
                } else {
                    hydrationManager.cancelHydrationReminders()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun toggleNotifications() {
        viewModelScope.launch {
            try {
                repository.toggleNotifications()
                loadSettings() // Reload to get updated value
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun toggleDarkTheme() {
        viewModelScope.launch {
            try {
                repository.toggleDarkTheme()
                loadSettings() // Reload to get updated value
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (!enabled) {
                    // Turn off notifications AND hydration reminders together
                    val current = _settings.value
                    val updated = current.copy(
                        notificationsEnabled = false,
                        hydrationReminderEnabled = false
                    )
                    repository.saveSettings(updated)
                    _settings.value = updated
                    // Ensure any scheduled hydration jobs are canceled
                    hydrationManager.cancelHydrationReminders()
                } else {
                    // Enabling notifications just flips the flag
                    repository.setNotificationsEnabled(true)
                    // Refresh local state
                    val refreshed = repository.getSettings()
                    _settings.value = refreshed
                    // If hydration reminders were already enabled, ensure schedule is active
                    if (refreshed.hydrationReminderEnabled) {
                        hydrationManager.scheduleHydrationReminders()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updateWaterGoal(goal: Int) {
        viewModelScope.launch {
            try {
                val current = _settings.value
                val updated = current.copy(dailyWaterGoal = goal)
                repository.saveSettings(updated)
                _settings.value = updated
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updateHydrationTimes(startTime: String, endTime: String) {
        viewModelScope.launch {
            try {
                val current = _settings.value
                val updated = current.copy(
                    hydrationStartTime = startTime,
                    hydrationEndTime = endTime
                )
                repository.saveSettings(updated)
                _settings.value = updated
                
                // Update schedule if reminders are enabled
                if (current.hydrationReminderEnabled) {
                    hydrationManager.updateSchedule()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updateHydrationInterval(intervalHours: Float) {
        viewModelScope.launch {
            try {
                val intervalMillis = (intervalHours * 60 * 60 * 1000).toLong()
                val current = _settings.value
                val updated = current.copy(hydrationInterval = intervalMillis)
                repository.saveSettings(updated)
                _settings.value = updated
                
                // Update schedule if reminders are enabled
                if (current.hydrationReminderEnabled) {
                    hydrationManager.updateSchedule()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _events.value = "logged_out"
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser.value
                if (user == null) {
                    _events.value = "not_logged_in"
                    return@launch
                }
                val result = authRepository.changePassword(user.email, oldPassword, newPassword)
                if (result.isSuccess) {
                    _events.value = "password_changed"
                } else {
                    _events.value = "password_change_error:${result.exceptionOrNull()?.message ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.value = "password_change_error:${e.message ?: "Unknown error"}"
            }
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}