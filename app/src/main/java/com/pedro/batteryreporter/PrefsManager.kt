package com.pedro.batteryreporter

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("battery_reporter_prefs", Context.MODE_PRIVATE)

    var deviceKey: String?
        get() = prefs.getString("device_key", null)
        set(value) = prefs.edit().putString("device_key", value).apply()

    var deviceName: String
        get() = prefs.getString("device_name", android.os.Build.MODEL) ?: android.os.Build.MODEL
        set(value) = prefs.edit().putString("device_name", value).apply()

    var alertThreshold: Int
        get() = prefs.getInt("alert_threshold", 20)
        set(value) = prefs.edit().putInt("alert_threshold", value).apply()

    var lastChargingState: Boolean
        get() = prefs.getBoolean("last_charging_state", false)
        set(value) = prefs.edit().putBoolean("last_charging_state", value).apply()

    var lastReportedState: String
        get() = prefs.getString("last_reported_state", "NORMAL") ?: "NORMAL"
        set(value) = prefs.edit().putString("last_reported_state", value).apply()

    var lastReportedBattery: Int
        get() = prefs.getInt("last_reported_battery", -1)
        set(value) = prefs.edit().putInt("last_reported_battery", value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
