package com.example.batterytg

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wm = WorkManager.getInstance(applicationContext)
        val net = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        // Every 3 hours; survives reboots; system batches it with other jobs (Doze-friendly)
        wm.enqueueUniquePeriodicWork(
            "battery_report",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<BatteryWorker>(3, TimeUnit.HOURS)
                .setConstraints(net).build()
        )
        // One immediate test message
        wm.enqueue(OneTimeWorkRequestBuilder<BatteryWorker>().setConstraints(net).build())

        Toast.makeText(this, "Battery reports scheduled (every 3h)", Toast.LENGTH_LONG).show()
        finish()
    }
}
