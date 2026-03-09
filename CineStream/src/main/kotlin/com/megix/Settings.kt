package com.megix

import android.app.AlertDialog
import android.content.Context
import android.widget.LinearLayout
import android.widget.Switch
import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey

object Settings {
    const val DOWNLOAD_ENABLE = "DownloadEnable"

    fun showSettingsDialog(context: Context, onSave: () -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        val toggle = Switch(context).apply {
            text = "Enable Download only links"
            textSize = 18f
            isChecked = getKey<Boolean>(DOWNLOAD_ENABLE) ?: false

            setOnCheckedChangeListener { _, isNowChecked ->
                setKey(DOWNLOAD_ENABLE, isNowChecked)
            }
        }

        layout.addView(toggle)

        AlertDialog.Builder(context)
            .setTitle("Provider Settings")
            .setView(layout)
            .setPositiveButton("Save & Reload") { _, _ ->
                onSave()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
