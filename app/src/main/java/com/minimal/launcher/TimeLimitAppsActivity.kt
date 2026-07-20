package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TimeLimitAppsActivity : AppCompatActivity() {

    private lateinit var selected: MutableSet<String>
    private lateinit var tvHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)

        tvHeader = findViewById(R.id.tvHeader)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        selected = Prefs.getTimeLimitedPackages(this).toMutableSet()
        val hidden = Prefs.getHiddenPackages(this)
        val apps = loadApps().filter { it.packageName !in hidden }
        updateHeader()

        val adapter = CheckableAppAdapter(
            apps,
            isChecked = { selected.contains(it) }
        ) { entry, checked ->
            if (checked) selected.add(entry.packageName) else selected.remove(entry.packageName)
            Prefs.setTimeLimitedPackages(this, selected)
            updateHeader()
            true
        }
        recyclerView.adapter = adapter

        findViewById<android.view.View>(R.id.rowSelectAll).visibility = android.view.View.VISIBLE
        findViewById<TextView>(R.id.btnSelectAll).setOnClickListener {
            selected.clear()
            selected.addAll(apps.map { it.packageName })
            Prefs.setTimeLimitedPackages(this, selected)
            updateHeader()
            adapter.notifyDataSetChanged()
        }
        findViewById<TextView>(R.id.btnSelectNone).setOnClickListener {
            selected.clear()
            Prefs.setTimeLimitedPackages(this, selected)
            updateHeader()
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateHeader() {
        tvHeader.text = "${selected.size} App(s) mit Zeitlimit-Abfrage beim Öffnen"
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
}
