package com.pedro.batteryreporter

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class BatteryCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val client = OkHttpClient()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = PrefsManager(applicationContext)
        val key = prefs.deviceKey ?: return@withContext Result.success()

        val info = BatteryHelper.getCurrentBattery(applicationContext)
        val threshold = prefs.alertThreshold
        val wasCharging = prefs.lastChargingState
        val lastState = prefs.lastReportedState

        val currentState = when {
            info.percentage <= 5 && !info.isCharging -> "DYING"
            info.percentage <= threshold && !info.isCharging -> "LOW"
            else -> "NORMAL"
        }

        val stateChanged = (currentState != lastState && currentState != "NORMAL") ||
                (currentState == "NORMAL" && lastState != "NORMAL")
        val chargingChanged = (wasCharging != info.isCharging)
        val belowThresholdOnBattery = (!info.isCharging && info.percentage <= threshold)

        // Dispatch update if state changed, plug/unplug occurred, or battery is below threshold
        if (stateChanged || chargingChanged || belowThresholdOnBattery) {
            val jsonBody = JSONObject().apply {
                put("key", key)
                put("name", prefs.deviceName)
                put("battery", info.percentage)
                put("isCharging", info.isCharging)
                put("powerSource", info.powerSource)
                put("threshold", threshold)
                put("state", currentState)
            }

            val request = Request.Builder()
                .url("https://battery-reporter.1pedro.workers.dev/report")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    prefs.lastReportedState = currentState
                    prefs.lastChargingState = info.isCharging
                    prefs.lastReportedBattery = info.percentage
                    response.close()
                    return@withContext Result.success()
                }
                response.close()
                return@withContext Result.retry()
            } catch (e: Exception) {
                return@withContext Result.retry()
            }
        }

        Result.success()
    }
}
