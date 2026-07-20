package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ModeLockActivity : AppCompatActivity() {

    private lateinit var locked: MutableSet<String>
    private lateinit var tvHeader: TextView
    private lateinit var mode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)
        mode = intent.getStringExtra("mode") ?: run { finish(); return }

        tvHeader = findViewById(R.id.tvHeader)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        locked = Prefs.getModeLockedPackages(this, mode).toMutableSet()
        val globalHidden = Prefs.getHiddenPackages(this)
        val apps = loadApps().filter { it.packageName !in globalHidden }
        updateHeader()

        val adapter = CheckableAppAdapter(
            apps,
            isChecked = { locked.contains(it) }
        ) { entry, checked ->
            if (checked) locked.add(entry.packageName) else locked.remove(entry.packageName)
            Prefs.setModeLockedPackages(this, mode, locked)
            updateHeader()
            true
        }
        recyclerView.adapter = adapter

        findViewById<android.view.View>(R.id.rowSelectAll).visibility = android.view.View.VISIBLE
        findViewById<TextView>(R.id.btnSelectAll).setOnClickListener {
            locked.clear()
            locked.addAll(apps.map { it.packageName })
            Prefs.setModeLockedPackages(this, mode, locked)
            updateHeader()
            adapter.notifyDataSetChanged()
        }
        findViewById<TextView>(R.id.btnSelectNone).setOnClickListener {
            locked.clear()
            Prefs.setModeLockedPackages(this, mode, locked)
            updateHeader()
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateHeader() {
        tvHeader.text = "Modus \"$mode\": ${locked.size} App(s) gesperrt/ausgeblendet"
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
}
