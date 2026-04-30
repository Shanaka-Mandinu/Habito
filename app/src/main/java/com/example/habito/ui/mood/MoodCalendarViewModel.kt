package com.example.habito.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habito.data.model.MoodEntry
import com.example.habito.database.repositories.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    val entry: MoodEntry? = null,
    val inCurrentMonth: Boolean = true
)

class MoodCalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MoodRepository.getInstance(application)

    private val _days = MutableStateFlow<List<CalendarDay>>(emptyList())
    val days: StateFlow<List<CalendarDay>> = _days.asStateFlow()

    fun loadCurrentMonth(reference: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val firstOfMonth = reference.withDayOfMonth(1)
            val lastOfMonth = reference.withDayOfMonth(reference.lengthOfMonth())

            // Build grid starting from Monday (or Sunday) - here Monday
            val start = firstOfMonth.minusDays(((firstOfMonth.dayOfWeek.value + 6) % 7).toLong())
            val end = lastOfMonth.plusDays((7 - (lastOfMonth.dayOfWeek.value % 7)).toLong() % 7)

            val result = mutableListOf<CalendarDay>()
            var cursor = start
            while (!cursor.isAfter(end)) {
                val entry = repository.getMoodForDate(cursor)
                result.add(
                    CalendarDay(
                        date = cursor,
                        entry = entry,
                        inCurrentMonth = cursor.month == reference.month
                    )
                )
                cursor = cursor.plusDays(1)
            }
            _days.value = result
        }
    }
}
