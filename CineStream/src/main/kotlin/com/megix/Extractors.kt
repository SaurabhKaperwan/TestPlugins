package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.network.WebViewResolver
import com.fasterxml.jackson.annotation.JsonProperty
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.getAndUnpack
import com.lagradost.cloudstream3.USER_AGENT
import android.util.Log
import kotlinx.coroutines.delay

open class SuperVideo : ExtractorApi() {
    override val name = "SuperVideo"
    override val mainUrl = "https://supervideo.cc"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val res = app.get(url.replace("tv","cc"), referer = referer)
        val script =
            res.document.selectFirst("script:containsData(function(p,a,c,k,e,d))")?.data()
        val unpacked = getAndUnpack(script ?: return)
        val m3u8 = Regex("file:\"(.*?m3u8.*?)").find(unpacked)?.groupValues?.getOrNull(1) ?:""
        M3u8Helper.generateM3u8(
            this.name,
            m3u8,
            referer = "$mainUrl/",
        ).forEach(callback)
    }
}

class Kwik : ExtractorApi() {
    override val name            = "Kwik"
    override val mainUrl         = "https://kwik.cx"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val res = app.get(url, referer = url)
        val script =
            res.document.selectFirst("script:containsData(function(p,a,c,k,e,d))")?.data()
        val unpacked = getAndUnpack(script ?: return)
        val m3u8 =Regex("source=\\s*'(.*?m3u8.*?)'").find(unpacked)?.groupValues?.getOrNull(1) ?:""
        callback.invoke(
            newExtractorLink(
                name,
                name,
                m3u8,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = url
            }
        )
    }
}

class Pahe : ExtractorApi() {
    override val name = "Pahe"
    override val mainUrl = "https://pahe.win"
    override val requiresReferer = true

    private val kwikParamsRegex = Regex("""\("(\w+)",\d+,"(\w+)",(\d+),(\d+),\d+\)""")
    private val kwikDUrl = Regex("action=\"([^\"]+)\"")
    private val kwikDToken = Regex("value=\"([^\"]+)\"")

    private fun decrypt(fullString: String, key: String, v1: Int, v2: Int): String {
        val keyIndexMap = key.withIndex().associate { it.value to it.index }
        val sb = StringBuilder()
        var i = 0
        val toFind = key[v2]

        while (i < fullString.length) {
            val nextIndex = fullString.indexOf(toFind, i)
            if (nextIndex == -1) break

            val decodedCharStr = buildString {
                for (j in i until nextIndex) {
                    append(keyIndexMap[fullString[j]] ?: -1)
                }
            }

            i = nextIndex + 1
            val decodedChar = (decodedCharStr.toInt(v2) - v1).toChar()
            sb.append(decodedChar)
        }

        return sb.toString()
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {

        val kwikUrl = app.get(
            "$url/i",
            allowRedirects = false
        ).headers["location"] ?: return

        val fContentResp = app.get(
            kwikUrl,
            headers = mapOf(
                "Referer" to "https://kwik.cx/",
                "User-Agent" to USER_AGENT
            )
        )
        val fContentString = fContentResp.text
        val sessionCookie = fContentResp.headers["set-cookie"]?.substringBefore(";") ?: ""
        val match = kwikParamsRegex.find(fContentString) ?: return
        val (fullString, key, v1, v2) = match.destructured

        val decrypted = decrypt(fullString, key, v1.toInt(), v2.toInt())

        val uri = kwikDUrl.find(decrypted)?.groupValues?.get(1) ?: return
        val tok = kwikDToken.find(decrypted)?.groupValues?.get(1) ?: return

        var code = 419
        var tries = 0
        var location = ""

        while (code != 302 && tries < 20) {

            val postResp = app.post(
                uri,
                headers = mapOf(
                    "User-Agent" to USER_AGENT,
                    "Referer" to kwikUrl,
                    "Cookie" to sessionCookie
                ),
                data = mapOf("_token" to tok),
                allowRedirects = false
            )

            code = postResp.code
            if (code == 302 || code == 301) {
                location = postResp.headers["location"] ?: ""
                break
            }

            tries++
            delay(500)
        }

        if (location.isEmpty()) return

        callback.invoke(
            newExtractorLink(
                name,
                name,
                location
            ) {
                this.referer = "https://kwik.cx/"
                this.quality = Qualities.Unknown.value
            }
        )
    }
}

class Akamaicdn : ExtractorApi() {
    override val name = "Akamaicdn"
    override val mainUrl = "https://molop."
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val headers= mapOf("user-agent" to "okhttp/4.12.0")
        val res = app.get(url, referer = referer, headers = headers).document
        val sniffScript = res.selectFirst("script:containsData(sniff\\()")
            ?.data()
            ?.substringAfter("sniff(")
            ?.substringBefore(");") ?: return

