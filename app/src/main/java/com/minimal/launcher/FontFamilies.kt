package com.minimal.launcher

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

// Echte Font-Dateien (Google Fonts, OFL-Lizenz), passend zum gewuenschten
// technischen/kondensierten Look. Anton, Bebas Neue und Archivo Black gibt
// es kostenlos nur in einer Gewichtung (von Haus aus bereits sehr fett) -
// dafuer gibt es keine separate Bold-Variante.
// Key-Format: "fontKey:style" (style = normal oder bold)
object FontFamilies {
    val options = listOf(
        "space_mono:normal" to "Space Mono",
        "space_mono:bold" to "Space Mono Bold",
        "jetbrains_mono:normal" to "JetBrains Mono",
        "jetbrains_mono:bold" to "JetBrains Mono Bold",
        "space_grotesk:normal" to "Space Grotesk",
        "space_grotesk:bold" to "Space Grotesk Bold",
        "anton:normal" to "Anton",
        "bebas_neue:normal" to "Bebas Neue",
        "archivo_black:normal" to "Archivo Black"
    )

    fun labelFor(key: String): String =
        options.firstOrNull { it.first == key }?.second ?: "Space Mono"

    fun buildTypeface(context: Context, key: String): Typeface {
        val parts = key.split(":")
        val family = parts.getOrElse(0) { "space_mono" }
        val bold = parts.getOrNull(1) == "bold"

        return try {
            when (family) {
                "space_mono" -> ResourcesCompat.getFont(
                    context, if (bold) R.font.space_mono_bold else R.font.space_mono_regular
                ) ?: Typeface.MONOSPACE

                "jetbrains_mono" -> variableWeight(context, R.font.jetbrains_mono_variable, if (bold) 700 else 400)

                "space_grotesk" -> variableWeight(context, R.font.space_grotesk_variable, if (bold) 700 else 400)

                "anton" -> ResourcesCompat.getFont(context, R.font.anton_regular) ?: Typeface.DEFAULT_BOLD

                "bebas_neue" -> ResourcesCompat.getFont(context, R.font.bebas_neue_regular) ?: Typeface.DEFAULT_BOLD

                "archivo_black" -> ResourcesCompat.getFont(context, R.font.archivo_black_regular) ?: Typeface.DEFAULT_BOLD

                else -> Typeface.MONOSPACE
            }
        } catch (e: Exception) {
            Typeface.MONOSPACE
        }
    }

    // Variable Fonts (JetBrains Mono, Space Grotesk) enthalten alle Gewichte
    // in einer Datei - die gewuenschte Staerke wird per Variation-Setting
    // ("wght") ausgewaehlt statt eine eigene Datei pro Gewicht zu brauchen.
    private fun variableWeight(context: Context, fontResId: Int, weight: Int): Typeface {
        return Typeface.Builder(context, fontResId)
            .setFontVariationSettings("'wght' $weight")
            .build() ?: Typeface.MONOSPACE
    }
}
