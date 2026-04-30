# Migration Guide: SharedPreferences to Room Database

## Summary
This migration moves the app from SharedPreferences to Room Database ORM for better data persistence, type safety, and query capabilities.

## Changes Made

### 1. Database Layer (New)
- **Package**: `com.example.habito.database`
- **Entities**: UserEntity, HabitEntity, HabitCompletionEntity, MoodEntryEntity, HydrationRecordEntity, AppSettingsEntity
- **DAOs**: UserDao, HabitDao, HabitCompletionDao, MoodEntryDao, HydrationRecordDao, AppSettingsDao
- **Database**: WellnessDatabase (Room Database class)
- **Manager**: DatabaseManager (Singleton access to DAOs)
- **Converters**: Type converters for enums

### 2. Repository Migration

#### Old Repositories (Deprecated - to be removed)
- `com.example.habito.repository.AuthRepository` → Use `com.example.habito.database.repositories.UserRepository`
- `com.example.habito.data.repository.HabitRepository` → Use `com.example.habito.database.repositories.HabitRepository`
- `com.example.habito.data.repository.MoodRepository` → Use `com.example.habito.database.repositories.MoodRepository`
- `com.example.habito.data.repository.SettingsRepository` → Use `com.example.habito.database.repositories.SettingsRepository`

#### New Repositories (Room-based)
- `com.example.habito.database.repositories.UserRepository` - Handles authentication and user management
- `com.example.habito.database.repositories.HabitRepository` - Manages habits and completions
- `com.example.habito.database.repositories.MoodRepository` - Mood tracking
- `com.example.habito.database.repositories.SettingsRepository` - App settings
- `com.example.habito.database.repositories.HydrationRecordRepository` - Water intake tracking

### 3. Import Changes Required

Replace in all files:
```kotlin
// OLD
import com.example.habito.repository.AuthRepository
// NEW
import com.example.habito.database.repositories.UserRepository

// OLD
import com.example.habito.data.repository.HabitRepository
// NEW
import com.example.habito.database.repositories.HabitRepository

// OLD
import com.example.habito.data.repository.MoodRepository
// NEW
import com.example.habito.database.repositories.MoodRepository

// OLD
import com.example.habito.data.repository.SettingsRepository
// NEW
import com.example.habito.database.repositories.SettingsRepository
```

### 4. API Changes

#### UserRepository (formerly AuthRepository)
- All methods remain the same
- Auth state and current user flows unchanged
- Onboarding methods now use database

#### HabitRepository
- Added Flow support for reactive updates
- Cascade delete for completions
- Better query methods

#### MoodRepository
- Added Flow support
- Improved date-based queries
- Better analytics

#### SettingsRepository
- Added Flow support
- Individual setting update methods

### 5. Benefits
- **Type Safety**: Compile-time SQL query validation
- **Better Performance**: Optimized queries and indexing
- **Relationships**: Foreign keys for data integrity
- **Reactive**: Flow-based updates
- **Testability**: Easier to test with in-memory database
- **Migration**: Room handles schema migrations

### 6. Files to Update
See the list of files that need import updates above.
