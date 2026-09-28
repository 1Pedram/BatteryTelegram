package com.example.batterytg

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var editHours: EditText
    private lateinit var editMinutes: EditText
    private lateinit var editBotToken: EditText
    private lateinit var editChatId: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editHours = findViewById(R.id.editHours)
        editMinutes = findViewById(R.id.editMinutes)
        editBotToken = findViewById(R.id.editBotToken)
        editChatId = findViewById(R.id.editChatId)
        statusText = findViewById(R.id.statusText)

        loadCurrentSettings()

        findViewById<Button>(R.id.btnSave).setOnClickListener { onSave() }
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            Scheduler.sendTestNow(applicationContext)
            statusText.text = "Test message queued. Check Telegram shortly."
        }
    }

    private fun loadCurrentSettings() {
        val totalMinutes = Prefs.getIntervalMinutes(applicationContext)
        editHours.setText((totalMinutes / 60).toString())
        editMinutes.setText((totalMinutes % 60).toString())

        val token = Prefs.getBotToken(applicationContext)
        val chat = Prefs.getChatId(applicationContext)
        editBotToken.setText(if (token == "YOUR_BOT_TOKEN") "" else token)
        editChatId.setText(if (chat == "YOUR_CHAT_ID") "" else chat)
    }

    private fun onSave() {
        val hours = editHours.text.toString().trim().toIntOrNull() ?: 0
        val minutes = editMinutes.text.toString().trim().toIntOrNull() ?: 0
        val token = editBotToken.text.toString().trim()
        val chatId = editChatId.text.toString().trim()

        val totalMinutes = hours * 60 + minutes

        if (totalMinutes < Scheduler.MIN_INTERVAL_MINUTES) {
            Toast.makeText(
                this,
                "Interval must be at least ${Scheduler.MIN_INTERVAL_MINUTES} minutes total.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (token.isBlank() || chatId.isBlank()) {
            Toast.makeText(this, "Enter both the bot token and chat ID.", Toast.LENGTH_LONG).show()
            return
        }

        Prefs.save(applicationContext, token, chatId, totalMinutes)
        Scheduler.schedule(applicationContext, totalMinutes)

        val h = totalMinutes / 60
        val m = totalMinutes % 60
        statusText.text = "Saved. Checking battery every ${h}h ${m}m."
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
    }
}
