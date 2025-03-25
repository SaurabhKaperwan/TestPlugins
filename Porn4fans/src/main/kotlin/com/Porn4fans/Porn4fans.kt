package com.megix

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class Porn4fans : MainAPI() {
    override var mainUrl              = "https://www.porn4fans.com"
    override var name                 = "Porn4fans"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = false
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        "onlyfans-videos/" to "Latest",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/${request.data}/$page/").document
        val home     = document.select("div.item").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(
                list    = HomePageList(
                name    = request.name,
                list    = home,
                isHorizontalImages = true
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title     = this.select("a").attr("title")
        val href      = this.select("a").attr("href")
        val posterUrl = this.select("img").attr("src")

        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..7) {
            val document = app.get("$mainUrl/search/$query/?mode=async&function=get_block&block_id=custom_list_videos_videos_list_search_result&q=hi&category_ids=&sort_by=&from_videos=$i&from_albums=$i").document
            val results = document.select("div.item").mapNotNull { it.toSearchResult() }

            if (!searchResponse.containsAll(results)) {
                searchResponse.addAll(results)
            } else {
                break
            }

            if (results.isEmpty()) break
        }

        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val jsonString = document.select("script[type=application/ld+json]").data()
        val jsonObject = parseJson<Response>(jsonString)

        return newMovieLoadResponse(jsonObject.name, url, TvType.NSFW, jsonObject.contentUrl) {
            this.posterUrl = jsonObject.thumbnailUrl
            this.plot      = jsonObject.description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        callback.invoke(
            ExtractorLink(
                "Porn4fans",
                "Porn4fans",
                data,
                "",
                Qualities.Unknown.value
            )
        )
        return true
    }

    data class Response(
        val name: String,
        val thumbnailUrl: String,
        val description: String,
        val contentUrl: String
    )
}
