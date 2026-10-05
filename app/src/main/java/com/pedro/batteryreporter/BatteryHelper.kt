package com.pedro.batteryreporter

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class DeviceBatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val powerSource: String
)

object BatteryHelper {
    fun getCurrentBattery(context: Context): DeviceBatteryInfo {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, iFilter)

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 0

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val powerSource = when {
            !isCharging -> "Battery"
            chargePlug == BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
            chargePlug == BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> "Charger"
        }

        return DeviceBatteryInfo(percentage, isCharging, powerSource)
    }
}
