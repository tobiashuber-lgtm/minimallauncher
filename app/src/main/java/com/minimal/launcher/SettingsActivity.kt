package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Bewusst einfach gehalten: eine gruppierte Textliste statt einer
// aufwendigen UI. Texte auf diesem Hauptbildschirm sind DE/EN uebersetzt
// (siehe Strings.kt); tiefere Verwaltungs-Screens bleiben vorerst Deutsch.
class SettingsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        container = findViewById(R.id.settingsContainer)
        buildSettingsList()
    }

    override fun onResume() {
        super.onResume()
        container.removeAllViews()
        buildSettingsList()
    }

    private fun s(key: String) = Strings.get(this, key)

    private fun buildSettingsList() {
        addSectionHeader(s("section_display"))

        addCycleRow(
            s("row_font_list"),
            FontFamilies.options.map { it.first },
            FontFamilies.options.map { it.second },
            Prefs.getFontFamily(this)
        ) { Prefs.setFontFamily(this, it) }

        addCycleRow(
            s("row_font_clock"),
            FontFamilies.options.map { it.first },
            FontFamilies.options.map { it.second },
            Prefs.getClockFontFamily(this)
        ) { Prefs.setClockFontFamily(this, it) }

        addColorSliderRow(s("row_bg_color"), Prefs.getBgColorSlider(this)) {
            Prefs.setBgColorSlider(this, it)
        }

        addColorSliderRow(s("row_text_color"), Prefs.getTextColorSlider(this)) {
            Prefs.setTextColorSlider(this, it)
        }

        addColorSliderRow(s("row_accent_color"), Prefs.getAccentColorSlider(this)) {
            Prefs.setAccentColorSlider(this, it)
        }

        addCycleRow(
            s("row_clock_format"),
            listOf("24h", "12h"),
            listOf(s("val_24h"), s("val_12h")),
            if (Prefs.getClockFormat24h(this)) "24h" else "12h"
        ) { Prefs.setClockFormat24h(this, it == "24h") }

        addToggleRow(s("row_statusbar"), Prefs.getStatusBarHidden(this)) {
            Prefs.setStatusBarHidden(this, it)
        }

        addSliderRow(s("row_clock_size"), 28, 60, Prefs.getClockSizeSp(this)) {
            Prefs.setClockSizeSp(this, it)
        }

        addSliderRow(s("row_date_size"), 10, 20, Prefs.getDateSizeSp(this)) {
            Prefs.setDateSizeSp(this, it)
        }

        addSliderRow(s("row_applist_size"), 13, 24, Prefs.getAppListSizeSp(this)) {
            Prefs.setAppListSizeSp(this, it)
        }

        val dateActionValues = listOf("calendar", "new_event", "none")
        val dateActionLabels = listOf(s("val_calendar"), s("val_new_event"), s("val_none"))
        addCycleRow(s("row_date_click"), dateActionValues, dateActionLabels, Prefs.getDateClickAction(this)) {
            Prefs.setDateClickAction(this, it)
        }
        addCycleRow(s("row_date_longclick"), dateActionValues, dateActionLabels, Prefs.getDateLongClickAction(this)) {
            Prefs.setDateLongClickAction(this, it)
        }

        addCycleRow(
            s("row_language"),
            listOf("de", "en"),
            listOf(s("val_german"), s("val_english")),
            Prefs.getAppLanguage(this)
        ) { Prefs.setAppLanguage(this, it); recreate() }

        addSectionHeader(s("section_home"))

        addSliderRow(s("row_favorites_count"), 1, 10, Prefs.getFavoritesCount(this)) {
            Prefs.setFavoritesCount(this, it)
        }

        addToggleRow(s("row_dock_show"), Prefs.getDockEnabled(this)) {
            Prefs.setDockEnabled(this, it)
        }

        addButtonRow(s("row_favorites_pick")) {
            startActivity(Intent(this, FavoritesPickerActivity::class.java))
        }

        addButtonRow(s("row_dock_customize")) {
            startActivity(Intent(this, DockPickerActivity::class.java))
        }

        addSectionHeader(s("section_apps"))

        addButtonRow(s("row_visibility")) {
            startActivity(Intent(this, AppVisibilityActivity::class.java))
        }

        addButtonRow(s("row_rename")) {
            startActivity(Intent(this, AppRenameActivity::class.java))
        }

        addButtonRow(s("row_timelimit")) {
            startActivity(Intent(this, TimeLimitAppsActivity::class.java))
        }

        val overlayStatus = if (Settings.canDrawOverlays(this)) s("val_active") else s("val_inactive")
        addButtonRow("${s("row_overlay")}: $overlayStatus") {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
            )
        }

        addSectionHeader(s("section_modes"))

        val modeValues = listOf("Standard") + Prefs.getModeNames(this)
        val modeLabels = modeValues.map { if (it == "Standard") Prefs.getStandardModeDisplayName(this) else it }
        addCycleRow(s("row_active_mode"), modeValues, modeLabels, Prefs.getCurrentMode(this)) {
            Prefs.setCurrentMode(this, it)
        }

        addButtonRow(s("row_manage_modes")) {
            startActivity(Intent(this, ModeManagerActivity::class.java))
        }

        addSectionHeader(s("section_usage"))

        val statusText = if (UsageStatsHelper.hasPermission(this)) s("val_active") else s("val_inactive")
        addButtonRow("${s("row_usage_access")}: $statusText") {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        addFooter()
    }

    private fun addFooter() {
        val tv = TextView(this).apply {
            text = "GHOSTS Launcher"
            setTextColor(0xFF555555.toInt())
            textSize = 11f
            typeface = FontFamilies.buildTypeface(this@SettingsActivity, "space_mono:bold")
            gravity = Gravity.CENTER
            setPadding(0, dp(40), 0, dp(16))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.instagram.com/ghostsmakemusic/")))
            }
        }
        container.addView(tv)
    }

    private fun addSectionHeader(text: String) {
        val tv = TextView(this).apply {
            this.text = text
            setTextColor(0xFF666666.toInt())
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, dp(28), 0, dp(8))
        }
        container.addView(tv)
    }

    private fun addToggleRow(label: String, initial: Boolean, onChange: (Boolean) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }
        val tv = TextView(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val switch = Switch(this).apply {
            isChecked = initial
            setOnCheckedChangeListener { _, checked -> onChange(checked) }
        }
        row.addView(tv)
        row.addView(switch)
        container.addView(row)
        addDivider()
    }

    private fun addCycleRow(
        label: String,
        values: List<String>,
        displayNames: List<String>,
        current: String,
        onChange: (String) -> Unit
    ) {
        var index = values.indexOf(current).coerceAtLeast(0)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(14), 0, dp(14))
        }
        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvValue = TextView(this).apply {
            text = displayNames[index]
            setTextColor(0xFF888888.toInt())
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        row.setOnClickListener {
            index = (index + 1) % values.size
            tvValue.text = displayNames[index]
            onChange(values[index])
        }
        row.addView(tvLabel)
        row.addView(tvValue)
        container.addView(row)
        addDivider()
    }

    private fun addSliderRow(label: String, min: Int, max: Int, initial: Int, onChange: (Int) -> Unit) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(14), 0, dp(14))
        }
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvValue = TextView(this).apply {
            text = initial.toString()
            setTextColor(0xFF888888.toInt())
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        headerRow.addView(tvLabel)
        headerRow.addView(tvValue)

        val seekBar = SeekBar(this).apply {
            this.max = max - min
            progress = (initial - min).coerceIn(0, max - min)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = progress + min
                    tvValue.text = value.toString()
                    if (fromUser) onChange(value)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        wrapper.addView(headerRow)
        wrapper.addView(seekBar)
        container.addView(wrapper)
        addDivider()
    }

    // Wie addSliderRow, aber mit einer kleinen live mitlaufenden Farbvorschau
    // direkt neben dem Wert - fuer Hintergrund-/Schrift-/Akzentfarbe.
    private fun addColorSliderRow(label: String, initial: Int, onChange: (Int) -> Unit) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(14), 0, dp(14))
        }
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val swatch = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(20), dp(20)).apply { marginEnd = dp(10) }
            setBackgroundColor(ColorUtils.colorForSliderValue(initial))
        }
        val tvValue = TextView(this).apply {
            text = initial.toString()
            setTextColor(0xFF888888.toInt())
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        headerRow.addView(tvLabel)
        headerRow.addView(swatch)
        headerRow.addView(tvValue)

        val seekBar = SeekBar(this).apply {
            this.max = 100
            progress = initial.coerceIn(0, 100)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvValue.text = progress.toString()
                    swatch.setBackgroundColor(ColorUtils.colorForSliderValue(progress))
                    if (fromUser) onChange(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        wrapper.addView(headerRow)
        wrapper.addView(seekBar)
        container.addView(wrapper)
        addDivider()
    }

    private fun addButtonRow(label: String, onClick: () -> Unit) {
        val tv = TextView(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, dp(14), 0, dp(14))
            setOnClickListener { onClick() }
        }
        container.addView(tv)
        addDivider()
    }

    private fun addDivider() {
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1) / 2)
            setBackgroundColor(0xFF2A2A2A.toInt())
        }
        container.addView(divider)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
