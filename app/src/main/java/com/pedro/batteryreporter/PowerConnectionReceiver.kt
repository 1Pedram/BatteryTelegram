package com.pedro.batteryreporter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Wake and dispatch an immediate report upon plug, unplug, or critical battery drop
        val syncRequest = OneTimeWorkRequestBuilder<BatteryCheckWorker>().build()
        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}
