package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.lagradost.cloudstream3.base64Decode
import com.lagradost.cloudstream3.LoadResponse.Companion.addImdbUrl
import com.google.gson.Gson

open class MoviesmodProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://moviesmod.bet"
    override var name = "Moviesmod"
    override val hasMainPage = true
    override var lang = "hi"
    override val hasDownloadSupport = true
    val cinemeta_url = "https://v3-cinemeta.strem.io/meta"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries
    )

    override val mainPage = mainPageOf(
        "$mainUrl/page/" to "Home",
        "$mainUrl/web-series/on-going/page/" to "Latest Web Series",
        "$mainUrl/movies/latest-released/page/" to "Latest Movies",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select("div.post-cards > article").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("a")?.attr("title")?.replace("Download ", "").toString()
        val href = this.selectFirst("a")?.attr("href").toString()
        val posterUrl = this.selectFirst("a > div > img")?.attr("src").toString()

        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..3) {
            val document = app.get("$mainUrl/search/$query/page/$i").document

            val results = document.select("div.post-cards > article").mapNotNull { it.toSearchResult() }

            if (results.isEmpty()) {
                break
            }
            searchResponse.addAll(results)
        }

        return searchResponse
    }


    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        // val title = document.selectFirst("meta[property=og:title]")?.attr("content")?.replace("Download ", "").toString()
        // val posterUrl = document.selectFirst("meta[property=og:image]")?.attr("content").toString()
        // val description = document.selectFirst("div.imdbwp__teaser")?.text()
        val div = document.selectFirst("div.thecontent")?.text().toString()
        val imdbUrl = document.selectFirst("a.imdbwp__link")?.attr("href")
        val imdbId = imdbUrl?.substringAfter("title/")?.substringBefore("/")
        val tvtype = if (div.contains("season", ignoreCase = true) == true) "series" else "movie"
        val jsonResponse = app.get("$cinemeta_url/$tvtype/$imdbId.json").text
        val gson = Gson()
        val responseData = gson.fromJson(jsonResponse, ResponseData::class.java)
        val description = responseData.meta.description
        val cast = responseData.meta.cast
        val title = responseData.meta.name
        val genre = responseData.meta.genre
        val imdbRating = responseData.meta.imdbRating
        val year = responseData.meta.year
        val posterUrl = responseData.meta.background


        if(tvtype == "series") {
            val tvSeriesEpisodes = mutableListOf<Episode>()
            var seasonNum = 1
            val seasonList = mutableListOf<Pair<String, Int>>()
            val buttons = document.select("a.maxbutton-episode-links,.maxbutton-g-drive,.maxbutton-af-download")

            buttons.mapNotNull {
                var link = it.attr("href")
                val seasonText = it.parent()?.previousElementSibling()?.text().toString()
                seasonList.add(Pair(seasonText, seasonNum))

                if(link.contains("url=")) {
                    val base64Value = link.substringAfter("url=")
                    link = base64Decode(base64Value)
                }
                val doc = app.get(link).document
                val hTags = doc.select("h3,h4")
                var e = 1
                hTags.mapNotNull {
                    val titleText = it.text()
                    val epUrl = it.selectFirst("a")?.attr("href")
                    tvSeriesEpisodes.add(
                        newEpisode(epUrl) {
                            name = titleText
                            season = seasonNum
                            episode = e++
                        }
                    )
                }
                e = 1
                seasonNum++
            }
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, tvSeriesEpisodes) {
                this.posterUrl = posterUrl
                this.seasonNames = seasonList.map {(name, int) -> SeasonData(int, name)}
                this.plot = description
                addImdbUrl(imdbUrl)
            }
        }
        else {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = posterUrl
                this.plot = description
                addImdbUrl(imdbUrl)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        if(data.contains("unblockedgames")) {
            val link = bypass(data).toString()
            loadExtractor(link, subtitleCallback, callback)
        }
        else {
            val document = app.get(data).document
            document.select("a.maxbutton-download-links").amap {
                var link = it.attr("href")
                if(link.contains("url=")) {
                    val base64Value = link.substringAfter("url=")
                    link = base64Decode(base64Value)
                }

                val doc = app.get(link).document
                val url = doc.selectFirst("a.maxbutton-1, a.maxbutton-5")?.attr("href").toString()
                val driveLink = bypass(url).toString()
                loadExtractor(driveLink, subtitleCallback, callback)
            }
        }
        return true
    }

    data class Meta(
        val imdb_id: String,
        val name: String,
        val cast: List<String>,
        val genre: List<String>,
        val imdbRating: String,
        val year: String,
        val background: String,
        val description: String
    )

    data class ResponseData(
        val meta: Meta
    )
}


//url = https://v3-cinemeta.strem.io/meta/movie/tt17505010.json