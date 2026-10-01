package com.example.batterytg

import android.content.Context

object Prefs {
    private const val FILE = "battery_tg_prefs"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_REPORT_KEY = "report_key"
    private const val KEY_INTERVAL_MIN = "interval_minutes"

    private const val DEFAULT_INTERVAL = 180 // 3 hours

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getServerUrl(ctx: Context): String =
        prefs(ctx).getString(KEY_SERVER_URL, "") ?: ""

    fun getReportKey(ctx: Context): String =
        prefs(ctx).getString(KEY_REPORT_KEY, "") ?: ""

    fun getIntervalMinutes(ctx: Context): Int =
        prefs(ctx).getInt(KEY_INTERVAL_MIN, DEFAULT_INTERVAL)

    fun saveConnection(ctx: Context, serverUrl: String, reportKey: String) {
        prefs(ctx).edit()
            .putString(KEY_SERVER_URL, serverUrl)
            .putString(KEY_REPORT_KEY, reportKey)
            .apply()
    }

    fun saveInterval(ctx: Context, intervalMinutes: Int) {
        prefs(ctx).edit().putInt(KEY_INTERVAL_MIN, intervalMinutes).apply()
    }

    fun isConnected(ctx: Context): Boolean =
        getServerUrl(ctx).isNotBlank() && getReportKey(ctx).isNotBlank()
}
