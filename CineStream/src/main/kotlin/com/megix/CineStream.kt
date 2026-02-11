package com.megix

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
open class CineStream: Plugin() {

    companion object {
        var superstreamCookie: String = ""
    }

    override fun load(context: Context) {
        CineStreamStorage.init(context.applicationContext)

        // --- ADDED: Load the cookie from memory when app starts ---
        val prefs = context.getSharedPreferences("cinestream_settings", Context.MODE_PRIVATE)
        superstreamCookie = prefs.getString("superstream_cookie", "") ?: ""
        // ----------------------------------------------------------

        registerMainAPI(CineStreamProvider())
        registerMainAPI(CineSimklProvider())
        registerMainAPI(CineTmdbProvider())
        registerExtractorAPI(Kwik())
        registerExtractorAPI(Animezia())
        registerExtractorAPI(server2())
        registerExtractorAPI(MultimoviesAIO())
        registerExtractorAPI(Strwishcom())
        registerExtractorAPI(CdnwishCom())
        registerExtractorAPI(Dhcplay())
        registerExtractorAPI(Asnwish())
        registerExtractorAPI(Multimovies())
        registerExtractorAPI(Pahe())
        registerExtractorAPI(Smoothpre())
        registerExtractorAPI(Ryderjet())
        registerExtractorAPI(SuperVideo())
        registerExtractorAPI(MixDropPs())
        registerExtractorAPI(MixDropTo())
        registerExtractorAPI(MixDropSi())
        registerExtractorAPI(Dlions())
        registerExtractorAPI(Watchadsontape())
        registerExtractorAPI(Vembed())
        registerExtractorAPI(Multimoviesshg())
        registerExtractorAPI(Akamaicdn())
        registerExtractorAPI(VidHideHub())
        registerExtractorAPI(Rapidairmax())
        registerExtractorAPI(MegaUp())
        registerExtractorAPI(MegaUpTwoTwo())
        registerExtractorAPI(Fourspromax())
        registerExtractorAPI(Vidmolybiz())
        registerExtractorAPI(Vidmolynet())
        registerExtractorAPI(Cybervynx())
        registerExtractorAPI(Mivalyo())
        registerExtractorAPI(Mdy())
        registerExtractorAPI(Dsvplay())
        registerExtractorAPI(Luluvdoo())
        registerExtractorAPI(Doodspro())
    }
}
