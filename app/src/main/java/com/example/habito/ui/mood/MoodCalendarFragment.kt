package com.example.habito.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habito.databinding.FragmentMoodCalendarBinding
import kotlinx.coroutines.launch

class MoodCalendarFragment : Fragment() {
    private var _binding: FragmentMoodCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MoodCalendarAdapter
    private val viewModel: MoodCalendarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MoodCalendarAdapter()
        binding.recyclerViewCalendar.apply {
            adapter = this@MoodCalendarFragment.adapter
            layoutManager = GridLayoutManager(context, 7)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.days.collect { days ->
                adapter.submitList(days)
                val empty = days.isEmpty()
                binding.recyclerViewCalendar.visibility = if (empty) View.GONE else View.VISIBLE
                binding.textViewCalendarEmpty.visibility = if (empty) View.VISIBLE else View.GONE
            }
        }

        viewModel.loadCurrentMonth()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}