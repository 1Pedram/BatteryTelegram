package com.example.batterytg

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<TextView>(R.id.versionText).text = "Version ${BuildConfig.VERSION_NAME}"

        findViewById<TextView>(R.id.privacyLink).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${Prefs.SERVER_URL}/privacy")))
        }

        findViewById<TextView>(R.id.botLink).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/BatteryReporterBot")))
        }
    }
}
