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
}
