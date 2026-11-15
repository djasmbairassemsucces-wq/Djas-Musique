package com.dmcreation.djasmusique.data

import android.content.Context

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class AppPrefs(context: Context) {
    private val sp = context.getSharedPreferences("djas_prefs", Context.MODE_PRIVATE)

    var introSeen: Boolean
        get() = sp.getBoolean("intro_seen", false)
        set(v) = sp.edit().putBoolean("intro_seen", v).apply()

    var themeMode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(sp.getString("theme_mode", null) ?: "") }
            .getOrDefault(ThemeMode.SYSTEM)
        set(v) = sp.edit().putString("theme_mode", v.name).apply()

    var favorites: Set<Long>
        get() = (sp.getStringSet("favorites", emptySet()) ?: emptySet())
            .mapNotNull { it.toLongOrNull() }.toSet()
        set(v) = sp.edit().putStringSet("favorites", v.map { it.toString() }.toSet()).apply()

    val recents: List<Long>
        get() = (sp.getString("recents", "") ?: "")
            .split(",").mapNotNull { it.toLongOrNull() }

    fun addRecent(id: Long) {
        val list = (listOf(id) + recents.filter { it != id }).take(50)
        sp.edit().putString("recents", list.joinToString(",")).apply()
    }

    fun videoPosition(id: Long): Long = sp.getLong("vpos_$id", 0L)

    fun setVideoPosition(id: Long, positionMs: Long) {
        sp.edit().putLong("vpos_$id", positionMs).apply()
    }
}
