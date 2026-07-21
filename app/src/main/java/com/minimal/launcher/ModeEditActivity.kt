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

    private fun s(key: String) = Strings.get(this, key)

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
            text = "${s("mode_edit_title_prefix")}$displayName"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, dp(8), 0, dp(24))
        }
        container.addView(header)

        if (mode == "Standard") {
            addRow(s("row_favorites_pick")) {
                startActivity(Intent(this, FavoritesPickerActivity::class.java))
            }
            val note = TextView(this).apply {
                text = s("mode_edit_standard_note")
                setTextColor(0xFF666666.toInt())
                textSize = 12f
                typeface = android.graphics.Typeface.MONOSPACE
                setPadding(0, dp(20), 0, dp(8))
            }
            container.addView(note)
            return
        }

        addRow(s("mode_edit_lock_apps")) {
            startActivity(Intent(this, ModeLockActivity::class.java).putExtra("mode", mode))
        }
        addRow(s("mode_edit_favorites")) {
            startActivity(Intent(this, FavoritesPickerActivity::class.java).putExtra("mode", mode))
        }
        addRow(s("mode_edit_mute")) {
            startActivity(Intent(this, ModeNotificationMuteActivity::class.java).putExtra("mode", mode))
        }

        addColorSliderRow(
            s("mode_edit_accent"),
            Prefs.getModeAccentColorSlider(this, mode) ?: Prefs.getAccentColorSlider(this)
        ) { Prefs.setModeAccentColorSlider(this, mode, it) }

        addCycleRow(
            s("mode_edit_dock"),
            listOf("inherit", "on", "off"),
            listOf(s("mode_edit_inherit"), s("mode_edit_show"), s("mode_edit_hide")),
            when (Prefs.getModeDockEnabled(this, mode)) {
                true -> "on"; false -> "off"; null -> "inherit"
            }
        ) {
            Prefs.setModeDockEnabled(this, mode, when (it) { "on" -> true; "off" -> false; else -> null })
        }

        addCycleRow(
            s("mode_edit_statusbar"),
            listOf("inherit", "on", "off"),
            listOf(s("mode_edit_inherit"), s("mode_edit_hide"), s("mode_edit_show")),
            when (Prefs.getModeStatusBarHidden(this, mode)) {
                true -> "on"; false -> "off"; null -> "inherit"
            }
        ) {
            Prefs.setModeStatusBarHidden(this, mode, when (it) { "on" -> true; "off" -> false; else -> null })
        }

        addRow(s("mode_edit_rename")) { showRenameDialog() }

        addRow(s("mode_edit_delete")) {
            AlertDialog.Builder(this, R.style.Theme_MinimalLauncher_Dialog)
                .setTitle("\"$mode\" ${s("mode_edit_delete_confirm")}")
                .setPositiveButton(s("mode_edit_delete")) { _, _ ->
                    Prefs.removeMode(this, mode)
                    finish()
                }
                .setNegativeButton(s("modes_cancel"), null)
                .show()
        }
    }

    private fun showRenameDialog() {
        val input = android.widget.EditText(this).apply {
            setText(mode)
            setSelection(text.length)
        }
        AlertDialog.Builder(this, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle(s("mode_edit_rename_title"))
            .setView(input)
            .setPositiveButton(s("modes_save")) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != mode && !Prefs.getModeNames(this).contains(newName)) {
                    Prefs.renameMode(this, mode, newName)
                    mode = newName
                    container.removeAllViews()
                    buildList()
                }
            }
            .setNegativeButton(s("modes_cancel"), null)
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
