package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailySummaryEntity::class, AppUsageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WellbeingDatabase : RoomDatabase() {
    abstract fun wellbeingDao(): WellbeingDao

    companion object {
        @Volatile
        private var INSTANCE: WellbeingDatabase? = null

        fun getInstance(context: Context): WellbeingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WellbeingDatabase::class.java,
                    "screen_wellbeing.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
