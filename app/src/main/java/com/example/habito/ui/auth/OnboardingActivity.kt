package com.example.habito.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.habito.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.habito.database.repositories.UserRepository
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingPagerAdapter
    private val authRepository by lazy { UserRepository.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = onboardingAdapter

        // Connect tabs with ViewPager2
//        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ ->
//            // Tabs are just dots, no text needed
//        }.attach()

        // Handle page changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtons(position)
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingAdapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updateButtons(position: Int) {
        if (position == onboardingAdapter.itemCount - 1) {
            binding.btnNext.text = "Get Started"
            binding.btnSkip.text = ""
        } else {
            binding.btnNext.text = "Next"
            binding.btnSkip.text = "Skip"
        }
    }

    private fun finishOnboarding() {
        // Mark onboarding and first launch as completed so we don't show this again
        lifecycleScope.launch {
            authRepository.setOnboardingCompleted()
        }

        // Proceed directly to login; notifications permission handled in Settings toggle
        moveToLogin()
    }

    private fun moveToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

class OnboardingPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return OnboardingFragment.newInstance(position)
    }
}