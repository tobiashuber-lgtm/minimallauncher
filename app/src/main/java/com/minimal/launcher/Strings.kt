package com.minimal.launcher

import android.content.Context

// Zentrale DE/EN Uebersetzungstabelle - deckt jetzt Haupt-Einstellungen UND
// alle Unter-Screens ab (Modi, Favoriten, Sichtbarkeit, Umbenennen, Dock,
// Zeitlimit, Benachrichtigungen).
object Strings {

    private val table: Map<String, Pair<String, String>> = mapOf(
        // Haupt-Einstellungen
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
        "val_german" to ("Deutsch" to "German"),
        "val_english" to ("Englisch" to "English"),

        // Modi verwalten
        "modes_standard_header" to ("Standard-Modus" to "Standard Mode"),
        "modes_rename_standard" to ("umbenennen" to "rename"),
        "modes_edit_standard" to ("Standard-Modus bearbeiten (Favoriten, Sperren, Farbe, ...)" to "Edit Standard Mode (favorites, locks, color, ...)"),
        "modes_create_new" to ("+ Neuen Modus erstellen" to "+ Create New Mode"),
        "modes_your_modes" to ("Deine Modi" to "Your Modes"),
        "modes_rename_title" to ("Standard-Modus umbenennen" to "Rename Standard Mode"),
        "modes_new_name_title" to ("Name des neuen Modus" to "Name of the New Mode"),
        "modes_create_btn" to ("Erstellen" to "Create"),
        "modes_save" to ("Speichern" to "Save"),
        "modes_cancel" to ("Abbrechen" to "Cancel"),

        // Modus bearbeiten
        "mode_edit_title_prefix" to ("Modus: " to "Mode: "),
        "mode_edit_lock_apps" to ("Gesperrte/ausgeblendete Apps für diesen Modus wählen" to "Choose Locked/Hidden Apps for This Mode"),
        "mode_edit_favorites" to ("Favoriten für diesen Modus wählen" to "Choose Favorites for This Mode"),
        "mode_edit_mute" to ("Benachrichtigungen stummschalten für diesen Modus" to "Mute Notifications for This Mode"),
        "mode_edit_accent" to ("Akzentfarbe (Uhr) - eigene Einstellung" to "Accent Color (Clock) - Custom Setting"),
        "mode_edit_dock" to ("Dock in diesem Modus" to "Dock in This Mode"),
        "mode_edit_statusbar" to ("Statusleiste in diesem Modus" to "Status Bar in This Mode"),
        "mode_edit_inherit" to ("Wie Standard" to "Same as Standard"),
        "mode_edit_show" to ("Anzeigen" to "Show"),
        "mode_edit_hide" to ("Ausblenden" to "Hide"),
        "mode_edit_rename" to ("Diesen Modus umbenennen" to "Rename This Mode"),
        "mode_edit_delete" to ("Modus löschen" to "Delete Mode"),
        "mode_edit_delete_confirm" to ("wirklich löschen?" to "really delete?"),
        "mode_edit_rename_title" to ("Modus umbenennen" to "Rename Mode"),
        "mode_edit_standard_note" to (
            "Weitere Optionen (Akzentfarbe, Dock, Statusleiste, ausgeblendete Apps) findest du im Haupt-Einstellungsbildschirm." to
            "More options (accent color, dock, status bar, hidden apps) can be found in the main settings screen."
        ),

        // Favoriten-Auswahl
        "fav_reorder" to ("Reihenfolge anpassen" to "Adjust Order"),
        "fav_sort_az" to ("A-Z sortieren" to "Sort A-Z"),
        "fav_drag_hint" to ("Ziehen zum Sortieren" to "Drag to Reorder"),
        "fav_max_toast" to ("Maximal %d Favoriten (in den Einstellungen aenderbar)" to "Maximum %d favorites (changeable in settings)"),
        "fav_selected_of" to ("ausgewaehlt" to "selected"),
        "fav_mode_prefix" to ("Modus \"%s\": " to "Mode \"%s\": "),

        // Apps aus-/einblenden
        "visibility_hidden_count" to ("App(s) ausgeblendet" to "app(s) hidden"),
        "lock_count" to ("App(s) gesperrt/ausgeblendet" to "app(s) locked/hidden"),

        // Apps umbenennen
        "rename_hint" to ("Tippe eine App an, um sie umzubenennen" to "Tap an app to rename it"),
        "rename_reset" to ("Zuruecksetzen" to "Reset"),

        // Dock anpassen
        "dock_default" to ("Standard" to "Default"),
        "dock_empty" to ("Leer (ausgeblendet)" to "Empty (hidden)"),
        "dock_default_reset" to ("Standard (zurücksetzen)" to "Default (reset)"),
        "dock_empty_option" to ("Leer (ausblenden)" to "Empty (hide)"),
        "dock_choose_icon" to ("Icon für diesen Slot wählen" to "Choose Icon for This Slot"),
        "dock_own_icon" to ("App-eigenes Icon verwenden" to "Use App's Own Icon"),

        // Zeitlimit-Apps
        "timelimit_count" to ("App(s) mit Zeitlimit-Abfrage beim Öffnen" to "app(s) with time-limit prompt on open"),

        // Benachrichtigungen stummschalten
        "mute_count_prefix" to ("Modus \"%s\": " to "Mode \"%s\": "),
        "mute_count_suffix" to ("App(s) stummgeschaltet" to "app(s) muted"),
        "mute_access_missing" to ("Benachrichtigungszugriff fehlt noch - antippen zum Aktivieren" to "Notification access not granted yet - tap to enable"),

        // Auswahl-Screens generell
        "select_all" to ("Alle auswählen" to "Select All"),
        "select_none" to ("Keine auswählen" to "Select None"),

        // Notizen
        "notes_title" to ("Notepad" to "Notepad"),
        "notes_delete_title" to ("Notiz löschen?" to "Delete Note?"),
        "notes_delete_yes" to ("Ja" to "Yes"),
        "notes_delete_no" to ("Nein" to "No")
    )

    fun get(context: Context, key: String): String {
        val pair = table[key] ?: return key
        return if (Prefs.getAppLanguage(context) == "en") pair.second else pair.first
    }
}
