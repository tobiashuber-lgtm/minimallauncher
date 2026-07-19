package com.minimal.launcher

import android.graphics.Typeface

// System-Schriftfamilien als Naeherung an die gewuenschten Stile (Mono,
// fette Kondensierte/Grotesk wie im Moodboard). Echte Custom-Fonts (z.B.
// Space Mono, Neue Montreal) wuerden eigene .ttf-Dateien im Projekt
// erfordern - das kann spaeter ergaenzt werden, wenn gewuenscht.
// Key-Format: "fontFamilyName:style" (style = normal oder bold)
object FontFamilies {
    val options = listOf(
        "monospace:normal" to "Mono",
        "monospace:bold" to "Mono Bold",
        "sans-serif-condensed:bold" to "Kondensiert Bold",
        "sans-serif-black:normal" to "Grotesk Black",
        "serif:normal" to "Serif"
    )

    fun labelFor(key: String): String =
        options.firstOrNull { it.first == key }?.second ?: "Mono"

    fun buildTypeface(key: String): Typeface {
        val parts = key.split(":")
        val family = parts.getOrElse(0) { "monospace" }
        val style = if (parts.getOrNull(1) == "bold") Typeface.BOLD else Typeface.NORMAL
        return Typeface.create(family, style)
    }
}
