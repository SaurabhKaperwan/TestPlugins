package com.megix

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey

object Settings {

    // --- THEME COLORS ---
    private val BG_DARK        = Color.parseColor("#0D0F14")
    private val BG_CARD        = Color.parseColor("#13161E")
    private val ACCENT_START   = Color.parseColor("#6C63FF")
    private val ACCENT_END     = Color.parseColor("#A855F7")
    private val TEXT_PRIMARY   = Color.parseColor("#F0F2FF")
    private val TEXT_SECONDARY = Color.parseColor("#7B82A0")
    private val SWITCH_ON      = Color.parseColor("#6C63FF")
    private val SWITCH_OFF     = Color.parseColor("#2A2D3E")
    private val DIVIDER_COLOR  = Color.parseColor("#1F2235")
    private val DANGER_COLOR   = Color.parseColor("#FF4E6A")

    // --- SECTION THEME COLORS ---
    private val THEME_SCRAPING_START = Color.parseColor("#8B5CF6")
    private val THEME_SCRAPING_END   = Color.parseColor("#C084FC")
    private val THEME_CATALOG_START  = Color.parseColor("#10B981")
    private val THEME_CATALOG_END    = Color.parseColor("#34D399")
    private val THEME_PROVIDER_START = Color.parseColor("#F43F5E")
    private val THEME_PROVIDER_END   = Color.parseColor("#FB7185")

    // --- DATABASE KEYS: Global ---
    const val DOWNLOAD_ENABLE     = "DownloadEnable"
    const val PROVIDER_CINESTREAM = "ProviderCineStream"
    const val PROVIDER_SIMKL      = "ProviderSimkl"
    const val PROVIDER_TMDB       = "ProviderTmdb"
    private const val COOKIE_KEY    = "nf_cookie"
    private const val TIMESTAMP_KEY = "nf_cookie_timestamp"
    const val SHOWBOX_TOKEN_KEY     = "showbox_ui_token"
    const val STREMIO_ADDONS_KEY    = "stremio_addons"

