package com.example.habito.database.repositories

import android.content.Context
import com.example.habito.data.model.AppSettings
import com.example.habito.database.DatabaseManager
import com.example.habito.database.entities.AppSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepository private constructor(context: Context) {
    
    private val appSettingsDao = DatabaseManager.getInstance(context).appSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null
        
        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    

    suspend fun getSettings(): AppSettings = withContext(Dispatchers.IO) {
        val settingsEntity = appSettingsDao.getSettings()
        if (settingsEntity != null) {
            settingsEntity.toAppSettings()
        } else {
            val defaultSettings = AppSettings()
            saveSettings(defaultSettings)
            defaultSettings
        }
    }
    
    fun getSettingsFlow(): Flow<AppSettings> {
        return appSettingsDao.getSettingsFlow().map { entity ->
            entity?.toAppSettings() ?: AppSettings()
        }
    }
    
    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        val entity = settings.toEntity()
        appSettingsDao.insertSettings(entity)
    }
    
    suspend fun updateSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        val entity = settings.toEntity()
        appSettingsDao.updateSettings(entity)
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
    
    suspend fun setHydrationReminderEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettingsDao.updateHydrationReminderEnabled(enabled)
    }
    
    suspend fun setDailyWaterGoal(goal: Int) = withContext(Dispatchers.IO) {
        appSettingsDao.updateDailyWaterGoal(goal)
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettingsDao.updateNotificationsEnabled(enabled)
    }
    
    suspend fun toggleNotifications() = withContext(Dispatchers.IO) {
        val currentSettings = getSettings()
        val updatedSettings = currentSettings.copy(notificationsEnabled = !currentSettings.notificationsEnabled)
        saveSettings(updatedSettings)
    }
    
    suspend fun setDarkThemeEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettingsDao.updateDarkThemeEnabled(enabled)
    }
    
    suspend fun toggleDarkTheme() = withContext(Dispatchers.IO) {
        val currentSettings = getSettings()
        val updatedSettings = currentSettings.copy(darkThemeEnabled = !currentSettings.darkThemeEnabled)
        saveSettings(updatedSettings)
    }
    
    suspend fun setFirstLaunchCompleted() = withContext(Dispatchers.IO) {
        appSettingsDao.updateFirstLaunch(false)
    }
    
    suspend fun isFirstLaunch(): Boolean = withContext(Dispatchers.IO) {
        getSettings().firstLaunch
    }
    
    // ========== EXTENSION FUNCTIONS ==========
    
    private fun AppSettingsEntity.toAppSettings(): AppSettings {
        return AppSettings(
            hydrationReminderEnabled = this.hydrationReminderEnabled,
            hydrationInterval = this.hydrationInterval,
            hydrationStartTime = this.hydrationStartTime,
            hydrationEndTime = this.hydrationEndTime,
            dailyWaterGoal = this.dailyWaterGoal,
            notificationsEnabled = this.notificationsEnabled,
            darkThemeEnabled = this.darkThemeEnabled,
            firstLaunch = this.firstLaunch
        )
    }
    
    private fun AppSettings.toEntity(): AppSettingsEntity {
        return AppSettingsEntity(
            id = 1, // Always use ID = 1 for singleton settings
            hydrationReminderEnabled = this.hydrationReminderEnabled,
            hydrationInterval = this.hydrationInterval,
            hydrationStartTime = this.hydrationStartTime,
            hydrationEndTime = this.hydrationEndTime,
            dailyWaterGoal = this.dailyWaterGoal,
            notificationsEnabled = this.notificationsEnabled,
            darkThemeEnabled = this.darkThemeEnabled,
            firstLaunch = this.firstLaunch
        )
    }
}
