package com.example.habito.database

import android.content.Context
import com.example.habito.database.daos.*


class DatabaseManager private constructor(context: Context) {
    
    private val database: WellnessDatabase = WellnessDatabase.getInstance(context)
    
    val userDao: UserDao = database.userDao()
    val habitDao: HabitDao = database.habitDao()
    val habitCompletionDao: HabitCompletionDao = database.habitCompletionDao()
    val moodEntryDao: MoodEntryDao = database.moodEntryDao()
    val hydrationRecordDao: HydrationRecordDao = database.hydrationRecordDao()
    val appSettingsDao: AppSettingsDao = database.appSettingsDao()
    
    companion object {
        @Volatile
        private var INSTANCE: DatabaseManager? = null
        
        fun getInstance(context: Context): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
