package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Wird sowohl fuer die globalen Favoriten (kein "mode"-Extra) als auch fuer
// modusspezifische Favoriten verwendet (Intent-Extra "mode" gesetzt).
class FavoritesPickerActivity : AppCompatActivity() {

    private val selected = mutableListOf<String>()
    private lateinit var tvHeader: TextView
    private var mode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)
        mode = intent.getStringExtra("mode")

        tvHeader = findViewById(R.id.tvHeader)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val excluded = Prefs.getHiddenPackages(this) + (mode?.let { Prefs.getModeLockedPackages(this, it) } ?: emptySet())
        val apps = loadApps().filter { it.packageName !in excluded }
        val maxCount = Prefs.getFavoritesCount(this)

        selected.addAll(Prefs.getFavoritePackages(this, mode).filter { pkg -> apps.any { it.packageName == pkg } })
        updateHeader(maxCount)

        val adapter = CheckableAppAdapter(
            apps,
            isChecked = { selected.contains(it) }
        ) { entry, checked ->
            if (checked) {
                if (selected.size >= maxCount) {
                    Toast.makeText(
                        this,
                        "Maximal $maxCount Favoriten (in den Einstellungen aenderbar)",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@CheckableAppAdapter false
                }
                selected.add(entry.packageName)
            } else {
                selected.remove(entry.packageName)
            }
            Prefs.setFavoritePackages(this, selected, mode)
            updateHeader(maxCount)
            true
        }
        recyclerView.adapter = adapter
    }

    private fun updateHeader(maxCount: Int) {
        val prefix = mode?.let { "Modus \"$it\": " } ?: ""
        tvHeader.text = "$prefix${selected.size}/$maxCount ausgewaehlt"
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
}
