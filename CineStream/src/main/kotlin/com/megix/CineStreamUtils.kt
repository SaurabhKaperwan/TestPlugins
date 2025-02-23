package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.base64Decode
import okhttp3.FormBody
import org.jsoup.nodes.Document
import java.net.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.api.Log
import com.lagradost.nicehttp.NiceResponse
import kotlinx.coroutines.delay

fun String.getHost(): String {
    return fixTitle(URI(this).host.substringBeforeLast(".").substringAfterLast("."))
}

var NfCookie = ""

suspend fun NFBypass(mainUrl : String): String {
    if(NfCookie != "") {
        return NfCookie
    }
    val homePageDocument = app.get("${mainUrl}/home").document
    val addHash          = homePageDocument.select("body").attr("data-addhash")
    val time             = homePageDocument.select("body").attr("data-time")

    var verificationUrl  = "https://raw.githubusercontent.com/SaurabhKaperwan/Utils/refs/heads/main/NF.json"
    verificationUrl      = app.get(verificationUrl).parsed<NFVerifyUrl>().url.replace("###", addHash)
    // val hashDigits       = addHash.filter { it.isDigit() }
    // val first16Digits    = hashDigits.take(16)
    // app.get("${verificationUrl}&t=0.${first16Digits}")
    app.get(verificationUrl + "&t=${time}")

    var verifyCheck: String
    var verifyResponse: NiceResponse
    var tries = 0

    do {
        delay(1000)
        tries++
        val requestBody = FormBody.Builder().add("verify", addHash).build()
        verifyResponse  = app.post("${mainUrl}/verify2.php", requestBody = requestBody)
        verifyCheck     = verifyResponse.text
    } while (!verifyCheck.contains("\"statusup\":\"All Done\"") || tries < 5)

    return verifyResponse.cookies["t_hash_t"].orEmpty()
}

suspend fun cinemaluxeBypass(url: String): String {
    val document = app.get(url, allowRedirects = true).document.toString()
    val encodeUrl = Regex("""link":"([^"]+)""").find(document) ?. groupValues ?. get(1) ?: ""
    return base64Decode(encodeUrl)
}

fun getFirstCharacterOrZero(input: String): String {
    val firstChar = input[0]
    return if (!firstChar.isLetter()) {
        "0"
    } else {
        firstChar.toString()
    }
}

fun getBaseUrl(url: String): String {
    return URI(url).let {
        "${it.scheme}://${it.host}"
    }
}

fun String?.createSlug(): String? {
    return this?.filter { it.isWhitespace() || it.isLetterOrDigit() }
        ?.trim()
        ?.replace("\\s+".toRegex(), "-")
        ?.lowercase()
}

suspend fun extractMdrive(url: String): List<String> {
    val doc = app.get(url).document
    return doc.select("a")
        .mapNotNull { it.attr("href").takeIf { href ->
            href.contains(Regex("hubcloud|gdflix", RegexOption.IGNORE_CASE))
        }}
}


fun getEpisodeSlug(
    season: Int? = null,
    episode: Int? = null,
): Pair<String, String> {
    return if (season == null && episode == null) {
        "" to ""
    } else {
        (if (season!! < 10) "0$season" else "$season") to (if (episode!! < 10) "0$episode" else "$episode")
    }
}

fun getIndexQuality(str: String?): Int {
    return Regex("""(\d{3,4})[pP]""").find(str ?: "") ?. groupValues ?. getOrNull(1) ?. toIntOrNull()
        ?: Qualities.Unknown.value
}

suspend fun loadSourceNameExtractor(
    source: String,
    url: String,
    referer: String? = null,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit,
    quality: Int? = null,
) {
    loadExtractor(url, referer, subtitleCallback) { link ->
        callback.invoke(
            ExtractorLink(
                "$source[${link.source}]",
                "$source - ${link.name}",
                link.url,
                link.referer,
                quality ?: link.quality ,
                link.type,
                link.headers,
                link.extractorData
            )
        )
    }
}

suspend fun loadCustomTagExtractor(
        tag: String? = null,
        url: String,
        referer: String? = null,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
        quality: Int? = null,
) {
    loadExtractor(url, referer, subtitleCallback) { link ->
        callback.invoke(
            ExtractorLink(
                link.source,
                "${link.name} $tag",
                link.url,
                link.referer,
                quality ?: link.quality,
                link.type,
                link.headers,
                link.extractorData
            )
        )
    }
}

suspend fun loadCustomExtractor(
    name: String? = null,
    url: String,
    referer: String? = null,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit,
    quality: Int? = null,
) {
    loadExtractor(url, referer, subtitleCallback) { link ->
        callback.invoke(
            ExtractorLink(
                name ?: link.source,
                name ?: link.name,
                link.url,
                link.referer,
                when {
                    link.name == "VidSrc" -> Qualities.P1080.value
                    else -> quality ?: link.quality
                },
                link.type,
                link.headers,
                link.extractorData
            )
        )
    }
}

fun fixUrl(url: String, domain: String): String {
    if (url.startsWith("http")) {
        return url
    }
    if (url.isEmpty()) {
        return ""
    }

    val startsWithNoHttp = url.startsWith("//")
    if (startsWithNoHttp) {
        return "https:$url"
    } else {
        if (url.startsWith('/')) {
            return domain + url
        }
        return "$domain/$url"
    }
}

suspend fun bypassHrefli(url: String): String? {
    fun Document.getFormUrl(): String {
        return this.select("form#landing").attr("action")
    }

    fun Document.getFormData(): Map<String, String> {
        return this.select("form#landing input").associate { it.attr("name") to it.attr("value") }
    }

    val host = getBaseUrl(url)
    var res = app.get(url).document
    var formUrl = res.getFormUrl()
    var formData = res.getFormData()

    res = app.post(formUrl, data = formData).document
    formUrl = res.getFormUrl()
    formData = res.getFormData()

    res = app.post(formUrl, data = formData).document
    val skToken = res.selectFirst("script:containsData(?go=)")?.data()?.substringAfter("?go=")
        ?.substringBefore("\"") ?: return null
    val driveUrl = app.get(
        "$host?go=$skToken", cookies = mapOf(
            skToken to "${formData["_wp_http2"]}"
        )
    ).document.selectFirst("meta[http-equiv=refresh]")?.attr("content")?.substringAfter("url=")
    val path = app.get(driveUrl ?: return null).text.substringAfter("replace(\"")
        .substringBefore("\")")
    if (path == "/404") return null
    return fixUrl(path, getBaseUrl(driveUrl))
}

