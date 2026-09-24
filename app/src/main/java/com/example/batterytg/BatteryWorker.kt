package com.example.batterytg

import android.content.Context
import android.os.BatteryManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class BatteryWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    // TODO: put your NEW token and chat id here
    private val botToken = "8964236884:AAFpEYAnNa4uhdBjLWlX4JXillOsmejuMaU"
    private val chatId = "101098680"

    override fun doWork(): Result {
        val bm = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val text = URLEncoder.encode("Battery Level: $level%", "UTF-8")
        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"

        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            val code = conn.responseCode
            conn.disconnect()
            if (code == 200) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
