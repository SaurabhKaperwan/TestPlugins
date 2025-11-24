package com.CXXX

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.httpsify
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.runAllAsync
import org.json.JSONObject
import java.lang.Exception

class SxyPrn : MainAPI() {
    override var mainUrl = "https://sxyprn.com"
    override var name = "Sxyprn"
    override val hasMainPage = true
    override val hasDownloadSupport = true
    override val vpnStatus = VPNStatus.MightBeNeeded
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
        "$mainUrl/new.html?page=" to "New Videos",
        "$mainUrl/new.html?sm=trending&page=" to "Trending",
        "$mainUrl/new.html?sm=views&page=" to "Most Viewed",
        "$mainUrl/popular/top-viewed.html?p=day" to "Popular - Day",
        "$mainUrl/popular/top-viewed.html" to "Popular - Week",
        "$mainUrl/popular/top-viewed.html?p=month" to "Popular - Month",
        "$mainUrl/popular/top-viewed.html?p=all" to "Popular - All Time"
    )

    override suspend fun getMainPage(
        page: Int, request: MainPageRequest
    ): HomePageResponse {
        var pageStr = ((page - 1) * 30).toString()

        val document = if ("page=" in request.data) {
            app.get(request.data + pageStr).document
        } else if ("/blog/" in request.data) {
            pageStr = ((page - 1) * 20).toString()
            app.get(request.data.replace(".html", "$pageStr.html")).document
        } else {
            app.get(request.data.replace(".html", ".html/$pageStr")).document
        }
        val home = document.select("div.main_content div.post_el_small").mapNotNull {
                it.toSearchResult()
            }
        return newHomePageResponse(
            list = HomePageList(
                name = request.name, list = home, isHorizontalImages = true
            ), hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.post_text")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a.js-pop")!!.attr("href"))
        var posterUrl = fixUrl(this.select("div.vid_container div.post_vid_thumb img").attr("src"))
        if (posterUrl == "") {
            posterUrl =
                fixUrl(this.select("div.vid_container div.post_vid_thumb img").attr("data-src"))
        }
        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val document = app.get(
            "$mainUrl/${query.replace(" ", "-")}.html?page=${(page-1) * 30}"
        ).document

        val results = document.select("div.main_content div.post_el_small").mapNotNull {
            it.toSearchResult()
        }

        val hasNext = if(results.isEmpty()) false else true
        return newSearchResponseList(results, hasNext)
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("div.post_text")?.text()?.trim().toString()
        val poster = httpsify(document.select("meta[property=og:image]").attr("content"))

        val recommendations = document.select("div.main_content div div.post_el_small").mapNotNull {
            it.toSearchResult()
        }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.recommendations = recommendations
        }
    }

    private fun updateUrl(arg: MutableList<String>): MutableList<String> {
        arg[5] =
            (Integer.parseInt(arg[5]) - (generateNumber(arg[6]) + generateNumber(arg[7]))).toString()
        return arg
    }

    private fun generateNumber(arg: String): Int {
        val str = arg.replace(Regex("\\D"), "")
        var sut = 0
        for (element in str) {
            sut += Integer.parseInt(element.toString(), 10)
        }
        return sut
    }

    private fun preda(arg: MutableList<String>): MutableList<String> {
        if (arg.size <= 7) return arg

        try {
            val val5 = arg[5].toLong()
            val deduction = ssut51(arg[6]) + ssut51(arg[7])
            arg[5] = (val5 - deduction).toString()

        } catch (e: NumberFormatException) {
            println("Error parsing URL segments in preda: ${e.message}")
        }
        return arg
    }


    private fun ssut51(arg: String): Long {
        val str = arg.replace(Regex("[^0-9]"), "")

        var sut: Long = 0

        for (char in str) {
            sut += char.toString().toInt()
        }

        return sut
    }


   override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        runAllAsync(
            {
                document.select("div.post_el_wrap a.extlink").amap {
                    loadExtractor(it.attr("href"), "", subtitleCallback, callback)
                }
            },
            {
                val vnfoString = document.select(".vidsnfo").attr("data-vnfo")
                val vidsnfo = JSONObject(vnfoString)
                val keys = vidsnfo.keys()
                while (keys.hasNext()) {
                    val pid = keys.next()
                    val src = vidsnfo.getString(pid)

                    val tmp = src.split("/").toMutableList()

                    if (tmp.size > 1) {
                        tmp[1] = tmp[1] + "8"
                    }

                    val processedTmp = preda(tmp)
                    callback.invoke(
                        newExtractorLink(
                            source = this.name,
                            name = this.name,
                            url = processedTmp.joinToString("/")
                        ) {
                            this.referer = mainUrl
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
            },
        )

        return true
    }
}