        val cleaned = sniffScript.replace(Regex("\\[.*?\\]"), "")
        val regex = Regex("\"(.*?)\"")
        val args = regex.findAll(cleaned).map { it.groupValues[1].trim() }.toList()
        val token = args.lastOrNull().orEmpty()
        val m3u8 = "$mainUrl/m3u8/${args[1]}/${args[2]}/master.txt?s=1&cache=1&plt=$token"
        M3u8Helper.generateM3u8(name, m3u8, mainUrl, headers = headers).forEach(callback)
    }
}

class Fourspromax : MegaUp() {
    override var mainUrl = "https://4spromax.site"
    override val requiresReferer = true
}

class MegaUpTwoTwo : MegaUp() {
    override var mainUrl = "https://megaup22.online"
    override val requiresReferer = true
}

class Rapidairmax : MegaUp() {
    override var mainUrl = "https://rapidairmax.site"
    override val requiresReferer = true
}
//Thanks to https://github.com/AzartX47/EncDecEndpoints
open class MegaUp : ExtractorApi() {
    override var name = "MegaUp"
    override var mainUrl = "https://megaup.live"
    override val requiresReferer = true

    companion object {
        private val HEADERS = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:134.0) Gecko/20100101 Firefox/134.0",
                "Accept" to "text/html, *//*; q=0.01",
                "Accept-Language" to "en-US,en;q=0.5",
                "Sec-GPC" to "1",
                "Sec-Fetch-Dest" to "empty",
                "Sec-Fetch-Mode" to "cors",
                "Sec-Fetch-Site" to "same-origin",
                "Priority" to "u=0",
                "Pragma" to "no-cache",
                "Cache-Control" to "no-cache",
                "referer" to "https://animekai.to/",
        )
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val mediaUrl = url.replace("/e/", "/media/").replace("/e2/", "/media/")
        val displayName = referer ?: this.name

        val encodedResult = app.get(mediaUrl, headers = HEADERS)
        .parsedSafe<AnimeKaiResponse>()
        ?.result

        if (encodedResult == null) return

        val m3u8Data = app.post(
            url = "https://enc-dec.app/api/dec-mega",
            json = mapOf(
                "text" to encodedResult,
                "agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:134.0) Gecko/20100101 Firefox/134.0"
            )
        ).text

        if (m3u8Data.isBlank()) {
            Log.d("Phisher", "Encoded result is null or empty")
            return
        }

        try {
            val root = JSONObject(m3u8Data)
            val result = root.optJSONObject("result")
            if (result == null) {
                Log.d("Error:", "No 'result' object in M3U8 JSON")
                return
            }

            val sources = result.optJSONArray("sources") ?: JSONArray()
            if (sources.length() > 0) {
                val firstSourceObj = sources.optJSONObject(0)
                val m3u8File = when {
                    firstSourceObj != null -> firstSourceObj.optString("file").takeIf { it.isNotBlank() }
                    else -> {
                        val maybeString = sources.optString(0)
                        maybeString.takeIf { it.isNotBlank() }
                    }
                }
                if (m3u8File != null) {
                    M3u8Helper.generateM3u8(displayName, m3u8File, mainUrl).forEach(callback)
                } else {
                    Log.d("Error:", "No 'file' found in first source")
                }
            } else {
                Log.d("Error:", "No sources found in M3U8 data")
            }

            val tracks = result.optJSONArray("tracks") ?: JSONArray()
            for (i in 0 until tracks.length()) {
                val trackObj = tracks.optJSONObject(i) ?: continue
                val label = trackObj.optString("label").trim().takeIf { it.isNotEmpty() }
                val file = trackObj.optString("file").takeIf { it.isNotBlank() }
                if (label != null && file != null) {
                    subtitleCallback(newSubtitleFile(getLanguage(label) ?: label, file))
                }
            }
        } catch (_: JSONException) {
            Log.e("Error", "Failed to parse M3U8 JSON")
        }
      }

    data class AnimeKaiResponse(
        @param:JsonProperty("status") val status: Int,
        @param:JsonProperty("result") val result: String
    )

}
