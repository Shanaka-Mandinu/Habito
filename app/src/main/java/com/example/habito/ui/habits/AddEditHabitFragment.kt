package com.example.habito.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habito.R
import com.example.habito.data.model.HabitCategory
import com.example.habito.data.model.TimeOfDay
import com.example.habito.databinding.FragmentAddEditHabitBinding
import kotlinx.coroutines.launch

class AddEditHabitFragment : DialogFragment() {
    private var _binding: FragmentAddEditHabitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddEditHabitViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSpinners()
        setupButtons()
        observeViewModel()
        
        // Load habit if editing, otherwise create new habit
        val habitId = arguments?.getString("habitId")
        viewModel.loadHabit(habitId)
    }
    
    override fun onStart() {
        super.onStart()
        // Make dialog fill width
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun setupSpinners() {
        // Category spinner
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            HabitCategory.values().map { it.name }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCategory.adapter = categoryAdapter
        
        // Time of day spinner
        val timeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            TimeOfDay.values().map { it.name }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerTimeOfDay.adapter = timeAdapter
    }
    
    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            saveHabit()
        }
        
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.buttonDelete.setOnClickListener {
            viewModel.deleteHabit()
        }
        
//        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
//            viewModel.updateReminderEnabled(isChecked)
//        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habit.collect { habit ->
                habit?.let {
                    binding.editTextTitle.setText(it.title)
                    binding.editTextTarget.setText(it.target.toString())
                    binding.spinnerCategory.setSelection(it.category.ordinal)
                    binding.spinnerTimeOfDay.setSelection(it.timeOfDay.ordinal)
                    //binding.switchReminder.isChecked = it.reminderEnabled
                    
                    // Show delete button only in edit mode
                    binding.buttonDelete.visibility = if (arguments?.getString("habitId") != null) View.VISIBLE else View.GONE
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveEvent.collect { success ->
                if (success) {
                    // Notify previous destination to refresh
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("habit_changed", true)
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

        // Also listen for saveEvent after delete calls to refresh parent
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveEvent.collect { success ->
                if (success) {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("habit_changed", true)
                }
            }
        }
    }
    
    private fun saveHabit() {
        // Update habit with current form values
        val title = binding.editTextTitle.text.toString()
        val target = binding.editTextTarget.text.toString().toIntOrNull() ?: 1
        val category = HabitCategory.values()[binding.spinnerCategory.selectedItemPosition]
        val timeOfDay = TimeOfDay.values()[binding.spinnerTimeOfDay.selectedItemPosition]
        
        viewModel.updateTitle(title)
        viewModel.updateTarget(target)
        viewModel.updateCategory(category)
        viewModel.updateTimeOfDay(timeOfDay)
        
        if (viewModel.isFormValid()) {
            viewModel.saveHabit()
        } else {
            binding.editTextTitle.error = "Title is required"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}