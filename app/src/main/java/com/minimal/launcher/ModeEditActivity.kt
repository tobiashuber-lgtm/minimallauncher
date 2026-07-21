package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ModeEditActivity : AppCompatActivity() {

    private lateinit var mode: String
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        mode = intent.getStringExtra("mode") ?: run { finish(); return }
        container = findViewById(R.id.settingsContainer)
        buildList()
    }

    override fun onResume() {
        super.onResume()
        container.removeAllViews()
        buildList()
    }

    private fun buildList() {
        val displayName = if (mode == "Standard") Prefs.getStandardModeDisplayName(this) else mode
        val header = TextView(this).apply {
            text = "Modus: $displayName"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, dp(8), 0, dp(24))
        }
        container.addView(header)

        if (mode == "Standard") {
            // Standard hat schon eigene globale Einstellungen (Akzentfarbe, Dock,
            // Statusleiste, gesperrte Apps) im Haupt-Einstellungsbildschirm -
            // hier nur die Favoriten-Verwaltung (inkl. Reihenfolge) als Komfort-
            // Zugang, ohne eine zweite, widerspruechliche Datenquelle zu erzeugen.
            addRow("Favoriten auswählen") {
                startActivity(Intent(this, FavoritesPickerActivity::class.java))
            }
            val note = TextView(this).apply {
                text = "Weitere Optionen (Akzentfarbe, Dock, Statusleiste, ausgeblendete Apps) findest du im Haupt-Einstellungsbildschirm."
                setTextColor(0xFF666666.toInt())
                textSize = 12f
                typeface = android.graphics.Typeface.MONOSPACE
                setPadding(0, dp(20), 0, dp(8))
            }
            container.addView(note)
            return
        }

        addRow("Gesperrte/ausgeblendete Apps für diesen Modus wählen") {
            startActivity(Intent(this, ModeLockActivity::class.java).putExtra("mode", mode))
        }
        addRow("Favoriten für diesen Modus wählen") {
            startActivity(Intent(this, FavoritesPickerActivity::class.java).putExtra("mode", mode))
        }
        addRow("Benachrichtigungen stummschalten für diesen Modus") {
            startActivity(Intent(this, ModeNotificationMuteActivity::class.java).putExtra("mode", mode))
        }

        addColorSliderRow(
            "Akzentfarbe (Uhr) - eigene Einstellung",
            Prefs.getModeAccentColorSlider(this, mode) ?: Prefs.getAccentColorSlider(this)
        ) { Prefs.setModeAccentColorSlider(this, mode, it) }

        addCycleRow(
            "Dock in diesem Modus",
            listOf("inherit", "on", "off"),
            listOf("Wie Standard", "Anzeigen", "Ausblenden"),
            when (Prefs.getModeDockEnabled(this, mode)) {
                true -> "on"; false -> "off"; null -> "inherit"
            }
        ) {
            Prefs.setModeDockEnabled(this, mode, when (it) { "on" -> true; "off" -> false; else -> null })
        }

        addCycleRow(
            "Statusleiste in diesem Modus",
            listOf("inherit", "on", "off"),
            listOf("Wie Standard", "Ausblenden", "Anzeigen"),
            when (Prefs.getModeStatusBarHidden(this, mode)) {
                true -> "on"; false -> "off"; null -> "inherit"
            }
        ) {
            Prefs.setModeStatusBarHidden(this, mode, when (it) { "on" -> true; "off" -> false; else -> null })
        }

        if (mode != "Standard") {
            addRow("Diesen Modus umbenennen") { showRenameDialog() }

            addRow("Modus löschen") {
                AlertDialog.Builder(this, R.style.Theme_MinimalLauncher_Dialog)
                    .setTitle("\"$mode\" wirklich löschen?")
                    .setPositiveButton("Löschen") { _, _ ->
                        Prefs.removeMode(this, mode)
                        finish()
                    }
                    .setNegativeButton("Abbrechen", null)
                    .show()
            }
        }
    }

    private fun showRenameDialog() {
        val input = android.widget.EditText(this).apply {
            setText(mode)
            setSelection(text.length)
        }
        AlertDialog.Builder(this, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle("Modus umbenennen")
            .setView(input)
            .setPositiveButton("Speichern") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != mode && !Prefs.getModeNames(this).contains(newName)) {
                    Prefs.renameMode(this, mode, newName)
                    mode = newName
                    container.removeAllViews()
                    buildList()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun addRow(label: String, onClick: () -> Unit) {
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

    private fun addCycleRow(
        label: String, values: List<String>, displayNames: List<String>,
        current: String, onChange: (String) -> Unit
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

    private fun addDivider() {
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1) / 2)
            setBackgroundColor(0xFF2A2A2A.toInt())
        }
        container.addView(divider)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
