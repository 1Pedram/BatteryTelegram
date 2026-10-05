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
import java.util.concurrent.TimeUnit

class BatteryCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = PrefsManager(appContext)
        val key = prefs.deviceKey ?: return@withContext Result.success()

        val info = BatteryHelper.getCurrentBattery(appContext)
        val threshold = prefs.alertThreshold
        val lastState = prefs.lastReportedState
        val wasCharging = prefs.lastWasCharging

        val currentState = when {
            info.percentage <= 5 -> "DYING"
            info.percentage <= threshold -> "LOW"
            info.percentage >= (threshold + 5) -> "NORMAL"
            else -> lastState
        }

        val stateChanged = (currentState != lastState && currentState != "NORMAL") ||
                (currentState == "NORMAL" && lastState != "NORMAL")
        val chargingChanged = (wasCharging != info.isCharging)

        if (stateChanged || chargingChanged) {
            val jsonBody = JSONObject().apply {
                put("key", key)
                put("battery", info.percentage)
                put("isCharging", info.isCharging)
                put("powerSource", info.powerSource)
                put("state", currentState)
            }

            val request = Request.Builder()
                .url(API_ENDPOINT)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    prefs.lastReportedState = currentState
                    prefs.lastWasCharging = info.isCharging
                }
                response.close()
            } catch (_: Exception) {
                return@withContext Result.retry()
            }
        }

        Result.success()
    }

    companion object {
        const val API_ENDPOINT = "https://battery-reporter.1pedro.workers.dev/report"
    }
}
