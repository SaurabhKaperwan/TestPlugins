package com.megix

// import com.lagradost.cloudstream3.*
// import com.lagradost.cloudstream3.utils.*
// import com.lagradost.cloudstream3.extractors.Jeniusplay

// class Asumanaksoy: Jeniusplay() {
//     override var mainUrl = "https://cdn.asumanaksoy.com"
// }

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson

class Asumanaksoy : ExtractorApi() {
    override val name = "Asumanaksoy"
    override val mainUrl = "https://cdn.asumanaksoy.com"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val document = app.get(url, referer = "$mainUrl/").document
        val hash = url.split("/").last().substringAfter("data=")

        val m3uLink = app.post(
            url = "$mainUrl/player/index.php?data=$hash&do=getVideo",
            data = mapOf("hash" to hash, "r" to "$referer"),
            referer = url,
            headers = mapOf("X-Requested-With" to "XMLHttpRequest")
        ).parsed<ResponseSource>().videoSource

        M3u8Helper.generateM3u8(
            this.name,
            m3uLink,
            url,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.186 Mobile Safari/537.36",
                "Accept-Encoding" to "gzip, deflate",
                "Accept" to "*/*",
                "Connection" to "keep-alive"
            )
        ).forEach(callback)


        document.select("script").map { script ->
            if (script.data().contains("eval(function(p,a,c,k,e,d)")) {
                val subData =
                    getAndUnpack(script.data()).substringAfter("\"tracks\":[").substringBefore("],")
                tryParseJson<List<Tracks>>("[$subData]")?.map { subtitle ->
                    subtitleCallback.invoke(
                        SubtitleFile(
                            getLanguage(subtitle.label ?: ""),
                            subtitle.file,
                            headers = mapOf(
                                "User-Agent" to "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.186 Mobile Safari/537.36",
                                "Accept-Encoding" to "gzip, deflate",
                                "Accept" to "*/*",
                                "Connection" to "keep-alive"
                            )
                        )
                    )
                }
            }
        }
    }

    private fun getLanguage(str: String): String {
        return when {
            str.contains("indonesia", true) || str
                .contains("bahasa", true) -> "Indonesian"
            else -> str
        }
    }

    data class ResponseSource(
        @JsonProperty("hls") val hls: Boolean,
        @JsonProperty("videoSource") val videoSource: String,
        @JsonProperty("securedLink") val securedLink: String?,
    )

    data class Tracks(
        @JsonProperty("kind") val kind: String?,
        @JsonProperty("file") val file: String,
        @JsonProperty("label") val label: String?,
    )
}

