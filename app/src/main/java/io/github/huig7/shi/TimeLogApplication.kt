package io.github.huig7.shi

import android.app.Application
import io.github.huig7.shi.data.local.AppDatabase
import io.github.huig7.shi.data.repository.TimeLogRepository

class TimeLogApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: TimeLogRepository by lazy { TimeLogRepository(database) }
}
