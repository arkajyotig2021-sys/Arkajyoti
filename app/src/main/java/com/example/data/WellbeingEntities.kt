package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val dayIndex: Int, // 0 = Today, 1..6 = previous 6 days
    val dayLabel: String,
    val shortLabel: String,
    val unlocksCount: Int,
    val notificationsCount: Int,
    val useCustomTotalOverride: Boolean = false,
    val customTotalMinutes: Int = 0,
)

@Entity(tableName = "app_usages")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayIndex: Int,
    val appName: String,
    val category: String,
    val iconKey: String,
    val colorIndex: Int,
    val usageMinutes: Int,
    val timerLimitMinutes: Int = 0,
)

data class PresetAppTemplate(
    val appName: String,
    val category: String,
    val iconKey: String,
    val colorIndex: Int,
    val defaultMinutes: Int,
)

object PresetAppCatalog {
    val templates = listOf(
        PresetAppTemplate("YouTube", "Video", "video", 3, 74),
        PresetAppTemplate("Instagram", "Social", "photo_social", 4, 52),
        PresetAppTemplate("Chrome", "Browser", "browser", 0, 38),
        PresetAppTemplate("WhatsApp", "Messaging", "chat", 1, 29),
        PresetAppTemplate("Spotify", "Music", "music", 1, 24),
        PresetAppTemplate("Gmail", "Productivity", "mail", 3, 18),
        PresetAppTemplate("Maps", "Navigation", "map", 5, 16),
        PresetAppTemplate("TikTok", "Video", "movie", 6, 45),
        PresetAppTemplate("Reddit", "Social", "forum", 2, 32),
        PresetAppTemplate("X", "Social", "tag", 7, 25),
        PresetAppTemplate("Photos", "Media", "image", 2, 14),
        PresetAppTemplate("Games", "Gaming", "gamepad", 4, 40),
    )

    val iconKeys = listOf(
        "video" to "Video",
        "photo_social" to "Social",
        "browser" to "Browser",
        "chat" to "Chat",
        "music" to "Music",
        "mail" to "Mail",
        "map" to "Maps",
        "movie" to "Clips",
        "forum" to "Forum",
        "tag" to "Feed",
        "image" to "Photos",
        "gamepad" to "Games",
        "apps" to "App",
    )

    val categories = listOf(
        "Social",
        "Video",
        "Messaging",
        "Browser",
        "Music",
        "Productivity",
        "Gaming",
        "Navigation",
        "Utilities",
    )
}

fun formatScreenTimeLong(totalMinutes: Int): String {
    val safe = totalMinutes.coerceAtLeast(0)
    val hrs = safe / 60
    val mins = safe % 60
    return when {
        hrs > 0 && mins > 0 -> "$hrs hr, $mins min"
        hrs > 0 -> "$hrs hr"
        else -> "$mins min"
    }
}

fun formatScreenTimeCompact(totalMinutes: Int): String {
    val safe = totalMinutes.coerceAtLeast(0)
    val hrs = safe / 60
    val mins = safe % 60
    return when {
        hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
        hrs > 0 -> "${hrs}h"
        else -> "${mins}m"
    }
}
