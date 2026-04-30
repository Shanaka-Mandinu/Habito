package com.example.habito.database.daos

import androidx.room.*
import com.example.habito.database.entities.HydrationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationRecordDao {
    
    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HydrationRecordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<HydrationRecordEntity>)
    
    // Read
    @Query("SELECT * FROM hydration_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Int): HydrationRecordEntity?
    
    @Query("SELECT * FROM hydration_records WHERE date = :date ORDER BY time DESC")
    suspend fun getRecordsByDate(date: String): List<HydrationRecordEntity>
    
    @Query("SELECT * FROM hydration_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, time DESC")
    suspend fun getRecordsBetweenDates(startDate: String, endDate: String): List<HydrationRecordEntity>
    
    @Query("SELECT * FROM hydration_records ORDER BY createdAt DESC")
    suspend fun getAllRecords(): List<HydrationRecordEntity>
    
    @Query("SELECT * FROM hydration_records ORDER BY createdAt DESC")
    fun getAllRecordsFlow(): Flow<List<HydrationRecordEntity>>
    
    @Query("SELECT SUM(amount) FROM hydration_records WHERE date = :date")
    suspend fun getTotalAmountForDate(date: String): Int?
    
    @Query("SELECT SUM(amount) FROM hydration_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalAmountBetweenDates(startDate: String, endDate: String): Int?
    
    @Query("SELECT COUNT(*) FROM hydration_records WHERE date = :date")
    suspend fun getRecordCountForDate(date: String): Int
    
    // Update
    @Update
    suspend fun updateRecord(record: HydrationRecordEntity)
    
    @Query("UPDATE hydration_records SET amount = :amount WHERE id = :recordId")
    suspend fun updateAmount(recordId: Int, amount: Int)
    
    // Delete
    @Delete
    suspend fun deleteRecord(record: HydrationRecordEntity)
    
    @Query("DELETE FROM hydration_records WHERE id = :recordId")
    suspend fun deleteRecordById(recordId: Int)
    
    @Query("DELETE FROM hydration_records WHERE date = :date")
    suspend fun deleteRecordsByDate(date: String)
    
    @Query("DELETE FROM hydration_records")
    suspend fun deleteAllRecords()
}
