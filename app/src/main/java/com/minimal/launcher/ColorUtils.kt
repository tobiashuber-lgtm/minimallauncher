package com.minimal.launcher

import android.graphics.Color
import kotlin.math.abs

// Ein einzelner Regler (0-100) ergibt eine durchgehende Farbskala:
// 0 = Schwarz, 100 = Weiss, dazwischen ein voller Farbkreis bei fixer
// Saettigung/Helligkeit. So lassen sich mit nur einem Slider sowohl
// monochrome als auch bunte Akzentfarben einstellen.
object ColorUtils {

    fun colorForSliderValue(v: Int): Int {
        val value = v.coerceIn(0, 100)
        return when {
            value <= 10 -> lerpColor(Color.BLACK, hslColor(0f), value / 10f)
            value >= 90 -> lerpColor(hslColor(359f), Color.WHITE, (value - 90) / 10f)
            else -> hslColor((value - 10) / 80f * 360f)
        }
    }

    // Gedimmte Variante der Hauptfarbe fuer sekundaeren Text (Nutzungszeit etc.)
    fun secondaryFrom(color: Int): Int =
        Color.argb(150, Color.red(color), Color.green(color), Color.blue(color))

    private fun hslColor(hue: Float, saturation: Float = 0.6f, lightness: Float = 0.5f): Int {
        val c = (1 - abs(2 * lightness - 1)) * saturation
        val x = c * (1 - abs((hue / 60f) % 2 - 1))
        val m = lightness - c / 2
        val (r1, g1, b1) = when {
            hue < 60 -> Triple(c, x, 0f)
            hue < 120 -> Triple(x, c, 0f)
            hue < 180 -> Triple(0f, c, x)
            hue < 240 -> Triple(0f, x, c)
            hue < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color.rgb(
            (((r1 + m) * 255).toInt()).coerceIn(0, 255),
            (((g1 + m) * 255).toInt()).coerceIn(0, 255),
            (((b1 + m) * 255).toInt()).coerceIn(0, 255)
        )
    }

    private fun lerpColor(a: Int, b: Int, t: Float): Int {
        val tt = t.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * tt).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * tt).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * tt).toInt()
        )
    }
}
