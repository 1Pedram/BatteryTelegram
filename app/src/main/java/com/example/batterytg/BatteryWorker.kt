package com.example.batterytg

import android.content.Context
import android.os.BatteryManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL

class BatteryWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val reportKey = Prefs.getReportKey(applicationContext)
        if (reportKey.isBlank()) {
            return Result.success() // not connected yet, nothing to send
        }

        val bm = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val payload = """{"key":"$reportKey","battery":$level}"""

        return try {
            val conn = URL("${Prefs.SERVER_URL}/report").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.outputStream.use { it.write(payload.toByteArray()) }

            val code = conn.responseCode
            conn.disconnect()
            // 429 = rate limited by the server; treat as success, don't retry immediately.
            if (code == 200 || code == 429) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
