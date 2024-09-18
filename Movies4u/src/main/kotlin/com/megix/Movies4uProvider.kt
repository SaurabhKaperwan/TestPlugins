package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.lagradost.cloudstream3.LoadResponse.Companion.addImdbUrl
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.google.gson.Gson
import com.lagradost.cloudstream3.utils.AppUtils.parseJson

class Movies4uProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://movies4u.taxi"
    override var name = "Movies4u"
    override val hasMainPage = true
    override var lang = "hi"
    override val hasDownloadSupport = true
    val cinemeta_url = "https://v3-cinemeta.strem.io/meta"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.AsianDrama,
        TvType.Anime
    )

    override val mainPage = mainPageOf(
        "$mainUrl/page/" to "Home"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select("article.post").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("figure > a > img").attr("alt")
        val href = this.selectFirst("figure > a").attr("href")
        val posterUrl = this.selectFirst("figure > a > img").attr("src").toString()
    
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..3) {
            val document = app.get("$mainUrl/page/$i/?s=$query").document

            val results = document.select("article.post").mapNotNull { it.toSearchResult() }

            if (results.isEmpty()) {
                break
            }
            searchResponse.addAll(results)
        }

        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        var title = document.selectFirst("title").text()
        var posterUrl = document.selectFirst("meta[property=og:image]").attr("content")

        val movieTitle = document.selectFirst("h3.movie-title")
        val nextElement = movieTitle?.nextElementSibling()
        val imdbUrl = nextElement?.selectFirst("a")?.attr("href")
        var description = nextElement?.nextElementSibling()?.nextElementSibling() ?.text()

        val tvtype = if (nextElement?.text().contains("Movie Name")) {
            "movie"
        } else {
            "series"
        }

        val responseData = if (!imdbUrl.isNullOrEmpty()) {
            val imdbId = imdbUrl.substringAfter("title/").substringBefore("/")
            val jsonResponse = app.get("$cinemeta_url/$tvtype/$imdbId.json").text
            if(jsonResponse.isNotEmpty() && jsonResponse.startsWith("{")) {
                val gson = Gson()
                gson.fromJson(jsonResponse, ResponseData::class.java)
            }
            else {
                null
            }
        } else {
            null
        }

        var cast: List<String> = emptyList()
        var genre: List<String> = emptyList()
        var imdbRating: String = ""
        var year: String = ""
        var background: String = posterUrl

        if(responseData != null) {
            description = responseData.meta?.description ?: description
            cast = responseData.meta?.cast ?: emptyList()
            title = responseData.meta?.name ?: title
            genre = responseData.meta?.genre ?: emptyList()
            imdbRating = responseData.meta?.imdbRating ?: ""
            year = responseData.meta?.year ?: ""
            posterUrl = responseData.meta?.poster ?: posterUrl
            background = responseData.meta?.background ?: background
        }

        // if(tvtype == "series") {
        //     val tvSeriesEpisodes = mutableListOf<Episode>()
        //     val episodesMap: MutableMap<Pair<Int, Int>, List<String>> = mutableMapOf()
        //     var buttons = document.select("h5 > a")
        //         .filter { element -> !element.text().contains("Zip", true) }


        //     buttons.forEach { button ->
        //         val titleElement = button.parent() ?. previousElementSibling()
        //         val mainTitle = titleElement ?. text() ?: ""
        //         val realSeasonRegex = Regex("""(?:Season |S)(\d+)""")
        //         val realSeason = realSeasonRegex.find(mainTitle.toString()) ?. groupValues ?. get(1) ?.toInt() ?: 0
        //         val episodeLink = button.attr("href") ?: ""

        //         val doc = app.get(episodeLink).document
        //         var elements = doc.select("span:matches((?i)(Ep))")
        //         if(elements.isEmpty()) {
        //             elements = doc.select("a:matches((?i)(HubCloud))")
        //         }
        //         var e = 1

        //         elements.forEach { element ->
        //             if(element.tagName() == "span") {
        //                 val titleTag = element.parent()
        //                 var hTag = titleTag?.nextElementSibling()
        //                 e = Regex("""Ep(\d{2})""").find(element.toString())?.groups?.get(1)?.value ?.toIntOrNull() ?: e
        //                 while (hTag != null && hTag.text().contains("HubCloud", ignoreCase = true)) {
        //                     val aTag = hTag.selectFirst("a")
        //                     val epUrl = aTag?.attr("href").toString()
        //                     val key = Pair(realSeason, e)
        //                     if (episodesMap.containsKey(key)) {
        //                         val currentList = episodesMap[key] ?: emptyList()
        //                         val newList = currentList.toMutableList()
        //                         newList.add(epUrl)
        //                         episodesMap[key] = newList
        //                     } else {
        //                         episodesMap[key] = mutableListOf(epUrl)
        //                     }
        //                     hTag = hTag.nextElementSibling()
        //                 }
        //                 e++
        //             }
        //             else {
        //                 val epUrl = element.attr("href")
        //                 val key = Pair(realSeason, e)
        //                 if (episodesMap.containsKey(key)) {
        //                     val currentList = episodesMap[key] ?: emptyList()
        //                     val newList = currentList.toMutableList()
        //                     newList.add(epUrl)
        //                     episodesMap[key] = newList
        //                 } else {
        //                     episodesMap[key] = mutableListOf(epUrl)
        //                 }
        //                 e++
        //             }
        //         }
        //         e = 1
        //     }

        //     for ((key, value) in episodesMap) {
        //         val episodeInfo = responseData?.meta?.videos?.find { it.season == key.first && it.episode == key.second }
        //         val data = value.map { source->
        //             EpisodeLink(
        //                 source
        //             )
        //         }
        //         tvSeriesEpisodes.add(
        //             newEpisode(data) {
        //                 this.name = episodeInfo?.name ?: episodeInfo?.title
        //                 this.season = key.first
        //                 this.episode = key.second
        //                 this.posterUrl = episodeInfo?.thumbnail
        //                 this.description = episodeInfo?.overview
        //             }
        //         )
        //     }
        //     return newTvSeriesLoadResponse(title, url, TvType.TvSeries, tvSeriesEpisodes) {
        //         this.posterUrl = posterUrl
        //         this.plot = description
        //         this.tags = genre
        //         this.rating = imdbRating.toRatingInt()
        //         this.year = year.toIntOrNull()
        //         this.backgroundPosterUrl = background
        //         addActors(cast)
        //         addImdbUrl(imdbUrl)
        //     }
        // }

        val movieLink = document.selectFirst("downloads-btns-div > a").attr("href")
        val doc = app.get(movieLink).document
        val data = doc.select("div.downloads-btns-div > a").mapNotNull {
            EpisodeLink(
                it.attr("href")
            )
        }

        return newMovieLoadResponse(title, url, TvType.Movie, data) {
            this.posterUrl = posterUrl
            this.plot = description
            this.tags = genre
            this.rating = imdbRating.toRatingInt()
            this.year = year.toIntOrNull()
            this.backgroundPosterUrl = background
            addActors(cast)
            addImdbUrl(imdbUrl)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val sources = parseJson<ArrayList<EpisodeLink>>(data)
        sources.amap {
            val source = it.source
            loadExtractor(source, subtitleCallback, callback)
        }
        return true   
    }

    data class Meta(
        val id: String?,
        val imdb_id: String?,
        val type: String?,
        val poster: String?,
        val logo: String?,
        val background: String?,
        val moviedb_id: Int?,
        val name: String?,
        val description: String?,
        val genre: List<String>?,
        val releaseInfo: String?,
        val status: String?,
        val runtime: String?,
        val cast: List<String>?,
        val language: String?,
        val country: String?,
        val imdbRating: String?,
        val slug: String?,
        val year: String?,
        val videos: List<EpisodeDetails>?
    )

    data class EpisodeDetails(
        val id: String?,
        val name: String?,
        val title: String?,
        val season: Int?,
        val episode: Int?,
        val released: String?,
        val overview: String?,
        val thumbnail: String?,
        val moviedb_id: Int?
    )

    data class ResponseData(
        val meta: Meta?
    )

    data class EpisodeLink(
        val source: String
    )
}

