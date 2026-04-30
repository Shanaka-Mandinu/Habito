package com.example.habito.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habito.databinding.FragmentAddMoodBinding
import com.example.habito.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class AddMoodFragment : Fragment() {
    private var _binding: FragmentAddMoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddMoodViewModel by viewModels()
    private lateinit var emojiAdapter: EmojiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Hide global FAB while adding a mood entry
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.visibility = View.GONE

        setupEmojiGrid()
        setupButtons()
        observeViewModel()
    }
    
    private fun setupEmojiGrid() {
        emojiAdapter = EmojiAdapter { emoji ->
            viewModel.selectEmoji(emoji)
            binding.textViewSelectedEmoji.text = emoji.emoji
            binding.textViewSelectedEmoji.visibility = View.VISIBLE
        }
        
        binding.recyclerViewEmojis.apply {
            adapter = emojiAdapter
            layoutManager = GridLayoutManager(context, 5)
        }
        
        emojiAdapter.submitList(com.example.habito.data.model.MoodEmojis.moodOptions)
    }
    
    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            val note = binding.editTextNote.text.toString()
            viewModel.updateNote(note)
            viewModel.saveMoodEntry()
        }
        
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveEvent.collect { success ->
                if (success) {
                    findNavController().navigateUp()
                } else {
                    // Show error message
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBarSaving.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.buttonSave.isEnabled = !isLoading
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedEmoji.collect { emoji ->
                binding.buttonSave.isEnabled = emoji != null && !viewModel.isLoading.value
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restore FAB visibility when leaving Add Mood screen
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.visibility = View.VISIBLE
        _binding = null
    }
}