package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
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

    private fun buildList() {
        val header = TextView(this).apply {
            text = "Modus: $mode"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, dp(8), 0, dp(24))
        }
        container.addView(header)

        addRow("Gesperrte/ausgeblendete Apps für diesen Modus wählen") {
            startActivity(Intent(this, ModeLockActivity::class.java).putExtra("mode", mode))
        }
        addRow("Favoriten für diesen Modus wählen") {
            startActivity(Intent(this, FavoritesPickerActivity::class.java).putExtra("mode", mode))
        }
        addRow("Modus löschen") {
            AlertDialog.Builder(this)
                .setTitle("\"$mode\" wirklich löschen?")
                .setPositiveButton("Löschen") { _, _ ->
                    Prefs.removeMode(this, mode)
                    finish()
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }
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
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
