package com.example.habito.utils

import android.content.Context
import androidx.work.*
import com.example.habito.database.repositories.SettingsRepository
import com.example.habito.work.HydrationReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class HydrationManager private constructor(private val context: Context) {
    
    companion object {
        private const val HYDRATION_WORK_NAME = "hydration_reminders"
        private const val HYDRATION_ONE_TIME_NAME = "hydration_reminders_one_time"
        
        @Volatile
        private var INSTANCE: HydrationManager? = null
        
        fun getInstance(context: Context): HydrationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HydrationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val settingsRepository = SettingsRepository.getInstance(context)
    private val workManager = WorkManager.getInstance(context)
    
    fun scheduleHydrationReminders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.getSettings()
                
                if (!settings.hydrationReminderEnabled || !settings.notificationsEnabled) {
                    cancelHydrationReminders()
                    return@launch
                }
                
                val intervalMinutesRaw = settings.hydrationInterval / (60 * 1000)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .build()
                
                if (intervalMinutesRaw >= 15) {
                    // Periodic work (>= 15 min)
                    val intervalMinutes = intervalMinutesRaw.toLong()
                    val workRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                        intervalMinutes, TimeUnit.MINUTES
                    )
                        .setConstraints(constraints)
                        .addTag(HYDRATION_WORK_NAME)
                        .build()
                    workManager.enqueueUniquePeriodicWork(
                        HYDRATION_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        workRequest
                    )
                    // Cancel any one-time chain
                    workManager.cancelUniqueWork(HYDRATION_ONE_TIME_NAME)
                } else {
                    // One-time repeating pattern for < 15 min
                    scheduleOneTime(settings.hydrationInterval)
                    // Cancel periodic if any
                    workManager.cancelUniqueWork(HYDRATION_WORK_NAME)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun cancelHydrationReminders() {
        workManager.cancelUniqueWork(HYDRATION_WORK_NAME)
        workManager.cancelUniqueWork(HYDRATION_ONE_TIME_NAME)
    }
    
    fun isScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(HYDRATION_WORK_NAME).get()
        return workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
    
    fun updateSchedule() {
        scheduleHydrationReminders()
    }

    fun scheduleOneTime(delayMillis: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()
        val request = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(HYDRATION_ONE_TIME_NAME)
            .build()
        workManager.enqueueUniqueWork(
            HYDRATION_ONE_TIME_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
    
    private fun isWithinActiveHours(settings: com.example.habito.data.model.AppSettings): Boolean {
        val now = LocalTime.now()
        val startTime = LocalTime.parse(settings.hydrationStartTime, DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = LocalTime.parse(settings.hydrationEndTime, DateTimeFormatter.ofPattern("HH:mm"))
        
        return if (startTime.isBefore(endTime)) {
            // Same day range (e.g., 08:00 to 22:00)
            now.isAfter(startTime) && now.isBefore(endTime)
        } else {
            // Crosses midnight (e.g., 22:00 to 08:00)
            now.isAfter(startTime) || now.isBefore(endTime)
        }
    }
}