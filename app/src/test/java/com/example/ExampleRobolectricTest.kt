package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppUsageEntity
import com.example.data.WellbeingDatabase
import com.example.data.WellbeingRepository
import com.example.data.formatScreenTimeLong
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: WellbeingDatabase
    private lateinit var repository: WellbeingRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, WellbeingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WellbeingRepository(db.wellbeingDao(), context)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `app name matches Screen Wellbeing`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Screen Wellbeing", appName)
    }

    @Test
    fun `seed and custom app edit updates widget top 3 snapshot`() = runTest {
        repository.ensureSeeded()
        val initialSnapshot = repository.getTodayWidgetSnapshot()
        assertEquals("Today", initialSnapshot.dayLabel)
        assertEquals(3, initialSnapshot.topApps.size)
        assertTrue(initialSnapshot.totalMinutes > 0)

        repository.addAppUsage(
            AppUsageEntity(
                dayIndex = 0,
                appName = "Custom Focus App",
                category = "Productivity",
                iconKey = "apps",
                colorIndex = 0,
                usageMinutes = 200,
                timerLimitMinutes = 60,
            )
        )

        val updatedSnapshot = repository.getTodayWidgetSnapshot()
        assertEquals("Custom Focus App", updatedSnapshot.topApps.first().appName)
        assertEquals(200, updatedSnapshot.topApps.first().usageMinutes)
    }

    @Test
    fun `screen time formatting handles hours and minutes`() {
        assertEquals("3 hr, 42 min", formatScreenTimeLong(222))
        assertEquals("2 hr", formatScreenTimeLong(120))
        assertEquals("45 min", formatScreenTimeLong(45))
        assertEquals("0 min", formatScreenTimeLong(0))
    }
}
