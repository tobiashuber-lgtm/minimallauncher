package com.minimal.launcher

import android.content.Context

data class Palette(
    val background: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val accent: Int
)

object Palettes {
    fun current(context: Context): Palette {
        val background = ColorUtils.colorForSliderValue(Prefs.getBgColorSlider(context))
        val textPrimary = ColorUtils.colorForSliderValue(Prefs.getTextColorSlider(context))
        val accent = ColorUtils.colorForSliderValue(Prefs.getAccentColorSlider(context))
        val textSecondary = ColorUtils.secondaryFrom(textPrimary)
        return Palette(background, textPrimary, textSecondary, accent)
    }
}
