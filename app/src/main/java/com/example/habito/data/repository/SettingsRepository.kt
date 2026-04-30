package com.example.habito.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.habito.data.model.AppSettings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_SETTINGS = "settings_v1"
        
        @Volatile
        private var INSTANCE: SettingsRepository? = null
        
        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun getSettings(): AppSettings = withContext(Dispatchers.IO) {
        val settingsJson = prefs.getString(KEY_SETTINGS, null)
        if (settingsJson != null) {
            gson.fromJson(settingsJson, AppSettings::class.java) ?: AppSettings()
        } else {
            AppSettings()
        }
    }
    
    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        val settingsJson = gson.toJson(settings)
        prefs.edit().putString(KEY_SETTINGS, settingsJson).apply()
    }
    
    suspend fun updateHydrationSettings(
        enabled: Boolean,
        interval: Long,
        startTime: String,
        endTime: String,
        dailyGoal: Int
    ) = withContext(Dispatchers.IO) {
        val currentSettings = getSettings()
        val updatedSettings = currentSettings.copy(
            hydrationReminderEnabled = enabled,
            hydrationInterval = interval,
            hydrationStartTime = startTime,
            hydrationEndTime = endTime,
            dailyWaterGoal = dailyGoal
        )
        saveSettings(updatedSettings)
    }
    
    suspend fun setFirstLaunchCompleted() = withContext(Dispatchers.IO) {
        val currentSettings = getSettings()
        val updatedSettings = currentSettings.copy(firstLaunch = false)
        saveSettings(updatedSettings)
    }
    
    suspend fun isFirstLaunch(): Boolean = withContext(Dispatchers.IO) {
        getSettings().firstLaunch
    }
    
    suspend fun toggleDarkTheme() = withContext(Dispatchers.IO) {
        val currentSettings = getSettings()
        val updatedSettings = currentSettings.copy(darkThemeEnabled = !currentSettings.darkThemeEnabled)
        saveSettings(updatedSettings)
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val currentSettings = getSettings()
        val updatedSettings = currentSettings.copy(notificationsEnabled = enabled)
        saveSettings(updatedSettings)
    }

    suspend fun toggleNotifications() = withContext(Dispatchers.IO) {
        val currentSettings = getSettings()
        val updatedSettings = currentSettings.copy(notificationsEnabled = !currentSettings.notificationsEnabled)
        saveSettings(updatedSettings)
    }
}