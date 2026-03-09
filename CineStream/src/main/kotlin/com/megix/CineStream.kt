package com.megix

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.lagradost.cloudstream3.MainActivity
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
open class CineStream: Plugin() {
    override fun load(context: Context) {
        CineStreamStorage.init(context.applicationContext)
        registerMainAPI(CineStreamProvider())
        registerMainAPI(CineSimklProvider())
        registerMainAPI(CineTmdbProvider())
        registerExtractorAPI(Kwik())
        registerExtractorAPI(Pahe())
        registerExtractorAPI(SuperVideo())
        registerExtractorAPI(Akamaicdn())
        registerExtractorAPI(MegaUp())
        registerExtractorAPI(MegaUpTwoTwo())
        registerExtractorAPI(Fourspromax())
        registerExtractorAPI(Rapidairmax())
        registerExtractorAPI(HubCloud())
        registerExtractorAPI(VCloud())
        registerExtractorAPI(GDLink())
        registerExtractorAPI(GDFlixApp())
        registerExtractorAPI(GdFlix1())
        registerExtractorAPI(GdFlix2())
        registerExtractorAPI(GDFlixNet())
        registerExtractorAPI(GDFlix())
        registerExtractorAPI(fastdlserver())
        registerExtractorAPI(Hubdrive())
        registerExtractorAPI(Driveseed())
        registerExtractorAPI(Driveleech())
        registerExtractorAPI(Howblogs())
        registerExtractorAPI(Wootly())
        registerExtractorAPI(Gofile())

        this.openSettings = { ctx: Context ->
            Settings.showSettingsDialog(ctx as AppCompatActivity) {
                MainActivity.reloadHomeEvent.invoke(true)
            }
        }
    }
}
