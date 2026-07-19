package com.minimal.launcher

import android.content.Context
import android.content.SharedPreferences

// Zentrale Stelle fuer alle Einstellungen. Alles liegt in SharedPreferences,
// damit Home/Drawer/Settings-Screens ueberall denselben Stand lesen.
object Prefs {

    private const val FILE = "launcher_prefs"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // --- Darstellung ---

    fun getFontFamily(context: Context): String =
        prefs(context).getString("font_family", "monospace") ?: "monospace"

    fun setFontFamily(context: Context, value: String) {
        prefs(context).edit().putString("font_family", value).apply()
    }

    fun getColorScheme(context: Context): String =
        prefs(context).getString("color_scheme", "mono_dark") ?: "mono_dark"

    fun setColorScheme(context: Context, value: String) {
        prefs(context).edit().putString("color_scheme", value).apply()
    }

    fun getClockFormat24h(context: Context): Boolean =
        prefs(context).getBoolean("clock_format_24h", true)

    fun setClockFormat24h(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("clock_format_24h", value).apply()
    }

    fun getClockSizeSp(context: Context): Int =
        prefs(context).getInt("clock_size_sp", 46)

    fun setClockSizeSp(context: Context, value: Int) {
        prefs(context).edit().putInt("clock_size_sp", value).apply()
    }

    fun getDateSizeSp(context: Context): Int =
        prefs(context).getInt("date_size_sp", 14)

    fun setDateSizeSp(context: Context, value: Int) {
        prefs(context).edit().putInt("date_size_sp", value).apply()
    }

    fun getAppListSizeSp(context: Context): Int =
        prefs(context).getInt("applist_size_sp", 17)

    fun setAppListSizeSp(context: Context, value: Int) {
        prefs(context).edit().putInt("applist_size_sp", value).apply()
    }

    fun getStatusBarHidden(context: Context): Boolean =
        prefs(context).getBoolean("statusbar_hidden", true)

    fun setStatusBarHidden(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("statusbar_hidden", value).apply()
    }

    // --- Home-Screen ---

    fun getFavoritesCount(context: Context): Int =
        prefs(context).getInt("favorites_count", 6)

    fun setFavoritesCount(context: Context, value: Int) {
        prefs(context).edit().putInt("favorites_count", value).apply()
    }

    fun getFavoritePackages(context: Context): List<String> {
        val raw = prefs(context).getString("favorite_packages", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    fun setFavoritePackages(context: Context, packages: List<String>) {
        prefs(context).edit().putString("favorite_packages", packages.joinToString(",")).apply()
    }

    fun getDockEnabled(context: Context): Boolean =
        prefs(context).getBoolean("dock_enabled", true)

    fun setDockEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("dock_enabled", value).apply()
    }

    // --- Apps: ausblenden ---

    fun getHiddenPackages(context: Context): Set<String> =
        prefs(context).getStringSet("hidden_packages", emptySet()) ?: emptySet()

    fun setHiddenPackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet("hidden_packages", packages).apply()
    }

    // --- Apps: umbenennen ---

    fun getRename(context: Context, packageName: String): String? =
        prefs(context).getString("rename_$packageName", null)

    fun setRename(context: Context, packageName: String, name: String?) {
        val editor = prefs(context).edit()
        if (name.isNullOrBlank()) {
            editor.remove("rename_$packageName")
        } else {
            editor.putString("rename_$packageName", name)
        }
        editor.apply()
    }

    fun displayNameFor(context: Context, packageName: String, defaultLabel: String): String =
        getRename(context, packageName) ?: defaultLabel
}
