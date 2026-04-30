# Room Database Quick Reference Guide

## How to Use the New Database Repositories

### 1. UserRepository (Authentication)

```kotlin
val userRepo = UserRepository.getInstance(context)

// Register new user
lifecycleScope.launch {
    val request = RegisterRequest("John", "john@example.com", "password123", "password123")
    val result = userRepo.register(request)
    result.onSuccess { user ->
        // Registration successful
    }
}

// Login
lifecycleScope.launch {
    val request = LoginRequest("john@example.com", "password123")
    val result = userRepo.login(request)
    result.onSuccess { user ->
        // Login successful
    }
}

// Logout
lifecycleScope.launch {
    userRepo.logout()
}

// Observe auth state
lifecycleScope.launch {
    userRepo.authState.collect { state ->
        when (state) {
            AuthState.AUTHENTICATED -> // User logged in
            AuthState.UNAUTHENTICATED -> // User logged out
            AuthState.FIRST_TIME_USER -> // New user
        }
    }
}
```

### 2. HabitRepository

```kotlin
val habitRepo = HabitRepository.getInstance(context)

// Create new habit
lifecycleScope.launch {
    val habit = Habit(
        id = UUID.randomUUID().toString(),
        title = "Drink Water",
        iconRes = R.drawable.ic_water,
        target = 8,
        category = HabitCategory.HEALTH
    )
    habitRepo.saveHabit(habit)
}

// Get all habits
lifecycleScope.launch {
    val habits = habitRepo.getAllHabits()
}

// Get habits with Flow (reactive)
habitRepo.getAllHabitsFlow().collect { habits ->
    // UI updates automatically
}

// Mark habit complete
lifecycleScope.launch {
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    habitRepo.markHabitComplete(habitId, today)
}

// Get daily progress
lifecycleScope.launch {
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val progress = habitRepo.getDailyProgress(today)
    // progress.completionPercentage
}

// Delete habit
lifecycleScope.launch {
    habitRepo.deleteHabit(habitId)
}
```

### 3. MoodRepository

```kotlin
val moodRepo = MoodRepository.getInstance(context)

// Save mood entry
lifecycleScope.launch {
    val mood = MoodEntry(
        id = UUID.randomUUID().toString(),
        isoDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        emoji = "😊",
        moodLevel = 4,
        note = "Feeling great today!"
    )
    moodRepo.saveMoodEntry(mood)
}

// Get all moods
lifecycleScope.launch {
    val moods = moodRepo.getMoodEntriesSortedByDate()
}

// Get mood for specific date
lifecycleScope.launch {
    val mood = moodRepo.getMoodForDate(LocalDate.now())
}

// Get weekly mood data
lifecycleScope.launch {
    val weeklyData = moodRepo.getWeeklyMoodData()
    // Returns List<Pair<String, Float>> for charts
}

// Delete mood
lifecycleScope.launch {
    moodRepo.deleteMoodEntry(entryId)
}
```

### 4. SettingsRepository

```kotlin
val settingsRepo = SettingsRepository.getInstance(context)

// Get settings
lifecycleScope.launch {
    val settings = settingsRepo.getSettings()
}

// Observe settings with Flow
settingsRepo.getSettingsFlow().collect { settings ->
    // UI updates automatically
}

// Update hydration settings
lifecycleScope.launch {
    settingsRepo.updateHydrationSettings(
        enabled = true,
        interval = 3600000L,
        startTime = "08:00",
        endTime = "22:00",
        dailyGoal = 8
    )
}

// Toggle dark theme
lifecycleScope.launch {
    settingsRepo.toggleDarkTheme()
}

// Toggle notifications
lifecycleScope.launch {
    settingsRepo.toggleNotifications()
}
```

### 5. HydrationRecordRepository (NEW)

```kotlin
val hydrationRepo = HydrationRecordRepository.getInstance(context)

// Add water intake
lifecycleScope.launch {
    hydrationRepo.addWaterIntake(amount = 1) // 1 glass
}

// Get today's total
lifecycleScope.launch {
    val total = hydrationRepo.getTodayTotalAmount()
    // Returns number of glasses
}

// Get today's records
lifecycleScope.launch {
    val records = hydrationRepo.getTodayRecords()
}

// Get weekly hydration data
lifecycleScope.launch {
    val weeklyData = hydrationRepo.getWeeklyHydrationData()
    // Returns List<Pair<String, Int>> for charts
}

// Delete a record
lifecycleScope.launch {
    hydrationRepo.deleteRecord(recordId)
}
```

## Database Operations Cheat Sheet

### Common Patterns

**1. Using suspend functions:**
```kotlin
lifecycleScope.launch {
    // All repository methods are suspend functions
    val data = repository.getData()
}
```

**2. Using Flow for reactive updates:**
```kotlin
// In ViewModel
val habits: StateFlow<List<Habit>> = habitRepo.getAllHabitsFlow()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

// In Fragment/Activity
viewModel.habits.collect { habits ->
    // Update UI
}
```

**3. Error handling:**
```kotlin
lifecycleScope.launch {
    try {
        val result = repository.operation()
        // Success
    } catch (e: Exception) {
        // Handle error
    }
}
```

## DAO Methods Overview

### UserDao
- `insertUser()` - Add new user
- `getUserById()` - Get user by ID
- `getUserByEmail()` - Get user by email
- `authenticateUser()` - Login verification
- `userExists()` - Check if email exists
- `updateUser()` - Update user profile
- `updatePassword()` - Change password
- `deleteUser()` - Remove user

### HabitDao
- `insertHabit()` - Add new habit
- `getAllHabits()` - Get all habits
- `getHabitById()` - Get specific habit
- `getHabitsByCategory()` - Filter by category
- `updateHabit()` - Update habit
- `deleteHabitById()` - Remove habit

### HabitCompletionDao
- `insertCompletion()` - Mark habit complete
- `getCompletionForDate()` - Get date completions
- `getCompletionsForHabit()` - Habit history
- `getTotalCompletionCount()` - Total completions
- `getStreakDays()` - Streak calculation
- `updateCompletionStatus()` - Update completion
- `deleteCompletion()` - Remove completion

### MoodEntryDao
- `insertMoodEntry()` - Add mood entry
- `getAllMoodEntries()` - Get all moods
- `getMoodEntryByDate()` - Get mood for date
- `getAverageMoodLevel()` - Calculate average
- `updateMoodEntry()` - Update mood
- `deleteMoodEntryById()` - Remove mood

### HydrationRecordDao
- `insertRecord()` - Add water intake
- `getRecordsByDate()` - Get date records
- `getTotalAmountForDate()` - Daily total
- `updateRecord()` - Update record
- `deleteRecordById()` - Remove record

### AppSettingsDao
- `insertSettings()` - Save settings
- `getSettings()` - Get current settings
- `updateSettings()` - Update all settings
- `updateHydrationReminderEnabled()` - Toggle reminders
- `updateDarkThemeEnabled()` - Toggle dark mode
- `updateNotificationsEnabled()` - Toggle notifications

## Best Practices

1. **Always use coroutines** for database operations
2. **Use Flow** for reactive UI updates
3. **Handle errors** with try-catch blocks
4. **Repository pattern** - Never call DAOs directly from UI
5. **ViewModelScope** - Use viewModelScope in ViewModels
6. **LifecycleScope** - Use lifecycleScope in Activities/Fragments

## Database Location
- **Database file**: `/data/data/com.example.habito/databases/wellness_database`
- **Inspect with**: Android Studio Database Inspector (View > Tool Windows > App Inspection)

---

Happy Coding! 🚀
