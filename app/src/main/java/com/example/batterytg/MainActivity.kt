package com.example.batterytg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var editCode: EditText
    private lateinit var editHours: EditText
    private lateinit var editMinutes: EditText
    private lateinit var editDeviceName: EditText
    private lateinit var editThreshold: EditText
    private lateinit var statusText: TextView
    private lateinit var connectionText: TextView
    private lateinit var devicesContainer: LinearLayout
    private lateinit var deviceLimitText: TextView

    private var billingManager: BillingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Prefs.isOnboardingShown(applicationContext)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        editCode = findViewById(R.id.editCode)
        editHours = findViewById(R.id.editHours)
        editMinutes = findViewById(R.id.editMinutes)
        editDeviceName = findViewById(R.id.editDeviceName)
        editThreshold = findViewById(R.id.editThreshold)
        statusText = findViewById(R.id.statusText)
        connectionText = findViewById(R.id.connectionText)
        devicesContainer = findViewById(R.id.devicesContainer)
        deviceLimitText = findViewById(R.id.deviceLimitText)

        loadCurrentSettings()

        findViewById<TextView>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        findViewById<Button>(R.id.btnVerify).setOnClickListener { onVerify() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { onSaveDeviceSettings() }
        findViewById<Button>(R.id.btnUpgrade).setOnClickListener { onUpgrade() }
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            if (!Prefs.isConnected(applicationContext)) {
                Toast.makeText(this, "Verify your code first.", Toast.LENGTH_LONG).show()
            } else {
                Scheduler.sendTestNow(applicationContext)
                statusText.text = "Test message queued. Check Telegram shortly."
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Prefs.isConnected(applicationContext)) refreshDeviceList()
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager?.endConnection()
    }

    private fun loadCurrentSettings() {
        val totalMinutes = Prefs.getIntervalMinutes(applicationContext)
        editHours.setText((totalMinutes / 60).toString())
        editMinutes.setText((totalMinutes % 60).toString())
        editThreshold.setText(Prefs.getThreshold(applicationContext).toString())

        val savedName = Prefs.getDeviceName(applicationContext)
        editDeviceName.setText(savedName.ifBlank { defaultDeviceName() })

        refreshConnectionLabel()
    }

    private fun defaultDeviceName(): String {
        return try {
            Settings.Global.getString(contentResolver, "device_name") ?: android.os.Build.MODEL
        } catch (e: Exception) {
            android.os.Build.MODEL
        }
    }

    private fun refreshConnectionLabel() {
        connectionText.text = if (Prefs.isConnected(applicationContext)) "Connected ✓" else "Not connected yet"
    }

    private fun refreshDeviceList() {
        val key = Prefs.getReportKey(applicationContext)
        Api.get("/devices?key=$key") { result ->
            result.onSuccess { resp ->
                if (resp.code != 200) return@onSuccess
                val devices = DeviceListParser.parseDevices(resp.body)
                val limit = Api.extractInt(resp.body, "limit") ?: 1
                val premium = Api.extractBool(resp.body, "premium") ?: false
                Prefs.setPremium(applicationContext, premium)
                renderDeviceList(devices, limit, premium)
            }
        }
    }

    private fun renderDeviceList(devices: List<DeviceInfo>, limit: Int, premium: Boolean) {
        devicesContainer.removeAllViews()
        for (device in devices) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_device, devicesContainer, false)
            val h = device.interval / 60
            val m = device.interval % 60
            row.findViewById<TextView>(R.id.deviceRowName).text =
                device.name + if (device.isThisDevice) " (this device)" else ""
            row.findViewById<TextView>(R.id.deviceRowDetail).text =
                "Every ${h}h ${m}m · alert below ${device.threshold}%"
            row.findViewById<TextView>(R.id.deviceRowRemove).setOnClickListener {
                removeDevice(device.key)
            }
            devicesContainer.addView(row)
        }
        deviceLimitText.text = "${devices.size} of $limit devices connected" +
            if (!premium) " (free plan)" else " (premium)"
        findViewById<Button>(R.id.btnUpgrade).visibility =
            if (premium) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun removeDevice(targetKey: String) {
        val requesterKey = Prefs.getReportKey(applicationContext)
        Api.post("/remove-device", """{"requesterKey":"$requesterKey","targetKey":"$targetKey"}""") { result ->
            result.onSuccess { resp ->
                if (resp.code == 200) {
                    statusText.text = "Device removed."
                    if (targetKey == requesterKey) {
                        Prefs.saveReportKey(applicationContext, "")
                        refreshConnectionLabel()
                        devicesContainer.removeAllViews()
                    } else {
                        refreshDeviceList()
                    }
                } else {
                    statusText.text = "Couldn't remove that device."
                }
            }
        }
    }

    private fun onVerify() {
        val code = editCode.text.toString().trim()
        if (code.isBlank()) {
            Toast.makeText(this, "Enter the code the bot sent you.", Toast.LENGTH_LONG).show()
            return
        }
        val name = editDeviceName.text.toString().trim().ifBlank { defaultDeviceName() }

        statusText.text = "Verifying..."
        val escapedName = name.replace("\"", "'")
        Api.post("/verify", """{"code":"$code","name":"$escapedName"}""") { result ->
            result.onSuccess { resp ->
                when (resp.code) {
                    200 -> {
                        val key = Api.extractString(resp.body, "key")
                        if (key != null) {
                            Prefs.saveReportKey(applicationContext, key)
                            Prefs.saveDeviceName(applicationContext, name)
                            refreshConnectionLabel()
                            statusText.text = "Verified. You're connected."
                            onSaveDeviceSettings() // push interval/threshold to the server right away
                            refreshDeviceList()
                        } else {
                            statusText.text = "Unexpected response from server."
                        }
                    }
                    409 -> {
                        val limit = Api.extractInt(resp.body, "limit") ?: 1
                        statusText.text = "You've reached your limit of $limit device(s). " +
                            "Remove one from another connected device's app, or upgrade to Premium."
                    }
                    else -> statusText.text = "Verification failed: ${resp.body}"
                }
            }.onFailure {
                statusText.text = "Couldn't reach the server. Check your connection."
            }
        }
    }

    private fun onSaveDeviceSettings() {
        val hours = editHours.text.toString().trim().toIntOrNull() ?: 0
        val minutes = editMinutes.text.toString().trim().toIntOrNull() ?: 0
        val totalMinutes = hours * 60 + minutes
        val threshold = editThreshold.text.toString().trim().toIntOrNull()?.coerceIn(1, 99) ?: 20
        val name = editDeviceName.text.toString().trim().ifBlank { defaultDeviceName() }

        if (totalMinutes < Scheduler.MIN_INTERVAL_MINUTES) {
            Toast.makeText(
                this,
                "Interval must be at least ${Scheduler.MIN_INTERVAL_MINUTES} minutes total.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Prefs.saveInterval(applicationContext, totalMinutes)
        Prefs.saveThreshold(applicationContext, threshold)
        Prefs.saveDeviceName(applicationContext, name)
        Scheduler.schedule(applicationContext, totalMinutes)

        if (Prefs.isConnected(applicationContext)) {
            val key = Prefs.getReportKey(applicationContext)
            val escapedName = name.replace("\"", "'")
            Api.post(
                "/settings",
                """{"key":"$key","name":"$escapedName","interval":$totalMinutes,"threshold":$threshold}"""
            ) { }
        }

        val h = totalMinutes / 60
        val m = totalMinutes % 60
        statusText.text = "Saved. Checking every ${h}h ${m}m, alert below $threshold%."
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun onUpgrade() {
        if (!Prefs.isConnected(applicationContext)) {
            Toast.makeText(this, "Connect this device first.", Toast.LENGTH_LONG).show()
            return
        }
        statusText.text = "Opening purchase screen..."
        billingManager = BillingManager(this) { success, message ->
            statusText.text = message
            if (success) refreshDeviceList()
        }
        billingManager?.startPurchaseFlow()
    }
}
