package com.example.habito

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.habito.database.repositories.HabitRepository
import com.example.habito.database.repositories.SettingsRepository
import com.example.habito.database.repositories.UserRepository
import com.example.habito.data.AuthState
import com.example.habito.ui.auth.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var authRepository: UserRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize auth repository
        authRepository = UserRepository.getInstance(this)
        
        // Check authentication status
        checkAuthenticationStatus()
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Apply system bar insets: top/side to root, bottom to BottomNavigationView so it sits flush
        val root = findViewById<android.view.View>(R.id.main)
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Do NOT apply bottom inset to root to avoid pushing the BottomNavigationView up
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Let the BottomNavigationView handle bottom inset internally
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
        
        setupNavigation()
        initializeApp()
    }
    
    private fun checkAuthenticationStatus() {
        lifecycleScope.launch {
            authRepository.authState.collect { authState ->
                if (authState == AuthState.UNAUTHENTICATED) {
                    // User is not authenticated, redirect to login
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                    return@collect
                }
                // User is authenticated, continue with normal flow
            }
        }
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_habits, R.id.navigation_mood -> {
                    fab.show()
                }
                else -> {
                    fab.hide()
                }
            }
        }
    }
    
    private fun initializeApp() {
        // Initialize repositories and sample data on first launch
        CoroutineScope(Dispatchers.IO).launch {
            val settingsRepository = SettingsRepository.getInstance(this@MainActivity)
            if (settingsRepository.isFirstLaunch()) {
                val habitRepository = HabitRepository.getInstance(this@MainActivity)
                habitRepository.initializeWithSampleData()
                settingsRepository.setFirstLaunchCompleted()
            }
            
            // Initialize hydration reminders if enabled
            val hydrationManager = com.example.habito.utils.HydrationManager.getInstance(this@MainActivity)
            val settings = settingsRepository.getSettings()
            if (settings.hydrationReminderEnabled) {
                hydrationManager.scheduleHydrationReminders()
            }
        }
    }
}