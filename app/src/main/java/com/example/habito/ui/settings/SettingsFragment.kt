package com.example.habito.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import android.widget.ArrayAdapter
import com.example.habito.R
import com.example.habito.databinding.FragmentSettingsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import com.example.habito.ui.auth.LoginActivity
import android.content.Intent
import android.widget.LinearLayout
import android.widget.Toast

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private var updatingUi: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Hide global FAB on Settings screen
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.visibility = View.GONE

        setupViews()
        observeViewModel()
        
        viewModel.loadSettings()
    }
    
    private fun setupViews() {
        binding.switchHydrationReminders.setOnCheckedChangeListener { _, isChecked ->
            if (updatingUi) return@setOnCheckedChangeListener
            viewModel.updateHydrationEnabled(isChecked)
        }
        
        binding.switchNotifications.setOnCheckedChangeListener { _, _ ->
            if (updatingUi) return@setOnCheckedChangeListener
            viewModel.toggleNotifications()
        }
        
        binding.switchDarkTheme.setOnCheckedChangeListener { _, _ ->
            if (updatingUi) return@setOnCheckedChangeListener
            viewModel.toggleDarkTheme()
        }
        
        binding.editTextWaterGoal.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val goal = binding.editTextWaterGoal.text.toString().toIntOrNull() ?: 8
                viewModel.updateWaterGoal(goal)
            }
        }
        
        // Removed start/end time pickers

        // Hydration interval options (in hours)
        val intervals = listOf(
            "1 min" to (1f/60f),
            "2 min" to (2f/60f),
            "5 min" to (5f/60f),
            "15 min" to 0.25f,
            "30 min" to 0.5f,
            "1 hour" to 1f,
            "2 hours" to 2f,
            "3 hours" to 3f,
            "4 hours" to 4f
        )
        val labels = intervals.map { it.first }
        // Use a no-filter adapter so the full list is always shown
        val adapter = object : ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, labels) {
            override fun getFilter(): android.widget.Filter = object : android.widget.Filter() {
                override fun performFiltering(prefix: CharSequence?): FilterResults {
                    return FilterResults().apply {
                        values = labels
                        count = labels.size
                    }
                }
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    // notify the dropdown to refresh
                    notifyDataSetChanged()
                }
                override fun convertResultToString(resultValue: Any?): CharSequence {
                    return (resultValue as? String) ?: ""
                }
            }
        }
        binding.autoCompleteInterval.setAdapter(adapter)
        // Open on click/focus
        binding.autoCompleteInterval.setOnClickListener { v ->
            (v as? com.google.android.material.textfield.MaterialAutoCompleteTextView)?.showDropDown()
        }
        binding.autoCompleteInterval.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as? com.google.android.material.textfield.MaterialAutoCompleteTextView)?.showDropDown()
        }
        binding.autoCompleteInterval.setOnItemClickListener { _, _, position, _ ->
            val hours = intervals[position].second
            viewModel.updateHydrationInterval(hours)
        }

        binding.buttonLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.logout_confirm_title))
                .setMessage(getString(R.string.logout_confirm_message))
                .setPositiveButton(getString(R.string.logout)) { _, _ ->
                    viewModel.logout()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        binding.buttonChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collect { settings ->
                updatingUi = true
                try {
                    binding.switchHydrationReminders.isChecked = settings.hydrationReminderEnabled
                    binding.switchNotifications.isChecked = settings.notificationsEnabled
                    binding.switchDarkTheme.isChecked = settings.darkThemeEnabled
                    binding.editTextWaterGoal.setText(settings.dailyWaterGoal.toString())
                    // Start/End time removed from UI

                    // Reflect current interval in dropdown
                    val intervalHours = settings.hydrationInterval / (60f * 60f * 1000f)
                    val label = when {
                        intervalHours in 0.015f..0.02f -> "1 min"
                        intervalHours in 0.02f..0.04f -> "2 min"
                        intervalHours in 0.07f..0.09f -> "5 min"
                        intervalHours in 0.20f..0.30f -> "15 min"
                        intervalHours in 0.45f..0.55f -> "30 min"
                        intervalHours in 0.9f..1.1f -> "1 hour"
                        intervalHours in 1.9f..2.1f -> "2 hours"
                        intervalHours in 2.9f..3.1f -> "3 hours"
                        intervalHours in 3.9f..4.1f -> "4 hours"
                        else -> "1 hour"
                    }
                    if (binding.autoCompleteInterval.text.toString() != label) {
                        // setText without filtering/reopening dropdown
                        binding.autoCompleteInterval.setText(label, false)
                    }
                } finally {
                    updatingUi = false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when {
                    event == null -> {}
                    event == "logged_out" -> {
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        activity?.finish()
                        viewModel.consumeEvent()
                    }
                    event == "password_changed" -> {
                        Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                        viewModel.consumeEvent()
                    }
                    event.startsWith("password_change_error") -> {
                        val message = event.substringAfter(":", "Error updating password")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        viewModel.consumeEvent()
                    }
                }
            }
        }
    }

    private fun showChangePasswordDialog() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 0)
        }
        val oldPass = EditText(requireContext()).apply {
            hint = getString(R.string.current_password)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val newPass = EditText(requireContext()).apply {
            hint = getString(R.string.new_password)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val confirmPass = EditText(requireContext()).apply {
            hint = getString(R.string.confirm_new_password)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        container.addView(oldPass)
        container.addView(newPass)
        container.addView(confirmPass)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_password))
            .setView(container)
            .setPositiveButton(getString(R.string.change_password), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val old = oldPass.text.toString()
                val new = newPass.text.toString()
                val confirm = confirmPass.text.toString()

                when {
                    new.isBlank() || confirm.isBlank() -> {
                        Toast.makeText(requireContext(), getString(R.string.password_empty), Toast.LENGTH_SHORT).show()
                    }
                    new != confirm -> {
                        Toast.makeText(requireContext(), getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        viewModel.changePassword(old, new)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        // Restore FAB visibility when leaving Settings
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.visibility = View.VISIBLE
        super.onDestroyView()
        _binding = null
    }
}