# Habito

Habito is a Kotlin-based Android wellness & habit-tracking app focused on helping users build consistent routines and monitor daily wellbeing. The app tracks **habits and completions**, **mood entries**, **hydration (water intake)**, and user preferences—backed by a structured local database so data remains reliable, queryable, and easy to analyze over time.

## What this project does (Purpose)
Habito is designed to support everyday self-improvement by combining **habit management** with **wellness logging** (mood + hydration) and simple insights (like trends and summaries). It uses a clean data layer to store and retrieve user progress efficiently and safely.

## Key Features
- **Habit tracking**
  - Create and manage habits (including categories / time-of-day style organization)
  - Track **habit completions** over time (counts, completion state, timestamps)
  - Progress and streak-friendly data model (completion records linked to habits)

- **Mood tracking**
  - Log mood entries (emoji/level + optional note + timestamp)
  - Calendar/trends-ready storage for mood history
  - Charting support for visualizing mood trends

- **Hydration tracking**
  - Log daily water intake records
  - Calculate totals and summaries over date ranges
  - Designed to support goal/progress tracking

- **User + settings storage**
  - User data persisted locally (authentication/profile fields present in the database layer)
  - Centralized **app settings** entity for preferences and first-launch/onboarding style flags

- **Modern local persistence (Room ORM)**
  - Migrated from SharedPreferences-style storage to **Room Database**
  - Proper **entities + DAOs + repositories** with extensive CRUD coverage
  - Reactive-style queries supported via Flow in the data layer where needed

## Tech Stack
- **Language:** Kotlin
- **Platform:** Android
- **Architecture patterns:** Repository pattern, MVVM-friendly setup (ViewModel + LiveData dependencies included)
- **UI / AndroidX:** AppCompat, Material Components, Fragment KTX, RecyclerView, ViewBinding + DataBinding
- **Navigation:** AndroidX Navigation Component (Safe Args)
- **Persistence:** Room ORM (room-runtime, room-ktx, compiler via KSP)
- **Async:** Kotlin Coroutines
- **Background work:** WorkManager
- **Data / Serialization:** Gson
- **Charts / Visualization:** MPAndroidChart (mood trends)
- **Calendar UI:** material-calendarview
- **Other:** SplashScreen API, AndroidSVG, DataStore Preferences

## Project Notes
- Database module includes well-defined **Entities**, **DAOs**, and **Repositories** for:
  - Users, Habits, Habit Completions, Mood Entries, Hydration Records, and App Settings.
- This repo also includes database and migration documentation (e.g., Room implementation and usage guides).
