package com.example.habito.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habito.data.model.MoodEntry
import com.example.habito.databinding.ItemMoodBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MoodAdapter(
    private val onMoodClick: (MoodEntry) -> Unit,
    private val onMoodLongPress: ((MoodEntry) -> Unit)? = null
) : ListAdapter<MoodEntry, MoodAdapter.MoodViewHolder>(MoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = getItem(position)
        holder.bind(moodEntry)
    }

    inner class MoodViewHolder(private val binding: ItemMoodBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(moodEntry: MoodEntry) {
            binding.apply {
                textViewEmoji.text = moodEntry.emoji
                textViewNote.text = moodEntry.note.takeIf { it.isNotEmpty() } ?: "No note"
                
                val dateTime = LocalDateTime.parse(moodEntry.isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                textViewDateTime.text = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
                
                root.setOnClickListener { onMoodClick(moodEntry) }
                root.setOnLongClickListener {
                    onMoodLongPress?.invoke(moodEntry)
                    true
                }
            }
        }
    }
}

class MoodDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
    override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
        return oldItem == newItem
    }
}