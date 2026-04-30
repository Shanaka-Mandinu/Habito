package com.example.habito.data.model

data class HabitCompletion(
    val habitId: String,
    val date: String, // ISO date format: "2025-10-04"
    val count: Int = 0, // How many times completed (for habits with targets > 1)
    val isCompleted: Boolean = false, // For simple yes/no habits
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyProgress(
    val date: String,
    val totalHabits: Int,
    val completedHabits: Int,
    val completionPercentage: Float
) {
    companion object {
        fun calculateProgress(habits: List<Habit>, completions: List<HabitCompletion>, date: String): DailyProgress {
            val totalHabits = habits.size
            val completedCount = habits.count { habit ->
                val completion = completions.find { it.habitId == habit.id && it.date == date }
                completion?.let {
                    if (habit.target == 1) it.isCompleted
                    else it.count >= habit.target
                } ?: false
            }
            val percentage = if (totalHabits > 0) (completedCount.toFloat() / totalHabits) * 100 else 0f
            
            return DailyProgress(date, totalHabits, completedCount, percentage)
        }
    }
}