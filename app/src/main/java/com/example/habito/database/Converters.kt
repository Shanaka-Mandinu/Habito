package com.example.habito.database

import androidx.room.TypeConverter
import com.example.habito.data.model.HabitCategory
import com.example.habito.data.model.TimeOfDay

class Converters {
    
    @TypeConverter
    fun fromHabitCategory(category: HabitCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toHabitCategory(categoryName: String): HabitCategory {
        return try {
            HabitCategory.valueOf(categoryName)
        } catch (e: IllegalArgumentException) {
            HabitCategory.HEALTH // Default fallback
        }
    }
    
    @TypeConverter
    fun fromTimeOfDay(timeOfDay: TimeOfDay): String {
        return timeOfDay.name
    }
    
    @TypeConverter
    fun toTimeOfDay(timeOfDayName: String): TimeOfDay {
        return try {
            TimeOfDay.valueOf(timeOfDayName)
        } catch (e: IllegalArgumentException) {
            TimeOfDay.ANYTIME // Default fallback
        }
    }
}
