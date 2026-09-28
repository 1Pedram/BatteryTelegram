package com.example.batterytg

import android.content.Context

object Prefs {
    private const val FILE = "battery_tg_prefs"
    private const val KEY_TOKEN = "bot_token"
    private const val KEY_CHAT = "chat_id"
    private const val KEY_INTERVAL_MIN = "interval_minutes"

    // Defaults: 3 hours, and the values you started with (change them any time in the app).
    private const val DEFAULT_TOKEN = "YOUR_BOT_TOKEN"
    private const val DEFAULT_CHAT = "YOUR_CHAT_ID"
    private const val DEFAULT_INTERVAL = 180

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getBotToken(ctx: Context): String =
        prefs(ctx).getString(KEY_TOKEN, DEFAULT_TOKEN) ?: DEFAULT_TOKEN

    fun getChatId(ctx: Context): String =
        prefs(ctx).getString(KEY_CHAT, DEFAULT_CHAT) ?: DEFAULT_CHAT

    fun getIntervalMinutes(ctx: Context): Int =
        prefs(ctx).getInt(KEY_INTERVAL_MIN, DEFAULT_INTERVAL)

    fun save(ctx: Context, token: String, chatId: String, intervalMinutes: Int) {
        prefs(ctx).edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_CHAT, chatId)
            .putInt(KEY_INTERVAL_MIN, intervalMinutes)
            .apply()
    }
}
