package com.example.batterytg

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object Scheduler {
    private const val WORK_NAME = "battery_report"
    const val MIN_INTERVAL_MINUTES = 15L // Android's floor for periodic work

    fun schedule(context: Context, intervalMinutes: Int) {
        val safeInterval = intervalMinutes.coerceAtLeast(MIN_INTERVAL_MINUTES.toInt())
        val net = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val request = PeriodicWorkRequestBuilder<BatteryWorker>(
            safeInterval.toLong(), TimeUnit.MINUTES
        ).setConstraints(net).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun sendTestNow(context: Context) {
        val net = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<BatteryWorker>().setConstraints(net).build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
