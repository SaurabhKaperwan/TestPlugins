package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.metaproviders.TmdbProvider

class CineTmdbProvider: TmdbProvider() {
    override var name = "CineTmdb"
    override var mainUrl = "https://www.themoviedb.org"
    override var supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime,
        TvType.AsianDrama,
        TvType.Torrent
    )
    override var lang = "en"
    override val hasMainPage = true
    override val hasQuickSearch = false
    override val useMetaLoadResponse = true
    private val apiUrl = "https://api.themoviedb.org"

     override suspend fun loadLinks(
            data: String,
            isCasting: Boolean,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ): Boolean {
        callback.invoke(
            newExtractorLink(
                "json",
                "json",
                data
            )
        )
        return true
    }
}
