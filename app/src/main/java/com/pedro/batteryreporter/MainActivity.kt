package com.pedro.batteryreporter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pedro.batteryreporter.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsManager
    private val client = OkHttpClient()

    // Live receiver that listens to charging & battery changes in real time
    private val liveBatteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateLiveStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)
        setupUI()
        updateLiveStatus()
    }

    override fun onResume() {
        super.onResume()
        updateLiveStatus()
        // Register receiver for instant live changes when plugging/unplugging
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(liveBatteryReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(liveBatteryReceiver)
        } catch (_: Exception) {}
    }

    private fun setupUI() {
        binding.deviceNameInput.setText(prefs.deviceName)
        binding.thresholdSlider.value = prefs.alertThreshold.toFloat()
        binding.thresholdLabel.text = "Alert below: ${prefs.alertThreshold}%"

        if (prefs.deviceKey != null) {
            binding.unlinkedCard.visibility = View.GONE
            binding.linkedCard.visibility = View.VISIBLE
        } else {
            binding.unlinkedCard.visibility = View.VISIBLE
            binding.linkedCard.visibility = View.GONE
        }

        binding.thresholdSlider.addOnChangeListener { _, value, _ ->
            prefs.alertThreshold = value.toInt()
            binding.thresholdLabel.text = "Alert below: ${value.toInt()}%"
        }

        binding.verifyButton.setOnClickListener {
            val code = binding.codeInput.text.toString().trim()
            val name = binding.deviceNameInput.text.toString().trim().ifEmpty { prefs.deviceName }
            if (code.length != 6) {
                Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.deviceName = name
            verifyDeviceWithServer(code, name, replace = false)
        }

        binding.sendTestButton.setOnClickListener {
            triggerInstantReport()
        }

        binding.unlinkButton.setOnClickListener {
            prefs.clear()
            setupUI()
            Toast.makeText(this, "Device disconnected locally", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLiveStatus() {
        val info = BatteryHelper.getCurrentBattery(this)
        binding.batteryText.text = "${info.percentage}%"
        binding.powerSourceText.text = if (info.isCharging) {
            "⚡ Charging via ${info.powerSource}"
        } else {
            "🔋 On Battery"
        }
    }

    private fun verifyDeviceWithServer(code: String, name: String, replace: Boolean) {
        binding.verifyProgress.visibility = View.VISIBLE
        binding.verifyButton.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("code", code)
                put("name", name)
                put("replace", replace)
            }

            val request = Request.Builder()
                .url("https://battery-reporter.1pedro.workers.dev/verify")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                val bodyStr = response.body?.string() ?: ""
                val resObj = JSONObject(bodyStr)

                withContext(Dispatchers.Main) {
                    binding.verifyProgress.visibility = View.GONE
                    binding.verifyButton.isEnabled = true

                    if (response.isSuccessful && resObj.has("key")) {
                        prefs.deviceKey = resObj.getString("key")
                        setupUI()
                        Toast.makeText(this@MainActivity, "Connected to Telegram Bot!", Toast.LENGTH_SHORT).show()
                        triggerInstantReport()
                    } else if (response.code == 409) {
                        Toast.makeText(this@MainActivity, "Device limit reached on your Telegram plan!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error: ${resObj.optString("error", "Invalid code")}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.verifyProgress.visibility = View.GONE
                    binding.verifyButton.isEnabled = true
                    Toast.makeText(this@MainActivity, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun triggerInstantReport() {
        val key = prefs.deviceKey ?: return
        val info = BatteryHelper.getCurrentBattery(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("key", key)
                put("battery", info.percentage)
                put("isCharging", info.isCharging)
                put("powerSource", info.powerSource)
                put("state", "MANUAL")
            }
            val request = Request.Builder()
                .url("https://battery-reporter.1pedro.workers.dev/report")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().close()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Status sent to Telegram", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
