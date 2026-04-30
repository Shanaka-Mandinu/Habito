package com.example.habito.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habito.R
import com.example.habito.databinding.FragmentHabitsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar

class HabitsFragment : Fragment() {
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitsViewModel by viewModels()
    private lateinit var adapter: HabitsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupFab()
        observeViewModel()
        observeNavigationResults()
        
        viewModel.loadHabits()
    }
    
    private fun setupUI() {
        // Recycler setup
        adapter = HabitsAdapter(
            onHabitClick = { habit ->
                // Open edit dialog
                val args = Bundle().apply { putString("habitId", habit.id) }
                findNavController().navigate(R.id.editHabitFragment, args)
            },
            onMarkComplete = { habit ->
                viewModel.markHabitComplete(habit.id)
            },
            onHabitLongPress = { habit ->
                Snackbar.make(binding.root, "Delete '${habit.title}'?", Snackbar.LENGTH_LONG)
                    .setAction("Delete") {
                        viewModel.deleteHabit(habit.id)
                    }.show()
            }
        )
        binding.recyclerHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHabits.adapter = adapter

        // Add via FAB (set up in setupFab())
        
        // Default progress
        updateCircularProgress(0)
    }
    
    // Greeting moved to Dashboard
    
    private fun updateCircularProgress(percentage: Int) {
        binding.progressCircle.progress = percentage
        binding.progressPercentage.text = "$percentage%"
    }
    
    private fun setupFab() {
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {
            findNavController().navigate(R.id.action_habits_to_add_habit)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitsWithProgress.collect { habitsWithProgress ->
                adapter.submitList(habitsWithProgress)
                // progress will be updated from dailyProgress collector
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dailyProgress.collect { progress ->
                updateCircularProgress(progress.completionPercentage.toInt())
            }
        }
    }

    private fun observeNavigationResults() {
        val navBackStackEntry = findNavController().currentBackStackEntry
        val handle = navBackStackEntry?.savedStateHandle
        handle?.getLiveData<Boolean>("habit_changed")?.observe(viewLifecycleOwner) { changed ->
            if (changed == true) {
                viewModel.loadHabits()
                handle.set("habit_changed", false) // reset
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}