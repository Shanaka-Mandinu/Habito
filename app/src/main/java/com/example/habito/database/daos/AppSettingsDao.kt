package com.example.habito.database.daos

import androidx.room.*
import com.example.habito.database.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    
    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity): Long
    
    // Read
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettingsEntity?
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>
    
    // Update
    @Update
    suspend fun updateSettings(settings: AppSettingsEntity)
    
    @Query("UPDATE app_settings SET hydrationReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateHydrationReminderEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET hydrationInterval = :interval WHERE id = 1")
    suspend fun updateHydrationInterval(interval: Long)
    
    @Query("UPDATE app_settings SET hydrationStartTime = :startTime, hydrationEndTime = :endTime WHERE id = 1")
    suspend fun updateHydrationTimes(startTime: String, endTime: String)
    
    @Query("UPDATE app_settings SET dailyWaterGoal = :goal WHERE id = 1")
    suspend fun updateDailyWaterGoal(goal: Int)
    
    @Query("UPDATE app_settings SET notificationsEnabled = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET darkThemeEnabled = :enabled WHERE id = 1")
    suspend fun updateDarkThemeEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET firstLaunch = :isFirstLaunch WHERE id = 1")
    suspend fun updateFirstLaunch(isFirstLaunch: Boolean)
    
    // Delete
    @Query("DELETE FROM app_settings")
    suspend fun deleteSettings()
}
