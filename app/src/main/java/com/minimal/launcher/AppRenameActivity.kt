package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppRenameActivity : AppCompatActivity() {

    private lateinit var adapter: RenameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)

        findViewById<TextView>(R.id.tvHeader).text = "Tippe eine App an, um sie umzubenennen"

        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val apps = loadApps()
        adapter = RenameAdapter(
            apps,
            displayNameFor = { Prefs.displayNameFor(this, it.packageName, it.label) }
        ) { entry -> showRenameDialog(entry) }
        recyclerView.adapter = adapter
    }

    private fun showRenameDialog(entry: AppEntry) {
        val input = EditText(this).apply {
            setText(Prefs.getRename(this@AppRenameActivity, entry.packageName) ?: entry.label)
            setSelection(text.length)
        }
        AlertDialog.Builder(this)
            .setTitle(entry.label)
            .setView(input)
            .setPositiveButton("Speichern") { _, _ ->
                val newName = input.text.toString().trim()
                Prefs.setRename(this, entry.packageName, if (newName == entry.label) null else newName)
                adapter.notifyRenamed()
            }
            .setNeutralButton("Zuruecksetzen") { _, _ ->
                Prefs.setRename(this, entry.packageName, null)
                adapter.notifyRenamed()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
}
