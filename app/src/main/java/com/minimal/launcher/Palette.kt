package com.minimal.launcher

// Drei einfache Farbschemata. "accent" faerbt nur die Uhrzeit ein,
// der Rest bleibt wie beim monochromen Dark-Schema.
data class Palette(
    val background: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val accent: Int
)

object Palettes {

    val monoDark = Palette(
        background = 0xFF000000.toInt(),
        textPrimary = 0xFFFFFFFF.toInt(),
        textSecondary = 0xFF999999.toInt(),
        accent = 0xFFFFFFFF.toInt()
    )

    val monoLight = Palette(
        background = 0xFFFFFFFF.toInt(),
        textPrimary = 0xFF000000.toInt(),
        textSecondary = 0xFF555555.toInt(),
        accent = 0xFF000000.toInt()
    )

    val accentRed = Palette(
        background = 0xFF000000.toInt(),
        textPrimary = 0xFFFFFFFF.toInt(),
        textSecondary = 0xFF999999.toInt(),
        accent = 0xFFE04B3C.toInt()
    )

    fun forName(name: String): Palette = when (name) {
        "mono_light" -> monoLight
        "accent" -> accentRed
        else -> monoDark
    }
}
