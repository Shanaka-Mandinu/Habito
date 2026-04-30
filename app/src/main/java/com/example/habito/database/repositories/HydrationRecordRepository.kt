package com.example.habito.database.repositories

import android.content.Context
import com.example.habito.database.DatabaseManager
import com.example.habito.database.entities.HydrationRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HydrationRecordRepository private constructor(context: Context) {
    
    private val hydrationRecordDao = DatabaseManager.getInstance(context).hydrationRecordDao
    
    companion object {
        @Volatile
        private var INSTANCE: HydrationRecordRepository? = null
        
        fun getInstance(context: Context): HydrationRecordRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HydrationRecordRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    

    
    suspend fun addWaterIntake(amount: Int, date: String? = null, time: String? = null) = withContext(Dispatchers.IO) {
        val recordDate = date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val recordTime = time ?: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        
        val record = HydrationRecordEntity(
            amount = amount,
            date = recordDate,
            time = recordTime,
            createdAt = System.currentTimeMillis()
        )
        
        hydrationRecordDao.insertRecord(record)
    }
    
    suspend fun getRecordById(recordId: Int): HydrationRecordEntity? = withContext(Dispatchers.IO) {
        hydrationRecordDao.getRecordById(recordId)
    }
    
    suspend fun updateRecord(record: HydrationRecordEntity) = withContext(Dispatchers.IO) {
        hydrationRecordDao.updateRecord(record)
    }
    
    suspend fun deleteRecord(recordId: Int) = withContext(Dispatchers.IO) {
        hydrationRecordDao.deleteRecordById(recordId)
    }
    
    suspend fun deleteRecordsByDate(date: String) = withContext(Dispatchers.IO) {
        hydrationRecordDao.deleteRecordsByDate(date)
    }
    

    
    suspend fun getRecordsByDate(date: String): List<HydrationRecordEntity> = withContext(Dispatchers.IO) {
        hydrationRecordDao.getRecordsByDate(date)
    }
    
    suspend fun getTodayRecords(): List<HydrationRecordEntity> = withContext(Dispatchers.IO) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        hydrationRecordDao.getRecordsByDate(today)
    }
    
    suspend fun getRecordsBetweenDates(startDate: String, endDate: String): List<HydrationRecordEntity> = withContext(Dispatchers.IO) {
        hydrationRecordDao.getRecordsBetweenDates(startDate, endDate)
    }
    
    suspend fun getAllRecords(): List<HydrationRecordEntity> = withContext(Dispatchers.IO) {
        hydrationRecordDao.getAllRecords()
    }
    
    fun getAllRecordsFlow(): Flow<List<HydrationRecordEntity>> {
        return hydrationRecordDao.getAllRecordsFlow()
    }
    

    
    suspend fun getTotalAmountForDate(date: String): Int = withContext(Dispatchers.IO) {
        hydrationRecordDao.getTotalAmountForDate(date) ?: 0
    }
    
    suspend fun getTodayTotalAmount(): Int = withContext(Dispatchers.IO) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        hydrationRecordDao.getTotalAmountForDate(today) ?: 0
    }
    
    suspend fun getTotalAmountBetweenDates(startDate: String, endDate: String): Int = withContext(Dispatchers.IO) {
        hydrationRecordDao.getTotalAmountBetweenDates(startDate, endDate) ?: 0
    }
    
    suspend fun getRecordCountForDate(date: String): Int = withContext(Dispatchers.IO) {
        hydrationRecordDao.getRecordCountForDate(date)
    }
    
    suspend fun getTodayRecordCount(): Int = withContext(Dispatchers.IO) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        hydrationRecordDao.getRecordCountForDate(today)
    }
    

    
    suspend fun getWeeklyHydrationData(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val sevenDaysAgo = LocalDate.now().minusDays(6)
        val result = mutableListOf<Pair<String, Int>>()
        
        for (i in 0..6) {
            val date = sevenDaysAgo.plusDays(i.toLong())
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val displayDate = date.format(DateTimeFormatter.ofPattern("MM/dd"))
            val total = getTotalAmountForDate(dateString)
            result.add(Pair(displayDate, total))
        }
        
        result
    }
    
    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        hydrationRecordDao.deleteAllRecords()
    }
}
