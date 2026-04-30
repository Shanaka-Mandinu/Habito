package com.example.habito.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.habito.R
import com.example.habito.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val position = arguments?.getInt(ARG_POSITION) ?: 0
        setupOnboardingContent(position)
    }

    private fun setupOnboardingContent(position: Int) {
        when (position) {
            0 -> {
                binding.imageView.setImageResource(R.drawable.img)
                binding.titleTextView.text = "Track Your Habits"
                binding.descriptionTextView.text = "Build positive routines and break bad habits with our easy-to-use habit tracker. Set goals and watch your progress grow day by day."
            }
            1 -> {
                binding.imageView.setImageResource(R.drawable.img)
                binding.titleTextView.text = "Monitor Your Mood"
                binding.descriptionTextView.text = "Keep track of your emotional well-being with our mood journal. Understand patterns and improve your mental health journey."
            }
            2 -> {
                binding.imageView.setImageResource(R.drawable.img)
                binding.titleTextView.text = "Stay Hydrated"
                binding.descriptionTextView.text = "Get gentle reminders to drink water throughout the day. Build the healthy habit of staying properly hydrated."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}