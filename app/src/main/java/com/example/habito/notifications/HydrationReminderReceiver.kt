package com.example.habito.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.habito.MainActivity
import com.example.habito.R
import com.example.habito.database.repositories.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HydrationReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "hydration_reminders"
        const val NOTIFICATION_ID = 1001
        const val ACTION_MARK_WATER = "com.example.habito.MARK_WATER"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_WATER -> {
                markWaterConsumed(context)
            }
            else -> {
                showHydrationNotification(context)
            }
        }
    }
    
    private fun showHydrationNotification(context: Context) {
        if (!hasNotificationPermission(context)) {
            return
        }
        
        createNotificationChannel(context)
        
        // Intent to open the app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent to mark water consumed
        val markWaterIntent = Intent(context, HydrationReminderReceiver::class.java).apply {
            action = ACTION_MARK_WATER
        }
        val markWaterPendingIntent = PendingIntent.getBroadcast(
            context, 0, markWaterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.img)
            .setContentTitle(context.getString(R.string.hydration_reminder_title))
            .setContentText(context.getString(R.string.hydration_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_water_drop,
                context.getString(R.string.mark_water_consumed),
                markWaterPendingIntent
            )
            .build()
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            // Permission denied, handle gracefully
            e.printStackTrace()
        }
    }
    
    private fun markWaterConsumed(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = HabitRepository.getInstance(context)
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // Find the water habit and mark it complete
                val habits = repository.getAllHabits()
                val waterHabit = habits.find { it.title.contains("Water", ignoreCase = true) }
                
                if (waterHabit != null) {
                    repository.markHabitComplete(waterHabit.id, today)
                    
                    // Show confirmation notification
                    showWaterMarkedNotification(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Cancel the original notification
        try {
            with(NotificationManagerCompat.from(context)) {
                cancel(NOTIFICATION_ID)
            }
        } catch (e: SecurityException) {
            // Permission denied, handle gracefully
            e.printStackTrace()
        }
    }
    
    private fun showWaterMarkedNotification(context: Context) {
        if (!hasNotificationPermission(context)) {
            return
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("Water Logged!")
            .setContentText("Great job staying hydrated!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(3000) // Auto-dismiss after 3 seconds
            .build()
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID + 1, notification)
            }
        } catch (e: SecurityException) {
            // Permission denied, handle gracefully
            e.printStackTrace()
        }
    }
    
    private fun hasNotificationPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 13, notifications don't require explicit permission
            true
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.hydration_reminders)
        val descriptionText = "Reminders to drink water throughout the day"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}