package com.example.habito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_records")
data class HydrationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Int, // glasses of water
    val date: String, // ISO date format
    val time: String, // Time in HH:mm format
    val createdAt: Long = System.currentTimeMillis()
)