    // --- DATABASE KEYS: Providers ---
    const val P_TORRENTIO     = "p_torrentio"
    const val P_TORRENTSDB    = "p_torrentsdb"
    const val P_ANIMETOSHO    = "p_animetosho"
    const val P_VIDFLIX       = "p_vidflix"
    const val P_MOVIEBOX      = "p_moviebox"
    const val P_WYZIESUBS     = "p_wyziesubs"
    const val P_STREMIOSUBS   = "p_stremiosubs"
    const val P_CINEMACITY    = "p_cinemacity"
    const val P_WEBSTREAMR    = "p_webstreamr"
    const val P_STREAMVIX     = "p_streamvix"
    const val P_NOTORRENT     = "p_notorrent"
    const val P_CASTLE        = "p_castle"
    const val P_CINE          = "p_cine"
    const val P_ALLMOVIELAND  = "p_allmovieland"
    const val P_MADPLAYCDN    = "p_madplaycdn"
    const val P_VIDFASTPRO    = "p_vidfastpro"
    const val P_HEXA          = "p_hexa"
    const val P_YFLIX         = "p_yflix"
    const val P_XPASS         = "p_xpass"
    const val P_PLAYSRC       = "p_playsrc"
    const val P_2EMBED        = "p_2embed"
    const val P_DRAMAFULL     = "p_dramafull"
    const val P_VIDEASY       = "p_videasy"
    const val P_CINEMAOS      = "p_cinemaos"
    const val P_VICSRCWTF     = "p_vicsrcwtf"
    const val P_VIDLINK       = "p_vidlink"
    const val P_MAPPLE        = "p_mapple"
    const val P_VIDSTACK      = "p_vidstack"
    const val P_KISSKH        = "p_kisskh"
    const val P_NETFLIX       = "p_netflix"
    const val P_PRIMEVIDEO    = "p_primevideo"
    const val P_DISNEY        = "p_disney"
    const val P_BOLLYWOOD     = "p_bollywood"
    const val P_VIDZEE        = "p_vidzee"
    const val P_XDMOVIES      = "p_xdmovies"
    const val P_4KHDHUB       = "p_4khdhub"
    const val P_FLIXINDIA     = "p_flixindia"
    const val P_MOVIESDRIVE   = "p_moviesdrive"
    const val P_VEGAMOVIES    = "p_vegamovies"
    const val P_ROGMOVIES     = "p_rogmovies"
    const val P_BOLLYFLIX     = "p_bollyflix"
    const val P_TOPMOVIES     = "p_topmovies"
    const val P_MOVIESMOD     = "p_moviesmod"
    const val P_MOVIES4U      = "p_movies4u"
    const val P_UHDMOVIES     = "p_uhdmovies"
    const val P_PRIMESRC      = "p_primesrc"
    const val P_PROJECTFREETV = "p_projectfreetv"
    const val P_HINDMOVIEZ    = "p_hindmoviez"
    const val P_LEVIDIA       = "p_levidia"
    const val P_DAHMERMOVIES  = "p_dahmermovies"
    const val P_MULTIMOVIES   = "p_multimovies"
    const val P_PROTONMOVIES  = "p_protonmovies"
    const val P_AKWAM         = "p_akwam"
    const val P_RTALLY        = "p_rtally"
    const val P_TOONSTREAM    = "p_toonstream"
    const val P_ASIAFLIX      = "p_asiaflix"
    const val P_SKYMOVIES     = "p_skymovies"
    const val P_HDMOVIE2      = "p_hdmovie2"
    const val P_MOSTRAGUARDA  = "p_mostraguarda"
    const val P_ALLANIME      = "p_allanime"
    const val P_SUDATCHI      = "p_sudatchi"
    const val P_TOKYOINSIDER  = "p_tokyoinsider"
    const val P_ANIZONE       = "p_anizone"
    const val P_ANIMES        = "p_animes"
    const val P_GOJO          = "p_gojo"
    const val P_ANIMEWORLD    = "p_animeworld"
    const val P_SHOWBOX       = "p_showbox"

    private const val PROVIDER_ORDER_KEY = "provider_order"
    private val TORRENT_KEYS = setOf(P_TORRENTIO, P_TORRENTSDB, P_ANIMETOSHO)

    val PROVIDER_NAMES = linkedMapOf(
        P_TORRENTIO     to "🧲 Torrentio",
        P_TORRENTSDB    to "🧲 TorrentsDB",
        P_ANIMETOSHO    to "🧲 AnimeTosho",
        P_WEBSTREAMR    to "WebStreamr",
        P_STREAMVIX     to "Streamvix",
        P_NOTORRENT     to "NoTorrent",
        P_CASTLE        to "Castle",
        P_CINE          to "Cine",
        P_ANIMEWORLD    to "AnimeWorld",
        P_SHOWBOX       to "ShowBox",
        P_VIDFLIX       to "Vidflix",
        P_MOVIEBOX      to "Moviebox",
        P_CINEMACITY    to "Cinemacity",
        P_ALLMOVIELAND  to "Allmovieland",
        P_MADPLAYCDN    to "MadplayCDN",
        P_VIDFASTPRO    to "VidFastPro",
        P_HEXA          to "Hexa",
        P_YFLIX         to "Yflix",
        P_XPASS         to "Xpass",
        P_PLAYSRC       to "Playsrc",
        P_2EMBED        to "2Embed",
        P_VIDEASY       to "Videasy",
        P_CINEMAOS      to "CinemaOS",
        P_VICSRCWTF     to "VicSrcWtf",
        P_VIDLINK       to "Vidlink",
        P_MAPPLE        to "Mapple",
        P_VIDSTACK      to "Vidstack",
        P_VIDZEE        to "Vidzee",
        P_WYZIESUBS     to "WYZIESubs",
        P_STREMIOSUBS   to "StremioSubs",
        P_NETFLIX       to "Netflix",
        P_PRIMEVIDEO    to "Prime Video",
        P_DISNEY        to "Hotstar",
        P_BOLLYWOOD     to "Gramcinema",
        P_FLIXINDIA     to "FlixIndia",
        P_VEGAMOVIES    to "VegaMovies",
        P_ROGMOVIES     to "RogMovies",
        P_BOLLYFLIX     to "Bollyflix",
        P_TOPMOVIES     to "TopMovies",
        P_MOVIESMOD     to "Moviessmod",
        P_MOVIES4U      to "Movies4u",
        P_UHDMOVIES     to "UHDMovies",
        P_MOVIESDRIVE   to "MoviesDrive",
        P_HINDMOVIEZ    to "Hindmoviez",
        P_4KHDHUB       to "4KHDHub",
        P_XDMOVIES      to "XDMovies",
        P_PRIMESRC      to "PrimeSrc",
        P_PROJECTFREETV to "ProjectFreeTV",
        P_LEVIDIA       to "Levidia",
        P_DAHMERMOVIES  to "DahmerMovies",
        P_MULTIMOVIES   to "Multimovies",
        P_PROTONMOVIES  to "Protonmovies",
        P_AKWAM         to "Akwam",
        P_RTALLY        to "Rtally",
        P_ASIAFLIX      to "Asiaflix",
        P_SKYMOVIES     to "SkyMovies",
        P_HDMOVIE2      to "HDMovie2",
        P_MOSTRAGUARDA  to "Mostraguarda",
        P_TOONSTREAM    to "Toonstream",
        P_ALLANIME      to "AllAnime",
        P_SUDATCHI      to "Sudatchi",
        P_TOKYOINSIDER  to "TokyoInsider",
        P_ANIZONE       to "Anizone",
        P_ANIMES        to "Animes",
        P_GOJO          to "Animetsu",
        P_KISSKH        to "KissKH",
        P_DRAMAFULL     to "Dramafull",
    )

