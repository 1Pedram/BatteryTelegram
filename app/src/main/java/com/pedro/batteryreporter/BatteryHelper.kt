package com.pedro.batteryreporter

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val powerSource: String
)

object BatteryHelper {
    fun getCurrentBattery(context: Context): BatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, filter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            0
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val powerSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charger"
            else -> if (isCharging) "Charger" else "Battery"
        }

        return BatteryInfo(percentage, isCharging, powerSource)
    }
}
