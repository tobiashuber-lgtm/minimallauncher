package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DockPickerActivity : AppCompatActivity() {

    private val slotLabels = listOf("Slot 1 (links)", "Slot 2", "Slot 3", "Slot 4 (rechts)")
    private lateinit var apps: List<AppEntry>
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dock_picker)
        container = findViewById(R.id.dockSlotsContainer)

        val hidden = Prefs.getHiddenPackages(this)
        apps = loadApps().filter { it.packageName !in hidden }

        buildRows()
    }

    private fun buildRows() {
        container.removeAllViews()
        for (i in slotLabels.indices) {
            val currentPkg = Prefs.getDockPackage(this, i)
            val currentLabel = when (currentPkg) {
                null -> "Standard"
                "__EMPTY__" -> "Leer (ausgeblendet)"
                else -> apps.firstOrNull { it.packageName == currentPkg }?.label ?: "Standard"
            }

            val row = TextView(this).apply {
                text = "${slotLabels[i]}: $currentLabel"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 15f
                typeface = android.graphics.Typeface.MONOSPACE
                setPadding(0, dp(14), 0, dp(14))
                setOnClickListener { showPicker(i) }
            }
            container.addView(row)

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1) / 2)
                setBackgroundColor(0xFF2A2A2A.toInt())
            }
            container.addView(divider)
        }
    }

    private fun showPicker(slot: Int) {
        val options = listOf("Standard (zurücksetzen)", "Leer (ausblenden)") + apps.map { it.label }
        AlertDialog.Builder(this, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle(slotLabels[slot])
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> Prefs.setDockPackage(this, slot, null)
                    1 -> Prefs.setDockPackage(this, slot, "__EMPTY__")
                    else -> Prefs.setDockPackage(this, slot, apps[which - 2].packageName)
                }
                buildRows()
            }
            .show()
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
