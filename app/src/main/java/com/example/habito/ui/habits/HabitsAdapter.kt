package com.example.habito.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habito.R
import com.example.habito.data.model.Habit
import com.example.habito.databinding.ItemHabitBinding

class HabitsAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onMarkComplete: (Habit) -> Unit,
    private val onHabitLongPress: ((Habit) -> Unit)? = null
) : ListAdapter<HabitWithProgress, HabitsAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habitWithProgress = getItem(position)
        holder.bind(habitWithProgress)
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(habitWithProgress: HabitWithProgress) {
            val habit = habitWithProgress.habit
            
            binding.apply {
                textViewHabitTitle.text = habit.title
                textViewHabitCategory.text = habit.category.name
                imageViewHabitIcon.setImageResource(habit.iconRes)
                
                // Progress display
                if (habit.target == 1) {
                    // Simple yes/no habit
                    textViewProgress.text = if (habitWithProgress.isCompleted) "✓ Completed" else "Not completed"
                    progressBarHabit.progress = if (habitWithProgress.isCompleted) 100 else 0
                    progressBarHabit.max = 100
                } else {
                    // Count-based habit
                    textViewProgress.text = "${habitWithProgress.progress}/${habitWithProgress.target}"
                    progressBarHabit.progress = habitWithProgress.progress
                    progressBarHabit.max = habitWithProgress.target
                }
                
                // Mark complete button
                buttonMarkComplete.apply {
                    text = if (habitWithProgress.isCompleted) "Completed" else "Mark Done"

                    // Disable when completed for all habits
                    isEnabled = !habitWithProgress.isCompleted
                    
                    setOnClickListener {
                        onMarkComplete(habit)
                    }
                }
                
                // Card click to edit
                root.setOnClickListener { onHabitClick(habit) }
                // Long press to delete
                root.setOnLongClickListener {
                    onHabitLongPress?.invoke(habit)
                    true
                }
                
                // Time of day indicator
                textViewTimeOfDay.text = habit.timeOfDay.name
                
//                // Reminder indicator
//                imageViewReminderEnabled.setImageResource(
//                    if (habit.reminderEnabled) R.drawable.ic_notifications_active
//                    else R.drawable.ic_notifications_off
//                )
            }
        }
    }
}

class HabitDiffCallback : DiffUtil.ItemCallback<HabitWithProgress>() {
    override fun areItemsTheSame(oldItem: HabitWithProgress, newItem: HabitWithProgress): Boolean {
        return oldItem.habit.id == newItem.habit.id
    }

    override fun areContentsTheSame(oldItem: HabitWithProgress, newItem: HabitWithProgress): Boolean {
        return oldItem == newItem
    }
}