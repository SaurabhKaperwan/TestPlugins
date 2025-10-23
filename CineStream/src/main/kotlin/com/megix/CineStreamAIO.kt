package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class CineStreamAIOProvider : MainAPI() {
    override var mainUrl = "https://aiometadata.elfhosted.com/stremio/9197a4a9-2f5b-4911-845e-8704c520bdf7"
    override var name = "CineStreamAIO"
    override val hasMainPage = true
    override var lang = "en"
    override val hasDownloadSupport = true
    private val skipMap: MutableMap<String, Int> = mutableMapOf()
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.AsianDrama,
        TvType.Anime,
        TvType.Torrent,
    )
    val gson by lazy { Gson() }

    override val mainPage = mainPageOf(
        "$mainUrl/catalog/movie/tmdb.trending/skip=###" to "Top Movies",
        "$mainUrl/catalog/series/tmdb.trending/skip=###" to "Top Series",
        "$mainUrl/catalog/anime/mal.airing/skip=###" to "Airing Anime",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        if(page == 1) skipMap.clear()
        val skip = skipMap[request.name] ?: 0
        val newRequestData = request.data.replace("###", skip.toString())
        val json = app.get("$newRequestData.json").text
        val movies = gson.fromJson(json, Home::class.java)
        val movieCount = movies.metas.size
        skipMap[request.name] = skip + movieCount
        val home = movies.metas.mapNotNull { movie ->
            val title = movie.name ?: ""
            newMovieSearchResponse(title, PassData(movie.id, movie.type).toJson()) {
                this.posterUrl = movie.poster
                this.score = Score.from10(movie.imdbRating)
            }
        }
        return newHomePageResponse(
            list = HomePageList(
                name = request.name,
                list = home,
            ),
            hasNext = movies.hasMore
        )
    }

    override suspend fun search(query: String): List<SearchResponse> = coroutineScope {

        suspend fun fetchResults(url: String): List<SearchResponse> {
            val result = runCatching {
                val json = app.get(url).text
                gson.fromJson(json, SearchResult::class.java).map {
                    val title = it.name ?: ""
                    newMovieSearchResponse(title, PassData(it.id, it.type).toJson()).apply {
                        posterUrl = it.poster
                        this.score = Score.from10(it.imdbRating)
                    }
                } ?: emptyList()
            }.getOrDefault(emptyList())

            if (result.isNotEmpty()) return result
            return emptyList()
        }

        val endpoints = listOf(
            "$mainUrl/catalog/movie/search/search=$query.json"
            "$mainUrl/catalog/series/search/search=$query.json",
            "$mainUrl/catalog/anime.series/search/search=$query.json",
            "$mainUrl/catalog/anime.movie/search/search=$query.json"
        )

        val resultsLists = endpoints.map {
            async { fetchResults(it) }
        }.awaitAll()

        val maxSize = resultsLists.maxOfOrNull { it.size } ?: 0

        buildList {
            for (i in 0 until maxSize) {
                for (list in resultsLists) {
                    if (i < list.size) add(list[i])
                }
            }
        }

    }

    data class PassData(
        val id: String,
        val type: String,
    )

    data class Meta(
        val id: String?,
        val imdb_id: String?,
        val type: String?,
        val aliases: ArrayList<String>?,
        val poster: String?,
        val background: String?,
        val moviedb_id: Int?,
        val name: String?,
        val description: String?,
        val genre: List<String>?,
        val genres: List<String>?,
        val releaseInfo: String?,
        val status: String?,
        val runtime: String?,
        val cast: List<String>?,
        val app_extras: AppExtras? = null,
        val language: String?,
        val country: String?,
        val imdbRating: String?,
        val year: String?,
        val videos: List<EpisodeDetails>?,
    )

    data class AppExtras(
        val cast: List<Cast> = emptyList()
    )

    data class Cast(
        val name      : String? = null,
        val character : String? = null,
        val photo     : String? = null
    )

    data class SearchResult(
        val metas: List<Media>
    )

    data class Media(
        val id: String,
        val type: String,
        val name: String?,
        val poster: String?,
        val description: String?,
        val imdbRating: String?,
        val aliases: ArrayList<String>?,
    )

    data class EpisodeDetails(
        val id: String?,
        val name: String?,
        val title: String?,
        val season: Int,
        val episode: Int,
        val rating: String?,
        val released: String?,
        val firstAired: String?,
        val overview: String?,
        val thumbnail: String?,
        val moviedb_id: Int?,
        val imdb_id: String?,
        val imdbSeason: Int?,
        val imdbEpisode: Int?,
    )

    data class ResponseData(
        val meta: Meta,
    )

    data class Home(
        val metas: List<Media>,
        val hasMore: Boolean = false,
    )
}
