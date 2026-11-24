package com.example.data

import android.content.Context
import com.example.widget.ScreenTimeWidgetProvider
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TodayWidgetSnapshot(
    val dayLabel: String,
    val totalMinutes: Int,
    val topApps: List<AppUsageEntity>,
)

class WellbeingRepository(
    private val dao: WellbeingDao,
    private val appContext: Context,
) {
    val dailySummaries: Flow<List<DailySummaryEntity>> = dao.observeDailySummaries()
    val allAppUsages: Flow<List<AppUsageEntity>> = dao.observeAllAppUsages()

    suspend fun ensureSeeded() {
        if (dao.getSummaryCount() == 0) {
            seedDefaultWeek()
        }
    }

    suspend fun resetToDefaults() {
        dao.clearAllAppUsages()
        dao.clearAllSummaries()
        seedDefaultWeek()
        notifyWidgets()
    }

    suspend fun updateDailySummary(summary: DailySummaryEntity) {
        dao.upsertDailySummary(summary)
        notifyWidgets()
    }

    suspend fun addAppUsage(app: AppUsageEntity) {
        dao.insertAppUsage(app)
        notifyWidgets()
    }

    suspend fun updateAppUsage(app: AppUsageEntity) {
        dao.updateAppUsage(app)
        notifyWidgets()
    }

    suspend fun deleteAppUsage(appId: Int) {
        dao.deleteAppUsageById(appId)
        notifyWidgets()
    }

    suspend fun getTodayWidgetSnapshot(): TodayWidgetSnapshot {
        ensureSeeded()
        val summary = dao.getDailySummary(0)
        val apps = dao.getAppUsagesForDay(0)
        val appSum = apps.sumOf { it.usageMinutes }
        val totalMinutes = if (summary?.useCustomTotalOverride == true) {
            summary.customTotalMinutes
        } else {
            appSum
        }
        return TodayWidgetSnapshot(
            dayLabel = summary?.dayLabel ?: "Today",
            totalMinutes = totalMinutes,
            topApps = apps.filter { it.usageMinutes > 0 }.take(3),
        )
    }

    private fun notifyWidgets() {
        ScreenTimeWidgetProvider.refreshAllWidgets(appContext)
    }

    private suspend fun seedDefaultWeek() {
        val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val shortFmt = SimpleDateFormat("EEEEE", Locale.getDefault())
        val cal = Calendar.getInstance()

        val summaries = mutableListOf<DailySummaryEntity>()
        val seedApps = mutableListOf<AppUsageEntity>()

        val unlockSeeds = listOf(42, 51, 38, 46, 59, 64, 35)
        val notifSeeds = listOf(118, 142, 95, 130, 165, 184, 88)
        val dayMultipliers = listOf(1.0, 1.15, 0.85, 1.05, 1.25, 1.35, 0.78)

        for (dayOffset in 0..6) {
            val dayCal = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val label = when (dayOffset) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> dayFmt.format(dayCal.time)
            }
            val shortLabel = if (dayOffset == 0) "Today" else shortFmt.format(dayCal.time)

            summaries.add(
                DailySummaryEntity(
                    dayIndex = dayOffset,
                    dayLabel = label,
                    shortLabel = shortLabel,
                    unlocksCount = unlockSeeds[dayOffset],
                    notificationsCount = notifSeeds[dayOffset],
                    useCustomTotalOverride = false,
                    customTotalMinutes = 0,
                )
            )

            val mult = dayMultipliers[dayOffset]
            val dayPresetApps = listOf(
                AppUsageEntity(0, dayOffset, "YouTube", "Video", "video", 3, (78 * mult).toInt(), 90),
                AppUsageEntity(0, dayOffset, "Instagram", "Social", "photo_social", 4, (54 * mult).toInt(), 60),
                AppUsageEntity(0, dayOffset, "Chrome", "Browser", "browser", 0, (41 * mult).toInt(), 0),
                AppUsageEntity(0, dayOffset, "WhatsApp", "Messaging", "chat", 1, (31 * mult).toInt(), 0),
                AppUsageEntity(0, dayOffset, "Spotify", "Music", "music", 1, (22 * mult).toInt(), 0),
                AppUsageEntity(0, dayOffset, "Gmail", "Productivity", "mail", 2, (14 * mult).toInt(), 0),
            )
            seedApps.addAll(dayPresetApps)
        }

        dao.insertDailySummaries(summaries)
        dao.insertAppUsages(seedApps)
    }
}
