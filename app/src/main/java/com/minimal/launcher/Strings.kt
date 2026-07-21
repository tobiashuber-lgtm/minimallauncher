package com.minimal.launcher

import android.content.Context

// Bewusst einfach gehalten: eine Tabelle mit den wichtigsten Texten des
// Haupt-Einstellungsbildschirms. Tiefere Verwaltungs-Screens (Favoriten-
// Auswahl, App umbenennen, Modi verwalten, ...) bleiben vorerst auf
// Deutsch - eine vollstaendige Uebersetzung aller Screens waere ein
// deutlich groesseres Folge-Update.
object Strings {

    private val table: Map<String, Pair<String, String>> = mapOf(
        "section_display" to ("Darstellung" to "Appearance"),
        "section_home" to ("Home-Screen" to "Home Screen"),
        "section_apps" to ("Apps" to "Apps"),
        "section_modes" to ("Modi" to "Modes"),
        "section_usage" to ("Nutzungszeit" to "Usage Time"),
        "row_font_list" to ("Schriftart (App-Liste)" to "Font (App List)"),
        "row_font_clock" to ("Schriftart (Uhr)" to "Font (Clock)"),
        "row_bg_color" to ("Hintergrundfarbe" to "Background Color"),
        "row_text_color" to ("Schriftfarbe" to "Text Color"),
        "row_accent_color" to ("Akzentfarbe (Uhr)" to "Accent Color (Clock)"),
        "row_clock_format" to ("Uhr-Format" to "Clock Format"),
        "row_statusbar" to ("Statusleiste ausblenden" to "Hide Status Bar"),
        "row_clock_size" to ("Schriftgröße Uhr" to "Clock Font Size"),
        "row_date_size" to ("Schriftgröße Datum" to "Date Font Size"),
        "row_applist_size" to ("Schriftgröße App-Liste" to "App List Font Size"),
        "row_date_click" to ("Bei Datum-Klick" to "On Date Tap"),
        "row_date_longclick" to ("Bei Datum-Halten" to "On Date Long-Press"),
        "row_language" to ("Sprache" to "Language"),
        "row_favorites_count" to ("Anzahl Favoriten" to "Number of Favorites"),
        "row_dock_show" to ("Dock anzeigen" to "Show Dock"),
        "row_favorites_pick" to ("Favoriten auswählen" to "Choose Favorites"),
        "row_dock_customize" to ("Dock anpassen" to "Customize Dock"),
        "row_visibility" to ("Apps aus-/einblenden" to "Show/Hide Apps"),
        "row_rename" to ("Apps umbenennen" to "Rename Apps"),
        "row_timelimit" to ("Apps mit Zeitlimit auswählen" to "Choose Time-Limited Apps"),
        "row_overlay" to ("Countdown-Anzeige beim Zeitlimit" to "Time Limit Countdown Display"),
        "row_active_mode" to ("Aktiver Modus" to "Active Mode"),
        "row_manage_modes" to ("Modi verwalten (erstellen/bearbeiten/löschen)" to "Manage Modes (create/edit/delete)"),
        "row_usage_access" to ("Nutzungszugriff" to "Usage Access"),
        "val_active" to ("aktiv" to "active"),
        "val_inactive" to ("nicht aktiviert" to "not enabled"),
        "val_24h" to ("24-Stunden" to "24-Hour"),
        "val_12h" to ("12-Stunden" to "12-Hour"),
        "val_dark" to ("Dunkel" to "Dark"),
        "val_light" to ("Hell" to "Light"),
        "val_calendar" to ("Kalender öffnen" to "Open Calendar"),
        "val_new_event" to ("Neuer Termin" to "New Event"),
        "val_none" to ("Nichts" to "Nothing"),
        "val_german" to ("Deutsch" to "German"),
        "val_english" to ("Englisch" to "English")
    )

    fun get(context: Context, key: String): String {
        val pair = table[key] ?: return key
        return if (Prefs.getAppLanguage(context) == "en") pair.second else pair.first
    }
}
