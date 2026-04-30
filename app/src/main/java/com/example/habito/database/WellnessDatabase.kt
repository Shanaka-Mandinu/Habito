package com.example.habito.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.habito.database.daos.*
import com.example.habito.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        MoodEntryEntity::class,
        HydrationRecordEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WellnessDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun hydrationRecordDao(): HydrationRecordDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: WellnessDatabase? = null
        
        private const val DATABASE_NAME = "habito_database"
        
        fun getInstance(context: Context): WellnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellnessDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
