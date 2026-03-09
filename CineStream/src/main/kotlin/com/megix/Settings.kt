package com.megix

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey

object Settings {
    // --- DATABASE KEYS ---

    // Scraping Keys
    const val DOWNLOAD_ENABLE = "DownloadEnable"
    const val TORRENT_ENABLE = "TorrentEnable"

    // Provider Keys
    const val PROVIDER_CINESTREAM = "ProviderCineStream"
    const val PROVIDER_SIMKL = "ProviderSimkl"
    const val PROVIDER_TMDB = "ProviderTmdb"

    // Cookie Keys
    private const val COOKIE_KEY = "nf_cookie"
    private const val TIMESTAMP_KEY = "nf_cookie_timestamp"

    // --- STORAGE FUNCTIONS (For NF Bypass) ---

    fun saveCookie(cookie: String) {
        setKey(COOKIE_KEY, cookie)
        setKey(TIMESTAMP_KEY, System.currentTimeMillis())
    }

    fun getCookie(): Pair<String?, Long> {
        val cookie = getKey<String>(COOKIE_KEY)
        val timestamp = getKey<Long>(TIMESTAMP_KEY) ?: 0L
        return Pair(cookie, timestamp)
    }

    fun clearCookie() {
        setKey(COOKIE_KEY, null)
        setKey(TIMESTAMP_KEY, null)
    }

    // --- UI POPUP DIALOG ---

    fun showSettingsDialog(context: Context, onSave: () -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        // Scraping Settings
        layout.addView(createHeader(context, "Scraping Settings"))
        layout.addView(createToggle(context, "Enable Download Only Links", DOWNLOAD_ENABLE, false))
        layout.addView(createToggle(context, "Enable Torrents", TORRENT_ENABLE, false))

        // Provider Settings
        layout.addView(createHeader(context, "Active Catalogs (Requires App Restart)"))
        layout.addView(createToggle(context, "Enable CineStream", PROVIDER_CINESTREAM, true))
        layout.addView(createToggle(context, "Enable Simkl", PROVIDER_SIMKL, true))
        layout.addView(createToggle(context, "Enable TMDB", PROVIDER_TMDB, true))

        // Clear Cookies Button
        val clearCookieButton = Button(context).apply {
            text = "Clear Saved Cookies"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 40, 0, 0)
            }
            setOnClickListener {
                clearCookie()
                Toast.makeText(context, "Cookies Cleared!", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(clearCookieButton)

        // Build and show the dialog
        AlertDialog.Builder(context)
            .setTitle("Plugin Settings")
            .setView(layout)
            .setPositiveButton("Save & Reload") { _, _ ->
                onSave()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- HELPER FUNCTIONS ---

    private fun createHeader(context: Context, title: String): TextView {
        return TextView(context).apply {
            text = title
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#8842f3"))
            setPadding(0, 30, 0, 10)
        }
    }

    private fun createToggle(context: Context, label: String, databaseKey: String, defaultState: Boolean): Switch {
        return Switch(context).apply {
            text = label
            textSize = 16f
            setPadding(0, 10, 0, 10)

            // Read state from database, fallback to defaultState if it hasn't been set
            isChecked = getKey<Boolean>(databaseKey) ?: defaultState

            setOnCheckedChangeListener { _, isNowChecked ->
                setKey(databaseKey, isNowChecked)
            }
        }
    }
}
