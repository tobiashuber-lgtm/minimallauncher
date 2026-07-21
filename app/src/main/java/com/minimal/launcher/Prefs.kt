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
        prefs(context).getString("font_family", "space_mono:normal") ?: "space_mono:normal"

    fun setFontFamily(context: Context, value: String) {
        prefs(context).edit().putString("font_family", value).apply()
    }

    fun getClockFontFamily(context: Context): String =
        prefs(context).getString("clock_font_family", "space_mono:bold") ?: "space_mono:bold"

    fun setClockFontFamily(context: Context, value: String) {
        prefs(context).edit().putString("clock_font_family", value).apply()
    }

    // Farb-Regler 0-100: 0 = Schwarz, 100 = Weiss, dazwischen der Farbkreis.
    fun getBgColorSlider(context: Context): Int = prefs(context).getInt("bg_color_slider", 0)
    fun setBgColorSlider(context: Context, value: Int) {
        prefs(context).edit().putInt("bg_color_slider", value).apply()
    }

    fun getTextColorSlider(context: Context): Int = prefs(context).getInt("text_color_slider", 100)
    fun setTextColorSlider(context: Context, value: Int) {
        prefs(context).edit().putInt("text_color_slider", value).apply()
    }

    fun getAccentColorSlider(context: Context): Int = prefs(context).getInt("accent_color_slider", 14)
    fun setAccentColorSlider(context: Context, value: Int) {
        prefs(context).edit().putInt("accent_color_slider", value).apply()
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

    fun getFavoritePackages(context: Context, mode: String? = null): List<String> {
        val key = if (mode == null) "favorite_packages" else "mode_favorites_$mode"
        val raw = prefs(context).getString(key, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    fun setFavoritePackages(context: Context, packages: List<String>, mode: String? = null) {
        val key = if (mode == null) "favorite_packages" else "mode_favorites_$mode"
        prefs(context).edit().putString(key, packages.joinToString(",")).apply()
    }

    fun getDockEnabled(context: Context): Boolean =
        prefs(context).getBoolean("dock_enabled", true)

    fun setDockEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("dock_enabled", value).apply()
    }

    // --- Vertikale Wischgesten (oben/unten): frei zuweisbare Aktion ---
    // Werte: "none", "flashlight", "notes", "drawer", oder "app:<packageName>"

    fun getSwipeUpAction(context: Context): String =
        prefs(context).getString("swipe_up_action", "none") ?: "none"

    fun setSwipeUpAction(context: Context, value: String) {
        prefs(context).edit().putString("swipe_up_action", value).apply()
    }

    fun getSwipeDownAction(context: Context): String =
        prefs(context).getString("swipe_down_action", "none") ?: "none"

    fun setSwipeDownAction(context: Context, value: String) {
        prefs(context).edit().putString("swipe_down_action", value).apply()
    }

    // --- Zeitlimit-Dialog fuer bestimmte Apps (z.B. Social Media) ---

    fun getTimeLimitedPackages(context: Context): Set<String> =
        prefs(context).getStringSet("time_limited_packages", emptySet()) ?: emptySet()

    fun setTimeLimitedPackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet("time_limited_packages", packages).apply()
    }

    // --- Aktive Zeitlimit-Session: welche App laeuft gerade mit Timer,
    // und wann laeuft das urspruenglich gewaehlte Fenster ab. So kann die
    // App innerhalb des Fensters beliebig oft neu geoeffnet werden, ohne
    // dass der Dialog erneut erscheint oder der Timer sich verlaengert.

    fun getActiveTimeLimitPackage(context: Context): String? =
        prefs(context).getString("active_timelimit_package", null)

    fun setActiveTimeLimitPackage(context: Context, packageName: String?) {
        val editor = prefs(context).edit()
        if (packageName == null) editor.remove("active_timelimit_package")
        else editor.putString("active_timelimit_package", packageName)
        editor.apply()
    }

    fun getActiveTimeLimitEndAt(context: Context): Long =
        prefs(context).getLong("active_timelimit_end_at", 0L)

    fun setActiveTimeLimitEndAt(context: Context, endAt: Long) {
        prefs(context).edit().putLong("active_timelimit_end_at", endAt).apply()
    }

    // --- Modi: Standard + frei erstellbare Modi (z.B. "Musik", "Arbeit") ---
    // Ein gesperrter Package in einem Modus wird in diesem Modus komplett
    // ausgeblendet (Drawer + Favoriten) statt nur mit einer Sperr-Meldung
    // versehen - einfacher zu verstehen als zwei getrennte Konzepte.

    fun getModeNames(context: Context): List<String> {
        val raw = prefs(context).getString("mode_names", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    fun addMode(context: Context, name: String) {
        val current = getModeNames(context).toMutableList()
        if (!current.contains(name)) {
            current.add(name)
            prefs(context).edit().putString("mode_names", current.joinToString(",")).apply()
        }
    }

    fun removeMode(context: Context, name: String) {
        val current = getModeNames(context).toMutableList()
        current.remove(name)
        val editor = prefs(context).edit()
        editor.putString("mode_names", current.joinToString(","))
        editor.remove("mode_favorites_$name")
        editor.remove("mode_locked_$name")
        editor.apply()
        if (getCurrentMode(context) == name) setCurrentMode(context, "Standard")
    }

    fun getCurrentMode(context: Context): String =
        prefs(context).getString("current_mode", "Standard") ?: "Standard"

    fun setCurrentMode(context: Context, mode: String) {
        prefs(context).edit().putString("current_mode", mode).apply()
    }

    // "Standard" bleibt intern immer der feste Schluessel (damit alle
    // Vergleiche im Code stabil bleiben) - hier nur ein kosmetischer
    // Anzeigename, der ueberall statt "Standard" gezeigt wird.
    fun getStandardModeDisplayName(context: Context): String =
        prefs(context).getString("standard_mode_display_name", "Standard") ?: "Standard"

    fun setStandardModeDisplayName(context: Context, name: String) {
        prefs(context).edit().putString("standard_mode_display_name", name).apply()
    }

    fun getDockIcon(context: Context, slot: Int): String? =
        prefs(context).getString("dock_icon_$slot", null)

    fun setDockIcon(context: Context, slot: Int, iconKey: String?) {
        val editor = prefs(context).edit()
        if (iconKey == null) editor.remove("dock_icon_$slot") else editor.putString("dock_icon_$slot", iconKey)
        editor.apply()
    }

    fun getModeLockedPackages(context: Context, mode: String): Set<String> =
        prefs(context).getStringSet("mode_locked_$mode", emptySet()) ?: emptySet()

    fun setModeLockedPackages(context: Context, mode: String, packages: Set<String>) {
        prefs(context).edit().putStringSet("mode_locked_$mode", packages).apply()
    }

    // --- Modus-Overrides: null = "wie Standard-Einstellung", sonst eigener Wert ---

    fun getModeStatusBarHidden(context: Context, mode: String): Boolean? {
        val value = prefs(context).getString("mode_statusbar_$mode", "inherit") ?: "inherit"
        return when (value) {
            "on" -> true
            "off" -> false
            else -> null
        }
    }

    fun setModeStatusBarHidden(context: Context, mode: String, value: Boolean?) {
        val stored = when (value) {
            true -> "on"
            false -> "off"
            null -> "inherit"
        }
        prefs(context).edit().putString("mode_statusbar_$mode", stored).apply()
    }

    fun getModeDockEnabled(context: Context, mode: String): Boolean? {
        val value = prefs(context).getString("mode_dock_$mode", "inherit") ?: "inherit"
        return when (value) {
            "on" -> true
            "off" -> false
            else -> null
        }
    }

    fun setModeDockEnabled(context: Context, mode: String, value: Boolean?) {
        val stored = when (value) {
            true -> "on"
            false -> "off"
            null -> "inherit"
        }
        prefs(context).edit().putString("mode_dock_$mode", stored).apply()
    }

    fun getModeAccentColorSlider(context: Context, mode: String): Int? {
        val value = prefs(context).getInt("mode_accent_$mode", -1)
        return if (value < 0) null else value
    }

    fun setModeAccentColorSlider(context: Context, mode: String, value: Int?) {
        prefs(context).edit().putInt("mode_accent_$mode", value ?: -1).apply()
    }

    // --- Modus: welche Apps sollen in diesem Modus stummgeschaltet werden ---

    fun getModeMutedNotificationPackages(context: Context, mode: String): Set<String> =
        prefs(context).getStringSet("mode_muted_notifications_$mode", emptySet()) ?: emptySet()

    fun setModeMutedNotificationPackages(context: Context, mode: String, packages: Set<String>) {
        prefs(context).edit().putStringSet("mode_muted_notifications_$mode", packages).apply()
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

    // --- Dock: manuelle App-Zuordnung pro Slot (0=links...3=rechts) ---

    fun getDockPackage(context: Context, slot: Int): String? =
        prefs(context).getString("dock_slot_$slot", null)

    fun setDockPackage(context: Context, slot: Int, packageName: String?) {
        val editor = prefs(context).edit()
        if (packageName == null) editor.remove("dock_slot_$slot")
        else editor.putString("dock_slot_$slot", packageName)
        editor.apply()
    }

    // --- Sprache: "de" oder "en". Wirkt auf Datum/Wochentag und die
    // wichtigsten Texte im Einstellungen-Hauptbildschirm. ---

    fun getAppLanguage(context: Context): String =
        prefs(context).getString("app_language", "de") ?: "de"

    fun setAppLanguage(context: Context, value: String) {
        prefs(context).edit().putString("app_language", value).apply()
    }

    // --- Was beim Antippen/Halten des Datums passiert ---
    // Werte: "calendar", "new_event", "none"

    fun getDateClickAction(context: Context): String =
        prefs(context).getString("date_click_action", "calendar") ?: "calendar"

    fun setDateClickAction(context: Context, value: String) {
        prefs(context).edit().putString("date_click_action", value).apply()
    }

    fun getDateLongClickAction(context: Context): String =
        prefs(context).getString("date_longclick_action", "new_event") ?: "new_event"

    fun setDateLongClickAction(context: Context, value: String) {
        prefs(context).edit().putString("date_longclick_action", value).apply()
    }

    // --- Modus umbenennen: alle modusspezifischen Daten auf den neuen
    // Namen umziehen (Standard kann so nicht umbenannt werden - dafuer
    // gibt es den separaten Anzeigenamen). ---

    fun renameMode(context: Context, oldName: String, newName: String) {
        if (oldName == "Standard" || newName.isBlank() || oldName == newName) return
        val p = prefs(context)
        val names = getModeNames(context).toMutableList()
        val idx = names.indexOf(oldName)
        if (idx == -1) return
        names[idx] = newName

        val editor = p.edit()
        editor.putString("mode_names", names.joinToString(","))

        val stringKeys = listOf("mode_favorites_", "mode_statusbar_", "mode_dock_")
        val setKeys = listOf("mode_locked_", "mode_muted_notifications_")
        val intKeys = listOf("mode_accent_")

        for (prefix in stringKeys) {
            val oldKey = "$prefix$oldName"
            if (p.contains(oldKey)) {
                p.getString(oldKey, null)?.let { editor.putString("$prefix$newName", it) }
                editor.remove(oldKey)
            }
        }
        for (prefix in setKeys) {
            val oldKey = "$prefix$oldName"
            if (p.contains(oldKey)) {
                editor.putStringSet("$prefix$newName", p.getStringSet(oldKey, emptySet()) ?: emptySet())
                editor.remove(oldKey)
            }
        }
        for (prefix in intKeys) {
            val oldKey = "$prefix$oldName"
            if (p.contains(oldKey)) {
                editor.putInt("$prefix$newName", p.getInt(oldKey, -1))
                editor.remove(oldKey)
            }
        }
        editor.apply()

        if (getCurrentMode(context) == oldName) setCurrentMode(context, newName)
    }

    // --- Notizen: einfache Zeilen-Struktur, als Text serialisiert.
    // Format pro Zeile: "TYP|text", getrennt durch \n.
    // TYP: PLAIN, BOLD, BULLET, CHECK_OFF, CHECK_ON

    fun getNotesRaw(context: Context): String =
        prefs(context).getString("notes_lines", "") ?: ""

    fun setNotesRaw(context: Context, value: String) {
        prefs(context).edit().putString("notes_lines", value).apply()
    }
}
