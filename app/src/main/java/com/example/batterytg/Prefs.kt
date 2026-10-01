package com.example.batterytg

import android.content.Context

object Prefs {
    private const val FILE = "battery_tg_prefs"
    private const val KEY_REPORT_KEY = "report_key"
    private const val KEY_INTERVAL_MIN = "interval_minutes"
    private const val KEY_DEVICE_NAME = "device_name"
    private const val KEY_THRESHOLD = "low_battery_threshold"
    private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    private const val KEY_IS_PREMIUM = "is_premium"

    private const val DEFAULT_INTERVAL = 180 // 3 hours
    private const val DEFAULT_THRESHOLD = 20 // percent

    // Fixed on purpose: this app ships pointing at one server, and that's not user-editable.
    const val SERVER_URL = "https://battery-reporter.1pedro.workers.dev"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getReportKey(ctx: Context): String =
        prefs(ctx).getString(KEY_REPORT_KEY, "") ?: ""

    fun saveReportKey(ctx: Context, reportKey: String) {
        prefs(ctx).edit().putString(KEY_REPORT_KEY, reportKey).apply()
    }

    fun getIntervalMinutes(ctx: Context): Int =
        prefs(ctx).getInt(KEY_INTERVAL_MIN, DEFAULT_INTERVAL)

    fun saveInterval(ctx: Context, intervalMinutes: Int) {
        prefs(ctx).edit().putInt(KEY_INTERVAL_MIN, intervalMinutes).apply()
    }

    fun getDeviceName(ctx: Context): String =
        prefs(ctx).getString(KEY_DEVICE_NAME, "") ?: ""

    fun saveDeviceName(ctx: Context, name: String) {
        prefs(ctx).edit().putString(KEY_DEVICE_NAME, name).apply()
    }

    fun getThreshold(ctx: Context): Int =
        prefs(ctx).getInt(KEY_THRESHOLD, DEFAULT_THRESHOLD)

    fun saveThreshold(ctx: Context, threshold: Int) {
        prefs(ctx).edit().putInt(KEY_THRESHOLD, threshold).apply()
    }

    fun isOnboardingShown(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_ONBOARDING_SHOWN, false)

    fun setOnboardingShown(ctx: Context) {
        prefs(ctx).edit().putBoolean(KEY_ONBOARDING_SHOWN, true).apply()
    }

    fun isPremium(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_IS_PREMIUM, false)

    fun setPremium(ctx: Context, premium: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
    }

    fun isConnected(ctx: Context): Boolean =
        getReportKey(ctx).isNotBlank()
}
