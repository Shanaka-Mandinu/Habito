package com.example.habito.work

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.habito.notifications.HydrationReminderReceiver
import com.example.habito.utils.HydrationManager

class HydrationReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        return try {
            // Send broadcast to show notification
            val intent = Intent(applicationContext, HydrationReminderReceiver::class.java)
            applicationContext.sendBroadcast(intent)
            // Re-schedule next run if needed (handles <15 min intervals via one-time work)
            HydrationManager.getInstance(applicationContext).updateSchedule()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}