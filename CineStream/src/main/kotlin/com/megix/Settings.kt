package com.megix

import android.app.AlertDialog
import android.content.Context
import android.widget.LinearLayout
import android.widget.Switch
import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey

object Settings {
    const val DOWNLOAD_ENABLE = "DownloadEnable"
    const val TORRENT_ENABLE = "TorrentEnable"

    fun showSettingsDialog(context: Context, onSave: () -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        val downloadToggle = Switch(context).apply {
            text = "Enable Download Only Links"
            textSize = 18f
            setPadding(0, 0, 0, 40)

            isChecked = getKey<Boolean>(DOWNLOAD_ENABLE) ?: false
            setOnCheckedChangeListener { _, isNowChecked ->
                setKey(DOWNLOAD_ENABLE, isNowChecked)
            }
        }
        layout.addView(downloadToggle)

        val torrentToggle = Switch(context).apply {
            text = "Enable Torrents"
            textSize = 18f

            isChecked = getKey<Boolean>(TORRENT_ENABLE) ?: false
            setOnCheckedChangeListener { _, isNowChecked ->
                setKey(TORRENT_ENABLE, isNowChecked)
            }
        }
        layout.addView(torrentToggle)

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
