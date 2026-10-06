package com.pedro.batteryreporter

import android.app.Application
import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class BatteryReporterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleBatterySync(this)
    }

    companion object {
        fun scheduleBatterySync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<BatteryCheckWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "BatteryReporterSync",
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }
    }
}
