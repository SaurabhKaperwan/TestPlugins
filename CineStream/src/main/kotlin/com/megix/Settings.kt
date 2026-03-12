package com.megix

import android.animation.ObjectAnimator
import android.app.AlertDialog
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

    // ── THEME ─────────────────────────────────────────────────
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

    // ── GLOBAL DB KEYS ────────────────────────────────────────
    const val DOWNLOAD_ENABLE     = "DownloadEnable"
    const val PROVIDER_CINESTREAM = "ProviderCineStream"
    const val PROVIDER_SIMKL      = "ProviderSimkl"
    const val PROVIDER_TMDB       = "ProviderTmdb"
    const val SHOWBOX_TOKEN_KEY   = "showbox_ui_token"
    private const val COOKIE_KEY         = "nf_cookie"
    private const val TIMESTAMP_KEY      = "nf_cookie_timestamp"
    private const val PROVIDER_ORDER_KEY = "provider_order"

    // ── PROVIDER REGISTRY ─────────────────────────────────────
    private var allProviders: List<Provider> = emptyList()

    fun registerProviders(list: List<Provider>) {
        allProviders = list
    }

    // Derived automatically from allProviders
    val providerNames: LinkedHashMap<String, String>
        get() = LinkedHashMap<String, String>().also { m -> allProviders.forEach { m[it.key] = it.name } }

    private val torrentKeys: Set<String>
        get() = allProviders.filter { it.isTorrent }.map { it.key }.toSet()

    private val defaultOrder: List<String>
        get() = allProviders.map { it.key }

    // ── PROVIDER STATE HELPERS ────────────────────────────────

    fun enabled(key: String): Boolean =
        getKey<Boolean>(key) ?: (allProviders.find { it.key == key }?.defaultOn ?: true)

    fun getOrder(): List<String> {
        val saved = getKey<String>(PROVIDER_ORDER_KEY)
            ?.split(",")?.filter { it.isNotBlank() }
            ?: return defaultOrder
        return saved + (defaultOrder - saved.toSet())
    }

    fun saveOrder(order: List<String>) =
        setKey(PROVIDER_ORDER_KEY, order.joinToString(","))

    // ── NETMIRROR COOKIE HELPERS ────────────────────────────────────────

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

    // ── SHOWBOX TOKEN HELPERS ─────────────────────────────────

    fun saveShowboxToken(token: String) = setKey(SHOWBOX_TOKEN_KEY, token.trim())
    fun getShowboxToken(): String?       = getKey<String>(SHOWBOX_TOKEN_KEY)?.takeIf { it.isNotBlank() }
    fun clearShowboxToken()              = setKey(SHOWBOX_TOKEN_KEY, null)

    // =========================================================
    //  SETTINGS DIALOG
    // =========================================================

    fun showSettingsDialog(context: Context, onSave: () -> Unit) {
        var requiresRestart = false

        val scroll = ScrollView(context).apply {
            isScrollbarFadingEnabled = true
            background = ColorDrawable(BG_DARK)
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24.dp(context))
            background = ColorDrawable(BG_DARK)
        }

        layout.addView(createHeroBanner(context))

        layout.addView(View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8.dp(context))
        })

        layout.addView(createCollapsibleCard(context, "⚙️  Scraping Settings") {
            addView(createToggleRow(context, "Download Only Links",
                "Only great for downloading (Not for Streaming)", DOWNLOAD_ENABLE, false))
            addView(createDivider(context))
            addView(createCookieClearRow(context))
        })

        layout.addView(createShowboxTokenCard(context))

        val restartBanner = createRestartBanner(context).also { it.visibility = View.GONE }

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

        layout.addView(createCollapsibleCard(context, "📡  Active Catalogs") {
            addView(createToggleRow(context, "CineStream", "Cinemeta catalog",  PROVIDER_CINESTREAM, true, onCatalogChanged))
            addView(createDivider(context))
            addView(createToggleRow(context, "CineSimkl",  "Simkl catalog",     PROVIDER_SIMKL,      true, onCatalogChanged))
            addView(createDivider(context))
            addView(createToggleRow(context, "CineTmdb",   "TMDB catalog",      PROVIDER_TMDB,       true, onCatalogChanged))
        })

        layout.addView(restartBanner)
        layout.addView(createProvidersCard(context))
        layout.addView(createCreditsCard(context))

        scroll.addView(layout)

        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog)
            .setView(scroll)
            .setPositiveButton("Save & Reload") { _, _ ->
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply { setTextColor(ACCENT_START); isAllCaps = false }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply { setTextColor(TEXT_SECONDARY); isAllCaps = false }
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
            addView(pillBtn(context, "Clear", DANGER_COLOR, Color.parseColor("#1A0A0D"), Color.parseColor("#3A1520")) {
                clearCookie()
                Toast.makeText(context, "🍪 Cookies cleared!", Toast.LENGTH_SHORT).show()
            })
        }
    }

    // =========================================================
    //  SHOWBOX TOKEN CARD
    // =========================================================

    private fun createShowboxTokenCard(context: Context): View {
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

        val input = EditText(context).apply {
            hint = "Paste UI token"
            setText(getShowboxToken() ?: "")
            setTextColor(TEXT_PRIMARY); setHintTextColor(TEXT_SECONDARY)
            textSize = 13f; setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(14.dp(context), 12.dp(context), 14.dp(context), 12.dp(context))
            background = GradientDrawable().apply {
                cornerRadius = 10f.dp(context)
                setColor(Color.parseColor("#0D1117"))
                setStroke(1, Color.parseColor("#2E2850"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 10.dp(context) }
        }
        content.addView(input)

        content.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            var isVisible = false
            addView(TextView(context).apply {
                text = "👁 Show"; textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(TEXT_SECONDARY)
                setPadding(0, 0, 12.dp(context), 0)
                isClickable = true; isFocusable = true
                setOnClickListener {
                    isVisible = !isVisible
                    input.inputType = if (isVisible)
                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    else
                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    input.setSelection(input.text?.length ?: 0)
                    text = if (isVisible) "🙈 Hide" else "👁 Show"
                }
            })
            addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })
            addView(pillBtn(context, "Clear", DANGER_COLOR, Color.parseColor("#1A0A0D"), Color.parseColor("#3A1520")) {
                input.setText(""); clearShowboxToken()
                Toast.makeText(context, "Febbox token cleared", Toast.LENGTH_SHORT).show()
            })
            addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(8.dp(context), 1) })
            addView(pillBtn(context, "Save", SHOWBOX_ACCENT, SHOWBOX_BG, SHOWBOX_BORDER) {
                val token = input.text.toString().trim()
                if (token.isBlank()) Toast.makeText(context, "Token cannot be empty", Toast.LENGTH_SHORT).show()
                else { saveShowboxToken(token); Toast.makeText(context, "✓ Febbox token saved", Toast.LENGTH_SHORT).show() }
            })
        })

        val chevron    = TextView(context).apply { text = "▼"; textSize = 11f; setTextColor(TEXT_SECONDARY) }
        val savedBadge = TextView(context).apply {
            text = if (getShowboxToken() != null) "✓ Saved" else ""
            textSize = 10f; setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#4ADE80")); setPadding(0, 0, 8.dp(context), 0)
        }

        card.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp(context), 16.dp(context), 16.dp(context), 16.dp(context))
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true; isFocusable = true; background = stateDrawable(context)
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(3.dp(context), 18.dp(context)).also { it.marginEnd = 12.dp(context) }
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(SHOWBOX_ACCENT, Color.parseColor("#D97706"))).apply { cornerRadius = 99f }
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
                if (!expanded) savedBadge.text = if (getShowboxToken() != null) "✓ Saved" else ""
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
        card.alpha = 0f; card.translationY = 20f
        card.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(DecelerateInterpolator()).start()
        return card
    }

    // =========================================================
    //  CREDITS CARD
    // =========================================================

    private fun createCreditsCard(context: Context): View {
        val CREDIT_ACCENT = Color.parseColor("#38BDF8")
        val CREDIT_BG     = Color.parseColor("#0A1420")
        val CREDIT_BORDER = Color.parseColor("#1A3040")
        val contributors  = listOf(
            Triple("phisher98",     "For multi-source plugin inspiration", "github.com/phisher98"),
            Triple("AzartX47",      "For providing multiple API",          "github.com/AzartX47"),
            Triple("yogesh-hacker", "For providing reference",             "github.com/yogesh-hacker"),
        )

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
            text = "Special thanks to these developers whose work served as reference and inspiration ❤️"
            textSize = 12f; setTextColor(TEXT_SECONDARY)
            setPadding(4.dp(context), 4.dp(context), 4.dp(context), 14.dp(context))
        })

        contributors.forEachIndexed { i, (name, role, url) ->
            if (i > 0) content.addView(createDivider(context))
            content.addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(4.dp(context), 12.dp(context), 4.dp(context), 12.dp(context))
                gravity = Gravity.CENTER_VERTICAL
                addView(TextView(context).apply {
                    text = name.first().uppercaseChar().toString(); textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(CREDIT_ACCENT)
                    gravity = Gravity.CENTER
                    val size = 36.dp(context)
                    layoutParams = LinearLayout.LayoutParams(size, size).also { it.marginEnd = 14.dp(context) }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL; setColor(CREDIT_BG); setStroke(2, CREDIT_BORDER)
                    }
                })
                val col = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                col.addView(TextView(context).apply {
                    text = name; textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(TEXT_PRIMARY)
                })
                col.addView(TextView(context).apply {
                    text = role; textSize = 11f; setTextColor(TEXT_SECONDARY)
                    setPadding(0, 2.dp(context), 0, 0)
                })
                addView(col)
                addView(pillBtn(context, "GitHub", CREDIT_ACCENT, CREDIT_BG, CREDIT_BORDER) {
                    try {
                        context.startActivity(android.content.Intent(
                            android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://$url")
                        ))
                    } catch (_: Exception) { Toast.makeText(context, url, Toast.LENGTH_SHORT).show() }
                })
            })
        }

        val chevron = TextView(context).apply { text = "▼"; textSize = 11f; setTextColor(TEXT_SECONDARY) }
        card.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp(context), 16.dp(context), 16.dp(context), 16.dp(context))
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true; isFocusable = true; background = stateDrawable(context)
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(3.dp(context), 18.dp(context)).also { it.marginEnd = 12.dp(context) }
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(CREDIT_ACCENT, Color.parseColor("#0EA5E9"))).apply { cornerRadius = 99f }
            })
            addView(TextView(context).apply {
                text = "🙏  Credits & Thanks"; textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(TEXT_SECONDARY); letterSpacing = 0.08f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(chevron)
            setOnClickListener {
                expanded = !expanded; chevron.text = if (expanded) "▲" else "▼"
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
        card.alpha = 0f; card.translationY = 20f
        card.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(DecelerateInterpolator()).start()
        return card
    }

    // =========================================================
    //  COLLAPSIBLE CARD
    // =========================================================

    private fun createCollapsibleCard(
        context: Context, title: String,
        startExpanded: Boolean = false,
        block: LinearLayout.() -> Unit
    ): View {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val m = 16.dp(context)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(m, 0, m, m) }
            background = roundRect(BG_CARD, 16f.dp(context)); elevation = 4f
        }
        var expanded = startExpanded
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, 8.dp(context))
            visibility = if (expanded) View.VISIBLE else View.GONE
        }
        content.block()
        val chevron = TextView(context).apply {
            text = if (expanded) "▲" else "▼"; textSize = 11f; setTextColor(TEXT_SECONDARY)
        }
        card.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp(context), 16.dp(context), 16.dp(context), 16.dp(context))
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true; isFocusable = true; background = stateDrawable(context)
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(3.dp(context), 18.dp(context)).also { it.marginEnd = 12.dp(context) }
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(ACCENT_START, ACCENT_END)).apply { cornerRadius = 99f }
            })
            addView(TextView(context).apply {
                text = title; textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(TEXT_SECONDARY); letterSpacing = 0.08f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(chevron)
            setOnClickListener {
                expanded = !expanded; chevron.text = if (expanded) "▲" else "▼"
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
        card.alpha = 0f; card.translationY = 20f
        card.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(DecelerateInterpolator()).start()
        return card
    }

    // =========================================================
    //  PROVIDERS CARD
    // =========================================================

    private fun createProvidersCard(context: Context): View {
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
            setPadding(0, 0, 0, 8.dp(context)); visibility = View.GONE
        }
        val rows  = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val order = getOrder().toMutableList()

        fun rebuild() {
            rows.removeAllViews()
            // Get the current list of keys to display
            val order = getOrder()

            order.forEachIndexed { i, key ->
                // Find the full provider object from your registry
                val p = allProviders.find { it.key == key }

                if (i > 0) rows.addView(createDivider(context))
                rows.addView(createProviderRow(
                    context,
                    label       = p?.name ?: key,
                    key         = key,
                    index       = i + 1,
                    totalCount  = order.size,
                    isTorrent   = p?.isTorrent ?: false,
                    canMoveUp   = i > 0,
                    canMoveDown = i < order.lastIndex,
                    onMoveUp    = {
                        val currentOrder = order.toMutableList()
                        currentOrder.add(i - 1, currentOrder.removeAt(i))
                        saveOrder(currentOrder)
                        rebuild()
                    },
                    onMoveDown  = {
                        val currentOrder = order.toMutableList()
                        currentOrder.add(i + 1, currentOrder.removeAt(i))
                        saveOrder(currentOrder)
                        rebuild()
                    },
                    onMoveTo    = { target ->
                        val currentOrder = order.toMutableList()
                        val item = currentOrder.removeAt(i)
                        currentOrder.add(target.coerceIn(0, currentOrder.size), item)
                        saveOrder(currentOrder)
                        rebuild()
                    }
                ))
            }
        }

        val pillRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 6.dp(context) }
        }
        pillRow.addView(pillBtn(context, "✓ All", Color.parseColor("#4ADE80"), Color.parseColor("#0A1A0F"), Color.parseColor("#1A3A1F")) {
            order.forEach { setKey(it, true) }; rebuild()
            Toast.makeText(context, "All providers enabled", Toast.LENGTH_SHORT).show()
        })
        pillRow.addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(8.dp(context), 1) })
        pillRow.addView(pillBtn(context, "✕ None", DANGER_COLOR, Color.parseColor("#1A0A0D"), Color.parseColor("#3A1520")) {
            order.forEach { setKey(it, false) }; rebuild()
            Toast.makeText(context, "All providers disabled", Toast.LENGTH_SHORT).show()
        })
        pillRow.addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(8.dp(context), 1) })
        pillRow.addView(pillBtn(context, "↺ Reset Order", ACCENT_START, Color.parseColor("#1A1730"), Color.parseColor("#2E2850")) {
            order.clear(); order.addAll(defaultOrder); saveOrder(order); rebuild()
            Toast.makeText(context, "Order reset", Toast.LENGTH_SHORT).show()
        })

        val toolbar = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(context), 8.dp(context), 16.dp(context), 4.dp(context))
            addView(pillRow)
            addView(TextView(context).apply {
                text = "🧲 = off by default  ·  ↑↓ or tap # = scraping order"
                textSize = 10f; setTextColor(Color.parseColor("#44475A"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 4.dp(context) }
            })
        }
        val sep = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                .also { it.setMargins(16.dp(context), 0, 16.dp(context), 4.dp(context)) }
            setBackgroundColor(DIVIDER_COLOR)
        }

        rebuild()
        content.addView(toolbar); content.addView(sep); content.addView(rows)

        val chevron = TextView(context).apply { text = "▼"; textSize = 11f; setTextColor(TEXT_SECONDARY) }
        val summary = TextView(context).apply {
            textSize = 11f; setTextColor(Color.parseColor("#5A5E7A")); setPadding(0, 0, 8.dp(context), 0)
        }
        fun updateSummary() { summary.text = "${order.count { enabled(it) }} / ${order.size} on" }
        updateSummary()

        card.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp(context), 16.dp(context), 16.dp(context), 16.dp(context))
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true; isFocusable = true; background = stateDrawable(context)
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(3.dp(context), 18.dp(context)).also { it.marginEnd = 12.dp(context) }
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(ACCENT_START, ACCENT_END)).apply { cornerRadius = 99f }
            })
            addView(TextView(context).apply {
                text = "🎬  Providers"; textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(TEXT_SECONDARY); letterSpacing = 0.08f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(summary); addView(chevron)
            setOnClickListener {
                expanded = !expanded; chevron.text = if (expanded) "▲" else "▼"
                updateSummary()
                if (expanded) {
                    content.visibility = View.VISIBLE; content.alpha = 0f
                    content.animate().alpha(1f).setDuration(220).start()
                } else {
                    content.animate().alpha(0f).setDuration(160).withEndAction {
                        content.visibility = View.GONE; content.alpha = 1f; updateSummary()
                    }.start()
                }
            }
        })
        card.addView(content)
        card.alpha = 0f; card.translationY = 20f
        card.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(DecelerateInterpolator()).start()
        return card
    }

    // =========================================================
    //  PROVIDER ROW
    // =========================================================

    private fun createProviderRow(
        context: Context, label: String, key: String,
        index: Int, totalCount: Int, isTorrent: Boolean,
        canMoveUp: Boolean, canMoveDown: Boolean,
        onMoveUp: () -> Unit, onMoveDown: () -> Unit,
        onMoveTo: (Int) -> Unit
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16.dp(context), 10.dp(context), 12.dp(context), 10.dp(context))
            gravity = Gravity.CENTER_VERTICAL

            addView(TextView(context).apply {
                text = "$index"; textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(if (isTorrent) Color.parseColor("#5A3E1E") else ACCENT_START)
                gravity = Gravity.CENTER
                setPadding(4.dp(context), 4.dp(context), 4.dp(context), 4.dp(context))
                background = GradientDrawable().apply {
                    cornerRadius = 6f.dp(context); setColor(Color.parseColor("#1A1730"))
                    setStroke(1, if (isTorrent) Color.parseColor("#3A2810") else Color.parseColor("#2E2850"))
                }
                minWidth = 28.dp(context); isClickable = true; isFocusable = true
                setOnClickListener {
                    val input = EditText(context).apply {
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER
                        hint = "1 – $totalCount"; setText("$index")
                        setTextColor(TEXT_PRIMARY); setHintTextColor(TEXT_SECONDARY); selectAll()
                        setPadding(16.dp(context), 12.dp(context), 16.dp(context), 12.dp(context))
                        background = GradientDrawable().apply {
                            cornerRadius = 10f.dp(context); setColor(Color.parseColor("#1A1E28"))
                            setStroke(1, Color.parseColor("#2E2850"))
                        }
                    }
                    val wrapper = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(24.dp(context), 16.dp(context), 24.dp(context), 8.dp(context))
                        addView(TextView(context).apply {
                            text = "Move \"$label\" to position"; textSize = 13f
                            setTextColor(TEXT_SECONDARY); setPadding(0, 0, 0, 10.dp(context))
                        })
                        addView(input)
                    }
                    val d = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog)
                        .setView(wrapper)
                        .setPositiveButton("Move") { _, _ ->
                            val t = input.text.toString().toIntOrNull()
                            if (t != null && t in 1..totalCount) onMoveTo(t - 1)
                            else Toast.makeText(context, "Enter 1 – $totalCount", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null).create()
                    d.window?.setBackgroundDrawable(roundRect(BG_DARK, 16f.dp(context))); d.show()
                    d.getButton(AlertDialog.BUTTON_POSITIVE)?.apply { setTextColor(ACCENT_START); isAllCaps = false }
                    d.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply { setTextColor(TEXT_SECONDARY); isAllCaps = false }
                    input.requestFocus()
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                        .showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                }
            })

            addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(10.dp(context), 1) })

            addView(TextView(context).apply {
                text = label; textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(if (isTorrent) Color.parseColor("#C87C3A") else TEXT_PRIMARY)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            fun arrowBtn(sym: String, active: Boolean, action: () -> Unit) = TextView(context).apply {
                text = sym; textSize = 17f; gravity = Gravity.CENTER
                setTextColor(if (active) ACCENT_START else Color.parseColor("#252840"))
                setPadding(9.dp(context), 6.dp(context), 9.dp(context), 6.dp(context))
                isClickable = active; isFocusable = active
                if (active) setOnClickListener {
                    animate().scaleX(0.75f).scaleY(0.75f).setDuration(60).withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    }.start(); action()
                }
            }
            addView(arrowBtn("↑", canMoveUp, onMoveUp))
            addView(arrowBtn("↓", canMoveDown, onMoveDown))

            addView(Switch(context).apply {
                isChecked = getKey<Boolean>(key) ?: (allProviders.find { it.key == key }?.defaultOn ?: true)
                thumbTintList = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(Color.WHITE, Color.parseColor("#9099B8"))
                )
                trackTintList = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(if (isTorrent) Color.parseColor("#8B5E3C") else SWITCH_ON, SWITCH_OFF)
                )
                setOnCheckedChangeListener { _, v -> setKey(key, v) }
            })
        }
    }

    // =========================================================
    //  PILL BUTTON
    // =========================================================

    private fun pillBtn(
        context: Context, label: String,
        textColor: Int, bgColor: Int, borderColor: Int,
        onClick: () -> Unit
    ) = TextView(context).apply {
        text = label; textSize = 11f
        setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(textColor)
        setPadding(12.dp(context), 6.dp(context), 12.dp(context), 6.dp(context))
        background = GradientDrawable().apply { cornerRadius = 99f; setColor(bgColor); setStroke(1, borderColor) }
        isClickable = true; isFocusable = true
        setOnClickListener {
            animate().scaleX(0.88f).scaleY(0.88f).setDuration(70).withEndAction {
                animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start(); onClick()
        }
    }

    // =========================================================
    //  HERO BANNER
    // =========================================================

    private fun createHeroBanner(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28.dp(context), 32.dp(context), 28.dp(context), 24.dp(context))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.parseColor("#1A1730"), BG_DARK))
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(48.dp(context), 4.dp(context)).also { it.bottomMargin = 16.dp(context) }
                background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(ACCENT_START, ACCENT_END)).apply { cornerRadius = 99f }
            })
            addView(TextView(context).apply {
                text = "Plugin Settings"; textSize = 22f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(TEXT_PRIMARY); letterSpacing = -0.02f
            })
            addView(TextView(context).apply {
                text = "Configure sources, catalogs & cookies"
                textSize = 13f; setTextColor(TEXT_SECONDARY); setPadding(0, 6.dp(context), 0, 0)
            })
        }
    }

    // =========================================================
    //  TOGGLE ROW
    // =========================================================

    private fun createToggleRow(
        context: Context, label: String, subtitle: String,
        databaseKey: String, defaultState: Boolean, onChanged: () -> Unit = {}
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp(context), 14.dp(context), 16.dp(context), 14.dp(context))
            gravity = Gravity.CENTER_VERTICAL; background = stateDrawable(context)
            val textCol = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            textCol.addView(TextView(context).apply {
                text = label; textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(TEXT_PRIMARY)
            })
            textCol.addView(TextView(context).apply {
                text = subtitle; textSize = 12f; setTextColor(TEXT_SECONDARY)
                setPadding(0, 3.dp(context), 0, 0)
            })
            addView(textCol)
            addView(Switch(context).apply {
                isChecked = getKey<Boolean>(databaseKey) ?: defaultState
                thumbTintList = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(Color.WHITE, Color.parseColor("#9099B8"))
                )
                trackTintList = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(SWITCH_ON, SWITCH_OFF)
                )
                setOnCheckedChangeListener { _, isChecked ->
                    setKey(databaseKey, isChecked)
                    animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    }.start(); onChanged()
                }
            })
        }
    }

    // =========================================================
    //  RESTART BANNER
    // =========================================================

    private fun createRestartBanner(context: Context): LinearLayout {
        val WARN_BG     = Color.parseColor("#13100A")
        val WARN_BORDER = Color.parseColor("#4A3200")
        val WARN_ACCENT = Color.parseColor("#F5A623")
        val WARN_DIM    = Color.parseColor("#9E7A30")
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            val m = 16.dp(context)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(m, 0, m, m) }
            background = GradientDrawable().apply {
                cornerRadius = 14f.dp(context); setColor(WARN_BG); setStroke(1, WARN_BORDER)
            }
            setPadding(16.dp(context), 14.dp(context), 16.dp(context), 14.dp(context))
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(8.dp(context), 8.dp(context)).also {
                    it.marginEnd = 12.dp(context); it.topMargin = 2.dp(context)
                }
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(WARN_ACCENT) }
            }
            addView(dot)
            ObjectAnimator.ofFloat(dot, "alpha", 1f, 0.25f, 1f).apply {
                duration = 1200; repeatCount = ObjectAnimator.INFINITE; interpolator = DecelerateInterpolator()
            }.start()
            val textCol = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            textCol.addView(TextView(context).apply {
                text = "Restart Required"; textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(WARN_ACCENT)
            })
            textCol.addView(TextView(context).apply {
                text = "Fully close & reopen Cloudstream to apply catalog changes"
                textSize = 11f; setTextColor(WARN_DIM); setPadding(0, 3.dp(context), 0, 0)
            })
            addView(textCol)
            addView(TextView(context).apply {
                text = "↺"; textSize = 22f; setTextColor(WARN_ACCENT)
                setPadding(12.dp(context), 0, 0, 0); alpha = 0.85f
            })
        }
    }

    // =========================================================
    //  RESTART WARNING DIALOG
    // =========================================================

    private fun showRestartWarning(context: Context, onSave: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Restart Required ⚠️")
            .setMessage("You've changed your Active Catalogs.\n\nPlease fully close and reopen Cloudstream for providers to update.")
            .setPositiveButton("Got it") { _, _ -> onSave() }
            .setCancelable(false).show()
    }

    // =========================================================
    //  DIVIDER
    // =========================================================

    private fun createDivider(context: Context): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                .also { it.setMargins(20.dp(context), 0, 20.dp(context), 0) }
            setBackgroundColor(DIVIDER_COLOR)
        }
    }

    // ── DRAWING HELPERS ───────────────────────────────────────

    private fun roundRect(color: Int, radius: Float) = GradientDrawable().apply {
        cornerRadius = radius; setColor(color)
    }

    private fun stateDrawable(context: Context) = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_pressed),
            GradientDrawable().apply { setColor(Color.parseColor("#1F2235")) })
        addState(intArrayOf(), GradientDrawable().apply { setColor(Color.TRANSPARENT) })
    }

    // ── EXTENSION HELPERS ─────────────────────────────────────

    private fun Int.dp(context: Context): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()

    private fun Float.dp(context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
}
