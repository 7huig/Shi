package com.example.timelog

import android.app.Application
import com.example.timelog.data.local.AppDatabase
import com.example.timelog.data.repository.TimeLogRepository

class TimeLogApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: TimeLogRepository by lazy { TimeLogRepository(database) }
}
