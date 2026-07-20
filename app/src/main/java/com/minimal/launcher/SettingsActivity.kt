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
// aufwendigen UI, wie besprochen. Alles wird direkt in Prefs gespeichert
// und beim naechsten Aufruf von Home/Drawer automatisch verwendet.
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
        // Nutzungszugriff-Status koennte sich in der Zwischenzeit geaendert haben.
        container.removeAllViews()
        buildSettingsList()
    }

    private fun buildSettingsList() {
        addSectionHeader("Darstellung")

        addCycleRow(
            "Schriftart (App-Liste)",
            FontFamilies.options.map { it.first },
            FontFamilies.options.map { it.second },
            Prefs.getFontFamily(this)
        ) { Prefs.setFontFamily(this, it) }

        addCycleRow(
            "Schriftart Uhr",
            FontFamilies.options.map { it.first },
            FontFamilies.options.map { it.second },
            Prefs.getClockFontFamily(this)
        ) { Prefs.setClockFontFamily(this, it) }

        addSliderRow("Hintergrundfarbe", 0, 100, Prefs.getBgColorSlider(this)) {
            Prefs.setBgColorSlider(this, it)
        }

        addSliderRow("Schriftfarbe", 0, 100, Prefs.getTextColorSlider(this)) {
            Prefs.setTextColorSlider(this, it)
        }

        addSliderRow("Akzentfarbe (Uhr)", 0, 100, Prefs.getAccentColorSlider(this)) {
            Prefs.setAccentColorSlider(this, it)
        }

        addCycleRow(
            "Uhr-Format",
            listOf("24h", "12h"),
            listOf("24-Stunden", "12-Stunden"),
            if (Prefs.getClockFormat24h(this)) "24h" else "12h"
        ) { Prefs.setClockFormat24h(this, it == "24h") }

        addToggleRow("Statusleiste ausblenden", Prefs.getStatusBarHidden(this)) {
            Prefs.setStatusBarHidden(this, it)
        }

        addSliderRow("Schriftgröße Uhr", 28, 60, Prefs.getClockSizeSp(this)) {
            Prefs.setClockSizeSp(this, it)
        }

        addSliderRow("Schriftgröße Datum", 10, 20, Prefs.getDateSizeSp(this)) {
            Prefs.setDateSizeSp(this, it)
        }

        addSliderRow("Schriftgröße App-Liste", 13, 24, Prefs.getAppListSizeSp(this)) {
            Prefs.setAppListSizeSp(this, it)
        }

        addSectionHeader("Home-Screen")

        addSliderRow("Anzahl Favoriten", 1, 10, Prefs.getFavoritesCount(this)) {
            Prefs.setFavoritesCount(this, it)
        }

        addToggleRow("Dock anzeigen", Prefs.getDockEnabled(this)) {
            Prefs.setDockEnabled(this, it)
        }

        addButtonRow("Favoriten auswählen") {
            startActivity(Intent(this, FavoritesPickerActivity::class.java))
        }

        addButtonRow("Dock anpassen") {
            startActivity(Intent(this, DockPickerActivity::class.java))
        }

        addSectionHeader("Apps")

        addButtonRow("Apps aus-/einblenden") {
            startActivity(Intent(this, AppVisibilityActivity::class.java))
        }

        addButtonRow("Apps umbenennen") {
            startActivity(Intent(this, AppRenameActivity::class.java))
        }

        addButtonRow("Apps mit Zeitlimit auswählen") {
            startActivity(Intent(this, TimeLimitAppsActivity::class.java))
        }

        val overlayStatus = if (android.provider.Settings.canDrawOverlays(this)) "aktiv" else "nicht aktiviert"
        addButtonRow("Countdown-Anzeige beim Zeitlimit: $overlayStatus") {
            startActivity(
                Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
            )
        }

        addSectionHeader("Modi")

        val modeValues = listOf("Standard") + Prefs.getModeNames(this)
        val modeLabels = modeValues.map { if (it == "Standard") Prefs.getStandardModeDisplayName(this) else it }
        addCycleRow(
            "Aktiver Modus",
            modeValues,
            modeLabels,
            Prefs.getCurrentMode(this)
        ) { Prefs.setCurrentMode(this, it) }

        addButtonRow("Modi verwalten (erstellen/bearbeiten/löschen)") {
            startActivity(Intent(this, ModeManagerActivity::class.java))
        }

        addSectionHeader("Nutzungszeit")

        val statusText = if (UsageStatsHelper.hasPermission(this)) "aktiv" else "nicht aktiviert"
        addButtonRow("Nutzungszugriff: $statusText") {
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
