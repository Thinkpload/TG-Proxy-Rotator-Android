package com.thinkpload.tgproxyrotator

import android.content.Context
import android.content.SharedPreferences

object Settings {
    private const val PREFS = "tg_proxy_rotator"
    private const val KEY_MAX_PING = "max_ping_ms"
    private const val KEY_DISABLED_SOURCES = "disabled_sources"
    private const val KEY_FAVORITES = "favorites"

    val DEFAULT_SOURCES = listOf(
        "https://cdn.jsdelivr.net/gh/SoliSpirit/mtproto@master/all_proxies.txt",
        "https://cdn.jsdelivr.net/gh/Grim1313/mtproto-for-telegram@master/all_proxies.txt",
        "https://cdn.jsdelivr.net/gh/ALIILAPRO/MTProtoProxy@main/mtproto.txt",
        "https://raw.githubusercontent.com/SoliSpirit/mtproto/master/all_proxies.txt",
    )

    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var maxPingMs: Int
        get() = prefs.getInt(KEY_MAX_PING, 1000)
        set(v) { prefs.edit().putInt(KEY_MAX_PING, v).apply() }

    fun enabledSources(): List<String> {
        val disabled = prefs.getStringSet(KEY_DISABLED_SOURCES, emptySet()) ?: emptySet()
        return DEFAULT_SOURCES.filter { it !in disabled }
    }

    fun isSourceEnabled(url: String): Boolean =
        url !in (prefs.getStringSet(KEY_DISABLED_SOURCES, emptySet()) ?: emptySet())

    fun setSourceEnabled(url: String, enabled: Boolean) {
        val current = (prefs.getStringSet(KEY_DISABLED_SOURCES, emptySet()) ?: emptySet()).toMutableSet()
        if (enabled) current.remove(url) else current.add(url)
        prefs.edit().putStringSet(KEY_DISABLED_SOURCES, current).apply()
    }

    fun favorites(): Set<String> =
        prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun isFavorite(key: String): Boolean = key in favorites()

    fun toggleFavorite(key: String) {
        val current = favorites().toMutableSet()
        if (key in current) current.remove(key) else current.add(key)
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }
}