    private val DEFAULT_ORDER = PROVIDER_NAMES.keys.toList()

    fun enabled(key: String): Boolean = getKey<Boolean>(key) ?: (key !in TORRENT_KEYS)

    fun getOrder(): List<String> {
        val saved = getKey<String>(PROVIDER_ORDER_KEY)
            ?.split(",")?.filter { it.isNotBlank() }
            ?: return DEFAULT_ORDER
        return saved + (DEFAULT_ORDER - saved.toSet())
    }

    fun saveOrder(order: List<String>) = setKey(PROVIDER_ORDER_KEY, order.joinToString(","))

    // =========================================================
    // NETMIRROR COOKIE HELPERS
    // =========================================================

    fun saveCookie(cookie: String) {
        setKey(COOKIE_KEY, cookie)
        setKey(TIMESTAMP_KEY, System.currentTimeMillis())
    }

    fun getCookie(): Pair<String?, Long> =
        Pair(getKey<String>(COOKIE_KEY), getKey<Long>(TIMESTAMP_KEY) ?: 0L)

    fun clearCookie() {
        setKey(COOKIE_KEY, null)
        setKey(TIMESTAMP_KEY, null)
    }

    fun saveShowboxToken(token: String) = setKey(SHOWBOX_TOKEN_KEY, token.trim())
    fun getShowboxToken(): String?       = getKey<String>(SHOWBOX_TOKEN_KEY)?.takeIf { it.isNotBlank() }
    fun clearShowboxToken()              = setKey(SHOWBOX_TOKEN_KEY, null)

    // =========================================================
    //  STREMIO ADDON HELPERS
    // =========================================================

    enum class AddonType { HTTPS, TORRENT, DEBRID }

    data class StremioAddon(
        val name: String,
        val url: String,
        val type: AddonType
    )

