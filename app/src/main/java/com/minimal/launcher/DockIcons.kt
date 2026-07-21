package com.minimal.launcher

// Zehn einfache Mono-Style-Icons zur freien Auswahl pro Dock-Slot,
// unabhaengig davon, welche App diesem Slot zugewiesen ist.
object DockIcons {
    val options = listOf(
        "phone" to Pair("Telefon", R.drawable.ic_dock_phone),
        "camera" to Pair("Kamera", R.drawable.ic_dock_camera),
        "message" to Pair("Nachrichten", R.drawable.ic_dock_message),
        "mail" to Pair("Mail", R.drawable.ic_dock_mail),
        "flashlight" to Pair("Taschenlampe", R.drawable.ic_dock_flashlight),
        "music" to Pair("Musik", R.drawable.ic_dock_music),
        "search" to Pair("Suche", R.drawable.ic_dock_search),
        "star" to Pair("Stern", R.drawable.ic_dock_star),
        "settings" to Pair("Einstellungen", R.drawable.ic_dock_settings),
        "calendar" to Pair("Kalender", R.drawable.ic_dock_calendar)
    )

    fun drawableFor(key: String): Int = options.firstOrNull { it.first == key }?.second?.second ?: R.drawable.ic_dock_star

    fun labelFor(key: String): String = options.firstOrNull { it.first == key }?.second?.first ?: "Icon"
}
