package com.minimal.launcher

// System-Schriftfamilien als Naeherung an "Nothing-artige" Stile.
// Echte Custom-Fonts (z.B. Space Mono) koennten spaeter als .ttf-Dateien
// im Projekt ergaenzt werden - das wuerde aber Font-Dateien erfordern,
// die hier nicht automatisch beschafft werden koennen.
object FontFamilies {
    val options = listOf(
        "monospace" to "Mono",
        "sans-serif-condensed" to "Kondensiert",
        "sans-serif-light" to "Grotesk",
        "serif" to "Serif"
    )

    fun labelFor(key: String): String = options.firstOrNull { it.first == key }?.second ?: "Mono"
}
