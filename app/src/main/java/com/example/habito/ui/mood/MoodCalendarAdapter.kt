package com.example.habito.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habito.databinding.ItemCalendarDayBinding

class MoodCalendarAdapter : ListAdapter<CalendarDay, MoodCalendarAdapter.DayViewHolder>(DayDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: CalendarDay) {
            binding.textViewDayNumber.text = day.date.dayOfMonth.toString()
            binding.textViewEmoji.isVisible = day.entry != null
            binding.textViewEmoji.text = day.entry?.emoji ?: ""
            binding.root.alpha = if (day.inCurrentMonth) 1f else 0.4f
        }
    }
}

class DayDiff : DiffUtil.ItemCallback<CalendarDay>() {
    override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean = oldItem.date == newItem.date
    override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean = oldItem == newItem
}
