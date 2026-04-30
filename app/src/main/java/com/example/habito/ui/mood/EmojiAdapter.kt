package com.example.habito.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habito.data.model.MoodEmojis
import com.example.habito.databinding.ItemEmojiBinding

class EmojiAdapter(
    private val onEmojiClick: (MoodEmojis.MoodOption) -> Unit
) : ListAdapter<MoodEmojis.MoodOption, EmojiAdapter.EmojiViewHolder>(EmojiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val binding = ItemEmojiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmojiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emoji = getItem(position)
        holder.bind(emoji)
    }

    inner class EmojiViewHolder(private val binding: ItemEmojiBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(emoji: MoodEmojis.MoodOption) {
            binding.apply {
                textViewEmoji.text = emoji.emoji
                textViewDescription.text = "${emoji.description} (level ${emoji.level})"
                
                root.setOnClickListener {
                    onEmojiClick(emoji)
                }
            }
        }
    }
}

class EmojiDiffCallback : DiffUtil.ItemCallback<MoodEmojis.MoodOption>() {
    override fun areItemsTheSame(oldItem: MoodEmojis.MoodOption, newItem: MoodEmojis.MoodOption): Boolean {
        return oldItem.emoji == newItem.emoji
    }

    override fun areContentsTheSame(oldItem: MoodEmojis.MoodOption, newItem: MoodEmojis.MoodOption): Boolean {
        return oldItem == newItem
    }
}