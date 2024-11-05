package com.coxju

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.getQualityFromName
import okhttp3.FormBody
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.AppUtils.parseJson

class Porn11 : MainAPI() {
    override var mainUrl              = "https://pornken.com"
    override var name                 = "PornKen"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = false
    override val hasDownloadSupport   = true
    override val hasChromecastSupport = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
            "" to "Home",
            "playlist/naughty-america-brazzers-realitykings-bangbros/331131-565969" to "Brazzers",
            "playlist/bangbros/248746-327137" to "Bangbros",
            "playlist/japan/215132-126095" to "Japan",
            "playlist/brazzers/229889-170657" to "Milf"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/${request.data}/page-$page").document
        val home     = document.select("div.video-preview-screen").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(
            list    = HomePageList(
                name               = request.name,
                list               = home,
                isHorizontalImages = true
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title     = fixTitle(this.select("a > img").attr("alt"))
        val href      = fixUrl(this.select("a").attr("href"))
        val posterUrl = fixUrl(this.select("a > img").attr("src"))

        return newMovieSearchResponse(title, PassData(href, posterUrl).toJson(), TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..5) {
            val document = app.get("${mainUrl}/page/$i?s=$query").document

            val results = document.select("div.video-preview-screen").mapNotNull { it.toSearchResult() }

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
        val data = parseJson<PassData>(url)
        val document = app.get(data.url).document

        val title       = document.selectFirst("meta[property=og:title]")?.attr("content")?.trim().toString()
        val description = document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()

        return newMovieLoadResponse(title, data.url, TvType.NSFW, data.url) {
            this.posterUrl = data.posterUrl
            this.plot      = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        println(data)
        val extractedscript =document.selectFirst("script:containsData(moviesiframe)")?.data().toString()
        val numRegex = Regex("num:'(.*?)'")
        val numValue = numRegex.find(extractedscript)?.groupValues?.get(1)

        val body = FormBody.Builder()
            .addEncoded("mix", "moviesiframe")
            .addEncoded("num", "$numValue")
            .build()
        val dataextract=app.post("https://pornken.com/hash-pornken",
            headers = mapOf("content-type" to "application/x-www-form-urlencoded"),
            referer = data,
            requestBody = body,
            )
        val srcRegex = Regex("""<iframe[^>]+src=["']([^"']+)["'][^>]*>""")
        val matchResult = srcRegex.find(dataextract.toString())
        val srcAttribute = matchResult?.groups?.get(1)?.value
        val mainpage=app.get("https://pornken.com$srcAttribute").document
        val source=mainpage.select("video > source").forEach {
            val url =it.attr("src")
            val resolution=it.attr("res")
            callback.invoke(
                ExtractorLink(
                    source  = this.name,
                    name    = this.name,
                    url     = url,
                    referer = data,
                    quality = getQualityFromName(resolution)
                )
            )
        }

        Log.d("Test120","$source")
        return true
    }

    data class PassData(
        var url: String,
        var posterUrl: String,
    )
}
