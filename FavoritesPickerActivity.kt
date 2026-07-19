package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesPickerActivity : AppCompatActivity() {

    private val selected = mutableListOf<String>()
    private lateinit var tvHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)

        tvHeader = findViewById(R.id.tvHeader)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val hidden = Prefs.getHiddenPackages(this)
        val apps = loadApps().filter { it.packageName !in hidden }
        val maxCount = Prefs.getFavoritesCount(this)

        selected.addAll(Prefs.getFavoritePackages(this).filter { pkg -> apps.any { it.packageName == pkg } })
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
            Prefs.setFavoritePackages(this, selected)
            updateHeader(maxCount)
            true
        }
        recyclerView.adapter = adapter
    }

    private fun updateHeader(maxCount: Int) {
        tvHeader.text = "${selected.size}/$maxCount ausgewaehlt"
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
}
