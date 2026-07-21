package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ModeManagerActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        container = findViewById(R.id.settingsContainer)
        buildList()
    }

    override fun onResume() {
        super.onResume()
        container.removeAllViews()
        buildList()
    }

    private fun buildList() {
        addHeader("Standard-Modus")

        addRow("\"${Prefs.getStandardModeDisplayName(this)}\" umbenennen") {
            showRenameStandardDialog()
        }

        addRow("Standard-Modus bearbeiten (Favoriten, Sperren, Farbe, ...)") {
            openModeEdit("Standard")
        }

        addRow("+ Neuen Modus erstellen") { showCreateDialog() }

        val modes = Prefs.getModeNames(this)
        if (modes.isNotEmpty()) {
            addHeader("Deine Modi")
            for (mode in modes) {
                addRow(mode) { openModeEdit(mode) }
            }
        }
    }

    private fun showRenameStandardDialog() {
        val input = EditText(this).apply {
            setText(Prefs.getStandardModeDisplayName(this@ModeManagerActivity))
            setSelection(text.length)
        }
        AlertDialog.Builder(this, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle("Standard-Modus umbenennen")
            .setView(input)
            .setPositiveButton("Speichern") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) Prefs.setStandardModeDisplayName(this, name)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showCreateDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle("Name des neuen Modus")
            .setView(input)
            .setPositiveButton("Erstellen") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty() && name != "Standard" && !Prefs.getModeNames(this).contains(name)) {
                    Prefs.addMode(this, name)
                    openModeEdit(name)
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun openModeEdit(mode: String) {
        startActivity(Intent(this, ModeEditActivity::class.java).putExtra("mode", mode))
    }

    private fun addHeader(text: String) {
        val tv = TextView(this).apply {
            this.text = text
            setTextColor(0xFF666666.toInt())
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, dp(20), 0, dp(8))
        }
        container.addView(tv)
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
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1) / 2)
            setBackgroundColor(0xFF2A2A2A.toInt())
        }
        container.addView(divider)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
