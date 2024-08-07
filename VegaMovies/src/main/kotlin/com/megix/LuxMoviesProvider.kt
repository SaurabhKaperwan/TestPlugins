package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.network.CloudflareKiller


class LuxMoviesProvider : VegaMoviesProvider() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://luxmovies.wiki"
    override var name = "LuxMovies"
    override val hasMainPage = true
    override var lang = "hi"
    override val hasDownloadSupport = true
    override val usesCloudFlareKiller = true
    private val cfInterceptor = CloudflareKiller()
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries
    )

    override val mainPage = mainPageOf(
        "$mainUrl/page/%d/" to "Home",
        "$mainUrl/category/web-series/netflix/page/%d/" to "Netflix",
        "$mainUrl/category/web-series/disney-plus-hotstar/page/%d/" to "Disney Plus Hotstar",
        "$mainUrl/category/web-series/amazon-prime-video/page/%d/" to "Amazon Prime",
        "$mainUrl/category/web-series/mx-original/page/%d/" to "MX Original",
        "$mainUrl/category/web-series/jio-studios/page/%d/" to "Jio Cinema",
        "$mainUrl/category/web-series/sonyliv/page/%d/" to "Sony Liv",
        "$mainUrl/category/web-series/zee5-originals/page/%d/" to "Zee5",
        "$mainUrl/category/web-series/alt-balaji-web-series/page/%d/" to "ALT Balaji",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(request.data.format(page), interceptor = cfInterceptor).document
        val home = document.select("article.post-item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("a")?.attr("title")
        val trimTitle = title?.let {
            if (it.contains("Download ")) {
                it.replace("Download ", "")
            } else {
                it
            }
        } ?: ""
        val href = fixUrl(this.selectFirst("a")?.attr("href").toString())
        val imgTag = this.selectFirst("img.blog-picture")
        val posterUrl = imgTag ?. attr("data-src")

        return newMovieSearchResponse(trimTitle, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }
    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()
        for (i in 1..3) {
            val headers = mapOf("User-Agent" to "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.186 Mobile Safari/537.36")
            val document = app.get("$mainUrl/page/$i/?s=$query", interceptor = cfInterceptor, headers = headers).document
            val results = document.select("article.post-item").mapNotNull { it.toSearchResult() }
            if (results.isEmpty()) {
                break
            }
            searchResponse.addAll(results)
        }
        return searchResponse
    }

}
