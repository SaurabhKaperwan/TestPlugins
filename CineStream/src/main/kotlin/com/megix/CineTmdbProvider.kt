package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.metaproviders.TmdbProvider
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.metaproviders.TmdbLink

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

    companion object {
        private const val apiKey = BuildConfig.TMDB_KEY
    }

    override val mainPage = mainPageOf(
        "trending/all/day?api_key=$apiKey&region=US" to "Trending",
        "trending/movie/week?api_key=$apiKey&region=US&with_original_language=en" to "Popular Movies",
        "trending/tv/week?api_key=$apiKey&region=US&with_original_language=en" to "Popular TV Shows",
        "tv/airing_today?api_key=$apiKey&region=US&with_original_language=en" to "Airing Today TV Shows",
        "discover/tv?api_key=$apiKey&with_networks=213" to "Netflix",
        "discover/tv?api_key=$apiKey&with_networks=1024" to "Amazon",
        "discover/tv?api_key=$apiKey&with_networks=2739" to "Disney+",
        "discover/tv?api_key=$apiKey&with_networks=453" to "Hulu",
        "discover/tv?api_key=$apiKey&with_networks=2552" to "Apple TV+",
        "discover/tv?api_key=$apiKey&with_networks=49" to "HBO",
        "discover/tv?api_key=$apiKey&with_networks=4330" to "Paramount+",
        "discover/tv?api_key=$apiKey&with_networks=3353" to "Peacock",
        "discover/movie?api_key=$apiKey&language=en-US&page=1&sort_by=popularity.desc&with_origin_country=IN&release_date.gte=${getDate().lastWeekStart}&release_date.lte=${getDate().today}" to "Trending Indian Movies",
        "discover/tv?api_key=$apiKey&with_keywords=210024|222243&sort_by=popularity.desc&air_date.lte=${getDate().today}&air_date.gte=${getDate().today}" to "Airing Today Anime",
        "discover/tv?api_key=$apiKey&with_keywords=210024|222243&sort_by=popularity.desc&air_date.lte=${getDate().nextWeek}&air_date.gte=${getDate().today}" to "On The Air Anime",
        "discover/movie?api_key=$apiKey&with_keywords=210024|222243" to "Anime Movies",
        "movie/top_rated?api_key=$apiKey&region=US" to "Top Rated Movies",
        "tv/top_rated?api_key=$apiKey&region=US" to "Top Rated TV Shows",
        "discover/tv?api_key=$apiKey&with_original_language=ko" to "Korean Shows",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val type = if (request.data.contains("/movie")) "movie" else "tv"
        val home = app.get("$apiUrl/${request.data}&page=$page", timeout = 10000)
            .parsedSafe<Results>()?.results?.mapNotNull { media ->
                media.toSearchResponse(type)
            } ?: throw ErrorLoadingException("Invalid Json reponse")
        return newHomePageResponse(request.name, home)
    }

     private fun Media.toSearchResponse(type: String? = null): SearchResponse? {
        return newMovieSearchResponse(
            title ?: name ?: originalTitle ?: return null,
            Data(id = id, type = mediaType ?: type).toJson(),
            TvType.Movie,
        ) {
            this.posterUrl = getImageUrl(posterPath)
            this.score= Score.from10(voteAverage)
        }
    }

    private fun getImageUrl(link: String?): String? {
        if (link == null) return null
        return if (link.startsWith("/")) "https://image.tmdb.org/t/p/w500/$link" else link
    }


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

    data class Data(
        val id: Int? = null,
        val type: String? = null,
        val aniId: String? = null,
        val malId: Int? = null,
    )

    data class Results(
        @JsonProperty("results") val results: ArrayList<Media>? = arrayListOf(),
    )

    data class Media(
        @JsonProperty("id") val id: Int? = null,
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("original_title") val originalTitle: String? = null,
        @JsonProperty("media_type") val mediaType: String? = null,
        @JsonProperty("poster_path") val posterPath: String? = null,
        @JsonProperty("vote_average") val voteAverage: Double? = null,
    )
}
