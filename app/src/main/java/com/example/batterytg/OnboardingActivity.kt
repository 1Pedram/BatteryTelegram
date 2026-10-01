package com.example.batterytg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.app.Activity

class OnboardingActivity : Activity() {

    private data class Page(val title: String, val body: String)

    private val pages = listOf(
        Page(
            "Welcome to Battery Reporter",
            "Keep an eye on a phone or tablet's battery from Telegram — your own device, or one you look after for your kids.\n\nInstall this app on the device you want to monitor. You'll get battery updates in a Telegram chat, automatically, on the schedule you choose."
        ),
        Page(
            "Free and Premium",
            "Free: connect 1 device.\nPremium: connect up to 5 devices — handy for a whole family.\n\nYou only need Telegram on the phone that receives the alerts. The devices being monitored don't need Telegram installed at all."
        ),
        Page(
            "Set it up in 3 steps",
            "1. In Telegram, search for @BatteryReporterBot and tap Start.\n2. The bot sends you a 6-digit code.\n3. Enter that code in this app, on the device you want monitored, and tap Verify.\n\nYou can also set a low-battery alert — if the level drops below it, you'll get a bold warning message right away."
        )
    )

    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        showPage(0)

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            if (currentPage < pages.size - 1) {
                showPage(currentPage + 1)
            } else {
                Prefs.setOnboardingShown(applicationContext)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        findViewById<Button>(R.id.btnSkip).setOnClickListener {
            Prefs.setOnboardingShown(applicationContext)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showPage(index: Int) {
        currentPage = index
        val page = pages[index]
        findViewById<TextView>(R.id.onboardingTitle).text = page.title
        findViewById<TextView>(R.id.onboardingBody).text = page.body
        findViewById<TextView>(R.id.pageIndicator).text = "${index + 1} / ${pages.size}"
        findViewById<Button>(R.id.btnNext).text = if (index == pages.size - 1) "Get Started" else "Next"
    }
}
