package com.example

import android.app.Application
import com.example.data.WellbeingDatabase
import com.example.data.WellbeingRepository

class WellbeingApplication : Application() {
    val database: WellbeingDatabase by lazy { WellbeingDatabase.getInstance(this) }
    val repository: WellbeingRepository by lazy {
        WellbeingRepository(database.wellbeingDao(), applicationContext)
    }
}
