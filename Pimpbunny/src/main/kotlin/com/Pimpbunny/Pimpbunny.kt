package com.megix

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class Pimpbunny : MainAPI() {
    override var mainUrl              = "https://pimpbunny.com"
    override var name                 = "Pimpbunny"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = false
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        "" to "Latest",

    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl${request.data}/$page/?videos_per_page=30").document
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
        val title     = this.select("div.pb-item-title").text()
        val href      = this.select("a.pb-item-link").attr("href")
        val posterUrl = this.select("img").attr("data-original")

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..5) {
            val document = app.get("$mainUrl/search/?q=$query&mode=async&function=get_block&block_id=list_videos_videos_list_search_result&videos_per_page=%2030&sort_by=&from_videos=$i&_=1759852672421").document
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

        val title       = document.select("meta[property=og:title]").attr("content")
        val poster      = document.select("meta[property='og:image']").attr("content")
        val description = document.select("meta[property=og:description]").attr("content")


        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot      = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val text = app.get(data).text
        val urlPattern = Regex("""video(?:_alt)?_url\d*:\s*'([^']+)'""")

        val videoLinks = urlPattern.findAll(text).map {
            val url = it.groupValues[1].removePrefix("function/0/")
            val quality = getIndexQuality(url)
            callback.invoke(
                newExtractorLink(
                    "Pimpbunny",
                    "Pimpbunny",
                    url,
                    ExtractorLinkType.VIDEO
                ) {
                    this.referer = "$mainUrl/"
                    this.quality = quality
                }
            )

        }
        return true
    }

    fun getIndexQuality(str: String?): Int {
        return Regex("""(\d{3,4})[pP]""").find(str ?: "") ?. groupValues ?. getOrNull(1) ?. toIntOrNull()
            ?: Qualities.Unknown.value
    }
}
