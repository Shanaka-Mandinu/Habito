package com.example.habito.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habito.R
import com.example.habito.databinding.FragmentMoodBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MoodFragment : Fragment() {
    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoodViewModel by viewModels()
    private lateinit var adapter: MoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFab()
        setupButtons()
        observeViewModel()
        viewModel.loadMoodEntries()
    }
    
    private fun setupRecyclerView() {
        adapter = MoodAdapter(
            onMoodClick = { entry ->
                // For simplicity, reopen AddMoodFragment to add today's mood; editing specific entry requires dedicated UI.
                // Here we'll show a quick toast and plan for future edit screen.
                // TODO: Optional: navigate to an EditMoodFragment with entryId.
            },
            onMoodLongPress = { entry ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete mood")
                    .setMessage("Delete this mood entry?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteMood(entry.id)
                            viewModel.loadMoodEntries()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.recyclerViewMood.apply {
            adapter = this@MoodFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFab() {
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {
            findNavController().navigate(R.id.action_mood_to_add_mood)
        }
    }
    
    private fun setupButtons() {
        // No extra buttons for calendar/trends/export
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moodEntries.collect { entries ->
                adapter.submitList(entries)
                binding.progressBarLoading.visibility = View.GONE
                if (entries.isEmpty()) {
                    binding.textViewEmptyState.visibility = View.VISIBLE
                    binding.recyclerViewMood.visibility = View.GONE
                } else {
                    binding.textViewEmptyState.visibility = View.GONE
                    binding.recyclerViewMood.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}