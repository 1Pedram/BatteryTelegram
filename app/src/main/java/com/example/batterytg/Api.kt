package com.example.batterytg

import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/** Tiny JSON HTTP helper. No external library, keeps the app small. */
object Api {
    private val bgExecutor = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    data class ApiResponse(val code: Int, val body: String)

    fun post(path: String, jsonBody: String, callback: (Result<ApiResponse>) -> Unit) {
        request("POST", path, jsonBody, callback)
    }

    fun get(path: String, callback: (Result<ApiResponse>) -> Unit) {
        request("GET", path, null, callback)
    }

    private fun request(method: String, path: String, jsonBody: String?, callback: (Result<ApiResponse>) -> Unit) {
        bgExecutor.execute {
            val result = runCatching {
                val conn = URL("${Prefs.SERVER_URL}$path").openConnection() as HttpURLConnection
                conn.requestMethod = method
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                if (jsonBody != null) {
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.outputStream.use { it.write(jsonBody.toByteArray()) }
                }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
                conn.disconnect()
                ApiResponse(code, body)
            }
            mainHandler.post { callback(result) }
        }
    }

    // Minimal field extraction, enough for this app's flat JSON responses.
    fun extractString(body: String, field: String): String? =
        Regex("\"$field\"\\s*:\\s*\"([^\"]*)\"").find(body)?.groupValues?.get(1)

    fun extractInt(body: String, field: String): Int? =
        Regex("\"$field\"\\s*:\\s*(\\d+)").find(body)?.groupValues?.get(1)?.toIntOrNull()

    fun extractBool(body: String, field: String): Boolean? =
        Regex("\"$field\"\\s*:\\s*(true|false)").find(body)?.groupValues?.get(1)?.toBoolean()
}
