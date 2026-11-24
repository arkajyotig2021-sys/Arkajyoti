package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WellbeingDao {
    @Query("SELECT * FROM daily_summaries ORDER BY dayIndex ASC")
    fun observeDailySummaries(): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM app_usages ORDER BY usageMinutes DESC, id ASC")
    fun observeAllAppUsages(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM daily_summaries WHERE dayIndex = :dayIndex LIMIT 1")
    suspend fun getDailySummary(dayIndex: Int): DailySummaryEntity?

    @Query("SELECT * FROM app_usages WHERE dayIndex = :dayIndex ORDER BY usageMinutes DESC, id ASC")
    suspend fun getAppUsagesForDay(dayIndex: Int): List<AppUsageEntity>

    @Query("SELECT COUNT(*) FROM daily_summaries")
    suspend fun getSummaryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummaries(summaries: List<DailySummaryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailySummary(summary: DailySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsages(apps: List<AppUsageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsage(app: AppUsageEntity): Long

    @Update
    suspend fun updateAppUsage(app: AppUsageEntity)

    @Query("DELETE FROM app_usages WHERE id = :appId")
    suspend fun deleteAppUsageById(appId: Int)

    @Query("DELETE FROM daily_summaries")
    suspend fun clearAllSummaries()

    @Query("DELETE FROM app_usages")
    suspend fun clearAllAppUsages()
}