    fun getStremioAddons(): MutableList<StremioAddon> {
        val raw = getKey<String>(STREMIO_ADDONS_KEY) ?: return mutableListOf()
        return raw.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size < 3) return@mapNotNull null
                val type = runCatching { AddonType.valueOf(parts[2]) }.getOrDefault(AddonType.HTTPS)
                StremioAddon(name = parts[0], url = parts[1], type = type)
            }.toMutableList()
    }

    fun saveStremioAddons(addons: List<StremioAddon>) {
        if (addons.isEmpty()) { setKey(STREMIO_ADDONS_KEY, null as String?); return }
        setKey(STREMIO_ADDONS_KEY, addons.joinToString("\n") { "${it.name}|${it.url}|${it.type}" })
    }


    // =========================================================
    //  SETTINGS DIALOG
    // =========================================================

    fun showSettingsDialog(context: Context, onSave: () -> Unit) {
        var requiresRestart = false

        val pendingChanges = mutableMapOf<String, Any?>()

        var commitOrder: () -> Unit = {}
        var commitStremio: () -> Unit = {}

        val scroll = ScrollView(context).apply {
            isScrollbarFadingEnabled = true
            background = ColorDrawable(BG_DARK)
            isFocusable = false
            descendantFocusability = ScrollView.FOCUS_AFTER_DESCENDANTS
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24.dp(context))
            background = ColorDrawable(BG_DARK)
        }

        layout.addView(createHeroBanner(context))
        layout.addView(View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 8.dp(context))
        })

        // ── Scraping Settings (Purple Theme) ──
        layout.addView(createCollapsibleCard(
            context, "⚙️  Scraping Settings", THEME_SCRAPING_START, THEME_SCRAPING_END
        ) {
            addView(createToggleRow(context, "Download Only Links",
                "Only great for downloading (Not for Streaming)",
                DOWNLOAD_ENABLE, false, pendingChanges, THEME_SCRAPING_START))
            addView(createDivider(context))
            addView(createCookieClearRow(context))
        })

        // ── Febbox / ShowBox Token (Amber Theme) ──
        layout.addView(createShowboxTokenCard(context, pendingChanges))

        // ── Restart banner ──
        val restartBanner = createRestartBanner(context).also { it.visibility = View.GONE }

        // ── Active Catalogs (Emerald Theme) ──
        val onCatalogChanged = {
            requiresRestart = true
            if (restartBanner.visibility == View.GONE) {
                restartBanner.visibility = View.VISIBLE
                restartBanner.alpha = 0f
                restartBanner.translationY = (-12f).dp(context)
                restartBanner.animate()
                    .alpha(1f).translationY(0f)
                    .setDuration(350).setInterpolator(DecelerateInterpolator()).start()
            }
        }
        layout.addView(createCollapsibleCard(
            context, "📡  Active Catalogs", THEME_CATALOG_START, THEME_CATALOG_END
        ) {
            addView(createToggleRow(context, "CineStream", "Cinemeta catalog",
                PROVIDER_CINESTREAM, true, pendingChanges, THEME_CATALOG_START, onCatalogChanged))
            addView(createDivider(context))
            addView(createToggleRow(context, "CineSimkl", "Simkl catalog",
                PROVIDER_SIMKL, true, pendingChanges, THEME_CATALOG_START, onCatalogChanged))
            addView(createDivider(context))
            addView(createToggleRow(context, "CineTmdb", "TMDB catalog",
                PROVIDER_TMDB, true, pendingChanges, THEME_CATALOG_START, onCatalogChanged))
        })

        layout.addView(restartBanner)

        // ── Providers (Rose Theme) ──
        layout.addView(createProvidersCard(context, pendingChanges) { commit ->
            commitOrder = commit
        })

        // ── Stremio Addons (Cyan Theme) ──
        layout.addView(createStremioAddonsCard(context) { commit ->
            commitStremio = commit
        })

        // ── Credits (Sky Blue Theme) ──
        layout.addView(createCreditsCard(context))

        scroll.addView(layout)

        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog)
            .setView(scroll)
            .setPositiveButton("Save") { _, _ ->
                pendingChanges.forEach { (key, value) ->
                    when {
                        key == SHOWBOX_TOKEN_KEY && value == null   -> clearShowboxToken()
                        key == SHOWBOX_TOKEN_KEY && value is String -> saveShowboxToken(value)
                        value is Boolean                            -> setKey(key, value)
                        value == null                               -> setKey(key, null as String?)
                    }
                }
                commitOrder()
                commitStremio()
                if (requiresRestart) showRestartWarning(context, onSave) else onSave()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.window?.setBackgroundDrawable(roundRect(BG_DARK, 20f.dp(context)))
        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.95).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.apply { setTextColor(ACCENT_START); isAllCaps = false }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.apply { setTextColor(TEXT_SECONDARY); isAllCaps = false }
    }

    // =========================================================
    //  COOKIE CLEAR ROW
    // =========================================================

    private fun createCookieClearRow(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp(context), 14.dp(context), 16.dp(context), 14.dp(context))
            gravity = Gravity.CENTER_VERTICAL

            val textCol = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            textCol.addView(TextView(context).apply {
                text = "Clear Netmirror Cookies"; textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(TEXT_PRIMARY)
            })
            textCol.addView(TextView(context).apply {
                text = "Remove saved Netmirror session cookies"
                textSize = 12f; setTextColor(TEXT_SECONDARY); setPadding(0, 3.dp(context), 0, 0)
            })
            addView(textCol)

            addView(pillBtn(context, "Clear", DANGER_COLOR,
                Color.parseColor("#1A0A0D"), Color.parseColor("#3A1520")) {
                clearCookie()
                Toast.makeText(context, "🍪 Cookies cleared!", Toast.LENGTH_SHORT).show()
            })
        }
    }

    // =========================================================
    //  SHOWBOX TOKEN CARD
    // =========================================================

    private fun createShowboxTokenCard(
        context: Context,
        pendingChanges: MutableMap<String, Any?>
    ): View {
        val SHOWBOX_ACCENT = Color.parseColor("#F59E0B")
        val SHOWBOX_BG     = Color.parseColor("#13100A")
        val SHOWBOX_BORDER = Color.parseColor("#3A2800")

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val m = 16.dp(context)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(m, 0, m, m) }
            background = roundRect(BG_CARD, 16f.dp(context)); elevation = 4f
        }

        var expanded = false

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(context), 4.dp(context), 16.dp(context), 16.dp(context))
            visibility = View.GONE
        }

        content.addView(TextView(context).apply {
            text = "Enter your Febbox token to enable ShowBox source"
            textSize = 12f; setTextColor(TEXT_SECONDARY)
            setPadding(4.dp(context), 0, 4.dp(context), 10.dp(context))
        })

        val initialToken = (pendingChanges[SHOWBOX_TOKEN_KEY] as? String) ?: getShowboxToken() ?: ""

        val input = EditText(context).apply {
            hint = "Paste UI token"
            setText(initialToken)
            setTextColor(TEXT_PRIMARY); setHintTextColor(TEXT_SECONDARY)
            textSize = 13f; setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            isFocusable = true; isFocusableInTouchMode = true
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) (v.parent?.parent as? ScrollView)?.requestChildFocus(v, v)
            }
            setPadding(14.dp(context), 12.dp(context), 14.dp(context), 12.dp(context))
            background = GradientDrawable().apply {
                cornerRadius = 10f.dp(context)
                setColor(Color.parseColor("#0D1117"))
                setStroke(1, Color.parseColor("#2E2850"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 8.dp(context) }
        }
        content.addView(input)

        // ── TV-friendly clipboard row ─────────────────────────────
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val CLIP_TEXT   = Color.parseColor("#94A3B8")
        val CLIP_BG     = Color.parseColor("#0F1520")
        val CLIP_BORDER = Color.parseColor("#1E2A3A")

        content.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 10.dp(context) }

            // 📋 Paste — reads system clipboard into the field
            addView(pillBtn(context, "📋 Paste", CLIP_TEXT, CLIP_BG, CLIP_BORDER) {
                val clip = clipboard.primaryClip
                    ?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                if (!clip.isNullOrBlank()) {
                    input.setText(clip)
                    input.setSelection(input.text?.length ?: 0)
                    Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                }
            })
            addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(8.dp(context), 1) })

            // 📄 Copy — copies field contents to system clipboard
            addView(pillBtn(context, "📄 Copy", CLIP_TEXT, CLIP_BG, CLIP_BORDER) {
                val text = input.text?.toString()?.trim()
                if (!text.isNullOrBlank()) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Febbox Token", text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Nothing to copy", Toast.LENGTH_SHORT).show()
                }
            })
        })
        // ─────────────────────────────────────────────────────────────────

        val savedBadge = TextView(context).apply {
            text = when {
                pendingChanges.containsKey(SHOWBOX_TOKEN_KEY) ->
                    if ((pendingChanges[SHOWBOX_TOKEN_KEY] as? String) != null) "✓ Staged" else ""
                getShowboxToken() != null -> "✓ Saved"
                else -> ""
            }
            textSize = 10f; setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#4ADE80"))
            setPadding(0, 0, 8.dp(context), 0)
        }

        content.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            var isVisible = false
            addView(TextView(context).apply {
                text = "👁 Show"; textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(TEXT_SECONDARY)
                setPadding(0, 0, 12.dp(context), 0)
                isClickable = true; isFocusable = true; isFocusableInTouchMode = false
                setOnClickListener {
                    isVisible = !isVisible
                    input.inputType = if (isVisible)
                        android.text.InputType.TYPE_CLASS_TEXT or
                                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    else
                        android.text.InputType.TYPE_CLASS_TEXT or
                                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    input.setSelection(input.text?.length ?: 0)
                    text = if (isVisible) "🙈 Hide" else "👁 Show"
                }
            })
            addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })

            addView(pillBtn(context, "Clear", DANGER_COLOR,
                Color.parseColor("#1A0A0D"), Color.parseColor("#3A1520")) {
                input.setText("")
                pendingChanges[SHOWBOX_TOKEN_KEY] = null
                savedBadge.text = ""
                Toast.makeText(context, "Cleared — tap outer Save to apply",
                    Toast.LENGTH_SHORT).show()
            })
            addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(8.dp(context), 1) })

            addView(pillBtn(context, "Save", SHOWBOX_ACCENT, SHOWBOX_BG, SHOWBOX_BORDER) {
                val token = input.text.toString().trim()
                if (token.isBlank()) {
                    Toast.makeText(context, "Token cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    pendingChanges[SHOWBOX_TOKEN_KEY] = token
                    savedBadge.text = "✓ Staged"
                    Toast.makeText(context, "✓ Staged — tap outer Save to apply",
                        Toast.LENGTH_SHORT).show()
                }
            })
        })

        val chevron = TextView(context).apply {
            text = "▼"; textSize = 11f; setTextColor(TEXT_SECONDARY)
        }

        card.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp(context), 16.dp(context), 16.dp(context), 16.dp(context))
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true; isFocusable = true; isFocusableInTouchMode = false; background = stateDrawable(context)

            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(3.dp(context), 18.dp(context))
                    .also { it.marginEnd = 12.dp(context) }
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(SHOWBOX_ACCENT, Color.parseColor("#D97706")))
                    .apply { cornerRadius = 99f }
            })
            addView(TextView(context).apply {
                text = "📦  Febbox Token"; textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(TEXT_SECONDARY); letterSpacing = 0.08f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(savedBadge); addView(chevron)

            setOnClickListener {
                expanded = !expanded; chevron.text = if (expanded) "▲" else "▼"
                if (!expanded) {
                    savedBadge.text = when {
                        pendingChanges.containsKey(SHOWBOX_TOKEN_KEY) ->
                            if ((pendingChanges[SHOWBOX_TOKEN_KEY] as? String) != null) "✓ Staged" else ""
                        getShowboxToken() != null -> "✓ Saved"
                        else -> ""
                    }
                }
                if (expanded) {
                    content.visibility = View.VISIBLE; content.alpha = 0f
                    content.animate().alpha(1f).setDuration(200).start()
                } else {
                    content.animate().alpha(0f).setDuration(150).withEndAction {
                        content.visibility = View.GONE; content.alpha = 1f
                    }.start()
                }
            }
        })

        card.addView(content)
        card.alpha = 0
