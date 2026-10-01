package com.example.batterytg

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private lateinit var editServerUrl: EditText
    private lateinit var editCode: EditText
    private lateinit var editHours: EditText
    private lateinit var editMinutes: EditText
    private lateinit var statusText: TextView
    private lateinit var connectionText: TextView

    private val bgExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editServerUrl = findViewById(R.id.editServerUrl)
        editCode = findViewById(R.id.editCode)
        editHours = findViewById(R.id.editHours)
        editMinutes = findViewById(R.id.editMinutes)
        statusText = findViewById(R.id.statusText)
        connectionText = findViewById(R.id.connectionText)

        loadCurrentSettings()

        findViewById<Button>(R.id.btnVerify).setOnClickListener { onVerify() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { onSaveInterval() }
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            if (!Prefs.isConnected(applicationContext)) {
                Toast.makeText(this, "Verify your code first.", Toast.LENGTH_LONG).show()
            } else {
                Scheduler.sendTestNow(applicationContext)
                statusText.text = "Test message queued. Check Telegram shortly."
            }
        }

        editServerUrl.setText(Prefs.getServerUrl(applicationContext))
    }

    private fun loadCurrentSettings() {
        val totalMinutes = Prefs.getIntervalMinutes(applicationContext)
        editHours.setText((totalMinutes / 60).toString())
        editMinutes.setText((totalMinutes % 60).toString())
        refreshConnectionLabel()
    }

    private fun refreshConnectionLabel() {
        connectionText.text = if (Prefs.isConnected(applicationContext)) {
            "Connected ✓"
        } else {
            "Not connected yet"
        }
    }

    private fun onVerify() {
        val serverUrl = editServerUrl.text.toString().trim().trimEnd('/')
        val code = editCode.text.toString().trim()

        if (serverUrl.isBlank()) {
            Toast.makeText(this, "Enter your server URL.", Toast.LENGTH_LONG).show()
            return
        }
        if (code.isBlank()) {
            Toast.makeText(this, "Enter the code the bot sent you.", Toast.LENGTH_LONG).show()
            return
        }

        statusText.text = "Verifying..."
        bgExecutor.execute {
            val result = runCatching {
                val conn = URL("$serverUrl/verify").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                conn.outputStream.use { it.write("""{"code":"$code"}""".toByteArray()) }

                val responseCode = conn.responseCode
                val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                val body = stream.bufferedReader().use { it.readText() }
                conn.disconnect()
                Pair(responseCode, body)
            }

            mainHandler.post {
                result.onSuccess { (code2, body) ->
                    if (code2 == 200) {
                        val key = Regex("\"key\"\\s*:\\s*\"([a-f0-9]+)\"").find(body)?.groupValues?.get(1)
                        if (key != null) {
                            Prefs.saveConnection(applicationContext, serverUrl, key)
                            refreshConnectionLabel()
                            statusText.text = "Verified. You're connected."
                        } else {
                            statusText.text = "Unexpected response from server."
                        }
                    } else {
                        statusText.text = "Verification failed: $body"
                    }
                }.onFailure {
                    statusText.text = "Couldn't reach the server. Check the URL and your connection."
                }
            }
        }
    }

    private fun onSaveInterval() {
        val hours = editHours.text.toString().trim().toIntOrNull() ?: 0
        val minutes = editMinutes.text.toString().trim().toIntOrNull() ?: 0
        val totalMinutes = hours * 60 + minutes

        if (totalMinutes < Scheduler.MIN_INTERVAL_MINUTES) {
            Toast.makeText(
                this,
                "Interval must be at least ${Scheduler.MIN_INTERVAL_MINUTES} minutes total.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Prefs.saveInterval(applicationContext, totalMinutes)
        Scheduler.schedule(applicationContext, totalMinutes)

        val h = totalMinutes / 60
        val m = totalMinutes % 60
        statusText.text = "Saved. Checking battery every ${h}h ${m}m."
        Toast.makeText(this, "Interval saved", Toast.LENGTH_SHORT).show()
    }
}
