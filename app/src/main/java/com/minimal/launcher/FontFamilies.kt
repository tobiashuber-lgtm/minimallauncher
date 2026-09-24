package com.minimal.launcher

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

// Nur noch die 4 gewuenschten Fonts: Space Mono / Space Mono Bold /
// Space Grotesk / Space Grotesk Bold. Beide Familien sind bei Google Fonts
// unter der SIL Open Font License 1.1 lizenziert - frei nutz-, modifizier-
// und weitergebbar (auch kommerziell, auch in einer weitergegebenen APK),
// die Lizenztexte liegen im Ordner font_licenses/.
// Key-Format: "fontKey:style" (style = normal oder bold)
object FontFamilies {
    val options = listOf(
        "space_mono:normal" to "Space Mono",
        "space_mono:bold" to "Space Mono Bold",
        "space_grotesk:normal" to "Space Grotesk",
        "space_grotesk:bold" to "Space Grotesk Bold"
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

                "space_grotesk" -> variableWeight(context, "fonts/space_grotesk_variable.ttf", if (bold) 700 else 400)

                else -> Typeface.MONOSPACE
            }
        } catch (e: Exception) {
            Typeface.MONOSPACE
        }
    }

    // Space Grotesk ist ein Variable Font (eine Datei enthaelt alle
    // Gewichte) - die gewuenschte Staerke wird per Variation-Setting
    // ("wght") ausgewaehlt.
    private fun variableWeight(context: Context, assetPath: String, weight: Int): Typeface {
        return Typeface.Builder(context.assets, assetPath)
            .setFontVariationSettings("'wght' $weight")
            .build() ?: Typeface.MONOSPACE
    }
}
