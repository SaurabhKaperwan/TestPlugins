package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.jsoup.Jsoup

class VadaPavProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://vadapav.mov"
    override var name = "VadaPav"
    override val hasMainPage = true
    override var lang = "en"
    override val hasDownloadSupport = true

    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime,
    )

    override val mainPage = mainPageOf(
        "movies" to "Movies",
        "shows" to "Shows",
        "hindi" to "Hindi",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get("$mainUrl/${request.data}/").document
        val home = document.select("div.directory > ul > li > div > a").filter { element -> !element.text().contains("Parent Directory", true) }.mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.text() ?: ""
        val link = fixUrl(this.attr("href"))

        return newMovieSearchResponse(title, link, TvType.Movie) {
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()
        val document = app.get("$mainUrl/s/$query").document

        val results = document.select("div.directory > ul > li > div > a").filter { element -> !element.text().contains("Parent Directory", true) }.mapNotNull { it.toSearchResult() }

        searchResponse.addAll(results)

        return searchResponse
    }

    data class MutableInt(var value: Int)

    private suspend fun traverse(dTags: List<Element> ,tvSeriesEpisodes: MutableList<Episode>, seasonList: MutableList<Pair<String, Int>>, mutableSeasonNum: MutableInt) {
        for(dTag in dTags) {
            val document = app.get(fixUrl(dTag.attr("href"))).document
            val innerDTags = document.select("div.directory > ul > li > div > a.directory-entry").filter { element -> !element.text().contains("Parent Directory", true) }
            val innerFTags = document.select("div.directory > ul > li > div > a.file-entry")

            if(innerFTags.isNotEmpty()) {
                val span = document.select("div > span")
                val lastSpan = span.takeIf { it.isNotEmpty() } ?. lastOrNull()
                val title = lastSpan ?. text() ?: ""
                seasonList.add("$title" to mutableSeasonNum.value)
                val episodes = innerFTags.amap { tag ->
                    newEpisode(tag.attr("href")){
                        name = tag.text()
                        season = mutableSeasonNum.value
                        episode = innerFTags.indexOf(tag) + 1
                    }
                }
                tvSeriesEpisodes.addAll(episodes)
                mutableSeasonNum.value++
            }

            if(innerDTags.isNotEmpty()) {
                traverse(innerDTags, tvSeriesEpisodes, seasonList, mutableSeasonNum)
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val span = document.select("div > span")
        val lastSpan = span.takeIf { it.isNotEmpty() } ?. lastOrNull()
        val title = lastSpan ?. text() ?: ""
        var seasonNum = 1
        val tvSeriesEpisodes = mutableListOf<Episode>()
        val dTags = document.select("div.directory > ul > li > div > a.directory-entry").filter { element -> !element.text().contains("Parent Directory", true) }
        val fTags = document.select("div.directory > ul > li > div > a.file-entry")
        val seasonList = mutableListOf<Pair<String, Int>>()
        val mutableSeasonNum = MutableInt(seasonNum)

        if(fTags.isNotEmpty()) {
            val innerSpan = document.select("div > span")
            val lastInnerSpan = innerSpan.takeIf { it.isNotEmpty() } ?. lastOrNull()
            val titleText = lastInnerSpan ?. text() ?: ""
            seasonList.add("$titleText" to mutableSeasonNum.value)
            val episodes = fTags.amap { tag ->
                newEpisode(tag.attr("href")){
                    name = tag.text()
                    season = mutableSeasonNum.value
                    episode = fTags.indexOf(tag) + 1
                }
            }
            tvSeriesEpisodes.addAll(episodes)
            mutableSeasonNum.value++
        }

        if(dTags.isNotEmpty()) {
            traverse(dTags, tvSeriesEpisodes, seasonList, mutableSeasonNum)
        }
       
        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, tvSeriesEpisodes) {
            this.seasonNames = seasonList.map {(name, int) -> SeasonData(int, name)}
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        callback.invoke(
            newExtractorLink(
               this.name,
               this.name,
               data,
            )
        )

        return true
    }

}
